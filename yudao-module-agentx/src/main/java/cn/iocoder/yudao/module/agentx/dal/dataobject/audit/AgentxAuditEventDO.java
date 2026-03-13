package cn.iocoder.yudao.module.agentx.dal.dataobject.audit;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * AgentX 审计事件。
 */
@TableName("agentx_audit_event")
@KeySequence("agentx_audit_event_seq")
@Data
@Accessors(chain = true)
public class AgentxAuditEventDO extends BaseDO {

    @TableId
    private Long id;
    private Long tenantId;
    private String initiatorId;
    private String agentCode;
    private String eventType;
    private String scenarioCode;
    private String businessKey;
    private String openfangTaskRunId;
    private String openfangApprovalId;
    private String toolName;
    private Integer riskLevel;
    private String dataScopeSummary;
    private String requestSummary;
    private Long durationMs;
    private Integer errorCode;
    private String businessImpactSummary;
    private String resultSummary;

}
