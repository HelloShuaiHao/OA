package cn.iocoder.yudao.module.agentx.service.tool;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.agentx.service.audit.AgentxAuditService;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import cn.iocoder.yudao.module.agentx.service.identity.ExecutionIdentity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.TOOL_ADAPTER_NOT_EXISTS;
import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.TOOL_REQUEST_INVALID;

/**
 * Tool 统一调用入口：参数校验、权限校验、适配器调用、审计落库。
 */
public class AgentxToolInvocationService {

    private final AgentxToolGuardService toolGuardService;
    private final AgentxAuditService auditService;
    private final Map<String, AgentxToolAdapter> adapters;

    public AgentxToolInvocationService(AgentxToolGuardService toolGuardService,
                                       AgentxAuditService auditService,
                                       List<AgentxToolAdapter> adapters) {
        this.toolGuardService = toolGuardService;
        this.auditService = auditService;
        this.adapters = adapters == null ? Collections.emptyMap() : adapters.stream()
                .collect(Collectors.toMap(AgentxToolAdapter::toolName, item -> item, (left, right) -> right));
    }

    public Map<String, Object> invoke(AgentxToolDescriptor descriptor,
                                      AgentxToolInvocationRequest invocation,
                                      ExecutionIdentity identity,
                                      Set<AgentxCapability> agentCapabilities,
                                      Set<AgentxCapability> scenarioCapabilities,
                                      Map<String, Object> params) {
        if (descriptor == null || invocation == null) {
            throw ServiceExceptionUtil.exception(TOOL_REQUEST_INVALID);
        }
        Map<String, Object> request = params == null ? Collections.emptyMap() : params;
        validateRequiredFields(descriptor, request);
        toolGuardService.validateDescriptor(descriptor, invocation, identity, agentCapabilities, scenarioCapabilities);

        AgentxToolAdapter adapter = adapters.get(descriptor.getToolName());
        if (adapter == null) {
            throw ServiceExceptionUtil.exception(TOOL_ADAPTER_NOT_EXISTS, descriptor.getToolName());
        }

        long start = System.currentTimeMillis();
        try {
            Map<String, Object> result = adapter.invoke(request);
            recordCompletion(identity, invocation, descriptor.getToolName(), invocation.getDataScope(),
                    invocation.getRiskLevel(), request, result, System.currentTimeMillis() - start, null);
            return result;
        } catch (RuntimeException ex) {
            Integer errorCode = ex instanceof ServiceException ? ((ServiceException) ex).getCode() : null;
            recordCompletion(identity, invocation, descriptor.getToolName(), invocation.getDataScope(),
                    invocation.getRiskLevel(), request, buildFailureResult(ex),
                    System.currentTimeMillis() - start, errorCode);
            throw ex;
        }
    }

    private void validateRequiredFields(AgentxToolDescriptor descriptor, Map<String, Object> params) {
        if (descriptor.getInputSchema() == null || descriptor.getInputSchema().getRequiredFields() == null) {
            return;
        }
        for (String requiredField : descriptor.getInputSchema().getRequiredFields()) {
            if (!params.containsKey(requiredField) || params.get(requiredField) == null
                    || (params.get(requiredField) instanceof String && StrUtil.isBlank((String) params.get(requiredField)))) {
                throw ServiceExceptionUtil.exception(TOOL_REQUEST_INVALID);
            }
        }
    }

    private void recordCompletion(ExecutionIdentity identity, AgentxToolInvocationRequest invocation, String toolName,
                                  AgentxDataScope dataScope, Integer riskLevel,
                                  Map<String, Object> request, Map<String, Object> result,
                                  Long durationMs, Integer errorCode) {
        if (auditService == null) {
            return;
        }
        auditService.recordToolCompletion(identity,
                invocation.getScenarioCode(),
                invocation.getBusinessKey(),
                invocation.getTaskRunId(),
                toolName,
                dataScope,
                riskLevel,
                summarize(request),
                summarize(result),
                durationMs,
                errorCode,
                "tool=" + toolName);
    }

    private Map<String, Object> buildFailureResult(RuntimeException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", ex.getMessage());
        return result;
    }

    private String summarize(Map<String, Object> payload) {
        if (MapUtil.isEmpty(payload)) {
            return "{}";
        }
        Map<String, Object> sanitized = new HashMap<>();
        payload.forEach((key, value) -> {
            if (StrUtil.containsAnyIgnoreCase(key, "password", "passwd", "pwd", "key", "token", "secret")) {
                sanitized.put(key, "***");
            } else {
                sanitized.put(key, value);
            }
        });
        return sanitized.toString();
    }

}
