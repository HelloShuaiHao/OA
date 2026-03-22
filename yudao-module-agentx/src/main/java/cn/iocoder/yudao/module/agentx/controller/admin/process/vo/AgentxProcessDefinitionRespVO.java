package cn.iocoder.yudao.module.agentx.controller.admin.process.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AgentX 流程定义 Response VO")
@Data
public class AgentxProcessDefinitionRespVO {

    @Schema(description = "流程定义 ID", example = "leave:2:0f1f3d8e")
    private String id;

    @Schema(description = "流程 Key", example = "oa_leave_approval")
    private String key;

    @Schema(description = "流程名称", example = "请假审批流程")
    private String name;

    @Schema(description = "分类编码", example = "oa")
    private String category;

    @Schema(description = "分类名称", example = "行政审批")
    private String categoryName;

    @Schema(description = "版本", example = "2")
    private Integer version;

}
