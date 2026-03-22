package cn.iocoder.yudao.module.agentx.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 生成绑定链接 Response VO")
@Data
public class AgentxBindGenerateRespVO {

    @Schema(description = "绑定 URL")
    private String bindUrl;

    @Schema(description = "绑定 Token")
    private String token;

}
