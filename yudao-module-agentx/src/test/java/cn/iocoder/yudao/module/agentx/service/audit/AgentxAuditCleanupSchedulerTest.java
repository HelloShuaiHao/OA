package cn.iocoder.yudao.module.agentx.service.audit;

import cn.iocoder.yudao.module.agentx.dal.mysql.audit.AgentxAuditEventMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AgentxAuditCleanupSchedulerTest {

    @Test
    void shouldDeleteExpiredAuditEvents() {
        AtomicReference<LocalDateTime> deadlineSink = new AtomicReference<>();
        AgentxAuditEventMapper mapper = (AgentxAuditEventMapper) Proxy.newProxyInstance(
                AgentxAuditEventMapper.class.getClassLoader(),
                new Class<?>[] { AgentxAuditEventMapper.class },
                (proxy, method, args) -> {
                    if ("deleteByCreateTimeBefore".equals(method.getName())) {
                        deadlineSink.set((LocalDateTime) args[0]);
                        return 3;
                    }
                    if ("insert".equals(method.getName())) {
                        return 1;
                    }
                    if (method.getReturnType().equals(boolean.class)) {
                        return false;
                    }
                    if (method.getReturnType().equals(int.class) || method.getReturnType().equals(long.class)) {
                        return 0;
                    }
                    return null;
                });

        AgentxAuditCleanupScheduler scheduler = new AgentxAuditCleanupScheduler();
        ReflectionTestUtils.setField(scheduler, "auditEventMapper", mapper);

        scheduler.cleanupExpiredEvents();

        assertNotNull(deadlineSink.get());
    }

}
