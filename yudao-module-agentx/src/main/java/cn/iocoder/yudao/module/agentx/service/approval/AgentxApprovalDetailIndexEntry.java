package cn.iocoder.yudao.module.agentx.service.approval;

import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangApprovalDetailRespDTO;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AgentxApprovalDetailIndexEntry {

    private Long tenantId;
    private String taskRunId;
    private String approvalId;
    private OpenfangApprovalDetailRespDTO detail;

}
