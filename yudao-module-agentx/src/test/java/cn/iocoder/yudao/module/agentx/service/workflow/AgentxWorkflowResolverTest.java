package cn.iocoder.yudao.module.agentx.service.workflow;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentxWorkflowResolverTest {

    private final AgentxWorkflowResolver resolver = new AgentxWorkflowResolver();

    @Test
    void shouldResolveWorkflowByScenarioCode() {
        AgentxWorkflowResolution result = resolver.resolve(
                "oa.leave.approval",
                "v1",
                Collections.singletonList(new AgentxWorkflowMapping(
                        "oa.leave.approval", "openfang-leave", "v1", true
                ))
        );

        assertEquals("oa.leave.approval", result.getScenarioCode());
        assertEquals("openfang-leave", result.getOpenfangWorkflowId());
        assertEquals("v1", result.getExpectedWorkflowVersion());
    }

    @Test
    void shouldRejectDisabledScenario() {
        ServiceException exception = assertThrows(ServiceException.class, () -> resolver.resolve(
                "oa.leave.approval",
                "v1",
                Collections.singletonList(new AgentxWorkflowMapping(
                        "oa.leave.approval", "openfang-leave", "v1", false
                ))
        ));

        assertEquals(1_024_001_001, exception.getCode());
    }

    @Test
    void shouldRejectWorkflowVersionMismatch() {
        ServiceException exception = assertThrows(ServiceException.class, () -> resolver.resolve(
                "oa.leave.approval",
                "v2",
                Collections.singletonList(new AgentxWorkflowMapping(
                        "oa.leave.approval", "openfang-leave", "v1", true
                ))
        ));

        assertEquals(1_024_001_002, exception.getCode());
    }

}
