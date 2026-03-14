package cn.iocoder.yudao.module.agentx.service.governance;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AgentxToolGovernanceTest {

    @Test
    void shouldDefineToolMetadataStructureAndControlledServiceBoundary() {
        AgentxToolGovernance governance = new AgentxToolGovernance();

        assertEquals(List.of(
                "ToolDescriptor",
                "ToolSchema",
                "ToolPolicy",
                "ToolAdapter"
        ), governance.requiredMetadataTypes());
        assertEquals("service-adapter-not-mapper-or-table", governance.exposureBoundary());
        assertFalse(governance.allowsDirectMapperExposure());
        assertFalse(governance.allowsDirectTableExposure());
    }

}
