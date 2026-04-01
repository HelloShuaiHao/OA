package cn.iocoder.yudao.module.agentx.service.access;

import cn.iocoder.yudao.module.agentx.config.AgentxEntitlementProperties;
import cn.iocoder.yudao.module.agentx.controller.admin.access.vo.AccessEnvelopeRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.access.vo.AccessEvaluateReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxBindGenerateRespVO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentxAccessServiceImplTest {

    private final AgentxUserChannelBindingMapper bindingMapper = Mockito.mock(AgentxUserChannelBindingMapper.class);
    private final AgentxAgentMapper agentMapper = Mockito.mock(AgentxAgentMapper.class);
    private final AgentxBindService bindService = Mockito.mock(AgentxBindService.class);
    private final AgentxEntitlementService entitlementService = Mockito.mock(AgentxEntitlementService.class);
    private final AdminUserService adminUserService = Mockito.mock(AdminUserService.class);
    private final AgentxAccessAuditService accessAuditService = Mockito.mock(AgentxAccessAuditService.class);
    private final DecisionIdGenerator decisionIdGenerator = Mockito.mock(DecisionIdGenerator.class);
    private final StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

    private final Map<String, String> cache = new HashMap<>();

    private final AgentxEntitlementProperties properties = new AgentxEntitlementProperties();
    private final EnvelopeSignatureUtil signatureUtil = new EnvelopeSignatureUtil();

    private AgentxAccessServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        properties.setSignatureSecret("ut-sign-secret");
        properties.setPolicyVersion("v2026.04.01");
        properties.setEnvelopeTtlMinutes(5);

        setField(signatureUtil, "entitlementProperties", properties);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenAnswer(invocation -> cache.get(invocation.getArgument(0)));
        Mockito.doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            String value = invocation.getArgument(1);
            cache.put(key, value);
            return null;
        }).when(valueOps).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        when(decisionIdGenerator.generate()).thenReturn("dec_ut_1", "dec_ut_2", "dec_ut_3");

        service = new AgentxAccessServiceImpl();
        setField(service, "userChannelBindingMapper", bindingMapper);
        setField(service, "agentMapper", agentMapper);
        setField(service, "bindService", bindService);
        setField(service, "entitlementService", entitlementService);
        setField(service, "adminUserService", adminUserService);
        setField(service, "entitlementProperties", properties);
        setField(service, "decisionIdGenerator", decisionIdGenerator);
        setField(service, "signatureUtil", signatureUtil);
        setField(service, "accessAuditService", accessAuditService);
        setField(service, "stringRedisTemplate", redisTemplate);
    }

    @Test
    void shouldEvaluateAccessAllowAndUseCache() {
        AgentxUserChannelBindingDO binding = new AgentxUserChannelBindingDO();
        binding.setUserId(99L);
        when(bindingMapper.selectByChannelIdentity("telegram", "10001")).thenReturn(binding);

        AgentxUserEntitlementDO entitlement = new AgentxUserEntitlementDO();
        entitlement.setAllowedActions("[\"route.read\"]");
        entitlement.setRoleTags("[\"dispatcher\"]");
        entitlement.setResourceFilters("{\"region_codes\":[\"east\"]}");
        entitlement.setObligations("[{\"action\":\"route.plan.cross_region\",\"requires\":\"approval\",\"reason\":\"跨区域调度需要审批\"}]");
        entitlement.setDeptName("物流调度部");
        entitlement.setJobTitle("调度员");
        entitlement.setWorkRegion("华东区");
        when(entitlementService.getEffectiveEntitlement(99L, "dispatch-assistant")).thenReturn(entitlement);

        AdminUserDO user = new AdminUserDO();
        user.setNickname("张三");
        when(adminUserService.getUser(99L)).thenReturn(user);

        AccessEvaluateReqVO reqVO = new AccessEvaluateReqVO();
        reqVO.setChannelUserId("telegram:10001");
        reqVO.setAgentId("dispatch-assistant");
        reqVO.setConversationScope("session_1");

        AccessEnvelopeRespVO first = service.evaluateAccess(reqVO);
        assertEquals("ALLOW", first.getAccessDecision());
        assertNotNull(first.getModelVisibleContext());
        assertNotNull(first.getSystemEnforcedContext());
        assertTrue(service.verifyEnvelope(first.getSystemEnforcedContext()));

        AccessEnvelopeRespVO second = service.evaluateAccess(reqVO);
        assertEquals("ALLOW", second.getAccessDecision());
        assertEquals(first.getSystemEnforcedContext().getPolicyVersion(), second.getSystemEnforcedContext().getPolicyVersion());

        verify(bindingMapper, Mockito.times(1)).selectByChannelIdentity("telegram", "10001");
    }

    @Test
    void shouldReturnRequireBindingWhenUserNotBound() {
        when(bindingMapper.selectByChannelIdentity("telegram", "10001")).thenReturn(null);
        AgentxBindGenerateRespVO bindRespVO = new AgentxBindGenerateRespVO();
        bindRespVO.setBindUrl("https://oa.example.com/agentx/bind?token=abc");
        bindRespVO.setToken("abc");
        when(bindService.generateBindLink(any())).thenReturn(bindRespVO);

        AccessEvaluateReqVO reqVO = new AccessEvaluateReqVO();
        reqVO.setChannelUserId("telegram:10001");
        reqVO.setAgentId("dispatch-assistant");
        reqVO.setConversationScope("session_2");

        AccessEnvelopeRespVO result = service.evaluateAccess(reqVO);
        assertEquals("REQUIRE_BINDING", result.getAccessDecision());
        assertNotNull(result.getBindingUrl());
        assertEquals(result.getBindingUrl(), result.getBindUrl());
        assertEquals("abc", result.getBindToken());
        assertFalse(result.getAccessGranted());
        assertTrue(result.getAuthRequired());
        assertFalse(result.getBound());
        assertNull(result.getSystemEnforcedContext());
    }

    @Test
    void shouldInvalidateCacheWhenPolicyVersionChanged() {
        AgentxUserChannelBindingDO binding = new AgentxUserChannelBindingDO();
        binding.setUserId(99L);
        when(bindingMapper.selectByChannelIdentity("telegram", "10001")).thenReturn(binding);

        AgentxUserEntitlementDO entitlement = new AgentxUserEntitlementDO();
        entitlement.setAllowedActions("[\"route.read\"]");
        entitlement.setRoleTags("[]");
        entitlement.setResourceFilters("{}");
        entitlement.setObligations("[]");
        when(entitlementService.getEffectiveEntitlement(99L, "dispatch-assistant")).thenReturn(entitlement);

        when(adminUserService.getUser(99L)).thenReturn(new AdminUserDO());

        AccessEvaluateReqVO reqVO = new AccessEvaluateReqVO();
        reqVO.setChannelUserId("telegram:10001");
        reqVO.setAgentId("dispatch-assistant");
        reqVO.setConversationScope("session_3");

        AccessEnvelopeRespVO first = service.evaluateAccess(reqVO);
        assertEquals("v2026.04.01", first.getSystemEnforcedContext().getPolicyVersion());

        properties.setPolicyVersion("v2026.04.02");

        AccessEnvelopeRespVO second = service.evaluateAccess(reqVO);
        assertEquals("v2026.04.02", second.getSystemEnforcedContext().getPolicyVersion());

        verify(bindingMapper, Mockito.times(2)).selectByChannelIdentity("telegram", "10001");
    }

    @Test
    void shouldIsolateContextByChannelUserInGroupConversation() {
        AgentxUserChannelBindingDO bindingA = new AgentxUserChannelBindingDO();
        bindingA.setUserId(100L);
        AgentxUserChannelBindingDO bindingB = new AgentxUserChannelBindingDO();
        bindingB.setUserId(200L);
        when(bindingMapper.selectByChannelIdentity("telegram", "userA")).thenReturn(bindingA);
        when(bindingMapper.selectByChannelIdentity("telegram", "userB")).thenReturn(bindingB);

        AgentxUserEntitlementDO entitlementA = new AgentxUserEntitlementDO();
        entitlementA.setAllowedActions("[\"route.read\"]");
        entitlementA.setRoleTags("[\"dispatcher_a\"]");
        entitlementA.setResourceFilters("{\"region_codes\":[\"east\"]}");
        entitlementA.setObligations("[]");
        entitlementA.setDeptName("调度A组");
        entitlementA.setJobTitle("调度员");
        entitlementA.setWorkRegion("华东区");
        AgentxUserEntitlementDO entitlementB = new AgentxUserEntitlementDO();
        entitlementB.setAllowedActions("[\"route.read\",\"route.plan.adjust\"]");
        entitlementB.setRoleTags("[\"dispatcher_b\"]");
        entitlementB.setResourceFilters("{\"region_codes\":[\"west\"]}");
        entitlementB.setObligations("[]");
        entitlementB.setDeptName("调度B组");
        entitlementB.setJobTitle("调度员");
        entitlementB.setWorkRegion("华西区");
        when(entitlementService.getEffectiveEntitlement(100L, "dispatch-assistant")).thenReturn(entitlementA);
        when(entitlementService.getEffectiveEntitlement(200L, "dispatch-assistant")).thenReturn(entitlementB);

        AdminUserDO userA = new AdminUserDO();
        userA.setNickname("用户A");
        AdminUserDO userB = new AdminUserDO();
        userB.setNickname("用户B");
        when(adminUserService.getUser(100L)).thenReturn(userA);
        when(adminUserService.getUser(200L)).thenReturn(userB);

        AccessEvaluateReqVO reqA = new AccessEvaluateReqVO();
        reqA.setChannelUserId("telegram:userA");
        reqA.setAgentId("dispatch-assistant");
        reqA.setConversationScope("group:1000");
        AccessEvaluateReqVO reqB = new AccessEvaluateReqVO();
        reqB.setChannelUserId("telegram:userB");
        reqB.setAgentId("dispatch-assistant");
        reqB.setConversationScope("group:1000");

        AccessEnvelopeRespVO envelopeA = service.evaluateAccess(reqA);
        AccessEnvelopeRespVO envelopeB = service.evaluateAccess(reqB);

        assertEquals(100L, envelopeA.getSystemEnforcedContext().getUserId());
        assertEquals(200L, envelopeB.getSystemEnforcedContext().getUserId());
        assertNotEquals(
                envelopeA.getSystemEnforcedContext().getAllowedActions(),
                envelopeB.getSystemEnforcedContext().getAllowedActions());

        AccessEnvelopeRespVO envelopeA2 = service.evaluateAccess(reqA);
        AccessEnvelopeRespVO envelopeB2 = service.evaluateAccess(reqB);
        assertEquals(100L, envelopeA2.getSystemEnforcedContext().getUserId());
        assertEquals(200L, envelopeB2.getSystemEnforcedContext().getUserId());
        verify(bindingMapper, Mockito.times(1)).selectByChannelIdentity("telegram", "userA");
        verify(bindingMapper, Mockito.times(1)).selectByChannelIdentity("telegram", "userB");
    }

    @Test
    void shouldSupportLegacyPayloadWithAgentKeyAndSeparatedChannelType() {
        AgentxUserChannelBindingDO binding = new AgentxUserChannelBindingDO();
        binding.setUserId(99L);
        when(bindingMapper.selectByChannelIdentity("telegram", "10001")).thenReturn(binding);

        AgentxUserEntitlementDO entitlement = new AgentxUserEntitlementDO();
        entitlement.setAllowedActions("[\"route.read\"]");
        entitlement.setRoleTags("[]");
        entitlement.setResourceFilters("{}");
        entitlement.setObligations("[]");
        when(entitlementService.getEffectiveEntitlement(99L, "dispatch-assistant")).thenReturn(entitlement);
        when(adminUserService.getUser(99L)).thenReturn(new AdminUserDO());

        AccessEvaluateReqVO reqVO = new AccessEvaluateReqVO();
        reqVO.setChannelType("telegram");
        reqVO.setChannelUserId("10001");
        reqVO.setAgentKey("dispatch-assistant");
        reqVO.setConversationScope("legacy_session");

        AccessEnvelopeRespVO result = service.evaluateAccess(reqVO);
        assertEquals("ALLOW", result.getAccessDecision());
        assertEquals("dispatch-assistant", result.getSystemEnforcedContext().getAgentId());
        assertTrue(result.getAccessGranted());
        assertTrue(result.getAuthRequired());
        assertTrue(result.getBound());
        assertEquals("绑定校验通过，允许触发 Agent 能力", result.getMessage());
    }

    @Test
    void shouldResolveNumericAgentIdToAgentKeyForLegacyCaller() {
        AgentxAgentDO agent = new AgentxAgentDO();
        agent.setId(1L);
        agent.setAgentKey("dispatch-assistant");
        when(agentMapper.selectById(1L)).thenReturn(agent);

        AgentxUserChannelBindingDO binding = new AgentxUserChannelBindingDO();
        binding.setUserId(88L);
        when(bindingMapper.selectByChannelIdentity("telegram", "10002")).thenReturn(binding);

        AgentxUserEntitlementDO entitlement = new AgentxUserEntitlementDO();
        entitlement.setAllowedActions("[\"route.read\"]");
        entitlement.setRoleTags("[]");
        entitlement.setResourceFilters("{}");
        entitlement.setObligations("[]");
        when(entitlementService.getEffectiveEntitlement(88L, "dispatch-assistant")).thenReturn(entitlement);
        when(adminUserService.getUser(88L)).thenReturn(new AdminUserDO());

        AccessEvaluateReqVO reqVO = new AccessEvaluateReqVO();
        reqVO.setChannelUserId("telegram:10002");
        reqVO.setAgentId("1");
        reqVO.setConversationScope("legacy_session_2");

        AccessEnvelopeRespVO result = service.evaluateAccess(reqVO);
        assertEquals("ALLOW", result.getAccessDecision());
        assertEquals("dispatch-assistant", result.getSystemEnforcedContext().getAgentId());
    }

    @Test
    void shouldAllowBoundUserWithoutEntitlement() {
        AgentxUserChannelBindingDO binding = new AgentxUserChannelBindingDO();
        binding.setUserId(77L);
        when(bindingMapper.selectByChannelIdentity("telegram", "10003")).thenReturn(binding);
        when(entitlementService.getEffectiveEntitlement(77L, "dispatch-assistant")).thenReturn(null);
        AdminUserDO user = new AdminUserDO();
        user.setNickname("新绑定用户");
        when(adminUserService.getUser(77L)).thenReturn(user);

        AccessEvaluateReqVO reqVO = new AccessEvaluateReqVO();
        reqVO.setChannelUserId("telegram:10003");
        reqVO.setAgentId("dispatch-assistant");
        reqVO.setConversationScope("session_denied");

        AccessEnvelopeRespVO result = service.evaluateAccess(reqVO);
        assertEquals("ALLOW", result.getAccessDecision());
        assertTrue(result.getAccessGranted());
        assertTrue(result.getAuthRequired());
        assertTrue(result.getBound());
        assertEquals("绑定校验通过，允许触发 Agent 能力", result.getMessage());
        assertNotNull(result.getModelVisibleContext());
        assertEquals("新绑定用户", result.getModelVisibleContext().getSubjectProfile().getDisplayName());
        assertNotNull(result.getSystemEnforcedContext());
        assertEquals(77L, result.getSystemEnforcedContext().getUserId());
        assertTrue(service.verifyEnvelope(result.getSystemEnforcedContext()));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

}
