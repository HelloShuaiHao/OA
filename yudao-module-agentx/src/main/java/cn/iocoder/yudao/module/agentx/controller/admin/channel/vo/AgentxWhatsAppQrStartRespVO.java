package cn.iocoder.yudao.module.agentx.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - WhatsApp 扫码开始 Response VO")
@Data
public class AgentxWhatsAppQrStartRespVO {

    @Schema(description = "是否可用")
    private Boolean available;

    @Schema(description = "二维码 Data URL")
    private String qrDataUrl;

    @Schema(description = "会话 ID")
    private String sessionId;

    @Schema(description = "状态消息")
    private String message;

    @Schema(description = "帮助说明")
    private String help;

    @Schema(description = "是否已连接")
    private Boolean connected;
}
