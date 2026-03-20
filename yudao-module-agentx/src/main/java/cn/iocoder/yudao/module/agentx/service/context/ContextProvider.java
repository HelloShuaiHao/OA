package cn.iocoder.yudao.module.agentx.service.context;

import java.util.Map;

/**
 * 动态上下文 Provider。
 */
public interface ContextProvider {

    /**
     * Provider 类型，对应场景配置中的 contextProviders[*].type。
     */
    String getType();

    /**
     * 生成上下文。
     */
    Map<String, Object> provide(ContextRequest request);

}
