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
    ErrorCode TOOL_ADAPTER_NOT_EXISTS = new ErrorCode(1_024_001_008, "Tool({})未注册适配器");
    ErrorCode TOOL_ACTION_DENIED = new ErrorCode(1_024_001_009, "Tool 调用被拒绝，缺少 action({})");

    ErrorCode OPENFANG_INSTANCE_NOT_EXISTS = new ErrorCode(1_024_002_001, "OpenFang 实例不存在");
    ErrorCode OPENFANG_INSTANCE_NAME_DUPLICATED = new ErrorCode(1_024_002_002, "OpenFang 实例名称已存在");
    ErrorCode OPENFANG_INSTANCE_ENDPOINT_REQUIRED = new ErrorCode(1_024_002_003, "OpenFang 实例地址不能为空");
    ErrorCode OPENFANG_INSTANCE_API_KEY_REQUIRED = new ErrorCode(1_024_002_004, "OpenFang API Key 不能为空");
    ErrorCode OPENFANG_INSTANCE_ENCRYPTION_KEY_NOT_CONFIGURED = new ErrorCode(1_024_002_005, "未配置环境变量 AGENTX_ENCRYPTION_KEY");
    ErrorCode OPENFANG_INSTANCE_ENCRYPTION_KEY_INVALID = new ErrorCode(1_024_002_006, "环境变量 AGENTX_ENCRYPTION_KEY 非法，要求 32 字节 base64");
    ErrorCode OPENFANG_INSTANCE_ENCRYPT_FAILED = new ErrorCode(1_024_002_007, "OpenFang API Key 加密失败");
    ErrorCode OPENFANG_INSTANCE_DECRYPT_FAILED = new ErrorCode(1_024_002_008, "OpenFang API Key 解密失败");
    ErrorCode OPENFANG_INSTANCE_ENCRYPT_FORMAT_INVALID = new ErrorCode(1_024_002_009, "OpenFang API Key 密文格式非法");

    ErrorCode SCENARIO_CONFIG_NOT_EXISTS = new ErrorCode(1_024_003_001, "场景配置不存在");
    ErrorCode SCENARIO_CONFIG_CODE_DUPLICATED = new ErrorCode(1_024_003_002, "场景编码已存在");

    ErrorCode AGENT_NOT_EXISTS = new ErrorCode(1_024_004_001, "数字员工不存在");
    ErrorCode AGENT_NAME_DUPLICATED = new ErrorCode(1_024_004_002, "数字员工名称已存在");
    ErrorCode AGENT_STATUS_INVALID = new ErrorCode(1_024_004_003, "数字员工状态非法");
    ErrorCode AGENT_CAPABILITY_REQUIRED = new ErrorCode(1_024_004_004, "激活发布时至少配置一个能力");
    ErrorCode AGENT_PROCESS_REQUIRED = new ErrorCode(1_024_004_005, "激活发布时至少关联一个流程");

    ErrorCode CHANNEL_CONFIG_NOT_EXISTS = new ErrorCode(1_024_005_001, "渠道配置不存在");
    ErrorCode CHANNEL_TYPE_INVALID = new ErrorCode(1_024_005_002, "渠道类型非法");
    ErrorCode CHANNEL_BOT_TOKEN_REQUIRED = new ErrorCode(1_024_005_003, "Bot Token 不能为空");
    ErrorCode CHANNEL_TEST_CONNECT_FAILED = new ErrorCode(1_024_005_004, "渠道连接测试失败：{}");
    ErrorCode CHANNEL_SYNC_OPENFANG_FAILED = new ErrorCode(1_024_005_005, "同步 OpenFang 渠道失败：{}");

    ErrorCode CHANNEL_BIND_TOKEN_INVALID = new ErrorCode(1_024_006_001, "绑定 Token 无效");
    ErrorCode CHANNEL_BIND_TOKEN_EXPIRED = new ErrorCode(1_024_006_002, "绑定链接已过期，请重新获取");
    ErrorCode CHANNEL_BIND_ALREADY_EXISTS = new ErrorCode(1_024_006_003, "当前渠道账号已绑定，无需重复操作");

    ErrorCode ENTITLEMENT_CONFIG_NOT_EXISTS = new ErrorCode(1_024_007_001, "权限配置不存在");
    ErrorCode ENVELOPE_SIGNATURE_INVALID = new ErrorCode(1_024_007_002, "Envelope 签名无效");

}
