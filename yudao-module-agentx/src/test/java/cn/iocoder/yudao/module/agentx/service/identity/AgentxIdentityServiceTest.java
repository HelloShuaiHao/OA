package cn.iocoder.yudao.module.agentx.service.identity;

import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxIdentityServiceTest {

    private final AgentxIdentityService identityService = new AgentxIdentityService();

    @Test
    void shouldBuildExecutionIdentityFromGrant() {
        HumanPrincipal human = new HumanPrincipal(100L, 1L, "张三");
        AgentPrincipal agent = new AgentPrincipal("leave-agent", 1L, "请假审批助理");
        DelegationGrant grant = new DelegationGrant(9L, human.getUserId(), agent.getAgentCode(),
                EnumSet.of(AgentxCapability.READ_LEAVE, AgentxCapability.SUBMIT_LEAVE),
                LocalDateTime.now().plusHours(2), true);

        ExecutionIdentity identity = identityService.createExecutionIdentity(human, agent, grant);

        assertEquals("human", identity.getPrincipalType());
        assertEquals("100", identity.getPrincipalId());
        assertEquals("leave-agent", identity.getAgentCode());
        assertEquals(EnumSet.of(AgentxCapability.READ_LEAVE, AgentxCapability.SUBMIT_LEAVE),
                identity.getDelegatedCapabilities());
    }

    @Test
    void shouldMarkExpiredGrantAsInactive() {
        HumanPrincipal human = new HumanPrincipal(100L, 1L, "张三");
        AgentPrincipal agent = new AgentPrincipal("leave-agent", 1L, "请假审批助理");
        DelegationGrant grant = new DelegationGrant(9L, human.getUserId(), agent.getAgentCode(),
                EnumSet.of(AgentxCapability.READ_LEAVE),
                LocalDateTime.now().minusMinutes(1), true);

        ExecutionIdentity identity = identityService.createExecutionIdentity(human, agent, grant);

        assertEquals(Boolean.FALSE, identity.getDelegationActive());
    }

}
