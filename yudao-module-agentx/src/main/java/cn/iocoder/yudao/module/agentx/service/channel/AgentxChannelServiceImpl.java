package cn.iocoder.yudao.module.agentx.service.channel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.*;
import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.channel.AgentxChannelAgentDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.channel.AgentxChannelConfigDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.channel.AgentxUserChannelBindingDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.instance.AgentxOpenfangInstanceDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.agent.AgentxAgentMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.channel.AgentxChannelAgentMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.channel.AgentxChannelConfigMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.channel.AgentxUserChannelBindingMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.instance.AgentxOpenfangInstanceMapper;
import cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.agentx.service.instance.OpenfangApiKeyCrypto;
import cn.iocoder.yudao.module.agentx.service.metrics.AgentxMetricsService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Service
@Validated
@Slf4j
public class AgentxChannelServiceImpl implements AgentxChannelService {

    private static final Set<String> VALID_CHANNEL_TYPES = new HashSet<>(Arrays.asList("telegram", "whatsapp", "wecom", "dingtalk"));
    private static final String BOT_TOKEN_FALLBACK_PREFIX = "b64:";
    private static final String AUTH_MODE_PUBLIC = "public";
    private static final String AUTH_MODE_BIND_REQUIRED = "bind_required";
    private static final Long TELEGRAM_AUTH_REQUIRED_PLACEHOLDER_USER_ID = -1L;

