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

class AgentxAuditToolEventTest {

    @Test
    void shouldPersistMinimalToolAuditFields() {
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

        service.recordToolInvocation(new ExecutionIdentity()
                        .setPrincipalId("u-1")
                        .setTenantId(1L)
                        .setAgentCode("leave-agent"),
                "oa.leave.approval", "leave:1", "task-run-1", "leave.submit",
                new AgentxDataScope().setTenantId(1L).setBusinessKeys(List.of("leave:1")),
                20, "TOOL_ALLOWED");

        AgentxAuditEventDO event = events.get(0);
        assertEquals("u-1", event.getInitiatorId());
        assertEquals("leave-agent", event.getAgentCode());
        assertEquals(Long.valueOf(1L), event.getTenantId());
        assertEquals("task-run-1", event.getOpenfangTaskRunId());
        assertEquals("leave.submit", event.getToolName());
        assertEquals("tenant=1,business=[leave:1]", event.getDataScopeSummary());
        assertEquals("TOOL_ALLOWED", event.getResultSummary());
    }

}
