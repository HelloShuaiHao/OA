package cn.iocoder.yudao.module.agentx.service.access;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.agentx.config.AgentxEntitlementProperties;
import cn.iocoder.yudao.module.agentx.controller.admin.access.vo.AccessEnvelopeRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.access.vo.AccessEvaluateReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxBindGenerateReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxBindGenerateRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.entitlement.vo.EntitlementObligationVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.channel.AgentxUserChannelBindingDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.entitlement.AgentxUserEntitlementDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.agent.AgentxAgentMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.channel.AgentxUserChannelBindingMapper;
import cn.iocoder.yudao.module.agentx.service.accessaudit.AgentxAccessAuditService;
import cn.iocoder.yudao.module.agentx.service.channel.AgentxBindService;
import cn.iocoder.yudao.module.agentx.service.entitlement.AgentxEntitlementService;
import cn.iocoder.yudao.module.agentx.util.DecisionIdGenerator;
import cn.iocoder.yudao.module.agentx.util.EnvelopeSignatureUtil;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Validated
public class AgentxAccessServiceImpl implements AgentxAccessService {

    private static final String DECISION_ALLOW = "ALLOW";
    private static final String DECISION_DENY = "DENY";
    private static final String DECISION_REQUIRE_BINDING = "REQUIRE_BINDING";
    private static final String CONTEXT_CACHE_KEY_PREFIX = "agentx:context:";

    @Resource
    private AgentxUserChannelBindingMapper userChannelBindingMapper;
    @Resource
    private AgentxAgentMapper agentMapper;
    @Resource
    private AgentxBindService bindService;
    @Resource
    private AgentxEntitlementService entitlementService;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private AgentxEntitlementProperties entitlementProperties;
    @Resource
    private DecisionIdGenerator decisionIdGenerator;
    @Resource
    private EnvelopeSignatureUtil signatureUtil;
    @Resource
    private AgentxAccessAuditService accessAuditService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public AccessEnvelopeRespVO evaluateAccess(AccessEvaluateReqVO reqVO) {
        String decisionId = decisionIdGenerator.generate();
        String resolvedAgentId = resolveAgentId(reqVO);
        if (StrUtil.isBlank(resolvedAgentId)) {
            accessAuditService.logAccessDecision(decisionId, null, reqVO.getChannelUserId(), null,
                    reqVO.getConversationScope(), DECISION_DENY, "missing_agent_id_or_key", entitlementProperties.getPolicyVersion());
            return buildDecision(DECISION_DENY, null, null, "请求参数不正确：agentId 或 agentKey 不能为空",
                    false, false, false);
        }
        AccessEnvelopeRespVO cached = readCachedEnvelope(reqVO, resolvedAgentId);
        if (cached != null) {
            accessAuditService.logAccessDecision(decisionId,
                    cached.getSystemEnforcedContext().getUserId(), reqVO.getChannelUserId(), resolvedAgentId,
                    reqVO.getConversationScope(), DECISION_ALLOW, null, cached.getSystemEnforcedContext().getPolicyVersion());
            return cached;
        }

        String[] channelInfo = parseChannelIdentity(reqVO);
        if (channelInfo == null) {
            accessAuditService.logAccessDecision(decisionId, null, reqVO.getChannelUserId(), resolvedAgentId,
                    reqVO.getConversationScope(), DECISION_DENY, "invalid_channel_user_id", entitlementProperties.getPolicyVersion());
            return buildDecision(DECISION_DENY, null, null, "渠道用户标识不合法，格式应为 channel:userId",
                    false, false, false);
        }

        AgentxUserChannelBindingDO binding = userChannelBindingMapper.selectByChannelIdentity(channelInfo[0], channelInfo[1]);
        if (binding == null) {
            AccessEnvelopeRespVO result = new AccessEnvelopeRespVO();
            result.setAccessDecision(DECISION_REQUIRE_BINDING);
            applyLegacyFields(result, false, true, false,
                    "bind_required", null, null, "当前 Agent 需要先完成身份绑定");
            AgentxBindGenerateReqVO bindReqVO = new AgentxBindGenerateReqVO();
            bindReqVO.setChannelType(channelInfo[0]);
            bindReqVO.setChannelUserId(channelInfo[1]);
            AgentxBindGenerateRespVO bindRespVO = bindService.generateBindLink(bindReqVO);
            result.setBindingUrl(bindRespVO.getBindUrl());
            result.setBindUrl(bindRespVO.getBindUrl());
            result.setBindToken(bindRespVO.getToken());
            accessAuditService.logAccessDecision(decisionId, null, reqVO.getChannelUserId(), resolvedAgentId,
                    reqVO.getConversationScope(), DECISION_REQUIRE_BINDING, "user_not_bound", entitlementProperties.getPolicyVersion());
            return result;
        }

        AgentxUserEntitlementDO entitlement = entitlementService.getEffectiveEntitlement(binding.getUserId(), resolvedAgentId);
        if (entitlement == null) {
            accessAuditService.logAccessDecision(decisionId, binding.getUserId(), reqVO.getChannelUserId(), resolvedAgentId,
                    reqVO.getConversationScope(), DECISION_ALLOW, null, entitlementProperties.getPolicyVersion());
            return buildBoundAllowDecision(decisionId, binding.getUserId(), reqVO.getChannelUserId(), resolvedAgentId);
        }

        AccessEnvelopeRespVO envelope = new AccessEnvelopeRespVO();
        envelope.setAccessDecision(DECISION_ALLOW);
        applyLegacyFields(envelope, true, true, true,
                "bind_required", null, null, "绑定校验通过，允许触发 Agent 能力");
        envelope.setModelVisibleContext(buildModelVisibleContext(binding.getUserId(), entitlement));
        envelope.setSystemEnforcedContext(buildSystemContext(decisionId, binding.getUserId(),
                reqVO.getChannelUserId(), resolvedAgentId, entitlement));
        writeCachedEnvelope(reqVO, resolvedAgentId, envelope);

        accessAuditService.logAccessDecision(decisionId, binding.getUserId(), reqVO.getChannelUserId(), resolvedAgentId,
                reqVO.getConversationScope(), DECISION_ALLOW, null, envelope.getSystemEnforcedContext().getPolicyVersion());
        return envelope;
    }

