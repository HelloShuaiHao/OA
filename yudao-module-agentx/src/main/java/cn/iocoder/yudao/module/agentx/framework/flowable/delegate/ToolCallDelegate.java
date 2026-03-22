package cn.iocoder.yudao.module.agentx.framework.flowable.delegate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxAuthorizationService;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import cn.iocoder.yudao.module.agentx.service.identity.ExecutionIdentity;
import cn.iocoder.yudao.module.agentx.service.tool.*;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * AgentX 扩展节点：统一 Tool 调用 Delegate。
 */
@Component("agentxToolCallDelegate")
@Slf4j
public class ToolCallDelegate implements JavaDelegate {

    private static final int DEFAULT_MAX_RETRIES = 2;
    private static final String DEFAULT_OUTPUT_VARIABLE = "toolResult";

    private static final Map<String, AgentxToolDescriptor> BUILTIN_DESCRIPTORS = new HashMap<>();

    static {
        BUILTIN_DESCRIPTORS.put(BpmQueryTasksToolDescriptor.TOOL_NAME, BpmQueryTasksToolDescriptor.build());
        BUILTIN_DESCRIPTORS.put(BpmApproveToolDescriptor.TOOL_NAME, BpmApproveToolDescriptor.build());
    }

    @Autowired(required = false)
    private List<AgentxToolAdapter> toolAdapters = Collections.emptyList();

    @Override
    @SuppressWarnings("unchecked")
    public void execute(DelegateExecution execution) {
        String toolName = firstNonBlank(
                stringValue(execution.getVariable("toolName")),
                stringValue(execution.getVariable("tool")));
        if (StrUtil.isBlank(toolName)) {
            execution.setVariable("toolCallStatus", "SKIPPED");
            execution.setVariable("toolCallMessage", "toolName 为空，已跳过");
            execution.setVariable("toolCallError", null);
            return;
        }

        String outputVariable = firstNonBlank(
                stringValue(execution.getVariable("toolOutputVariable")),
                stringValue(execution.getVariable("toolResultVariable")),
                DEFAULT_OUTPUT_VARIABLE);
        Map<String, AgentxToolAdapter> adapterMap = buildAdapterMap();
        AgentxToolAdapter adapter = adapterMap.get(toolName);
        if (adapter == null) {
            execution.setVariable("toolCallStatus", "FAILED");
            execution.setVariable("toolCallMessage", "未找到已注册 Tool 适配器");
            execution.setVariable("toolCallError", "adapter not found: " + toolName);
            execution.setVariable(outputVariable, null);
            return;
        }

        Map<String, Object> params = resolveParams(execution);
        params = normalizeParams(params);
        int maxRetries = resolveMaxRetries(execution);
        int attempts = 0;
        try {
            validatePermission(execution, toolName);
            RuntimeException lastException = null;
            for (int i = 0; i <= maxRetries; i++) {
                attempts++;
                try {
                    Map<String, Object> result = adapter.invoke(params);
                    execution.setVariable(outputVariable, result);
                    execution.setVariable("toolCallStatus", "SUCCESS");
                    execution.setVariable("toolCallMessage", "工具调用成功");
                    execution.setVariable("toolCallError", null);
                    execution.setVariable("toolCallAttempts", attempts);
                    return;
                } catch (RuntimeException ex) {
                    lastException = ex;
                    log.warn("[ToolCallDelegate] invoke failed, toolName={}, attempt={}/{}",
                            toolName, attempts, maxRetries + 1, ex);
                }
            }
            execution.setVariable(outputVariable, null);
            execution.setVariable("toolCallStatus", "FAILED");
            execution.setVariable("toolCallMessage", "工具调用失败，超过最大重试次数");
            execution.setVariable("toolCallError", lastException != null ? lastException.getMessage() : "unknown error");
            execution.setVariable("toolCallAttempts", attempts);
        } catch (RuntimeException ex) {
            execution.setVariable(outputVariable, null);
            execution.setVariable("toolCallStatus", "DENIED");
            execution.setVariable("toolCallMessage", "工具调用权限校验失败");
            execution.setVariable("toolCallError", ex.getMessage());
            execution.setVariable("toolCallAttempts", attempts);
            log.warn("[ToolCallDelegate] permission check failed, toolName={}", toolName, ex);
        }
    }

