package cn.iocoder.yudao.module.agentx.controller.admin.approval.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理后台 - AgentX 审批绑定 Response VO")
public class AgentxApprovalBindingRespVO {

    private Long id;
    private String scenarioCode;
    private String businessKey;
    private String openfangTaskRunId;
    private String openfangApprovalId;
    private String bpmProcessInstanceId;
    private Integer riskLevel;
    private Integer decisionStatus;
    private Integer callbackRetryCount;
    private Boolean callbackFailed;
    private String callbackLastError;
    private String actionSummary;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