    @Override
    public boolean verifyEnvelope(AccessEnvelopeRespVO.SystemEnforcedContext context) {
        return signatureUtil.verify(context);
    }

    private AccessEnvelopeRespVO buildDecision(String decision, String bindingUrl, String bindToken, String message,
                                               boolean accessGranted, boolean authRequired, boolean bound) {
        AccessEnvelopeRespVO respVO = new AccessEnvelopeRespVO();
        respVO.setAccessDecision(decision);
        respVO.setBindingUrl(bindingUrl);
        applyLegacyFields(respVO, accessGranted, authRequired, bound,
                authRequired ? "bind_required" : null, null, bindToken, message);
        if (StrUtil.isNotBlank(message)) {
            AccessEnvelopeRespVO.ModelVisibleContext visible = new AccessEnvelopeRespVO.ModelVisibleContext();
            AccessEnvelopeRespVO.CapabilitySummary summary = new AccessEnvelopeRespVO.CapabilitySummary();
            summary.setCannotDo(CollUtil.newArrayList(message));
            visible.setCapabilitySummary(summary);
            respVO.setModelVisibleContext(visible);
        }
        return respVO;
    }

    private AccessEnvelopeRespVO buildBoundAllowDecision(String decisionId, Long userId, String channelUserId, String agentId) {
        AccessEnvelopeRespVO respVO = new AccessEnvelopeRespVO();
        respVO.setAccessDecision(DECISION_ALLOW);
        applyLegacyFields(respVO, true, true, true,
                "bind_required", null, null, "绑定校验通过，允许触发 Agent 能力");
        respVO.setModelVisibleContext(buildDefaultModelVisibleContext(userId));
        respVO.setSystemEnforcedContext(buildDefaultSystemContext(decisionId, userId, channelUserId, agentId));
        return respVO;
    }

    private void applyLegacyFields(AccessEnvelopeRespVO respVO, boolean accessGranted, boolean authRequired,
                                   boolean bound, String authMode, String accessControlType,
                                   String bindToken, String message) {
        respVO.setAccessGranted(accessGranted);
        respVO.setAuthRequired(authRequired);
        respVO.setBound(bound);
        respVO.setAuthMode(authMode);
        respVO.setAccessControlType(accessControlType);
        respVO.setBindUrl(respVO.getBindingUrl());
        respVO.setBindToken(bindToken);
        respVO.setMessage(message);
    }

    private AccessEnvelopeRespVO.ModelVisibleContext buildModelVisibleContext(Long userId, AgentxUserEntitlementDO entitlement) {
        AccessEnvelopeRespVO.ModelVisibleContext context = new AccessEnvelopeRespVO.ModelVisibleContext();

        AccessEnvelopeRespVO.SubjectProfile profile = new AccessEnvelopeRespVO.SubjectProfile();
        AdminUserDO user = adminUserService.getUser(userId);
        profile.setDisplayName(user == null ? null : user.getNickname());
        profile.setDeptName(entitlement.getDeptName());
        profile.setJobTitle(entitlement.getJobTitle());
        profile.setRoleTags(parseStringList(entitlement.getRoleTags()));
        profile.setWorkRegion(entitlement.getWorkRegion());
        context.setSubjectProfile(profile);

        AccessEnvelopeRespVO.CapabilitySummary summary = new AccessEnvelopeRespVO.CapabilitySummary();
        List<String> actions = parseStringList(entitlement.getAllowedActions());
        List<String> canDo = new ArrayList<>();
        for (String action : actions) {
            canDo.add("允许执行：" + action);
        }
        summary.setCanDo(canDo);
        summary.setCannotDo(buildCannotDo(entitlement.getResourceFilters()));
        summary.setRequiresApproval(buildRequireApproval(entitlement.getObligations()));
        context.setCapabilitySummary(summary);

        return context;
    }

