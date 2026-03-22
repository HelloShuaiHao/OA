package cn.iocoder.yudao.module.agentx.controller.admin.process.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AgentX 流程启动 Response VO")
@Data
public class AgentxProcessStartRespVO {

    @Schema(description = "流程实例 ID")
    private String processInstanceId;

}
