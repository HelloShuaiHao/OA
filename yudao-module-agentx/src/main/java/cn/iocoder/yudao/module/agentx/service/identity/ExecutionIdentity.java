package cn.iocoder.yudao.module.agentx.service.identity;

import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * 运行时执行主体。
 */
@Data
@Accessors(chain = true)
public class ExecutionIdentity {

    private String principalType;
    private String principalId;
    private Long tenantId;
    private String agentCode;
    private Long delegationGrantId;
    private Boolean delegationActive;
    private Set<AgentxCapability> delegatedCapabilities;

}
