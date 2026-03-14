package cn.iocoder.yudao.module.agentx.service.identity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 人类执行主体。
 */
@Data
@AllArgsConstructor
public class HumanPrincipal {

    private Long userId;
    private Long tenantId;
    private String displayName;

}
