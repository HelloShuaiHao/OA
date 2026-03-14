package cn.iocoder.yudao.module.agentx.service.governance;

import java.util.List;

/**
 * 冻结 Phase 1 的职责边界、真相归属和集成协议前提。
 */
public class AgentxPhase1Governance {

    public List<String> oaResponsibilities() {
        return List.of(
                "organization-truth",
                "approval-decision-truth",
                "existing-bpm-and-business-records"
        );
    }

    public List<String> openfangResponsibilities() {
        return List.of(
                "workflow-definition-truth",
                "task-run-truth",
                "approval-blocking-runtime-projection"
        );
    }

    public List<String> agentxResponsibilities() {
        return List.of(
                "scenario-workflow-mapping",
                "authorization-and-tool-guard",
                "approval-bridge",
                "audit-summary-and-task-projection"
        );
    }

    public String truthOwnerOf(String subject) {
        switch (subject) {
            case "organization":
            case "approval_decision":
                return "oa";
            case "task_run":
            case "pending_approval_ids":
            case "recovery_projection":
            case "workflow_definition":
                return "openfang";
            case "scenario_workflow_mapping":
                return "agentx";
            default:
                throw new IllegalArgumentException("Unknown subject: " + subject);
        }
    }

    public String phase1ProtocolMode() {
        return "REST_ONLY";
    }

    public boolean requiresWebhook() {
        return false;
    }

    public boolean requiresOutbox() {
        return false;
    }

}
