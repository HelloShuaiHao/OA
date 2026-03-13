package cn.iocoder.yudao.module.agentx.service.audit;

import cn.iocoder.yudao.module.agentx.dal.dataobject.audit.AgentxAuditEventDO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxRiskAlertServiceTest {

    @Test
    void shouldRaiseAlertsForAuthorizationFailureDuplicateWriteAndRetryStorm() {
        AgentxRiskAlertService service = new AgentxRiskAlertService();

        List<String> alerts = service.evaluate(List.of(
                new AgentxAuditEventDO().setEventType("AUTHORIZATION_DENIED").setBusinessKey("leave:1"),
                new AgentxAuditEventDO().setEventType("WRITE_DUPLICATED").setBusinessKey("leave:1"),
                new AgentxAuditEventDO().setEventType("WRITE_RETRY").setBusinessKey("leave:1"),
                new AgentxAuditEventDO().setEventType("WRITE_RETRY").setBusinessKey("leave:1"),
                new AgentxAuditEventDO().setEventType("WRITE_RETRY").setBusinessKey("leave:1")
        ));

        assertEquals(List.of("AUTHORIZATION_DENIED", "DUPLICATE_WRITE", "RETRY_STORM"), alerts);
    }

}
