package cn.iocoder.yudao.module.agentx.dal.dataobject.channel;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@TableName("agentx_user_channel_binding")
@KeySequence("agentx_user_channel_binding_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AgentxUserChannelBindingDO extends BaseDO {

    @TableId
    private Long id;
    private Long userId;
    private String channelType;
    private String channelUserId;
    private String channelUsername;
    private LocalDateTime bindTime;
    private LocalDateTime unbindTime;
    private Integer status;

}
