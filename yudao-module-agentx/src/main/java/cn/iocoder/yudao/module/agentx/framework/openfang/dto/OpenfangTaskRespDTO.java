package cn.iocoder.yudao.module.agentx.framework.openfang.dto;

import lombok.Data;

import java.util.List;

/**
 * OpenFang task 投影响应。
 */
@Data
public class OpenfangTaskRespDTO {

    private String taskRunId;
    private String workflowId;
    private String workflowVersion;
    private String status;
    private String stage;
    private List<String> pendingApprovalIds;
    private String resultSummary;
    private String failureSummary;
    private String auditSummary;

}
