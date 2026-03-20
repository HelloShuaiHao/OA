package cn.iocoder.yudao.module.agentx.controller.admin.audit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理后台 - AgentX 审计事件 Response VO")
public class AgentxAuditEventRespVO {

    private Long id;
    private Long tenantId;
    private String initiatorId;
    private String agentCode;
    private String eventType;
    private String scenarioCode;
    private String businessKey;
    private String openfangTaskRunId;
    private String openfangApprovalId;
    private String toolName;
    private Integer riskLevel;
    private String dataScopeSummary;
    private String requestSummary;
    private Long durationMs;
    private Integer errorCode;
    private String businessImpactSummary;
    private String resultSummary;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
