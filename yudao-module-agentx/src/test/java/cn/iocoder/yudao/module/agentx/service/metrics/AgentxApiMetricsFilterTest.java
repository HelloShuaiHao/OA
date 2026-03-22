package cn.iocoder.yudao.module.agentx.service.metrics;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.FilterChain;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AgentxApiMetricsFilterTest {

    @Test
    void shouldRecordDurationAndAvailabilityForAgentxUri() throws Exception {
        AgentxMetricsService metricsService = Mockito.mock(AgentxMetricsService.class);
        AgentxApiMetricsFilter filter = new AgentxApiMetricsFilter();
        ReflectionTestUtils.setField(filter, "metricsService", metricsService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin-api/agentx/task/page");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, resp) -> ((MockHttpServletResponse) resp).setStatus(200);

        filter.doFilter(request, response, chain);

        verify(metricsService, times(1)).recordApiDuration(anyLong());
        verify(metricsService, times(1)).recordApiResult(200);
    }

    @Test
    void shouldSkipNonAgentxUri() throws Exception {
        AgentxMetricsService metricsService = Mockito.mock(AgentxMetricsService.class);
        AgentxApiMetricsFilter filter = new AgentxApiMetricsFilter();
        ReflectionTestUtils.setField(filter, "metricsService", metricsService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin-api/system/user/page");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, resp) -> ((MockHttpServletResponse) resp).setStatus(200);

        filter.doFilter(request, response, chain);

        verify(metricsService, times(0)).recordApiDuration(anyLong());
        verify(metricsService, times(0)).recordApiResult(200);
    }

}
