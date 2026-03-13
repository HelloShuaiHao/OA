package cn.iocoder.yudao.module.agentx.service.approval;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxApprovalBpmMapperTest {

    @Test
    void shouldMapApprovalRequestToBpmPayload() {
        AgentxApprovalBpmMapper mapper = new AgentxApprovalBpmMapper();
        AgentxApprovalRequest request = new AgentxApprovalRequest()
                .setScenarioCode("oa.leave.approval")
                .setBusinessKey("leave:12")
                .setOpenfangTaskRunId("task-run-1")
                .setOpenfangApprovalId("approval-1")
                .setTitle("请假提交审批")
                .setReason("年假 2 天")
                .setRiskLevel(20)
                .setActionSummary("提交请假单")
                .setRequesterId("u-1")
                .setApproverSource("dept-manager")
                .setApproverRef("dept:tech");

        AgentxBpmApprovalCreateReq payload = mapper.toCreateRequest(request);

        assertEquals("请假提交审批", payload.getTitle());
        assertEquals("提交请假单 | 年假 2 天", payload.getSummary());
        assertEquals("dept-manager", payload.getApproverSource());
        assertEquals("dept:tech", payload.getApproverRef());
        assertEquals("oa.leave.approval", payload.getScenarioCode());
        assertEquals("leave:12", payload.getBusinessKey());
        assertEquals("task-run-1", payload.getTaskRunId());
        assertEquals("approval-1", payload.getApprovalId());
    }

}
