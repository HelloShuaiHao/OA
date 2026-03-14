package cn.iocoder.yudao.module.agentx.scenario;

import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;

import java.util.Set;

/**
 * 场景定义接口。
 */
public interface AgentxScenarioDefinition {

    String getScenarioCode();

    String getDisplayName();

    Set<AgentxCapability> getRequiredCapabilities();

}
