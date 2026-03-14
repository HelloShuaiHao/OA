package cn.iocoder.yudao.module.agentx.service.context;

/**
 * AgentX 上下文提供器。
 */
public interface AgentxContextProvider {

    ContextContribution provide(AgentxContextRequest request);

}
