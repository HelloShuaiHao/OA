package cn.iocoder.yudao.module.agentx.service.governance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxAiCapabilityLayeringTest {

    @Test
    void shouldDecideWhichAiCapabilitiesAreReusedAndWhichNeedAgentxIntegrationWrappers() {
        AgentxAiCapabilityLayering layering = new AgentxAiCapabilityLayering();

        assertEquals("reuse-ai-console", layering.decisionOf("workflow-console"));
        assertEquals("reuse-ai-tool-registry", layering.decisionOf("tool-registry"));
        assertEquals("reuse-with-agentx-wrapper", layering.decisionOf("tool-context"));
        assertEquals("reuse-with-agentx-wrapper", layering.decisionOf("tenant-user-context"));
        assertEquals("new-agentx-scenario-mapping", layering.decisionOf("scenario-workflow-mapping"));
        assertEquals("forbid-second-console", layering.decisionOf("agentx-workflow-console"));
    }

}
