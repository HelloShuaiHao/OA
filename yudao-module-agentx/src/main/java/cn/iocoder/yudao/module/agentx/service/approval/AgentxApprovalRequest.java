package cn.iocoder.yudao.module.agentx.service.approval;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * OA 建单所需的精确审批详情。
 */
@Data
@Accessors(chain = true)
public class AgentxApprovalRequest {

    private String scenarioCode;
    private String businessKey;
    private String openfangTaskRunId;
    private String openfangApprovalId;
    private String title;
    private String reason;
    private Integer riskLevel;
    private String actionSummary;
    private String requesterId;
    private String approverSource;
    private String approverRef;

}
