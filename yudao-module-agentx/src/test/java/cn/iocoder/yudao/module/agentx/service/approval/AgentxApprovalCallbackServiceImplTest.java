package cn.iocoder.yudao.module.agentx.service.approval;

import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.approval.AgentxApprovalBindingMapper;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridge;
import cn.iocoder.yudao.module.agentx.service.approval.callback.AgentxApprovalAlertService;
import cn.iocoder.yudao.module.agentx.service.approval.callback.AgentxApprovalCallbackServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxApprovalCallbackServiceImplTest {

    @Test
    void shouldRetryApproveCallbackAndSucceed() {
        AtomicInteger callbackCount = new AtomicInteger();
        AtomicReference<AgentxApprovalBindingDO> lastUpdate = new AtomicReference<>();
        AtomicReference<AgentxApprovalBridgeService.ApprovalDecision> decisionSink = new AtomicReference<>();

        AgentxApprovalCallbackServiceImpl service = createService(callbackCount, 2, lastUpdate, decisionSink);
        AgentxApprovalBindingDO binding = new AgentxApprovalBindingDO();
        binding.setId(1L);
        binding.setOpenfangApprovalId("ap-1");

        service.approveCallbackWithRetry(binding, 9L, "ok");

        assertEquals(2, callbackCount.get());
        assertEquals(Boolean.FALSE, lastUpdate.get().getCallbackFailed());
        assertEquals(Integer.valueOf(1), lastUpdate.get().getCallbackRetryCount());
        assertEquals(AgentxApprovalBridgeService.ApprovalDecision.APPROVED, decisionSink.get());
    }

    @Test
    void shouldMarkFailedAfterMaxRetry() {
        AtomicInteger callbackCount = new AtomicInteger();
        AtomicReference<AgentxApprovalBindingDO> lastUpdate = new AtomicReference<>();
        AtomicReference<AgentxApprovalBridgeService.ApprovalDecision> decisionSink = new AtomicReference<>();
        AtomicReference<String> alertError = new AtomicReference<>();

        AgentxApprovalCallbackServiceImpl service = createService(callbackCount, Integer.MAX_VALUE, lastUpdate, decisionSink, alertError);
        AgentxApprovalBindingDO binding = new AgentxApprovalBindingDO();
        binding.setId(2L);
        binding.setOpenfangApprovalId("ap-2");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.rejectCallbackWithRetry(binding, 10L, "reject"));

        assertTrue(ex.getMessage().contains("mock callback"));
        assertEquals(5, callbackCount.get());
        assertEquals(Boolean.TRUE, lastUpdate.get().getCallbackFailed());
        assertEquals(Integer.valueOf(5), lastUpdate.get().getCallbackRetryCount());
        assertTrue(alertError.get().contains("mock callback fail #5"));
    }

    @SuppressWarnings("unchecked")
    private AgentxApprovalCallbackServiceImpl createService(AtomicInteger callbackCount,
                                                            int succeedAt,
                                                            AtomicReference<AgentxApprovalBindingDO> updateSink,
                                                            AtomicReference<AgentxApprovalBridgeService.ApprovalDecision> decisionSink) {
        return createService(callbackCount, succeedAt, updateSink, decisionSink, new AtomicReference<>());
    }

    @SuppressWarnings("unchecked")
    private AgentxApprovalCallbackServiceImpl createService(AtomicInteger callbackCount,
                                                            int succeedAt,
                                                            AtomicReference<AgentxApprovalBindingDO> updateSink,
                                                            AtomicReference<AgentxApprovalBridgeService.ApprovalDecision> decisionSink,
                                                            AtomicReference<String> alertSink) {
        OpenfangRuntimeBridge runtimeBridge = (OpenfangRuntimeBridge) Proxy.newProxyInstance(
                OpenfangRuntimeBridge.class.getClassLoader(),
                new Class<?>[] { OpenfangRuntimeBridge.class },
                (proxy, method, args) -> {
                    if ("approveCallback".equals(method.getName())
                            || "rejectCallback".equals(method.getName())
                            || "approve".equals(method.getName())
                            || "reject".equals(method.getName())) {
                        int current = callbackCount.incrementAndGet();
                        if (current < succeedAt) {
                            throw new RuntimeException("mock callback fail #" + current);
                        }
                    }
                    return null;
                });

        AgentxApprovalBridgeService approvalBridgeService = (AgentxApprovalBridgeService) Proxy.newProxyInstance(
                AgentxApprovalBridgeService.class.getClassLoader(),
                new Class<?>[] { AgentxApprovalBridgeService.class },
                (proxy, method, args) -> {
                    if ("syncDecision".equals(method.getName())) {
                        decisionSink.set((AgentxApprovalBridgeService.ApprovalDecision) args[2]);
                    }
                    return null;
                });

        AgentxApprovalBindingMapper bindingMapper = (AgentxApprovalBindingMapper) Proxy.newProxyInstance(
                AgentxApprovalBindingMapper.class.getClassLoader(),
                new Class<?>[] { AgentxApprovalBindingMapper.class },
                (proxy, method, args) -> {
                    if ("updateById".equals(method.getName())) {
                        updateSink.set((AgentxApprovalBindingDO) args[0]);
                        return 1;
                    }
                    return null;
                });

        AgentxApprovalAlertService alertService = (AgentxApprovalAlertService) Proxy.newProxyInstance(
                AgentxApprovalAlertService.class.getClassLoader(),
                new Class<?>[] { AgentxApprovalAlertService.class },
                (proxy, method, args) -> {
                    if ("alertRetryExhausted".equals(method.getName())) {
                        RuntimeException ex = (RuntimeException) args[1];
                        alertSink.set(ex.getMessage());
                    }
                    return null;
                });

        AgentxApprovalCallbackServiceImpl service = new AgentxApprovalCallbackServiceImpl() {
            @Override
            protected void backoffSeconds(int seconds) {
                // no-op for test
            }
        };
        ReflectionTestUtils.setField(service, "runtimeBridge", runtimeBridge);
        ReflectionTestUtils.setField(service, "approvalBridgeService", approvalBridgeService);
        ReflectionTestUtils.setField(service, "approvalBindingMapper", bindingMapper);
        ReflectionTestUtils.setField(service, "approvalAlertService", alertService);
        return service;
    }

}
