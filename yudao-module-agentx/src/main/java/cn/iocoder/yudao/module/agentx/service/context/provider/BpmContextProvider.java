package cn.iocoder.yudao.module.agentx.service.context.provider;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.agentx.service.context.ContextProvider;
import cn.iocoder.yudao.module.agentx.service.context.ContextRequest;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BPM 待办上下文 Provider。
 */
@Component
public class BpmContextProvider implements ContextProvider {

    @Resource
    private TaskService taskService;

    @Override
    public String getType() {
        return "bpm_tasks";
    }

    @Override
    public Map<String, Object> provide(ContextRequest request) {
        TaskQuery query = taskService.createTaskQuery();
        if (request.getUserId() != null) {
            query.taskAssignee(String.valueOf(request.getUserId()));
        }
        String processKey = stringParam(request.getParams(), "processKey");
        if (StrUtil.isNotBlank(processKey)) {
            query.processDefinitionKey(processKey);
        }
        String status = stringParam(request.getParams(), "status");
        if (StrUtil.equalsIgnoreCase("pending", status)) {
            query.active();
        }
        List<Task> tasks = query.list();

        List<Map<String, Object>> taskList = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("task_id", task.getId());
            taskMap.put("task_name", task.getName());
            taskMap.put("process_instance_id", task.getProcessInstanceId());
            taskMap.put("process_definition_id", task.getProcessDefinitionId());
            taskList.add(taskMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("tasks", taskList);
        return result;
    }

    @SuppressWarnings("unchecked")
    private String stringParam(Map<String, Object> params, String key) {
        if (params == null) {
            return null;
        }
        Object value = params.get(key);
        if (value != null) {
            return String.valueOf(value);
        }
        Object filter = params.get("filter");
        if (filter instanceof Map) {
            Object nested = ((Map<String, Object>) filter).get(key);
            return nested == null ? null : String.valueOf(nested);
        }
        return null;
    }

}
