package cn.iocoder.yudao.module.agentx.controller.admin.process.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Schema(description = "管理后台 - AgentX 流程选择 Request VO")
@Data
public class AgentxProcessSelectionReqVO {

    @Schema(description = "Agent ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "agentId 不能为空")
    private Long agentId;

    @Schema(description = "流程选择模式（rule/auto）")
    private String selectionMode;

    @Schema(description = "流程上下文")
    private Map<String, Object> context;

}
