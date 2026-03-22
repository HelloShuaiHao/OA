package cn.iocoder.yudao.module.agentx.dal.dataobject.agent;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@TableName("agentx_agent")
@KeySequence("agentx_agent_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AgentxAgentDO extends BaseDO {

    @TableId
    private Long id;
    private String agentName;
    private String agentKey;
    private String description;
    private String avatarUrl;
    private Long deptId;
    private String deptName;
    private String templateType;
    private Integer status;
    private Integer configVersion;
    private Integer lastSyncStatus;
    private LocalDateTime lastSyncTime;
    private String lastSyncMessage;

}
