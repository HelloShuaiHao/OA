package cn.iocoder.yudao.module.agentx.service.task.query;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.task.vo.AgentxTaskProjectionPageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.task.AgentxTaskProjectionMapper;
import cn.iocoder.yudao.module.agentx.framework.openfang.client.OpenfangRuntimeBridge;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangTaskRespDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AgentxTaskQueryServiceImpl implements AgentxTaskQueryService {

    @Resource
    private AgentxTaskProjectionMapper taskProjectionMapper;

    @Resource
    private OpenfangRuntimeBridge runtimeBridge;

    @Override
    public PageResult<AgentxTaskProjectionDO> getTaskProjectionPage(AgentxTaskProjectionPageReqVO pageReqVO) {
        return taskProjectionMapper.selectPage(pageReqVO);
    }

    @Override
    public AgentxTaskProjectionDO getTaskProjection(Long id) {
        return taskProjectionMapper.selectById(id);
    }

    @Override
    public OpenfangTaskRespDTO getTaskRuntime(String taskRunId) {
        return runtimeBridge.getTaskRun(taskRunId);
    }

}
