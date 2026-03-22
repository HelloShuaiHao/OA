package cn.iocoder.yudao.module.agentx.framework.flowable.delegate;

import cn.iocoder.yudao.module.agentx.service.tool.AgentxToolAdapter;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ToolCallDelegateTest {

    @Test
    void shouldInvokeRegisteredToolWithParamConversion() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("toolName", "bpm_query_tasks");
        variables.put("toolParams", "{\"user_id\":\"100\",\"limit\":\"2\"}");
        variables.put("agentCapabilities", "READ_LEAVE");
        variables.put("scenarioCapabilities", "READ_LEAVE");
        variables.put("delegatedCapabilities", "READ_LEAVE");
        variables.put("delegationActive", true);
        variables.put("tenantId", 1L);
        variables.put("userId", 100L);

        ToolCallDelegate delegate = buildDelegate(List.of(new AgentxToolAdapter() {
            @Override
            public String toolName() {
                return "bpm_query_tasks";
            }

            @Override
            public Map<String, Object> invoke(Map<String, Object> request) {
                assertEquals(100L, request.get("user_id"));
                assertEquals(2L, request.get("limit"));
                return Map.of("tasks", Collections.emptyList(), "count", 0);
            }
        }));

        delegate.execute(mockExecution(variables));

        assertEquals("SUCCESS", variables.get("toolCallStatus"));
        assertEquals(1, variables.get("toolCallAttempts"));
        assertTrue(variables.get("toolResult") instanceof Map);
    }

    @Test
    void shouldDenyWhenCapabilityMissing() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("toolName", "bpm_approve");
        variables.put("toolParams", "{\"task_id\":\"T-1\",\"approved\":true}");
        variables.put("agentCapabilities", "READ_LEAVE");
        variables.put("scenarioCapabilities", "READ_LEAVE");
        variables.put("delegatedCapabilities", "READ_LEAVE");
        variables.put("delegationActive", true);
        AtomicInteger invokeCount = new AtomicInteger();

        ToolCallDelegate delegate = buildDelegate(List.of(new AgentxToolAdapter() {
            @Override
            public String toolName() {
                return "bpm_approve";
            }

            @Override
            public Map<String, Object> invoke(Map<String, Object> request) {
                invokeCount.incrementAndGet();
                throw new AssertionError("permission denied before adapter invocation");
            }
        }));

        delegate.execute(mockExecution(variables));

        assertEquals("DENIED", variables.get("toolCallStatus"));
        assertEquals(0, invokeCount.get());
        assertTrue(variables.get("toolCallError") != null);
    }

    @Test
    void shouldRetryAndSucceed() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("toolName", "custom_tool");
        variables.put("toolParams", Map.of("x", "1"));
        variables.put("toolMaxRetries", 1);
        variables.put("delegationActive", true);

        AtomicInteger counter = new AtomicInteger();
        ToolCallDelegate delegate = buildDelegate(List.of(new AgentxToolAdapter() {
            @Override
            public String toolName() {
                return "custom_tool";
            }

            @Override
            public Map<String, Object> invoke(Map<String, Object> request) {
                if (counter.incrementAndGet() == 1) {
                    throw new RuntimeException("first try failed");
                }
                return Map.of("ok", true);
            }
        }));

        delegate.execute(mockExecution(variables));

        assertEquals("SUCCESS", variables.get("toolCallStatus"));
        assertEquals(2, variables.get("toolCallAttempts"));
        assertEquals(Map.of("ok", true), variables.get("toolResult"));
    }

    private ToolCallDelegate buildDelegate(List<AgentxToolAdapter> adapters) {
        ToolCallDelegate delegate = new ToolCallDelegate();
        try {
            Field field = ToolCallDelegate.class.getDeclaredField("toolAdapters");
            field.setAccessible(true);
            field.set(delegate, adapters);
            return delegate;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
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
}
