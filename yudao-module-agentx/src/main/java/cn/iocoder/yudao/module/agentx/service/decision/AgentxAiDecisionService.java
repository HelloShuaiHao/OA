package cn.iocoder.yudao.module.agentx.service.decision;

import java.util.Map;

/**
 * AgentX AI 决策服务。
 */
public interface AgentxAiDecisionService {

    /**
     * 基于 prompt 和输入变量给出决策结果。
     *
     * @param promptTemplate prompt 模板，可包含 ${var} 变量
     * @param inputVariables 输入变量
     * @return 决策结果文本
     */
    String decide(String promptTemplate, Map<String, Object> inputVariables);
}
