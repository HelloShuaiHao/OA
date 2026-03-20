package cn.iocoder.yudao.module.agentx.framework.openfang.dto;

import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * OpenFang workflow run 请求。
 */
@Data
public class OpenfangWorkflowRunReqDTO {

    private String scenarioCode;
    private String idempotencyKey;
    private String businessKey;
    private String principalType;
    private String principalId;
    /**
     * 透传给 OpenFang 内置 bpm_* 工具使用的 OA 访问令牌。
     */
    private String bpmAccessToken;
    private Set<String> capabilityCodes;
    private Map<String, Object> contextBundle;
    private String contextSummary;

}
