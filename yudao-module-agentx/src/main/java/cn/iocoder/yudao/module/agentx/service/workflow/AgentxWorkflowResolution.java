package cn.iocoder.yudao.module.agentx.service.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * workflow 映射解析结果。
 */
@Data
@AllArgsConstructor
public class AgentxWorkflowResolution {

    private String scenarioCode;
    private String openfangWorkflowId;
    private String expectedWorkflowVersion;

}
