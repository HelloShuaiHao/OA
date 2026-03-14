package cn.iocoder.yudao.module.agentx.service.task;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AgentxApprovalWaitDecision {

    private AgentxApprovalWaitAction action;
    private String reason;

}
