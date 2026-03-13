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

import javax.annotation.Resource;
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
        taskProjectionMapper.insert(projection);
        return projection;
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

    private OpenfangWorkflowRunReqDTO buildRunRequest(AgentxTaskStartRequest request, String workflowId,
                                                      Set<AgentxCapability> capabilities) {
        OpenfangWorkflowRunReqDTO dto = new OpenfangWorkflowRunReqDTO();
        dto.setScenarioCode(workflowId);
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
        if ("WAITING_APPROVAL".equals(status)) {
            return AgentxTaskProjectionStatusEnum.WAITING_APPROVAL.getStatus();
        }
        if ("SUCCEEDED".equals(status)) {
            return AgentxTaskProjectionStatusEnum.SUCCEEDED.getStatus();
        }
        if ("FAILED".equals(status)) {
            return AgentxTaskProjectionStatusEnum.FAILED.getStatus();
        }
        return AgentxTaskProjectionStatusEnum.RUNNING.getStatus();
    }

}
