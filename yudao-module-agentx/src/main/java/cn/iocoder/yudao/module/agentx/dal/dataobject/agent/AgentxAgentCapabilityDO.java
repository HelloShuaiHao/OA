package cn.iocoder.yudao.module.agentx.dal.dataobject.agent;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@TableName("agentx_agent_capability")
@KeySequence("agentx_agent_capability_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AgentxAgentCapabilityDO extends BaseDO {

    @TableId
    private Long id;
    private Long agentId;
    private String capabilityKey;
    private String capabilityName;
    private Boolean enabled;
    private Integer maxCallsPerHour;
    private String conditions;

}
