package cn.iocoder.yudao.module.agentx.controller.admin.process.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AgentX BPMN 预览验证 Response VO")
@Data
public class AgentxBpmnPreviewRespVO {

    @Schema(description = "是否有效")
    private Boolean valid;

    @Schema(description = "提示消息")
    private String message;

    @Schema(description = "开始节点数量")
    private Integer startEventCount;

    @Schema(description = "结束节点数量")
    private Integer endEventCount;

    @Schema(description = "用户任务节点数量")
    private Integer userTaskCount;

    @Schema(description = "网关节点数量")
    private Integer gatewayCount;

}
