package cn.iocoder.yudao.module.agentx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AgentX MQ 配置。
 */
@Data
@ConfigurationProperties(prefix = "yudao.agentx.mq")
public class AgentxMqProperties {

    /**
     * 是否启用 RabbitMQ 异步事件。
     */
    private Boolean enabled = false;
    /**
     * Topic Exchange 名称。
     */
    private String exchange = "agentx.events";
    /**
     * Queue 名称。
     */
    private String queue = "agentx.events.queue";
    /**
     * 路由键。
     */
    private String routingKey = "agentx.event.#";
    /**
     * 发布失败重试次数（总尝试次数 = 1 + publishMaxRetries）。
     */
    private Integer publishMaxRetries = 2;
    /**
     * 发布重试退避时长（毫秒）。
     */
    private Long publishRetryBackoffMillis = 200L;
    /**
     * 消费处理失败重试次数（总尝试次数 = 1 + consumerMaxRetries）。
     */
    private Integer consumerMaxRetries = 2;
    /**
     * 是否异步发布事件，避免阻塞主业务链路。
     */
    private Boolean asyncPublish = true;
    /**
     * 发布线程池核心线程数。
     */
    private Integer publishCorePoolSize = 2;
    /**
     * 发布线程池最大线程数。
     */
    private Integer publishMaxPoolSize = 4;
    /**
     * 发布线程池队列容量。
     */
    private Integer publishQueueCapacity = 200;

}
