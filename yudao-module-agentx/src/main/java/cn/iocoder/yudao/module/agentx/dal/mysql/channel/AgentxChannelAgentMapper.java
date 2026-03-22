package cn.iocoder.yudao.module.agentx.dal.mysql.channel;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.agentx.dal.dataobject.channel.AgentxChannelAgentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgentxChannelAgentMapper extends BaseMapperX<AgentxChannelAgentDO> {

    default List<AgentxChannelAgentDO> selectListByChannelId(Long channelId) {
        return selectList(AgentxChannelAgentDO::getChannelId, channelId);
    }

    default void deleteByChannelId(Long channelId) {
        delete(AgentxChannelAgentDO::getChannelId, channelId);
    }

}
