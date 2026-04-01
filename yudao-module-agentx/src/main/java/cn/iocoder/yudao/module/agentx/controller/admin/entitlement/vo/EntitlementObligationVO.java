package cn.iocoder.yudao.module.agentx.controller.admin.entitlement.vo;

import lombok.Data;

@Data
public class EntitlementObligationVO {

    private String action;

    private String requires;

    private String approverRole;

    private String reason;

}
