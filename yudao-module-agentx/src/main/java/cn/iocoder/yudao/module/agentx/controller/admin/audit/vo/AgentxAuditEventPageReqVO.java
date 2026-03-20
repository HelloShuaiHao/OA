package cn.iocoder.yudao.module.agentx.controller.admin.audit.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理后台 - AgentX 审计事件分页 Request VO")
public class AgentxAuditEventPageReqVO extends PageParam {

    @Schema(description = "事件类型", example = "TOOL_COMPLETED")
    private String eventType;

    @Schema(description = "场景编码", example = "oa.leave.approval")
    private String scenarioCode;

    @Schema(description = "业务键", example = "leave:99")
    private String businessKey;

    @Schema(description = "工具名", example = "bpm_query_tasks")
    private String toolName;

    @Schema(description = "OpenFang TaskRunId", example = "task-run-1")
    private String openfangTaskRunId;

    @Schema(description = "错误码", example = "1024001008")
    private Integer errorCode;

    @DateTimeFormat(pattern = DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
