package cn.iocoder.yudao.module.agentx.service.governance;

import java.util.List;

/**
 * 记录首个请假场景落地后的平台复盘结果，用于指导后续同类场景扩展。
 */
public class AgentxPlatformRetrospective {

    public List<String> reusablePlatformAbstractions() {
        return List.of(
                "workflow-mapping",
                "context-assembly",
                "task-orchestration",
                "tool-guard",
                "approval-bridge",
                "authorization-delegation",
                "audit-reporting"
        );
    }

    public List<String> leaveSpecificPlugins() {
        return List.of(
                "leave-scenario-definition",
                "leave-context-provider",
                "leave-tool-set",
                "leave-approval-policy"
        );
    }

    public List<String> nextCandidateScenarios() {
        return List.of(
                "expense",
                "travel",
                "procurement"
        );
    }

}
