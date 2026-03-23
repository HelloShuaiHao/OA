package cn.iocoder.yudao.module.agentx.dal.mysql.agent;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.agentx.controller.admin.agent.vo.AgentxAgentPageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentxAgentMapper extends BaseMapperX<AgentxAgentDO> {

    default PageResult<AgentxAgentDO> selectPage(AgentxAgentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AgentxAgentDO>()
                .likeIfPresent(AgentxAgentDO::getAgentName, reqVO.getAgentName())
                .eqIfPresent(AgentxAgentDO::getDeptId, reqVO.getDeptId())
                .eqIfPresent(AgentxAgentDO::getStatus, reqVO.getStatus())
                .eqIfPresent(AgentxAgentDO::getTemplateType, reqVO.getTemplateType())
                .betweenIfPresent(AgentxAgentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AgentxAgentDO::getId));
    }

    default AgentxAgentDO selectByAgentName(String agentName) {
        return selectOne(AgentxAgentDO::getAgentName, agentName);
    }

    default AgentxAgentDO selectByAgentKey(String agentKey) {
        return selectOne(AgentxAgentDO::getAgentKey, agentKey);
    }

}
