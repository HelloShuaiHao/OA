package cn.iocoder.yudao.module.agentx.service.governance;

import java.util.List;
import java.util.Map;

/**
 * 汇总 AgentX 的核心对象模型、字段说明、跨系统映射和运行态边界。
 */
public class AgentxObjectModelCatalog {

    private static final Map<String, List<String>> FIELDS = Map.ofEntries(
            Map.entry("AgentPrincipal", List.of("agentId", "agentCode", "displayName", "tenantId")),
            Map.entry("HumanPrincipal", List.of("userId", "username", "tenantId")),
            Map.entry("DelegationGrant", List.of("grantId", "delegatorUserId", "agentId", "scope", "expiresAt", "tenantId")),
            Map.entry("ExecutionIdentity", List.of("executionId", "agentId", "humanUserId", "delegationGrantId", "tenantId")),
            Map.entry("Capability", List.of("capabilityCode", "riskLevel", "tenantId")),
            Map.entry("ToolCapability", List.of("toolCode", "capabilityCode", "approvalRequired")),
            Map.entry("DataAccessPolicy", List.of("scopeType", "scopeValue", "maskedFields", "tenantId")),
            Map.entry("ScenarioPolicy", List.of("scenarioCode", "requiredCapabilities", "enabled", "tenantId")),
            Map.entry("ScenarioTaskRequest", List.of("requestId", "scenarioCode", "businessKey", "idempotencyKey", "tenantId")),
            Map.entry("TaskBinding", List.of("bindingId", "oaTaskId", "openfangTaskRunId", "businessKey", "tenantId")),
            Map.entry("TaskProjection", List.of("projectionId", "openfangTaskRunId", "status", "stage", "resultSummary", "tenantId")),
            Map.entry("TaskCheckpoint", List.of("checkpointId", "openfangTaskRunId", "checkpointStage", "checkpointAt")),
            Map.entry("ApprovalBinding", List.of("bindingId", "approvalInstanceId", "openfangApprovalId", "openfangTaskRunId", "tenantId")),
            Map.entry("ApprovalRequest", List.of("title", "reason", "riskLevel", "actionSummary", "tenantId")),
            Map.entry("ApprovalDecision", List.of("approvalInstanceId", "decisionStatus", "decidedBy", "decidedAt", "tenantId")),
            Map.entry("ApprovalBlockingProjection", List.of("openfangApprovalId", "pendingApprovalIds", "runtimeStatus", "updatedAt")),
            Map.entry("AuditEvent", List.of("eventId", "eventType", "createdBy", "tenantId", "createdAt")),
            Map.entry("ToolAuditEvent", List.of("eventId", "toolCode", "resultCode", "durationMs", "tenantId")),
            Map.entry("BusinessWriteAuditEvent", List.of("eventId", "businessAction", "businessKey", "impactScope", "tenantId")),
            Map.entry("RiskControlEvent", List.of("eventId", "riskType", "riskLevel", "decision", "tenantId"))
    );

    public List<String> modelNames() {
        return List.of(
                "AgentPrincipal",
                "HumanPrincipal",
                "DelegationGrant",
                "ExecutionIdentity",
                "Capability",
                "ToolCapability",
                "DataAccessPolicy",
                "ScenarioPolicy",
                "ScenarioTaskRequest",
                "TaskBinding",
                "TaskProjection",
                "TaskCheckpoint",
                "ApprovalBinding",
                "ApprovalRequest",
                "ApprovalDecision",
                "ApprovalBlockingProjection",
                "AuditEvent",
                "ToolAuditEvent",
                "BusinessWriteAuditEvent",
                "RiskControlEvent"
        );
    }

    public List<String> fieldsOf(String modelName) {
        List<String> fields = FIELDS.get(modelName);
        if (fields == null) {
            throw new IllegalArgumentException("Unknown model: " + modelName);
        }
        return fields;
    }

    public String lifecycleOf(String modelName) {
        switch (modelName) {
            case "TaskBinding":
            case "TaskProjection":
            case "ApprovalDecision":
                return "persistent";
            case "TaskCheckpoint":
            case "ApprovalBlockingProjection":
                return "runtime";
            default:
                throw new IllegalArgumentException("Unknown model lifecycle: " + modelName);
        }
    }

    public List<String> crossSystemMappings() {
        return List.of(
                "oaTaskId -> openfangTaskRunId",
                "oaApprovalInstanceId -> openfangApprovalId",
                "businessDocumentId -> approvalInstanceId"
        );
    }

    public String separationRule(String modelName) {
        switch (modelName) {
            case "ApprovalDecision":
                return "oa-decision-truth";
            case "ApprovalBlockingProjection":
                return "openfang-gate-projection";
            default:
                throw new IllegalArgumentException("Unknown separation model: " + modelName);
        }
    }

}
