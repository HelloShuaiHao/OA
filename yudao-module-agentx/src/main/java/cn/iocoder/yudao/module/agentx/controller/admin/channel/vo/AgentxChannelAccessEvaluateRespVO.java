package cn.iocoder.yudao.module.agentx.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 渠道运行时访问评估 Response VO")
@Data
public class AgentxChannelAccessEvaluateRespVO {

    @Schema(description = "是否允许继续触发能力")
    private Boolean accessGranted;

    @Schema(description = "是否要求绑定")
    private Boolean authRequired;

    @Schema(description = "当前渠道用户是否已绑定")
    private Boolean bound;

    @Schema(description = "Agent 认证模式（public/bind_required）")
    private String authMode;

    @Schema(description = "渠道访问控制类型（all/dept/user）")
    private String accessControlType;

    @Schema(description = "绑定链接")
    private String bindUrl;

    @Schema(description = "绑定 Token")
    private String bindToken;

    @Schema(description = "提示消息")
    private String message;

}
