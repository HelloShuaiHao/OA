package cn.iocoder.yudao.module.agentx.dal.mysql.agent;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentCapabilityDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AgentxAgentCapabilityMapper extends BaseMapperX<AgentxAgentCapabilityDO> {

    default List<AgentxAgentCapabilityDO> selectListByAgentId(Long agentId) {
        return selectList(AgentxAgentCapabilityDO::getAgentId, agentId);
    }

    default void deleteByAgentId(Long agentId) {
        delete(AgentxAgentCapabilityDO::getAgentId, agentId);
    }

    @Update("DELETE FROM agentx_agent_capability WHERE agent_id = #{agentId}")
    void deleteForceByAgentId(@Param("agentId") Long agentId);

}
