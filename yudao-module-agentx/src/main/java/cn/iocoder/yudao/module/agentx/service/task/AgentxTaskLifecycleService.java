package cn.iocoder.yudao.module.agentx.service.task;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridge;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangTaskRespDTO;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalBridgeService;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalOutcome;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalResolution;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalRequest;
import cn.iocoder.yudao.module.agentx.service.audit.AgentxAuditService;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxAuthorizationService;
import cn.iocoder.yudao.module.agentx.service.identity.ExecutionIdentity;
import cn.iocoder.yudao.module.agentx.service.workflow.AgentxWorkflowMapping;

import java.util.List;
import java.util.Set;

/**
 * AgentX 任务生命周期编排。
 */
public class AgentxTaskLifecycleService {

    private final AgentxTaskOrchestrationService orchestrationService;
    private final AgentxApprovalBridgeService approvalBridgeService;
    private final OpenfangRuntimeBridge runtimeBridge;
    private final AgentxAuditService auditService;
    private final AgentxAuthorizationService authorizationService;

    public AgentxTaskLifecycleService(AgentxTaskOrchestrationService orchestrationService,
                                      AgentxApprovalBridgeService approvalBridgeService,
                                      OpenfangRuntimeBridge runtimeBridge) {
        this(orchestrationService, approvalBridgeService, runtimeBridge, null, null);
    }

    public AgentxTaskLifecycleService(AgentxTaskOrchestrationService orchestrationService,
                                      AgentxApprovalBridgeService approvalBridgeService,
                                      OpenfangRuntimeBridge runtimeBridge,
                                      AgentxAuditService auditService) {
        this(orchestrationService, approvalBridgeService, runtimeBridge, auditService, null);
    }

    public AgentxTaskLifecycleService(AgentxTaskOrchestrationService orchestrationService,
                                      AgentxApprovalBridgeService approvalBridgeService,
                                      OpenfangRuntimeBridge runtimeBridge,
                                      AgentxAuditService auditService,
                                      AgentxAuthorizationService authorizationService) {
        this.orchestrationService = orchestrationService;
        this.approvalBridgeService = approvalBridgeService;
        this.runtimeBridge = runtimeBridge;
        this.auditService = auditService;
        this.authorizationService = authorizationService;
    }

    public AgentxTaskProjectionDO startTask(AgentxTaskStartRequest request, List<AgentxWorkflowMapping> mappings) {
        AgentxTaskProjectionDO projection = orchestrationService.startTask(request, mappings);
        if (auditService != null) {
            auditService.recordTaskStarted(projection);
        }
        return projection;
    }

    public AgentxTaskProjectionDO startAuthorizedTask(AgentxTaskStartRequest request,
                                                      List<AgentxWorkflowMapping> mappings,
                                                      Set<AgentxCapability> agentCapabilities,
                                                      ExecutionIdentity executionIdentity,
                                                      Set<AgentxCapability> scenarioCapabilities) {
        try {
            authorizationService.authorize(agentCapabilities, executionIdentity, scenarioCapabilities, request.getCapabilities());
            return startTask(request, mappings);
        } catch (ServiceException ex) {
            if (auditService != null) {
                auditService.recordAuthorizationDenied(request.getScenarioCode(), request.getBusinessKey(), ex.getMessage());
            }
            throw ex;
        }
    }

    public AgentxApprovalRequest pullPendingApprovalRequest(AgentxTaskProjectionDO projection) {
        OpenfangTaskRespDTO task = runtimeBridge.getTaskRun(projection.getOpenfangTaskRunId());
        orchestrationService.refreshProjection(projection, task);
        if (task.getPendingApprovalIds() == null || task.getPendingApprovalIds().isEmpty()) {
            return null;
        }
        AgentxApprovalRequest request = approvalBridgeService.buildApprovalRequest(projection, task.getPendingApprovalIds().get(0));
        if (auditService != null) {
            auditService.recordPendingApproval(projection, request);
        }
        return request;
    }

    public AgentxApprovalBindingDO createApprovalBinding(AgentxTaskProjectionDO projection,
                                                         AgentxApprovalRequest request,
                                                         String bpmProcessInstanceId) {
        return approvalBridgeService.createApprovalBinding(projection, request, bpmProcessInstanceId);
    }

    public AgentxApprovalBindingDO createApprovalBindingWithBpm(AgentxTaskProjectionDO projection,
                                                                AgentxApprovalRequest request) {
        String bpmProcessInstanceId = approvalBridgeService.createBpmProcessInstance(request);
        return approvalBridgeService.createApprovalBinding(projection, request, bpmProcessInstanceId);
    }

    public void resolveApproval(AgentxApprovalBindingDO binding, Long taskProjectionId,
                                AgentxApprovalBridgeService.ApprovalDecision decision, String comment) {
        if (decision == AgentxApprovalBridgeService.ApprovalDecision.APPROVED) {
            runtimeBridge.approve(binding.getOpenfangApprovalId(), comment);
        } else {
            runtimeBridge.reject(binding.getOpenfangApprovalId(), comment);
        }
        approvalBridgeService.syncDecision(binding.getId(), taskProjectionId, decision);
        if (auditService != null) {
            auditService.recordApprovalDecision(binding, decision);
        }
    }

    public AgentxApprovalCallbackResult resolveApprovalOutcome(AgentxApprovalBindingDO binding, Long taskProjectionId,
                                                               AgentxApprovalOutcome outcome, String comment) {
        if (binding.getDecisionStatus() != null) {
            return new AgentxApprovalCallbackResult()
                    .setIgnored(true)
                    .setProcessed(false)
                    .setCompensationRequired(false)
                    .setReason("DUPLICATE_CALLBACK");
        }
        AgentxApprovalResolution resolution = approvalBridgeService.resolveOutcome(binding.getId(), taskProjectionId, outcome);
        if (resolution.getRuntimeAction() == cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalRuntimeAction.APPROVE) {
            runtimeBridge.approve(binding.getOpenfangApprovalId(), comment);
        } else if (resolution.getRuntimeAction() == cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalRuntimeAction.REJECT) {
            runtimeBridge.reject(binding.getOpenfangApprovalId(), comment);
        }
        return new AgentxApprovalCallbackResult()
                .setProcessed(true)
                .setIgnored(false)
                .setCompensationRequired(resolution.isCompensationRequired())
                .setReason(outcome.name());
    }

}
