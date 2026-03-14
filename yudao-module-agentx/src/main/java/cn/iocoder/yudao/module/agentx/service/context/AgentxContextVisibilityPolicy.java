package cn.iocoder.yudao.module.agentx.service.context;

import java.util.LinkedHashMap;
import java.util.Map;

public class AgentxContextVisibilityPolicy {

    public Map<String, Object> filterForRuntime(ContextSnapshot snapshot) {
        Map<String, Object> visible = new LinkedHashMap<>();
        if (snapshot.getLayers() == null) {
            return visible;
        }
        snapshot.getLayers().forEach((layer, values) -> {
            if (layer != ContextLayer.SENSITIVE && values != null) {
                visible.putAll(values);
            }
        });
        return visible;
    }

}
