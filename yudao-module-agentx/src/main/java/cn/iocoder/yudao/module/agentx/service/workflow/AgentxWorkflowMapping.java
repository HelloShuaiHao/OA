package cn.iocoder.yudao.module.agentx.service.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 场景到 OpenFang workflow 的映射。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentxWorkflowMapping {

    private String scenarioCode;
    private String openfangWorkflowId;
    private String expectedWorkflowVersion;
    private boolean enabled;

}
