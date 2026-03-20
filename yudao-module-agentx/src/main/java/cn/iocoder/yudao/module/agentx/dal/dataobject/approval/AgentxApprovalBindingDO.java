package cn.iocoder.yudao.module.agentx.dal.dataobject.approval;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * OA 审批与 OpenFang gate 的绑定关系。
 */
@TableName("agentx_approval_binding")
@KeySequence("agentx_approval_binding_seq")
@Data
public class AgentxApprovalBindingDO extends BaseDO {

    @TableId
    private Long id;
    private String scenarioCode;
    private String businessKey;
    private String openfangTaskRunId;
    private String openfangApprovalId;
    private String bpmProcessInstanceId;
    private Integer riskLevel;
    private Integer decisionStatus;
    private Integer callbackRetryCount;
    private Boolean callbackFailed;
    private String callbackLastError;
    private String actionSummary;

}
