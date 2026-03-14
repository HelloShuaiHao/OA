package cn.iocoder.yudao.module.agentx.dal.mysql.workflow;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.agentx.dal.dataobject.workflow.AgentxScenarioWorkflowMappingDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface AgentxScenarioWorkflowMappingMapper extends BaseMapperX<AgentxScenarioWorkflowMappingDO> {

    default AgentxScenarioWorkflowMappingDO selectByScenarioCode(String scenarioCode) {
        return selectOne(AgentxScenarioWorkflowMappingDO::getScenarioCode, scenarioCode);
    }

    default List<AgentxScenarioWorkflowMappingDO> selectListByScenarioCodes(Collection<String> scenarioCodes) {
        return selectList(AgentxScenarioWorkflowMappingDO::getScenarioCode, scenarioCodes);
    }

}
