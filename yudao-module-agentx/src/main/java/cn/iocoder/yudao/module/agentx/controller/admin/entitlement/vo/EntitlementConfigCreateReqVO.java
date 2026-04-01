package cn.iocoder.yudao.module.agentx.controller.admin.entitlement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "管理后台 - AgentX 权限配置创建 Request VO")
public class EntitlementConfigCreateReqVO {

    @NotNull(message = "userId 不能为空")
    private Long userId;

    private String agentId;

    private String deptName;

    private String jobTitle;

    private List<String> roleTags;

    private String workRegion;

    @NotEmpty(message = "allowedActions 不能为空")
    private List<String> allowedActions;

    private Map<String, Object> resourceFilters;

    private List<EntitlementObligationVO> obligations;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveUntil;

}
