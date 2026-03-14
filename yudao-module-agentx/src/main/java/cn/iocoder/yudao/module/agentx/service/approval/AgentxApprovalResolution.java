package cn.iocoder.yudao.module.agentx.service.approval;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AgentxApprovalResolution {

    private AgentxApprovalOutcome outcome;
    private AgentxApprovalRuntimeAction runtimeAction;
    private Integer decisionStatus;
    private Integer projectionStatus;
    private boolean compensationRequired;

}
