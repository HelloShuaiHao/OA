package cn.iocoder.yudao.module.agentx.service.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxContextLifecyclePolicyTest {

    @Test
    void shouldCacheSnapshotUntilInvalidatedOrExpired() {
        AgentxContextLifecyclePolicy policy = new AgentxContextLifecyclePolicy(300);

        assertTrue(policy.shouldReuseSnapshot(100, 200, false));
        assertFalse(policy.shouldReuseSnapshot(100, 401, false));
        assertFalse(policy.shouldReuseSnapshot(100, 200, true));
    }

}
