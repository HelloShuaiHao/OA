package cn.iocoder.yudao.module.agentx.framework.flowable.delegate;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.agentx.service.decision.AgentxAiDecisionService;
import cn.iocoder.yudao.module.agentx.service.process.AgentxProcessSelectionService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * AgentX 扩展节点：AI 选择流程。
 */
@Component("agentxAiDecisionDelegate")
@Slf4j
public class AiDecisionDelegate implements JavaDelegate {

    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final String DEFAULT_OUTPUT_VARIABLE = "aiDecision";

    @Resource
    private AgentxProcessSelectionService processSelectionService;
    @Resource
    private AgentxAiDecisionService aiDecisionService;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(DelegateExecution execution) {
        // Phase C: prompt 存在时走 AI 决策模式；否则兼容 Phase A 的流程选择模式
        String promptTemplate = firstNonBlank(
                (String) execution.getVariable("aiPromptTemplate"),
                (String) execution.getVariable("prompt"));
        if (StrUtil.isNotBlank(promptTemplate)) {
            executeAiDecision(execution, promptTemplate);
            return;
        }
        executeProcessSelection(execution);
    }

    private void executeProcessSelection(DelegateExecution execution) {
        Long agentId = parseLong(execution.getVariable("agentId"));
        if (agentId == null) {
            log.warn("[AiDecisionDelegate] missing agentId, skip");
            return;
        }
        String selectionMode = StrUtil.blankToDefault((String) execution.getVariable("selectionMode"), "auto");
        Object contextObj = execution.getVariable("selectionContext");
        Map<String, Object> context = Collections.emptyMap();
        if (contextObj instanceof Map) {
            context = (Map<String, Object>) contextObj;
        } else if (contextObj instanceof String && StrUtil.isNotBlank((String) contextObj)) {
            Map<String, Object> parsed = JsonUtils.parseObject((String) contextObj, Map.class);
            context = parsed == null ? Collections.emptyMap() : parsed;
        }

        AgentxProcessSelectionService.SelectionResult result =
                processSelectionService.selectProcess(agentId, selectionMode, context);
        execution.setVariable("selectedProcessDefinitionId", result.getProcessDefinitionId());
        execution.setVariable("selectedProcessDefinitionKey", result.getProcessDefinitionKey());
        execution.setVariable("selectionReason", result.getReason());
    }

    private void executeAiDecision(DelegateExecution execution, String promptTemplate) {
        Map<String, Object> inputVariables = resolveInputVariables(execution);
        int timeoutSeconds = resolveTimeoutSeconds(execution);
        String outputVariable = firstNonBlank(
                (String) execution.getVariable("aiOutputVariable"),
                (String) execution.getVariable("outputVariableName"),
                DEFAULT_OUTPUT_VARIABLE);
        FutureTask<String> task = new FutureTask<>(() -> aiDecisionService.decide(promptTemplate, inputVariables));
        Thread worker = new Thread(task, "agentx-ai-decision");
        worker.setDaemon(true);
        worker.start();
        try {
            String decision = task.get(timeoutSeconds, TimeUnit.SECONDS);
            execution.setVariable(outputVariable, decision);
            execution.setVariable("aiDecisionStatus", "SUCCESS");
            execution.setVariable("aiDecisionError", null);
        } catch (TimeoutException ex) {
            task.cancel(true);
            execution.setVariable(outputVariable, "TIMEOUT");
            execution.setVariable("aiDecisionStatus", "TIMEOUT");
            execution.setVariable("aiDecisionError", "AI 决策超时");
            log.warn("[AiDecisionDelegate] timeout after {}s", timeoutSeconds);
        } catch (Exception ex) {
            execution.setVariable(outputVariable, "ERROR");
            execution.setVariable("aiDecisionStatus", "FAILED");
            execution.setVariable("aiDecisionError", ex.getMessage());
            log.error("[AiDecisionDelegate] decision failed", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveInputVariables(DelegateExecution execution) {
        Object raw = execution.getVariable("aiInputVariables");
        if (raw instanceof Map) {
            return new LinkedHashMap<>((Map<String, Object>) raw);
        }
        if (raw instanceof String && StrUtil.isNotBlank((String) raw)) {
            String text = (String) raw;
            if (StrUtil.startWith(text.trim(), "{")) {
                Map<String, Object> parsed = JsonUtils.parseObject(text, Map.class);
                return parsed == null ? Collections.emptyMap() : parsed;
            }
            List<String> variableNames = parseVariableNames(text);
            Map<String, Object> variables = new LinkedHashMap<>();
            for (String variableName : variableNames) {
                variables.put(variableName, execution.getVariable(variableName));
            }
            return variables;
        }
        return Collections.emptyMap();
    }

    private List<String> parseVariableNames(String text) {
        List<String> names = new ArrayList<>();
        String[] split = StrUtil.splitToArray(text, ',');
        if (split == null) {
            return names;
        }
        for (String item : split) {
            String value = StrUtil.trim(item);
            if (StrUtil.isNotBlank(value)) {
                names.add(value);
            }
        }
        return names;
    }

    private int resolveTimeoutSeconds(DelegateExecution execution) {
        Long timeout = parseLong(firstNonBlank(
                String.valueOf(execution.getVariable("aiTimeoutSeconds")),
                String.valueOf(execution.getVariable("timeoutSeconds"))));
        if (timeout == null || timeout <= 0) {
            return DEFAULT_TIMEOUT_SECONDS;
        }
        return timeout.intValue();
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

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

}
