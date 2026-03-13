package cn.iocoder.yudao.module.agentx.scenario.leave;

import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaveScenarioDefinitionTest {

    private final LeaveScenarioDefinition definition = new LeaveScenarioDefinition();
    private final LeaveContextProvider contextProvider = new LeaveContextProvider();
    private final LeaveToolSet toolSet = new LeaveToolSet();
    private final LeaveApprovalPolicy approvalPolicy = new LeaveApprovalPolicy();

    @Test
    void shouldExposeThinScenarioDefinition() {
        assertEquals("oa.leave.approval", definition.getScenarioCode());
        assertEquals("请假审批助理", definition.getDisplayName());
        assertTrue(definition.getRequiredCapabilities().contains(AgentxCapability.READ_LEAVE));
        assertTrue(definition.getRequiredCapabilities().contains(AgentxCapability.SUBMIT_LEAVE));
    }

    @Test
    void shouldExposeLeaveContextAndToolAssembly() {
        assertEquals(Arrays.asList("leave.form", "leave.balance", "applicant.profile"),
                contextProvider.requiredContextKeys());
        assertEquals(Arrays.asList("leave.query", "leave.submit"), toolSet.toolCodes());
    }

    @Test
    void shouldRequireApprovalForSubmitAction() {
        assertTrue(approvalPolicy.requiresApproval("leave.submit", 20));
        assertTrue(approvalPolicy.requiresApproval("leave.submit", 30));
    }

}
