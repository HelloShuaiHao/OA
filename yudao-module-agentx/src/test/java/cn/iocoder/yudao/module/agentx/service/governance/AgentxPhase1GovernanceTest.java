package cn.iocoder.yudao.module.agentx.service.governance;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AgentxPhase1GovernanceTest {

    @Test
    void shouldDefineResponsibilitiesTruthOwnershipAndPhase1ProtocolBoundary() {
        AgentxPhase1Governance governance = new AgentxPhase1Governance();

        assertEquals(List.of(
                "organization-truth",
                "approval-decision-truth",
                "existing-bpm-and-business-records"
        ), governance.oaResponsibilities());
        assertEquals(List.of(
                "workflow-definition-truth",
                "task-run-truth",
                "approval-blocking-runtime-projection"
        ), governance.openfangResponsibilities());
        assertEquals(List.of(
                "scenario-workflow-mapping",
                "authorization-and-tool-guard",
                "approval-bridge",
                "audit-summary-and-task-projection"
        ), governance.agentxResponsibilities());
        assertEquals("oa", governance.truthOwnerOf("organization"));
        assertEquals("oa", governance.truthOwnerOf("approval_decision"));
        assertEquals("openfang", governance.truthOwnerOf("task_run"));
        assertEquals("openfang", governance.truthOwnerOf("pending_approval_ids"));
        assertEquals("openfang", governance.truthOwnerOf("recovery_projection"));
        assertEquals("openfang", governance.truthOwnerOf("workflow_definition"));
        assertEquals("agentx", governance.truthOwnerOf("scenario_workflow_mapping"));
        assertEquals("REST_ONLY", governance.phase1ProtocolMode());
        assertFalse(governance.requiresWebhook());
        assertFalse(governance.requiresOutbox());
    }

}
