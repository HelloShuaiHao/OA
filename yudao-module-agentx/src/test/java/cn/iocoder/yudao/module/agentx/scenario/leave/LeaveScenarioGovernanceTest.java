package cn.iocoder.yudao.module.agentx.scenario.leave;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaveScenarioGovernanceTest {

    @Test
    void shouldDeclareLeaveScenarioAsThinPluginAndReusePlatformCapabilities() {
        LeaveScenarioGovernance governance = new LeaveScenarioGovernance();

        assertEquals("thin-plugin", governance.integrationMode());
        assertTrue(governance.reusesPlatformIdentity());
        assertTrue(governance.reusesPlatformApproval());
        assertTrue(governance.reusesPlatformAudit());
        assertFalse(governance.allowsScenarioOwnedWorkflowRegistry());
        assertFalse(governance.allowsScenarioOwnedToolRegistry());
    }

}
