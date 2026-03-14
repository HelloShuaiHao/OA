package cn.iocoder.yudao.module.agentx.service.governance;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxPhase1GuardrailsTest {

    @Test
    void shouldFreezeForbiddenActionsAndPhase1TechDeploymentBoundaries() {
        AgentxPhase1Guardrails guardrails = new AgentxPhase1Guardrails();

        assertEquals(List.of(
                "forbid-openfang-direct-oa-db-access",
                "forbid-scenario-owned-permission-system",
                "forbid-dual-approval-state-machines"
        ), guardrails.forbiddenActions());
        assertEquals("java", guardrails.primaryImplementationLanguage());
        assertEquals("not-in-core-phase1", guardrails.rustRole());
        assertEquals("in-existing-oa-modules", guardrails.oaDeploymentMode());
        assertEquals("standalone-rest-peer", guardrails.openfangDeploymentMode());
    }

}
