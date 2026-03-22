package cn.iocoder.yudao.module.system.controller.admin.dept.vo.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 部门成员 Response VO")
@Data
public class DeptMemberRespVO {

    @Schema(description = "用户编号", example = "1")
    private Long id;

    @Schema(description = "账号")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "用户类型（human/agent）")
    private String userType;

    @Schema(description = "关联 Agent ID")
    private Long agentId;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "状态")
    private Integer status;

}
