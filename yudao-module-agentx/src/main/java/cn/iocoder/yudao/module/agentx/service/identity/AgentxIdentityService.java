package cn.iocoder.yudao.module.agentx.service.identity;

import java.time.LocalDateTime;

/**
 * AgentX 身份服务。
 */
public class AgentxIdentityService {

    public ExecutionIdentity createExecutionIdentity(HumanPrincipal human, AgentPrincipal agent, DelegationGrant grant) {
        boolean active = Boolean.TRUE.equals(grant.getEnabled())
                && (grant.getExpireTime() == null || grant.getExpireTime().isAfter(LocalDateTime.now()));
        return new ExecutionIdentity()
                .setPrincipalType("human")
                .setPrincipalId(String.valueOf(human.getUserId()))
                .setTenantId(human.getTenantId())
                .setAgentCode(agent.getAgentCode())
                .setDelegationGrantId(grant.getGrantId())
                .setDelegationActive(active)
                .setDelegatedCapabilities(grant.getDelegatedCapabilities());
    }

}
