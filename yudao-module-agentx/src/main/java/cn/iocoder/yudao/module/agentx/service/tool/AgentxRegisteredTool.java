package cn.iocoder.yudao.module.agentx.service.tool;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AgentxRegisteredTool {

    private String toolName;
    private String version;
    private AgentxToolDescriptor descriptor;
    private AgentxToolLifecycleStatus status;
    private String submittedBy;
    private String approvedBy;
    private String statusReason;

}
