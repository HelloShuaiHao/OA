package cn.iocoder.yudao.module.agentx.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - AgentX 数字员工状态更新 Request VO")
@Data
public class AgentxAgentUpdateStatusReqVO {

    @Schema(description = "状态（1=激活,2=停用）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

}
