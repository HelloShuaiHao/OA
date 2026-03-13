package cn.iocoder.yudao.module.agentx.framework.openfang.client;

import java.util.List;

public class OpenfangPhase1Protocol {

    public List<String> getPrimaryEndpoints() {
        return List.of(
                "POST /api/workflows/{id}/run",
                "GET /api/tasks/{id}",
                "GET /api/approvals/{approvalId}?task_run_id={taskRunId}",
                "POST /api/approvals/{approvalId}/approve",
                "POST /api/approvals/{approvalId}/reject"
        );
    }

    public boolean isWebhookEnabledByDefault() {
        return false;
    }

    public String getProjectionSyncMode() {
        return "POLLING";
    }

}
