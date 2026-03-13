package cn.iocoder.yudao.module.agentx.service.audit;

import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.audit.AgentxAuditEventDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.audit.AgentxAuditEventMapper;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalBridgeService;
import cn.iocoder.yudao.module.agentx.service.approval.AgentxApprovalRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxAuditServiceImplTest {

    @Test
    void shouldRecordTaskApprovalLifecycleEvents() {
        List<AgentxAuditEventDO> events = new CopyOnWriteArrayList<>();
        AgentxAuditEventMapper mapper = (AgentxAuditEventMapper) Proxy.newProxyInstance(
                AgentxAuditEventMapper.class.getClassLoader(),
                new Class<?>[] { AgentxAuditEventMapper.class },
                (proxy, method, args) -> {
                    if ("insert".equals(method.getName())) {
                        events.add((AgentxAuditEventDO) args[0]);
                        return 1;
                    }
                    return null;
                });
        AgentxAuditServiceImpl service = new AgentxAuditServiceImpl();
        ReflectionTestUtils.setField(service, "auditEventMapper", mapper);

        AgentxTaskProjectionDO projection = new AgentxTaskProjectionDO();
        projection.setScenarioCode("oa.leave.approval");
        projection.setBusinessKey("leave:1");
        projection.setOpenfangTaskRunId("task-run-1");
        projection.setRiskLevel(20);
        AgentxApprovalRequest request = new AgentxApprovalRequest()
                .setOpenfangApprovalId("approval-1")
                .setRiskLevel(20)
                .setActionSummary("提交请假单");
        AgentxApprovalBindingDO binding = new AgentxApprovalBindingDO();
        binding.setScenarioCode("oa.leave.approval");
        binding.setBusinessKey("leave:1");
        binding.setOpenfangTaskRunId("task-run-1");
        binding.setOpenfangApprovalId("approval-1");
        binding.setRiskLevel(20);
        binding.setActionSummary("提交请假单");

        service.recordTaskStarted(projection);
        service.recordPendingApproval(projection, request);
        service.recordApprovalDecision(binding, AgentxApprovalBridgeService.ApprovalDecision.APPROVED);

        assertEquals("TASK_STARTED", events.get(0).getEventType());
        assertEquals("TASK_WAITING_APPROVAL", events.get(1).getEventType());
        assertEquals("APPROVAL_APPROVED", events.get(2).getEventType());
        assertEquals("approval-1", events.get(2).getOpenfangApprovalId());
    }

    @Test
    void shouldRecordAuthorizationDeniedEvent() {
        List<AgentxAuditEventDO> events = new CopyOnWriteArrayList<>();
        AgentxAuditEventMapper mapper = (AgentxAuditEventMapper) Proxy.newProxyInstance(
                AgentxAuditEventMapper.class.getClassLoader(),
                new Class<?>[] { AgentxAuditEventMapper.class },
                (proxy, method, args) -> {
                    if ("insert".equals(method.getName())) {
                        events.add((AgentxAuditEventDO) args[0]);
                        return 1;
                    }
                    return null;
                });
        AgentxAuditServiceImpl service = new AgentxAuditServiceImpl();
        ReflectionTestUtils.setField(service, "auditEventMapper", mapper);

        service.recordAuthorizationDenied("oa.leave.approval", "leave:2", "委托已过期");

        assertEquals("AUTHORIZATION_DENIED", events.get(0).getEventType());
        assertEquals("委托已过期", events.get(0).getResultSummary());
    }

}
