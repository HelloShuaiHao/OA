package cn.iocoder.yudao.module.agentx.service.context;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * 上下文组装请求。
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AgentxContextRequest {

    private Long userId;
    private String scenarioCode;
    private String businessKey;
    private Map<String, Object> seedContext = new HashMap<>();

    public AgentxContextRequest(String scenarioCode, String businessKey, Map<String, Object> seedContext) {
        this.scenarioCode = scenarioCode;
        this.businessKey = businessKey;
        this.seedContext = seedContext;
    }

}
