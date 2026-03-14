package cn.iocoder.yudao.module.agentx.service.authorization;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.agentx.service.identity.ExecutionIdentity;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.AUTHORIZATION_DENIED;
import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.DELEGATION_EXPIRED;

/**
 * AgentX 授权服务。
 */
public class AgentxAuthorizationService {

    public AgentxAuthorizationSnapshot calculateEffectiveCapabilities(Set<AgentxCapability> agentCapabilities,
                                                                     Set<AgentxCapability> delegationCapabilities,
                                                                     Set<AgentxCapability> scenarioCapabilities) {
        EnumSet<AgentxCapability> result = agentCapabilities.isEmpty()
                ? EnumSet.noneOf(AgentxCapability.class)
                : EnumSet.copyOf(agentCapabilities);
        result.retainAll(delegationCapabilities);
        result.retainAll(scenarioCapabilities);
        return new AgentxAuthorizationSnapshot(result);
    }

    public AgentxAuthorizationSnapshot authorize(Set<AgentxCapability> agentCapabilities,
                                                 ExecutionIdentity executionIdentity,
                                                 Set<AgentxCapability> scenarioCapabilities,
                                                 Set<AgentxCapability> requestedCapabilities) {
        if (!Boolean.TRUE.equals(executionIdentity.getDelegationActive())) {
            throw ServiceExceptionUtil.exception(DELEGATION_EXPIRED);
        }
        Set<AgentxCapability> delegated = executionIdentity.getDelegatedCapabilities() != null
                ? executionIdentity.getDelegatedCapabilities() : Collections.emptySet();
        AgentxAuthorizationSnapshot snapshot = calculateEffectiveCapabilities(agentCapabilities, delegated, scenarioCapabilities);
        if (!snapshot.getEffectiveCapabilities().containsAll(requestedCapabilities)) {
            EnumSet<AgentxCapability> missing = requestedCapabilities.isEmpty()
                    ? EnumSet.noneOf(AgentxCapability.class)
                    : EnumSet.copyOf(requestedCapabilities);
            missing.removeAll(snapshot.getEffectiveCapabilities());
            throw ServiceExceptionUtil.exception(AUTHORIZATION_DENIED, missing);
        }
        return snapshot;
    }

}
