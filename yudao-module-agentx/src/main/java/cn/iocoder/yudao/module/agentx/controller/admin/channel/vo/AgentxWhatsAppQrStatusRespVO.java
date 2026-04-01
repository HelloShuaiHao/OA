package cn.iocoder.yudao.module.agentx.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - WhatsApp 扫码状态 Response VO")
@Data
public class AgentxWhatsAppQrStatusRespVO {

    @Schema(description = "是否已连接")
    private Boolean connected;

    @Schema(description = "是否过期")
    private Boolean expired;

    @Schema(description = "状态消息")
    private String message;

    @Schema(description = "最新二维码 Data URL")
    private String qrDataUrl;

    @Schema(description = "会话 ID")
    private String sessionId;
}
