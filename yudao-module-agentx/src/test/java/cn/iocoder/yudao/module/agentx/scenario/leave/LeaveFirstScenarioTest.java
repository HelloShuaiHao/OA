package cn.iocoder.yudao.module.agentx.scenario.leave;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LeaveFirstScenarioTest {

    @Test
    void shouldDeclareLeaveAssistantAsTheFirstValidationScenario() {
        LeaveFirstScenario governance = new LeaveFirstScenario();

        assertEquals("leave-approval-assistant", governance.scenarioCode());
        assertEquals("platform-path-validation-only", governance.validationScope());
        assertFalse(governance.allowsPlatformBoundaryExpansion());
    }

}
