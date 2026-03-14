package cn.iocoder.yudao.module.agentx.service.task;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class AgentxCrossSystemWriteExecutor {

    private final AgentxCrossSystemWritePolicy policy;
    private final Map<String, Object> results = new ConcurrentHashMap<>();

    public AgentxCrossSystemWriteExecutor(AgentxCrossSystemWritePolicy policy) {
        this.policy = policy;
    }

    @SuppressWarnings("unchecked")
    public <T> T execute(String idempotencyKey, Callable<T> action) {
        Object cached = results.get(idempotencyKey);
        if (cached != null) {
            return (T) cached;
        }
        int attempt = 1;
        while (true) {
            try {
                T result = action.call();
                results.putIfAbsent(idempotencyKey, result);
                return result;
            } catch (Exception ex) {
                if (!policy.shouldRetry(ex, attempt)) {
                    throw ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex);
                }
                attempt++;
            }
        }
    }

}
