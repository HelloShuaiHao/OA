package cn.iocoder.yudao.module.agentx.config;

import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridge;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridgeHttpClient;
import cn.iocoder.yudao.module.agentx.framework.openfang.config.AgentxOpenfangProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AgentxOpenfangProperties.class)
public class AgentxConfiguration {

    @Bean
    public OpenfangRuntimeBridge openfangRuntimeBridge(RestTemplate restTemplate,
                                                       AgentxOpenfangProperties properties) {
        return new OpenfangRuntimeBridgeHttpClient(restTemplate, properties);
    }
}
