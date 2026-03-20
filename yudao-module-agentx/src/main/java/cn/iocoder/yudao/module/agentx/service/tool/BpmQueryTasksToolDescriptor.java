package cn.iocoder.yudao.module.agentx.service.tool;

import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;

import java.util.List;

/**
 * `bpm_query_tasks` Tool 描述符。
 */
public final class BpmQueryTasksToolDescriptor {

    public static final String TOOL_NAME = "bpm_query_tasks";

    private BpmQueryTasksToolDescriptor() {
    }

    public static AgentxToolDescriptor build() {
        return new AgentxToolDescriptor()
                .setToolName(TOOL_NAME)
                .setInputSchema(new AgentxToolSchema()
                        .setSchemaType("object")
                        .setRequiredFields(List.of("user_id")))
                .setOutputSchema(new AgentxToolSchema()
                        .setSchemaType("object")
                        .setRequiredFields(List.of("tasks")))
                .setPolicy(new AgentxToolPolicy()
                        .setRequiredCapability(AgentxCapability.READ_LEAVE)
                        .setRiskLevel(10)
                        .setApprovalRequired(false)
                        .setAuditTags(List.of("bpm", "read")));
    }

}

