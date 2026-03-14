package cn.iocoder.yudao.module.agentx.service.audit;

import java.util.List;

public class AgentxAuditReportProtocol {

    public List<String> getRequiredFields() {
        return List.of("initiatorId", "agentCode", "toolName", "dataScopeSummary", "businessImpactSummary");
    }

    public String getExposureRule() {
        return "summary only";
    }

}
