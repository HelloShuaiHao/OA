package cn.iocoder.yudao.module.agentx.service.governance;

import java.util.List;

/**
 * 定义统一审计事件模型、轨迹归档边界和检索维度。
 */
public class AgentxAuditGovernance {

    public List<String> auditEventCategories() {
        return List.of(
                "task-lifecycle",
                "tool-invocation",
                "approval-action",
                "business-write",
                "authorization-failure"
        );
    }

    public String promptTraceArchiveMode() {
        return "summary-only";
    }

    public String decisionTraceArchiveMode() {
        return "summary-only";
    }

    public List<String> queryDimensions() {
        return List.of(
                "userId",
                "agentId",
                "businessKey",
                "approvalInstanceId",
                "taskStatus",
                "riskLevel"
        );
    }

}
