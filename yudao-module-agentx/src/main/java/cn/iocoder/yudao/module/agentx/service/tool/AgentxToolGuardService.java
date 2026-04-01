package cn.iocoder.yudao.module.agentx.service.tool;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.agentx.service.audit.AgentxAuditService;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxAuthorizationService;
import cn.iocoder.yudao.module.agentx.service.identity.ExecutionIdentity;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.DATA_SCOPE_DENIED;
import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.TOOL_ACTION_DENIED;
import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.TOOL_REQUEST_INVALID;

/**
 * Tool 调用前校验链。
 */
public class AgentxToolGuardService {

    private final AgentxAuthorizationService authorizationService;
    private final AgentxAuditService auditService;

    public AgentxToolGuardService(AgentxAuthorizationService authorizationService, AgentxAuditService auditService) {
        this.authorizationService = authorizationService;
        this.auditService = auditService;
    }

    public AgentxToolGuardResult validate(AgentxToolInvocationRequest request, ExecutionIdentity identity,
                                          Set<cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability> agentCapabilities,
                                          Set<cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability> scenarioCapabilities) {
        if (request.getToolName() == null || request.getRequiredCapability() == null || request.getDataScope() == null) {
            throw ServiceExceptionUtil.exception(TOOL_REQUEST_INVALID);
        }
        try {
            authorizationService.authorize(agentCapabilities, identity, scenarioCapabilities,
                    EnumSet.of(request.getRequiredCapability()));
            if (identity.getTenantId() != null && request.getDataScope().getTenantId() != null
                    && !identity.getTenantId().equals(request.getDataScope().getTenantId())) {
                throw ServiceExceptionUtil.exception(DATA_SCOPE_DENIED, identity.getTenantId(), request.getDataScope().getTenantId());
            }
            if (CollUtil.isNotEmpty(request.getRequiredActions())) {
                List<String> missingActions = request.getRequiredActions().stream()
                        .filter(requiredAction -> request.getAllowedActions() == null
                                || !request.getAllowedActions().contains(requiredAction))
                        .collect(Collectors.toList());
                if (CollUtil.isNotEmpty(missingActions)) {
                    throw ServiceExceptionUtil.exception(TOOL_ACTION_DENIED, String.join(",", missingActions));
                }
            }
            boolean approvalRequired = Boolean.TRUE.equals(request.getApprovalRequired())
                    || (request.getRiskLevel() != null && request.getRiskLevel() >= 30);
            if (auditService != null) {
                auditService.recordToolInvocation(identity, request.getScenarioCode(), request.getBusinessKey(),
                        request.getTaskRunId(), request.getToolName(), request.getDataScope(),
                        request.getRiskLevel(), "TOOL_ALLOWED");
            }
            return new AgentxToolGuardResult(true, approvalRequired);
        } catch (RuntimeException ex) {
            if (auditService != null) {
                auditService.recordToolInvocation(identity, request.getScenarioCode(), request.getBusinessKey(),
                        request.getTaskRunId(), request.getToolName(), request.getDataScope(),
                        request.getRiskLevel(), "TOOL_DENIED");
            }
            throw ex;
        }
    }

    public AgentxToolGuardResult validateDescriptor(AgentxToolDescriptor descriptor, AgentxToolInvocationRequest request,
                                                    ExecutionIdentity identity,
                                                    Set<cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability> agentCapabilities,
                                                    Set<cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability> scenarioCapabilities) {
        request.setToolName(descriptor.getToolName());
        if (descriptor.getPolicy() != null) {
            request.setRequiredCapability(descriptor.getPolicy().getRequiredCapability());
            request.setRequiredActions(descriptor.getPolicy().getRequiredActions());
            request.setRiskLevel(descriptor.getPolicy().getRiskLevel());
            request.setApprovalRequired(descriptor.getPolicy().getApprovalRequired());
        }
        return validate(request, identity, agentCapabilities, scenarioCapabilities);
    }

}
