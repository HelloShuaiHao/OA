package cn.iocoder.yudao.module.agentx.controller.admin.scenario.vo;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - AgentX 场景配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentxScenarioConfigPageReqVO extends PageParam {

    @Schema(description = "场景编码", example = "oa.leave.approval")
    private String scenarioCode;

    @Schema(description = "场景名称", example = "请假审批助手")
    private String scenarioName;

    @Schema(description = "OpenFang Workflow ID", example = "leave-approval-assistant")
    private String openfangWorkflowId;

    @Schema(description = "是否启用", example = "1")
    @InEnum(CommonStatusEnum.class)
    private Integer enabled;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
