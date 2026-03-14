package cn.iocoder.yudao.module.agentx.service.tool;

import java.util.Map;

/**
 * Tool 适配器只暴露受控服务能力，不直接暴露表或 Mapper。
 */
public interface AgentxToolAdapter {

    String toolName();

    Map<String, Object> invoke(Map<String, Object> request);

}
