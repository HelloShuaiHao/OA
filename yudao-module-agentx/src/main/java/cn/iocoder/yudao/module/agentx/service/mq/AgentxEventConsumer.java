package cn.iocoder.yudao.module.agentx.service.mq;

import cn.iocoder.yudao.module.agentx.config.AgentxMqProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * AgentX 事件消费者（骨架实现）。
 */
@Component
@ConditionalOnProperty(prefix = "yudao.agentx.mq", name = "enabled", havingValue = "true")
@RabbitListener(queues = "${yudao.agentx.mq.queue}")
@Slf4j
public class AgentxEventConsumer {

    @Resource
    private AgentxMqProperties mqProperties;

    @RabbitHandler
    public void onMessage(AgentxMqEvent event) {
        int retries = mqProperties.getConsumerMaxRetries() == null ? 0 : Math.max(0, mqProperties.getConsumerMaxRetries());
        for (int attempt = 0; attempt <= retries; attempt++) {
            try {
                handleEvent(event);
                return;
            } catch (Exception ex) {
                if (attempt >= retries) {
                    log.error("[AgentxEventConsumer] consume failed after retries, eventType={}",
                            event != null ? event.getEventType() : null, ex);
                    return;
                }
                log.warn("[AgentxEventConsumer] consume failed, attempt={}/{}, eventType={}",
                        attempt + 1, retries + 1, event != null ? event.getEventType() : null, ex);
            }
        }
    }

    protected void handleEvent(AgentxMqEvent event) {
        // 当前阶段先打通收消息链路；后续按事件类型分发到具体 handler
        log.info("[AgentxEventConsumer] receive eventType={}, businessKey={}",
                event != null ? event.getEventType() : null,
                event != null ? event.getBusinessKey() : null);
    }

}
