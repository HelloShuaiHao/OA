package cn.iocoder.yudao.module.agentx.framework.openfang.client;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.agentx.framework.openfang.config.AgentxOpenfangProperties;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangApprovalDetailRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangHealthRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangTaskRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunReqDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunRespDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * OpenFang Runtime HTTP 客户端。
 */
public class OpenfangRuntimeBridgeHttpClient implements OpenfangRuntimeBridge {

    private final RestTemplate restTemplate;
    private final AgentxOpenfangProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenfangRuntimeBridgeHttpClient(RestTemplate restTemplate, AgentxOpenfangProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public OpenfangWorkflowRunRespDTO runWorkflow(String workflowId, OpenfangWorkflowRunReqDTO request) {
        return exchange("/api/workflows/" + workflowId + "/run", HttpMethod.POST,
                buildWorkflowRunPayload(request), OpenfangWorkflowRunRespDTO.class);
    }

    @Override
    public OpenfangTaskRespDTO getTask(String taskRunId) {
        return exchange("/api/tasks/" + taskRunId, HttpMethod.GET, null, OpenfangTaskRespDTO.class);
    }

    @Override
    public OpenfangTaskRespDTO getTaskRun(String taskRunId) {
        return getTask(taskRunId);
    }

    @Override
    public OpenfangApprovalDetailRespDTO getApprovalDetail(String taskRunId, String approvalId) {
        return exchange("/api/approvals/" + approvalId + "?task_run_id=" + taskRunId, HttpMethod.GET, null,
                OpenfangApprovalDetailRespDTO.class);
    }

    @Override
    public void approve(String approvalId, String decisionComment) {
        exchange("/api/approvals/" + approvalId + "/approve", HttpMethod.POST,
                java.util.Collections.singletonMap("comment", decisionComment),
                Object.class);
    }

    @Override
    public void reject(String approvalId, String decisionComment) {
        exchange("/api/approvals/" + approvalId + "/reject", HttpMethod.POST,
                java.util.Collections.singletonMap("comment", decisionComment),
                Object.class);
    }

    @Override
    public void approveCallback(String approvalId, String decisionComment) {
        approve(approvalId, decisionComment);
    }

    @Override
    public void rejectCallback(String approvalId, String decisionComment) {
        reject(approvalId, decisionComment);
    }

    @Override
    @SuppressWarnings("unchecked")
    public OpenfangHealthRespDTO health(String baseUrl, String accessToken) {
        String normalized = StrUtil.removeSuffix(baseUrl, "/");
        String[] candidatePaths = new String[] { "/health", "/api/health" };
        for (String path : candidatePaths) {
            try {
                ResponseEntity<Object> response = restTemplate.exchange(
                        normalized + path, HttpMethod.GET,
                        new HttpEntity<>(null, buildHeaders(accessToken)), Object.class);
                Object body = response.getBody();
                String version = null;
                if (body instanceof Map) {
                    Object data = ((Map<?, ?>) body).get("data");
                    if (data instanceof Map) {
                        Object dataVersion = ((Map<?, ?>) data).get("version");
                        if (dataVersion != null) {
                            version = String.valueOf(dataVersion);
                        }
                    } else {
                        Object bodyVersion = ((Map<?, ?>) body).get("version");
                        if (bodyVersion != null) {
                            version = String.valueOf(bodyVersion);
                        }
                    }
                }
                return new OpenfangHealthRespDTO().setOnline(true).setVersion(version).setMessage("OK");
            } catch (Exception ex) {
                // continue trying next path
            }
        }
        return new OpenfangHealthRespDTO().setOnline(false).setMessage("OpenFang 健康检查失败");
    }

    @SuppressWarnings("unchecked")
    private <T> T exchange(String path, HttpMethod method, Object body, Class<T> clazz) {
        ResponseEntity<Object> response = restTemplate.exchange(
                buildUrl(path), method, new HttpEntity<>(body, buildHeaders()), Object.class);
        Object rawBody = response.getBody();
        if (rawBody == null) {
            return null;
        }
        Object payload = rawBody;
        if (rawBody instanceof Map && ((Map<?, ?>) rawBody).containsKey("data")) {
            payload = ((Map<?, ?>) rawBody).get("data");
        }
        if (payload == null) {
            return null;
        }
        if (payload instanceof Map) {
            payload = normalizePayload((Map<?, ?>) payload, clazz);
        }
        return objectMapper.convertValue(payload, clazz);
    }

    private <T> Map<String, Object> normalizePayload(Map<?, ?> source, Class<T> clazz) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (entry.getKey() != null) {
                normalized.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        if (clazz == OpenfangTaskRespDTO.class) {
            copyIfAbsent(normalized, "status", "state");
            copyIfAbsent(normalized, "stage", "current_phase");
            copyIfAbsent(normalized, "trace_events", "skill_invocations");
        } else if (clazz == OpenfangWorkflowRunRespDTO.class) {
            copyIfAbsent(normalized, "task_status", "state");
        }
        return normalized;
    }

    private void copyIfAbsent(Map<String, Object> target, String key, String alias) {
        if (!target.containsKey(key) && target.containsKey(alias)) {
            target.put(key, target.get(alias));
        }
    }

    private String buildUrl(String path) {
        return StrUtil.removeSuffix(properties.getBaseUrl(), "/") + path;
    }

    private Map<String, Object> buildWorkflowRunPayload(OpenfangWorkflowRunReqDTO request) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("scenario_code", request.getScenarioCode());
        input.put("business_key", request.getBusinessKey());
        input.put("idempotency_key", request.getIdempotencyKey());
        if (StrUtil.isNotBlank(request.getPrincipalId()) && StrUtil.isNumeric(request.getPrincipalId())) {
            input.put("user_id", Long.valueOf(request.getPrincipalId()));
        } else if (StrUtil.isNotBlank(request.getPrincipalId())) {
            input.put("user_id", request.getPrincipalId());
        }
        if (StrUtil.isNotBlank(request.getBpmAccessToken())) {
            input.put("access_token", request.getBpmAccessToken());
        }
        if (request.getContextBundle() != null) {
            input.put("context_bundle", request.getContextBundle());
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("input", serializeWorkflowInput(input));
        return payload;
    }

    private String serializeWorkflowInput(Map<String, Object> input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (Exception ex) {
            return input.toString();
        }
    }

    private HttpHeaders buildHeaders() {
        return buildHeaders(properties.getAccessToken());
    }

    private HttpHeaders buildHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StrUtil.isNotBlank(accessToken)) {
            headers.setBearerAuth(accessToken);
        }
        return headers;
    }

}
