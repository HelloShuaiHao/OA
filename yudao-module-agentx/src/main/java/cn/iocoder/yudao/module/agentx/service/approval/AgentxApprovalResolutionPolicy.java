package cn.iocoder.yudao.module.agentx.service.approval;

import cn.iocoder.yudao.module.agentx.enums.AgentxTaskProjectionStatusEnum;

public class AgentxApprovalResolutionPolicy {

    public AgentxApprovalResolution resolve(AgentxApprovalOutcome outcome) {
        switch (outcome) {
            case APPROVED:
                return new AgentxApprovalResolution()
                        .setOutcome(outcome)
                        .setRuntimeAction(AgentxApprovalRuntimeAction.APPROVE)
                        .setDecisionStatus(10)
                        .setProjectionStatus(AgentxTaskProjectionStatusEnum.APPROVED.getStatus())
                        .setCompensationRequired(false);
            case REJECTED:
            case WITHDRAWN:
                return new AgentxApprovalResolution()
                        .setOutcome(outcome)
                        .setRuntimeAction(AgentxApprovalRuntimeAction.REJECT)
                        .setDecisionStatus(outcome == AgentxApprovalOutcome.REJECTED ? 20 : 30)
                        .setProjectionStatus(AgentxTaskProjectionStatusEnum.REJECTED.getStatus())
                        .setCompensationRequired(false);
            case TIMEOUT:
                return new AgentxApprovalResolution()
                        .setOutcome(outcome)
                        .setRuntimeAction(AgentxApprovalRuntimeAction.REJECT)
                        .setDecisionStatus(40)
                        .setProjectionStatus(AgentxTaskProjectionStatusEnum.COMPENSATED.getStatus())
                        .setCompensationRequired(true);
            case CANCELLED:
                return new AgentxApprovalResolution()
                        .setOutcome(outcome)
                        .setRuntimeAction(AgentxApprovalRuntimeAction.REJECT)
                        .setDecisionStatus(50)
                        .setProjectionStatus(AgentxTaskProjectionStatusEnum.FAILED.getStatus())
                        .setCompensationRequired(false);
            default:
                throw new IllegalArgumentException("Unsupported approval outcome: " + outcome);
        }
    }

}
