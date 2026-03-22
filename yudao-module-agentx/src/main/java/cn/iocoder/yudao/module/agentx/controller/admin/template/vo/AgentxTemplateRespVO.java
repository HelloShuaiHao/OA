package cn.iocoder.yudao.module.agentx.controller.admin.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AgentX 模板 Response VO")
@Data
public class AgentxTemplateRespVO {

    @Schema(description = "模板类型")
    private String templateType;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "模板图标")
    private String icon;

    @Schema(description = "默认能力 key 列表")
    private List<String> defaultCapabilities;

    @Schema(description = "推荐流程 key 列表")
    private List<String> recommendedProcessKeys;

    @Schema(description = "默认规则")
    private List<RuleItem> defaultRules;

    @Data
    public static class RuleItem {
        private String field;
        private String operator;
        private String value;
        private String processDefinitionKey;
    }

}
