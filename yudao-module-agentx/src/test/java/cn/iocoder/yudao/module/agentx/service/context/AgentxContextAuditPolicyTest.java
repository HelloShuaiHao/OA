package cn.iocoder.yudao.module.agentx.service.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxContextAuditPolicyTest {

    @Test
    void shouldCreateAuditRecordWithSourceTimeActorAndRuleVersion() {
        AgentxContextAuditPolicy policy = new AgentxContextAuditPolicy();
        ContextSnapshot snapshot = new ContextSnapshot()
                .setSnapshotId("ctx-1")
                .setScenarioCode("oa.leave.approval")
                .setBusinessKey("leave:1")
                .setAssembledBy("u-1")
                .setAssembledAt("2026-03-12T21:45:00+08:00")
                .setRuleVersion("v3");

        ContextAuditRecord record = policy.toRecord(snapshot);

        assertEquals("ctx-1", record.getSnapshotId());
        assertEquals("u-1", record.getAssembledBy());
        assertEquals("2026-03-12T21:45:00+08:00", record.getAssembledAt());
        assertEquals("v3", record.getRuleVersion());
    }

}
