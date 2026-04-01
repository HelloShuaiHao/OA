package cn.iocoder.yudao.module.agentx.service.process;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.agentx.controller.admin.access.vo.AccessEnvelopeRespVO;
import cn.iocoder.yudao.module.agentx.service.access.AgentxAccessService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants.ENVELOPE_SIGNATURE_INVALID;

@Service
@Validated
public class AgentxProcessExecutionServiceImpl implements AgentxProcessExecutionService {

    @Resource
    private RuntimeService runtimeService;
    @Resource
    private AgentxProcessSelectionService processSelectionService;
    @Resource
    private AgentxAccessService accessService;

    @Override
    public String startSelectedProcess(Long agentId, String selectionMode, String businessKey, Map<String, Object> context) {
        AgentxProcessSelectionService.SelectionResult selected = processSelectionService
                .selectProcess(agentId, selectionMode, context);
        Map<String, Object> variables = new HashMap<>();
        if (context != null) {
            variables.putAll(context);
        }
        applySystemContext(variables);
        variables.put("agentId", agentId);
        variables.put("selectedProcessDefinitionId", selected.getProcessDefinitionId());
        variables.put("selectedProcessDefinitionKey", selected.getProcessDefinitionKey());
        variables.put("selectionReason", selected.getReason());

        ProcessInstance processInstance;
        if (StrUtil.isNotBlank(selected.getProcessDefinitionId())) {
            processInstance = runtimeService.startProcessInstanceById(selected.getProcessDefinitionId(), businessKey, variables);
        } else {
            processInstance = runtimeService.startProcessInstanceByKey(selected.getProcessDefinitionKey(), businessKey, variables);
        }
        return processInstance.getProcessInstanceId();
    }

    @SuppressWarnings("unchecked")
    private void applySystemContext(Map<String, Object> variables) {
        Object rawSystemContext = variables.get("systemEnforcedContext");
        if (rawSystemContext == null) {
            return;
        }
        AccessEnvelopeRespVO.SystemEnforcedContext context = toSystemContext(rawSystemContext);
        if (context == null) {
            return;
        }
        if (!accessService.verifyEnvelope(context)) {
            throw exception(ENVELOPE_SIGNATURE_INVALID);
        }
        Map<String, Object> contextMap = JsonUtils.parseObject(JsonUtils.toJsonString(context), Map.class);
        variables.put("systemEnforcedContext", contextMap);
        if (context.getAllowedActions() != null) {
            variables.put("allowedActions", context.getAllowedActions());
            variables.put("entitlementAllowedActions", context.getAllowedActions());
        }
        if (context.getResourceFilters() != null) {
            variables.put("resourceFilters", context.getResourceFilters());
        }
        if (context.getObligations() != null) {
            List<Map<String, Object>> obligations = JsonUtils.parseObject(
                    JsonUtils.toJsonString(context.getObligations()), List.class);
            variables.put("obligations", obligations == null ? Collections.emptyList() : obligations);
        }
        if (context.getUserId() != null) {
            variables.putIfAbsent("userId", context.getUserId());
        }
        if (StrUtil.isNotBlank(context.getChannelUserId())) {
            variables.put("channelUserId", context.getChannelUserId());
        }
        if (StrUtil.isNotBlank(context.getDecisionId())) {
            variables.put("decisionId", context.getDecisionId());
        }
        if (StrUtil.isNotBlank(context.getPolicyVersion())) {
            variables.put("policyVersion", context.getPolicyVersion());
        }
    }

    private AccessEnvelopeRespVO.SystemEnforcedContext toSystemContext(Object rawSystemContext) {
        if (rawSystemContext instanceof AccessEnvelopeRespVO.SystemEnforcedContext) {
            return (AccessEnvelopeRespVO.SystemEnforcedContext) rawSystemContext;
        }
        if (rawSystemContext instanceof Map || rawSystemContext instanceof String) {
            return JsonUtils.parseObject(JsonUtils.toJsonString(rawSystemContext),
                    AccessEnvelopeRespVO.SystemEnforcedContext.class);
        }
        return null;
    }

}
