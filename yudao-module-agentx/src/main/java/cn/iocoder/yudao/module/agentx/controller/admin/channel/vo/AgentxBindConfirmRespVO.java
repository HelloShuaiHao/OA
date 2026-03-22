package cn.iocoder.yudao.module.agentx.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 绑定确认 Response VO")
@Data
public class AgentxBindConfirmRespVO {

    @Schema(description = "是否已绑定")
    private Boolean bound;

    @Schema(description = "提示信息")
    private String message;

}
