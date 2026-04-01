package cn.iocoder.yudao.module.agentx.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "管理后台 - 渠道测试连接 Request VO")
@Data
public class AgentxChannelTestReqVO {

    @Schema(description = "渠道配置 ID（可选）")
    private Long channelId;

    @Schema(description = "渠道类型（telegram/whatsapp/wecom/dingtalk）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "渠道类型不能为空")
    private String channelType;

    @Schema(description = "Bot Token（可选，为空时优先使用已保存配置）")
    private String botToken;

}
