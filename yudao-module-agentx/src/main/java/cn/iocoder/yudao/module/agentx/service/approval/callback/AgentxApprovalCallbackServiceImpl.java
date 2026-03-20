package cn.iocoder.yudao.module.agentx.service.approval.callback;

import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.approval.AgentxApprovalBindingMapper;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridge;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalBridgeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AgentxApprovalCallbackServiceImpl implements AgentxApprovalCallbackService {

    private static final int MAX_RETRY = 5;

    @Resource
    private OpenfangRuntimeBridge runtimeBridge;
    @Resource
    private AgentxApprovalBridgeService approvalBridgeService;
    @Resource
    private AgentxApprovalBindingMapper approvalBindingMapper;
    @Resource
    private AgentxApprovalAlertService approvalAlertService;

    @Override
    public void approveCallbackWithRetry(AgentxApprovalBindingDO binding, Long taskProjectionId, String comment) {
        executeWithRetry(binding, () -> runtimeBridge.approveCallback(binding.getOpenfangApprovalId(), comment));
        approvalBridgeService.syncDecision(binding.getId(), taskProjectionId, AgentxApprovalBridgeService.ApprovalDecision.APPROVED);
    }

    @Override
    public void rejectCallbackWithRetry(AgentxApprovalBindingDO binding, Long taskProjectionId, String comment) {
        executeWithRetry(binding, () -> runtimeBridge.rejectCallback(binding.getOpenfangApprovalId(), comment));
        approvalBridgeService.syncDecision(binding.getId(), taskProjectionId, AgentxApprovalBridgeService.ApprovalDecision.REJECTED);
    }

    private void executeWithRetry(AgentxApprovalBindingDO binding, Runnable callbackAction) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                callbackAction.run();
                approvalBindingMapper.updateById(new AgentxApprovalBindingDO()
                        .setId(binding.getId())
                        .setCallbackRetryCount(attempt - 1)
                        .setCallbackFailed(false)
                        .setCallbackLastError(null));
                return;
            } catch (RuntimeException ex) {
                last = ex;
                approvalBindingMapper.updateById(new AgentxApprovalBindingDO()
                        .setId(binding.getId())
                        .setCallbackRetryCount(attempt)
                        .setCallbackFailed(attempt >= MAX_RETRY)
                        .setCallbackLastError(ex.getMessage()));
                if (attempt < MAX_RETRY) {
                    backoffSeconds((int) Math.pow(2, attempt - 1));
                }
            }
        }
        RuntimeException finalEx = last == null ? new IllegalStateException("Approval callback failed") : last;
        approvalAlertService.alertRetryExhausted(binding, finalEx);
        throw finalEx;
    }

    protected void backoffSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Approval callback retry interrupted", ex);
        }
    }

}
