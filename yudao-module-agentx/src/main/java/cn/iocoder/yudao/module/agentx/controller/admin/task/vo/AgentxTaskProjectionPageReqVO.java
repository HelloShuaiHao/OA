package cn.iocoder.yudao.module.agentx.controller.admin.task.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AgentX 任务投影分页 Request VO")
@Data
public class AgentxTaskProjectionPageReqVO extends PageParam {

    @Schema(description = "场景编码")
    private String scenarioCode;

    @Schema(description = "业务主键")
    private String businessKey;

    @Schema(description = "投影状态")
    private Integer projectionStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
