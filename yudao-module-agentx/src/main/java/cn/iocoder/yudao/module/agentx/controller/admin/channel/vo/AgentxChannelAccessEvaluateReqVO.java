package cn.iocoder.yudao.module.agentx.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "管理后台 - 渠道运行时访问评估 Request VO")
@Data
public class AgentxChannelAccessEvaluateReqVO {

    @Schema(description = "渠道类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "渠道类型不能为空")
    private String channelType;

    @Schema(description = "渠道用户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "渠道用户 ID 不能为空")
    private String channelUserId;

    @Schema(description = "渠道用户名")
    private String channelUsername;

    @Schema(description = "Agent ID")
    private Long agentId;

    @Schema(description = "Agent Key")
    private String agentKey;

}
