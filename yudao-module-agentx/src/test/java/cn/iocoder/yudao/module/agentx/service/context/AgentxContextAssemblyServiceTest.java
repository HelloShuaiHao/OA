package cn.iocoder.yudao.module.agentx.service.context;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxContextAssemblyServiceTest {

    @Test
    void shouldMergeContextFromMatchingProviders() {
        AgentxContextAssemblyService service = new AgentxContextAssemblyService(Arrays.asList(
                request -> new ContextContribution()
                        .setSource(ContextSource.BPM)
                        .setLayer(ContextLayer.REQUIRED)
                        .setValues("oa.leave.approval".equals(request.getScenarioCode())
                                ? Collections.singletonMap("leave.form", "L-1") : Collections.emptyMap()),
                request -> new ContextContribution()
                        .setSource(ContextSource.USER_PROFILE)
                        .setLayer(ContextLayer.OPTIONAL)
                        .setValues(Collections.singletonMap("applicant.profile", "U-1"))
        ));

        BusinessContextBundle bundle = service.assemble(new AgentxContextRequest("oa.leave.approval", "leave:1", new HashMap<>()));

        assertEquals("oa.leave.approval", bundle.getScenarioCode());
        assertEquals("L-1", bundle.getContext().get("leave.form"));
        assertEquals("U-1", bundle.getContext().get("applicant.profile"));
        assertNotNull(bundle.getSnapshot());
        assertEquals(2, bundle.getSnapshot().getSources().size());
        assertTrue(bundle.getSummaryContext().containsKey("contextSummary"));
    }

}
