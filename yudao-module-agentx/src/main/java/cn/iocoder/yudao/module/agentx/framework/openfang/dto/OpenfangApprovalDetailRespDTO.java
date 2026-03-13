package cn.iocoder.yudao.module.agentx.framework.openfang.dto;

import lombok.Data;

/**
 * OpenFang 审批详情响应。
 */
@Data
public class OpenfangApprovalDetailRespDTO {

    private String approvalId;
    private String taskRunId;
    private String title;
    private String reason;
    private Integer riskLevel;
    private String actionSummary;
    private String requesterId;

}
