package cn.iocoder.yudao.module.agentx.service.approval;

public class AgentxApprovalBpmMapper {

    public AgentxBpmApprovalCreateReq toCreateRequest(AgentxApprovalRequest request) {
        return new AgentxBpmApprovalCreateReq()
                .setTitle(request.getTitle())
                .setSummary(buildSummary(request))
                .setApproverSource(request.getApproverSource())
                .setApproverRef(request.getApproverRef())
                .setScenarioCode(request.getScenarioCode())
                .setBusinessKey(request.getBusinessKey())
                .setTaskRunId(request.getOpenfangTaskRunId())
                .setApprovalId(request.getOpenfangApprovalId())
                .setRiskLevel(request.getRiskLevel())
                .setRequesterId(request.getRequesterId());
    }

    private String buildSummary(AgentxApprovalRequest request) {
        if (request.getActionSummary() == null) {
            return request.getReason();
        }
        if (request.getReason() == null) {
            return request.getActionSummary();
        }
        return request.getActionSummary() + " | " + request.getReason();
    }

}
