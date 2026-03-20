package cn.iocoder.yudao.module.agentx.framework.openfang.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * OpenFang task 投影响应。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenfangTaskRespDTO {

    @JsonAlias("task_run_id")
    private String taskRunId;
    @JsonAlias("workflow_id")
    private String workflowId;
    @JsonAlias("workflow_version")
    private String workflowVersion;
    @JsonAlias({"state", "task_status"})
    private String status;
    @JsonAlias("current_phase")
    private String stage;
    @JsonAlias("pending_approval_ids")
    private List<String> pendingApprovalIds;
    @JsonAlias("result_summary")
    private String resultSummary;
    @JsonAlias("failure_summary")
    private String failureSummary;
    @JsonAlias("audit_summary")
    private String auditSummary;
    @JsonAlias("workflow_projections")
    private List<Map<String, Object>> workflowProjections;
    @JsonAlias({"trace_events", "skill_invocations"})
    private List<Map<String, Object>> traceEvents;
    @JsonAlias("last_error")
    private String lastError;

}
