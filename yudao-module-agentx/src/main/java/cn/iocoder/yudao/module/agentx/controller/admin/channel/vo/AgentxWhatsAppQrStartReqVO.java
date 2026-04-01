package cn.iocoder.yudao.module.agentx.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - WhatsApp 二维码开始请求 VO")
@Data
public class AgentxWhatsAppQrStartReqVO {

    @Schema(description = "渠道配置 ID，用于自动生成稳定 bindingId", example = "12")
    private Long channelId;

    @Schema(description = "可选绑定标识；不传则按 channelId 自动生成", example = "agentx-channel-12")
    private String bindingId;

}
