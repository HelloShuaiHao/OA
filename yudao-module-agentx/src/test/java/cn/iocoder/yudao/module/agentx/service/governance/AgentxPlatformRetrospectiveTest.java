package cn.iocoder.yudao.module.agentx.service.governance;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxPlatformRetrospectiveTest {

    @Test
    void shouldDistinguishReusablePlatformAbstractionsFromLeaveSpecificPlugins() {
        AgentxPlatformRetrospective retrospective = new AgentxPlatformRetrospective();

        assertEquals(List.of(
                "workflow-mapping",
                "context-assembly",
                "task-orchestration",
                "tool-guard",
                "approval-bridge",
                "authorization-delegation",
                "audit-reporting"
        ), retrospective.reusablePlatformAbstractions());
        assertEquals(List.of(
                "leave-scenario-definition",
                "leave-context-provider",
                "leave-tool-set",
                "leave-approval-policy"
        ), retrospective.leaveSpecificPlugins());
        assertEquals(List.of(
                "expense",
                "travel",
                "procurement"
        ), retrospective.nextCandidateScenarios());
    }

}
