package cn.iocoder.yudao.module.agentx.service.identity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Agent 主体。
 */
@Data
@AllArgsConstructor
public class AgentPrincipal {

    private String agentCode;
    private Long tenantId;
    private String displayName;

}
