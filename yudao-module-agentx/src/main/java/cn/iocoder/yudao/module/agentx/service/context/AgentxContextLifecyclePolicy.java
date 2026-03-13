package cn.iocoder.yudao.module.agentx.service.context;

public class AgentxContextLifecyclePolicy {

    private final long ttlSeconds;

    public AgentxContextLifecyclePolicy(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public boolean shouldReuseSnapshot(long snapshotTimestampSeconds, long nowTimestampSeconds, boolean invalidated) {
        return !invalidated && nowTimestampSeconds - snapshotTimestampSeconds <= ttlSeconds;
    }

}
