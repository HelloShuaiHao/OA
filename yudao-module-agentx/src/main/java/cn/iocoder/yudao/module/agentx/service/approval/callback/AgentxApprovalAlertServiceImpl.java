package cn.iocoder.yudao.module.agentx.service.approval.callback;

import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AgentxApprovalAlertServiceImpl implements AgentxApprovalAlertService {

    @Override
    public void alertRetryExhausted(AgentxApprovalBindingDO binding, RuntimeException ex) {
        log.error("[agentx][approval callback retry exhausted][bindingId={}][approvalId={}] {}",
                binding.getId(), binding.getOpenfangApprovalId(), ex.getMessage(), ex);
    }

}
