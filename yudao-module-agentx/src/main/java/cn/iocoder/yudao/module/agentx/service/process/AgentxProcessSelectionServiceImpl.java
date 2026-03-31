package cn.iocoder.yudao.module.agentx.service.process;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentProcessDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.agent.AgentxAgentProcessMapper;
import cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
@Slf4j
public class AgentxProcessSelectionServiceImpl implements AgentxProcessSelectionService {

    private static final String DEEPSEEK_BASE_URL = "https://api.deepseek.com";

    @Resource
    private AgentxAgentProcessMapper processMapper;
    private final RestTemplate llmRestTemplate;

    @Value("${spring.ai.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${spring.ai.deepseek.base-url:}")
    private String deepseekBaseUrl;

    @Value("${spring.ai.deepseek.chat.options.model:deepseek-chat}")
    private String deepseekModel;

    public AgentxProcessSelectionServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.llmRestTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Override
    public SelectionResult selectProcess(Long agentId, String selectionMode, Map<String, Object> context) {
        List<AgentxAgentProcessDO> processes = processMapper.selectListByAgentId(agentId);
        if (CollUtil.isEmpty(processes)) {
            throw exception(ErrorCodeConstants.AGENT_PROCESS_REQUIRED);
        }
        if (processes.size() == 1) {
            return toResult(processes.get(0), "单流程默认命中");
        }

        String mode = StrUtil.blankToDefault(selectionMode, processes.get(0).getSelectionMode());
        if ("auto".equalsIgnoreCase(mode)) {
            return selectByAuto(processes, context);
        }
        return selectByRule(processes, context);
    }

    private SelectionResult selectByRule(List<AgentxAgentProcessDO> processes, Map<String, Object> context) {
        List<SelectionRule> rules = parseRules(processes.get(0).getSelectionRules());
        if (CollUtil.isEmpty(rules)) {
            return toResult(processes.get(0), "规则为空，按优先级降级");
        }
        for (SelectionRule rule : rules) {
            if (!matchRule(rule, context)) {
                continue;
            }
            AgentxAgentProcessDO target = findProcess(processes, rule.getProcessDefinitionId(), rule.getProcessDefinitionKey());
            if (target != null) {
                return toResult(target, "规则命中：" + rule.getField() + " " + rule.getOperator() + " " + rule.getValue());
            }
        }
        return toResult(processes.get(0), "无匹配规则，按优先级降级");
    }

    private SelectionResult selectByAuto(List<AgentxAgentProcessDO> processes, Map<String, Object> context) {
        List<SelectionRule> rules = parseRules(processes.get(0).getSelectionRules());
        SelectionResult llmResult = selectByLlm(processes, context, rules);
        if (llmResult != null) {
            return llmResult;
        }
        if (CollUtil.isEmpty(rules)) {
            return toResult(processes.get(0), "AI 自动选择降级：无规则描述");
        }
        for (SelectionRule rule : rules) {
            if (matchRule(rule, context)) {
                AgentxAgentProcessDO target = findProcess(processes, rule.getProcessDefinitionId(), rule.getProcessDefinitionKey());
                if (target != null) {
                    return toResult(target, "AI 自动选择降级：规则命中");
                }
            }
        }
        return toResult(processes.get(0), "AI 自动选择降级：按优先级");
    }

