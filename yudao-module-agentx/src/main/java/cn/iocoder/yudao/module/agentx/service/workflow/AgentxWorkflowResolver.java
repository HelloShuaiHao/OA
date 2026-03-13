package cn.iocoder.yudao.module.agentx.service.workflow;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;

import java.util.List;

import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.SCENARIO_DISABLED;
import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.SCENARIO_WORKFLOW_MAPPING_NOT_EXISTS;
import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.WORKFLOW_VERSION_MISMATCH;

/**
 * AgentX workflow 解析器。
 */
public class AgentxWorkflowResolver {

    public AgentxWorkflowResolution resolve(String scenarioCode, String actualWorkflowVersion,
                                            List<AgentxWorkflowMapping> mappings) {
        AgentxWorkflowMapping mapping = mappings.stream()
                .filter(item -> StrUtil.equals(item.getScenarioCode(), scenarioCode))
                .findFirst()
                .orElseThrow(() -> ServiceExceptionUtil.exception(SCENARIO_WORKFLOW_MAPPING_NOT_EXISTS, scenarioCode));
        if (!mapping.isEnabled()) {
            throw ServiceExceptionUtil.exception(SCENARIO_DISABLED);
        }
        if (StrUtil.isNotBlank(actualWorkflowVersion)
                && ObjectUtil.notEqual(mapping.getExpectedWorkflowVersion(), actualWorkflowVersion)) {
            throw ServiceExceptionUtil.exception(WORKFLOW_VERSION_MISMATCH,
                    mapping.getExpectedWorkflowVersion(), actualWorkflowVersion);
        }
        return new AgentxWorkflowResolution(mapping.getScenarioCode(), mapping.getOpenfangWorkflowId(),
                mapping.getExpectedWorkflowVersion());
    }

}
