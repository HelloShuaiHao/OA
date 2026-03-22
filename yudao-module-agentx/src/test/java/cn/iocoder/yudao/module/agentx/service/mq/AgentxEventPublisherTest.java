package cn.iocoder.yudao.module.agentx.service.mq;

import cn.iocoder.yudao.module.agentx.config.AgentxMqProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class AgentxEventPublisherTest {

    @Test
    void shouldRetryAndSucceed() {
        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        Mockito.doThrow(new RuntimeException("1"))
                .doThrow(new RuntimeException("2"))
                .doNothing()
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), Mockito.any(Object.class));
        AgentxEventPublisher publisher = new AgentxEventPublisher();
        AgentxMqProperties properties = new AgentxMqProperties();
        properties.setExchange("agentx.events");
        properties.setPublishMaxRetries(2);
        properties.setPublishRetryBackoffMillis(0L);
        ReflectionTestUtils.setField(publisher, "rabbitTemplate", rabbitTemplate);
        ReflectionTestUtils.setField(publisher, "mqProperties", properties);

        publisher.publish("agentx.event.task.created", new AgentxMqEvent().setEventType("TASK_CREATED"));

        verify(rabbitTemplate, times(3)).convertAndSend(anyString(), anyString(), Mockito.any(Object.class));
    }

    @Test
    void shouldStopAfterMaxRetries() {
        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        Mockito.doThrow(new RuntimeException("always"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), Mockito.any(Object.class));
        AgentxEventPublisher publisher = new AgentxEventPublisher();
        AgentxMqProperties properties = new AgentxMqProperties();
        properties.setExchange("agentx.events");
        properties.setPublishMaxRetries(1);
        properties.setPublishRetryBackoffMillis(0L);
        ReflectionTestUtils.setField(publisher, "rabbitTemplate", rabbitTemplate);
        ReflectionTestUtils.setField(publisher, "mqProperties", properties);

        publisher.publish("agentx.event.task.created", new AgentxMqEvent().setEventType("TASK_CREATED"));

        verify(rabbitTemplate, times(2)).convertAndSend(anyString(), anyString(), Mockito.any(Object.class));
    }

    @Test
    void shouldPublishAsyncWhenExecutorProvided() {
        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        AgentxEventPublisher publisher = new AgentxEventPublisher();
        AgentxMqProperties properties = new AgentxMqProperties();
        properties.setExchange("agentx.events");
        properties.setAsyncPublish(true);
        ReflectionTestUtils.setField(publisher, "rabbitTemplate", rabbitTemplate);
        ReflectionTestUtils.setField(publisher, "mqProperties", properties);
        ReflectionTestUtils.setField(publisher, "publishExecutor", (java.util.concurrent.Executor) Runnable::run);

        publisher.publish("agentx.event.task.created", new AgentxMqEvent().setEventType("TASK_CREATED"));

        verify(rabbitTemplate, timeout(100).times(1))
                .convertAndSend(anyString(), anyString(), Mockito.any(Object.class));
    }

}
