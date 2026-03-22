package cn.iocoder.yudao.module.agentx.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "管理后台 - AgentX 数字员工详情 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentxAgentDetailRespVO extends AgentxAgentRespVO {

    @Schema(description = "能力列表")
    private List<CapabilityItem> capabilities;

    @Schema(description = "流程列表")
    private List<ProcessItem> processes;

    @Schema(description = "流程选择模式（rule/auto）")
    private String selectionMode;

    @Schema(description = "流程选择规则")
    private List<SelectionRuleItem> selectionRules;

    @Data
    public static class CapabilityItem {
        private String capabilityKey;
        private String capabilityName;
        private Boolean enabled;
        private Integer maxCallsPerHour;
        private String conditions;
    }

    @Data
    public static class ProcessItem {
        private String processDefinitionId;
        private String processDefinitionKey;
        private String processName;
        private Integer processVersion;
        private Integer priority;
    }

    @Data
    public static class SelectionRuleItem {
        private String field;
        private String operator;
        private String value;
        private String processDefinitionId;
        private String processDefinitionKey;
    }

}
