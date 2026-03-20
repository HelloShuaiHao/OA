package cn.iocoder.yudao.module.agentx.controller.admin.instance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - OpenFang 实例测试连接 Response VO")
@Data
public class AgentxOpenfangInstanceTestRespVO {

    @Schema(description = "是否在线", example = "true")
    private Boolean online;

    @Schema(description = "版本", example = "0.3.0")
    private String version;

    @Schema(description = "诊断信息")
    private String message;

    @Schema(description = "检测时间")
    private LocalDateTime checkedAt;

}
