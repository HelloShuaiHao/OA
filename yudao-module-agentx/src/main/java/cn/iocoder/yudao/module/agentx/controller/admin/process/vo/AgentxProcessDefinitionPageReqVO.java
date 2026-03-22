package cn.iocoder.yudao.module.agentx.controller.admin.process.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - AgentX 流程定义分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentxProcessDefinitionPageReqVO extends PageParam {

    @Schema(description = "流程名称", example = "请假审批流程")
    private String name;

    @Schema(description = "流程 Key", example = "oa_leave_approval")
    private String key;

    @Schema(description = "只看启用流程", example = "true")
    private Boolean activeOnly = true;

}
