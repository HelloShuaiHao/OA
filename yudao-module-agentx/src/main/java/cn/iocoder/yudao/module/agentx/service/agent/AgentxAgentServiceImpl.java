package cn.iocoder.yudao.module.agentx.service.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.agentx.controller.admin.agent.vo.AgentxAgentDetailRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.agent.vo.AgentxAgentPageReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.agent.vo.AgentxAgentRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.agent.vo.AgentxAgentSaveReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentCapabilityDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentConfigVersionDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentProcessDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.agent.AgentxAgentCapabilityMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.agent.AgentxAgentConfigVersionMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.agent.AgentxAgentMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.agent.AgentxAgentProcessMapper;
import cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.agentx.dal.dataobject.instance.AgentxOpenfangInstanceDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.instance.AgentxOpenfangInstanceMapper;
import cn.iocoder.yudao.module.agentx.service.instance.OpenfangApiKeyCrypto;
import cn.iocoder.yudao.module.agentx.service.metrics.AgentxMetricsService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Service
@Validated
public class AgentxAgentServiceImpl implements AgentxAgentService {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_DISABLED = 2;
    private static final int OPENFANG_SYNC_MAX_ATTEMPTS = 3;
    private static final long OPENFANG_SYNC_BASE_BACKOFF_MILLIS = 200L;

