package cn.iocoder.yudao.module.agentx.service.governance;

import java.util.List;

/**
 * 定义 Phase 1 的 Agent 身份、委托执行、Tool 权限边界和高风险能力清单。
 */
public class AgentxAuthorizationGovernance {

    public String agentIdentityModel() {
        return "service-principal-not-human-user";
    }

    public List<String> delegationGrantFields() {
        return List.of(
                "delegatorUserId",
                "agentId",
                "delegatedCapabilities",
                "dataScope",
                "expiresAt",
                "revokedAt",
                "auditTraceId"
        );
    }

    public String toolPermissionBoundary() {
        return "tool-capability-not-menu-visibility";
    }

    public List<String> highRiskCapabilities() {
        return List.of(
                "submit-approval",
                "modify-business-record",
                "create-business-record",
                "trigger-external-notification"
        );
    }

}
