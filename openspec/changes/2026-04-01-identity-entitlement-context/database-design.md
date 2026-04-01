# 数据库设计

## 1. agentx_user_entitlement（用户权限配置表）

```sql
CREATE TABLE agentx_user_entitlement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    agent_id VARCHAR(64) COMMENT 'Agent ID，NULL表示全局权限',
    
    -- 身份信息
    dept_name VARCHAR(100) COMMENT '部门名称',
    job_title VARCHAR(100) COMMENT '岗位',
    role_tags JSON COMMENT '角色标签数组',
    work_region VARCHAR(50) COMMENT '工作区域',
    
    -- 权限配置
    allowed_actions JSON COMMENT '允许的操作列表',
    resource_filters JSON COMMENT '资源过滤条件',
    obligations JSON COMMENT '审批约束',
    
    -- 元数据
    policy_version VARCHAR(50) COMMENT '策略版本',
    effective_from DATETIME COMMENT '生效时间',
    effective_until DATETIME COMMENT '失效时间',
    
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BIT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    
    INDEX idx_user_agent (user_id, agent_id),
    INDEX idx_policy_version (policy_version)
) COMMENT='用户权限配置表';
```

### 字段说明

**身份信息字段**：
- `dept_name`: 部门名称，用于 Model-Visible Context
- `job_title`: 岗位，用于 Model-Visible Context
- `role_tags`: 角色标签数组（JSON），如 `["dispatcher", "regional_ops"]`
- `work_region`: 工作区域，如"华东区"

**权限配置字段**：
- `allowed_actions`: 允许的操作列表（JSON），如 `["route.read", "route.plan.create"]`
- `resource_filters`: 资源过滤条件（JSON），如 `{"region_codes": ["east"], "warehouse_ids": [101, 102]}`
- `obligations`: 审批约束（JSON），定义哪些操作需要审批

**元数据字段**：
- `policy_version`: 策略版本，用于缓存失效
- `effective_from/until`: 生效时间范围，支持临时授权

### 示例数据

```json
{
  "user_id": 12345,
  "agent_id": "dispatch-assistant",
  "dept_name": "物流调度部",
  "job_title": "调度员",
  "role_tags": ["dispatcher", "regional_ops"],
  "work_region": "华东区",
  "allowed_actions": [
    "route.read",
    "route.plan.create",
    "route.plan.adjust",
    "warehouse.inventory.read"
  ],
  "resource_filters": {
    "region_codes": ["east"],
    "warehouse_ids": [101, 102, 103]
  },
  "obligations": [
    {
      "action": "route.plan.cross_region",
      "requires": "approval",
      "approver_role": "regional_manager"
    }
  ],
  "policy_version": "v2026.04.01",
  "effective_from": "2026-04-01T00:00:00",
  "effective_until": null
}
```

---

## 2. agentx_access_audit（权限决策审计表）

```sql
CREATE TABLE agentx_access_audit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    decision_id VARCHAR(64) NOT NULL COMMENT '决策ID',
    
    user_id BIGINT NOT NULL COMMENT '用户ID',
    channel_user_id VARCHAR(255) NOT NULL COMMENT '渠道用户ID',
    agent_id VARCHAR(64) NOT NULL COMMENT 'Agent ID',
    conversation_scope VARCHAR(255) COMMENT '会话范围',
    
    action VARCHAR(100) COMMENT '操作',
    resource_type VARCHAR(50) COMMENT '资源类型',
    resource_id VARCHAR(255) COMMENT '资源ID',
    
    decision VARCHAR(20) NOT NULL COMMENT '决策结果: ALLOW/DENY',
    deny_reason VARCHAR(255) COMMENT '拒绝原因',
    
    policy_version VARCHAR(50) COMMENT '策略版本',
    request_time DATETIME NOT NULL COMMENT '请求时间',
    
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    
    INDEX idx_decision_id (decision_id),
    INDEX idx_user_time (user_id, request_time),
    INDEX idx_agent_time (agent_id, request_time)
) COMMENT='权限决策审计表';
```

