package cn.iocoder.yudao.module.agentx.dal.mysql.task;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgentxTaskProjectionMapper extends BaseMapperX<AgentxTaskProjectionDO> {

    default AgentxTaskProjectionDO selectByOpenfangTaskRunId(String taskRunId) {
        return selectOne(AgentxTaskProjectionDO::getOpenfangTaskRunId, taskRunId);
    }

    default List<AgentxTaskProjectionDO> selectListByBusinessKey(String businessKey) {
        return selectList(AgentxTaskProjectionDO::getBusinessKey, businessKey);
    }

}
