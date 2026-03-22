package cn.iocoder.yudao.module.agentx.dal.dataobject.channel;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@TableName("agentx_channel_config")
@KeySequence("agentx_channel_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AgentxChannelConfigDO extends BaseDO {

    @TableId
    private Long id;
    private String channelType;
    private String channelName;
    private String botTokenEncrypted;
    private String config;
    private Integer status;

}
