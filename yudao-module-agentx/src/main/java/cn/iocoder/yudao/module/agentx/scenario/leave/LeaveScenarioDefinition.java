package cn.iocoder.yudao.module.agentx.scenario.leave;

import cn.iocoder.yudao.module.agentx.scenario.AgentxScenarioDefinition;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;

import java.util.EnumSet;
import java.util.Set;

/**
 * 请假审批助理场景定义。
 */
public class LeaveScenarioDefinition implements AgentxScenarioDefinition {

    @Override
    public String getScenarioCode() {
        return "oa.leave.approval";
    }

    @Override
    public String getDisplayName() {
        return "请假审批助理";
    }

    @Override
    public Set<AgentxCapability> getRequiredCapabilities() {
        return EnumSet.of(AgentxCapability.READ_LEAVE, AgentxCapability.SUBMIT_LEAVE);
    }

}
