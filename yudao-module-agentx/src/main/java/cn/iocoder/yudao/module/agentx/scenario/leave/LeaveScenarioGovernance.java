package cn.iocoder.yudao.module.agentx.scenario.leave;

public class LeaveScenarioGovernance {

    public String integrationMode() {
        return "thin-plugin";
    }

    public boolean reusesPlatformIdentity() {
        return true;
    }

    public boolean reusesPlatformApproval() {
        return true;
    }

    public boolean reusesPlatformAudit() {
        return true;
    }

    public boolean allowsScenarioOwnedWorkflowRegistry() {
        return false;
    }

    public boolean allowsScenarioOwnedToolRegistry() {
        return false;
    }

}
