package cn.iocoder.yudao.module.agentx.service.tool;

public class AgentxToolRegistryService {

    public AgentxRegisteredTool register(AgentxToolDescriptor descriptor, String version) {
        return new AgentxRegisteredTool()
                .setToolName(descriptor.getToolName())
                .setVersion(version)
                .setDescriptor(descriptor)
                .setStatus(AgentxToolLifecycleStatus.DRAFT);
    }

    public AgentxRegisteredTool submitForReview(AgentxRegisteredTool tool, String submittedBy) {
        return copy(tool)
                .setStatus(AgentxToolLifecycleStatus.IN_REVIEW)
                .setSubmittedBy(submittedBy)
                .setStatusReason(null);
    }

    public AgentxRegisteredTool approve(AgentxRegisteredTool tool, String approvedBy) {
        return copy(tool)
                .setStatus(AgentxToolLifecycleStatus.ENABLED)
                .setApprovedBy(approvedBy)
                .setStatusReason(null);
    }

    public AgentxRegisteredTool disable(AgentxRegisteredTool tool, String reason) {
        return copy(tool)
                .setStatus(AgentxToolLifecycleStatus.DISABLED)
                .setStatusReason(reason);
    }

    public AgentxRegisteredTool enable(AgentxRegisteredTool tool) {
        return copy(tool)
                .setStatus(AgentxToolLifecycleStatus.ENABLED)
                .setStatusReason(null);
    }

    public AgentxRegisteredTool publishNewVersion(AgentxRegisteredTool tool, AgentxToolDescriptor descriptor, String version) {
        return new AgentxRegisteredTool()
                .setToolName(descriptor.getToolName())
                .setVersion(version)
                .setDescriptor(descriptor)
                .setSubmittedBy(tool.getSubmittedBy())
                .setApprovedBy(tool.getApprovedBy())
                .setStatus(AgentxToolLifecycleStatus.DRAFT);
    }

    public AgentxRegisteredTool deprecate(AgentxRegisteredTool tool, String reason) {
        return copy(tool)
                .setStatus(AgentxToolLifecycleStatus.DEPRECATED)
                .setStatusReason(reason);
    }

    private AgentxRegisteredTool copy(AgentxRegisteredTool tool) {
        return new AgentxRegisteredTool()
                .setToolName(tool.getToolName())
                .setVersion(tool.getVersion())
                .setDescriptor(tool.getDescriptor())
                .setStatus(tool.getStatus())
                .setSubmittedBy(tool.getSubmittedBy())
                .setApprovedBy(tool.getApprovedBy())
                .setStatusReason(tool.getStatusReason());
    }

}
