package cn.iocoder.yudao.module.agentx.dal.mysql.scenario;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.agentx.controller.admin.scenario.vo.AgentxScenarioConfigPageReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.scenario.AgentxScenarioConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentxScenarioConfigMapper extends BaseMapperX<AgentxScenarioConfigDO> {

    default PageResult<AgentxScenarioConfigDO> selectPage(AgentxScenarioConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AgentxScenarioConfigDO>()
                .likeIfPresent(AgentxScenarioConfigDO::getScenarioCode, reqVO.getScenarioCode())
                .likeIfPresent(AgentxScenarioConfigDO::getScenarioName, reqVO.getScenarioName())
                .likeIfPresent(AgentxScenarioConfigDO::getOpenfangWorkflowId, reqVO.getOpenfangWorkflowId())
                .eqIfPresent(AgentxScenarioConfigDO::getEnabled, reqVO.getEnabled())
                .betweenIfPresent(AgentxScenarioConfigDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AgentxScenarioConfigDO::getId));
    }

    default AgentxScenarioConfigDO selectByScenarioCode(String scenarioCode) {
        return selectOne(AgentxScenarioConfigDO::getScenarioCode, scenarioCode);
    }

}
