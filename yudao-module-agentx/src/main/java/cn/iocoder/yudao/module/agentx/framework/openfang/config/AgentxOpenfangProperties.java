package cn.iocoder.yudao.module.agentx.framework.openfang.config;

import cn.iocoder.yudao.module.agentx.enums.AgentxApprovalDetailFetchModeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenFang 集成配置。
 */
@ConfigurationProperties(prefix = "yudao.agentx.openfang")
@Data
public class AgentxOpenfangProperties {

    /**
     * OpenFang 服务地址。
     */
    private String baseUrl;
    /**
     * 访问令牌。
     */
    private String accessToken;
    /**
     * 审批详情读取模式。
     */
    private AgentxApprovalDetailFetchModeEnum approvalDetailFetchMode = AgentxApprovalDetailFetchModeEnum.EXACT_QUERY;
    /**
     * 是否启用熔断保护。
     */
    private Boolean protectionEnabled = true;
    /**
     * 熔断失败阈值（连续失败次数）。
     */
    private Integer circuitFailureThreshold = 3;
    /**
     * 熔断打开时长（秒）。
     */
    private Integer circuitOpenSeconds = 60;
    /**
     * 是否启用降级返回。
     */
    private Boolean fallbackEnabled = true;

}
