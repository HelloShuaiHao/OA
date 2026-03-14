package cn.iocoder.yudao.module.agentx.service.governance;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxAuditGovernanceTest {

    @Test
    void shouldDefineUnifiedAuditModelArchiveBoundaryAndQueryDimensions() {
        AgentxAuditGovernance governance = new AgentxAuditGovernance();

        assertEquals(List.of(
                "task-lifecycle",
                "tool-invocation",
                "approval-action",
                "business-write",
                "authorization-failure"
        ), governance.auditEventCategories());
        assertEquals("summary-only", governance.promptTraceArchiveMode());
        assertEquals("summary-only", governance.decisionTraceArchiveMode());
        assertEquals(List.of(
                "userId",
                "agentId",
                "businessKey",
                "approvalInstanceId",
                "taskStatus",
                "riskLevel"
        ), governance.queryDimensions());
    }

}
