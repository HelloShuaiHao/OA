package cn.iocoder.yudao.module.agentx.dal.mysql.approval;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.agentx.dal.dataobject.approval.AgentxApprovalBindingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AgentxApprovalBindingMapper extends BaseMapperX<AgentxApprovalBindingDO> {

    default AgentxApprovalBindingDO selectByOpenfangApprovalId(String openfangApprovalId) {
        return selectOne(AgentxApprovalBindingDO::getOpenfangApprovalId, openfangApprovalId);
    }

    default List<AgentxApprovalBindingDO> selectListByTaskRunId(String taskRunId) {
        return selectList(AgentxApprovalBindingDO::getOpenfangTaskRunId, taskRunId);
    }

}
