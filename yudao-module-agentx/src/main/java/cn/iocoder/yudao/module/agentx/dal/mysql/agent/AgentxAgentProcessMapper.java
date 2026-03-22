package cn.iocoder.yudao.module.agentx.dal.mysql.agent;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentProcessDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgentxAgentProcessMapper extends BaseMapperX<AgentxAgentProcessDO> {

    default List<AgentxAgentProcessDO> selectListByAgentId(Long agentId) {
        return selectList(new LambdaQueryWrapperX<AgentxAgentProcessDO>()
                .eq(AgentxAgentProcessDO::getAgentId, agentId)
                .orderByDesc(AgentxAgentProcessDO::getPriority)
                .orderByDesc(AgentxAgentProcessDO::getId));
    }

    default void deleteByAgentId(Long agentId) {
        delete(AgentxAgentProcessDO::getAgentId, agentId);
    }

}
