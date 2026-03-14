package cn.iocoder.yudao.module.agentx.service.governance;

import java.util.List;

/**
 * 定义 AgentX 的逻辑模块划分和依赖边界。
 */
public class AgentxModuleBlueprint {

    public List<String> logicalModules() {
        return List.of(
                "agentx-domain",
                "agentx-identity",
                "agentx-authorization",
                "agentx-context",
                "agentx-runtime-bridge",
                "agentx-approval-bridge",
                "agentx-audit",
                "agentx-scenario"
        );
    }

    public List<String> responsibilitiesOf(String module) {
        switch (module) {
            case "agentx-domain":
                return List.of("domain-objects", "enums", "protocol-dtos", "domain-service-interfaces");
            case "agentx-identity":
                return List.of("agent-principal", "delegation-grant", "execution-identity");
            case "agentx-authorization":
                return List.of("capability-intersection", "tool-permission", "data-scope-control", "scenario-policy-evaluation");
            case "agentx-audit":
                return List.of("standardized-audit-event", "tool-audit-trail", "prompt-decision-archive-boundary", "risk-event-recording");
            default:
                throw new IllegalArgumentException("Unknown module: " + module);
        }
    }

    public boolean allows(String module, String element) {
        if ("agentx-domain".equals(module)) {
            return !("controller".equals(element) || "infrastructure-implementation".equals(element));
        }
        if ("agentx-identity".equals(module) && "authorization-decision".equals(element)) {
            return false;
        }
        return true;
    }

    public boolean canDependOn(String fromModule, String toModule) {
        if (!"agentx-scenario".equals(fromModule)) {
            return true;
        }
        return "agentx-domain".equals(toModule)
                || "agentx-context".equals(toModule)
                || "agentx-identity".equals(toModule)
                || "agentx-authorization".equals(toModule)
                || "agentx-runtime-bridge".equals(toModule)
                || "agentx-approval-bridge".equals(toModule)
                || "agentx-audit".equals(toModule);
    }

}
