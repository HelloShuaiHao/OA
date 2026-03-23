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
import cn.iocoder.yudao.module.agentx.service.instance.OpenfangApiKeyCrypto;
import cn.iocoder.yudao.module.agentx.service.metrics.AgentxMetricsService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        channel.setStatus(1);
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
