package cn.iocoder.yudao.module.agentx.framework.openfang.client;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.agentx.framework.openfang.config.AgentxOpenfangProperties;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangApprovalDetailRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangHealthRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangTaskRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunReqDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunRespDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenfangRuntimeBridgeHttpClientTest {

    @Test
    void shouldSendWorkflowRunRequestWithBearerToken() {
        AtomicReference<String> url = new AtomicReference<>();
        AtomicReference<HttpEntity<?>> entityRef = new AtomicReference<>();
        RestTemplate restTemplate = new CapturingRestTemplate(url, entityRef,
                ResponseEntity.ok(CommonResult.success(new OpenfangWorkflowRunRespDTO().setTaskRunId("task-1"))));
        AgentxOpenfangProperties properties = new AgentxOpenfangProperties();
        properties.setBaseUrl("http://openfang.test");
        properties.setAccessToken("token-1");
        OpenfangRuntimeBridgeHttpClient client = new OpenfangRuntimeBridgeHttpClient(restTemplate, properties);

        OpenfangWorkflowRunReqDTO req = new OpenfangWorkflowRunReqDTO();
        req.setBusinessKey("leave:1");
        OpenfangWorkflowRunRespDTO result = client.runWorkflow("wf-1", req);

        assertEquals("task-1", result.getTaskRunId());
        assertEquals("http://openfang.test/api/workflows/wf-1/run", url.get());
        assertEquals("Bearer token-1", entityRef.get().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertTrue(entityRef.get().getHeaders().getContentType().includes(MediaType.APPLICATION_JSON));
        Object body = entityRef.get().getBody();
        assertTrue(body instanceof java.util.Map);
        Object input = ((java.util.Map<?, ?>) body).get("input");
        assertTrue(input instanceof String);
        assertTrue(((String) input).contains("\"business_key\":\"leave:1\""));
    }

    @Test
    void shouldReadTaskAndApprovalDetailByRestEndpoints() {
        AtomicReference<String> url = new AtomicReference<>();
        AtomicReference<HttpMethod> methodRef = new AtomicReference<>();
        RestTemplate restTemplate = new CapturingRestTemplate(url, null,
                ResponseEntity.ok(CommonResult.success(new OpenfangTaskRespDTO().setTaskRunId("task-1")))) {
            @Override
            protected HttpMethod captureMethod(HttpMethod method) {
                methodRef.set(method);
                return method;
            }
        };
        AgentxOpenfangProperties properties = new AgentxOpenfangProperties();
        properties.setBaseUrl("http://openfang.test");
        OpenfangRuntimeBridgeHttpClient client = new OpenfangRuntimeBridgeHttpClient(restTemplate, properties);

        OpenfangTaskRespDTO task = client.getTask("task-1");
        assertEquals("task-1", task.getTaskRunId());
        assertEquals(HttpMethod.GET, methodRef.get());
        assertEquals("http://openfang.test/api/tasks/task-1", url.get());

        RestTemplate approvalTemplate = new CapturingRestTemplate(url, null,
                ResponseEntity.ok(CommonResult.success(new OpenfangApprovalDetailRespDTO().setApprovalId("approval-1"))));
        OpenfangRuntimeBridgeHttpClient approvalClient = new OpenfangRuntimeBridgeHttpClient(approvalTemplate, properties);
        OpenfangApprovalDetailRespDTO detail = approvalClient.getApprovalDetail("task-1", "approval-1");
        assertEquals("approval-1", detail.getApprovalId());
        assertEquals("http://openfang.test/api/approvals/approval-1?task_run_id=task-1", url.get());
    }

    @Test
    void shouldPostApprovalDecisionComment() {
        AtomicReference<String> url = new AtomicReference<>();
        AtomicReference<HttpEntity<?>> entityRef = new AtomicReference<>();
        AtomicReference<HttpMethod> methodRef = new AtomicReference<>();
        RestTemplate restTemplate = new CapturingRestTemplate(url, entityRef,
                ResponseEntity.ok(CommonResult.success(null))) {
            @Override
            protected HttpMethod captureMethod(HttpMethod method) {
                methodRef.set(method);
                return method;
            }
        };
        AgentxOpenfangProperties properties = new AgentxOpenfangProperties();
        properties.setBaseUrl("http://openfang.test");
        OpenfangRuntimeBridgeHttpClient client = new OpenfangRuntimeBridgeHttpClient(restTemplate, properties);

        client.approve("approval-1", "同意");

        assertEquals(HttpMethod.POST, methodRef.get());
        assertEquals("http://openfang.test/api/approvals/approval-1/approve", url.get());
        assertEquals("同意", ((java.util.Map<?, ?>) entityRef.get().getBody()).get("comment"));

        client.reject("approval-1", "拒绝");

        assertEquals("http://openfang.test/api/approvals/approval-1/reject", url.get());
        assertEquals("拒绝", ((java.util.Map<?, ?>) entityRef.get().getBody()).get("comment"));
    }

    @Test
    void shouldProbeHealthByDynamicBaseUrl() {
        AtomicReference<String> url = new AtomicReference<>();
        RestTemplate restTemplate = new CapturingRestTemplate(url, null,
                ResponseEntity.ok(Collections.singletonMap("version", "0.3.0")));
        AgentxOpenfangProperties properties = new AgentxOpenfangProperties();
        properties.setBaseUrl("http://openfang.default");
        OpenfangRuntimeBridgeHttpClient client = new OpenfangRuntimeBridgeHttpClient(restTemplate, properties);

        OpenfangHealthRespDTO healthRespDTO = client.health("http://openfang.health", "token-x");
        assertEquals("http://openfang.health/health", url.get());
        assertTrue(Boolean.TRUE.equals(healthRespDTO.getOnline()));
        assertEquals("0.3.0", healthRespDTO.getVersion());
    }

    private static class CapturingRestTemplate extends RestTemplate {

        private final AtomicReference<String> url;
        private final AtomicReference<HttpEntity<?>> entityRef;
        private final ResponseEntity<?> response;

        private CapturingRestTemplate(AtomicReference<String> url, AtomicReference<HttpEntity<?>> entityRef,
                                      ResponseEntity<?> response) {
            this.url = url;
            this.entityRef = entityRef;
            this.response = response;
        }

        protected HttpMethod captureMethod(HttpMethod method) {
            return method;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
                                              org.springframework.core.ParameterizedTypeReference<T> responseType,
                                              Object... uriVariables) {
            this.url.set(url);
            if (entityRef != null) {
                this.entityRef.set(requestEntity);
            }
            captureMethod(method);
            return (ResponseEntity<T>) response;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
                                              Class<T> responseType, Object... uriVariables) {
            this.url.set(url);
            if (entityRef != null) {
                this.entityRef.set(requestEntity);
            }
            captureMethod(method);
            return (ResponseEntity<T>) response;
        }
    }

}
