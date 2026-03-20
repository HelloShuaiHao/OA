package cn.iocoder.yudao.module.agentx.service.task;

import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.task.AgentxTaskProjectionMapper;
import cn.iocoder.yudao.module.agentx.enums.AgentxRiskLevelEnum;
import cn.iocoder.yudao.module.agentx.enums.AgentxTaskProjectionStatusEnum;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridge;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangTaskRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunReqDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunRespDTO;
import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import cn.iocoder.yudao.module.agentx.service.workflow.AgentxWorkflowMapping;
import cn.iocoder.yudao.module.agentx.service.workflow.AgentxWorkflowResolution;
import cn.iocoder.yudao.module.agentx.service.workflow.AgentxWorkflowResolver;
import org.springframework.dao.DuplicateKeyException;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AgentX 任务编排服务。
 */
public class AgentxTaskOrchestrationService {

    private final AgentxWorkflowResolver workflowResolver;

    @Resource
    private AgentxTaskProjectionMapper taskProjectionMapper;
    @Resource
    private OpenfangRuntimeBridge runtimeBridge;

    public AgentxTaskOrchestrationService(AgentxWorkflowResolver workflowResolver) {
        this.workflowResolver = workflowResolver;
    }

    public AgentxTaskProjectionDO startTask(AgentxTaskStartRequest request, List<AgentxWorkflowMapping> mappings) {
        return createTask(request, mappings);
    }

    public AgentxTaskProjectionDO createTask(AgentxTaskStartRequest request, List<AgentxWorkflowMapping> mappings) {
        if (request.getIdempotencyKey() != null) {
            AgentxTaskProjectionDO existing = taskProjectionMapper.selectByIdempotencyKey(request.getIdempotencyKey());
            if (existing != null) {
                return existing;
            }
        }
        AgentxWorkflowResolution resolution = workflowResolver.resolve(
                request.getScenarioCode(), request.getWorkflowVersion(), mappings);
        OpenfangWorkflowRunRespDTO response = runtimeBridge.runWorkflow(resolution.getOpenfangWorkflowId(),
                buildRunRequest(request, resolution.getOpenfangWorkflowId(), request.getCapabilities()));
        AgentxTaskProjectionDO projection = new AgentxTaskProjectionDO();
        projection.setScenarioCode(request.getScenarioCode());
        projection.setBusinessKey(request.getBusinessKey());
        projection.setIdempotencyKey(request.getIdempotencyKey());
        projection.setOpenfangTaskRunId(response.getTaskRunId());
        projection.setProjectionStatus(AgentxTaskProjectionStatusEnum.RUNNING.getStatus());
        projection.setRiskLevel(AgentxRiskLevelEnum.MEDIUM.getLevel());
        try {
            taskProjectionMapper.insert(projection);
        } catch (DuplicateKeyException ex) {
            if (request.getIdempotencyKey() != null) {
                AgentxTaskProjectionDO existing = taskProjectionMapper.selectByIdempotencyKey(request.getIdempotencyKey());
                if (existing != null) {
                    return existing;
                }
            }
            throw ex;
        }
        return projection;
    }

    public OpenfangTaskRespDTO getTaskRun(String taskRunId) {
        return runtimeBridge.getTaskRun(taskRunId);
    }

    public void refreshProjection(AgentxTaskProjectionDO existing, OpenfangTaskRespDTO task) {
        AgentxTaskProjectionDO update = new AgentxTaskProjectionDO();
        update.setId(existing.getId());
        update.setProjectionStatus(mapStatus(task.getStatus()));
        update.setResultSummary(task.getResultSummary());
        update.setFailureSummary(task.getFailureSummary());
        update.setAuditSummary(task.getAuditSummary());
        taskProjectionMapper.updateById(update);
    }

    public Integer nextPollIntervalSeconds(int attempt) {
        int normalizedAttempt = Math.max(1, attempt);
        int interval = (int) Math.pow(2, normalizedAttempt);
        return Math.min(interval, 30);
    }

    public boolean markFailedIfTimedOut(AgentxTaskProjectionDO projection, LocalDateTime now) {
        if (projection == null || projection.getUpdateTime() == null || projection.getProjectionStatus() == null) {
            return false;
        }
        if (isTerminalStatus(projection.getProjectionStatus())) {
            return false;
        }
        if (projection.getUpdateTime().isAfter(now.minusHours(24))) {
            return false;
        }
        AgentxTaskProjectionDO update = new AgentxTaskProjectionDO();
        update.setId(projection.getId());
        update.setProjectionStatus(AgentxTaskProjectionStatusEnum.FAILED.getStatus());
        update.setFailureSummary("Task 24 小时无更新，自动失败");
        taskProjectionMapper.updateById(update);
        return true;
    }

    private OpenfangWorkflowRunReqDTO buildRunRequest(AgentxTaskStartRequest request, String workflowId,
                                                      Set<AgentxCapability> capabilities) {
        OpenfangWorkflowRunReqDTO dto = new OpenfangWorkflowRunReqDTO();
        dto.setScenarioCode(request.getScenarioCode());
        dto.setBusinessKey(request.getBusinessKey());
        dto.setIdempotencyKey(request.getIdempotencyKey());
        dto.setPrincipalType(request.getPrincipalType());
        dto.setPrincipalId(request.getPrincipalId());
        dto.setCapabilityCodes(capabilities.stream().map(Enum::name).collect(Collectors.toSet()));
        dto.setContextBundle(request.getContextBundle());
        dto.setContextSummary("context-keys=" + request.getContextBundle().keySet());
        return dto;
    }

    private Integer mapStatus(String status) {
        if ("WAITING_APPROVAL".equalsIgnoreCase(status) || "waiting_for_approval".equalsIgnoreCase(status)) {
            return AgentxTaskProjectionStatusEnum.WAITING_APPROVAL.getStatus();
        }
        if ("SUCCEEDED".equalsIgnoreCase(status) || "succeeded".equalsIgnoreCase(status)) {
            return AgentxTaskProjectionStatusEnum.SUCCEEDED.getStatus();
        }
        if ("FAILED".equalsIgnoreCase(status)
                || "failed_recoverable".equalsIgnoreCase(status)
                || "failed_terminal".equalsIgnoreCase(status)
                || "cancelled".equalsIgnoreCase(status)) {
            return AgentxTaskProjectionStatusEnum.FAILED.getStatus();
        }
        return AgentxTaskProjectionStatusEnum.RUNNING.getStatus();
    }

    private boolean isTerminalStatus(Integer status) {
        return AgentxTaskProjectionStatusEnum.SUCCEEDED.getStatus().equals(status)
                || AgentxTaskProjectionStatusEnum.FAILED.getStatus().equals(status)
                || AgentxTaskProjectionStatusEnum.COMPENSATED.getStatus().equals(status);
    }

}
