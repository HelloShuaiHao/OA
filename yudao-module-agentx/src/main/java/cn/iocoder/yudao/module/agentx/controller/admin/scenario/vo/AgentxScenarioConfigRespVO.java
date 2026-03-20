package cn.iocoder.yudao.module.agentx.controller.admin.scenario.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AgentX 场景配置 Response VO")
@Data
public class AgentxScenarioConfigRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "场景编码", example = "oa.leave.approval")
    private String scenarioCode;

    @Schema(description = "场景名称", example = "请假审批助手")
    private String scenarioName;

    @Schema(description = "OpenFang Workflow ID", example = "leave-approval-assistant")
    private String openfangWorkflowId;

    @Schema(description = "Workflow 版本", example = "1.0.0")
    private String workflowVersion;

    @Schema(description = "是否启用", example = "1")
    private Integer enabled;

    @Schema(description = "场景配置 JSON")
    private String config;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
