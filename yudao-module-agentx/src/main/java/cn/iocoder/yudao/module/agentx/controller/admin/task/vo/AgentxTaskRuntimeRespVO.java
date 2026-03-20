package cn.iocoder.yudao.module.agentx.controller.admin.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - AgentX 任务运行时详情 Response VO")
@Data
public class AgentxTaskRuntimeRespVO {

    private String taskRunId;
    private String workflowId;
    private String workflowVersion;
    private String status;
    private String stage;
    private List<String> pendingApprovalIds;
    private String resultSummary;
    private String failureSummary;
    private String auditSummary;
    private List<Map<String, Object>> workflowProjections;
    private List<Map<String, Object>> traceEvents;
    private String lastError;

}
