package cn.iocoder.yudao.module.agentx.framework.openfang.client;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class OpenfangPhase1ProtocolTest {

    @Test
    void shouldUsePollingAsDefaultPhase1ProjectionProtocol() {
        OpenfangPhase1Protocol protocol = new OpenfangPhase1Protocol();

        assertEquals(List.of("POST /api/workflows/{id}/run", "GET /api/tasks/{id}",
                "GET /api/approvals/{approvalId}?task_run_id={taskRunId}",
                "POST /api/approvals/{approvalId}/approve", "POST /api/approvals/{approvalId}/reject"),
                protocol.getPrimaryEndpoints());
        assertFalse(protocol.isWebhookEnabledByDefault());
        assertEquals("POLLING", protocol.getProjectionSyncMode());
    }

}
