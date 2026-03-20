package cn.iocoder.yudao.module.agentx.service.approval.callback;

import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;

public interface AgentxApprovalAlertService {

    void alertRetryExhausted(AgentxApprovalBindingDO binding, RuntimeException ex);

}
