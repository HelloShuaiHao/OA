package cn.iocoder.yudao.module.agentx.service.tool;

import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

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
    /**
     * Tool 所需 action（通常来自 ToolPolicy.requiredActions）
     */
    private List<String> requiredActions;
    /**
     * 当前会话可执行 action（通常来自 systemEnforcedContext.allowedActions）
     */
    private List<String> allowedActions;
    private Integer riskLevel;
    private Boolean approvalRequired;
    private AgentxDataScope dataScope;

}
