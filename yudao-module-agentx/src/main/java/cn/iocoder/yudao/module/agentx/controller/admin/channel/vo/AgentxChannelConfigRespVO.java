package cn.iocoder.yudao.module.agentx.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Schema(description = "管理后台 - 渠道配置 Response VO")
@Data
public class AgentxChannelConfigRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "渠道类型")
    private String channelType;

    @Schema(description = "渠道名称")
    private String channelName;

    @Schema(description = "关联 Agent ID 列表")
    private List<Long> agentIds;

    @Schema(description = "访问控制类型")
    private String accessControlType;

    @Schema(description = "部门 ID 列表")
    private List<Long> deptIds;

    @Schema(description = "用户 ID 列表")
    private List<Long> userIds;

    @Schema(description = "Agent 认证策略列表")
    private List<AgentAccessPolicyItem> agentAccessPolicies = Collections.emptyList();

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Data
    public static class AgentAccessPolicyItem {
        @Schema(description = "Agent ID")
        private Long agentId;

        @Schema(description = "认证模式（public/bind_required）")
        private String authMode;
    }

}
