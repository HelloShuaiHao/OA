package cn.iocoder.yudao.module.agentx.service.approval;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AgentxBpmApprovalCreateReq {

    private String title;
    private String summary;
    private String approverSource;
    private String approverRef;
    private String scenarioCode;
    private String businessKey;
    private String taskRunId;
    private String approvalId;
    private Integer riskLevel;
    private String requesterId;

}
