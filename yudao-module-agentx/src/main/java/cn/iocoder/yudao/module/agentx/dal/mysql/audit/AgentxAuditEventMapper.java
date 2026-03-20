package cn.iocoder.yudao.module.agentx.dal.mysql.audit;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.agentx.controller.admin.audit.vo.AgentxAuditEventPageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.audit.AgentxAuditEventDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AgentxAuditEventMapper extends BaseMapperX<AgentxAuditEventDO> {

    default cn.iocoder.yudao.framework.common.pojo.PageResult<AgentxAuditEventDO> selectPage(AgentxAuditEventPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AgentxAuditEventDO>()
                .eqIfPresent(AgentxAuditEventDO::getEventType, reqVO.getEventType())
                .eqIfPresent(AgentxAuditEventDO::getScenarioCode, reqVO.getScenarioCode())
                .likeIfPresent(AgentxAuditEventDO::getBusinessKey, reqVO.getBusinessKey())
                .eqIfPresent(AgentxAuditEventDO::getToolName, reqVO.getToolName())
                .eqIfPresent(AgentxAuditEventDO::getOpenfangTaskRunId, reqVO.getOpenfangTaskRunId())
                .eqIfPresent(AgentxAuditEventDO::getErrorCode, reqVO.getErrorCode())
                .betweenIfPresent(AgentxAuditEventDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AgentxAuditEventDO::getId));
    }

    default List<AgentxAuditEventDO> selectListByTaskRunId(String taskRunId) {
        return selectList(AgentxAuditEventDO::getOpenfangTaskRunId, taskRunId);
    }

    default List<AgentxAuditEventDO> selectListByBusinessKey(String businessKey) {
        return selectList(AgentxAuditEventDO::getBusinessKey, businessKey);
    }

    default int deleteByCreateTimeBefore(LocalDateTime deadline) {
        return delete(new LambdaQueryWrapperX<AgentxAuditEventDO>()
                .lt(AgentxAuditEventDO::getCreateTime, deadline));
    }

}
