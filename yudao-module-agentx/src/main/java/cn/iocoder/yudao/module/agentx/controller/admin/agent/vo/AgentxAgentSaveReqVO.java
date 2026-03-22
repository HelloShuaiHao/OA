package cn.iocoder.yudao.module.agentx.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - AgentX 数字员工新增/修改 Request VO")
@Data
public class AgentxAgentSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "员工名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "员工名称不能为空")
    private String agentName;

    @Schema(description = "员工描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "员工描述不能为空")
    private String description;

    @Schema(description = "部门 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "部门不能为空")
    private Long deptId;

    @Schema(description = "头像 URL")
    private String avatarUrl;

    @Schema(description = "模板类型")
    private String templateType;

    @Schema(description = "能力列表")
    @Valid
    private List<CapabilityItem> capabilities;

    @Schema(description = "流程列表")
    @Valid
    private List<ProcessItem> processes;

    @Schema(description = "流程选择模式（rule/auto）")
    private String selectionMode;

    @Schema(description = "流程选择规则")
    @Valid
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
