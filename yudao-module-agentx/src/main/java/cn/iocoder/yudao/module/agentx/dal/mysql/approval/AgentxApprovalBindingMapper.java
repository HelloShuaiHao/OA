package cn.iocoder.yudao.module.agentx.dal.mysql.approval;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.agentx.controller.admin.approval.vo.AgentxApprovalBindingPageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AgentxApprovalBindingMapper extends BaseMapperX<AgentxApprovalBindingDO> {

    default cn.iocoder.yudao.framework.common.pojo.PageResult<AgentxApprovalBindingDO> selectPage(AgentxApprovalBindingPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AgentxApprovalBindingDO>()
                .eqIfPresent(AgentxApprovalBindingDO::getScenarioCode, reqVO.getScenarioCode())
                .likeIfPresent(AgentxApprovalBindingDO::getBusinessKey, reqVO.getBusinessKey())
                .eqIfPresent(AgentxApprovalBindingDO::getDecisionStatus, reqVO.getDecisionStatus())
                .eqIfPresent(AgentxApprovalBindingDO::getCallbackFailed, reqVO.getCallbackFailed())
                .betweenIfPresent(AgentxApprovalBindingDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AgentxApprovalBindingDO::getId));
    }

    default AgentxApprovalBindingDO selectByOpenfangApprovalId(String openfangApprovalId) {
        return selectOne(AgentxApprovalBindingDO::getOpenfangApprovalId, openfangApprovalId);
    }

    default List<AgentxApprovalBindingDO> selectListByTaskRunId(String taskRunId) {
        return selectList(AgentxApprovalBindingDO::getOpenfangTaskRunId, taskRunId);
    }

    default List<AgentxApprovalBindingDO> selectListPendingTimeout(LocalDateTime deadline) {
        return selectList(new LambdaQueryWrapperX<AgentxApprovalBindingDO>()
                .isNull(AgentxApprovalBindingDO::getDecisionStatus)
                .lt(AgentxApprovalBindingDO::getCreateTime, deadline)
                .orderByAsc(AgentxApprovalBindingDO::getId));
    }

}
