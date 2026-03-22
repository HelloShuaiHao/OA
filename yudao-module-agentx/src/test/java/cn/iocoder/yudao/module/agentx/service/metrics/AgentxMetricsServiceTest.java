package cn.iocoder.yudao.module.agentx.service.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxMetricsServiceTest {

    @Test
    void shouldTreat4xxAsUnavailable() {
        AgentxMetricsService service = new AgentxMetricsService(new SimpleMeterRegistry());
        service.recordApiResult(200);
        service.recordApiResult(404);

        double rate = (double) ReflectionTestUtils.invokeMethod(service, "apiAvailabilityRate");

        assertEquals(0.5D, rate, 0.0001);
    }

    @Test
    void shouldTreat3xxAsAvailable() {
        AgentxMetricsService service = new AgentxMetricsService(new SimpleMeterRegistry());
        service.recordApiResult(302);

        double rate = (double) ReflectionTestUtils.invokeMethod(service, "apiAvailabilityRate");

        assertEquals(1D, rate, 0.0001);
    }
}
