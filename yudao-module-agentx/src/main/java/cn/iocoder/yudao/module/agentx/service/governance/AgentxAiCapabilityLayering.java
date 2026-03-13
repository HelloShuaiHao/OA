package cn.iocoder.yudao.module.agentx.service.governance;

/**
 * 定义 yudao-module-ai 与 AgentX 在管理台、注册表和上下文注入上的复用边界。
 */
public class AgentxAiCapabilityLayering {

    public String decisionOf(String capability) {
        switch (capability) {
            case "workflow-console":
                return "reuse-ai-console";
            case "tool-registry":
                return "reuse-ai-tool-registry";
            case "tool-context":
            case "tenant-user-context":
                return "reuse-with-agentx-wrapper";
            case "scenario-workflow-mapping":
                return "new-agentx-scenario-mapping";
            case "agentx-workflow-console":
                return "forbid-second-console";
            default:
                throw new IllegalArgumentException("Unknown capability: " + capability);
        }
    }

}
