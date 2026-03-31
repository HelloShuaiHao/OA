package cn.iocoder.yudao.module.agentx.controller.admin.process.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Schema(description = "管理后台 - AgentX AI 生成 BPMN Request VO")
@Data
public class AgentxBpmnGenerateReqVO {

    @Schema(description = "流程描述文本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "流程描述不能为空")
    @Size(min = 20, max = 2000, message = "流程描述长度需在 20 到 2000 字之间")
    private String description;

    @Schema(description = "流程名称（可选）")
    private String processName;

    @Schema(description = "流程标识 Key（可选）")
    private String processKey;

}
