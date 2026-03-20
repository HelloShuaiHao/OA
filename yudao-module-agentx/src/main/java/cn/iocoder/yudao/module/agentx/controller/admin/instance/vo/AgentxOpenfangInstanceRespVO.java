package cn.iocoder.yudao.module.agentx.controller.admin.instance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - OpenFang 实例 Response VO")
@Data
public class AgentxOpenfangInstanceRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "租户编号", example = "1")
    private Long tenantId;

    @Schema(description = "实例名称", example = "本地开发实例")
    private String instanceName;

    @Schema(description = "实例地址", example = "http://localhost:4201")
    private String endpoint;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "版本", example = "0.3.0")
    private String version;

    @Schema(description = "最后心跳时间")
    private LocalDateTime lastHeartbeat;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
