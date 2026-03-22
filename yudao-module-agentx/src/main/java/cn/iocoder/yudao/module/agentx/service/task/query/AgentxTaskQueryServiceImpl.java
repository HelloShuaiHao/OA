package cn.iocoder.yudao.module.agentx.service.task.query;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.task.vo.AgentxTaskProjectionPageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.task.AgentxTaskProjectionMapper;
import cn.iocoder.yudao.module.agentx.enums.AgentxTaskProjectionStatusEnum;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridge;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangTaskRespDTO;
import cn.iocoder.yudao.module.agentx.service.task.AgentxTaskOrchestrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class AgentxTaskQueryServiceImpl implements AgentxTaskQueryService {

    @Resource
    private AgentxTaskProjectionMapper taskProjectionMapper;

    @Resource
    private OpenfangRuntimeBridge runtimeBridge;
    @Resource
    private AgentxTaskOrchestrationService orchestrationService;

    @Override
    public PageResult<AgentxTaskProjectionDO> getTaskProjectionPage(AgentxTaskProjectionPageReqVO pageReqVO) {
        PageResult<AgentxTaskProjectionDO> page = taskProjectionMapper.selectPage(pageReqVO);
        if (page.getList() == null || page.getList().isEmpty()) {
            return page;
        }
        page.getList().forEach(this::syncProjectionIfNeeded);
        return page;
    }

    @Override
    public AgentxTaskProjectionDO getTaskProjection(Long id) {
        AgentxTaskProjectionDO projection = taskProjectionMapper.selectById(id);
        if (projection == null) {
            return null;
        }
        syncProjectionIfNeeded(projection);
        return projection;
    }

    @Override
    public OpenfangTaskRespDTO getTaskRuntime(String taskRunId) {
        return runtimeBridge.getTaskRun(taskRunId);
    }

    private void syncProjectionIfNeeded(AgentxTaskProjectionDO projection) {
        if (projection == null || projection.getOpenfangTaskRunId() == null || isTerminal(projection.getProjectionStatus())) {
            return;
        }
        try {
            OpenfangTaskRespDTO runtime = runtimeBridge.getTaskRun(projection.getOpenfangTaskRunId());
            orchestrationService.refreshProjection(projection, runtime);
            projection.setProjectionStatus(mapStatus(runtime.getStatus()));
            projection.setResultSummary(runtime.getResultSummary());
            projection.setFailureSummary(runtime.getFailureSummary());
            projection.setAuditSummary(runtime.getAuditSummary());
        } catch (Exception ex) {
            log.warn("[syncProjectionIfNeeded][projectionId={} taskRunId={} sync failed]",
                    projection.getId(), projection.getOpenfangTaskRunId(), ex);
        }
    }

    private boolean isTerminal(Integer status) {
        return AgentxTaskProjectionStatusEnum.SUCCEEDED.getStatus().equals(status)
                || AgentxTaskProjectionStatusEnum.FAILED.getStatus().equals(status)
                || AgentxTaskProjectionStatusEnum.COMPENSATED.getStatus().equals(status);
    }

    private Integer mapStatus(String status) {
        if (status == null) {
            return AgentxTaskProjectionStatusEnum.RUNNING.getStatus();
        }
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

}
