package cn.iocoder.yudao.module.agentx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "yudao.agentx.bind")
@Validated
@Data
public class AgentxBindProperties {

    @NotBlank(message = "AgentX 绑定签名密钥不能为空")
    private String secret = "agentx-bind-secret";

    @NotBlank(message = "AgentX 绑定基础地址不能为空")
    private String baseUrl = "http://localhost:48080";

    @Min(value = 1, message = "绑定 Token 过期分钟必须 >= 1")
    private Integer expireMinutes = 10;

}
