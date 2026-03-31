package cn.iocoder.yudao.module.agentx.controller.admin.process.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AgentX AI 生成 BPMN Response VO")
@Data
public class AgentxBpmnGenerateRespVO {

    @Schema(description = "流程名称")
    private String processName;

    @Schema(description = "流程 Key")
    private String processKey;

    @Schema(description = "BPMN XML")
    private String bpmnXml;

    @Schema(description = "是否有效")
    private Boolean valid;

    @Schema(description = "提示消息")
    private String message;

}
