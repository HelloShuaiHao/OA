package cn.iocoder.yudao.module.agentx.framework.openfang.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * OpenFang 审批详情响应。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenfangApprovalDetailRespDTO {

    @JsonAlias("approval_id")
    private String approvalId;
    @JsonAlias("task_run_id")
    private String taskRunId;
    private String title;
    private String reason;
    @JsonAlias("risk_level")
    private Integer riskLevel;
    @JsonAlias("action_summary")
    private String actionSummary;
    @JsonAlias("requester_id")
    private String requesterId;

}
