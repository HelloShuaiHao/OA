package cn.iocoder.yudao.module.agentx.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 渠道测试连接 Response VO")
@Data
public class AgentxChannelTestRespVO {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "结果消息")
    private String message;

}
