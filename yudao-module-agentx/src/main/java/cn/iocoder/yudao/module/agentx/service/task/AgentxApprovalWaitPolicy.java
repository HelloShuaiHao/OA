package cn.iocoder.yudao.module.agentx.service.task;

public class AgentxApprovalWaitPolicy {

    private final int maxRetryCount;

    public AgentxApprovalWaitPolicy(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public AgentxApprovalWaitDecision evaluate(String runtimeStatus, boolean timeout, boolean approvalReleased, int retryCount) {
        if ("WAITING_APPROVAL".equals(runtimeStatus)) {
            return new AgentxApprovalWaitDecision()
                    .setAction(timeout ? AgentxApprovalWaitAction.TERMINATE : AgentxApprovalWaitAction.BLOCK)
                    .setReason(timeout ? "APPROVAL_TIMEOUT" : "WAITING_APPROVAL");
        }
        if (approvalReleased && "RUNNING".equals(runtimeStatus)) {
            return new AgentxApprovalWaitDecision()
                    .setAction(AgentxApprovalWaitAction.RESUME)
                    .setReason("APPROVAL_RELEASED");
        }
        if ("FAILED".equals(runtimeStatus) && retryCount < maxRetryCount) {
            return new AgentxApprovalWaitDecision()
                    .setAction(AgentxApprovalWaitAction.RETRY)
                    .setReason("RETRY_GATE");
        }
        return new AgentxApprovalWaitDecision()
                .setAction(AgentxApprovalWaitAction.TERMINATE)
                .setReason("TERMINATED");
    }

}