    private void validatePermission(DelegateExecution execution, String toolName) {
        AgentxToolDescriptor descriptor = BUILTIN_DESCRIPTORS.get(toolName);
        AgentxToolInvocationRequest invocation = new AgentxToolInvocationRequest()
                .setScenarioCode(stringValue(execution.getVariable("scenarioCode")))
                .setBusinessKey(stringValue(execution.getVariable("businessKey")))
                .setTaskRunId(firstNonBlank(
                        stringValue(execution.getVariable("taskRunId")),
                        stringValue(execution.getVariable("openfangTaskRunId"))))
                .setDataScope(resolveDataScope(execution))
                .setRequiredCapability(resolveRequiredCapability(execution));

        ExecutionIdentity identity = new ExecutionIdentity()
                .setPrincipalType(firstNonBlank(stringValue(execution.getVariable("principalType")), "agent"))
                .setPrincipalId(firstNonBlank(
                        stringValue(execution.getVariable("principalId")),
                        stringValue(execution.getVariable("initiatorId")),
                        stringValue(execution.getVariable("userId"))))
                .setTenantId(parseLong(execution.getVariable("tenantId")))
                .setAgentCode(stringValue(execution.getVariable("agentCode")))
                .setDelegationActive(resolveDelegationActive(execution))
                .setDelegatedCapabilities(resolveCapabilities(execution.getVariable("delegatedCapabilities")));

        Set<AgentxCapability> agentCapabilities = resolveCapabilities(execution.getVariable("agentCapabilities"));
        Set<AgentxCapability> scenarioCapabilities = resolveCapabilities(execution.getVariable("scenarioCapabilities"));

        AgentxToolGuardService guardService = new AgentxToolGuardService(new AgentxAuthorizationService(), null);
        if (descriptor != null) {
            guardService.validateDescriptor(descriptor, invocation, identity, agentCapabilities, scenarioCapabilities);
            return;
        }
        if (invocation.getRequiredCapability() != null) {
            invocation.setToolName(toolName);
            guardService.validate(invocation, identity, agentCapabilities, scenarioCapabilities);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveParams(DelegateExecution execution) {
        Object toolParams = execution.getVariable("toolParams");
        if (toolParams instanceof Map) {
            return new LinkedHashMap<>((Map<String, Object>) toolParams);
        }
        if (toolParams instanceof String && StrUtil.isNotBlank((String) toolParams)) {
            Map<String, Object> parsed = JsonUtils.parseObject((String) toolParams, Map.class);
            if (MapUtil.isNotEmpty(parsed)) {
                return parsed;
            }
        }
        Object toolPayload = execution.getVariable("toolPayload");
        if (toolPayload instanceof Map) {
            return new LinkedHashMap<>((Map<String, Object>) toolPayload);
        }
        if (toolPayload instanceof String && StrUtil.isNotBlank((String) toolPayload)) {
            Map<String, Object> parsed = JsonUtils.parseObject((String) toolPayload, Map.class);
            if (MapUtil.isNotEmpty(parsed)) {
                return parsed;
            }
        }
        return new LinkedHashMap<>();
    }

    private Map<String, Object> normalizeParams(Map<String, Object> params) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            normalized.put(entry.getKey(), normalizeValue(entry.getValue()));
        }
        return normalized;
    }

    @SuppressWarnings("unchecked")
    private Object normalizeValue(Object value) {
        if (value instanceof Map) {
            Map<String, Object> result = new LinkedHashMap<>();
            ((Map<String, Object>) value).forEach((k, v) -> result.put(k, normalizeValue(v)));
            return result;
        }
        if (value instanceof List) {
            List<Object> result = new ArrayList<>();
            for (Object item : (List<?>) value) {
                result.add(normalizeValue(item));
            }
            return result;
        }
        if (!(value instanceof String)) {
            return value;
        }
        String text = StrUtil.trim((String) value);
        if (StrUtil.isBlank(text)) {
            return value;
        }
        if ("true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text)) {
            return Boolean.valueOf(text);
        }
        if (StrUtil.isNumeric(text)) {
            try {
                return Long.valueOf(text);
            } catch (Exception ignore) {
                // ignore
            }
        }
        if (StrUtil.startWithAny(text, "{", "[")) {
            try {
                Object parsed = JsonUtils.parseObject(text, Object.class);
                if (parsed != null) {
                    return parsed;
                }
            } catch (Exception ignore) {
                // ignore invalid json text
            }
        }
        return value;
    }

