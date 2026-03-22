package cn.iocoder.yudao.module.agentx.controller.admin.agent.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AgentX 数字员工分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentxAgentPageReqVO extends PageParam {

    @Schema(description = "员工名称")
    private String agentName;

    @Schema(description = "部门 ID")
    private Long deptId;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "模板类型")
    private String templateType;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
