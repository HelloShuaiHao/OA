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

}
