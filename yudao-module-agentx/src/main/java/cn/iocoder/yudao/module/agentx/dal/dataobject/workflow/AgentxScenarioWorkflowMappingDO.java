package cn.iocoder.yudao.module.agentx.dal.dataobject.workflow;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 场景到 OpenFang workflow 的映射配置。
 */
@TableName("agentx_scenario_workflow_mapping")
@KeySequence("agentx_scenario_workflow_mapping_seq")
@Data
public class AgentxScenarioWorkflowMappingDO extends BaseDO {

    @TableId
    private Long id;
    private String scenarioCode;
    private String openfangWorkflowId;
    private String expectedWorkflowVersion;
    private Integer status;
    private String remark;

}
