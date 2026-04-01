package cn.iocoder.yudao.module.agentx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "yudao.agentx.entitlement")
@Validated
@Data
public class AgentxEntitlementProperties {

    @NotBlank(message = "AgentX Entitlement 签名密钥不能为空")
    private String signatureSecret = "change-me-in-prod";

    @NotBlank(message = "AgentX Entitlement 策略版本不能为空")
    private String policyVersion = "v2026.04.01";

    @Min(value = 1, message = "Envelope TTL 分钟必须 >= 1")
    private Integer envelopeTtlMinutes = 5;

}
