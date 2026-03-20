package cn.iocoder.yudao.module.agentx.dal.mysql.instance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.agentx.controller.admin.instance.vo.AgentxOpenfangInstancePageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.instance.AgentxOpenfangInstanceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgentxOpenfangInstanceMapper extends BaseMapperX<AgentxOpenfangInstanceDO> {

    default PageResult<AgentxOpenfangInstanceDO> selectPage(AgentxOpenfangInstancePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AgentxOpenfangInstanceDO>()
                .likeIfPresent(AgentxOpenfangInstanceDO::getInstanceName, reqVO.getInstanceName())
                .likeIfPresent(AgentxOpenfangInstanceDO::getEndpoint, reqVO.getEndpoint())
                .eqIfPresent(AgentxOpenfangInstanceDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(AgentxOpenfangInstanceDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AgentxOpenfangInstanceDO::getId));
    }

    default AgentxOpenfangInstanceDO selectByInstanceName(String instanceName) {
        return selectOne(AgentxOpenfangInstanceDO::getInstanceName, instanceName);
    }

    default List<AgentxOpenfangInstanceDO> selectListByStatus(Integer status) {
        return selectList(AgentxOpenfangInstanceDO::getStatus, status);
    }

}
