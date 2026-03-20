package cn.iocoder.yudao.module.agentx.service.audit;

import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.audit.AgentxAuditEventDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.audit.AgentxAuditEventMapper;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalBridgeService;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalRequest;
import cn.iocoder.yudao.module.agentx.service.identity.ExecutionIdentity;
import cn.iocoder.yudao.module.agentx.service.tool.AgentxDataScope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * AgentX 审计服务实现。
 */
@Service
public class AgentxAuditServiceImpl implements AgentxAuditService {

    @Resource
    private AgentxAuditEventMapper auditEventMapper;
    @Resource
    private AgentxAuditDesensitizeService desensitizeService;

    @Override
    public void recordTaskStarted(AgentxTaskProjectionDO projection) {
        auditEventMapper.insert(new AgentxAuditEventDO()
                .setEventType("TASK_STARTED")
                .setScenarioCode(projection.getScenarioCode())
                .setBusinessKey(projection.getBusinessKey())
                .setOpenfangTaskRunId(projection.getOpenfangTaskRunId())
                .setRiskLevel(projection.getRiskLevel())
                .setResultSummary(mask("task started")));
    }

    @Override
    public void recordTaskCompleted(AgentxTaskProjectionDO projection) {
        auditEventMapper.insert(new AgentxAuditEventDO()
                .setEventType("TASK_COMPLETED")
                .setScenarioCode(projection.getScenarioCode())
                .setBusinessKey(projection.getBusinessKey())
                .setOpenfangTaskRunId(projection.getOpenfangTaskRunId())
                .setRiskLevel(projection.getRiskLevel())
                .setResultSummary(mask(projection.getResultSummary())));
    }

    @Override
    public void recordPendingApproval(AgentxTaskProjectionDO projection, AgentxApprovalRequest request) {
        auditEventMapper.insert(new AgentxAuditEventDO()
                .setEventType("TASK_WAITING_APPROVAL")
                .setScenarioCode(projection.getScenarioCode())
                .setBusinessKey(projection.getBusinessKey())
                .setOpenfangTaskRunId(projection.getOpenfangTaskRunId())
                .setOpenfangApprovalId(request.getOpenfangApprovalId())
                .setRiskLevel(request.getRiskLevel())
                .setResultSummary(mask(request.getActionSummary())));
    }

    @Override
    public void recordApprovalDecision(AgentxApprovalBindingDO binding, AgentxApprovalBridgeService.ApprovalDecision decision) {
        auditEventMapper.insert(new AgentxAuditEventDO()
                .setEventType("APPROVAL_" + decision.name())
                .setScenarioCode(binding.getScenarioCode())
                .setBusinessKey(binding.getBusinessKey())
                .setOpenfangTaskRunId(binding.getOpenfangTaskRunId())
                .setOpenfangApprovalId(binding.getOpenfangApprovalId())
                .setRiskLevel(binding.getRiskLevel())
                .setResultSummary(mask(binding.getActionSummary())));
    }

    @Override
    public void recordAuthorizationDenied(String scenarioCode, String businessKey, String reason) {
        auditEventMapper.insert(new AgentxAuditEventDO()
                .setEventType("AUTHORIZATION_DENIED")
                .setScenarioCode(scenarioCode)
                .setBusinessKey(businessKey)
                .setResultSummary(mask(reason)));
    }

    @Override
    public void recordToolInvocation(ExecutionIdentity identity, String scenarioCode, String businessKey, String taskRunId,
                                     String toolName, AgentxDataScope dataScope, Integer riskLevel, String result) {
        auditEventMapper.insert(new AgentxAuditEventDO()
                .setTenantId(identity != null ? identity.getTenantId() : null)
                .setInitiatorId(identity != null ? identity.getPrincipalId() : null)
                .setAgentCode(identity != null ? identity.getAgentCode() : null)
                .setEventType("TOOL_INVOCATION")
                .setScenarioCode(scenarioCode)
                .setBusinessKey(businessKey)
                .setOpenfangTaskRunId(taskRunId)
                .setToolName(toolName)
                .setRiskLevel(riskLevel)
                .setDataScopeSummary(dataScope != null ? dataScope.summarize() : null)
                .setResultSummary(mask(result)));
    }

    @Override
    public void recordToolCompletion(ExecutionIdentity identity, String scenarioCode, String businessKey, String taskRunId,
                                     String toolName, AgentxDataScope dataScope, Integer riskLevel, String requestSummary,
                                     String resultSummary, Long durationMs, Integer errorCode, String businessImpactSummary) {
        auditEventMapper.insert(new AgentxAuditEventDO()
                .setTenantId(identity != null ? identity.getTenantId() : null)
                .setInitiatorId(identity != null ? identity.getPrincipalId() : null)
                .setAgentCode(identity != null ? identity.getAgentCode() : null)
                .setEventType("TOOL_COMPLETED")
                .setScenarioCode(scenarioCode)
                .setBusinessKey(businessKey)
                .setOpenfangTaskRunId(taskRunId)
                .setToolName(toolName)
                .setRiskLevel(riskLevel)
                .setDataScopeSummary(dataScope != null ? dataScope.summarize() : null)
                .setRequestSummary(mask(requestSummary))
                .setResultSummary(mask(resultSummary))
                .setDurationMs(durationMs)
                .setErrorCode(errorCode)
                .setBusinessImpactSummary(mask(businessImpactSummary)));
    }

    private String mask(String value) {
        return desensitizeService.mask(value);
    }

}
