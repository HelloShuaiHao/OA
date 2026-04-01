package cn.iocoder.yudao.module.agentx.service.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BpmToolDescriptorTest {

    @Test
    void shouldBuildBpmQueryTasksDescriptor() {
        AgentxToolDescriptor descriptor = BpmQueryTasksToolDescriptor.build();

        assertEquals("bpm_query_tasks", descriptor.getToolName());
        assertEquals("object", descriptor.getInputSchema().getSchemaType());
        assertTrue(descriptor.getInputSchema().getRequiredFields().contains("user_id"));
        assertEquals(Integer.valueOf(10), descriptor.getPolicy().getRiskLevel());
        assertTrue(descriptor.getPolicy().getRequiredActions().contains("bpm.task.read"));
    }

    @Test
    void shouldBuildBpmApproveDescriptor() {
        AgentxToolDescriptor descriptor = BpmApproveToolDescriptor.build();

        assertEquals("bpm_approve", descriptor.getToolName());
        assertTrue(descriptor.getInputSchema().getRequiredFields().contains("task_id"));
        assertTrue(descriptor.getInputSchema().getRequiredFields().contains("approved"));
        assertEquals(Integer.valueOf(30), descriptor.getPolicy().getRiskLevel());
        assertTrue(descriptor.getPolicy().getApprovalRequired());
        assertTrue(descriptor.getPolicy().getRequiredActions().contains("bpm.task.approve"));
    }

}
