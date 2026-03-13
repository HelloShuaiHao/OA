package cn.iocoder.yudao.module.agentx.service.approval;

public class AgentxApprovalInjectionPolicy {

    public boolean allowForOaBridge() {
        return false;
    }

    public boolean allowForExternalSystemManualGate() {
        return true;
    }

}
