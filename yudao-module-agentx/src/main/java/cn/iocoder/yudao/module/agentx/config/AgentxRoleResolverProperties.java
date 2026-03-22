package cn.iocoder.yudao.module.agentx.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "yudao.agentx.role")
@Validated
@Data
public class AgentxRoleResolverProperties {

    @NotNull(message = "默认审批人 ID 不能为空")
    private Long defaultApproverId = 1L;

    @Min(value = 1, message = "角色缓存分钟数必须 >= 1")
    private Integer cacheMinutes = 5;
}
