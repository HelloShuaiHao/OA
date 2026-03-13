package cn.iocoder.yudao.module.agentx.framework.openfang.dto;

import lombok.Data;

/**
 * OpenFang workflow run 响应。
 */
@Data
public class OpenfangWorkflowRunRespDTO {

    private String taskRunId;
    private String workflowId;
    private String workflowVersion;
    private String taskStatus;

}
