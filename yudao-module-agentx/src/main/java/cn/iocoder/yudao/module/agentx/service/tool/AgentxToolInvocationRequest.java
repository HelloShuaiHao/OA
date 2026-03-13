package cn.iocoder.yudao.module.agentx.service.tool;

import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Tool 调用前校验请求。
 */
@Data
@Accessors(chain = true)
public class AgentxToolInvocationRequest {

    private String scenarioCode;
    private String businessKey;
    private String taskRunId;
    private String toolName;
    private AgentxCapability requiredCapability;
    private Integer riskLevel;
    private Boolean approvalRequired;
    private AgentxDataScope dataScope;

}
