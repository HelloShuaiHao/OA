package cn.iocoder.yudao.module.agentx.service.approval.callback;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.approval.AgentxApprovalBindingMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.task.AgentxTaskProjectionMapper;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalBridgeService;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalOutcome;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批超时检查：24 小时未决策则标记超时并驱动补偿状态。
 */
@Slf4j
@Component
public class AgentxApprovalTimeoutScheduler {

    @Resource
    private AgentxApprovalBindingMapper approvalBindingMapper;
    @Resource
    private AgentxTaskProjectionMapper taskProjectionMapper;
    @Resource
    private AgentxApprovalBridgeService approvalBridgeService;

    @Scheduled(fixedDelay = 300000)
    public void checkApprovalTimeout() {
        TenantUtils.executeIgnore(() -> {
            LocalDateTime deadline = LocalDateTime.now().minusHours(24);
            List<AgentxApprovalBindingDO> candidates = approvalBindingMapper.selectListPendingTimeout(deadline);
            for (AgentxApprovalBindingDO binding : candidates) {
                AgentxTaskProjectionDO projection = taskProjectionMapper.selectByOpenfangTaskRunId(binding.getOpenfangTaskRunId());
                if (projection == null) {
                    continue;
                }
                try {
                    approvalBridgeService.resolveOutcome(binding.getId(), projection.getId(), AgentxApprovalOutcome.TIMEOUT);
                } catch (RuntimeException ex) {
                    log.warn("Agentx approval timeout resolve failed, bindingId={} taskRunId={} err={}",
                            binding.getId(), binding.getOpenfangTaskRunId(), ex.getMessage());
                }
            }
        });
    }

}