    private AccessEnvelopeRespVO.ModelVisibleContext buildDefaultModelVisibleContext(Long userId) {
        AccessEnvelopeRespVO.ModelVisibleContext context = new AccessEnvelopeRespVO.ModelVisibleContext();
        AccessEnvelopeRespVO.SubjectProfile profile = new AccessEnvelopeRespVO.SubjectProfile();
        AdminUserDO user = adminUserService.getUser(userId);
        if (user != null) {
            profile.setDisplayName(user.getNickname());
        }
        context.setSubjectProfile(profile);

        AccessEnvelopeRespVO.CapabilitySummary summary = new AccessEnvelopeRespVO.CapabilitySummary();
        summary.setCanDo(CollUtil.newArrayList("已完成身份绑定，可按当前数字员工职责提供服务"));
        summary.setCannotDo(new ArrayList<>());
        summary.setRequiresApproval(new ArrayList<>());
        context.setCapabilitySummary(summary);
        return context;
    }

    private AccessEnvelopeRespVO.SystemEnforcedContext buildSystemContext(String decisionId, Long userId,
                                                                          String channelUserId, String agentId,
                                                                          AgentxUserEntitlementDO entitlement) {
        AccessEnvelopeRespVO.SystemEnforcedContext context = new AccessEnvelopeRespVO.SystemEnforcedContext();
        context.setUserId(userId);
        context.setChannelUserId(channelUserId);
        context.setAgentId(agentId);
        context.setAllowedActions(parseStringList(entitlement.getAllowedActions()));
        context.setResourceFilters(parseMap(entitlement.getResourceFilters()));
        context.setObligations(parseObligations(entitlement.getObligations()));
        context.setPolicyVersion(entitlementProperties.getPolicyVersion());
        context.setDecisionId(decisionId);
        context.setIssuedAt(LocalDateTime.now());
        context.setExpiresAt(context.getIssuedAt().plusMinutes(entitlementProperties.getEnvelopeTtlMinutes()));
        context.setSignature(signatureUtil.sign(context));
        return context;
    }

    private AccessEnvelopeRespVO.SystemEnforcedContext buildDefaultSystemContext(String decisionId, Long userId,
                                                                                 String channelUserId, String agentId) {
        AccessEnvelopeRespVO.SystemEnforcedContext context = new AccessEnvelopeRespVO.SystemEnforcedContext();
        context.setUserId(userId);
        context.setChannelUserId(channelUserId);
        context.setAgentId(agentId);
        context.setAllowedActions(new ArrayList<>());
        context.setResourceFilters(new HashMap<>());
        context.setObligations(new ArrayList<>());
        context.setPolicyVersion(entitlementProperties.getPolicyVersion());
        context.setDecisionId(decisionId);
        context.setIssuedAt(LocalDateTime.now());
        context.setExpiresAt(context.getIssuedAt().plusMinutes(entitlementProperties.getEnvelopeTtlMinutes()));
        context.setSignature(signatureUtil.sign(context));
        return context;
    }

    private String[] parseChannelIdentity(AccessEvaluateReqVO reqVO) {
        String channelUserId = reqVO.getChannelUserId();
        if (StrUtil.isBlank(channelUserId)) {
            return null;
        }
        if (StrUtil.isNotBlank(reqVO.getChannelType())) {
            return new String[]{reqVO.getChannelType(), channelUserId};
        }
        if (!channelUserId.contains(":")) {
            return null;
        }
        String[] parts = channelUserId.split(":", 2);
        if (parts.length < 2 || StrUtil.hasBlank(parts[0], parts[1])) {
            return null;
        }
        return parts;
    }

    private String resolveAgentId(AccessEvaluateReqVO reqVO) {
        String agentId = StrUtil.trimToNull(reqVO.getAgentId());
        if (StrUtil.isBlank(agentId)) {
            agentId = StrUtil.trimToNull(reqVO.getAgentKey());
        }
        if (StrUtil.isBlank(agentId)) {
            return null;
        }
        if (!StrUtil.isNumeric(agentId)) {
            return agentId;
        }
        AgentxAgentDO agent = agentMapper.selectById(Long.valueOf(agentId));
        if (agent != null && StrUtil.isNotBlank(agent.getAgentKey())) {
            return agent.getAgentKey();
        }
        return agentId;
    }

