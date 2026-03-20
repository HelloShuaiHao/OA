package cn.iocoder.yudao.module.agentx.service.approval.callback;

import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;

public interface AgentxApprovalCallbackService {

    void approveCallbackWithRetry(AgentxApprovalBindingDO binding, Long taskProjectionId, String comment);

    void rejectCallbackWithRetry(AgentxApprovalBindingDO binding, Long taskProjectionId, String comment);

}
