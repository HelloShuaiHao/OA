package cn.iocoder.yudao.module.agentx.service.channel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Service
@Validated
public class AgentxChannelServiceImpl implements AgentxChannelService {

    private static final Set<String> VALID_CHANNEL_TYPES = new HashSet<>(Arrays.asList("telegram", "wecom", "dingtalk"));
    private static final String BOT_TOKEN_FALLBACK_PREFIX = "b64:";
    private static final String AUTH_MODE_PUBLIC = "public";
    private static final String AUTH_MODE_BIND_REQUIRED = "bind_required";

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
        if (StrUtil.isBlank(reqVO.getBotToken())) {
            throw exception(ErrorCodeConstants.CHANNEL_BOT_TOKEN_REQUIRED);
        }
        AgentxChannelConfigDO channel = new AgentxChannelConfigDO()
                .setChannelType(reqVO.getChannelType())
                .setChannelName(reqVO.getChannelName())
                .setBotTokenEncrypted(encryptBotToken(reqVO.getBotToken()))
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
        return BeanUtils.toBean(page, AgentxChannelConfigRespVO.class, resp -> {
            AgentxChannelConfigDO channel = page.getList().stream()
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
                .filter(item -> ObjectUtil.equal(item.getStatus(), 1))
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
        channelAgentMapper.deleteByChannelId(channelId);
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
        if (!ObjectUtil.equal(channel.getStatus(), 1)) {
            removeChannelFromOpenfang(channel);
            return;
        }
        AgentxOpenfangInstanceDO instance = getFirstEnabledOpenfangInstance();
        if (instance == null) {
            metricsService.recordOpenfangCall(false);
            return;
        }
        try {
            String endpoint = StrUtil.format("{}/api/channels/{}/configure",
                    StrUtil.removeSuffix(instance.getEndpoint(), "/"), channel.getChannelType());
            restTemplate.exchange(endpoint, HttpMethod.POST,
                    new HttpEntity<>(Collections.singletonMap("fields", buildOpenfangChannelFields(channel)),
                            buildOpenfangHeaders(instance)),
                    Object.class);
            metricsService.recordOpenfangCall(true);
        } catch (Exception ex) {
            metricsService.recordOpenfangCall(false);
        }
    }

    private void removeChannelFromOpenfang(AgentxChannelConfigDO channel) {
        AgentxOpenfangInstanceDO instance = getFirstEnabledOpenfangInstance();
        if (instance == null) {
            metricsService.recordOpenfangCall(false);
            return;
        }
        try {
            String endpoint = StrUtil.format("{}/api/channels/{}/configure",
                    StrUtil.removeSuffix(instance.getEndpoint(), "/"), channel.getChannelType());
            restTemplate.exchange(endpoint, HttpMethod.DELETE, new HttpEntity<>(buildOpenfangHeaders(instance)), Object.class);
            metricsService.recordOpenfangCall(true);
        } catch (Exception ex) {
            metricsService.recordOpenfangCall(false);
        }
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
        String authToken = decryptBotToken(channel.getBotTokenEncrypted());
        if ("telegram".equals(channel.getChannelType())) {
            fields.put("bot_token_env", authToken);
        } else if ("dingtalk".equals(channel.getChannelType())) {
            String[] auth = splitAuthPair(authToken);
            fields.put("access_token_env", auth[0]);
            fields.put("secret_env", auth[1]);
        } else if ("wecom".equals(channel.getChannelType())) {
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
        List<AgentxChannelAgentDO> relations = channelAgentMapper.selectListByChannelId(channelId);
        if (CollUtil.isEmpty(relations)) {
            return null;
        }
        AgentxAgentDO agent = agentMapper.selectById(relations.get(0).getAgentId());
        if (agent == null || StrUtil.isBlank(agent.getAgentKey())) {
            return null;
        }
        return "oa-agent-" + StrUtil.subPre(agent.getAgentKey(), 24);
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

}
