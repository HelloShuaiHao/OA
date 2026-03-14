package cn.iocoder.yudao.module.agentx.service.approval;

import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangApprovalDetailRespDTO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class AgentxApprovalDetailIndexService {

    private final Map<String, AgentxApprovalDetailIndexEntry> entries = new ConcurrentHashMap<>();

    public void put(Long tenantId, String taskRunId, String approvalId, OpenfangApprovalDetailRespDTO detail) {
        entries.put(key(tenantId, taskRunId, approvalId), new AgentxApprovalDetailIndexEntry()
                .setTenantId(tenantId)
                .setTaskRunId(taskRunId)
                .setApprovalId(approvalId)
                .setDetail(detail));
    }

    public AgentxApprovalDetailIndexEntry get(Long tenantId, String taskRunId, String approvalId) {
        return entries.get(key(tenantId, taskRunId, approvalId));
    }

    public void invalidate(Long tenantId, String taskRunId, String approvalId) {
        entries.remove(key(tenantId, taskRunId, approvalId));
    }

    public void compensateMissingIndex(Long tenantId, String taskRunId, String approvalId, OpenfangApprovalDetailRespDTO detail) {
        entries.computeIfAbsent(key(tenantId, taskRunId, approvalId), ignored -> new AgentxApprovalDetailIndexEntry()
                .setTenantId(tenantId)
                .setTaskRunId(taskRunId)
                .setApprovalId(approvalId)
                .setDetail(detail));
    }

    public void repairInconsistency(Long tenantId, String taskRunId, String approvalId,
                                    Consumer<OpenfangApprovalDetailRespDTO> repairer) {
        AgentxApprovalDetailIndexEntry entry = get(tenantId, taskRunId, approvalId);
        if (entry == null) {
            return;
        }
        repairer.accept(entry.getDetail());
    }

    private String key(Long tenantId, String taskRunId, String approvalId) {
        return tenantId + ":" + taskRunId + ":" + approvalId;
    }

}
