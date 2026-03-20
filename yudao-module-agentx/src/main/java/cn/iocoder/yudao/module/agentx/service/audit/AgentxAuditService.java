package cn.iocoder.yudao.module.agentx.service.audit;

import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalBridgeService;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalRequest;
import cn.iocoder.yudao.module.agentx.service.identity.ExecutionIdentity;
import cn.iocoder.yudao.module.agentx.service.tool.AgentxDataScope;

/**
 * AgentX 审计服务。
 */
public interface AgentxAuditService {

    void recordTaskStarted(AgentxTaskProjectionDO projection);

    void recordTaskCompleted(AgentxTaskProjectionDO projection);

    void recordPendingApproval(AgentxTaskProjectionDO projection, AgentxApprovalRequest request);

    void recordApprovalDecision(AgentxApprovalBindingDO binding, AgentxApprovalBridgeService.ApprovalDecision decision);

    void recordAuthorizationDenied(String scenarioCode, String businessKey, String reason);

    void recordToolInvocation(ExecutionIdentity identity, String scenarioCode, String businessKey, String taskRunId,
                              String toolName, AgentxDataScope dataScope, Integer riskLevel, String result);

    void recordToolCompletion(ExecutionIdentity identity, String scenarioCode, String businessKey, String taskRunId,
                              String toolName, AgentxDataScope dataScope, Integer riskLevel, String requestSummary,
                              String resultSummary, Long durationMs, Integer errorCode, String businessImpactSummary);

}
