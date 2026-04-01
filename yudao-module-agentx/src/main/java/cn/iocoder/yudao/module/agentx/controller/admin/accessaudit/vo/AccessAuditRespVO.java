package cn.iocoder.yudao.module.agentx.controller.admin.accessaudit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理后台 - AgentX 访问审计 Response VO")
public class AccessAuditRespVO {

    private Long id;

    private String decisionId;

    private Long userId;

    private String channelUserId;

    private String agentId;

    private String conversationScope;

    private String action;

    private String resourceType;

    private String resourceId;

    private String decision;

    private String denyReason;

    private String policyVersion;

    private LocalDateTime requestTime;

}
