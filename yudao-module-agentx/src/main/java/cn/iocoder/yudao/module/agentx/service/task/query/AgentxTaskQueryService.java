package cn.iocoder.yudao.module.agentx.service.task.query;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.task.vo.AgentxTaskProjectionPageReqVO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangTaskRespDTO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;

public interface AgentxTaskQueryService {

    PageResult<AgentxTaskProjectionDO> getTaskProjectionPage(AgentxTaskProjectionPageReqVO pageReqVO);

    AgentxTaskProjectionDO getTaskProjection(Long id);

    OpenfangTaskRespDTO getTaskRuntime(String taskRunId);

}
