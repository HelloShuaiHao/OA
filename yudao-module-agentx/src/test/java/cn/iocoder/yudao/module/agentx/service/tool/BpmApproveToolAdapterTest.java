package cn.iocoder.yudao.module.agentx.service.tool;

import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BpmApproveToolAdapterTest {

    @Test
    void shouldCallApproveTaskWhenApprovedTrue() {
        AtomicReference<String> calledMethod = new AtomicReference<>();
        AtomicReference<Long> calledUserId = new AtomicReference<>();
        AtomicReference<String> calledTaskId = new AtomicReference<>();
        BpmTaskService bpmTaskService = proxyBpmTaskService(calledMethod, calledUserId, calledTaskId);

        BpmApproveToolAdapter adapter = new BpmApproveToolAdapter(bpmTaskService);
        Map<String, Object> result = adapter.invoke(new HashMap<String, Object>() {{
            put("task_id", "task-1");
            put("approved", true);
            put("user_id", 100L);
            put("reason", "自动通过");
        }});

        assertEquals("approveTask", calledMethod.get());
        assertEquals(Long.valueOf(100L), calledUserId.get());
        assertEquals("task-1", calledTaskId.get());
        assertTrue((Boolean) result.get("approved"));
    }

    @Test
    void shouldCallRejectTaskWhenApprovedFalse() {
        AtomicReference<String> calledMethod = new AtomicReference<>();
        AtomicReference<Long> calledUserId = new AtomicReference<>();
        AtomicReference<String> calledTaskId = new AtomicReference<>();
        BpmTaskService bpmTaskService = proxyBpmTaskService(calledMethod, calledUserId, calledTaskId);

        BpmApproveToolAdapter adapter = new BpmApproveToolAdapter(bpmTaskService);
        Map<String, Object> result = adapter.invoke(new HashMap<String, Object>() {{
            put("task_id", "task-2");
            put("approved", false);
            put("user_id", 200L);
            put("reason", "自动拒绝");
        }});

        assertEquals("rejectTask", calledMethod.get());
        assertEquals(Long.valueOf(200L), calledUserId.get());
        assertEquals("task-2", calledTaskId.get());
        assertFalse((Boolean) result.get("approved"));
    }

    private BpmTaskService proxyBpmTaskService(AtomicReference<String> calledMethod,
                                               AtomicReference<Long> calledUserId,
                                               AtomicReference<String> calledTaskId) {
        return (BpmTaskService) Proxy.newProxyInstance(
                BpmTaskService.class.getClassLoader(),
                new Class<?>[] { BpmTaskService.class },
                (proxy, method, args) -> {
                    if ("approveTask".equals(method.getName()) || "rejectTask".equals(method.getName())) {
                        calledMethod.set(method.getName());
                        calledUserId.set((Long) args[0]);
                        Object req = args[1];
                        calledTaskId.set((String) req.getClass().getMethod("getId").invoke(req));
                    }
                    return null;
                });
    }

}
