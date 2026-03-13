package cn.iocoder.yudao.module.agentx.service.governance;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxDeliveryPackageTest {

    @Test
    void shouldDeclareRequiredDeliveryDocuments() {
        AgentxDeliveryPackage deliveryPackage = new AgentxDeliveryPackage();

        assertEquals(List.of(
                "boundary-document",
                "object-model-document",
                "interface-document",
                "implementation-roadmap-document",
                "first-scenario-acceptance-checklist"
        ), deliveryPackage.deliverableTypes());
        assertEquals(List.of(
                "docs/deliverables/2026-03-12-openfang-boundary-document.md",
                "docs/deliverables/2026-03-12-openfang-object-model-document.md",
                "docs/deliverables/2026-03-12-openfang-interface-document.md",
                "docs/deliverables/2026-03-12-openfang-implementation-roadmap.md",
                "docs/deliverables/2026-03-12-openfang-first-scenario-acceptance-checklist.md"
        ), deliveryPackage.documentPaths());
    }

}
