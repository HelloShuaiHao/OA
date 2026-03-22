package cn.iocoder.yudao.module.agentx.service.decision;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxAiDecisionServiceImplTest {

    private final AgentxAiDecisionServiceImpl service = new AgentxAiDecisionServiceImpl();

    @Test
    void shouldRenderPromptVariable() {
        String decision = service.decide("请判断是否审批：${leaveDays}", Map.of("leaveDays", 1));
        assertEquals("无需审批", decision);
    }

    @Test
    void shouldReturnNeedApprovalWhenDaysGreaterThanTwo() {
        String decision = service.decide("请假审批判断", Map.of("leaveDays", 3));
        assertEquals("需要审批", decision);
    }

    @Test
    void shouldReturnRenderedPromptForGeneralCase() {
        String decision = service.decide("生成摘要：${topic}", Map.of("topic", "报销流程"));
        assertEquals("生成摘要：报销流程", decision);
    }
}
