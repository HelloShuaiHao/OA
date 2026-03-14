package cn.iocoder.yudao.module.agentx.service.tool;

import cn.iocoder.yudao.module.agentx.service.authorization.AgentxCapability;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentxToolRegistryServiceTest {

    @Test
    void shouldDriveToolLifecycleFromRegisterToDeprecate() {
        AgentxToolRegistryService service = new AgentxToolRegistryService();
        AgentxToolDescriptor descriptor = new AgentxToolDescriptor()
                .setToolName("leave.submit")
                .setInputSchema(new AgentxToolSchema().setSchemaType("object").setRequiredFields(List.of("leaveId")))
                .setOutputSchema(new AgentxToolSchema().setSchemaType("object").setRequiredFields(List.of("formId")))
                .setPolicy(new AgentxToolPolicy()
                        .setRequiredCapability(AgentxCapability.SUBMIT_LEAVE)
                        .setRiskLevel(30)
                        .setApprovalRequired(true)
                        .setAuditTags(List.of("leave", "write")));

        AgentxRegisteredTool registered = service.register(descriptor, "v1");
        AgentxRegisteredTool reviewed = service.submitForReview(registered, "architect-a");
        AgentxRegisteredTool enabled = service.approve(reviewed, "reviewer-a");
        AgentxRegisteredTool disabled = service.disable(enabled, "maintenance");
        AgentxRegisteredTool reenabled = service.enable(disabled);
        AgentxRegisteredTool nextVersion = service.publishNewVersion(reenabled, descriptor, "v2");
        AgentxRegisteredTool deprecated = service.deprecate(nextVersion, "replaced-by-v3");

        assertEquals(AgentxToolLifecycleStatus.DRAFT, registered.getStatus());
        assertEquals(AgentxToolLifecycleStatus.IN_REVIEW, reviewed.getStatus());
        assertEquals("architect-a", reviewed.getSubmittedBy());
        assertEquals(AgentxToolLifecycleStatus.ENABLED, enabled.getStatus());
        assertEquals("reviewer-a", enabled.getApprovedBy());
        assertEquals(AgentxToolLifecycleStatus.DISABLED, disabled.getStatus());
        assertEquals("maintenance", disabled.getStatusReason());
        assertEquals(AgentxToolLifecycleStatus.ENABLED, reenabled.getStatus());
        assertEquals("v2", nextVersion.getVersion());
        assertEquals(AgentxToolLifecycleStatus.DEPRECATED, deprecated.getStatus());
        assertEquals("replaced-by-v3", deprecated.getStatusReason());
    }

}
