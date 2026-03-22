package cn.iocoder.yudao.module.agentx.service.decision;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

/**
 * AgentX AI 决策服务默认实现。
 *
 * 当前阶段提供稳定可测的规则化兜底逻辑，后续可替换为真实 LLM 调用。
 */
@Service
public class AgentxAiDecisionServiceImpl implements AgentxAiDecisionService {

    @Override
    public String decide(String promptTemplate, Map<String, Object> inputVariables) {
        Map<String, Object> variables = inputVariables == null ? Collections.emptyMap() : inputVariables;
        String renderedPrompt = renderPrompt(promptTemplate, variables);

        // 基于常见审批语义做基础决策，保证节点可运行可测试
        Object leaveDaysObj = variables.get("leaveDays");
        Integer leaveDays = toInteger(leaveDaysObj);
        if (StrUtil.containsAnyIgnoreCase(renderedPrompt, "请假", "审批", "approval", "approve")) {
            if (leaveDays != null) {
                return leaveDays > 2 ? "需要审批" : "无需审批";
            }
            return "需要审批";
        }

        // 默认输出渲染后的 Prompt，便于在流程中追踪决策输入
        return renderedPrompt;
    }

    private String renderPrompt(String template, Map<String, Object> variables) {
        String prompt = StrUtil.blankToDefault(template, "");
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = "${" + entry.getKey() + "}";
            String value = String.valueOf(entry.getValue());
            prompt = StrUtil.replace(prompt, key, value);
        }
        return prompt;
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }
}
