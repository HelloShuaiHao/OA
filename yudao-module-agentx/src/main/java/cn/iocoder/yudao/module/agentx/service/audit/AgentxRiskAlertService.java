package cn.iocoder.yudao.module.agentx.service.audit;

import cn.iocoder.yudao.module.agentx.dal.dataobject.audit.AgentxAuditEventDO;

import java.util.ArrayList;
import java.util.List;

public class AgentxRiskAlertService {

    public List<String> evaluate(List<AgentxAuditEventDO> events) {
        List<String> alerts = new ArrayList<>();
        long retryCount = events.stream().filter(event -> "WRITE_RETRY".equals(event.getEventType())).count();
        if (events.stream().anyMatch(event -> "AUTHORIZATION_DENIED".equals(event.getEventType()))) {
            alerts.add("AUTHORIZATION_DENIED");
        }
        if (events.stream().anyMatch(event -> "WRITE_DUPLICATED".equals(event.getEventType()))) {
            alerts.add("DUPLICATE_WRITE");
        }
        if (retryCount >= 3) {
            alerts.add("RETRY_STORM");
        }
        return alerts;
    }

}
