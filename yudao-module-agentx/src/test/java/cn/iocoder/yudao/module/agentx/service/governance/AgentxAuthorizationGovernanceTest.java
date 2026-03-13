package cn.iocoder.yudao.module.agentx.service.governance;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxAuthorizationGovernanceTest {

    @Test
    void shouldDefineAgentIdentityDelegationToolPermissionBoundaryAndHighRiskCapabilities() {
        AgentxAuthorizationGovernance governance = new AgentxAuthorizationGovernance();

        assertEquals("service-principal-not-human-user", governance.agentIdentityModel());
        assertEquals(List.of(
                "delegatorUserId",
                "agentId",
                "delegatedCapabilities",
                "dataScope",
                "expiresAt",
                "revokedAt",
                "auditTraceId"
        ), governance.delegationGrantFields());
        assertEquals("tool-capability-not-menu-visibility", governance.toolPermissionBoundary());
        assertEquals(List.of(
                "submit-approval",
                "modify-business-record",
                "create-business-record",
                "trigger-external-notification"
        ), governance.highRiskCapabilities());
    }

}
