package cn.iocoder.yudao.module.agentx.service.approval;

import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangApprovalDetailRespDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AgentxApprovalDetailIndexServiceTest {

    @Test
    void shouldBuildInvalidateCompensateAndRepairApprovalIndex() {
        AgentxApprovalDetailIndexService service = new AgentxApprovalDetailIndexService();
        OpenfangApprovalDetailRespDTO detail = new OpenfangApprovalDetailRespDTO();
        detail.setApprovalId("approval-1");
        detail.setTaskRunId("task-run-1");
        detail.setTitle("请假提交审批");
        detail.setReason("年假 2 天");

        service.put(1L, "task-run-1", "approval-1", detail);

        AgentxApprovalDetailIndexEntry entry = service.get(1L, "task-run-1", "approval-1");
        assertNotNull(entry);
        assertEquals("请假提交审批", entry.getDetail().getTitle());

        service.invalidate(1L, "task-run-1", "approval-1");
        assertNull(service.get(1L, "task-run-1", "approval-1"));

        service.compensateMissingIndex(1L, "task-run-1", "approval-1", detail);
        assertNotNull(service.get(1L, "task-run-1", "approval-1"));

        service.repairInconsistency(1L, "task-run-1", "approval-1", stale -> detail.setReason("年假 3 天"));
        assertEquals("年假 3 天", service.get(1L, "task-run-1", "approval-1").getDetail().getReason());

        assertNull(service.get(2L, "task-run-1", "approval-1"));
    }

}
