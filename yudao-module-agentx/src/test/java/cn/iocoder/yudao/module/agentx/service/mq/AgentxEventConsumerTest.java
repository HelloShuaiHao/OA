package cn.iocoder.yudao.module.agentx.service.mq;

import cn.iocoder.yudao.module.agentx.config.AgentxMqProperties;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxEventConsumerTest {

    @Test
    void shouldRetryUntilSuccess() {
        TestableConsumer consumer = new TestableConsumer();
        AgentxMqProperties properties = new AgentxMqProperties();
        properties.setConsumerMaxRetries(2);
        ReflectionTestUtils.setField(consumer, "mqProperties", properties);
        consumer.failTimes = 2;

        consumer.onMessage(new AgentxMqEvent().setEventType("APPROVAL_BINDING_CREATED"));

        assertEquals(3, consumer.attempts);
    }

    @Test
    void shouldStopAfterMaxRetries() {
        TestableConsumer consumer = new TestableConsumer();
        AgentxMqProperties properties = new AgentxMqProperties();
        properties.setConsumerMaxRetries(1);
        ReflectionTestUtils.setField(consumer, "mqProperties", properties);
        consumer.failTimes = 10;

        consumer.onMessage(new AgentxMqEvent().setEventType("APPROVAL_BINDING_CREATED"));

        assertEquals(2, consumer.attempts);
    }

    private static class TestableConsumer extends AgentxEventConsumer {

        private int attempts = 0;
        private int failTimes = 0;

        @Override
        protected void handleEvent(AgentxMqEvent event) {
            attempts++;
            if (attempts <= failTimes) {
                throw new RuntimeException("mock consume failure");
            }
        }
    }

}
