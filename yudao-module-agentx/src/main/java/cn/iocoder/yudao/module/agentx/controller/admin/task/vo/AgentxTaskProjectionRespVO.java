package cn.iocoder.yudao.module.agentx.controller.admin.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AgentX 任务投影 Response VO")
@Data
public class AgentxTaskProjectionRespVO {

    private Long id;
    private String scenarioCode;
    private String businessKey;
    private String idempotencyKey;
    private String openfangTaskRunId;
    private Integer projectionStatus;
    private Integer riskLevel;
    private String resultSummary;
    private String failureSummary;
    private String auditSummary;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
