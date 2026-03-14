package cn.iocoder.yudao.module.agentx.service.tool;

import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxRiskControlServiceTest {

    private final AgentxRiskControlService service = new AgentxRiskControlService();

    @Test
    void shouldMarkSubmitAndUpdateAsHighRiskCapabilities() {
        assertTrue(service.isHighRiskCapability(AgentxCapability.SUBMIT_LEAVE));
        assertTrue(service.isHighRiskCapability(AgentxCapability.UPDATE_LEAVE));
        assertEquals(30, service.resolveRiskLevel(AgentxCapability.SUBMIT_LEAVE));
    }

}
