package cn.iocoder.yudao.module.agentx.controller.admin.process.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Schema(description = "管理后台 - AgentX BPMN 预览验证 Request VO")
@Data
public class AgentxBpmnPreviewReqVO {

    @Schema(description = "BPMN XML", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "BPMN XML 不能为空")
    private String bpmnXml;

}
