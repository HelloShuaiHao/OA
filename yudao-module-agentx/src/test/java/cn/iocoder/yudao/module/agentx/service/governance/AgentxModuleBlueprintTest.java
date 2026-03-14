package cn.iocoder.yudao.module.agentx.service.governance;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxModuleBlueprintTest {

    @Test
    void shouldDescribeLogicalModulesResponsibilitiesAndDependencyBoundaries() {
        AgentxModuleBlueprint blueprint = new AgentxModuleBlueprint();

        assertEquals(List.of(
                "agentx-domain",
                "agentx-identity",
                "agentx-authorization",
                "agentx-context",
                "agentx-runtime-bridge",
                "agentx-approval-bridge",
                "agentx-audit",
                "agentx-scenario"
        ), blueprint.logicalModules());
        assertEquals(List.of(
                "domain-objects",
                "enums",
                "protocol-dtos",
                "domain-service-interfaces"
        ), blueprint.responsibilitiesOf("agentx-domain"));
        assertEquals(List.of(
                "agent-principal",
                "delegation-grant",
                "execution-identity"
        ), blueprint.responsibilitiesOf("agentx-identity"));
        assertEquals(List.of(
                "capability-intersection",
                "tool-permission",
                "data-scope-control",
                "scenario-policy-evaluation"
        ), blueprint.responsibilitiesOf("agentx-authorization"));
        assertEquals(List.of(
                "standardized-audit-event",
                "tool-audit-trail",
                "prompt-decision-archive-boundary",
                "risk-event-recording"
        ), blueprint.responsibilitiesOf("agentx-audit"));
        assertFalse(blueprint.allows("agentx-domain", "controller"));
        assertFalse(blueprint.allows("agentx-domain", "infrastructure-implementation"));
        assertFalse(blueprint.allows("agentx-identity", "authorization-decision"));
        assertTrue(blueprint.canDependOn("agentx-scenario", "agentx-domain"));
        assertTrue(blueprint.canDependOn("agentx-scenario", "agentx-context"));
        assertFalse(blueprint.canDependOn("agentx-scenario", "bpm"));
        assertFalse(blueprint.canDependOn("agentx-scenario", "system"));
        assertFalse(blueprint.canDependOn("agentx-scenario", "erp"));
    }

}