    @Resource
    private AgentxChannelConfigMapper channelConfigMapper;
    @Resource
    private AgentxAgentMapper agentMapper;
    @Resource
    private AgentxChannelAgentMapper channelAgentMapper;
    @Resource
    private AgentxUserChannelBindingMapper userChannelBindingMapper;
    @Resource
    private OpenfangApiKeyCrypto openfangApiKeyCrypto;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private AgentxOpenfangInstanceMapper openfangInstanceMapper;
    @Resource
    private AgentxMetricsService metricsService;
    @Resource
    private AgentxBindService bindService;
    @Resource
    private AdminUserService adminUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createChannelConfig(AgentxChannelConfigSaveReqVO reqVO) {
        validateChannelType(reqVO.getChannelType());
        if (isBotTokenRequired(reqVO.getChannelType()) && StrUtil.isBlank(reqVO.getBotToken())) {
            throw exception(ErrorCodeConstants.CHANNEL_BOT_TOKEN_REQUIRED);
        }
        AgentxChannelConfigDO channel = new AgentxChannelConfigDO()
                .setChannelType(reqVO.getChannelType())
                .setChannelName(reqVO.getChannelName())
                // Keep DB NOT NULL compatible for channels (e.g. WhatsApp) that don't require token.
                .setBotTokenEncrypted(StrUtil.blankToDefault(encryptBotToken(reqVO.getBotToken()), ""))
                .setConfig(buildConfigJson(reqVO))
                .setStatus(reqVO.getStatus());
        channelConfigMapper.insert(channel);
        replaceChannelAgents(channel.getId(), reqVO.getAgentIds());
        syncChannelToOpenfang(channel.getId());
        return channel.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChannelConfig(AgentxChannelConfigSaveReqVO reqVO) {
        AgentxChannelConfigDO channel = validateChannelExists(reqVO.getId());
        validateChannelType(reqVO.getChannelType());
        AgentxChannelConfigDO update = new AgentxChannelConfigDO()
                .setId(channel.getId())
                .setChannelType(reqVO.getChannelType())
                .setChannelName(reqVO.getChannelName())
                .setConfig(buildConfigJson(reqVO))
                .setStatus(reqVO.getStatus());
        if (StrUtil.isNotBlank(reqVO.getBotToken())) {
            update.setBotTokenEncrypted(encryptBotToken(reqVO.getBotToken()));
        }
        channelConfigMapper.updateById(update);
        replaceChannelAgents(channel.getId(), reqVO.getAgentIds());
        syncChannelToOpenfang(channel.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChannelConfig(Long id) {
        AgentxChannelConfigDO channel = validateChannelExists(id);
        removeChannelFromOpenfang(channel);
        channelConfigMapper.deleteById(id);
        channelAgentMapper.deleteByChannelId(id);
    }

    @Override
    public AgentxChannelConfigRespVO getChannelConfig(Long id) {
        return convert(channelConfigMapper.selectById(id));
    }

    @Override
    public PageResult<AgentxChannelConfigRespVO> getChannelConfigPage(AgentxChannelConfigPageReqVO pageReqVO) {
        PageResult<AgentxChannelConfigDO> page = channelConfigMapper.selectPage(pageReqVO);
        List<AgentxChannelConfigDO> filtered = filterChannelsByOpenfangRuntime(page.getList());
        PageResult<AgentxChannelConfigDO> filteredPage = new PageResult<>(filtered, (long) filtered.size());
        return BeanUtils.toBean(filteredPage, AgentxChannelConfigRespVO.class, resp -> {
            AgentxChannelConfigDO channel = filtered.stream()
                    .filter(item -> ObjectUtil.equal(item.getId(), resp.getId()))
                    .findFirst().orElse(null);
            if (channel != null) {
                fillRespDetails(channel, resp);
            }
        });
    }

    @Override
    public AgentxChannelTestRespVO testChannelConnection(AgentxChannelTestReqVO reqVO) {
        validateChannelType(reqVO.getChannelType());
        if ("whatsapp".equals(reqVO.getChannelType())) {
            return testWhatsAppConnection();
        }
        String authToken = reqVO.getBotToken();
        if (StrUtil.isBlank(authToken) && reqVO.getChannelId() != null) {
            AgentxChannelConfigDO channel = channelConfigMapper.selectById(reqVO.getChannelId());
            if (channel != null && StrUtil.isNotBlank(channel.getBotTokenEncrypted())) {
                authToken = decryptBotToken(channel.getBotTokenEncrypted());
            }
        }
        if (StrUtil.isBlank(authToken)) {
            throw exception(ErrorCodeConstants.CHANNEL_BOT_TOKEN_REQUIRED);
        }
        AgentxChannelTestRespVO respVO = new AgentxChannelTestRespVO();
        try {
            if ("telegram".equals(reqVO.getChannelType())) {
                String url = "https://api.telegram.org/bot" + authToken + "/getMe";
                return buildRespByOkField(respVO, restTemplate.exchange(url, HttpMethod.GET, null, Map.class));
            }
            if ("wecom".equals(reqVO.getChannelType())) {
                String[] auth = splitAuthPair(authToken);
                String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + auth[0] + "&corpsecret=" + auth[1];
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);
                Object errCode = response.getBody() == null ? null : response.getBody().get("errcode");
                if ("0".equals(String.valueOf(errCode))) {
                    respVO.setSuccess(true);
                    respVO.setMessage("✓ 连接成功");
                    return respVO;
                }
                respVO.setSuccess(false);
                respVO.setMessage("✗ 连接失败：配置无效");
                return respVO;
            }
            String[] auth = splitAuthPair(authToken);
            String url = "https://oapi.dingtalk.com/gettoken?appkey=" + auth[0] + "&appsecret=" + auth[1];
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);
            Object errCode = response.getBody() == null ? null : response.getBody().get("errcode");
            if ("0".equals(String.valueOf(errCode))) {
                respVO.setSuccess(true);
                respVO.setMessage("✓ 连接成功");
                return respVO;
            }
            respVO.setSuccess(false);
            respVO.setMessage("✗ 连接失败：配置无效");
            return respVO;
        } catch (HttpStatusCodeException ex) {
            respVO.setSuccess(false);
            respVO.setMessage(resolveChannelTestErrorMessage(reqVO.getChannelType(), ex));
            return respVO;
        } catch (Exception ex) {
            respVO.setSuccess(false);
            respVO.setMessage("✗ 连接失败：" + ex.getMessage());
            return respVO;
        }
    }

    @Override
    public AgentxChannelAccessEvaluateRespVO evaluateChannelAccess(AgentxChannelAccessEvaluateReqVO reqVO) {
        validateChannelType(reqVO.getChannelType());
        AgentxAgentDO agent = resolveTargetAgent(reqVO);
        AgentxChannelConfigDO channel = resolveAgentChannel(agent.getId(), reqVO.getChannelType());
        if (channel == null) {
            return buildDeniedResp(false, false, null, null, "当前 Agent 未启用该渠道");
        }
        Map<String, Object> config = parseConfig(channel);
        String authMode = resolveAgentAuthMode(config, agent.getId());
        String accessControlType = String.valueOf(config.getOrDefault("accessControlType", "all"));
        if (AUTH_MODE_PUBLIC.equals(authMode)) {
            AgentxChannelAccessEvaluateRespVO respVO = new AgentxChannelAccessEvaluateRespVO();
            respVO.setAccessGranted(true);
            respVO.setAuthRequired(false);
            respVO.setBound(false);
            respVO.setAuthMode(authMode);
            respVO.setAccessControlType(accessControlType);
            respVO.setMessage("公开 Agent 允许匿名访问");
            return respVO;
        }

        AgentxUserChannelBindingDO binding = userChannelBindingMapper.selectByChannelIdentity(
                reqVO.getChannelType(), reqVO.getChannelUserId());
        if (binding == null) {
            AgentxBindGenerateReqVO bindReqVO = new AgentxBindGenerateReqVO();
            bindReqVO.setChannelType(reqVO.getChannelType());
            bindReqVO.setChannelUserId(reqVO.getChannelUserId());
            bindReqVO.setChannelUsername(reqVO.getChannelUsername());
            AgentxBindGenerateRespVO bindRespVO = bindService.generateBindLink(bindReqVO);
            AgentxChannelAccessEvaluateRespVO respVO = buildDeniedResp(true, false, authMode, accessControlType,
                    "当前 Agent 需要先完成身份绑定");
            respVO.setBindToken(bindRespVO.getToken());
            respVO.setBindUrl(bindRespVO.getBindUrl());
            return respVO;
        }

        if (!isBoundUserAllowed(config, binding.getUserId())) {
            return buildDeniedResp(true, true, authMode, accessControlType, "已绑定，但当前用户不在该 Agent 允许范围内");
        }

        AgentxChannelAccessEvaluateRespVO respVO = new AgentxChannelAccessEvaluateRespVO();
        respVO.setAccessGranted(true);
        respVO.setAuthRequired(true);
        respVO.setBound(true);
        respVO.setAuthMode(authMode);
        respVO.setAccessControlType(accessControlType);
        respVO.setMessage("绑定校验通过，允许触发 Agent 能力");
        return respVO;
    }

    @Override
    public AgentxWhatsAppQrStartRespVO startWhatsAppQrBind(AgentxWhatsAppQrStartReqVO reqVO) {
        AgentxWhatsAppQrStartRespVO respVO = new AgentxWhatsAppQrStartRespVO();
        AgentxOpenfangInstanceDO instance = getFirstEnabledOpenfangInstance();
        if (instance == null) {
            respVO.setAvailable(false);
            respVO.setConnected(false);
            respVO.setMessage("未找到可用的 OpenFang 实例");
            return respVO;
        }
        String bindingId = null;
        String targetAgent = null;
        if (reqVO != null) {
            bindingId = StrUtil.emptyToNull(StrUtil.trim(reqVO.getBindingId()));
            if (StrUtil.isBlank(bindingId) && reqVO.getChannelId() != null) {
                bindingId = "agentx-channel-" + reqVO.getChannelId();
                targetAgent = buildDefaultAgentName(reqVO.getChannelId());
            }
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        if (StrUtil.isNotBlank(bindingId)) {
            payload.put("binding_id", bindingId);
        }
        if (StrUtil.isNotBlank(targetAgent)) {
            payload.put("target_agent", targetAgent);
        }
        String endpoint = StrUtil.format("{}/api/channels/whatsapp/qr/start", StrUtil.removeSuffix(instance.getEndpoint(), "/"));
        try {
            ResponseEntity<Map> response = restTemplate.exchange(endpoint, HttpMethod.POST,
                    new HttpEntity<>(payload, buildOpenfangHeaders(instance)), Map.class);
            Map body = response.getBody();
            respVO.setAvailable(body != null && Boolean.TRUE.equals(body.get("available")));
            respVO.setQrDataUrl(readStringField(body, "qr_data_url"));
            respVO.setSessionId(readStringField(body, "session_id"));
            respVO.setMessage(readStringField(body, "message"));
            respVO.setHelp(readStringField(body, "help"));
            respVO.setConnected(body != null && Boolean.TRUE.equals(body.get("connected")));
            return respVO;
        } catch (HttpStatusCodeException ex) {
            throw exception(ErrorCodeConstants.CHANNEL_SYNC_OPENFANG_FAILED, resolveOpenfangErrorMessage(ex));
        } catch (Exception ex) {
            throw exception(ErrorCodeConstants.CHANNEL_SYNC_OPENFANG_FAILED, StrUtil.blankToDefault(ex.getMessage(), "未知错误"));
        }
    }

    @Override
    public AgentxWhatsAppQrStatusRespVO getWhatsAppQrStatus(String sessionId, Long channelId) {
        AgentxWhatsAppQrStatusRespVO respVO = new AgentxWhatsAppQrStatusRespVO();
        AgentxOpenfangInstanceDO instance = getFirstEnabledOpenfangInstance();
        if (instance == null) {
            respVO.setConnected(false);
            respVO.setExpired(false);
            respVO.setMessage("未找到可用的 OpenFang 实例");
            return respVO;
        }
        String bindingId = channelId == null ? null : buildWhatsAppBindingId(channelId);
        AgentxWhatsAppQrStatusRespVO sessionStatus = null;
        if (StrUtil.isNotBlank(sessionId)) {
            sessionStatus = queryWhatsAppQrStatus(instance, sessionId, bindingId);
            if (Boolean.TRUE.equals(sessionStatus.getConnected())) {
                return sessionStatus;
            }
            // After OpenFang restarts, transient session ids may be lost while the stable binding
            // still exists. Fall through to runtime binding lookup before reporting disconnected.
        }
        AgentxWhatsAppQrStatusRespVO runtimeStatus = queryWhatsAppRuntimeBindingStatus(instance, bindingId);
        if (runtimeStatus != null) {
            if (sessionStatus != null && StrUtil.isNotBlank(sessionStatus.getQrDataUrl())
                    && StrUtil.isBlank(runtimeStatus.getQrDataUrl())) {
                runtimeStatus.setQrDataUrl(sessionStatus.getQrDataUrl());
            }
            if (sessionStatus != null && StrUtil.isNotBlank(sessionStatus.getSessionId())
                    && StrUtil.isBlank(runtimeStatus.getSessionId())) {
                runtimeStatus.setSessionId(sessionStatus.getSessionId());
            }
            return runtimeStatus;
        }
        if (sessionStatus != null) {
            return sessionStatus;
        }
        respVO.setConnected(false);
        respVO.setExpired(false);
        respVO.setMessage(StrUtil.isNotBlank(bindingId)
                ? "未查询到该 WhatsApp 绑定的运行时连接"
                : "请提供 sessionId 或 channelId");
        return respVO;
    }

    @Override
    public List<String> getEnabledWhatsAppBindingIds() {
        return channelConfigMapper.selectListByChannelType("whatsapp").stream()
                .filter(item -> item != null && item.getId() != null)
                .filter(item -> CommonStatusEnum.isEnable(item.getStatus()))
                .map(item -> "agentx-channel-" + item.getId())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public void refreshRuntimeAccessByChannelType(String channelType) {
        validateChannelType(channelType);
        List<AgentxChannelConfigDO> channels = channelConfigMapper.selectListByChannelType(channelType);
        if (CollUtil.isEmpty(channels)) {
            return;
        }
        for (AgentxChannelConfigDO channel : channels) {
            syncChannelToOpenfang(channel.getId());
        }
    }

    @Override
    public List<AgentxUserChannelBindingRespVO> getMyBindings(Long userId) {
        return BeanUtils.toBean(userChannelBindingMapper.selectListByUserId(userId), AgentxUserChannelBindingRespVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbind(Long id, Long userId) {
        AgentxUserChannelBindingDO binding = userChannelBindingMapper.selectById(id);
        if (binding == null || !ObjectUtil.equal(binding.getUserId(), userId)) {
            return;
        }
        userChannelBindingMapper.updateById(new AgentxUserChannelBindingDO()
                .setId(id)
                .setStatus(0)
                .setUnbindTime(LocalDateTime.now()));
        refreshRuntimeAccessByChannelType(binding.getChannelType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUnbind(Long id) {
        AgentxUserChannelBindingDO binding = userChannelBindingMapper.selectById(id);
        if (binding == null || !ObjectUtil.equal(binding.getStatus(), 1)) {
            return;
        }
        userChannelBindingMapper.updateById(new AgentxUserChannelBindingDO()
                .setId(id)
                .setStatus(0)
                .setUnbindTime(LocalDateTime.now()));
        refreshRuntimeAccessByChannelType(binding.getChannelType());
    }

    @Override
    public PageResult<AgentxUserChannelBindingRespVO> getBindingPage(AgentxUserChannelBindingPageReqVO pageReqVO) {
        return BeanUtils.toBean(userChannelBindingMapper.selectPage(pageReqVO), AgentxUserChannelBindingRespVO.class);
    }

    private String buildConfigJson(AgentxChannelConfigSaveReqVO reqVO) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("accessControlType", reqVO.getAccessControlType());
        config.put("deptIds", reqVO.getDeptIds() == null ? Collections.emptyList() : reqVO.getDeptIds());
        config.put("userIds", reqVO.getUserIds() == null ? Collections.emptyList() : reqVO.getUserIds());
        config.put("agentAccessPolicies", normalizeAgentAccessPolicies(reqVO.getAgentIds(), reqVO.getAgentAccessPolicies()));
        return JsonUtils.toJsonString(config);
    }

    private AgentxChannelConfigRespVO convert(AgentxChannelConfigDO channel) {
        if (channel == null) {
            return null;
        }
        AgentxChannelConfigRespVO respVO = BeanUtils.toBean(channel, AgentxChannelConfigRespVO.class);
        fillRespDetails(channel, respVO);
        return respVO;
    }

    private void fillRespDetails(AgentxChannelConfigDO channel, AgentxChannelConfigRespVO respVO) {
        List<AgentxChannelAgentDO> channelAgents = channelAgentMapper.selectListByChannelId(channel.getId());
        respVO.setAgentIds(convertList(channelAgents, AgentxChannelAgentDO::getAgentId));
        Map<String, Object> config = JsonUtils.parseObject(channel.getConfig(), Map.class);
        if (config == null) {
            return;
        }
        respVO.setAccessControlType((String) config.get("accessControlType"));
        respVO.setDeptIds(convertIdList(config.get("deptIds")));
        respVO.setUserIds(convertIdList(config.get("userIds")));
        respVO.setAgentAccessPolicies(convertAgentAccessPolicies(config.get("agentAccessPolicies")));
    }

    private List<Long> convertIdList(Object value) {
        if (!(value instanceof Collection)) {
            return Collections.emptyList();
        }
        return convertList((Collection<Object>) value, item -> Long.valueOf(String.valueOf(item)));
    }

    private List<AgentxChannelConfigRespVO.AgentAccessPolicyItem> convertAgentAccessPolicies(Object value) {
        if (!(value instanceof Collection)) {
            return Collections.emptyList();
        }
        return convertList((Collection<Object>) value, item -> {
            if (!(item instanceof Map)) {
                return null;
            }
            Map<?, ?> map = (Map<?, ?>) item;
            AgentxChannelConfigRespVO.AgentAccessPolicyItem policy = new AgentxChannelConfigRespVO.AgentAccessPolicyItem();
            Object agentId = map.get("agentId");
            if (agentId != null) {
                policy.setAgentId(Long.valueOf(String.valueOf(agentId)));
            }
            Object authMode = map.get("authMode");
            policy.setAuthMode(String.valueOf(authMode == null ? AUTH_MODE_BIND_REQUIRED : authMode));
            return policy;
        }).stream().filter(Objects::nonNull).collect(java.util.stream.Collectors.toList());
    }

    private List<Map<String, Object>> normalizeAgentAccessPolicies(List<Long> agentIds,
                                                                   List<AgentxChannelConfigSaveReqVO.AgentAccessPolicyItem> policies) {
        Map<Long, String> authModeMap = new LinkedHashMap<>();
        if (CollUtil.isNotEmpty(agentIds)) {
            for (Long agentId : agentIds) {
                authModeMap.put(agentId, AUTH_MODE_BIND_REQUIRED);
            }
        }
        if (CollUtil.isNotEmpty(policies)) {
            for (AgentxChannelConfigSaveReqVO.AgentAccessPolicyItem policy : policies) {
                if (policy == null || policy.getAgentId() == null) {
                    continue;
                }
                authModeMap.put(policy.getAgentId(), normalizeAuthMode(policy.getAuthMode()));
            }
        }
        List<Map<String, Object>> normalized = new ArrayList<>();
        authModeMap.forEach((agentId, authMode) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("agentId", agentId);
            item.put("authMode", authMode);
            normalized.add(item);
        });
        return normalized;
    }

    private AgentxChannelConfigDO validateChannelExists(Long id) {
        AgentxChannelConfigDO channel = channelConfigMapper.selectById(id);
        if (channel == null) {
            throw exception(ErrorCodeConstants.CHANNEL_CONFIG_NOT_EXISTS);
        }
        return channel;
    }

    private void validateChannelType(String channelType) {
        if (!VALID_CHANNEL_TYPES.contains(channelType)) {
            throw exception(ErrorCodeConstants.CHANNEL_TYPE_INVALID);
        }
    }

    private AgentxAgentDO resolveTargetAgent(AgentxChannelAccessEvaluateReqVO reqVO) {
        AgentxAgentDO agent = null;
        if (reqVO.getAgentId() != null) {
            agent = agentMapper.selectById(reqVO.getAgentId());
        } else if (StrUtil.isNotBlank(reqVO.getAgentKey())) {
            agent = agentMapper.selectByAgentKey(reqVO.getAgentKey());
        }
        if (agent == null) {
            throw exception(ErrorCodeConstants.AGENT_NOT_EXISTS);
        }
        return agent;
    }

    private AgentxChannelConfigDO resolveAgentChannel(Long agentId, String channelType) {
        List<AgentxChannelAgentDO> relations = channelAgentMapper.selectListByAgentId(agentId);
        if (CollUtil.isEmpty(relations)) {
            return null;
        }
        List<Long> channelIds = convertList(relations, AgentxChannelAgentDO::getChannelId);
        return channelConfigMapper.selectListByIds(channelIds).stream()
                .filter(item -> CommonStatusEnum.isEnable(item.getStatus()))
                .filter(item -> StrUtil.equals(channelType, item.getChannelType()))
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> parseConfig(AgentxChannelConfigDO channel) {
        Map<String, Object> config = JsonUtils.parseObject(channel.getConfig(), Map.class);
        return config == null ? new LinkedHashMap<>() : config;
    }

    private String resolveAgentAuthMode(Map<String, Object> config, Long agentId) {
        Object policies = config.get("agentAccessPolicies");
        if (policies instanceof Collection<?>) {
            for (Object policy : (Collection<?>) policies) {
                if (!(policy instanceof Map<?, ?>)) {
                    continue;
                }
                Map<?, ?> map = (Map<?, ?>) policy;
                if (ObjectUtil.equal(String.valueOf(map.get("agentId")), String.valueOf(agentId))) {
                    return normalizeAuthMode((String) map.get("authMode"));
                }
            }
        }
        return AUTH_MODE_BIND_REQUIRED;
    }

    private String normalizeAuthMode(String authMode) {
        return AUTH_MODE_PUBLIC.equals(authMode) ? AUTH_MODE_PUBLIC : AUTH_MODE_BIND_REQUIRED;
    }

    private AgentxAgentDO resolveDefaultActiveAgent(Long channelId) {
        List<AgentxChannelAgentDO> relations = channelAgentMapper.selectListByChannelId(channelId);
        if (CollUtil.isEmpty(relations)) {
            return null;
        }
        for (AgentxChannelAgentDO relation : relations) {
            AgentxAgentDO agent = agentMapper.selectById(relation.getAgentId());
            if (agent == null || !ObjectUtil.equal(agent.getStatus(), 1)) {
                continue;
            }
            return agent;
        }
        return null;
    }

    private boolean isBoundUserAllowed(Map<String, Object> config, Long userId) {
        String accessControlType = String.valueOf(config.getOrDefault("accessControlType", "all"));
        if (StrUtil.equals("all", accessControlType)) {
            return true;
        }
        if (StrUtil.equals("user", accessControlType)) {
            return convertIdList(config.get("userIds")).contains(userId);
        }
        if (StrUtil.equals("dept", accessControlType)) {
            AdminUserDO user = adminUserService.getUser(userId);
            return user != null && convertIdList(config.get("deptIds")).contains(user.getDeptId());
        }
        return false;
    }

    private AgentxChannelAccessEvaluateRespVO buildDeniedResp(boolean authRequired, boolean bound,
                                                              String authMode, String accessControlType, String message) {
        AgentxChannelAccessEvaluateRespVO respVO = new AgentxChannelAccessEvaluateRespVO();
        respVO.setAccessGranted(false);
        respVO.setAuthRequired(authRequired);
        respVO.setBound(bound);
        respVO.setAuthMode(authMode);
        respVO.setAccessControlType(accessControlType);
        respVO.setMessage(message);
        return respVO;
    }

    private void replaceChannelAgents(Long channelId, List<Long> agentIds) {
        // Use physical delete here to avoid unique-index collisions with legacy soft-deleted rows.
        channelAgentMapper.deleteForceByChannelId(channelId);
        if (CollUtil.isEmpty(agentIds)) {
            return;
        }
        channelAgentMapper.insertBatch(convertList(agentIds,
                agentId -> new AgentxChannelAgentDO().setChannelId(channelId).setAgentId(agentId).setEnabled(true)));
    }

    private void syncChannelToOpenfang(Long channelId) {
        AgentxChannelConfigDO channel = channelConfigMapper.selectById(channelId);
        if (channel == null) {
            return;
        }
        if (!CommonStatusEnum.isEnable(channel.getStatus())) {
            removeChannelFromOpenfang(channel);
            return;
        }
        validateBindRequiredRuntimeGateSupport(channel);
        AgentxOpenfangInstanceDO instance = getFirstEnabledOpenfangInstance();
        if (instance == null) {
            metricsService.recordOpenfangCall(false);
            return;
        }
        try {
            if ("telegram".equals(channel.getChannelType())) {
                syncTelegramBinding(instance, channel);
            } else {
                String endpoint = StrUtil.format("{}/api/channels/{}/configure",
                        StrUtil.removeSuffix(instance.getEndpoint(), "/"), channel.getChannelType());
                restTemplate.exchange(endpoint, HttpMethod.POST,
                        new HttpEntity<>(Collections.singletonMap("fields", buildOpenfangChannelFields(channel)),
                                buildOpenfangHeaders(instance)),
                        Object.class);
            }
            metricsService.recordOpenfangCall(true);
        } catch (HttpStatusCodeException ex) {
            metricsService.recordOpenfangCall(false);
            throw exception(ErrorCodeConstants.CHANNEL_SYNC_OPENFANG_FAILED, resolveOpenfangErrorMessage(ex));
        } catch (Exception ex) {
            metricsService.recordOpenfangCall(false);
            throw exception(ErrorCodeConstants.CHANNEL_SYNC_OPENFANG_FAILED, StrUtil.blankToDefault(ex.getMessage(), "未知错误"));
        }
    }

    private void validateBindRequiredRuntimeGateSupport(AgentxChannelConfigDO channel) {
        AgentxAgentDO defaultAgent = resolveDefaultActiveAgent(channel.getId());
        if (defaultAgent == null) {
            return;
        }
        Map<String, Object> config = parseConfig(channel);
        String authMode = resolveAgentAuthMode(config, defaultAgent.getId());
        if (AUTH_MODE_BIND_REQUIRED.equals(authMode) && !supportsBindRequiredAuth(channel.getChannelType())) {
            throw exception(ErrorCodeConstants.CHANNEL_SYNC_OPENFANG_FAILED,
                    "当前渠道暂不支持 bind_required 强制门禁，请改用 Telegram / WhatsApp 或接入 OpenFang access/evaluate 前置校验");
        }
    }

    private void removeChannelFromOpenfang(AgentxChannelConfigDO channel) {
        AgentxOpenfangInstanceDO instance = getFirstEnabledOpenfangInstance();
        if (instance == null) {
            metricsService.recordOpenfangCall(false);
            return;
        }
        try {
            if ("telegram".equals(channel.getChannelType())) {
                String endpoint = StrUtil.format("{}/api/telegram/bindings/{}",
                        StrUtil.removeSuffix(instance.getEndpoint(), "/"), buildTelegramBindingId(channel.getId()));
                restTemplate.exchange(endpoint, HttpMethod.DELETE, new HttpEntity<>(buildOpenfangHeaders(instance)), Object.class);
            } else if ("whatsapp".equals(channel.getChannelType())) {
                String baseUrl = StrUtil.removeSuffix(instance.getEndpoint(), "/");
                String endpoint = StrUtil.format("{}/api/channels/whatsapp/bindings/{}",
                        baseUrl, buildWhatsAppBindingId(channel.getId()));
                restTemplate.exchange(endpoint, HttpMethod.DELETE, new HttpEntity<>(buildOpenfangHeaders(instance)), Object.class);
                cleanupWhatsAppOrphanBindings(instance, channel.getId());
            } else {
                String endpoint = StrUtil.format("{}/api/channels/{}/configure",
                        StrUtil.removeSuffix(instance.getEndpoint(), "/"), channel.getChannelType());
                restTemplate.exchange(endpoint, HttpMethod.DELETE, new HttpEntity<>(buildOpenfangHeaders(instance)), Object.class);
            }
            metricsService.recordOpenfangCall(true);
        } catch (HttpStatusCodeException ex) {
            metricsService.recordOpenfangCall(false);
            throw exception(ErrorCodeConstants.CHANNEL_SYNC_OPENFANG_FAILED, resolveOpenfangErrorMessage(ex));
        } catch (Exception ex) {
            metricsService.recordOpenfangCall(false);
            throw exception(ErrorCodeConstants.CHANNEL_SYNC_OPENFANG_FAILED, StrUtil.blankToDefault(ex.getMessage(), "未知错误"));
        }
    }

    private void syncTelegramBinding(AgentxOpenfangInstanceDO instance, AgentxChannelConfigDO channel) {
        AgentxAgentDO defaultAgent = resolveDefaultActiveAgent(channel.getId());
        if (defaultAgent == null) {
            return;
        }
        String defaultAgentName = buildOpenfangAgentName(defaultAgent);
        String baseUrl = StrUtil.removeSuffix(instance.getEndpoint(), "/");
        String bindingId = buildTelegramBindingId(channel.getId());
        String authToken = decryptBotToken(channel.getBotTokenEncrypted());
        Map<String, Object> config = parseConfig(channel);
        String authMode = resolveAgentAuthMode(config, defaultAgent.getId());
        List<Long> allowedUsers = AUTH_MODE_BIND_REQUIRED.equals(authMode)
                ? buildTelegramAllowedUsers(channel, config)
                : Collections.emptyList();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", bindingId);
        body.put("agent", defaultAgentName);
        body.put("token", authToken);
        body.put("enabled", true);
        body.put("poll_interval_secs", 1);
        body.put("allowed_users", allowedUsers);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildOpenfangHeaders(instance));
        String createEndpoint = baseUrl + "/api/telegram/bindings";
        String updateEndpoint = createEndpoint + "/" + bindingId;
        try {
            restTemplate.exchange(updateEndpoint, HttpMethod.PATCH, entity, Object.class);
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() != 404) {
                throw ex;
            }
            restTemplate.exchange(createEndpoint, HttpMethod.POST, entity, Object.class);
        }
    }

    private List<Long> buildTelegramAllowedUsers(AgentxChannelConfigDO channel, Map<String, Object> config) {
        List<Long> allowedUsers = userChannelBindingMapper.selectListByChannelType(channel.getChannelType()).stream()
                .filter(binding -> StrUtil.isNotBlank(binding.getChannelUserId()))
                .filter(binding -> isBoundUserAllowed(config, binding.getUserId()))
                .map(AgentxUserChannelBindingDO::getChannelUserId)
                .map(this::parseTelegramUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(allowedUsers)) {
            return allowedUsers;
        }
        return Collections.singletonList(TELEGRAM_AUTH_REQUIRED_PLACEHOLDER_USER_ID);
    }

    private Long parseTelegramUserId(String channelUserId) {
        try {
            return Long.valueOf(channelUserId);
        } catch (Exception ex) {
            log.warn("ignore non-numeric telegram channel user id, value={}", channelUserId);
            return null;
        }
    }

    private String buildTelegramBindingId(Long channelId) {
        return "agentx-channel-" + channelId;
    }

    private String buildWhatsAppBindingId(Long channelId) {
        return "agentx-channel-" + channelId;
    }

    private List<AgentxChannelConfigDO> filterChannelsByOpenfangRuntime(List<AgentxChannelConfigDO> channels) {
        if (CollUtil.isEmpty(channels)) {
            return channels;
        }
        Set<String> openfangBindingIds = fetchOpenfangWhatsAppBindingIds();
        List<AgentxChannelConfigDO> filtered = new ArrayList<>();
        for (AgentxChannelConfigDO channel : channels) {
            if (channel == null) {
                continue;
            }
            if (!StrUtil.equals("whatsapp", channel.getChannelType())) {
                filtered.add(channel);
                continue;
            }
            if (channel.getId() == null) {
                continue;
            }
            String bindingId = buildWhatsAppBindingId(channel.getId());
            if (openfangBindingIds.contains(bindingId)) {
                filtered.add(channel);
            }
        }
        return filtered;
    }

    private Set<String> fetchOpenfangWhatsAppBindingIds() {
        AgentxOpenfangInstanceDO instance = getFirstEnabledOpenfangInstance();
        if (instance == null) {
            return Collections.emptySet();
        }
        String endpoint = StrUtil.format("{}/api/channels/whatsapp/bindings",
                StrUtil.removeSuffix(instance.getEndpoint(), "/"));
        try {
            ResponseEntity<Map> response = restTemplate.exchange(endpoint, HttpMethod.GET,
                    new HttpEntity<>(buildOpenfangHeaders(instance)), Map.class);
            Map body = response.getBody();
            if (body == null) {
                return Collections.emptySet();
            }
            Object bindingsObj = body.get("bindings");
            if (!(bindingsObj instanceof List)) {
                return Collections.emptySet();
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> bindings = (List<Map<String, Object>>) bindingsObj;
            Set<String> ids = new HashSet<>();
            for (Map<String, Object> binding : bindings) {
                String bindingId = binding == null ? null : readStringField(binding, "binding_id");
                if (StrUtil.isNotBlank(bindingId)) {
                    ids.add(bindingId);
                }
            }
            return ids;
        } catch (Exception ex) {
            log.warn("fetch openfang whatsapp bindings failed, err={}", ex.getMessage());
            return Collections.emptySet();
        }
    }

    private void cleanupWhatsAppOrphanBindings(AgentxOpenfangInstanceDO instance, Long deletingChannelId) {
        String baseUrl = StrUtil.removeSuffix(instance.getEndpoint(), "/");
        String endpoint = baseUrl + "/api/channels/whatsapp/bindings";
        ResponseEntity<Map> response = restTemplate.exchange(endpoint, HttpMethod.GET,
                new HttpEntity<>(buildOpenfangHeaders(instance)), Map.class);
        Map body = response.getBody();
        if (body == null) {
            return;
        }
        Object bindingsObj = body.get("bindings");
        if (!(bindingsObj instanceof List)) {
            return;
        }
        Set<String> expected = buildExpectedWhatsAppBindingIds(deletingChannelId);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> bindings = (List<Map<String, Object>>) bindingsObj;
        for (Map<String, Object> binding : bindings) {
            String bindingId = binding == null ? null : readStringField(binding, "binding_id");
            if (StrUtil.isBlank(bindingId)) {
                continue;
            }
            if (!StrUtil.startWith(bindingId, "agentx-channel-")) {
                continue;
            }
            if (expected.contains(bindingId)) {
                continue;
            }
            String deleteEndpoint = StrUtil.format("{}/api/channels/whatsapp/bindings/{}", baseUrl, bindingId);
            restTemplate.exchange(deleteEndpoint, HttpMethod.DELETE,
                    new HttpEntity<>(buildOpenfangHeaders(instance)), Object.class);
        }
    }

    private Set<String> buildExpectedWhatsAppBindingIds(Long deletingChannelId) {
        List<AgentxChannelConfigDO> channels = channelConfigMapper.selectListByChannelType("whatsapp");
        if (CollUtil.isEmpty(channels)) {
            return Collections.emptySet();
        }
        Set<String> expected = new HashSet<>();
        for (AgentxChannelConfigDO item : channels) {
            if (item == null || item.getId() == null) {
                continue;
            }
            if (ObjectUtil.equal(item.getId(), deletingChannelId)) {
                continue;
            }
            expected.add(buildWhatsAppBindingId(item.getId()));
        }
        return expected;
    }

    private String resolveOpenfangErrorMessage(HttpStatusCodeException ex) {
        String body = StrUtil.trimToEmpty(ex.getResponseBodyAsString());
        if (StrUtil.isBlank(body)) {
            return "HTTP " + ex.getStatusCode().value();
        }
        try {
            Map<String, Object> payload = JsonUtils.parseObject(body, Map.class);
            if (payload != null) {
                Object error = payload.get("error");
                if (error != null && StrUtil.isNotBlank(String.valueOf(error))) {
                    return String.valueOf(error);
                }
                Object message = payload.get("message");
                if (message != null && StrUtil.isNotBlank(String.valueOf(message))) {
                    return String.valueOf(message);
                }
            }
        } catch (Exception ignore) {
            // ignore parse failure and fall back to raw response body
        }
        return body;
    }

    private AgentxOpenfangInstanceDO getFirstEnabledOpenfangInstance() {
        List<AgentxOpenfangInstanceDO> instances = openfangInstanceMapper.selectListByStatus(1);
        if (CollUtil.isEmpty(instances)) {
            return null;
        }
        return instances.get(0);
    }

    private HttpHeaders buildOpenfangHeaders(AgentxOpenfangInstanceDO instance) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String apiKey = openfangApiKeyCrypto.decrypt(instance.getApiKeyEncrypted());
        if (StrUtil.isNotBlank(apiKey)) {
            headers.setBearerAuth(apiKey);
        }
        return headers;
    }

    private Map<String, Object> buildOpenfangChannelFields(AgentxChannelConfigDO channel) {
        Map<String, Object> fields = new LinkedHashMap<>();
        if ("telegram".equals(channel.getChannelType())) {
            String authToken = decryptBotToken(channel.getBotTokenEncrypted());
            fields.put("bot_token_env", authToken);
        } else if ("dingtalk".equals(channel.getChannelType())) {
            String authToken = decryptBotToken(channel.getBotTokenEncrypted());
            String[] auth = splitAuthPair(authToken);
            fields.put("access_token_env", auth[0]);
            fields.put("secret_env", auth[1]);
        } else if ("wecom".equals(channel.getChannelType())) {
            String authToken = decryptBotToken(channel.getBotTokenEncrypted());
            String[] auth = splitAuthPair(authToken);
            fields.put("corp_id", auth[0]);
            fields.put("secret_env", auth[1]);
        }
        String defaultAgent = buildDefaultAgentName(channel.getId());
        if (StrUtil.isNotBlank(defaultAgent)) {
            fields.put("default_agent", defaultAgent);
        }
        return fields;
    }

    private String buildDefaultAgentName(Long channelId) {
        AgentxAgentDO agent = resolveDefaultActiveAgent(channelId);
        return agent == null ? null : buildOpenfangAgentName(agent);
    }

    private String buildOpenfangAgentName(AgentxAgentDO agent) {
        String displayName = StrUtil.blankToDefault(agent.getAgentName(), "OA 数字员工");
        return displayName + " [OA#" + agent.getId() + "]";
    }

    private String encryptBotToken(String botToken) {
        if (StrUtil.isBlank(botToken)) {
            return null;
        }
        if (StrUtil.isNotBlank(System.getenv("AGENTX_ENCRYPTION_KEY"))) {
            return openfangApiKeyCrypto.encrypt(botToken);
        }
        return BOT_TOKEN_FALLBACK_PREFIX + Base64.encode(botToken.getBytes(StandardCharsets.UTF_8));
    }

    private String decryptBotToken(String encryptedText) {
        if (StrUtil.isBlank(encryptedText)) {
            return null;
        }
        if (StrUtil.startWith(encryptedText, BOT_TOKEN_FALLBACK_PREFIX)) {
            return Base64.decodeStr(StrUtil.removePrefix(encryptedText, BOT_TOKEN_FALLBACK_PREFIX), StandardCharsets.UTF_8);
        }
        return openfangApiKeyCrypto.decrypt(encryptedText);
    }

    private AgentxChannelTestRespVO buildRespByOkField(AgentxChannelTestRespVO respVO, ResponseEntity<Map> response) {
        Object ok = response.getBody() == null ? null : response.getBody().get("ok");
        if (Boolean.TRUE.equals(ok)) {
            respVO.setSuccess(true);
            respVO.setMessage("✓ 连接成功");
            return respVO;
        }
        respVO.setSuccess(false);
        respVO.setMessage("✗ 连接失败：Token 无效");
        return respVO;
    }

    private String resolveChannelTestErrorMessage(String channelType, HttpStatusCodeException ex) {
        if ("telegram".equals(channelType)) {
            return "✗ 连接失败：Token 无效";
        }
        return "✗ 连接失败：" + ex.getStatusCode() + " " + ex.getStatusText();
    }

    private String[] splitAuthPair(String token) {
        String[] values = StrUtil.splitToArray(token, ':');
        if (values == null || values.length != 2 || StrUtil.hasBlank(values[0], values[1])) {
            throw exception(ErrorCodeConstants.CHANNEL_TEST_CONNECT_FAILED, "认证参数格式需为 key:secret");
        }
        return values;
    }

    private String readStringField(Map body, String key) {
        if (body == null) {
            return null;
        }
        Object value = body.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private Boolean readBooleanField(Map body, String key) {
        if (body == null || !body.containsKey(key)) {
            return null;
        }
        Object value = body.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if (StrUtil.isBlank(text)) {
            return null;
        }
        if ("true".equalsIgnoreCase(text) || "1".equals(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text) || "0".equals(text)) {
            return false;
        }
        return null;
    }

    private AgentxWhatsAppQrStatusRespVO queryWhatsAppQrStatus(AgentxOpenfangInstanceDO instance, String sessionId, String bindingId) {
        String endpoint = StrUtil.format("{}/api/channels/whatsapp/qr/status?session_id={}",
                StrUtil.removeSuffix(instance.getEndpoint(), "/"),
                URLEncoder.encode(sessionId, StandardCharsets.UTF_8));
        if (StrUtil.isNotBlank(bindingId)) {
            endpoint = endpoint + "&binding_id=" + URLEncoder.encode(bindingId, StandardCharsets.UTF_8);
        }
        try {
            ResponseEntity<Map> response = restTemplate.exchange(endpoint, HttpMethod.GET,
                    new HttpEntity<>(buildOpenfangHeaders(instance)), Map.class);
            return buildWhatsAppQrStatusResp(response.getBody());
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() == 404 && StrUtil.isNotBlank(bindingId)) {
                log.info("whatsapp qr session not found, fallback to runtime binding status, bindingId={}", bindingId);
                return null;
            }
            throw exception(ErrorCodeConstants.CHANNEL_SYNC_OPENFANG_FAILED, resolveOpenfangErrorMessage(ex));
        } catch (Exception ex) {
            throw exception(ErrorCodeConstants.CHANNEL_SYNC_OPENFANG_FAILED, StrUtil.blankToDefault(ex.getMessage(), "未知错误"));
        }
    }

    private AgentxWhatsAppQrStatusRespVO queryWhatsAppRuntimeBindingStatus(AgentxOpenfangInstanceDO instance, String bindingId) {
        if (StrUtil.isBlank(bindingId)) {
            return null;
        }
        Map<String, Object> binding = fetchWhatsAppRuntimeBinding(instance, bindingId);
        if (binding == null) {
            return null;
        }
        AgentxWhatsAppQrStatusRespVO respVO = buildWhatsAppQrStatusResp(binding);
        Boolean connected = readBooleanField(binding, "connected");
        if (connected == null) {
            connected = readBooleanField(binding, "available");
        }
        if (connected == null) {
            // Runtime binding presence means OpenFang still has the stable WhatsApp binding loaded.
            connected = true;
        }
        respVO.setConnected(connected);
        Boolean expired = readBooleanField(binding, "expired");
        respVO.setExpired(Boolean.TRUE.equals(expired));
        if (StrUtil.isBlank(respVO.getMessage())) {
            respVO.setMessage(connected ? "WhatsApp 已连接" : "WhatsApp 未连接");
        }
        if (StrUtil.isBlank(respVO.getSessionId())) {
            respVO.setSessionId(readStringField(binding, "session_id"));
        }
        return respVO;
    }

    private AgentxWhatsAppQrStatusRespVO buildWhatsAppQrStatusResp(Map body) {
        AgentxWhatsAppQrStatusRespVO respVO = new AgentxWhatsAppQrStatusRespVO();
        respVO.setConnected(Boolean.TRUE.equals(readBooleanField(body, "connected")));
        respVO.setExpired(Boolean.TRUE.equals(readBooleanField(body, "expired")));
        respVO.setMessage(readStringField(body, "message"));
        respVO.setQrDataUrl(firstNonBlank(readStringField(body, "qr_data_url"), readStringField(body, "qrDataUrl")));
        respVO.setSessionId(firstNonBlank(readStringField(body, "session_id"), readStringField(body, "sessionId")));
        return respVO;
    }

    private Map<String, Object> fetchWhatsAppRuntimeBinding(AgentxOpenfangInstanceDO instance, String bindingId) {
        String baseUrl = StrUtil.removeSuffix(instance.getEndpoint(), "/");
        String detailEndpoint = StrUtil.format("{}/api/channels/whatsapp/bindings/{}", baseUrl, bindingId);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(detailEndpoint, HttpMethod.GET,
                    new HttpEntity<>(buildOpenfangHeaders(instance)), Map.class);
            Map body = response.getBody();
            if (body == null) {
                return null;
            }
            Object binding = body.get("binding");
            if (binding instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) binding;
                return result;
            }
            return body;
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() != 404) {
                throw exception(ErrorCodeConstants.CHANNEL_SYNC_OPENFANG_FAILED, resolveOpenfangErrorMessage(ex));
            }
        } catch (Exception ex) {
            throw exception(ErrorCodeConstants.CHANNEL_SYNC_OPENFANG_FAILED, StrUtil.blankToDefault(ex.getMessage(), "未知错误"));
        }

        String listEndpoint = baseUrl + "/api/channels/whatsapp/bindings";
        try {
            ResponseEntity<Map> response = restTemplate.exchange(listEndpoint, HttpMethod.GET,
                    new HttpEntity<>(buildOpenfangHeaders(instance)), Map.class);
            Map body = response.getBody();
            if (body == null) {
                return null;
            }
            Object bindingsObj = body.get("bindings");
            if (!(bindingsObj instanceof List)) {
                return null;
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> bindings = (List<Map<String, Object>>) bindingsObj;
            for (Map<String, Object> binding : bindings) {
                String currentBindingId = binding == null ? null : readStringField(binding, "binding_id");
                if (StrUtil.equals(bindingId, currentBindingId)) {
                    return binding;
                }
            }
            return null;
        } catch (HttpStatusCodeException ex) {
            throw exception(ErrorCodeConstants.CHANNEL_SYNC_OPENFANG_FAILED, resolveOpenfangErrorMessage(ex));
        } catch (Exception ex) {
            throw exception(ErrorCodeConstants.CHANNEL_SYNC_OPENFANG_FAILED, StrUtil.blankToDefault(ex.getMessage(), "未知错误"));
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StrUtil.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private AgentxChannelTestRespVO testWhatsAppConnection() {
        AgentxChannelTestRespVO respVO = new AgentxChannelTestRespVO();
        AgentxOpenfangInstanceDO instance = getFirstEnabledOpenfangInstance();
        if (instance == null) {
            respVO.setSuccess(false);
            respVO.setMessage("✗ 连接失败：未找到可用的 OpenFang 实例");
            return respVO;
        }
        String endpoint = StrUtil.format("{}/api/channels/whatsapp/qr/start", StrUtil.removeSuffix(instance.getEndpoint(), "/"));
        try {
            ResponseEntity<Map> response = restTemplate.exchange(endpoint, HttpMethod.POST,
                    new HttpEntity<>(Collections.emptyMap(), buildOpenfangHeaders(instance)), Map.class);
            Map body = response.getBody();
            boolean available = body != null && Boolean.TRUE.equals(body.get("available"));
            boolean connected = body != null && Boolean.TRUE.equals(body.get("connected"));
            if (connected) {
                respVO.setSuccess(true);
                respVO.setMessage("✓ 连接成功：WhatsApp 已连接");
                return respVO;
            }
            if (available) {
                respVO.setSuccess(true);
                respVO.setMessage("✓ 连接成功：已就绪，可在 OpenFang 侧扫码绑定 WhatsApp");
                return respVO;
            }
            Object rawMessage = body == null ? null : body.get("message");
            String message = rawMessage == null ? null : String.valueOf(rawMessage);
            respVO.setSuccess(false);
            respVO.setMessage("✗ 连接失败：" + StrUtil.blankToDefault(message, "WhatsApp 网关不可用"));
            return respVO;
        } catch (HttpStatusCodeException ex) {
            respVO.setSuccess(false);
            respVO.setMessage("✗ 连接失败：" + resolveOpenfangErrorMessage(ex));
            return respVO;
        } catch (Exception ex) {
            respVO.setSuccess(false);
            respVO.setMessage("✗ 连接失败：" + StrUtil.blankToDefault(ex.getMessage(), "未知错误"));
            return respVO;
        }
    }

    private boolean isBotTokenRequired(String channelType) {
        return !"whatsapp".equals(channelType);
    }

    private boolean supportsBindRequiredAuth(String channelType) {
        return "telegram".equals(channelType) || "whatsapp".equals(channelType);
    }

}
