package cn.iocoder.yudao.module.agentx.service.instance;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.instance.vo.AgentxOpenfangInstancePageReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.instance.vo.AgentxOpenfangInstanceSaveReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.instance.vo.AgentxOpenfangInstanceTestReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.instance.vo.AgentxOpenfangInstanceTestRespVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.instance.AgentxOpenfangInstanceDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.instance.AgentxOpenfangInstanceMapper;
import cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridge;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangHealthRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * OpenFang 实例管理 Service 实现类。
 */
@Service
@Validated
public class OpenfangInstanceServiceImpl implements OpenfangInstanceService {

    @Resource
    private AgentxOpenfangInstanceMapper instanceMapper;
    @Resource
    private OpenfangApiKeyCrypto apiKeyCrypto;
    @Resource
    private OpenfangRuntimeBridge runtimeBridge;

    @Override
    public Long createInstance(AgentxOpenfangInstanceSaveReqVO createReqVO) {
        validateInstanceNameUnique(null, createReqVO.getInstanceName());
        if (StrUtil.isBlank(createReqVO.getApiKey())) {
            throw exception(ErrorCodeConstants.OPENFANG_INSTANCE_API_KEY_REQUIRED);
        }
        AgentxOpenfangInstanceDO instance = new AgentxOpenfangInstanceDO()
                .setInstanceName(createReqVO.getInstanceName())
                .setEndpoint(normalizeEndpoint(createReqVO.getEndpoint()))
                .setApiKeyEncrypted(apiKeyCrypto.encrypt(createReqVO.getApiKey()))
                .setStatus(createReqVO.getStatus());
        instanceMapper.insert(instance);
        return instance.getId();
    }

    @Override
    public void updateInstance(AgentxOpenfangInstanceSaveReqVO updateReqVO) {
        AgentxOpenfangInstanceDO existing = validateInstanceExists(updateReqVO.getId());
        validateInstanceNameUnique(existing.getId(), updateReqVO.getInstanceName());
        AgentxOpenfangInstanceDO updateObj = new AgentxOpenfangInstanceDO()
                .setId(updateReqVO.getId())
                .setInstanceName(updateReqVO.getInstanceName())
                .setEndpoint(normalizeEndpoint(updateReqVO.getEndpoint()))
                .setStatus(updateReqVO.getStatus());
        if (StrUtil.isNotBlank(updateReqVO.getApiKey())) {
            updateObj.setApiKeyEncrypted(apiKeyCrypto.encrypt(updateReqVO.getApiKey()));
        }
        instanceMapper.updateById(updateObj);
    }

    @Override
    public void deleteInstance(Long id) {
        validateInstanceExists(id);
        instanceMapper.deleteById(id);
    }

    @Override
    public AgentxOpenfangInstanceDO getInstance(Long id) {
        return instanceMapper.selectById(id);
    }

    @Override
    public PageResult<AgentxOpenfangInstanceDO> getInstancePage(AgentxOpenfangInstancePageReqVO pageReqVO) {
        return instanceMapper.selectPage(pageReqVO);
    }

    @Override
    public AgentxOpenfangInstanceTestRespVO testConnection(AgentxOpenfangInstanceTestReqVO testReqVO) {
        AgentxOpenfangInstanceDO instance = null;
        if (testReqVO.getId() != null) {
            instance = validateInstanceExists(testReqVO.getId());
        }

        String endpoint = normalizeEndpoint(ObjectUtil.defaultIfNull(testReqVO.getEndpoint(),
                instance != null ? instance.getEndpoint() : null));
        String apiKey = StrUtil.blankToDefault(testReqVO.getApiKey(),
                instance != null ? apiKeyCrypto.decrypt(instance.getApiKeyEncrypted()) : null);
        if (StrUtil.isBlank(endpoint)) {
            throw exception(ErrorCodeConstants.OPENFANG_INSTANCE_ENDPOINT_REQUIRED);
        }

        OpenfangHealthRespDTO healthResp = runtimeBridge.health(endpoint, apiKey);
        AgentxOpenfangInstanceTestRespVO respVO = new AgentxOpenfangInstanceTestRespVO();
        respVO.setOnline(Boolean.TRUE.equals(healthResp.getOnline()));
        respVO.setVersion(healthResp.getVersion());
        respVO.setMessage(healthResp.getMessage());
        respVO.setCheckedAt(LocalDateTime.now());
        return respVO;
    }

    @Override
    public List<AgentxOpenfangInstanceDO> getAllInstances() {
        return instanceMapper.selectList();
    }

    @Override
    public void refreshHealthStatus() {
        List<AgentxOpenfangInstanceDO> instances = instanceMapper.selectList();
        for (AgentxOpenfangInstanceDO instance : instances) {
            String apiKey = apiKeyCrypto.decrypt(instance.getApiKeyEncrypted());
            OpenfangHealthRespDTO healthResp = runtimeBridge.health(instance.getEndpoint(), apiKey);
            AgentxOpenfangInstanceDO updateObj = new AgentxOpenfangInstanceDO()
                    .setId(instance.getId())
                    .setStatus(Boolean.TRUE.equals(healthResp.getOnline()) ? 1 : 0)
                    .setVersion(healthResp.getVersion())
                    .setLastHeartbeat(LocalDateTime.now());
            instanceMapper.updateById(updateObj);
        }
    }

    private AgentxOpenfangInstanceDO validateInstanceExists(Long id) {
        AgentxOpenfangInstanceDO instance = instanceMapper.selectById(id);
        if (instance == null) {
            throw exception(ErrorCodeConstants.OPENFANG_INSTANCE_NOT_EXISTS);
        }
        return instance;
    }

    private void validateInstanceNameUnique(Long id, String instanceName) {
        AgentxOpenfangInstanceDO instance = instanceMapper.selectByInstanceName(instanceName);
        if (instance == null) {
            return;
        }
        if (id == null || !ObjectUtil.equal(id, instance.getId())) {
            throw exception(ErrorCodeConstants.OPENFANG_INSTANCE_NAME_DUPLICATED);
        }
    }

    private String normalizeEndpoint(String endpoint) {
        if (StrUtil.isBlank(endpoint)) {
            return endpoint;
        }
        String trimmed = endpoint.trim();
        return StrUtil.removeSuffix(trimmed, "/");
    }

}
