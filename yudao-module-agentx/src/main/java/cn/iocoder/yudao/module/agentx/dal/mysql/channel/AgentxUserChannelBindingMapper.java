package cn.iocoder.yudao.module.agentx.dal.mysql.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxUserChannelBindingPageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.channel.AgentxUserChannelBindingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgentxUserChannelBindingMapper extends BaseMapperX<AgentxUserChannelBindingDO> {

    default AgentxUserChannelBindingDO selectByChannelIdentity(String channelType, String channelUserId) {
        return selectOne(new LambdaQueryWrapperX<AgentxUserChannelBindingDO>()
                .eq(AgentxUserChannelBindingDO::getChannelType, channelType)
                .eq(AgentxUserChannelBindingDO::getChannelUserId, channelUserId)
                .eq(AgentxUserChannelBindingDO::getStatus, 1)
                .orderByDesc(AgentxUserChannelBindingDO::getId)
                .last("LIMIT 1"));
    }

    default List<AgentxUserChannelBindingDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<AgentxUserChannelBindingDO>()
                .eq(AgentxUserChannelBindingDO::getUserId, userId)
                .eq(AgentxUserChannelBindingDO::getStatus, 1)
                .orderByDesc(AgentxUserChannelBindingDO::getId));
    }

    default PageResult<AgentxUserChannelBindingDO> selectPage(AgentxUserChannelBindingPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AgentxUserChannelBindingDO>()
                .eqIfPresent(AgentxUserChannelBindingDO::getUserId, reqVO.getUserId())
                .eqIfPresent(AgentxUserChannelBindingDO::getChannelType, reqVO.getChannelType())
                .eqIfPresent(AgentxUserChannelBindingDO::getStatus, reqVO.getStatus())
                .orderByDesc(AgentxUserChannelBindingDO::getId));
    }

}
