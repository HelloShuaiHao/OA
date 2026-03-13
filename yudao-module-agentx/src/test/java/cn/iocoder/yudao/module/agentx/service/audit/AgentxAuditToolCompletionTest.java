package cn.iocoder.yudao.module.agentx.service.audit;

import cn.iocoder.yudao.module.agentx.dal.dataobject.audit.AgentxAuditEventDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.audit.AgentxAuditEventMapper;
import cn.iocoder.yudao.module.agentx.service.identity.ExecutionIdentity;
import cn.iocoder.yudao.module.agentx.service.tool.AgentxDataScope;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxAuditToolCompletionTest {

    @Test
    void shouldPersistToolCompletionAuditFields() {
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

        service.recordToolCompletion(new ExecutionIdentity()
                        .setPrincipalId("u-1")
                        .setTenantId(1L)
                        .setAgentCode("leave-agent"),
                "oa.leave.approval", "leave:1", "task-run-1", "leave.submit",
                new AgentxDataScope().setTenantId(1L).setBusinessKeys(List.of("leave:1")),
                30, "req:leaveId=L-1", "ok:formId=F-1", 88L, null, "leave-form-created");

        AgentxAuditEventDO event = events.get(0);
        assertEquals("TOOL_COMPLETED", event.getEventType());
        assertEquals("req:leaveId=L-1", event.getRequestSummary());
        assertEquals("ok:formId=F-1", event.getResultSummary());
        assertEquals(Long.valueOf(88L), event.getDurationMs());
        assertEquals("leave-form-created", event.getBusinessImpactSummary());
    }

}
