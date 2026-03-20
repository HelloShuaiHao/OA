package cn.iocoder.yudao.module.agentx.service.tool;

import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;

import java.util.List;

/**
 * `bpm_approve` Tool 描述符。
 */
public final class BpmApproveToolDescriptor {

    public static final String TOOL_NAME = "bpm_approve";

    private BpmApproveToolDescriptor() {
    }

    public static AgentxToolDescriptor build() {
        return new AgentxToolDescriptor()
                .setToolName(TOOL_NAME)
                .setInputSchema(new AgentxToolSchema()
                        .setSchemaType("object")
                        .setRequiredFields(List.of("task_id", "approved")))
                .setOutputSchema(new AgentxToolSchema()
                        .setSchemaType("object")
                        .setRequiredFields(List.of("approved")))
                .setPolicy(new AgentxToolPolicy()
                        .setRequiredCapability(AgentxCapability.UPDATE_LEAVE)
                        .setRiskLevel(30)
                        .setApprovalRequired(true)
                        .setAuditTags(List.of("bpm", "approve", "write")));
    }

}
