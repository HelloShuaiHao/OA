package cn.iocoder.yudao.module.agentx.framework.openfang.client;

import cn.iocoder.yudao.module.agentx.framework.openfang.config.AgentxOpenfangProperties;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangApprovalDetailRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangHealthRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangTaskRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunReqDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunRespDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * OpenFang Runtime 熔断降级包装器。
 */
@Slf4j
public class OpenfangRuntimeBridgeProtected implements OpenfangRuntimeBridge {

    public static final String FALLBACK_TASK_RUN_PREFIX = "flowable-fallback-";

    private final OpenfangRuntimeBridge delegate;
    private final AgentxOpenfangProperties properties;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private volatile long circuitOpenUntilEpochMs = 0L;

    public OpenfangRuntimeBridgeProtected(OpenfangRuntimeBridge delegate, AgentxOpenfangProperties properties) {
        this.delegate = delegate;
        this.properties = properties;
    }

    @Override
    public OpenfangWorkflowRunRespDTO runWorkflow(String workflowId, OpenfangWorkflowRunReqDTO request) {
        return execute("runWorkflow", () -> delegate.runWorkflow(workflowId, request), () -> fallbackRunWorkflow(workflowId));
    }

    @Override
    public OpenfangTaskRespDTO getTask(String taskRunId) {
        if (taskRunId != null && taskRunId.startsWith(FALLBACK_TASK_RUN_PREFIX)) {
            return fallbackTaskRun(taskRunId);
        }
        return execute("getTask", () -> delegate.getTask(taskRunId), () -> fallbackTaskRun(taskRunId));
    }

    @Override
    public OpenfangApprovalDetailRespDTO getApprovalDetail(String taskRunId, String approvalId) {
        return execute("getApprovalDetail", () -> delegate.getApprovalDetail(taskRunId, approvalId),
                () -> fallbackApprovalDetail(taskRunId, approvalId));
    }

    @Override
    public void approve(String approvalId, String decisionComment) {
        execute("approve", () -> {
            delegate.approve(approvalId, decisionComment);
            return null;
        }, () -> null);
    }

    @Override
    public void reject(String approvalId, String decisionComment) {
        execute("reject", () -> {
            delegate.reject(approvalId, decisionComment);
            return null;
        }, () -> null);
    }

    @Override
    public OpenfangHealthRespDTO health(String baseUrl, String accessToken) {
        return execute("health", () -> delegate.health(baseUrl, accessToken),
                () -> new OpenfangHealthRespDTO().setOnline(false).setMessage("Circuit breaker opened"));
    }

    private <T> T execute(String action, Supplier<T> supplier, Supplier<T> fallbackSupplier) {
        if (!Boolean.TRUE.equals(properties.getProtectionEnabled())) {
            return supplier.get();
        }
        if (isCircuitOpen()) {
            return onFallback(action, new IllegalStateException("OpenFang circuit breaker is open"), fallbackSupplier);
        }
        try {
            T result = supplier.get();
            consecutiveFailures.set(0);
            return result;
        } catch (Exception ex) {
            int failures = consecutiveFailures.incrementAndGet();
            int threshold = properties.getCircuitFailureThreshold() == null
                    ? 3
                    : Math.max(1, properties.getCircuitFailureThreshold());
            if (failures >= threshold) {
                int seconds = properties.getCircuitOpenSeconds() == null
                        ? 60
                        : Math.max(1, properties.getCircuitOpenSeconds());
                circuitOpenUntilEpochMs = System.currentTimeMillis() + seconds * 1000L;
                log.warn("[OpenfangRuntimeBridgeProtected] circuit opened for {}s after action={}", seconds, action, ex);
            }
            return onFallback(action, ex, fallbackSupplier);
        }
    }

    private <T> T onFallback(String action, Exception ex, Supplier<T> fallbackSupplier) {
        if (!Boolean.TRUE.equals(properties.getFallbackEnabled()) || fallbackSupplier == null) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new IllegalStateException(ex.getMessage(), ex);
        }
        log.warn("[OpenfangRuntimeBridgeProtected] fallback activated, action={}", action, ex);
        return fallbackSupplier.get();
    }

    private boolean isCircuitOpen() {
        long now = System.currentTimeMillis();
        if (circuitOpenUntilEpochMs <= now) {
            if (circuitOpenUntilEpochMs > 0L) {
                circuitOpenUntilEpochMs = 0L;
                consecutiveFailures.set(0);
            }
            return false;
        }
        return true;
    }

    private OpenfangWorkflowRunRespDTO fallbackRunWorkflow(String workflowId) {
        OpenfangWorkflowRunRespDTO resp = new OpenfangWorkflowRunRespDTO();
        resp.setTaskRunId(FALLBACK_TASK_RUN_PREFIX + UUID.randomUUID());
        resp.setWorkflowId(workflowId);
        resp.setTaskStatus("FALLBACK_TO_FLOWABLE");
        return resp;
    }

    private OpenfangTaskRespDTO fallbackTaskRun(String taskRunId) {
        OpenfangTaskRespDTO task = new OpenfangTaskRespDTO();
        task.setTaskRunId(taskRunId);
        task.setStatus("FAILED");
        task.setFailureSummary("OpenFang unavailable, fallback mode");
        task.setAuditSummary("fallback");
        return task;
    }

    private OpenfangApprovalDetailRespDTO fallbackApprovalDetail(String taskRunId, String approvalId) {
        OpenfangApprovalDetailRespDTO detail = new OpenfangApprovalDetailRespDTO();
        detail.setTaskRunId(taskRunId);
        detail.setApprovalId(approvalId);
        detail.setTitle("Fallback Approval");
        detail.setReason("OpenFang unavailable");
        detail.setRiskLevel(50);
        detail.setActionSummary("fallback");
        return detail;
    }

}
