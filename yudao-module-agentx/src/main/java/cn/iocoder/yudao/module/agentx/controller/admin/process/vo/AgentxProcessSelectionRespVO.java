package cn.iocoder.yudao.module.agentx.controller.admin.process.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AgentX 流程选择 Response VO")
@Data
public class AgentxProcessSelectionRespVO {

    @Schema(description = "流程定义 ID")
    private String processDefinitionId;

    @Schema(description = "流程定义 Key")
    private String processDefinitionKey;

    @Schema(description = "流程名称")
    private String processName;

    @Schema(description = "命中原因")
    private String reason;

}
