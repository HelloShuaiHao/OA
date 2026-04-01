package cn.iocoder.yudao.module.agentx.config;

import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridge;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridgeHttpClient;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridgeProtected;
import cn.iocoder.yudao.module.agentx.framework.openfang.config.AgentxOpenfangProperties;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalBridgeService;
import cn.iocoder.yudao.module.agentx.service.audit.AgentxAuditService;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxAuthorizationService;
import cn.iocoder.yudao.module.agentx.service.task.AgentxTaskLifecycleService;
import cn.iocoder.yudao.module.agentx.service.task.AgentxTaskOrchestrationService;
import cn.iocoder.yudao.module.agentx.service.tool.AgentxToolAdapter;
import cn.iocoder.yudao.module.agentx.service.tool.AgentxToolGuardService;
import cn.iocoder.yudao.module.agentx.service.tool.AgentxToolInvocationService;
import cn.iocoder.yudao.module.agentx.service.tool.BpmApproveToolAdapter;
import cn.iocoder.yudao.module.agentx.service.tool.BpmQueryTasksToolAdapter;
import cn.iocoder.yudao.module.agentx.service.workflow.AgentxWorkflowResolver;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.flowable.engine.TaskService;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({
        AgentxOpenfangProperties.class,
        AgentxBindProperties.class,
        AgentxEntitlementProperties.class,
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
    public AgentxAuthorizationService agentxAuthorizationService() {
        return new AgentxAuthorizationService();
    }

    @Bean
    public AgentxWorkflowResolver agentxWorkflowResolver() {
        return new AgentxWorkflowResolver();
    }

    @Bean
    public AgentxToolGuardService agentxToolGuardService(AgentxAuthorizationService authorizationService,
                                                         AgentxAuditService auditService) {
        return new AgentxToolGuardService(authorizationService, auditService);
    }

    @Bean
    public AgentxToolInvocationService agentxToolInvocationService(AgentxToolGuardService toolGuardService,
                                                                   AgentxAuditService auditService,
                                                                   List<AgentxToolAdapter> adapters) {
        return new AgentxToolInvocationService(toolGuardService, auditService, adapters);
    }

    @Bean
    public BpmQueryTasksToolAdapter bpmQueryTasksToolAdapter(TaskService taskService) {
        return new BpmQueryTasksToolAdapter(taskService);
    }

    @Bean
    public BpmApproveToolAdapter bpmApproveToolAdapter(BpmTaskService bpmTaskService) {
        return new BpmApproveToolAdapter(bpmTaskService);
    }

    @Bean
    public AgentxTaskOrchestrationService agentxTaskOrchestrationService(AgentxWorkflowResolver workflowResolver) {
        return new AgentxTaskOrchestrationService(workflowResolver);
    }

    @Bean
    public AgentxTaskLifecycleService agentxTaskLifecycleService(AgentxTaskOrchestrationService orchestrationService,
                                                                 AgentxApprovalBridgeService approvalBridgeService,
                                                                 OpenfangRuntimeBridge runtimeBridge,
                                                                 AgentxAuditService auditService,
                                                                 AgentxAuthorizationService authorizationService) {
        return new AgentxTaskLifecycleService(orchestrationService, approvalBridgeService, runtimeBridge,
                auditService, authorizationService);
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
