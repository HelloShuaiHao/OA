package cn.iocoder.yudao.module.agentx.framework.openfang.client;

public class OpenfangApprovalQueryContract {

    public String getQueryKeys() {
        return "approval_id + task_run_id";
    }

    public String getTenantIsolationRule() {
        return "tenant scoped bearer token";
    }

    public String getRequiredPermission() {
        return "approval:read";
    }

    public String getResponseFields() {
        return "approvalId, taskRunId, title, reason, riskLevel, actionSummary, requesterId";
    }

    public String getPaginationRule() {
        return "no pagination; exact query only";
    }

}
