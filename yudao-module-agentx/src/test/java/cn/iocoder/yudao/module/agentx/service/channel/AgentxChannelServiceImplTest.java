package cn.iocoder.yudao.module.agentx.service.channel;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxBindGenerateRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxChannelAccessEvaluateReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxChannelAccessEvaluateRespVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.channel.AgentxChannelAgentDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.channel.AgentxChannelConfigDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.channel.AgentxUserChannelBindingDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.agent.AgentxAgentMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.channel.AgentxChannelAgentMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.channel.AgentxChannelConfigMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.channel.AgentxUserChannelBindingMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.instance.AgentxOpenfangInstanceMapper;
import cn.iocoder.yudao.module.agentx.dal.dataobject.instance.AgentxOpenfangInstanceDO;
import cn.iocoder.yudao.module.agentx.service.instance.OpenfangApiKeyCrypto;
import cn.iocoder.yudao.module.agentx.service.metrics.AgentxMetricsService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentxChannelServiceImplTest {

    private final AgentxChannelConfigMapper channelConfigMapper = Mockito.mock(AgentxChannelConfigMapper.class);
    private final AgentxAgentMapper agentMapper = Mockito.mock(AgentxAgentMapper.class);
    private final AgentxChannelAgentMapper channelAgentMapper = Mockito.mock(AgentxChannelAgentMapper.class);
    private final AgentxUserChannelBindingMapper userChannelBindingMapper = Mockito.mock(AgentxUserChannelBindingMapper.class);
    private final OpenfangApiKeyCrypto openfangApiKeyCrypto = Mockito.mock(OpenfangApiKeyCrypto.class);
    private final RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
    private final AgentxOpenfangInstanceMapper openfangInstanceMapper = Mockito.mock(AgentxOpenfangInstanceMapper.class);
    private final AgentxMetricsService metricsService = Mockito.mock(AgentxMetricsService.class);
    private final AgentxBindService bindService = Mockito.mock(AgentxBindService.class);
    private final AdminUserService adminUserService = Mockito.mock(AdminUserService.class);

    @Test
    void shouldAllowAnonymousAccessForPublicAgent() {
        AgentxChannelServiceImpl service = buildService();
        stubAgentAndChannel("sales-agent", publicConfig(), null);

        AgentxChannelAccessEvaluateReqVO reqVO = new AgentxChannelAccessEvaluateReqVO();
        reqVO.setAgentKey("sales-agent");
        reqVO.setChannelType("telegram");
        reqVO.setChannelUserId("visitor-1");

        AgentxChannelAccessEvaluateRespVO respVO = service.evaluateChannelAccess(reqVO);
        assertTrue(respVO.getAccessGranted());
        assertFalse(respVO.getAuthRequired());
        assertEquals("public", respVO.getAuthMode());
    }

    @Test
    void shouldReturnBindLinkWhenInternalAgentNotBound() {
        AgentxChannelServiceImpl service = buildService();
        stubAgentAndChannel("hr-agent", internalConfig("all", List.of(), List.of()), null);
        AgentxBindGenerateRespVO bindRespVO = new AgentxBindGenerateRespVO();
        bindRespVO.setToken("bind-token");
        bindRespVO.setBindUrl("https://oa.company.com/agentx/bind?token=bind-token");
        when(bindService.generateBindLink(any())).thenReturn(bindRespVO);

        AgentxChannelAccessEvaluateReqVO reqVO = new AgentxChannelAccessEvaluateReqVO();
        reqVO.setAgentKey("hr-agent");
        reqVO.setChannelType("telegram");
        reqVO.setChannelUserId("visitor-2");
        reqVO.setChannelUsername("alice");

        AgentxChannelAccessEvaluateRespVO respVO = service.evaluateChannelAccess(reqVO);
        assertFalse(respVO.getAccessGranted());
        assertTrue(respVO.getAuthRequired());
        assertFalse(respVO.getBound());
        assertEquals("bind_required", respVO.getAuthMode());
        assertEquals("bind-token", respVO.getBindToken());
        assertTrue(respVO.getBindUrl().contains("token=bind-token"));
    }

    @Test
    void shouldDenyWhenBoundUserOutOfAllowedDept() {
        AgentxChannelServiceImpl service = buildService();
        AgentxUserChannelBindingDO binding = new AgentxUserChannelBindingDO();
        binding.setUserId(99L);
        stubAgentAndChannel("finance-agent", internalConfig("dept", List.of(10L), List.of()), binding);
        AdminUserDO user = new AdminUserDO();
        user.setId(99L);
        user.setDeptId(20L);
        when(adminUserService.getUser(99L)).thenReturn(user);

        AgentxChannelAccessEvaluateReqVO reqVO = new AgentxChannelAccessEvaluateReqVO();
        reqVO.setAgentKey("finance-agent");
        reqVO.setChannelType("telegram");
        reqVO.setChannelUserId("employee-99");

        AgentxChannelAccessEvaluateRespVO respVO = service.evaluateChannelAccess(reqVO);
        assertFalse(respVO.getAccessGranted());
        assertTrue(respVO.getAuthRequired());
        assertTrue(respVO.getBound());
        assertTrue(respVO.getMessage().contains("不在该 Agent 允许范围内"));
    }

    @Test
    void shouldSyncTelegramAllowedUsersWhenBindRequired() {
        AgentxChannelServiceImpl service = buildService();
        AgentxChannelConfigDO channel = new AgentxChannelConfigDO();
        channel.setId(11L);
        channel.setChannelType("telegram");
        channel.setStatus(0);
        channel.setConfig(internalConfig("all", List.of(), List.of()));
        channel.setBotTokenEncrypted("b64:dGVsZWdyYW0tdG9rZW4=");
        when(channelConfigMapper.selectById(11L)).thenReturn(channel);

        AgentxChannelAgentDO relation = new AgentxChannelAgentDO();
        relation.setChannelId(11L);
        relation.setAgentId(1L);
        when(channelAgentMapper.selectListByChannelId(11L)).thenReturn(List.of(relation));

        AgentxAgentDO agent = new AgentxAgentDO();
        agent.setId(1L);
        agent.setAgentName("hr-agent");
        agent.setStatus(1);
        when(agentMapper.selectById(1L)).thenReturn(agent);

        AgentxOpenfangInstanceDO instance = new AgentxOpenfangInstanceDO();
        instance.setEndpoint("https://openfang.company.com");
        when(openfangInstanceMapper.selectListByStatus(1)).thenReturn(List.of(instance));

        AgentxUserChannelBindingDO binding = new AgentxUserChannelBindingDO();
        binding.setUserId(99L);
        binding.setChannelType("telegram");
        binding.setChannelUserId("10001");
        binding.setStatus(1);
        when(userChannelBindingMapper.selectListByChannelType("telegram")).thenReturn(List.of(binding));

        String endpoint = "https://openfang.company.com/api/telegram/bindings/agentx-channel-11";
        when(restTemplate.exchange(eq(endpoint), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ReflectionTestUtils.invokeMethod(service, "syncChannelToOpenfang", 11L);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(endpoint), eq(HttpMethod.PATCH), captor.capture(), eq(Object.class));
        Object body = captor.getValue().getBody();
        Assertions.assertNotNull(body);
        @SuppressWarnings("unchecked")
        List<Long> allowedUsers = (List<Long>) ((java.util.Map<String, Object>) body).get("allowed_users");
        Assertions.assertEquals(List.of(10001L), allowedUsers);
    }

    @Test
    void shouldSyncTelegramBlockingPlaceholderWhenNoBoundUsers() {
        AgentxChannelServiceImpl service = buildService();
        AgentxChannelConfigDO channel = new AgentxChannelConfigDO();
        channel.setId(12L);
        channel.setChannelType("telegram");
        channel.setStatus(0);
        channel.setConfig(internalConfig("all", List.of(), List.of()));
        channel.setBotTokenEncrypted("b64:dGVsZWdyYW0tdG9rZW4=");
        when(channelConfigMapper.selectById(12L)).thenReturn(channel);

        AgentxChannelAgentDO relation = new AgentxChannelAgentDO();
        relation.setChannelId(12L);
        relation.setAgentId(1L);
        when(channelAgentMapper.selectListByChannelId(12L)).thenReturn(List.of(relation));

        AgentxAgentDO agent = new AgentxAgentDO();
        agent.setId(1L);
        agent.setAgentName("hr-agent");
        agent.setStatus(1);
        when(agentMapper.selectById(1L)).thenReturn(agent);

        AgentxOpenfangInstanceDO instance = new AgentxOpenfangInstanceDO();
        instance.setEndpoint("https://openfang.company.com");
        when(openfangInstanceMapper.selectListByStatus(1)).thenReturn(List.of(instance));
        when(userChannelBindingMapper.selectListByChannelType("telegram")).thenReturn(List.of());

        String endpoint = "https://openfang.company.com/api/telegram/bindings/agentx-channel-12";
        when(restTemplate.exchange(eq(endpoint), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ReflectionTestUtils.invokeMethod(service, "syncChannelToOpenfang", 12L);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(endpoint), eq(HttpMethod.PATCH), captor.capture(), eq(Object.class));
        Object body = captor.getValue().getBody();
        Assertions.assertNotNull(body);
        @SuppressWarnings("unchecked")
        List<Long> allowedUsers = (List<Long>) ((java.util.Map<String, Object>) body).get("allowed_users");
        Assertions.assertEquals(List.of(-1L), allowedUsers);
    }

    private AgentxChannelServiceImpl buildService() {
        AgentxChannelServiceImpl service = new AgentxChannelServiceImpl();
        ReflectionTestUtils.setField(service, "channelConfigMapper", channelConfigMapper);
        ReflectionTestUtils.setField(service, "agentMapper", agentMapper);
        ReflectionTestUtils.setField(service, "channelAgentMapper", channelAgentMapper);
        ReflectionTestUtils.setField(service, "userChannelBindingMapper", userChannelBindingMapper);
        ReflectionTestUtils.setField(service, "openfangApiKeyCrypto", openfangApiKeyCrypto);
        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(service, "openfangInstanceMapper", openfangInstanceMapper);
        ReflectionTestUtils.setField(service, "metricsService", metricsService);
        ReflectionTestUtils.setField(service, "bindService", bindService);
        ReflectionTestUtils.setField(service, "adminUserService", adminUserService);
        return service;
    }

    private void stubAgentAndChannel(String agentKey, String config, AgentxUserChannelBindingDO binding) {
        AgentxAgentDO agent = new AgentxAgentDO();
        agent.setId(1L);
        agent.setAgentKey(agentKey);
        when(agentMapper.selectByAgentKey(agentKey)).thenReturn(agent);

        AgentxChannelAgentDO relation = new AgentxChannelAgentDO();
        relation.setChannelId(11L);
        relation.setAgentId(1L);
        when(channelAgentMapper.selectListByAgentId(1L)).thenReturn(List.of(relation));

        AgentxChannelConfigDO channel = new AgentxChannelConfigDO();
        channel.setId(11L);
        channel.setChannelType("telegram");
        channel.setStatus(0);
        channel.setConfig(config);
        when(channelConfigMapper.selectListByIds(List.of(11L))).thenReturn(List.of(channel));

        when(userChannelBindingMapper.selectByChannelIdentity(eq("telegram"), Mockito.anyString())).thenReturn(binding);
    }

    private String publicConfig() {
        return JsonUtils.toJsonString(Map.of(
                "accessControlType", "all",
                "deptIds", List.of(),
                "userIds", List.of(),
                "agentAccessPolicies", List.of(Map.of("agentId", 1L, "authMode", "public"))));
    }

    private String internalConfig(String accessControlType, List<Long> deptIds, List<Long> userIds) {
        return JsonUtils.toJsonString(Map.of(
                "accessControlType", accessControlType,
                "deptIds", deptIds,
                "userIds", userIds,
                "agentAccessPolicies", List.of(Map.of("agentId", 1L, "authMode", "bind_required"))));
    }

}
