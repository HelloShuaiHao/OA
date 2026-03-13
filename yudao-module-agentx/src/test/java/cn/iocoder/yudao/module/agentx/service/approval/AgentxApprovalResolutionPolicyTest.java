package cn.iocoder.yudao.module.agentx.service.approval;

import cn.iocoder.yudao.module.agentx.enums.AgentxTaskProjectionStatusEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxApprovalResolutionPolicyTest {

    @Test
    void shouldMapTimeoutToRejectRuntimeAndCompensation() {
        AgentxApprovalResolutionPolicy policy = new AgentxApprovalResolutionPolicy();

        AgentxApprovalResolution resolution = policy.resolve(AgentxApprovalOutcome.TIMEOUT);

        assertEquals(AgentxApprovalRuntimeAction.REJECT, resolution.getRuntimeAction());
        assertEquals(AgentxTaskProjectionStatusEnum.COMPENSATED.getStatus(), resolution.getProjectionStatus());
        assertTrue(resolution.isCompensationRequired());
        assertEquals(Integer.valueOf(40), resolution.getDecisionStatus());
    }

}
