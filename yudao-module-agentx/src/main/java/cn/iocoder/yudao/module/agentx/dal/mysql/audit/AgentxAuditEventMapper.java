package cn.iocoder.yudao.module.agentx.dal.mysql.audit;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.agentx.dal.dataobject.audit.AgentxAuditEventDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgentxAuditEventMapper extends BaseMapperX<AgentxAuditEventDO> {

    default List<AgentxAuditEventDO> selectListByTaskRunId(String taskRunId) {
        return selectList(AgentxAuditEventDO::getOpenfangTaskRunId, taskRunId);
    }

    default List<AgentxAuditEventDO> selectListByBusinessKey(String businessKey) {
        return selectList(AgentxAuditEventDO::getBusinessKey, businessKey);
    }

}
