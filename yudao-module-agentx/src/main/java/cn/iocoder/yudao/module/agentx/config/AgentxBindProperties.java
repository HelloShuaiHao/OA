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

    /**
     * 绑定页地址前缀（可选）。
     *
     * 未配置时，优先回落到 yudao.web.admin-ui.url，再回落到默认 localhost 地址。
     */
    private String baseUrl;

    @Min(value = 1, message = "绑定 Token 过期分钟必须 >= 1")
    private Integer expireMinutes = 10;

}
