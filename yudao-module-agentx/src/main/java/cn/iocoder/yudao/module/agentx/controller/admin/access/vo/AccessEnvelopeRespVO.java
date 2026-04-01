package cn.iocoder.yudao.module.agentx.controller.admin.access.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "管理后台 - AgentX 访问评估响应 Envelope")
public class AccessEnvelopeRespVO {

    @Schema(description = "是否允许继续触发能力（旧协议兼容）")
    private Boolean accessGranted;

    @Schema(description = "是否要求绑定（旧协议兼容）")
    private Boolean authRequired;

    @Schema(description = "当前渠道用户是否已绑定（旧协议兼容）")
    private Boolean bound;

    @Schema(description = "Agent 认证模式（旧协议兼容）")
    private String authMode;

    @Schema(description = "渠道访问控制类型（旧协议兼容）")
    private String accessControlType;

    @Schema(description = "访问决策", requiredMode = Schema.RequiredMode.REQUIRED, example = "ALLOW")
    private String accessDecision;

    @Schema(description = "Model Visible 上下文")
    private ModelVisibleContext modelVisibleContext;

    @Schema(description = "System Enforced 上下文")
    private SystemEnforcedContext systemEnforcedContext;

    @Schema(description = "绑定链接（当 accessDecision=REQUIRE_BINDING 时返回）")
    private String bindingUrl;

    @Schema(description = "绑定链接（旧协议兼容）")
    private String bindUrl;

    @Schema(description = "绑定 Token（旧协议兼容）")
    private String bindToken;

    @Schema(description = "提示消息（旧协议兼容）")
    private String message;

    @Data
    public static class ModelVisibleContext {

        private SubjectProfile subjectProfile;

        private CapabilitySummary capabilitySummary;
    }

    @Data
    public static class SubjectProfile {

        private String displayName;

        private String deptName;

        private String jobTitle;

        private List<String> roleTags;

        private String workRegion;
    }

    @Data
    public static class CapabilitySummary {

        private List<String> canDo;

        private List<String> cannotDo;

        private List<String> requiresApproval;
    }

    @Data
    public static class SystemEnforcedContext {

        private Long userId;

        private String channelUserId;

        private String agentId;

        private List<String> allowedActions;

        private Map<String, Object> resourceFilters;

        private List<Obligation> obligations;

        private String policyVersion;

        private String decisionId;

        private LocalDateTime issuedAt;

        private LocalDateTime expiresAt;

        private String signature;
    }

    @Data
    public static class Obligation {

        private String action;

        private String requires;

        private String approverRole;

        private String reason;
    }

}
