package cn.iocoder.yudao.module.agentx.service.tool;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.TOOL_REQUEST_INVALID;

/**
 * `bpm_query_tasks` Tool 适配器。
 */
public class BpmQueryTasksToolAdapter implements AgentxToolAdapter {

    private final TaskService taskService;

    public BpmQueryTasksToolAdapter(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String toolName() {
        return BpmQueryTasksToolDescriptor.TOOL_NAME;
    }

    @Override
    public Map<String, Object> invoke(Map<String, Object> request) {
        Long userId = longValue(request.get("user_id"));
        if (userId == null) {
            throw ServiceExceptionUtil.exception(TOOL_REQUEST_INVALID);
        }
        int limit = intValue(request.get("limit"), 20);
        if (limit <= 0) {
            limit = 20;
        }

        TaskQuery query = taskService.createTaskQuery().taskAssignee(String.valueOf(userId)).active();
        String processKey = stringValue(request.get("process_key"));
        if (StrUtil.isNotBlank(processKey)) {
            query.processDefinitionKey(processKey);
        }

        List<Task> tasks = query.listPage(0, limit);
        List<Map<String, Object>> resultTasks = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> row = new HashMap<>();
            row.put("task_id", task.getId());
            row.put("task_name", task.getName());
            row.put("process_instance_id", task.getProcessInstanceId());
            row.put("process_definition_id", task.getProcessDefinitionId());
            resultTasks.add(row);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("tasks", resultTasks);
        result.put("count", resultTasks.size());
        return result;
    }

    private Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        String text = String.valueOf(value);
        if (!StrUtil.isNumeric(text)) {
            return null;
        }
        return Long.valueOf(text);
    }

    private int intValue(Object value, int defaultValue) {
        Long parsed = longValue(value);
        return parsed == null ? defaultValue : parsed.intValue();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

}
