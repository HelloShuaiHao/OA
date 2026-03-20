package cn.iocoder.yudao.module.agentx.dal.mysql.task;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.agentx.controller.admin.task.vo.AgentxTaskProjectionPageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.task.AgentxTaskProjectionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgentxTaskProjectionMapper extends BaseMapperX<AgentxTaskProjectionDO> {

    default cn.iocoder.yudao.framework.common.pojo.PageResult<AgentxTaskProjectionDO> selectPage(AgentxTaskProjectionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AgentxTaskProjectionDO>()
                .eqIfPresent(AgentxTaskProjectionDO::getScenarioCode, reqVO.getScenarioCode())
                .likeIfPresent(AgentxTaskProjectionDO::getBusinessKey, reqVO.getBusinessKey())
                .eqIfPresent(AgentxTaskProjectionDO::getProjectionStatus, reqVO.getProjectionStatus())
                .betweenIfPresent(AgentxTaskProjectionDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AgentxTaskProjectionDO::getId));
    }

    default AgentxTaskProjectionDO selectByOpenfangTaskRunId(String taskRunId) {
        return selectOne(AgentxTaskProjectionDO::getOpenfangTaskRunId, taskRunId);
    }

    default AgentxTaskProjectionDO selectByIdempotencyKey(String idempotencyKey) {
        return selectOne(AgentxTaskProjectionDO::getIdempotencyKey, idempotencyKey);
    }

    default List<AgentxTaskProjectionDO> selectListByBusinessKey(String businessKey) {
        return selectList(AgentxTaskProjectionDO::getBusinessKey, businessKey);
    }

}
