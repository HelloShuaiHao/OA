package cn.iocoder.yudao.module.agentx.service.audit;

import cn.iocoder.yudao.module.agentx.dal.dataobject.audit.AgentxAuditEventDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.audit.AgentxAuditEventMapper;
import cn.iocoder.yudao.module.agentx.service.identity.ExecutionIdentity;
import cn.iocoder.yudao.module.agentx.service.tool.AgentxDataScope;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxAuditServiceImplTest {

    @Test
    void shouldMaskSensitiveFieldsWhenRecordToolCompletion() {
        AtomicReference<AgentxAuditEventDO> inserted = new AtomicReference<>();
        AgentxAuditEventMapper mapper = (AgentxAuditEventMapper) Proxy.newProxyInstance(
                AgentxAuditEventMapper.class.getClassLoader(),
                new Class<?>[] { AgentxAuditEventMapper.class },
                (proxy, method, args) -> {
                    if ("insert".equals(method.getName())) {
                        inserted.set((AgentxAuditEventDO) args[0]);
                        return 1;
                    }
                    return null;
                });

        AgentxAuditServiceImpl service = new AgentxAuditServiceImpl();
        ReflectionTestUtils.setField(service, "auditEventMapper", mapper);
        ReflectionTestUtils.setField(service, "desensitizeService", new AgentxAuditDesensitizeService());

        service.recordToolCompletion(new ExecutionIdentity().setTenantId(1L).setPrincipalId("u-1").setAgentCode("agentx"),
                "oa.leave.approval", "leave:77", "task-run-77", "bpm_query_tasks",
                new AgentxDataScope().setTenantId(1L).setBusinessKeys(Collections.singletonList("leave:77"))
                        .setMaskedFields(Collections.emptyList()),
                20, "password=abc", "phone=13800138000", 10L, null, "id=110101199001011234");

        assertEquals("TOOL_COMPLETED", inserted.get().getEventType());
        assertFalse(inserted.get().getRequestSummary().contains("abc"));
        assertFalse(inserted.get().getResultSummary().contains("13800138000"));
        assertFalse(inserted.get().getBusinessImpactSummary().contains("110101199001011234"));
        assertTrue(inserted.get().getRequestSummary().contains("***"));
    }

    @Test
    void shouldRecordTaskCompletedEvent() {
        AtomicReference<AgentxAuditEventDO> inserted = new AtomicReference<>();
        AgentxAuditEventMapper mapper = (AgentxAuditEventMapper) Proxy.newProxyInstance(
                AgentxAuditEventMapper.class.getClassLoader(),
                new Class<?>[] { AgentxAuditEventMapper.class },
                (proxy, method, args) -> {
                    if ("insert".equals(method.getName())) {
                        inserted.set((AgentxAuditEventDO) args[0]);
                        return 1;
                    }
                    return null;
                });

        AgentxAuditServiceImpl service = new AgentxAuditServiceImpl();
        ReflectionTestUtils.setField(service, "auditEventMapper", mapper);
        ReflectionTestUtils.setField(service, "desensitizeService", new AgentxAuditDesensitizeService());

        service.recordTaskCompleted(new AgentxTaskProjectionDO()
                .setScenarioCode("oa.leave.approval")
                .setBusinessKey("leave:77")
                .setOpenfangTaskRunId("task-run-77")
                .setResultSummary("ok"));

        assertEquals("TASK_COMPLETED", inserted.get().getEventType());
        assertEquals("leave:77", inserted.get().getBusinessKey());
    }

}
