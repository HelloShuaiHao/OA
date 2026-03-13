package cn.iocoder.yudao.module.agentx.service.task;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxCrossSystemWriteExecutorTest {

    @Test
    void shouldRetryTransientWriteOnceAndDeduplicateByIdempotencyKey() {
        AgentxCrossSystemWriteExecutor executor = new AgentxCrossSystemWriteExecutor(new AgentxCrossSystemWritePolicy(2));
        AtomicInteger attempts = new AtomicInteger();

        String first = executor.execute("approve:approval-1", () -> {
            if (attempts.getAndIncrement() == 0) {
                throw new AgentxTransientWriteException("network jitter");
            }
            return "ok";
        });
        String second = executor.execute("approve:approval-1", () -> "duplicate");

        assertEquals("ok", first);
        assertEquals("ok", second);
        assertEquals(2, attempts.get());
    }

}
