package cn.iocoder.yudao.module.agentx.service.governance;

import java.util.List;

/**
 * 定义 Phase 1 必须交付的文档包。
 */
public class AgentxDeliveryPackage {

    public List<String> deliverableTypes() {
        return List.of(
                "boundary-document",
                "object-model-document",
                "interface-document",
                "implementation-roadmap-document",
                "first-scenario-acceptance-checklist"
        );
    }

    public List<String> documentPaths() {
        return List.of(
                "docs/deliverables/2026-03-12-openfang-boundary-document.md",
                "docs/deliverables/2026-03-12-openfang-object-model-document.md",
                "docs/deliverables/2026-03-12-openfang-interface-document.md",
                "docs/deliverables/2026-03-12-openfang-implementation-roadmap.md",
                "docs/deliverables/2026-03-12-openfang-first-scenario-acceptance-checklist.md"
        );
    }

}
