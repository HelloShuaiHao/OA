package cn.iocoder.yudao.module.agentx.service.process;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.agentx.dal.dataobject.agent.AgentxAgentProcessDO;
import cn.iocoder.yudao.module.agentx.dal.mysql.agent.AgentxAgentProcessMapper;
import cn.iocoder.yudao.module.agentx.enums.ErrorCodeConstants;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class AgentxProcessSelectionServiceImpl implements AgentxProcessSelectionService {

    @Resource
    private AgentxAgentProcessMapper processMapper;

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
        if (CollUtil.isEmpty(rules)) {
            return toResult(processes.get(0), "AI 自动选择降级：无规则描述");
        }
        for (SelectionRule rule : rules) {
            if (matchRule(rule, context)) {
                AgentxAgentProcessDO target = findProcess(processes, rule.getProcessDefinitionId(), rule.getProcessDefinitionKey());
                if (target != null) {
                    return toResult(target, "AI 自动选择（基于规则语义命中）");
                }
            }
        }
        return toResult(processes.get(0), "AI 自动选择降级：按优先级");
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

}
