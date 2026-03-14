package cn.iocoder.yudao.module.agentx.service.task;

import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Set;

/**
 * AgentX 任务启动请求。
 */
@Data
@Accessors(chain = true)
public class AgentxTaskStartRequest {

    private String scenarioCode;
    private String workflowVersion;
    private String businessKey;
    private String idempotencyKey;
    private String principalType;
    private String principalId;
    private Set<AgentxCapability> capabilities;
    private Map<String, Object> contextBundle;

}
