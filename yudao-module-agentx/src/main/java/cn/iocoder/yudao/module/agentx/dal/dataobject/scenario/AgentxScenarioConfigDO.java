package cn.iocoder.yudao.module.agentx.dal.dataobject.scenario;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * AgentX 场景配置。
 */
@TableName(value = "agentx_scenario_config", autoResultMap = true)
@KeySequence("agentx_scenario_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AgentxScenarioConfigDO extends BaseDO {

    @TableId
    private Long id;
    private String scenarioCode;
    private String scenarioName;
    private String openfangWorkflowId;
    private String workflowVersion;
    private Integer enabled;
    private String config;

}
