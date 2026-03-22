package cn.iocoder.yudao.module.agentx.service.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.agent.vo.AgentxAgentDetailRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.agent.vo.AgentxAgentPageReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.agent.vo.AgentxAgentRespVO;
import cn.iocoder.yudao.module.agentx.controller.admin.agent.vo.AgentxAgentSaveReqVO;

import javax.validation.Valid;

public interface AgentxAgentService {

    Long createAgent(@Valid AgentxAgentSaveReqVO reqVO, boolean publish);

    void updateAgent(@Valid AgentxAgentSaveReqVO reqVO);

    void updateAgentStatus(Long id, Integer status);

    void deleteAgent(Long id);

    AgentxAgentDetailRespVO getAgent(Long id);

    PageResult<AgentxAgentRespVO> getAgentPage(AgentxAgentPageReqVO pageReqVO);

    Boolean checkAgentName(Long id, String agentName);

}
