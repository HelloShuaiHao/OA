package cn.iocoder.yudao.module.agentx.dal.mysql.channel;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.agentx.controller.admin.channel.vo.AgentxChannelConfigPageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.channel.AgentxChannelConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface AgentxChannelConfigMapper extends BaseMapperX<AgentxChannelConfigDO> {

    default PageResult<AgentxChannelConfigDO> selectPage(AgentxChannelConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AgentxChannelConfigDO>()
                .eqIfPresent(AgentxChannelConfigDO::getChannelType, reqVO.getChannelType())
                .likeIfPresent(AgentxChannelConfigDO::getChannelName, reqVO.getChannelName())
                .eqIfPresent(AgentxChannelConfigDO::getStatus, reqVO.getStatus())
                .orderByDesc(AgentxChannelConfigDO::getId));
    }

    default List<AgentxChannelConfigDO> selectListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return selectBatchIds(ids);
    }

    default List<AgentxChannelConfigDO> selectListByChannelType(String channelType) {
        return selectList(AgentxChannelConfigDO::getChannelType, channelType);
    }

}
