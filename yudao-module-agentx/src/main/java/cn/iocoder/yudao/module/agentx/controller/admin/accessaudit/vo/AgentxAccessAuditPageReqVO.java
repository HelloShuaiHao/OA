package cn.iocoder.yudao.module.agentx.controller.admin.accessaudit.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理后台 - AgentX 访问审计分页 Request VO")
public class AgentxAccessAuditPageReqVO extends PageParam {

    private Long userId;

    private String agentId;

    @Schema(description = "决策结果", example = "ALLOW")
    private String decision;

    @DateTimeFormat(pattern = DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

}