    @Resource
    private AgentxAgentMapper agentMapper;
    @Resource
    private AgentxAgentProcessMapper processMapper;
    @Resource
    private AgentxAgentCapabilityMapper capabilityMapper;
    @Resource
    private AgentxAgentConfigVersionMapper configVersionMapper;
    @Resource
    private AgentxOpenfangInstanceMapper openfangInstanceMapper;
    @Resource
    private OpenfangApiKeyCrypto openfangApiKeyCrypto;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private AgentxMetricsService metricsService;
    @Resource
    private DeptApi deptApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAgent(AgentxAgentSaveReqVO reqVO, boolean publish) {
        validateAgentNameUnique(null, reqVO.getAgentName());
        validatePublishRequired(reqVO, publish);

        AgentxAgentDO agent = new AgentxAgentDO();
        agent.setAgentName(reqVO.getAgentName());
        agent.setAgentKey(generateAgentKey());
        agent.setDescription(reqVO.getDescription());
        agent.setAvatarUrl(reqVO.getAvatarUrl());
        agent.setDeptId(reqVO.getDeptId());
        agent.setDeptName(resolveDeptName(reqVO.getDeptId()));
        agent.setTemplateType(reqVO.getTemplateType());
        agent.setStatus(publish ? STATUS_ACTIVE : STATUS_DRAFT);
        agentMapper.insert(agent);

        replaceCapabilities(agent.getId(), reqVO.getCapabilities());
        replaceProcesses(agent.getId(), reqVO.getProcesses(), reqVO.getSelectionMode(), reqVO.getSelectionRules());
        if (publish) {
            syncAgentUserAndConfig(agent.getId(), "create-publish");
        }
        return agent.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAgent(AgentxAgentSaveReqVO reqVO) {
        validateAgentExists(reqVO.getId());
        validateAgentNameUnique(reqVO.getId(), reqVO.getAgentName());

        AgentxAgentDO updateObj = new AgentxAgentDO();
        updateObj.setId(reqVO.getId());
        updateObj.setAgentName(reqVO.getAgentName());
        updateObj.setDescription(reqVO.getDescription());
        updateObj.setAvatarUrl(reqVO.getAvatarUrl());
        updateObj.setDeptId(reqVO.getDeptId());
        updateObj.setDeptName(resolveDeptName(reqVO.getDeptId()));
        updateObj.setTemplateType(reqVO.getTemplateType());
        agentMapper.updateById(updateObj);

        replaceCapabilities(reqVO.getId(), reqVO.getCapabilities());
        replaceProcesses(reqVO.getId(), reqVO.getProcesses(), reqVO.getSelectionMode(), reqVO.getSelectionRules());
        AgentxAgentDO agent = agentMapper.selectById(reqVO.getId());
        if (agent != null && ObjectUtil.equal(agent.getStatus(), STATUS_ACTIVE)) {
            syncAgentUserAndConfig(agent.getId(), "update");
        }
    }

    @Override
    public void updateAgentStatus(Long id, Integer status) {
        validateAgentExists(id);
        if (!ObjectUtil.equal(status, STATUS_ACTIVE) && !ObjectUtil.equal(status, STATUS_DISABLED)) {
            throw exception(ErrorCodeConstants.AGENT_STATUS_INVALID);
        }
        agentMapper.updateById(new AgentxAgentDO().setId(id).setStatus(status));
        syncAgentUserAndConfig(id, "status-change");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAgent(Long id) {
        validateAgentExists(id);
        agentMapper.deleteById(id);
        capabilityMapper.deleteByAgentId(id);
        processMapper.deleteByAgentId(id);
    }

    @Override
    public AgentxAgentDetailRespVO getAgent(Long id) {
        AgentxAgentDO agent = agentMapper.selectById(id);
        if (agent == null) {
            throw exception(ErrorCodeConstants.AGENT_NOT_EXISTS);
        }

        List<AgentxAgentCapabilityDO> capabilityList = capabilityMapper.selectListByAgentId(id);
        List<AgentxAgentProcessDO> processList = processMapper.selectListByAgentId(id);

        AgentxAgentDetailRespVO respVO = BeanUtils.toBean(agent, AgentxAgentDetailRespVO.class);
        respVO.setCapabilities(convertList(capabilityList, item -> {
            AgentxAgentDetailRespVO.CapabilityItem vo = new AgentxAgentDetailRespVO.CapabilityItem();
            vo.setCapabilityKey(item.getCapabilityKey());
            vo.setCapabilityName(item.getCapabilityName());
            vo.setEnabled(item.getEnabled());
            vo.setMaxCallsPerHour(item.getMaxCallsPerHour());
            vo.setConditions(item.getConditions());
            return vo;
        }));
        respVO.setProcesses(convertList(processList, item -> {
            AgentxAgentDetailRespVO.ProcessItem vo = new AgentxAgentDetailRespVO.ProcessItem();
            vo.setProcessDefinitionId(item.getProcessDefinitionId());
            vo.setProcessDefinitionKey(item.getProcessDefinitionKey());
            vo.setProcessName(item.getProcessName());
            vo.setProcessVersion(item.getProcessVersion());
            vo.setPriority(item.getPriority());
            return vo;
        }));
        if (CollUtil.isNotEmpty(processList)) {
            AgentxAgentProcessDO first = processList.get(0);
            respVO.setSelectionMode(first.getSelectionMode());
            List<AgentxAgentDetailRespVO.SelectionRuleItem> rules = JsonUtils.parseArray(
                    first.getSelectionRules(), AgentxAgentDetailRespVO.SelectionRuleItem.class);
            respVO.setSelectionRules(rules == null ? Collections.emptyList() : rules);
        } else {
            respVO.setSelectionMode("rule");
            respVO.setSelectionRules(Collections.emptyList());
        }
        return respVO;
    }

    @Override
    public PageResult<AgentxAgentRespVO> getAgentPage(AgentxAgentPageReqVO pageReqVO) {
        PageResult<AgentxAgentDO> pageResult = agentMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(pageResult, AgentxAgentRespVO.class);
    }

    private void replaceCapabilities(Long agentId, List<AgentxAgentSaveReqVO.CapabilityItem> capabilities) {
        capabilityMapper.deleteByAgentId(agentId);
        if (CollUtil.isEmpty(capabilities)) {
            return;
        }
        capabilityMapper.insertBatch(convertList(capabilities, item -> new AgentxAgentCapabilityDO()
                .setAgentId(agentId)
                .setCapabilityKey(StrUtil.blankToDefault(item.getCapabilityKey(), item.getCapabilityName()))
                .setCapabilityName(item.getCapabilityName())
                .setEnabled(item.getEnabled() == null ? Boolean.TRUE : item.getEnabled())
                .setMaxCallsPerHour(item.getMaxCallsPerHour())
                .setConditions(item.getConditions())));
    }

    private void replaceProcesses(Long agentId,
                                  List<AgentxAgentSaveReqVO.ProcessItem> processes,
                                  String selectionMode,
                                  List<AgentxAgentSaveReqVO.SelectionRuleItem> selectionRules) {
        processMapper.deleteByAgentId(agentId);
        if (CollUtil.isEmpty(processes)) {
            return;
        }
        String mode = StrUtil.blankToDefault(selectionMode, "rule");
        String rulesJson = JsonUtils.toJsonString(selectionRules == null ? Collections.emptyList() : selectionRules);
        int[] priority = {0};
        processMapper.insertBatch(convertList(processes, item -> {
            priority[0]++;
            return new AgentxAgentProcessDO()
                    .setAgentId(agentId)
                    .setProcessDefinitionId(item.getProcessDefinitionId())
                    .setProcessDefinitionKey(item.getProcessDefinitionKey())
                    .setProcessName(item.getProcessName())
                    .setProcessVersion(item.getProcessVersion())
                    .setSelectionMode(mode)
                    .setSelectionRules(rulesJson)
                    .setPriority(priority[0]);
        }));
    }

    private String resolveDeptName(Long deptId) {
        DeptRespDTO dept = deptApi.getDept(deptId);
        return dept == null ? null : dept.getName();
    }

    private String generateAgentKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void validateAgentExists(Long id) {
        if (id == null || agentMapper.selectById(id) == null) {
            throw exception(ErrorCodeConstants.AGENT_NOT_EXISTS);
        }
    }

    private void validateAgentNameUnique(Long id, String agentName) {
        AgentxAgentDO agent = agentMapper.selectByAgentName(agentName);
        if (agent == null) {
            return;
        }
        if (id == null || !ObjectUtil.equal(id, agent.getId())) {
            throw exception(ErrorCodeConstants.AGENT_NAME_DUPLICATED);
        }
    }

    private void validatePublishRequired(AgentxAgentSaveReqVO reqVO, boolean publish) {
        if (!publish) {
            return;
        }
        if (CollUtil.isEmpty(reqVO.getCapabilities())) {
            throw exception(ErrorCodeConstants.AGENT_CAPABILITY_REQUIRED);
        }
        if (CollUtil.isEmpty(reqVO.getProcesses())) {
            throw exception(ErrorCodeConstants.AGENT_PROCESS_REQUIRED);
        }
    }

    private void syncAgentUserAndConfig(Long agentId, String trigger) {
        AgentxAgentDO agent = agentMapper.selectById(agentId);
        if (agent == null) {
            return;
        }
        adminUserService.createOrUpdateAgentUser(
                agent.getId(),
                "agent_" + agent.getAgentKey(),
                agent.getAgentName(),
                agent.getDeptId(),
                agent.getAvatarUrl(),
                toSystemUserStatus(agent.getStatus()));
        persistConfigVersionAndSync(agent, trigger);
    }

    private Integer toSystemUserStatus(Integer agentStatus) {
        return ObjectUtil.equal(agentStatus, STATUS_ACTIVE) ? 0 : 1;
    }

    private void persistConfigVersionAndSync(AgentxAgentDO agent, String trigger) {
        AgentxAgentConfigVersionDO latest = configVersionMapper.selectLatestByAgentId(agent.getId());
        int versionNo = latest == null ? 1 : latest.getVersionNo() + 1;

        List<AgentxAgentCapabilityDO> capabilities = capabilityMapper.selectListByAgentId(agent.getId());
        List<AgentxAgentProcessDO> processes = processMapper.selectListByAgentId(agent.getId());
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("trigger", trigger);
        snapshot.put("agent", agent);
        snapshot.put("capabilities", capabilities);
        snapshot.put("processes", processes);

        SyncResult syncResult = syncToOpenfang(agent, capabilities, processes);
        configVersionMapper.insert(new AgentxAgentConfigVersionDO()
                .setAgentId(agent.getId())
                .setVersionNo(versionNo)
                .setSnapshot(JsonUtils.toJsonString(snapshot))
                .setSyncStatus(syncResult.getStatus())
                .setSyncMessage(syncResult.getMessage())
                .setSyncTime(syncResult.getSyncTime()));

        agentMapper.updateById(new AgentxAgentDO()
                .setId(agent.getId())
                .setConfigVersion(versionNo)
                .setLastSyncStatus(syncResult.getStatus())
                .setLastSyncTime(syncResult.getSyncTime())
                .setLastSyncMessage(syncResult.getMessage()));
    }

    private SyncResult syncToOpenfang(AgentxAgentDO agent,
                                      List<AgentxAgentCapabilityDO> capabilities,
                                      List<AgentxAgentProcessDO> processes) {
        List<AgentxOpenfangInstanceDO> instances = openfangInstanceMapper.selectListByStatus(1);
        if (CollUtil.isEmpty(instances)) {
            return SyncResult.failed("未配置可用 OpenFang 实例");
        }
        AgentxOpenfangInstanceDO instance = instances.get(0);
        String endpoint = StrUtil.removeSuffix(instance.getEndpoint(), "/") + "/api/agents/register";

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("agentId", agent.getId());
        payload.put("agentKey", agent.getAgentKey());
        payload.put("agentName", agent.getAgentName());
        payload.put("templateType", agent.getTemplateType());
        payload.put("status", agent.getStatus());
        payload.put("capabilities", capabilities);
        payload.put("processes", processes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String apiKey = openfangApiKeyCrypto.decrypt(instance.getApiKeyEncrypted());
        if (StrUtil.isNotBlank(apiKey)) {
            headers.setBearerAuth(apiKey);
        }
        try {
            for (int attempt = 1; attempt <= OPENFANG_SYNC_MAX_ATTEMPTS; attempt++) {
                try {
                    ResponseEntity<Object> response = restTemplate.exchange(endpoint, HttpMethod.POST,
                            new HttpEntity<>(payload, headers), Object.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        metricsService.recordOpenfangCall(true);
                        return SyncResult.success("OpenFang 同步成功");
                    }
                    if (shouldRetry(response.getStatusCodeValue(), attempt)) {
                        backoff(attempt);
                        continue;
                    }
                    metricsService.recordOpenfangCall(false);
                    return SyncResult.failed("OpenFang 同步失败，HTTP=" + response.getStatusCodeValue());
                } catch (Exception ex) {
                    if (attempt >= OPENFANG_SYNC_MAX_ATTEMPTS) {
                        throw ex;
                    }
                    backoff(attempt);
                }
            }
            metricsService.recordOpenfangCall(false);
            return SyncResult.failed("OpenFang 同步失败，超过最大重试次数");
        } catch (Exception ex) {
            metricsService.recordOpenfangCall(false);
            return SyncResult.failed("OpenFang 同步异常：" + ex.getMessage());
        }
    }

    private boolean shouldRetry(int statusCode, int attempt) {
        return attempt < OPENFANG_SYNC_MAX_ATTEMPTS && statusCode >= 500;
    }

    private void backoff(int attempt) {
        long waitMillis = OPENFANG_SYNC_BASE_BACKOFF_MILLIS * (1L << Math.max(0, attempt - 1));
        try {
            Thread.sleep(waitMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static class SyncResult {
        private final Integer status;
        private final String message;
        private final LocalDateTime syncTime;

        private SyncResult(Integer status, String message, LocalDateTime syncTime) {
            this.status = status;
            this.message = message;
            this.syncTime = syncTime;
        }

        public static SyncResult success(String message) {
            return new SyncResult(1, message, LocalDateTime.now());
        }

        public static SyncResult failed(String message) {
            return new SyncResult(2, message, LocalDateTime.now());
        }

        public Integer getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getSyncTime() {
            return syncTime;
        }
    }

}
