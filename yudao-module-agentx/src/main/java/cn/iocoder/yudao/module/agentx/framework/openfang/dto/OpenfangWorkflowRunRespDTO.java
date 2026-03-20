package cn.iocoder.yudao.module.agentx.framework.openfang.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * OpenFang workflow run 响应。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenfangWorkflowRunRespDTO {

    @JsonAlias("task_run_id")
    private String taskRunId;
    @JsonAlias("workflow_id")
    private String workflowId;
    @JsonAlias("workflow_version")
    private String workflowVersion;
    @JsonAlias({"task_status", "state"})
    private String taskStatus;

}
