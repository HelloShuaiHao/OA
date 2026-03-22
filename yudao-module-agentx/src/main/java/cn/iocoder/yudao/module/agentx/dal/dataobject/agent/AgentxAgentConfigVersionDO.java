package cn.iocoder.yudao.module.agentx.dal.dataobject.agent;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@TableName("agentx_agent_config_version")
@KeySequence("agentx_agent_config_version_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AgentxAgentConfigVersionDO extends BaseDO {

    @TableId
    private Long id;
    private Long agentId;
    private Integer versionNo;
    private String snapshot;
    private Integer syncStatus;
    private String syncMessage;
    private LocalDateTime syncTime;

}
