package cn.iocoder.yudao.module.agentx.controller.admin.access.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "管理后台 - AgentX 访问评估 Request VO")
public class AccessEvaluateReqVO {

    @NotBlank(message = "channelUserId 不能为空")
    @Schema(description = "渠道用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "telegram:987654321")
    private String channelUserId;

    @Schema(description = "Agent ID（新协议，优先）", example = "dispatch-assistant")
    private String agentId;

    @Schema(description = "Agent Key（旧协议兼容）", example = "dispatch-assistant")
    private String agentKey;

    @Schema(description = "渠道类型（旧协议兼容）", example = "telegram")
    private String channelType;

    @Schema(description = "渠道用户名（旧协议兼容）")
    private String channelUsername;

    @Schema(description = "会话范围", example = "session_abc123")
    private String conversationScope;

}
