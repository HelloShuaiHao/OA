package cn.iocoder.yudao.module.agentx.service.audit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AgentxOaFieldVisibilityPolicy {

    private static final Set<String> HIDDEN_FIELDS = Set.of("promptLoop", "memoryState", "reasoningTrace");

    public Map<String, Object> filter(Map<String, Object> payload) {
        Map<String, Object> visible = new LinkedHashMap<>();
        payload.forEach((key, value) -> {
            if (!HIDDEN_FIELDS.contains(key)) {
                visible.put(key, value);
            }
        });
        return visible;
    }

}
