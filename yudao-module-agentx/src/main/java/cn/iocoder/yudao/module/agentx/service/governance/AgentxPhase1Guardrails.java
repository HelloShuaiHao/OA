package cn.iocoder.yudao.module.agentx.service.governance;

import java.util.List;

/**
 * 定义 Phase 1 的禁止事项、技术边界和部署边界。
 */
public class AgentxPhase1Guardrails {

    public List<String> forbiddenActions() {
        return List.of(
                "forbid-openfang-direct-oa-db-access",
                "forbid-scenario-owned-permission-system",
                "forbid-dual-approval-state-machines"
        );
    }

    public String primaryImplementationLanguage() {
        return "java";
    }

    public String rustRole() {
        return "not-in-core-phase1";
    }

    public String oaDeploymentMode() {
        return "in-existing-oa-modules";
    }

    public String openfangDeploymentMode() {
        return "standalone-rest-peer";
    }

}
