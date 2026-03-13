package cn.iocoder.yudao.module.agentx.service.tool;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.agentx.service.audit.AgentxAuditService;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxAuthorizationService;
import cn.iocoder.yudao.module.agentx.service.identity.ExecutionIdentity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxToolGuardServiceTest {

    @Test
    void shouldAllowToolInvocationWithinAuthorizedDataScope() {
        List<String> auditEvents = new CopyOnWriteArrayList<>();
        AgentxToolGuardService service = new AgentxToolGuardService(new AgentxAuthorizationService(), proxyAuditService(auditEvents));
        ExecutionIdentity identity = new ExecutionIdentity()
                .setPrincipalId("u-1")
                .setTenantId(1L)
                .setAgentCode("leave-agent")
                .setDelegationActive(true)
                .setDelegatedCapabilities(EnumSet.of(AgentxCapability.READ_LEAVE, AgentxCapability.SUBMIT_LEAVE));

        AgentxToolGuardResult result = service.validate(new AgentxToolInvocationRequest()
                        .setScenarioCode("oa.leave.approval")
                        .setBusinessKey("leave:1")
                        .setTaskRunId("task-run-1")
                        .setToolName("leave.submit")
                        .setRequiredCapability(AgentxCapability.SUBMIT_LEAVE)
                        .setRiskLevel(20)
                        .setApprovalRequired(false)
                        .setDataScope(new AgentxDataScope().setTenantId(1L).setBusinessKeys(List.of("leave:1"))),
                identity,
                EnumSet.of(AgentxCapability.SUBMIT_LEAVE),
                EnumSet.of(AgentxCapability.SUBMIT_LEAVE));

        assertTrue(result.isAuthorized());
        assertFalse(result.isApprovalRequired());
        assertEquals("TOOL_ALLOWED", auditEvents.get(0));
    }

    @Test
    void shouldRequireApprovalForHighRiskToolInvocation() {
        AgentxToolGuardService service = new AgentxToolGuardService(new AgentxAuthorizationService(), null);
        ExecutionIdentity identity = new ExecutionIdentity()
                .setPrincipalId("u-1")
                .setTenantId(1L)
                .setAgentCode("leave-agent")
                .setDelegationActive(true)
                .setDelegatedCapabilities(EnumSet.of(AgentxCapability.SUBMIT_LEAVE));

        AgentxToolGuardResult result = service.validate(new AgentxToolInvocationRequest()
                        .setScenarioCode("oa.leave.approval")
                        .setBusinessKey("leave:1")
                        .setTaskRunId("task-run-1")
                        .setToolName("leave.submit")
                        .setRequiredCapability(AgentxCapability.SUBMIT_LEAVE)
                        .setRiskLevel(30)
                        .setApprovalRequired(true)
                        .setDataScope(new AgentxDataScope().setTenantId(1L).setBusinessKeys(List.of("leave:1"))),
                identity,
                EnumSet.of(AgentxCapability.SUBMIT_LEAVE),
                EnumSet.of(AgentxCapability.SUBMIT_LEAVE));

        assertTrue(result.isApprovalRequired());
    }

    @Test
    void shouldUseDescriptorPolicyForApprovalAndAuditTags() {
        AgentxToolGuardService service = new AgentxToolGuardService(new AgentxAuthorizationService(), null);
        ExecutionIdentity identity = new ExecutionIdentity()
                .setPrincipalId("u-1")
                .setTenantId(1L)
                .setAgentCode("leave-agent")
                .setDelegationActive(true)
                .setDelegatedCapabilities(EnumSet.of(AgentxCapability.SUBMIT_LEAVE));

        AgentxToolDescriptor descriptor = new AgentxToolDescriptor()
                .setToolName("leave.submit")
                .setInputSchema(new AgentxToolSchema().setSchemaType("object").setRequiredFields(List.of("leaveId")))
                .setOutputSchema(new AgentxToolSchema().setSchemaType("object").setRequiredFields(List.of("formId")))
                .setPolicy(new AgentxToolPolicy()
                        .setRequiredCapability(AgentxCapability.SUBMIT_LEAVE)
                        .setRiskLevel(30)
                        .setApprovalRequired(true)
                        .setAuditTags(List.of("leave", "write")));

        AgentxToolGuardResult result = service.validateDescriptor(descriptor,
                new AgentxToolInvocationRequest()
                        .setScenarioCode("oa.leave.approval")
                        .setBusinessKey("leave:1")
                        .setTaskRunId("task-run-1")
                        .setToolName("leave.submit")
                        .setDataScope(new AgentxDataScope().setTenantId(1L).setBusinessKeys(List.of("leave:1"))),
                identity,
                EnumSet.of(AgentxCapability.SUBMIT_LEAVE),
                EnumSet.of(AgentxCapability.SUBMIT_LEAVE));

        assertTrue(result.isApprovalRequired());
    }

    @Test
    void shouldRejectCrossTenantDataScope() {
        List<String> auditEvents = new CopyOnWriteArrayList<>();
        AgentxToolGuardService service = new AgentxToolGuardService(new AgentxAuthorizationService(), proxyAuditService(auditEvents));
        ExecutionIdentity identity = new ExecutionIdentity()
                .setPrincipalId("u-1")
                .setTenantId(1L)
                .setAgentCode("leave-agent")
                .setDelegationActive(true)
                .setDelegatedCapabilities(EnumSet.of(AgentxCapability.READ_LEAVE));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.validate(new AgentxToolInvocationRequest()
                        .setScenarioCode("oa.leave.approval")
                        .setBusinessKey("leave:2")
                        .setTaskRunId("task-run-2")
                        .setToolName("leave.query")
                        .setRequiredCapability(AgentxCapability.READ_LEAVE)
                        .setRiskLevel(10)
                        .setDataScope(new AgentxDataScope().setTenantId(2L).setBusinessKeys(List.of("leave:2"))),
                identity,
                EnumSet.of(AgentxCapability.READ_LEAVE),
                EnumSet.of(AgentxCapability.READ_LEAVE)));

        assertEquals(Integer.valueOf(1_024_001_006), ex.getCode());
        assertEquals("TOOL_DENIED", auditEvents.get(0));
    }

    @SuppressWarnings("unchecked")
    private AgentxAuditService proxyAuditService(List<String> sink) {
        return (AgentxAuditService) Proxy.newProxyInstance(
                AgentxAuditService.class.getClassLoader(),
                new Class<?>[] { AgentxAuditService.class },
                (proxy, method, args) -> {
                    if ("recordToolInvocation".equals(method.getName())) {
                        sink.add(String.valueOf(args[7]));
                    }
                    return null;
                });
    }

}