    private SelectionResult selectByLlm(List<AgentxAgentProcessDO> processes, Map<String, Object> context, List<SelectionRule> rules) {
        if (CollUtil.size(processes) <= 1 || context == null || context.isEmpty() || StrUtil.isBlank(deepseekApiKey)) {
            return null;
        }
        try {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("model", StrUtil.blankToDefault(deepseekModel, "deepseek-chat"));
            request.put("temperature", 0);
            request.put("max_tokens", 120);
            request.put("enable_thinking", false);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(buildMessage("system", buildSelectionSystemPrompt()));
            messages.add(buildMessage("user", buildSelectionUserPrompt(processes, context, rules)));
            request.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepseekApiKey.trim());
            HttpEntity<String> entity = new HttpEntity<>(JsonUtils.toJsonString(request), headers);

            String endpoint = StrUtil.blankToDefault(deepseekBaseUrl, DEEPSEEK_BASE_URL).replaceAll("/$", "")
                    + "/chat/completions";
            String body = llmRestTemplate.postForObject(endpoint, entity, String.class);
            if (StrUtil.isBlank(body)) {
                return null;
            }
            JsonNode root = JsonUtils.parseTree(body);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (StrUtil.isBlank(content)) {
                return null;
            }
            LlmSelectionResult result = parseSelectionContent(content);
            if (result == null) {
                return null;
            }
            AgentxAgentProcessDO target = findProcess(processes, result.getProcessDefinitionId(), result.getProcessDefinitionKey());
            if (target == null) {
                return null;
            }
            String reason = StrUtil.blankToDefault(result.getReason(), "命中最匹配流程");
            return toResult(target, "AI 语义选择：" + StrUtil.maxLength(reason, 80));
        } catch (Exception ex) {
            log.warn("[selectByLlm][llm selection failed, fallback to rules]", ex);
            return null;
        }
    }

    private Map<String, String> buildMessage(String role, String content) {
        Map<String, String> message = new LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String buildSelectionSystemPrompt() {
        return "你是 OA 流程分类器。"
                + "根据用户上下文，只选择一个最匹配的流程。"
                + "只输出一行内容：如果能稳定输出 JSON，就输出"
                + "{\"processDefinitionKey\":\"...\",\"reason\":\"简短中文\"}；"
                + "否则只输出 processDefinitionKey。"
                + "不要 markdown，不要解释，不要输出候选之外的值。";
    }

    private String buildSelectionUserPrompt(List<AgentxAgentProcessDO> processes, Map<String, Object> context,
                                            List<SelectionRule> rules) {
        List<Map<String, Object>> processList = new ArrayList<>();
        for (AgentxAgentProcessDO process : processes) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("processDefinitionKey", process.getProcessDefinitionKey());
            item.put("processName", process.getProcessName());
            processList.add(item);
        }
        return "候选流程：" + JsonUtils.toJsonString(processList) + "\n"
                + "用户上下文：" + JsonUtils.toJsonString(context) + "\n"
                + "参考规则：" + JsonUtils.toJsonString(rules);
    }

    private LlmSelectionResult parseSelectionContent(String content) {
        String text = cleanJsonContent(content);
        if (StrUtil.startWith(text, "{")) {
            return JsonUtils.parseObject(text, LlmSelectionResult.class);
        }
        LlmSelectionResult result = new LlmSelectionResult();
        result.setProcessDefinitionKey(StrUtil.trim(text));
        return result;
    }

    private String cleanJsonContent(String content) {
        String text = StrUtil.trim(content);
        if (StrUtil.startWith(text, "```")) {
            text = text.replaceFirst("^```json", "");
            text = text.replaceFirst("^```", "");
            text = text.replaceFirst("```$", "");
        }
        return StrUtil.trim(text);
    }

    private AgentxAgentProcessDO findProcess(List<AgentxAgentProcessDO> processes, String processDefinitionId, String processDefinitionKey) {
        for (AgentxAgentProcessDO process : processes) {
            if (StrUtil.isNotBlank(processDefinitionId) && StrUtil.equals(processDefinitionId, process.getProcessDefinitionId())) {
                return process;
            }
            if (StrUtil.isNotBlank(processDefinitionKey) && StrUtil.equals(processDefinitionKey, process.getProcessDefinitionKey())) {
                return process;
            }
        }
        return null;
    }

    private List<SelectionRule> parseRules(String rulesJson) {
        if (StrUtil.isBlank(rulesJson)) {
            return Collections.emptyList();
        }
        List<SelectionRule> rules = JsonUtils.parseArray(rulesJson, SelectionRule.class);
        return rules == null ? Collections.emptyList() : rules;
    }

    private boolean matchRule(SelectionRule rule, Map<String, Object> context) {
        if (context == null || rule == null || StrUtil.isBlank(rule.getField()) || StrUtil.isBlank(rule.getOperator())) {
            return false;
        }
        Object actual = context.get(rule.getField());
        if (actual == null) {
            return false;
        }
        String operator = rule.getOperator().trim();
        Object expected = rule.getValue();
        switch (operator) {
            case "=":
                return ObjectUtil.equal(String.valueOf(actual), String.valueOf(expected));
            case "!=":
                return !ObjectUtil.equal(String.valueOf(actual), String.valueOf(expected));
            case ">":
                return compareNumber(actual, expected) > 0;
            case "<":
                return compareNumber(actual, expected) < 0;
            case ">=":
                return compareNumber(actual, expected) >= 0;
            case "<=":
                return compareNumber(actual, expected) <= 0;
            case "in":
                return isIn(actual, expected);
            case "contains":
                return StrUtil.contains(String.valueOf(actual), String.valueOf(expected));
            default:
                return false;
        }
    }

    private int compareNumber(Object actual, Object expected) {
        BigDecimal a = new BigDecimal(String.valueOf(actual));
        BigDecimal b = new BigDecimal(String.valueOf(expected));
        return a.compareTo(b);
    }

    @SuppressWarnings("unchecked")
    private boolean isIn(Object actual, Object expected) {
        if (expected instanceof Collection) {
            return ((Collection<Object>) expected).contains(actual);
        }
        if (expected instanceof String) {
            String raw = ((String) expected).trim();
            if (StrUtil.startWith(raw, "[") && StrUtil.endWith(raw, "]")) {
                List<String> list = JsonUtils.parseArray(raw, String.class);
                return list != null && list.contains(String.valueOf(actual));
            }
            return StrUtil.split(raw, ',').stream().map(String::trim)
                    .anyMatch(item -> StrUtil.equals(item, String.valueOf(actual)));
        }
        return false;
    }

    private SelectionResult toResult(AgentxAgentProcessDO process, String reason) {
        return new SelectionResult()
                .setProcessDefinitionId(process.getProcessDefinitionId())
                .setProcessDefinitionKey(process.getProcessDefinitionKey())
                .setProcessName(process.getProcessName())
                .setReason(reason);
    }

    @Data
    public static class SelectionRule {
        private String field;
        private String operator;
        private String value;
        private String processDefinitionId;
        private String processDefinitionKey;
    }

    @Data
    public static class LlmSelectionResult {
        private String processDefinitionId;
        private String processDefinitionKey;
        private String reason;
    }

}
