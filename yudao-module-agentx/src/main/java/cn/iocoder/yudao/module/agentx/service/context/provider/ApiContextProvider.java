package cn.iocoder.yudao.module.agentx.service.context.provider;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.agentx.service.context.ContextProvider;
import cn.iocoder.yudao.module.agentx.service.context.ContextRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 业务 API 上下文 Provider。
 */
@Component
public class ApiContextProvider implements ContextProvider {

    @Resource
    private RestTemplate restTemplate;

    @Override
    public String getType() {
        return "api";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> provide(ContextRequest request) {
        String url = String.valueOf(request.getParams().get("url"));
        String method = String.valueOf(request.getParams().getOrDefault("method", "GET"));
        Map<String, Object> params = request.getParams().get("params") instanceof Map
                ? (Map<String, Object>) request.getParams().get("params")
                : Collections.emptyMap();

        Map<String, Object> resolvedParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String && StrUtil.equals("${user.id}", (String) value)) {
                resolvedParams.put(entry.getKey(), request.getUserId());
            } else {
                resolvedParams.put(entry.getKey(), value);
            }
        }

        HttpMethod httpMethod = HttpMethod.resolve(method.toUpperCase());
        if (httpMethod == null) {
            httpMethod = HttpMethod.GET;
        }

        ResponseEntity<Object> response;
        if (httpMethod == HttpMethod.GET) {
            String finalUrl = url;
            if (!resolvedParams.isEmpty()) {
                StringBuilder builder = new StringBuilder(url);
                builder.append(url.contains("?") ? "&" : "?");
                boolean first = true;
                for (Map.Entry<String, Object> entry : resolvedParams.entrySet()) {
                    if (!first) {
                        builder.append('&');
                    }
                    builder.append(entry.getKey()).append('=').append(entry.getValue());
                    first = false;
                }
                finalUrl = builder.toString();
            }
            response = restTemplate.exchange(finalUrl, HttpMethod.GET, HttpEntity.EMPTY, Object.class);
        } else {
            response = restTemplate.exchange(url, httpMethod, new HttpEntity<>(resolvedParams), Object.class);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("response", response.getBody());
        result.put("status", response.getStatusCodeValue());
        return result;
    }

}
