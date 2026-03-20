package cn.iocoder.yudao.module.agentx.service.scenario;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.scenario.vo.AgentxScenarioConfigPageReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.scenario.vo.AgentxScenarioConfigSaveReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.scenario.AgentxScenarioConfigDO;

import javax.validation.Valid;

/**
 * AgentX 场景配置 Service。
 */
public interface ScenarioConfigService {

    Long createScenarioConfig(@Valid AgentxScenarioConfigSaveReqVO createReqVO);

    void updateScenarioConfig(@Valid AgentxScenarioConfigSaveReqVO updateReqVO);

    void deleteScenarioConfig(Long id);

    AgentxScenarioConfigDO getScenarioConfig(Long id);

    PageResult<AgentxScenarioConfigDO> getScenarioConfigPage(AgentxScenarioConfigPageReqVO pageReqVO);

}
