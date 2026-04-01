package cn.iocoder.yudao.module.agentx.controller.admin.entitlement.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "管理后台 - AgentX 权限配置更新 Request VO")
public class EntitlementConfigUpdateReqVO {

    private String deptName;

    private String jobTitle;

    private List<String> roleTags;

    private String workRegion;

    private List<String> allowedActions;

    private Map<String, Object> resourceFilters;

    private List<EntitlementObligationVO> obligations;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveUntil;

}
