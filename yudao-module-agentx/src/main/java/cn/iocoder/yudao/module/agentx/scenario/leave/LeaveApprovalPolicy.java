package cn.iocoder.yudao.module.agentx.scenario.leave;

/**
 * 请假场景审批策略。
 */
public class LeaveApprovalPolicy {

    public boolean requiresApproval(String actionCode, Integer riskLevel) {
        return "leave.submit".equals(actionCode) && riskLevel != null && riskLevel >= 20;
    }

}
