package cn.iocoder.yudao.module.agentx.service.mq;

import cn.iocoder.yudao.module.agentx.config.AgentxMqProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

/**
 * AgentX 事件发布器。
 */
@Service
@ConditionalOnProperty(prefix = "yudao.agentx.mq", name = "enabled", havingValue = "true")
@Slf4j
public class AgentxEventPublisher {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private AgentxMqProperties mqProperties;
    @Autowired(required = false)
    @Qualifier("agentxMqPublishExecutor")
    private Executor publishExecutor;

    public void publish(String routingKey, AgentxMqEvent event) {
        if (Boolean.TRUE.equals(mqProperties.getAsyncPublish()) && publishExecutor != null) {
            try {
                publishExecutor.execute(() -> doPublish(routingKey, event));
                return;
            } catch (RuntimeException ex) {
                // 线程池拒绝时降级为同步发布，保证事件不丢。
                log.warn("[AgentxEventPublisher] async publish rejected, fallback to sync");
            }
        }
        doPublish(routingKey, event);
    }

    private void doPublish(String routingKey, AgentxMqEvent event) {
        String finalRoutingKey = routingKey == null ? "agentx.event.default" : routingKey;
        int retries = mqProperties.getPublishMaxRetries() == null ? 0 : Math.max(0, mqProperties.getPublishMaxRetries());
        long backoff = mqProperties.getPublishRetryBackoffMillis() == null
                ? 0L
                : Math.max(0L, mqProperties.getPublishRetryBackoffMillis());
        for (int attempt = 0; attempt <= retries; attempt++) {
            try {
                rabbitTemplate.convertAndSend(mqProperties.getExchange(), finalRoutingKey, event);
                return;
            } catch (Exception ex) {
                if (attempt >= retries) {
                    // 发布失败时只记录日志，避免影响主流程
                    log.warn("[AgentxEventPublisher] publish failed after retries, routingKey={}", finalRoutingKey, ex);
                    return;
                }
                if (backoff > 0) {
                    sleep(backoff);
                }
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

}
