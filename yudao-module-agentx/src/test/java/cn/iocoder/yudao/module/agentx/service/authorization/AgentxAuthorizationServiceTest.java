package cn.iocoder.yudao.module.agentx.service.authorization;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.agentx.service.identity.ExecutionIdentity;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxAuthorizationServiceTest {

    private final AgentxAuthorizationService authorizationService = new AgentxAuthorizationService();

    @Test
    void shouldIntersectAgentDelegationAndScenarioCapabilities() {
        AgentxAuthorizationSnapshot snapshot = authorizationService.calculateEffectiveCapabilities(
                EnumSet.of(AgentxCapability.READ_LEAVE, AgentxCapability.SUBMIT_LEAVE),
                EnumSet.of(AgentxCapability.READ_LEAVE, AgentxCapability.UPDATE_LEAVE),
                EnumSet.of(AgentxCapability.READ_LEAVE, AgentxCapability.SUBMIT_LEAVE)
        );

        assertEquals(EnumSet.of(AgentxCapability.READ_LEAVE), snapshot.getEffectiveCapabilities());
    }

    @Test
    void shouldReturnEmptyWhenAnyDimensionDeniesCapability() {
        AgentxAuthorizationSnapshot snapshot = authorizationService.calculateEffectiveCapabilities(
                EnumSet.of(AgentxCapability.SUBMIT_LEAVE),
                EnumSet.noneOf(AgentxCapability.class),
                EnumSet.of(AgentxCapability.SUBMIT_LEAVE)
        );

        assertTrue(snapshot.getEffectiveCapabilities().isEmpty());
    }

    @Test
    void shouldThrowDelegationExpiredWhenIdentityInactive() {
        ExecutionIdentity identity = new ExecutionIdentity()
                .setDelegationActive(false)
                .setDelegatedCapabilities(EnumSet.of(AgentxCapability.READ_LEAVE));

        ServiceException ex = assertThrows(ServiceException.class, () -> authorizationService.authorize(
                EnumSet.of(AgentxCapability.READ_LEAVE),
                identity,
                EnumSet.of(AgentxCapability.READ_LEAVE),
                EnumSet.of(AgentxCapability.READ_LEAVE)
        ));

        assertEquals(Integer.valueOf(1_024_001_005), ex.getCode());
    }

    @Test
    void shouldThrowCapabilityDeniedWhenRequestedBeyondEffectiveBoundary() {
        ExecutionIdentity identity = new ExecutionIdentity()
                .setDelegationActive(true)
                .setDelegatedCapabilities(EnumSet.of(AgentxCapability.READ_LEAVE));

        ServiceException ex = assertThrows(ServiceException.class, () -> authorizationService.authorize(
                EnumSet.of(AgentxCapability.READ_LEAVE, AgentxCapability.SUBMIT_LEAVE),
                identity,
                EnumSet.of(AgentxCapability.READ_LEAVE, AgentxCapability.SUBMIT_LEAVE),
                EnumSet.of(AgentxCapability.SUBMIT_LEAVE)
        ));

        assertEquals(Integer.valueOf(1_024_001_004), ex.getCode());
        assertTrue(ex.getMessage().contains("SUBMIT_LEAVE"));
    }

}
