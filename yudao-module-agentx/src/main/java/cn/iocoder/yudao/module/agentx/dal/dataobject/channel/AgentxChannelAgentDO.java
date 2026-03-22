package cn.iocoder.yudao.module.agentx.dal.dataobject.channel;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@TableName("agentx_channel_agent")
@KeySequence("agentx_channel_agent_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AgentxChannelAgentDO extends BaseDO {

    @TableId
    private Long id;
    private Long channelId;
    private Long agentId;
    private Boolean enabled;

}
