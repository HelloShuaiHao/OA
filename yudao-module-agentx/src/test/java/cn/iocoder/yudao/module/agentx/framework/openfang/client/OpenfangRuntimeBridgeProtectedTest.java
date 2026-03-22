package cn.iocoder.yudao.module.agentx.framework.openfang.client;

import cn.iocoder.yudao.module.agentx.framework.openfang.config.AgentxOpenfangProperties;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangApprovalDetailRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangHealthRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangTaskRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunReqDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunRespDTO;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenfangRuntimeBridgeProtectedTest {

    @Test
    void shouldFallbackAndOpenCircuit() {
        AtomicInteger counter = new AtomicInteger(0);
        OpenfangRuntimeBridge delegate = new FailingBridge(counter);
        AgentxOpenfangProperties properties = new AgentxOpenfangProperties();
        properties.setProtectionEnabled(true);
        properties.setFallbackEnabled(true);
        properties.setCircuitFailureThreshold(1);
        properties.setCircuitOpenSeconds(60);
        OpenfangRuntimeBridgeProtected bridge = new OpenfangRuntimeBridgeProtected(delegate, properties);

        OpenfangWorkflowRunRespDTO first = bridge.runWorkflow("wf-1", new OpenfangWorkflowRunReqDTO());
        OpenfangWorkflowRunRespDTO second = bridge.runWorkflow("wf-1", new OpenfangWorkflowRunReqDTO());

        assertTrue(first.getTaskRunId().startsWith(OpenfangRuntimeBridgeProtected.FALLBACK_TASK_RUN_PREFIX));
        assertTrue(second.getTaskRunId().startsWith(OpenfangRuntimeBridgeProtected.FALLBACK_TASK_RUN_PREFIX));
        assertEquals(1, counter.get());
    }

    @Test
    void shouldThrowWhenFallbackDisabled() {
        AtomicInteger counter = new AtomicInteger(0);
        OpenfangRuntimeBridge delegate = new FailingBridge(counter);
        AgentxOpenfangProperties properties = new AgentxOpenfangProperties();
        properties.setProtectionEnabled(true);
        properties.setFallbackEnabled(false);
        properties.setCircuitFailureThreshold(3);
        OpenfangRuntimeBridgeProtected bridge = new OpenfangRuntimeBridgeProtected(delegate, properties);

        assertThrows(IllegalStateException.class, () -> bridge.runWorkflow("wf-1", new OpenfangWorkflowRunReqDTO()));
        assertEquals(1, counter.get());
    }

    private static class FailingBridge implements OpenfangRuntimeBridge {

        private final AtomicInteger counter;

        private FailingBridge(AtomicInteger counter) {
            this.counter = counter;
        }

        @Override
        public OpenfangWorkflowRunRespDTO runWorkflow(String workflowId, OpenfangWorkflowRunReqDTO request) {
            counter.incrementAndGet();
            throw new IllegalStateException("mock failure");
        }

        @Override
        public OpenfangTaskRespDTO getTask(String taskRunId) {
            counter.incrementAndGet();
            throw new IllegalStateException("mock failure");
        }

        @Override
        public OpenfangApprovalDetailRespDTO getApprovalDetail(String taskRunId, String approvalId) {
            counter.incrementAndGet();
            throw new IllegalStateException("mock failure");
        }

        @Override
        public void approve(String approvalId, String decisionComment) {
            counter.incrementAndGet();
            throw new IllegalStateException("mock failure");
        }

        @Override
        public void reject(String approvalId, String decisionComment) {
            counter.incrementAndGet();
            throw new IllegalStateException("mock failure");
        }

        @Override
        public OpenfangHealthRespDTO health(String baseUrl, String accessToken) {
            counter.incrementAndGet();
            throw new IllegalStateException("mock failure");
        }
    }

}
