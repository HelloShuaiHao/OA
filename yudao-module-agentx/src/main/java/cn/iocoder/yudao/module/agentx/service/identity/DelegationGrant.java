package cn.iocoder.yudao.module.agentx.service.identity;

import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 委托授权。
 */
@Data
@AllArgsConstructor
public class DelegationGrant {

    private Long grantId;
    private Long delegatorUserId;
    private String agentCode;
    private Set<AgentxCapability> delegatedCapabilities;
    private LocalDateTime expireTime;
    private Boolean enabled;

}
