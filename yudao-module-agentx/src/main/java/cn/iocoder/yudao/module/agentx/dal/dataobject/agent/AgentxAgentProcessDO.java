package cn.iocoder.yudao.module.agentx.dal.dataobject.agent;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@TableName("agentx_agent_process")
@KeySequence("agentx_agent_process_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AgentxAgentProcessDO extends BaseDO {

    @TableId
    private Long id;
    private Long agentId;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String processName;
    private Integer processVersion;
    private String selectionMode;
    private String selectionRules;
    private Integer priority;

}
