package cn.iocoder.yudao.module.agentx.service.tool;

import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentxToolDescriptorTest {

    @Test
    void shouldDescribeHighRiskSubmitTool() {
        AgentxToolDescriptor descriptor = new AgentxToolDescriptor()
                .setToolName("leave.submit")
                .setInputSchema(new AgentxToolSchema().setSchemaType("object").setRequiredFields(List.of("leaveId")))
                .setOutputSchema(new AgentxToolSchema().setSchemaType("object").setRequiredFields(List.of("formId")))
                .setPolicy(new AgentxToolPolicy()
                        .setRequiredCapability(AgentxCapability.SUBMIT_LEAVE)
                        .setRiskLevel(30)
                        .setApprovalRequired(true)
                        .setAuditTags(List.of("leave", "write")));

        assertEquals("leave.submit", descriptor.getToolName());
        assertEquals(30, descriptor.getPolicy().getRiskLevel());
        assertTrue(descriptor.getPolicy().getAuditTags().contains("write"));
        assertEquals(List.of("leaveId"), descriptor.getInputSchema().getRequiredFields());
    }

}
