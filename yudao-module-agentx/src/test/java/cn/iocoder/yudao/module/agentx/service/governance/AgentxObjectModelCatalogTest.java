package cn.iocoder.yudao.module.agentx.service.governance;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxObjectModelCatalogTest {

    @Test
    void shouldDefineCoreObjectModelsFieldMetadataAndCrossSystemMappings() {
        AgentxObjectModelCatalog catalog = new AgentxObjectModelCatalog();

        assertEquals(List.of(
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
        ), catalog.modelNames());
        assertTrue(catalog.fieldsOf("ExecutionIdentity").contains("tenantId"));
        assertTrue(catalog.fieldsOf("ScenarioTaskRequest").contains("idempotencyKey"));
        assertTrue(catalog.fieldsOf("ApprovalBinding").contains("approvalInstanceId"));
        assertTrue(catalog.fieldsOf("AuditEvent").contains("createdBy"));
        assertEquals("persistent", catalog.lifecycleOf("TaskBinding"));
        assertEquals("persistent", catalog.lifecycleOf("TaskProjection"));
        assertEquals("runtime", catalog.lifecycleOf("TaskCheckpoint"));
        assertEquals("persistent", catalog.lifecycleOf("ApprovalDecision"));
        assertEquals("runtime", catalog.lifecycleOf("ApprovalBlockingProjection"));
        assertEquals(List.of(
                "oaTaskId -> openfangTaskRunId",
                "oaApprovalInstanceId -> openfangApprovalId",
                "businessDocumentId -> approvalInstanceId"
        ), catalog.crossSystemMappings());
        assertEquals("oa-decision-truth", catalog.separationRule("ApprovalDecision"));
        assertEquals("openfang-gate-projection", catalog.separationRule("ApprovalBlockingProjection"));
    }

}
