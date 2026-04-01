package cn.iocoder.yudao.module.agentx.dal.mysql.accessaudit;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.agentx.controller.admin.accessaudit.vo.AgentxAccessAuditPageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.accessaudit.AgentxAccessAuditDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentxAccessAuditMapper extends BaseMapperX<AgentxAccessAuditDO> {

    default PageResult<AgentxAccessAuditDO> selectPage(AgentxAccessAuditPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AgentxAccessAuditDO>()
                .eqIfPresent(AgentxAccessAuditDO::getUserId, reqVO.getUserId())
                .eqIfPresent(AgentxAccessAuditDO::getAgentId, reqVO.getAgentId())
                .eqIfPresent(AgentxAccessAuditDO::getDecision, reqVO.getDecision())
                .geIfPresent(AgentxAccessAuditDO::getRequestTime, reqVO.getStartTime())
                .leIfPresent(AgentxAccessAuditDO::getRequestTime, reqVO.getEndTime())
                .orderByDesc(AgentxAccessAuditDO::getId));
    }

}
