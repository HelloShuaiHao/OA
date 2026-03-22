package cn.iocoder.yudao.module.agentx.framework.flowable.delegate;

import cn.iocoder.yudao.module.agentx.service.decision.AgentxAiDecisionService;
import cn.iocoder.yudao.module.agentx.service.process.AgentxProcessSelectionService;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class AiDecisionDelegateTest {

    private final AgentxProcessSelectionService processSelectionService = Mockito.mock(AgentxProcessSelectionService.class);
    private final AgentxAiDecisionService aiDecisionService = Mockito.mock(AgentxAiDecisionService.class);
    private final AiDecisionDelegate delegate = buildDelegate();

    @Test
    void shouldExecuteAiDecisionModeWhenPromptProvided() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("prompt", "请判断是否审批");
        variables.put("aiInputVariables", "leaveDays");
        variables.put("leaveDays", 1);

        when(aiDecisionService.decide(eq("请判断是否审批"), anyMap())).thenReturn("无需审批");

        delegate.execute(mockExecution(variables));

        assertEquals("无需审批", variables.get("aiDecision"));
        assertEquals("SUCCESS", variables.get("aiDecisionStatus"));
    }

    @Test
    void shouldFallbackToProcessSelectionModeWhenPromptMissing() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("agentId", 9L);
        AgentxProcessSelectionService.SelectionResult selectionResult =
                new AgentxProcessSelectionService.SelectionResult()
                        .setProcessDefinitionId("pd-1")
                        .setProcessDefinitionKey("leave_approval")
                        .setReason("auto");
        when(processSelectionService.selectProcess(eq(9L), anyString(), anyMap())).thenReturn(selectionResult);

        delegate.execute(mockExecution(variables));

        assertEquals("pd-1", variables.get("selectedProcessDefinitionId"));
        assertEquals("leave_approval", variables.get("selectedProcessDefinitionKey"));
    }

    @Test
    void shouldSetTimeoutStatusWhenAiDecisionTakesTooLong() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("prompt", "请判断是否审批");
        variables.put("aiTimeoutSeconds", 1);
        when(aiDecisionService.decide(anyString(), anyMap())).thenAnswer(invocation -> {
            Thread.sleep(1500L);
            return "需要审批";
        });

        delegate.execute(mockExecution(variables));

        assertEquals("TIMEOUT", variables.get("aiDecision"));
        assertEquals("TIMEOUT", variables.get("aiDecisionStatus"));
    }

    private DelegateExecution mockExecution(Map<String, Object> variables) {
        DelegateExecution execution = Mockito.mock(DelegateExecution.class);
        when(execution.getVariable(anyString())).thenAnswer(invocation -> variables.get(invocation.getArgument(0)));
        Mockito.doAnswer(invocation -> {
            variables.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        return execution;
    }

    private AiDecisionDelegate buildDelegate() {
        AiDecisionDelegate impl = new AiDecisionDelegate();
        try {
            java.lang.reflect.Field processField = AiDecisionDelegate.class.getDeclaredField("processSelectionService");
            processField.setAccessible(true);
            processField.set(impl, processSelectionService);
            java.lang.reflect.Field aiField = AiDecisionDelegate.class.getDeclaredField("aiDecisionService");
            aiField.setAccessible(true);
            aiField.set(impl, aiDecisionService);
            return impl;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
