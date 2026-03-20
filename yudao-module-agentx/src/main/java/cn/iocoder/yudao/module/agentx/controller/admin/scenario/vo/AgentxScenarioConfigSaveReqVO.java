package cn.iocoder.yudao.module.agentx.controller.admin.scenario.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - AgentX 场景配置新增/修改 Request VO")
@Data
public class AgentxScenarioConfigSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "场景编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "oa.leave.approval")
    @NotBlank(message = "场景编码不能为空")
    private String scenarioCode;

    @Schema(description = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "请假审批助手")
    @NotBlank(message = "场景名称不能为空")
    private String scenarioName;

    @Schema(description = "OpenFang Workflow ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "leave-approval-assistant")
    @NotBlank(message = "OpenFang Workflow ID 不能为空")
    private String openfangWorkflowId;

    @Schema(description = "Workflow 版本", example = "1.0.0")
    private String workflowVersion;

    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "启用状态不能为空")
    private Integer enabled;

    @Schema(description = "场景配置 JSON")
    private String config;

}
