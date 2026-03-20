package cn.iocoder.yudao.module.agentx.service.approval;

import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.approval.AgentxApprovalBindingMapper;
import cn.iocoder.yudao.module.agentx.dal.mysql.task.AgentxTaskProjectionMapper;
import cn.iocoder.yudao.module.agentx.service.approval.callback.AgentxApprovalTimeoutScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxApprovalTimeoutSchedulerTest {

    @Test
    void shouldResolveTimeoutForPendingBindings() {
        AgentxApprovalBindingDO pending = new AgentxApprovalBindingDO();
        pending.setId(1L);
        pending.setOpenfangTaskRunId("tr-1");

        AgentxTaskProjectionDO projection = new AgentxTaskProjectionDO();
        projection.setId(99L);
        projection.setOpenfangTaskRunId("tr-1");

        AtomicReference<Long> bindingIdSink = new AtomicReference<>();
        AtomicReference<Long> projectionIdSink = new AtomicReference<>();
        AtomicReference<AgentxApprovalOutcome> outcomeSink = new AtomicReference<>();

        AgentxApprovalBindingMapper bindingMapper = (AgentxApprovalBindingMapper) Proxy.newProxyInstance(
                AgentxApprovalBindingMapper.class.getClassLoader(),
                new Class<?>[] { AgentxApprovalBindingMapper.class },
                (proxy, method, args) -> {
                    if ("selectListPendingTimeout".equals(method.getName())) {
                        return List.of(pending);
                    }
                    return null;
                });

        AgentxTaskProjectionMapper projectionMapper = (AgentxTaskProjectionMapper) Proxy.newProxyInstance(
                AgentxTaskProjectionMapper.class.getClassLoader(),
                new Class<?>[] { AgentxTaskProjectionMapper.class },
                (proxy, method, args) -> {
                    if ("selectByOpenfangTaskRunId".equals(method.getName())) {
                        return projection;
                    }
                    return null;
                });

        AgentxApprovalBridgeService bridgeService = (AgentxApprovalBridgeService) Proxy.newProxyInstance(
                AgentxApprovalBridgeService.class.getClassLoader(),
                new Class<?>[] { AgentxApprovalBridgeService.class },
                (proxy, method, args) -> {
                    if ("resolveOutcome".equals(method.getName())) {
                        bindingIdSink.set((Long) args[0]);
                        projectionIdSink.set((Long) args[1]);
                        outcomeSink.set((AgentxApprovalOutcome) args[2]);
                    }
                    return null;
                });

        AgentxApprovalTimeoutScheduler scheduler = new AgentxApprovalTimeoutScheduler();
        ReflectionTestUtils.setField(scheduler, "approvalBindingMapper", bindingMapper);
        ReflectionTestUtils.setField(scheduler, "taskProjectionMapper", projectionMapper);
        ReflectionTestUtils.setField(scheduler, "approvalBridgeService", bridgeService);

        scheduler.checkApprovalTimeout();

        assertEquals(Long.valueOf(1L), bindingIdSink.get());
        assertEquals(Long.valueOf(99L), projectionIdSink.get());
        assertEquals(AgentxApprovalOutcome.TIMEOUT, outcomeSink.get());
    }

}
