package cn.iocoder.yudao.module.agentx.service.tool;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.agentx.service.audit.AgentxAuditService;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxAuthorizationService;
import cn.iocoder.yudao.module.agentx.service.identity.ExecutionIdentity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxToolInvocationServiceTest {

    @Test
    void shouldInvokeAdapterAfterGuardAndRecordAudit() {
        List<String> auditEvents = new ArrayList<>();
        AgentxAuditService auditService = proxyAuditService(auditEvents);

        AgentxToolGuardService guardService = new AgentxToolGuardService(new AgentxAuthorizationService(), auditService);
        AgentxToolAdapter adapter = new AgentxToolAdapter() {
            @Override
            public String toolName() {
                return BpmApproveToolDescriptor.TOOL_NAME;
            }

            @Override
            public Map<String, Object> invoke(Map<String, Object> request) {
                return Map.of("approved", true, "task_id", request.get("task_id"));
            }
        };
        AgentxToolInvocationService service = new AgentxToolInvocationService(guardService, auditService, List.of(adapter));

        Map<String, Object> result = service.invoke(BpmApproveToolDescriptor.build(),
                new AgentxToolInvocationRequest()
                        .setScenarioCode("oa.leave.approval")
                        .setBusinessKey("leave:1")
                        .setTaskRunId("tr-1")
                        .setDataScope(new AgentxDataScope().setTenantId(1L).setBusinessKeys(List.of("leave:1"))),
                new ExecutionIdentity()
                        .setPrincipalId("u-1")
                        .setTenantId(1L)
                        .setAgentCode("leave-agent")
                        .setDelegationActive(true)
                        .setDelegatedCapabilities(EnumSet.of(AgentxCapability.UPDATE_LEAVE)),
                EnumSet.of(AgentxCapability.UPDATE_LEAVE),
                EnumSet.of(AgentxCapability.UPDATE_LEAVE),
                new HashMap<String, Object>() {{
                    put("task_id", "task-1");
                    put("approved", true);
                    put("token", "sensitive");
                }});

        assertTrue((Boolean) result.get("approved"));
        assertTrue(auditEvents.stream().anyMatch(item -> item.startsWith("INVOCATION:TOOL_ALLOWED")));
        assertTrue(auditEvents.stream().anyMatch(item -> item.startsWith("COMPLETION:")));
        assertTrue(auditEvents.stream().anyMatch(item -> item.contains("token=***")));
    }

    @Test
    void shouldDenyWhenMissingCapability() {
        List<String> auditEvents = new ArrayList<>();
        AgentxAuditService auditService = proxyAuditService(auditEvents);

        AgentxToolGuardService guardService = new AgentxToolGuardService(new AgentxAuthorizationService(), auditService);
        AgentxToolAdapter adapter = new AgentxToolAdapter() {
            @Override
            public String toolName() {
                return BpmQueryTasksToolDescriptor.TOOL_NAME;
            }

            @Override
            public Map<String, Object> invoke(Map<String, Object> request) {
                return Map.of("tasks", List.of());
            }
        };
        AgentxToolInvocationService service = new AgentxToolInvocationService(guardService, auditService, List.of(adapter));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.invoke(BpmQueryTasksToolDescriptor.build(),
                new AgentxToolInvocationRequest()
                        .setScenarioCode("oa.leave.approval")
                        .setBusinessKey("leave:2")
                        .setTaskRunId("tr-2")
                        .setDataScope(new AgentxDataScope().setTenantId(1L).setBusinessKeys(List.of("leave:2"))),
                new ExecutionIdentity()
                        .setPrincipalId("u-1")
                        .setTenantId(1L)
                        .setAgentCode("leave-agent")
                        .setDelegationActive(true)
                        .setDelegatedCapabilities(EnumSet.of(AgentxCapability.READ_LEAVE)),
                EnumSet.noneOf(AgentxCapability.class),
                EnumSet.of(AgentxCapability.READ_LEAVE),
                Map.of("user_id", 100L)));

        assertEquals(Integer.valueOf(1_024_001_004), ex.getCode());
        assertTrue(auditEvents.stream().anyMatch(item -> item.startsWith("INVOCATION:TOOL_DENIED")));
    }

    @SuppressWarnings("unchecked")
    private AgentxAuditService proxyAuditService(List<String> sink) {
        return (AgentxAuditService) Proxy.newProxyInstance(
                AgentxAuditService.class.getClassLoader(),
                new Class<?>[] { AgentxAuditService.class },
                (proxy, method, args) -> {
                    if ("recordToolInvocation".equals(method.getName())) {
                        sink.add("INVOCATION:" + args[7]);
                    }
                    if ("recordToolCompletion".equals(method.getName())) {
                        sink.add("COMPLETION:" + args[7] + "|" + args[8]);
                    }
                    return null;
                });
    }

}
