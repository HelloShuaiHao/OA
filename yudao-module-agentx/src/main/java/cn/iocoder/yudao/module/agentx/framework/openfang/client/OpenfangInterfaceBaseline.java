package cn.iocoder.yudao.module.agentx.framework.openfang.client;

import java.util.List;

public class OpenfangInterfaceBaseline {

    public List<String> phase1Interfaces() {
        return List.of(
                "POST /api/workflows/{id}/run",
                "GET /api/tasks/{id}",
                "POST /api/tasks/{id}/resume",
                "POST /api/tasks/{id}/compensate",
                "GET /api/approvals/{approvalId}?task_run_id={taskRunId}",
                "POST /api/approvals/{approvalId}/approve",
                "POST /api/approvals/{approvalId}/reject"
        );
    }

}
