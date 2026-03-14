package cn.iocoder.yudao.module.agentx.service.tool;

import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;

import java.util.EnumSet;
import java.util.Set;

/**
 * 高风险能力目录。
 */
public class AgentxRiskControlService {

    private static final Set<AgentxCapability> HIGH_RISK_CAPABILITIES =
            EnumSet.of(AgentxCapability.SUBMIT_LEAVE, AgentxCapability.UPDATE_LEAVE);

    public boolean isHighRiskCapability(AgentxCapability capability) {
        return HIGH_RISK_CAPABILITIES.contains(capability);
    }

    public int resolveRiskLevel(AgentxCapability capability) {
        return isHighRiskCapability(capability) ? 30 : 10;
    }

}
