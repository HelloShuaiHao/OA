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
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        variables.put("allowedActions", List.of("bpm.task.read"));
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
    void shouldDenyWhenActionMissingEvenCapabilityExists() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("toolName", "bpm_query_tasks");
        variables.put("toolParams", "{\"user_id\":\"100\"}");
        variables.put("agentCapabilities", "READ_LEAVE");
        variables.put("scenarioCapabilities", "READ_LEAVE");
        variables.put("delegatedCapabilities", "READ_LEAVE");
        variables.put("delegationActive", true);
        variables.put("allowedActions", List.of("bpm.task.approve"));
        variables.put("tenantId", 1L);
        variables.put("userId", 100L);
        AtomicInteger invokeCount = new AtomicInteger();

        ToolCallDelegate delegate = buildDelegate(List.of(new AgentxToolAdapter() {
            @Override
            public String toolName() {
                return "bpm_query_tasks";
            }

            @Override
            public Map<String, Object> invoke(Map<String, Object> request) {
                invokeCount.incrementAndGet();
                return Map.of("tasks", Collections.emptyList());
            }
        }));

        delegate.execute(mockExecution(variables));

        assertEquals("DENIED", variables.get("toolCallStatus"));
        assertEquals(0, invokeCount.get());
        assertTrue(String.valueOf(variables.get("toolCallError")).contains("缺少 action"));
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

    @Test
    void shouldApplyEnforcedResourceFilters() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("toolName", "query_routes");
        variables.put("toolParams", Map.of("_filter_region_codes", List.of("west")));
        variables.put("requiredCapability", "READ_LEAVE");
        variables.put("requiredActions", List.of("route.read"));
        variables.put("allowedActions", List.of("route.read"));
        variables.put("agentCapabilities", "READ_LEAVE");
        variables.put("scenarioCapabilities", "READ_LEAVE");
        variables.put("delegatedCapabilities", "READ_LEAVE");
        variables.put("delegationActive", true);
        variables.put("systemEnforcedContext", Map.of(
                "resourceFilters", Map.of("region_codes", List.of("east"))));

        ToolCallDelegate delegate = buildDelegate(List.of(new AgentxToolAdapter() {
            @Override
            public String toolName() {
                return "query_routes";
            }

            @Override
            public Map<String, Object> invoke(Map<String, Object> request) {
                assertEquals(List.of("east"), request.get("_filter_region_codes"));
                assertTrue(request.containsKey("_system_resource_filters"));
                return Map.of("ok", true);
            }
        }));

        delegate.execute(mockExecution(variables));

        assertEquals("SUCCESS", variables.get("toolCallStatus"));
    }

    @Test
    void shouldReturnPendingApprovalWhenObligationMatched() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("toolName", "create_cross_region_plan");
        variables.put("toolParams", Map.of("route_id", "R-1"));
        variables.put("requiredCapability", "READ_LEAVE");
        variables.put("requiredActions", List.of("route.plan.cross_region"));
        variables.put("allowedActions", List.of("route.plan.cross_region"));
        variables.put("agentCapabilities", "READ_LEAVE");
        variables.put("scenarioCapabilities", "READ_LEAVE");
        variables.put("delegatedCapabilities", "READ_LEAVE");
        variables.put("delegationActive", true);
        variables.put("obligations", List.of(Map.of(
                "action", "route.plan.cross_region",
                "requires", "approval",
                "approverRole", "regional_manager",
                "reason", "跨区域调度需审批"
        )));
        AtomicInteger invokeCount = new AtomicInteger();

        ToolCallDelegate delegate = buildDelegate(List.of(new AgentxToolAdapter() {
            @Override
            public String toolName() {
                return "create_cross_region_plan";
            }

            @Override
            public Map<String, Object> invoke(Map<String, Object> request) {
                invokeCount.incrementAndGet();
                return Map.of("ok", true);
            }
        }));

        delegate.execute(mockExecution(variables));

        assertEquals("PENDING_APPROVAL", variables.get("toolCallStatus"));
        assertEquals("regional_manager", variables.get("toolApprovalRole"));
        assertEquals("route.plan.cross_region", variables.get("toolApprovalAction"));
        assertTrue(variables.get("toolApprovalId") != null);
        assertFalse(Boolean.FALSE.equals(variables.get("toolApprovalRequired")));
        assertEquals(0, invokeCount.get());
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