    private List<String> parseStringList(String json) {
        if (StrUtil.isBlank(json)) {
            return new ArrayList<>();
        }
        return JsonUtils.parseArray(json, String.class);
    }

    private Map<String, Object> parseMap(String json) {
        if (StrUtil.isBlank(json)) {
            return new HashMap<>();
        }
        return JsonUtils.parseObject(json, Map.class);
    }

    private List<AccessEnvelopeRespVO.Obligation> parseObligations(String json) {
        List<AccessEnvelopeRespVO.Obligation> list = new ArrayList<>();
        if (StrUtil.isBlank(json)) {
            return list;
        }
        List<EntitlementObligationVO> obligations = JsonUtils.parseArray(json, EntitlementObligationVO.class);
        for (EntitlementObligationVO item : obligations) {
            AccessEnvelopeRespVO.Obligation obligation = new AccessEnvelopeRespVO.Obligation();
            obligation.setAction(item.getAction());
            obligation.setRequires(item.getRequires());
            obligation.setApproverRole(item.getApproverRole());
            obligation.setReason(item.getReason());
            list.add(obligation);
        }
        return list;
    }

    private List<String> buildCannotDo(String resourceFiltersJson) {
        if (StrUtil.isBlank(resourceFiltersJson)) {
            return CollUtil.newArrayList("未授权的资源范围不可访问");
        }
        Map<String, Object> resourceFilters = JsonUtils.parseObject(resourceFiltersJson, Map.class);
        if (ObjectUtil.isEmpty(resourceFilters)) {
            return CollUtil.newArrayList("未授权的资源范围不可访问");
        }
        List<String> cannotDo = new ArrayList<>();
        for (String key : resourceFilters.keySet()) {
            cannotDo.add("超出 " + key + " 范围的数据不可访问");
        }
        return cannotDo;
    }

    private List<String> buildRequireApproval(String obligationsJson) {
        List<String> result = new ArrayList<>();
        if (StrUtil.isBlank(obligationsJson)) {
            return result;
        }
        List<EntitlementObligationVO> obligations = JsonUtils.parseArray(obligationsJson, EntitlementObligationVO.class);
        for (EntitlementObligationVO item : obligations) {
            if (StrUtil.isNotBlank(item.getReason())) {
                result.add(item.getReason());
            } else if (StrUtil.isNotBlank(item.getAction())) {
                result.add(item.getAction() + " 需要审批");
            }
        }
        return result;
    }

    private AccessEnvelopeRespVO readCachedEnvelope(AccessEvaluateReqVO reqVO, String resolvedAgentId) {
        if (stringRedisTemplate == null) {
            return null;
        }
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String key = buildCacheKey(reqVO, resolvedAgentId);
        String json = ops.get(key);
        if (StrUtil.isBlank(json)) {
            return null;
        }
        AccessEnvelopeRespVO cached = JsonUtils.parseObject(json, AccessEnvelopeRespVO.class);
        if (cached == null || cached.getSystemEnforcedContext() == null) {
            return null;
        }
        if (!StrUtil.equals(entitlementProperties.getPolicyVersion(),
                cached.getSystemEnforcedContext().getPolicyVersion())) {
            stringRedisTemplate.delete(key);
            return null;
        }
        if (cached.getSystemEnforcedContext().getExpiresAt() != null
                && cached.getSystemEnforcedContext().getExpiresAt().isBefore(LocalDateTime.now())) {
            stringRedisTemplate.delete(key);
            return null;
        }
        if (!signatureUtil.verify(cached.getSystemEnforcedContext())) {
            stringRedisTemplate.delete(key);
            return null;
        }
        return cached;
    }

    private void writeCachedEnvelope(AccessEvaluateReqVO reqVO, String resolvedAgentId, AccessEnvelopeRespVO envelope) {
        if (stringRedisTemplate == null || envelope == null || envelope.getSystemEnforcedContext() == null) {
            return;
        }
        stringRedisTemplate.opsForValue().set(buildCacheKey(reqVO, resolvedAgentId),
                JsonUtils.toJsonString(envelope),
                entitlementProperties.getEnvelopeTtlMinutes(), TimeUnit.MINUTES);
    }

    private String buildCacheKey(AccessEvaluateReqVO reqVO, String resolvedAgentId) {
        return CONTEXT_CACHE_KEY_PREFIX
                + StrUtil.blankToDefault(reqVO.getChannelUserId(), "-") + ":"
                + StrUtil.blankToDefault(resolvedAgentId, "-") + ":"
                + StrUtil.blankToDefault(reqVO.getConversationScope(), "default");
    }

}
