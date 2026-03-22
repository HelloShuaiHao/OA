package cn.iocoder.yudao.module.agentx.framework.flowable.delegate;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * AgentX 扩展节点：外部 API 调用节点。
 */
@Component("agentxExternalApiDelegate")
@Slf4j
public class ExternalApiDelegate implements JavaDelegate {

    private static final int DEFAULT_RETRY_TIMES = 2;
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final String DEFAULT_OUTPUT_VARIABLE = "externalApiResult";

    @Resource
    private RestTemplate restTemplate;

    @Override
    public void execute(DelegateExecution execution) {
        String url = firstNonBlank(
                stringValue(execution.getVariable("apiUrl")),
                stringValue(execution.getVariable("url")));
        String methodText = StrUtil.blankToDefault(
                firstNonBlank(stringValue(execution.getVariable("httpMethod")), stringValue(execution.getVariable("method"))),
                "GET");
        String outputVariable = firstNonBlank(
                stringValue(execution.getVariable("externalApiOutputVariable")),
                stringValue(execution.getVariable("outputVariableName")),
                DEFAULT_OUTPUT_VARIABLE);
        if (StrUtil.isBlank(url)) {
            fail(execution, outputVariable, "API 地址不能为空", "url empty", 0);
            return;
        }

        HttpMethod method = resolveMethod(methodText);
        if (method == null) {
            fail(execution, outputVariable, "不支持的 HTTP 方法", "unsupported method: " + methodText, 0);
            return;
        }

        HttpHeaders headers = resolveHeaders(execution.getVariable("headers"));
        headers.putAll(resolveHeaders(execution.getVariable("apiHeaders")));
        applyAuthHeaders(execution, headers);
        Map<String, Object> body = resolveBody(execution.getVariable("body"));
        if (MapUtil.isEmpty(body)) {
            body = resolveBody(execution.getVariable("apiBody"));
        }

        int retryTimes = resolvePositiveInt(execution.getVariable("retryTimes"), DEFAULT_RETRY_TIMES);
        retryTimes = resolvePositiveInt(execution.getVariable("apiMaxRetries"), retryTimes);
        int timeoutSeconds = resolvePositiveInt(execution.getVariable("timeoutSeconds"), DEFAULT_TIMEOUT_SECONDS);
        timeoutSeconds = resolvePositiveInt(execution.getVariable("apiTimeoutSeconds"), timeoutSeconds);

        RuntimeException lastException = null;
        for (int i = 0; i <= retryTimes; i++) {
            int attempt = i + 1;
            try {
                HttpEntity<?> request = method == HttpMethod.GET || method == HttpMethod.DELETE
                        ? new HttpEntity<>(headers)
                        : new HttpEntity<>(body, headers);
                ResponseEntity<Object> response = executeWithTimeout(url, method, request, timeoutSeconds);
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("status", response.getStatusCodeValue());
                result.put("body", response.getBody());
                execution.setVariable(outputVariable, result);
                execution.setVariable("externalApiStatus", "SUCCESS");
                execution.setVariable("externalApiMessage", "外部 API 调用成功");
                execution.setVariable("externalApiHttpStatus", response.getStatusCodeValue());
                execution.setVariable("externalApiAttempts", attempt);
                execution.setVariable("externalApiError", null);
                return;
            } catch (RuntimeException ex) {
                lastException = ex;
                log.warn("[ExternalApiDelegate] call failed, url={}, method={}, attempt={}/{}",
                        url, method, attempt, retryTimes + 1, ex);
            }
        }
        fail(execution, outputVariable, "外部 API 调用失败", lastException != null ? lastException.getMessage() : "unknown error",
                retryTimes + 1);
    }

    private ResponseEntity<Object> executeWithTimeout(String url, HttpMethod method, HttpEntity<?> request, int timeoutSeconds) {
        FutureTask<ResponseEntity<Object>> task = new FutureTask<>(
                () -> restTemplate.exchange(url, method, request, Object.class));
        Thread worker = new Thread(task, "agentx-external-api");
        worker.setDaemon(true);
        worker.start();
        try {
            return task.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            task.cancel(true);
            throw new RuntimeException("API 调用超时");
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private void applyAuthHeaders(DelegateExecution execution, HttpHeaders headers) {
        String authType = StrUtil.blankToDefault(stringValue(execution.getVariable("authType")), "NONE");
        if ("BEARER".equalsIgnoreCase(authType)) {
            String token = firstNonBlank(
                    stringValue(execution.getVariable("bearerToken")),
                    stringValue(execution.getVariable("authToken")));
            if (StrUtil.isNotBlank(token)) {
                headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }
            return;
        }
        if ("BASIC".equalsIgnoreCase(authType)) {
            String username = stringValue(execution.getVariable("basicUsername"));
            String password = stringValue(execution.getVariable("basicPassword"));
            if (StrUtil.isNotBlank(username) || StrUtil.isNotBlank(password)) {
                String text = StrUtil.blankToDefault(username, "") + ":" + StrUtil.blankToDefault(password, "");
                String encoded = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
                headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private HttpHeaders resolveHeaders(Object raw) {
        HttpHeaders headers = new HttpHeaders();
        Map<String, Object> map = Collections.emptyMap();
        if (raw instanceof Map) {
            map = (Map<String, Object>) raw;
        } else if (raw instanceof String && StrUtil.isNotBlank((String) raw)) {
            Map<String, Object> parsed = JsonUtils.parseObject((String) raw, Map.class);
            map = parsed == null ? Collections.emptyMap() : parsed;
        }
        map.forEach((k, v) -> {
            if (k != null && v != null) {
                headers.set(String.valueOf(k), String.valueOf(v));
            }
        });
        return headers;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveBody(Object raw) {
        if (raw instanceof Map) {
            return new LinkedHashMap<>((Map<String, Object>) raw);
        }
        if (raw instanceof String && StrUtil.isNotBlank((String) raw)) {
            if (StrUtil.startWith(((String) raw).trim(), "{")) {
                Map<String, Object> parsed = JsonUtils.parseObject((String) raw, Map.class);
                return parsed == null ? Collections.emptyMap() : parsed;
            }
            return MapUtil.of("value", raw);
        }
        return Collections.emptyMap();
    }

    private HttpMethod resolveMethod(String method) {
        try {
            return HttpMethod.valueOf(method.toUpperCase());
        } catch (Exception ex) {
            return null;
        }
    }

    private void fail(DelegateExecution execution, String outputVariable, String message, String error, int attempts) {
        execution.setVariable(outputVariable, Collections.emptyMap());
        execution.setVariable("externalApiStatus", "FAILED");
        execution.setVariable("externalApiMessage", message);
        execution.setVariable("externalApiError", error);
        execution.setVariable("externalApiAttempts", attempts);
    }

    private int resolvePositiveInt(Object raw, int defaultValue) {
        Integer value = Convert.toInt(raw, defaultValue);
        if (value == null || value <= 0) {
            return defaultValue;
        }
        return value;
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
