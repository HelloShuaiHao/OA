package cn.iocoder.yudao.module.agentx.service.process;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.agentx.service.access.AgentxAccessService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.ENVELOPE_SIGNATURE_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class AgentxProcessExecutionServiceImplTest {

    @Test
    void shouldVerifyAndPropagateSystemContextBeforeStart() {
        RuntimeService runtimeService = Mockito.mock(RuntimeService.class);
        AgentxProcessSelectionService selectionService = Mockito.mock(AgentxProcessSelectionService.class);
        AgentxAccessService accessService = Mockito.mock(AgentxAccessService.class);
        ProcessInstance processInstance = Mockito.mock(ProcessInstance.class);

        AgentxProcessSelectionService.SelectionResult selection = new AgentxProcessSelectionService.SelectionResult()
                .setProcessDefinitionKey("leave_process")
                .setReason("rule");
        when(selectionService.selectProcess(eq(10L), eq("rule"), anyMap())).thenReturn(selection);
        when(accessService.verifyEnvelope(Mockito.any())).thenReturn(true);
        when(runtimeService.startProcessInstanceByKey(eq("leave_process"), eq("BK-1"), anyMap())).thenReturn(processInstance);
        when(processInstance.getProcessInstanceId()).thenReturn("PI-1");

        AgentxProcessExecutionServiceImpl service = new AgentxProcessExecutionServiceImpl();
        setField(service, "runtimeService", runtimeService);
        setField(service, "processSelectionService", selectionService);
        setField(service, "accessService", accessService);

        Map<String, Object> context = Map.of(
                "systemEnforcedContext", Map.of(
                        "userId", 100L,
                        "channelUserId", "telegram:100",
                        "agentId", "dispatch-assistant",
                        "allowedActions", List.of("route.read"),
                        "resourceFilters", Map.of("region_codes", List.of("east")),
                        "obligations", List.of(Map.of("action", "route.plan.cross_region", "requires", "approval")),
                        "policyVersion", "v2026.04.01",
                        "decisionId", "dec_001",
                        "signature", "sig")
        );
        String processId = service.startSelectedProcess(10L, "rule", "BK-1", context);

        assertEquals("PI-1", processId);
        ArgumentCaptor<Map<String, Object>> varsCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(runtimeService).startProcessInstanceByKey(eq("leave_process"), eq("BK-1"), varsCaptor.capture());
        Map<String, Object> vars = varsCaptor.getValue();
        assertEquals(List.of("route.read"), vars.get("allowedActions"));
        assertEquals(List.of("route.read"), vars.get("entitlementAllowedActions"));
        assertEquals("dec_001", vars.get("decisionId"));
        assertEquals("v2026.04.01", vars.get("policyVersion"));
    }

    @Test
    void shouldRejectWhenSystemContextSignatureInvalid() {
        RuntimeService runtimeService = Mockito.mock(RuntimeService.class);
        AgentxProcessSelectionService selectionService = Mockito.mock(AgentxProcessSelectionService.class);
        AgentxAccessService accessService = Mockito.mock(AgentxAccessService.class);

        AgentxProcessSelectionService.SelectionResult selection = new AgentxProcessSelectionService.SelectionResult()
                .setProcessDefinitionKey("leave_process")
                .setReason("rule");
        when(selectionService.selectProcess(eq(10L), eq("rule"), anyMap())).thenReturn(selection);
        when(accessService.verifyEnvelope(Mockito.any())).thenReturn(false);

        AgentxProcessExecutionServiceImpl service = new AgentxProcessExecutionServiceImpl();
        setField(service, "runtimeService", runtimeService);
        setField(service, "processSelectionService", selectionService);
        setField(service, "accessService", accessService);

        Map<String, Object> context = Map.of(
                "systemEnforcedContext", Map.of(
                        "userId", 100L,
                        "channelUserId", "telegram:100",
                        "agentId", "dispatch-assistant",
                        "allowedActions", List.of("route.read"),
                        "policyVersion", "v2026.04.01",
                        "decisionId", "dec_001",
                        "signature", "tampered")
        );
        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.startSelectedProcess(10L, "rule", "BK-1", context));
        assertEquals(ENVELOPE_SIGNATURE_INVALID.getCode(), ex.getCode());
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
