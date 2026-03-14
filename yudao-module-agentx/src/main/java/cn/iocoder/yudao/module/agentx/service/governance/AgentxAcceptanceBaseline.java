package cn.iocoder.yudao.module.agentx.service.governance;

import java.util.List;

/**
 * 定义平台成立前必须满足的验收基线。
 */
public class AgentxAcceptanceBaseline {

    public List<String> gates() {
        return List.of(
                "scenario-reuses-platform-foundation",
                "openfang-cannot-access-oa-db-or-bypass-approval",
                "oa-only-sees-projection-binding-and-audit-summary",
                "approval-truth-stays-in-oa-runtime-projection-stays-in-openfang",
                "leave-scenario-runs-end-to-end-with-audit-and-binding",
                "platform-abstractions-support-next-approval-scenario"
        );
    }

}
