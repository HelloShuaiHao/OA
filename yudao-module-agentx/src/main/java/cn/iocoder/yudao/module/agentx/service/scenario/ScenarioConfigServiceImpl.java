package cn.iocoder.yudao.module.agentx.service.scenario;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.agentx.controller.admin.scenario.vo.AgentxScenarioConfigPageReqVO;
import cn.iocoder.yudao.module.agentx.controller.admin.scenario.vo.AgentxScenarioConfigSaveReqVO;
import cn.iocoder.yudao.module.agentx.dal.dataobject.scenario.AgentxScenarioConfigDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.scenario.AgentxScenarioConfigMapper;
import cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * AgentX 场景配置 Service 实现类。
 */
@Service
@Validated
public class ScenarioConfigServiceImpl implements ScenarioConfigService {

    @Resource
    private AgentxScenarioConfigMapper scenarioConfigMapper;

    @Override
    public Long createScenarioConfig(AgentxScenarioConfigSaveReqVO createReqVO) {
        validateScenarioCodeUnique(null, createReqVO.getScenarioCode());
        AgentxScenarioConfigDO config = new AgentxScenarioConfigDO()
                .setScenarioCode(createReqVO.getScenarioCode())
                .setScenarioName(createReqVO.getScenarioName())
                .setOpenfangWorkflowId(createReqVO.getOpenfangWorkflowId())
                .setWorkflowVersion(createReqVO.getWorkflowVersion())
                .setEnabled(createReqVO.getEnabled())
                .setConfig(createReqVO.getConfig());
        scenarioConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    public void updateScenarioConfig(AgentxScenarioConfigSaveReqVO updateReqVO) {
        validateScenarioConfigExists(updateReqVO.getId());
        validateScenarioCodeUnique(updateReqVO.getId(), updateReqVO.getScenarioCode());
        AgentxScenarioConfigDO updateObj = new AgentxScenarioConfigDO()
                .setId(updateReqVO.getId())
                .setScenarioCode(updateReqVO.getScenarioCode())
                .setScenarioName(updateReqVO.getScenarioName())
                .setOpenfangWorkflowId(updateReqVO.getOpenfangWorkflowId())
                .setWorkflowVersion(updateReqVO.getWorkflowVersion())
                .setEnabled(updateReqVO.getEnabled())
                .setConfig(updateReqVO.getConfig());
        scenarioConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteScenarioConfig(Long id) {
        validateScenarioConfigExists(id);
        scenarioConfigMapper.deleteById(id);
    }

    @Override
    public AgentxScenarioConfigDO getScenarioConfig(Long id) {
        return scenarioConfigMapper.selectById(id);
    }

    @Override
    public PageResult<AgentxScenarioConfigDO> getScenarioConfigPage(AgentxScenarioConfigPageReqVO pageReqVO) {
        return scenarioConfigMapper.selectPage(pageReqVO);
    }

    private void validateScenarioConfigExists(Long id) {
        if (scenarioConfigMapper.selectById(id) == null) {
            throw exception(ErrorCodeConstants.SCENARIO_CONFIG_NOT_EXISTS);
        }
    }

    private void validateScenarioCodeUnique(Long id, String scenarioCode) {
        AgentxScenarioConfigDO config = scenarioConfigMapper.selectByScenarioCode(scenarioCode);
        if (config == null) {
            return;
        }
        if (id == null || !ObjectUtil.equal(id, config.getId())) {
            throw exception(ErrorCodeConstants.SCENARIO_CONFIG_CODE_DUPLICATED);
        }
    }

}
