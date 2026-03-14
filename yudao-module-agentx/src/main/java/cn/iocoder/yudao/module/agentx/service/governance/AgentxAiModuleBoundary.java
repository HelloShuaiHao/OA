package cn.iocoder.yudao.module.agentx.service.governance;

import java.util.List;

/**
 * 定义 yudao-module-ai 与 agentx 集成层的职责边界，避免重复建设。
 */
public class AgentxAiModuleBoundary {

    public List<String> aiOwnedCapabilities() {
        return List.of(
                "ai.workflow.catalog-crud",
                "ai.workflow.test",
                "ai.tool.catalog-crud"
        );
    }

    public List<String> agentxOwnedCapabilities() {
        return List.of(
                "agentx.scenario.workflow-mapping",
                "agentx.runtime.task-orchestration",
                "agentx.runtime.tool-guard",
                "agentx.approval.bridge",
                "agentx.authorization.delegation",
                "agentx.audit.reporting"
        );
    }

    public boolean leaveScenarioReusesAgentxIntegration() {
        return true;
    }

    public List<String> duplicatedCapabilities() {
        return List.of();
    }

}
