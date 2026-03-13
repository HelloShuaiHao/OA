package cn.iocoder.yudao.module.agentx.framework.openfang.client;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenfangInterfaceBaselineTest {

    @Test
    void shouldListPhase1BaselineInterfacesIncludingResumeAndCompensate() {
        OpenfangInterfaceBaseline baseline = new OpenfangInterfaceBaseline();

        assertEquals(List.of(
                "POST /api/workflows/{id}/run",
                "GET /api/tasks/{id}",
                "POST /api/tasks/{id}/resume",
                "POST /api/tasks/{id}/compensate",
                "GET /api/approvals/{approvalId}?task_run_id={taskRunId}",
                "POST /api/approvals/{approvalId}/approve",
                "POST /api/approvals/{approvalId}/reject"
        ), baseline.phase1Interfaces());
    }

}
