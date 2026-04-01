package cn.iocoder.yudao.module.agentx.dal.dataobject.accessaudit;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@TableName("agentx_access_audit")
@KeySequence("agentx_access_audit_seq")
@Data
@Accessors(chain = true)
public class AgentxAccessAuditDO {

    @TableId
    private Long id;
    private Long tenantId;
    private String decisionId;
    private Long userId;
    private String channelUserId;
    private String agentId;
    private String conversationScope;
    private String action;
    private String resourceType;
    private String resourceId;
    private String decision;
    private String denyReason;
    private String policyVersion;
    private LocalDateTime requestTime;

}
