package cn.iocoder.yudao.module.agentx.service.tool;

import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BpmQueryTasksToolAdapterTest {

    @Test
    void shouldReturnTaskListByUser() {
        TaskService taskService = proxyTaskService(List.of(
                proxyTask("t-1", "审批任务1", "pi-1", "pd-1"),
                proxyTask("t-2", "审批任务2", "pi-2", "pd-2")
        ));

        BpmQueryTasksToolAdapter adapter = new BpmQueryTasksToolAdapter(taskService);
        Map<String, Object> result = adapter.invoke(new HashMap<String, Object>() {{
            put("user_id", 100L);
            put("process_key", "leave");
            put("limit", 10);
        }});

        assertEquals(2, result.get("count"));
        assertNotNull(result.get("tasks"));
    }

    private TaskService proxyTaskService(List<Task> tasks) {
        TaskQuery taskQuery = (TaskQuery) Proxy.newProxyInstance(
                TaskQuery.class.getClassLoader(),
                new Class<?>[] { TaskQuery.class },
                (proxy, method, args) -> {
                    if ("listPage".equals(method.getName())) {
                        return tasks;
                    }
                    return proxy;
                });
        return (TaskService) Proxy.newProxyInstance(
                TaskService.class.getClassLoader(),
                new Class<?>[] { TaskService.class },
                (proxy, method, args) -> {
                    if ("createTaskQuery".equals(method.getName())) {
                        return taskQuery;
                    }
                    return null;
                });
    }

    private Task proxyTask(String id, String name, String processInstanceId, String processDefinitionId) {
        return (Task) Proxy.newProxyInstance(
                Task.class.getClassLoader(),
                new Class<?>[] { Task.class },
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getId":
                            return id;
                        case "getName":
                            return name;
                        case "getProcessInstanceId":
                            return processInstanceId;
                        case "getProcessDefinitionId":
                            return processDefinitionId;
                        default:
                            return null;
                    }
                });
    }

}
