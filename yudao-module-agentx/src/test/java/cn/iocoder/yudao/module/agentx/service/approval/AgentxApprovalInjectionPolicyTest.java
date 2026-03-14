package cn.iocoder.yudao.module.agentx.service.approval;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxApprovalInjectionPolicyTest {

    @Test
    void shouldOnlyAllowManualGateInjectionForExternalSystems() {
        AgentxApprovalInjectionPolicy policy = new AgentxApprovalInjectionPolicy();

        assertFalse(policy.allowForOaBridge());
        assertTrue(policy.allowForExternalSystemManualGate());
    }

}
