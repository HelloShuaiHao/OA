package cn.iocoder.yudao.module.agentx.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * AgentX 错误码枚举类
 *
 * agentx 模块，使用 1-024-000-000 段
 */
public interface ErrorCodeConstants {

    ErrorCode SCENARIO_DISABLED = new ErrorCode(1_024_001_001, "场景已禁用，不能发起 OpenFang 执行");
    ErrorCode WORKFLOW_VERSION_MISMATCH = new ErrorCode(1_024_001_002, "OpenFang workflow 版本不匹配，期望为({})，当前为({})");
    ErrorCode SCENARIO_WORKFLOW_MAPPING_NOT_EXISTS = new ErrorCode(1_024_001_003, "场景({})未配置 OpenFang workflow 映射");
    ErrorCode AUTHORIZATION_DENIED = new ErrorCode(1_024_001_004, "权限求交失败，缺少能力({})");
    ErrorCode DELEGATION_EXPIRED = new ErrorCode(1_024_001_005, "委托已过期或失效，不能继续代理执行");
    ErrorCode DATA_SCOPE_DENIED = new ErrorCode(1_024_001_006, "数据域越权，当前租户({})不能访问目标租户({})");
    ErrorCode TOOL_REQUEST_INVALID = new ErrorCode(1_024_001_007, "Tool 调用请求不合法，缺少必要字段");

}
