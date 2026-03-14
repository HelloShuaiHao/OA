package cn.iocoder.yudao.module.agentx.service.tool;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Tool 调用前校验结果。
 */
@Data
@AllArgsConstructor
public class AgentxToolGuardResult {

    private boolean authorized;
    private boolean approvalRequired;

}
