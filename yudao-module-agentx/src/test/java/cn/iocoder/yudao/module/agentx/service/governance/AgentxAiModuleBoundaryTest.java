package cn.iocoder.yudao.module.agentx.service.governance;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxAiModuleBoundaryTest {

    @Test
    void shouldSeparateAiAuthoringCapabilitiesFromAgentxRuntimeIntegration() {
        AgentxAiModuleBoundary boundary = new AgentxAiModuleBoundary();

        assertEquals(List.of(
                "ai.workflow.catalog-crud",
                "ai.workflow.test",
                "ai.tool.catalog-crud"
        ), boundary.aiOwnedCapabilities());
        assertEquals(List.of(
                "agentx.scenario.workflow-mapping",
                "agentx.runtime.task-orchestration",
                "agentx.runtime.tool-guard",
                "agentx.approval.bridge",
                "agentx.authorization.delegation",
                "agentx.audit.reporting"
        ), boundary.agentxOwnedCapabilities());
        assertTrue(boundary.leaveScenarioReusesAgentxIntegration());
        assertTrue(boundary.duplicatedCapabilities().isEmpty());
    }

}
