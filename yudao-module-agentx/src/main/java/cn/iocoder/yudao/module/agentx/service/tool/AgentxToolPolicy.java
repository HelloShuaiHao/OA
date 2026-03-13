package cn.iocoder.yudao.module.agentx.service.tool;

import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Tool 安全策略。
 */
@Data
@Accessors(chain = true)
public class AgentxToolPolicy {

    private AgentxCapability requiredCapability;
    private Integer riskLevel;
    private Boolean approvalRequired;
    private List<String> auditTags;

}
