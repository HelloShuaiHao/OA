package cn.iocoder.yudao.module.agentx.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审批详情读取模式。
 */
@Getter
@AllArgsConstructor
public enum AgentxApprovalDetailFetchModeEnum {

    EXACT_QUERY("exact_query"),
    LOCAL_INDEX("local_index");

    private final String mode;

}
