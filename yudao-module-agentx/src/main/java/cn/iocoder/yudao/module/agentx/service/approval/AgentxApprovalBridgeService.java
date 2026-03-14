package cn.iocoder.yudao.module.agentx.service.approval;

import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangApprovalDetailRespDTO;

public interface AgentxApprovalBridgeService {

    AgentxApprovalRequest buildApprovalRequest(AgentxTaskProjectionDO projection, String approvalId);

    AgentxApprovalBindingDO createApprovalBinding(AgentxTaskProjectionDO projection,
                                                  AgentxApprovalRequest request,
                                                  String bpmProcessInstanceId);

    AgentxApprovalBindingDO createApprovalBinding(AgentxTaskProjectionDO projection,
                                                  OpenfangApprovalDetailRespDTO detail,
                                                  String bpmProcessInstanceId);

    AgentxBpmApprovalCreateReq buildBpmCreateRequest(AgentxApprovalRequest request);

    void syncDecision(Long approvalBindingId, Long taskProjectionId, ApprovalDecision decision);

    AgentxApprovalResolution resolveOutcome(Long approvalBindingId, Long taskProjectionId, AgentxApprovalOutcome outcome);

    enum ApprovalDecision {
        APPROVED,
        REJECTED
    }

}
