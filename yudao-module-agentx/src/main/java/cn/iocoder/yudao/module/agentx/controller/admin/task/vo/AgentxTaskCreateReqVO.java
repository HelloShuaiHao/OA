package cn.iocoder.yudao.module.agentx.controller.admin.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "管理后台 - AgentX 任务创建 Request VO")
@Data
public class AgentxTaskCreateReqVO {

    @Schema(description = "场景编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "oa.leave.approval")
    @NotBlank(message = "场景编码不能为空")
    private String scenarioCode;

    @Schema(description = "业务主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "leave-10")
    @NotBlank(message = "业务主键不能为空")
    private String businessKey;

    @Schema(description = "幂等键", example = "leave-10-20260318")
    private String idempotencyKey;

    @Schema(description = "Workflow 版本", example = "1.0.0")
    private String workflowVersion;

}
