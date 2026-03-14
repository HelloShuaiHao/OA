package cn.iocoder.yudao.module.agentx.service.task;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AgentxApprovalCallbackResult {

    private boolean processed;
    private boolean ignored;
    private boolean compensationRequired;
    private String reason;

}
