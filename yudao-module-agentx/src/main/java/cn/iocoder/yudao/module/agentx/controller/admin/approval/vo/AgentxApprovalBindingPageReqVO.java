package cn.iocoder.yudao.module.agentx.controller.admin.approval.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理后台 - AgentX 审批绑定分页 Request VO")
public class AgentxApprovalBindingPageReqVO extends PageParam {

    private String scenarioCode;
    private String businessKey;
    private Integer decisionStatus;
    private Boolean callbackFailed;

    @DateTimeFormat(pattern = DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