    private Map<String, AgentxToolAdapter> buildAdapterMap() {
        if (CollUtil.isEmpty(toolAdapters)) {
            return Collections.emptyMap();
        }
        Map<String, AgentxToolAdapter> result = new HashMap<>();
        for (AgentxToolAdapter adapter : toolAdapters) {
            result.put(adapter.toolName(), adapter);
        }
        return result;
    }

    private AgentxDataScope resolveDataScope(DelegateExecution execution) {
        Long tenantId = parseLong(execution.getVariable("tenantId"));
        Long deptId = parseLong(execution.getVariable("deptId"));
        Object raw = execution.getVariable("dataScope");
        AgentxDataScope scope = new AgentxDataScope();
        if (raw instanceof AgentxDataScope) {
            return (AgentxDataScope) raw;
        }
        if (raw instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) raw;
            tenantId = firstNonNull(parseLong(data.get("tenantId")), tenantId);
            Object orgUnitIds = data.get("orgUnitIds");
            if (orgUnitIds instanceof List) {
                scope.setOrgUnitIds(Convert.toList(Long.class, orgUnitIds));
            }
        }
        if (scope.getOrgUnitIds() == null && deptId != null) {
            scope.setOrgUnitIds(Collections.singletonList(deptId));
        }
        return scope.setTenantId(tenantId);
    }

    private AgentxCapability resolveRequiredCapability(DelegateExecution execution) {
        String capability = firstNonBlank(
                stringValue(execution.getVariable("requiredCapability")),
                stringValue(execution.getVariable("toolRequiredCapability")));
        if (StrUtil.isBlank(capability)) {
            return null;
        }
        try {
            return AgentxCapability.valueOf(capability.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean resolveDelegationActive(DelegateExecution execution) {
        Object raw = execution.getVariable("delegationActive");
        if (raw == null) {
            return true;
        }
        return Convert.toBool(raw, true);
    }

    private Set<AgentxCapability> resolveCapabilities(Object raw) {
        if (raw == null) {
            return Collections.emptySet();
        }
        Set<AgentxCapability> result = EnumSet.noneOf(AgentxCapability.class);
        if (raw instanceof Collection) {
            for (Object item : (Collection<?>) raw) {
                addCapability(result, item);
            }
            return result;
        }
        if (raw instanceof String) {
            for (String part : StrUtil.split((String) raw, ',')) {
                addCapability(result, part);
            }
            return result;
        }
        addCapability(result, raw);
        return result;
    }

    private void addCapability(Set<AgentxCapability> target, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof AgentxCapability) {
            target.add((AgentxCapability) value);
            return;
        }
        String text = StrUtil.trim(String.valueOf(value));
        if (StrUtil.isBlank(text)) {
            return;
        }
        try {
            target.add(AgentxCapability.valueOf(text));
        } catch (Exception ignore) {
            // ignore unknown capability
        }
    }

    private int resolveMaxRetries(DelegateExecution execution) {
        Integer value = Convert.toInt(firstNonBlank(
                stringValue(execution.getVariable("toolMaxRetries")),
                stringValue(execution.getVariable("retryTimes"))), DEFAULT_MAX_RETRIES);
        if (value == null || value < 0) {
            return DEFAULT_MAX_RETRIES;
        }
        return value;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StrUtil.isNotBlank(value) && !"null".equalsIgnoreCase(value)) {
                return value;
            }
        }
        return null;
    }

    private <T> T firstNonNull(T first, T second) {
        return first != null ? first : second;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

}
