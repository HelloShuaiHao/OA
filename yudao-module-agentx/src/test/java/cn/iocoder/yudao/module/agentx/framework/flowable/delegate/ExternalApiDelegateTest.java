package cn.iocoder.yudao.module.agentx.framework.flowable.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class ExternalApiDelegateTest {

    @Test
    void shouldCallApiWithRetryAndSucceed() {
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        AtomicInteger count = new AtomicInteger();
        when(restTemplate.exchange(eq("https://example.com/hook"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenAnswer(invocation -> {
                    if (count.incrementAndGet() == 1) {
                        throw new RuntimeException("network error");
                    }
                    return new ResponseEntity<>(Map.of("ok", true), HttpStatus.OK);
                });

        ExternalApiDelegate delegate = buildDelegate(restTemplate);
        Map<String, Object> variables = new HashMap<>();
        variables.put("apiUrl", "https://example.com/hook");
        variables.put("httpMethod", "POST");
        variables.put("apiBody", Map.of("k", "v"));
        variables.put("retryTimes", 1);
        variables.put("authType", "BEARER");
        variables.put("authToken", "abc");

        delegate.execute(mockExecution(variables));

        assertEquals("SUCCESS", variables.get("externalApiStatus"));
        assertEquals(2, variables.get("externalApiAttempts"));
        assertTrue(variables.get("externalApiResult") instanceof Map);
    }

    @Test
    void shouldFailWhenMethodInvalid() {
        ExternalApiDelegate delegate = buildDelegate(Mockito.mock(RestTemplate.class));
        Map<String, Object> variables = new HashMap<>();
        variables.put("apiUrl", "https://example.com/hook");
        variables.put("httpMethod", "PATCHX");

        delegate.execute(mockExecution(variables));

        assertEquals("FAILED", variables.get("externalApiStatus"));
        assertTrue(String.valueOf(variables.get("externalApiError")).contains("unsupported method"));
    }

    private ExternalApiDelegate buildDelegate(RestTemplate restTemplate) {
        ExternalApiDelegate delegate = new ExternalApiDelegate();
        try {
            Field field = ExternalApiDelegate.class.getDeclaredField("restTemplate");
            field.setAccessible(true);
            field.set(delegate, restTemplate);
            return delegate;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private DelegateExecution mockExecution(Map<String, Object> variables) {
        DelegateExecution execution = Mockito.mock(DelegateExecution.class);
        when(execution.getVariable(anyString())).thenAnswer(invocation -> variables.get(invocation.getArgument(0)));
        Mockito.doAnswer(invocation -> {
            variables.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());
        return execution;
    }
}
