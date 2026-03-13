package cn.iocoder.yudao.module.agentx.service.tool;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Tool 元数据描述。
 */
@Data
@Accessors(chain = true)
public class AgentxToolDescriptor {

    private String toolName;
    private AgentxToolSchema inputSchema;
    private AgentxToolSchema outputSchema;
    private AgentxToolPolicy policy;

}
