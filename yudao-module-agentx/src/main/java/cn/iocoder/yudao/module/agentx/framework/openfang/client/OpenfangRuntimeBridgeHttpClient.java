package cn.iocoder.yudao.module.agentx.framework.openfang.client;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.agentx.framework.openfang.config.AgentxOpenfangProperties;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangApprovalDetailRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangTaskRespDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunReqDTO;
import cn.iocoder.yudao.module.agentx.framework.openfang.dto.OpenfangWorkflowRunRespDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * OpenFang Runtime HTTP 客户端。
 */
public class OpenfangRuntimeBridgeHttpClient implements OpenfangRuntimeBridge {

    private final RestTemplate restTemplate;
    private final AgentxOpenfangProperties properties;

    public OpenfangRuntimeBridgeHttpClient(RestTemplate restTemplate, AgentxOpenfangProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public OpenfangWorkflowRunRespDTO runWorkflow(String workflowId, OpenfangWorkflowRunReqDTO request) {
        return exchange("/api/workflows/" + workflowId + "/run", HttpMethod.POST, request,
                new ParameterizedTypeReference<CommonResult<OpenfangWorkflowRunRespDTO>>() { });
    }

    @Override
    public OpenfangTaskRespDTO getTask(String taskRunId) {
        return exchange("/api/tasks/" + taskRunId, HttpMethod.GET, null,
                new ParameterizedTypeReference<CommonResult<OpenfangTaskRespDTO>>() { });
    }

    @Override
    public OpenfangApprovalDetailRespDTO getApprovalDetail(String taskRunId, String approvalId) {
        return exchange("/api/approvals/" + approvalId + "?task_run_id=" + taskRunId, HttpMethod.GET, null,
                new ParameterizedTypeReference<CommonResult<OpenfangApprovalDetailRespDTO>>() { });
    }

    @Override
    public void approve(String approvalId, String decisionComment) {
        exchange("/api/approvals/" + approvalId + "/approve", HttpMethod.POST,
                java.util.Collections.singletonMap("comment", decisionComment),
                new ParameterizedTypeReference<CommonResult<Object>>() { });
    }

    @Override
    public void reject(String approvalId, String decisionComment) {
        exchange("/api/approvals/" + approvalId + "/reject", HttpMethod.POST,
                java.util.Collections.singletonMap("comment", decisionComment),
                new ParameterizedTypeReference<CommonResult<Object>>() { });
    }

    private <T> T exchange(String path, HttpMethod method, Object body,
                           ParameterizedTypeReference<CommonResult<T>> typeReference) {
        ResponseEntity<CommonResult<T>> response = restTemplate.exchange(
                buildUrl(path), method, new HttpEntity<>(body, buildHeaders()), typeReference);
        return response.getBody() != null ? response.getBody().getData() : null;
    }

    private String buildUrl(String path) {
        return StrUtil.removeSuffix(properties.getBaseUrl(), "/") + path;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StrUtil.isNotBlank(properties.getAccessToken())) {
            headers.setBearerAuth(properties.getAccessToken());
        }
        return headers;
    }

}
