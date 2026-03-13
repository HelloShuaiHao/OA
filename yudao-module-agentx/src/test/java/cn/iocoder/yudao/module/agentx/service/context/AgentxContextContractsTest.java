package cn.iocoder.yudao.module.agentx.service.context;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxContextContractsTest {

    @Test
    void shouldDefineStandardProviderSnapshotAndBundleContracts() {
        ContextSnapshot snapshot = new ContextSnapshot()
                .setSnapshotId("ctx-1")
                .setScenarioCode("oa.leave.approval")
                .setBusinessKey("leave:1")
                .setRuleVersion("v1")
                .setLayers(Map.of(ContextLayer.REQUIRED, Map.of("leave.form", "L-1")));
        BusinessContextBundle bundle = new BusinessContextBundle("oa.leave.approval", Map.of("leave.form", "L-1"))
                .setSnapshot(snapshot)
                .setSummaryContext(Map.of("contextSummary", "leave.form"));

        assertEquals("ctx-1", bundle.getSnapshot().getSnapshotId());
        assertEquals("v1", bundle.getSnapshot().getRuleVersion());
        assertEquals("leave.form", bundle.getSummaryContext().get("contextSummary"));
    }

    @Test
    void shouldDefinePlatformContextSourcesAndLayers() {
        assertEquals(List.of(ContextSource.BPM, ContextSource.SYSTEM, ContextSource.ERP,
                ContextSource.CRM, ContextSource.KNOWLEDGE_BASE, ContextSource.USER_PROFILE, ContextSource.ORGANIZATION),
                List.of(ContextSource.values()));
        assertEquals(List.of(ContextLayer.REQUIRED, ContextLayer.OPTIONAL, ContextLayer.SENSITIVE, ContextLayer.SUMMARY),
                List.of(ContextLayer.values()));
    }

    @Test
    void shouldFilterSensitiveFieldsAndRecordAuditMetadata() {
        ContextSnapshot snapshot = new ContextSnapshot()
                .setSnapshotId("ctx-1")
                .setAssembledBy("u-1")
                .setAssembledAt("2026-03-12T21:40:00+08:00")
                .setRuleVersion("v2")
                .setSources(List.of(ContextSource.BPM, ContextSource.USER_PROFILE))
                .setLayers(Map.of(
                        ContextLayer.REQUIRED, Map.of("leave.form", "L-1"),
                        ContextLayer.SENSITIVE, Map.of("applicant.mobile", "1380000")
                ));
        AgentxContextVisibilityPolicy policy = new AgentxContextVisibilityPolicy();

        Map<String, Object> runtimeContext = policy.filterForRuntime(snapshot);

        assertEquals("L-1", runtimeContext.get("leave.form"));
        assertTrue(!runtimeContext.containsKey("applicant.mobile"));
        assertEquals("u-1", snapshot.getAssembledBy());
        assertEquals("v2", snapshot.getRuleVersion());
    }

}
