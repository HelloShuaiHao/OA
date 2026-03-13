package cn.iocoder.yudao.module.agentx.service.governance;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxAcceptanceBaselineTest {

    @Test
    void shouldDeclarePlatformAcceptanceGates() {
        AgentxAcceptanceBaseline baseline = new AgentxAcceptanceBaseline();

        assertEquals(List.of(
                "scenario-reuses-platform-foundation",
                "openfang-cannot-access-oa-db-or-bypass-approval",
                "oa-only-sees-projection-binding-and-audit-summary",
                "approval-truth-stays-in-oa-runtime-projection-stays-in-openfang",
                "leave-scenario-runs-end-to-end-with-audit-and-binding",
                "platform-abstractions-support-next-approval-scenario"
        ), baseline.gates());
    }

}
