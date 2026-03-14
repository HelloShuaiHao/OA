package cn.iocoder.yudao.module.agentx.dal.dataobject.task;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * OA 侧任务投影。
 */
@TableName("agentx_task_projection")
@KeySequence("agentx_task_projection_seq")
@Data
public class AgentxTaskProjectionDO extends BaseDO {

    @TableId
    private Long id;
    private String scenarioCode;
    private String businessKey;
    private String idempotencyKey;
    private String openfangTaskRunId;
    private Integer projectionStatus;
    private Integer riskLevel;
    private String resultSummary;
    private String failureSummary;
    private String auditSummary;

}
