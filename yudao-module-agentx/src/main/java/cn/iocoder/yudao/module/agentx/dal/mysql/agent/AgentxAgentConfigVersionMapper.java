package cn.iocoder.yudao.module.agentx.dal.mysql.agent;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentConfigVersionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentxAgentConfigVersionMapper extends BaseMapperX<AgentxAgentConfigVersionDO> {

    default AgentxAgentConfigVersionDO selectLatestByAgentId(Long agentId) {
        return selectOne(new LambdaQueryWrapperX<AgentxAgentConfigVersionDO>()
                .eq(AgentxAgentConfigVersionDO::getAgentId, agentId)
                .orderByDesc(AgentxAgentConfigVersionDO::getVersionNo)
                .last("LIMIT 1"));
    }

}
