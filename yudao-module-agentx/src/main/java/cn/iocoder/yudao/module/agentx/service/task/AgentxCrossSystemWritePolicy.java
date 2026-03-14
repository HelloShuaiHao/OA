package cn.iocoder.yudao.module.agentx.service.task;

public class AgentxCrossSystemWritePolicy {

    private final int maxAttempts;

    public AgentxCrossSystemWritePolicy(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public boolean shouldRetry(Throwable throwable, int attempt) {
        return throwable instanceof AgentxTransientWriteException && attempt < maxAttempts;
    }

}
