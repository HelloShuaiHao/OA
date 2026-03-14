package cn.iocoder.yudao.module.agentx.service.audit;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxOaFieldVisibilityPolicyTest {

    @Test
    void shouldHidePromptLoopMemoryAndReasoningFieldsFromOa() {
        AgentxOaFieldVisibilityPolicy policy = new AgentxOaFieldVisibilityPolicy();
        Map<String, Object> payload = Map.of(
                "resultSummary", "ok",
                "promptLoop", "secret",
                "memoryState", "hidden",
                "reasoningTrace", "hidden",
                "businessImpactSummary", "leave updated"
        );

        Map<String, Object> visible = policy.filter(payload);

        assertTrue(visible.containsKey("resultSummary"));
        assertTrue(visible.containsKey("businessImpactSummary"));
        assertFalse(visible.containsKey("promptLoop"));
        assertFalse(visible.containsKey("memoryState"));
        assertFalse(visible.containsKey("reasoningTrace"));
        assertEquals(2, visible.size());
    }

}
