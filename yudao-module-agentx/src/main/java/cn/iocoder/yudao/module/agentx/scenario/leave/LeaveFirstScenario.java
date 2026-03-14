package cn.iocoder.yudao.module.agentx.scenario.leave;

/**
 * 定义首个落地场景的验证范围。
 */
public class LeaveFirstScenario {

    public String scenarioCode() {
        return "leave-approval-assistant";
    }

    public String validationScope() {
        return "platform-path-validation-only";
    }

    public boolean allowsPlatformBoundaryExpansion() {
        return false;
    }

}
