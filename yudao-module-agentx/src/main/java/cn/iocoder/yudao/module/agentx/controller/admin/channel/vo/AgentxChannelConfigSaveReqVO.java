package cn.iocoder.yudao.module.agentx.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 渠道配置新增/修改 Request VO")
@Data
public class AgentxChannelConfigSaveReqVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "渠道类型（telegram/wecom/dingtalk）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "渠道类型不能为空")
    private String channelType;

    @Schema(description = "渠道名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "渠道名称不能为空")
    private String channelName;

    @Schema(description = "Bot Token（创建必填，更新可选）")
    private String botToken;

    @Schema(description = "关联 Agent ID 列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "至少关联一个 Agent")
    private List<Long> agentIds;

    @Schema(description = "访问控制类型（all/dept/user）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "访问控制类型不能为空")
    private String accessControlType;

    @Schema(description = "部门 ID 列表")
    private List<Long> deptIds;

    @Schema(description = "用户 ID 列表")
    private List<Long> userIds;

    @Schema(description = "状态（1启用 0停用）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

}
