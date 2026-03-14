package cn.iocoder.yudao.module.agentx.service.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxApprovalWaitPolicyTest {

    @Test
    void shouldBlockResumeTerminateAndRetryBasedOnRuntimeState() {
        AgentxApprovalWaitPolicy policy = new AgentxApprovalWaitPolicy(2);

        assertEquals(AgentxApprovalWaitAction.BLOCK,
                policy.evaluate("WAITING_APPROVAL", false, false, 0).getAction());
        assertEquals(AgentxApprovalWaitAction.TERMINATE,
                policy.evaluate("WAITING_APPROVAL", true, false, 0).getAction());
        assertEquals(AgentxApprovalWaitAction.RESUME,
                policy.evaluate("RUNNING", false, true, 0).getAction());
        assertEquals(AgentxApprovalWaitAction.RETRY,
                policy.evaluate("FAILED", false, false, 1).getAction());
        assertEquals(AgentxApprovalWaitAction.TERMINATE,
                policy.evaluate("FAILED", false, false, 2).getAction());
    }

}
