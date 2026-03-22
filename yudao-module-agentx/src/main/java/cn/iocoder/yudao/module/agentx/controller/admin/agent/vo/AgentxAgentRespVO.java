package cn.iocoder.yudao.module.agentx.controller.admin.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AgentX 数字员工 Response VO")
@Data
public class AgentxAgentRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "员工名称", example = "请假审批助手")
    private String agentName;

    @Schema(description = "员工 Key", example = "8b2244a8a5ad4f2dbf2b5a7ad5bc9a27")
    private String agentKey;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "头像 URL")
    private String avatarUrl;

    @Schema(description = "部门 ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "状态（0=草稿,1=激活,2=停用）")
    private Integer status;

    @Schema(description = "模板类型")
    private String templateType;

    @Schema(description = "配置版本")
    private Integer configVersion;

    @Schema(description = "同步状态（0未同步 1成功 2失败）")
    private Integer lastSyncStatus;

    @Schema(description = "最近同步时间")
    private LocalDateTime lastSyncTime;

    @Schema(description = "最近同步摘要")
    private String lastSyncMessage;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "更新人")
    private String updater;

}
