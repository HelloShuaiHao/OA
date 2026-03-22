package cn.iocoder.yudao.module.agentx.service.metrics;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AgentxApiMetricsFilter extends OncePerRequestFilter {

    @Resource
    private AgentxMetricsService metricsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (!isAgentxUri(uri)) {
            filterChain.doFilter(request, response);
            return;
        }
        long start = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            metricsService.recordApiDuration(System.nanoTime() - start);
            metricsService.recordApiResult(response.getStatus());
        }
    }

    private boolean isAgentxUri(String uri) {
        return uri != null && (uri.startsWith("/agentx/") || uri.startsWith("/admin-api/agentx/"));
    }
}
