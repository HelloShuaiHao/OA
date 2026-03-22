package cn.iocoder.yudao.module.agentx.framework.flowable.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class DataQueryDelegateTest {

    @Test
    void shouldQuerySqlSuccessfully() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        when(jdbcTemplate.queryForList(anyString(), any(Object[].class)))
                .thenReturn(List.of(Map.of("id", 1L, "name", "demo")));

        DataQueryDelegate delegate = buildDelegate(jdbcTemplate, restTemplate);
        Map<String, Object> variables = new HashMap<>();
        variables.put("sql", "select id, name from sys_user where id = ?");
        variables.put("queryArgs", List.of(1));

        delegate.execute(mockExecution(variables));

        assertEquals("SUCCESS", variables.get("dataQueryStatus"));
        assertTrue(variables.get("dataQueryResult") instanceof List);
    }

    @Test
    void shouldRejectUnsafeSql() {
        DataQueryDelegate delegate = buildDelegate(Mockito.mock(JdbcTemplate.class), Mockito.mock(RestTemplate.class));
        Map<String, Object> variables = new HashMap<>();
        variables.put("sql", "select * from sys_user; drop table sys_user");

        delegate.execute(mockExecution(variables));

        assertEquals("FAILED", variables.get("dataQueryStatus"));
        assertTrue(String.valueOf(variables.get("dataQueryError")).contains("SQL"));
    }

    @Test
    void shouldQueryApiSuccessfully() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        when(restTemplate.getForObject(eq("https://example.com/api"), eq(Map.class)))
                .thenReturn(Map.of("ok", true));

        DataQueryDelegate delegate = buildDelegate(jdbcTemplate, restTemplate);
        Map<String, Object> variables = new HashMap<>();
        variables.put("queryMode", "api");
        variables.put("apiUrl", "https://example.com/api");

        delegate.execute(mockExecution(variables));

        assertEquals("SUCCESS", variables.get("dataQueryStatus"));
        assertEquals(Map.of("ok", true), variables.get("dataQueryResult"));
    }

    private DataQueryDelegate buildDelegate(JdbcTemplate jdbcTemplate, RestTemplate restTemplate) {
        DataQueryDelegate delegate = new DataQueryDelegate();
        try {
            Field jdbcField = DataQueryDelegate.class.getDeclaredField("jdbcTemplate");
            jdbcField.setAccessible(true);
            jdbcField.set(delegate, jdbcTemplate);
            Field restField = DataQueryDelegate.class.getDeclaredField("restTemplate");
            restField.setAccessible(true);
            restField.set(delegate, restTemplate);
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
