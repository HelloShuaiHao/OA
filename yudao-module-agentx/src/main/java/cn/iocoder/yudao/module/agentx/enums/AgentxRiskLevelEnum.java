package cn.iocoder.yudao.module.agentx.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AgentX 风险等级枚举。
 */
@Getter
@AllArgsConstructor
public enum AgentxRiskLevelEnum {

    LOW(0),
    MEDIUM(10),
    HIGH(20),
    CRITICAL(30);

    private final Integer level;

}
