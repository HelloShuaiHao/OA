package cn.iocoder.yudao.module.agentx.framework.openfang.client;

import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangApprovalDetailRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangTaskRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunReqDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunRespDTO;

/**
 * OpenFang Runtime 受控桥接口。
 */
public interface OpenfangRuntimeBridge {

    OpenfangWorkflowRunRespDTO runWorkflow(String workflowId, OpenfangWorkflowRunReqDTO request);

    OpenfangTaskRespDTO getTask(String taskRunId);

    OpenfangApprovalDetailRespDTO getApprovalDetail(String taskRunId, String approvalId);

    void approve(String approvalId, String decisionComment);

    void reject(String approvalId, String decisionComment);

}
