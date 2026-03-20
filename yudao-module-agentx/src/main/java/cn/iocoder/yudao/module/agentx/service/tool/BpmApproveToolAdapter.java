package cn.iocoder.yudao.module.agentx.service.tool;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskApproveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.task.BpmTaskRejectReqVO;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;

import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.TOOL_REQUEST_INVALID;

/**
 * `bpm_approve` Tool 适配器。
 */
public class BpmApproveToolAdapter implements AgentxToolAdapter {

    private final BpmTaskService bpmTaskService;

    public BpmApproveToolAdapter(BpmTaskService bpmTaskService) {
        this.bpmTaskService = bpmTaskService;
    }

    @Override
    public String toolName() {
        return BpmApproveToolDescriptor.TOOL_NAME;
    }

    @Override
    public Map<String, Object> invoke(Map<String, Object> request) {
        String taskId = stringValue(request.get("task_id"));
        if (StrUtil.isBlank(taskId)) {
            throw ServiceExceptionUtil.exception(TOOL_REQUEST_INVALID);
        }
        Boolean approved = booleanValue(request.get("approved"));
        if (approved == null) {
            throw ServiceExceptionUtil.exception(TOOL_REQUEST_INVALID);
        }
        Long userId = longValue(request.get("user_id"));
        String reason = stringValue(request.get("reason"));

        if (approved) {
            BpmTaskApproveReqVO reqVO = new BpmTaskApproveReqVO();
            reqVO.setId(taskId);
            reqVO.setReason(reason);
            bpmTaskService.approveTask(userId, reqVO);
        } else {
            BpmTaskRejectReqVO reqVO = new BpmTaskRejectReqVO();
            reqVO.setId(taskId);
            reqVO.setReason(reason);
            bpmTaskService.rejectTask(userId, reqVO);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("approved", approved);
        result.put("task_id", taskId);
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

    private Boolean booleanValue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        if ("true".equalsIgnoreCase(text)) {
            return true;
        }
        if ("false".equalsIgnoreCase(text)) {
            return false;
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

}
