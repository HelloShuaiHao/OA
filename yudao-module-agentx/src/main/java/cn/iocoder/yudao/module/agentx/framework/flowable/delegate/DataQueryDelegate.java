package cn.iocoder.yudao.module.agentx.framework.flowable.delegate;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;

/**
 * AgentX 扩展节点：数据查询节点（SQL/API）。
 */
@Component("agentxDataQueryDelegate")
@Slf4j
public class DataQueryDelegate implements JavaDelegate {

    private static final String DEFAULT_OUTPUT_VARIABLE = "dataQueryResult";
    private static final Pattern FORBIDDEN_SQL =
            Pattern.compile("\\b(insert|update|delete|drop|alter|truncate|create|grant|revoke)\\b", Pattern.CASE_INSENSITIVE);

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private RestTemplate restTemplate;

    @Override
    public void execute(DelegateExecution execution) {
        String queryMode = StrUtil.blankToDefault(stringValue(execution.getVariable("queryMode")), "sql");
        String outputVariable = firstNonBlank(
                stringValue(execution.getVariable("dataQueryOutputVariable")),
                stringValue(execution.getVariable("outputVariableName")),
                DEFAULT_OUTPUT_VARIABLE);
        try {
            if ("api".equalsIgnoreCase(queryMode) || "oa_api".equalsIgnoreCase(queryMode)) {
                executeApiQuery(execution, outputVariable);
                return;
            }
            executeSqlQuery(execution, outputVariable);
        } catch (RuntimeException ex) {
            execution.setVariable(outputVariable, Collections.emptyList());
            execution.setVariable("dataQueryStatus", "FAILED");
            execution.setVariable("dataQueryMessage", "数据查询失败");
            execution.setVariable("dataQueryError", ex.getMessage());
            log.warn("[DataQueryDelegate] query failed, mode={}", queryMode, ex);
        }
    }

    private void executeSqlQuery(DelegateExecution execution, String outputVariable) {
        String sql = firstNonBlank(
                stringValue(execution.getVariable("sql")),
                stringValue(execution.getVariable("querySql")));
        if (StrUtil.isBlank(sql)) {
            execution.setVariable(outputVariable, Collections.emptyList());
            execution.setVariable("dataQueryStatus", "SKIPPED");
            execution.setVariable("dataQueryMessage", "SQL 为空，已跳过");
            return;
        }
        ensureSqlSafe(sql);
        Object[] args = resolveSqlArgs(execution.getVariable("queryArgs"));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args);
        execution.setVariable(outputVariable, rows);
        execution.setVariable("dataQueryStatus", "SUCCESS");
        execution.setVariable("dataQueryMessage", "SQL 查询成功");
        execution.setVariable("dataQueryCount", rows.size());
        execution.setVariable("dataQueryError", null);
    }

    private void executeApiQuery(DelegateExecution execution, String outputVariable) {
        String url = firstNonBlank(
                stringValue(execution.getVariable("apiUrl")),
                stringValue(execution.getVariable("url")));
        if (StrUtil.isBlank(url)) {
            execution.setVariable(outputVariable, Collections.emptyMap());
            execution.setVariable("dataQueryStatus", "FAILED");
            execution.setVariable("dataQueryMessage", "API 地址不能为空");
            execution.setVariable("dataQueryError", "api url empty");
            return;
        }
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        execution.setVariable(outputVariable, response == null ? Collections.emptyMap() : response);
        execution.setVariable("dataQueryStatus", "SUCCESS");
        execution.setVariable("dataQueryMessage", "API 查询成功");
        execution.setVariable("dataQueryError", null);
    }

    private void ensureSqlSafe(String sql) {
        String normalized = sql.trim();
        if (!StrUtil.startWithIgnoreCase(normalized, "select")) {
            throw new IllegalArgumentException("仅允许 SELECT 查询");
        }
        if (StrUtil.containsAny(normalized, ";", "--", "/*", "*/")) {
            throw new IllegalArgumentException("检测到潜在 SQL 注入风险");
        }
        if (FORBIDDEN_SQL.matcher(normalized).find()) {
            throw new IllegalArgumentException("SQL 包含高风险关键字");
        }
    }

    private Object[] resolveSqlArgs(Object raw) {
        if (raw == null) {
            return new Object[0];
        }
        if (raw instanceof List) {
            return ((List<?>) raw).toArray();
        }
        if (raw instanceof Object[]) {
            return (Object[]) raw;
        }
        if (raw instanceof String && StrUtil.isNotBlank((String) raw)) {
            String text = (String) raw;
            if (StrUtil.startWith(text.trim(), "[")) {
                List<Object> values = JsonUtils.parseArray(text, Object.class);
                return values.toArray();
            }
            return StrUtil.splitToArray(text, ',');
        }
        return new Object[]{raw};
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StrUtil.isNotBlank(value) && !"null".equalsIgnoreCase(value)) {
                return value;
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
