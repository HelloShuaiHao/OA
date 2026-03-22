package cn.iocoder.yudao.module.agentx.config;

import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridge;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridgeHttpClient;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridgeProtected;
import cn.iocoder.yudao.module.agentx.framework.openfang.config.AgentxOpenfangProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({
        AgentxOpenfangProperties.class,
        AgentxBindProperties.class,
        AgentxRoleResolverProperties.class,
        AgentxMqProperties.class
})
public class AgentxConfiguration {

    @Bean
    public OpenfangRuntimeBridge openfangRuntimeBridge(RestTemplate restTemplate,
                                                       AgentxOpenfangProperties properties) {
        OpenfangRuntimeBridge delegate = new OpenfangRuntimeBridgeHttpClient(restTemplate, properties);
        return new OpenfangRuntimeBridgeProtected(delegate, properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "yudao.agentx.mq", name = "enabled", havingValue = "true")
    public TopicExchange agentxTopicExchange(AgentxMqProperties properties) {
        return new TopicExchange(properties.getExchange(), true, false);
    }

    @Bean
    @ConditionalOnProperty(prefix = "yudao.agentx.mq", name = "enabled", havingValue = "true")
    public Queue agentxEventQueue(AgentxMqProperties properties) {
        return new Queue(properties.getQueue(), true);
    }

    @Bean
    @ConditionalOnProperty(prefix = "yudao.agentx.mq", name = "enabled", havingValue = "true")
    public Binding agentxEventBinding(Queue agentxEventQueue,
                                      TopicExchange agentxTopicExchange,
                                      AgentxMqProperties properties) {
        return BindingBuilder.bind(agentxEventQueue).to(agentxTopicExchange).with(properties.getRoutingKey());
    }

    @Bean(name = "agentxMqPublishExecutor")
    @ConditionalOnProperty(prefix = "yudao.agentx.mq", name = "enabled", havingValue = "true")
    public ThreadPoolTaskExecutor agentxMqPublishExecutor(AgentxMqProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("agentx-mq-pub-");
        executor.setCorePoolSize(Math.max(1, properties.getPublishCorePoolSize()));
        executor.setMaxPoolSize(Math.max(executor.getCorePoolSize(), properties.getPublishMaxPoolSize()));
        executor.setQueueCapacity(Math.max(0, properties.getPublishQueueCapacity()));
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean(name = "agentxContextExecutor")
    public ThreadPoolTaskExecutor agentxContextExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("agentx-ctx-");
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
