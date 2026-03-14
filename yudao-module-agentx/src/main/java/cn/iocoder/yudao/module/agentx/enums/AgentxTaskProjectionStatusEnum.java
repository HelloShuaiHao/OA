package cn.iocoder.yudao.module.agentx.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AgentX 任务投影状态枚举。
 */
@Getter
@AllArgsConstructor
public enum AgentxTaskProjectionStatusEnum {

    CREATED(0),
    RUNNING(10),
    WAITING_APPROVAL(20),
    APPROVED(30),
    REJECTED(40),
    SUCCEEDED(50),
    FAILED(60),
    COMPENSATED(70);

    private final Integer status;

}