### 字段说明

**身份字段**：
- `decision_id`: 决策ID，用于关联同一次请求的多条审计记录
- `user_id`: 用户ID
- `channel_user_id`: 渠道用户ID，如 `telegram:987654321`
- `agent_id`: Agent ID
- `conversation_scope`: 会话范围，用于隔离群聊等场景

**操作字段**：
- `action`: 操作，如 `route.read`、`route.plan.create`
- `resource_type`: 资源类型，如 `route`、`warehouse`
- `resource_id`: 资源ID

**决策字段**：
- `decision`: 决策结果，`ALLOW` 或 `DENY`
- `deny_reason`: 拒绝原因，如 `missing_action`、`resource_filtered`

### 示例数据

```json
{
  "decision_id": "dec_xyz789",
  "user_id": 12345,
  "channel_user_id": "telegram:987654321",
  "agent_id": "dispatch-assistant",
  "conversation_scope": "session_abc123",
  "action": "route.plan.create",
  "resource_type": "route",
  "resource_id": "R001",
  "decision": "ALLOW",
  "deny_reason": null,
  "policy_version": "v2026.04.01",
  "request_time": "2026-04-01T06:00:00"
}
```

---

## 3. 索引设计

### agentx_user_entitlement
- `idx_user_agent (user_id, agent_id)`: 查询用户在特定 Agent 的权限
- `idx_policy_version (policy_version)`: 按策略版本批量失效缓存

### agentx_access_audit
- `idx_decision_id (decision_id)`: 按决策ID查询审计记录
- `idx_user_time (user_id, request_time)`: 按用户查询审计历史
- `idx_agent_time (agent_id, request_time)`: 按 Agent 查询审计历史

---

## 4. 数据迁移脚本

```sql
-- V20260401_01__agentx_identity_entitlement_context.sql

-- 创建用户权限配置表
CREATE TABLE agentx_user_entitlement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    agent_id VARCHAR(64) COMMENT 'Agent ID，NULL表示全局权限',
    dept_name VARCHAR(100) COMMENT '部门名称',
    job_title VARCHAR(100) COMMENT '岗位',
    role_tags JSON COMMENT '角色标签数组',
    work_region VARCHAR(50) COMMENT '工作区域',
    allowed_actions JSON COMMENT '允许的操作列表',
    resource_filters JSON COMMENT '资源过滤条件',
    obligations JSON COMMENT '审批约束',
    policy_version VARCHAR(50) COMMENT '策略版本',
    effective_from DATETIME COMMENT '生效时间',
    effective_until DATETIME COMMENT '失效时间',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BIT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    INDEX idx_user_agent (user_id, agent_id),
    INDEX idx_policy_version (policy_version)
) COMMENT='用户权限配置表';

-- 创建权限决策审计表
CREATE TABLE agentx_access_audit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    decision_id VARCHAR(64) NOT NULL COMMENT '决策ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    channel_user_id VARCHAR(255) NOT NULL COMMENT '渠道用户ID',
    agent_id VARCHAR(64) NOT NULL COMMENT 'Agent ID',
    conversation_scope VARCHAR(255) COMMENT '会话范围',
    action VARCHAR(100) COMMENT '操作',
    resource_type VARCHAR(50) COMMENT '资源类型',
    resource_id VARCHAR(255) COMMENT '资源ID',
    decision VARCHAR(20) NOT NULL COMMENT '决策结果: ALLOW/DENY',
    deny_reason VARCHAR(255) COMMENT '拒绝原因',
    policy_version VARCHAR(50) COMMENT '策略版本',
    request_time DATETIME NOT NULL COMMENT '请求时间',
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户ID',
    INDEX idx_decision_id (decision_id),
    INDEX idx_user_time (user_id, request_time),
    INDEX idx_agent_time (agent_id, request_time)
) COMMENT='权限决策审计表';
```
