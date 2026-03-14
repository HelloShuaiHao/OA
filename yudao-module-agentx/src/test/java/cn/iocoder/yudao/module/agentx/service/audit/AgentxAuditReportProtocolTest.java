package cn.iocoder.yudao.module.agentx.service.audit;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxAuditReportProtocolTest {

    @Test
    void shouldDefineAuditReportDimensionsForActorDelegateToolScopeAndWriteAction() {
        AgentxAuditReportProtocol protocol = new AgentxAuditReportProtocol();

        assertEquals(List.of("initiatorId", "agentCode", "toolName", "dataScopeSummary", "businessImpactSummary"),
                protocol.getRequiredFields());
        assertEquals("summary only", protocol.getExposureRule());
    }

}
