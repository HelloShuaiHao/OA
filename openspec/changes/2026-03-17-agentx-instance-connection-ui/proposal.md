# 提案：AgentX 平台 Phase 1 端到端验证

• 实施这份 proposal 后，系统将完成 AgentX 平台的第一个完整闭环：从 OA UI 配置 OpenFang 实例，到通过 UI 触发一个请假审批任务，Agent 自动查询待办、应用规则、执行审批，最终结果回写 OA 并留下完整审计轨迹。这验证了身份映射、权限控制、任务编排、上下文组装、Tool 调用、审批桥接、审计留痕等所有核心架构能力。

## 概要

在已有 AgentX 模块骨架和架构设计基础上，实现第一个端到端可验收的业务闭环。不是单独实现某个功能点，而是让整个系统架构真正运转起来，验证 OA 治理约束与 OpenFang Runtime 的完整集成链路。

## 问题背景

当前状态：
- ✅ AgentX 架构设计完整（8 个模块、职责边界、协议定义）
- ✅ 代码骨架已存在（DO、Service 接口、Governance 类）
- ✅ OpenFang 集成平台 proposal 已完成
- ❌ 缺少端到端验证：没有一个完整用例能走通所有架构层

问题：
- 无法验证架构设计是否可行
- 无法验证各模块间的协作是否正确
- 无法验证 OA 与 OpenFang 的协议是否完整
- 无法向团队演示系统能力

需要一个最小但完整的用例，覆盖所有架构层，证明系统可以工作。

## 变更内容

### 1. 端到端验收用例

**用例：请假审批助手自动处理 2 天以内请假**

```
前置条件：
- OpenFang 本地实例运行在 localhost:4201
- 已配置请假审批 workflow
- 用户张三有 3 条待审批请假（1 天、2 天、3 天）

执行步骤：
1. 管理员在 OA 配置 OpenFang 实例连接
2. 管理员配置请假审批场景（映射到 OpenFang workflow）
3. 张三在 OA 点击"AI 审批助手"
4. 系统创建任务，调用 OpenFang workflow
5. Agent 查询张三的待办任务（调用 bpm_query_tasks Tool）
6. Agent 应用规则：≤2 天自动通过，>2 天需人工确认
7. Agent 自动审批通过 1 天和 2 天的请假（调用 bpm_approve Tool）
8. Agent 对 3 天请假发起审批请求
9. OA 创建 BPM 审批流程
10. 张三审批通过
11. 审批结果回调 OpenFang
12. Agent 继续执行，审批通过 3 天请假
13. 任务完成，结果回写 OA
14. 审计日志记录完整轨迹

验收标准：
- ✅ 1 天和 2 天请假自动通过
- ✅ 3 天请假经过人工审批后通过
- ✅ OA 可以看到任务状态（运行中 → 阻塞 → 完成）
- ✅ 审计日志记录所有 Tool 调用
- ✅ 审批绑定关系正确
```

### 2. 必须实现的架构层

#### 2.1 实例管理层
- OpenFang 实例配置（endpoint、API Key）
- 连接测试和健康检查
- 实例状态监控

#### 2.2 场景配置层
- 场景定义（请假审批场景）
- Workflow 映射（场景 → OpenFang workflow_id）
- 场景策略（自动审批规则）

#### 2.3 身份与权限层
- AgentPrincipal（请假审批助手身份）
- DelegationGrant（张三委托给 Agent 的权限）
- 权限计算（Agent 能力 ∩ 用户委托）

#### 2.4 上下文组装层
- BPM 上下文（待办任务列表）
- 用户上下文（张三的信息）
- 请假上下文（请假政策、余额）

#### 2.5 任务编排层
- 创建 TaskProjection
- 调用 OpenFang workflow run
- 轮询任务状态
- 状态同步

#### 2.6 Tool 注册与调用层
- 注册 bpm_query_tasks Tool
- 注册 bpm_approve Tool
- Tool 权限检查
- Tool 调用审计

#### 2.7 审批桥接层
- 检测高风险操作（3 天请假）
- 创建 ApprovalBinding
- 启动 BPM 流程
- 审批结果回调

#### 2.8 审计层
- 任务创建审计
- Tool 调用审计
- 审批请求审计
- 结果归档

### 3. 数据库表（完整）

```sql
-- 实例管理
CREATE TABLE agentx_openfang_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    instance_name VARCHAR(64) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    api_key_encrypted VARCHAR(512),
    status TINYINT NOT NULL DEFAULT 0,
    version VARCHAR(32),
    last_heartbeat DATETIME,
    creator VARCHAR(64) DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT 0,
    INDEX idx_tenant (tenant_id)
);

-- 场景配置
CREATE TABLE agentx_scenario_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scenario_code VARCHAR(64) NOT NULL,
    scenario_name VARCHAR(128) NOT NULL,
    openfang_workflow_id VARCHAR(64) NOT NULL,
    workflow_version VARCHAR(32),
    enabled TINYINT NOT NULL DEFAULT 1,
    config JSON COMMENT '场景配置',
    creator VARCHAR(64) DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_code (scenario_code, deleted)
);

-- 任务投影
CREATE TABLE agentx_task_projection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    scenario_code VARCHAR(64) NOT NULL,
    business_key VARCHAR(128),
    idempotency_key VARCHAR(128),
    openfang_task_run_id VARCHAR(64),
    projection_status TINYINT NOT NULL,
    risk_level TINYINT,
    result_summary TEXT,
    failure_summary TEXT,
    audit_summary TEXT,
    creator VARCHAR(64) DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT 0,
    INDEX idx_task_run (openfang_task_run_id),
    UNIQUE KEY uk_idempotency (idempotency_key, deleted)
);

-- 审批绑定
CREATE TABLE agentx_approval_binding (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    task_projection_id BIGINT NOT NULL,
    openfang_approval_id VARCHAR(64) NOT NULL,
    bpm_process_instance_id VARCHAR(64),
    approval_status TINYINT NOT NULL,
    risk_level TINYINT,
    tool_name VARCHAR(128),
    tool_params TEXT,
    approver_id BIGINT,
    approval_comment TEXT,
    creator VARCHAR(64) DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) NOT NULL DEFAULT 0,
    INDEX idx_task (task_projection_id),
    UNIQUE KEY uk_approval (openfang_approval_id, deleted)
);

-- 审计事件
CREATE TABLE agentx_audit_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    event_type VARCHAR(32) NOT NULL,
    agent_code VARCHAR(64),
    task_projection_id BIGINT,
    user_id BIGINT,
    resource_type VARCHAR(64),
    resource_id VARCHAR(128),
    action VARCHAR(64),
    result VARCHAR(16),
    details TEXT,
    creator VARCHAR(64) DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task (task_projection_id),
    INDEX idx_time (create_time)
);
```

### 4. 前端页面

```
views/agentx/
  ├── instance/
  │   ├── index.vue          # 实例管理
  │   └── InstanceForm.vue
  ├── scenario/
  │   ├── index.vue          # 场景配置
  │   └── ScenarioForm.vue
  ├── task/
  │   ├── index.vue          # 任务列表
  │   └── detail.vue         # 任务详情
  ├── approval/
  │   └── index.vue          # 审批列表
  └── audit/
      └── index.vue          # 审计日志
```

### 5. OpenFang Workflow 定义

```yaml
# leave-approval-assistant.yaml
name: leave-approval-assistant
version: 1.0.0
description: 请假审批助手

steps:
  - name: query_pending_tasks
    tool: bpm_query_tasks
    params:
      user_id: ${context.user_id}

  - name: analyze_and_approve
    tool: bpm_approve_batch
    params:
      tasks: ${steps.query_pending_tasks.result}
      rules:
        - condition: days <= 2
          action: auto_approve
        - condition: days > 2
          action: request_approval
```

## 非目标

- 不实现多租户完整支持（但表结构预留 `tenant_id` 字段，默认值 0）
- 不实现复杂的审批流程（只验证基础流程）
- 不实现性能优化（不做缓存、连接池等）
- 不实现完整的错误恢复机制（只做基础重试）
- 不实现任务取消功能（OpenFang 支持，但本期不暴露）

## 影响分析

### 预期收益

- 验证整个架构设计可行
- 证明 OA 与 OpenFang 可以协同工作
- 提供可演示的完整功能
- 为后续场景扩展奠定基础
- 发现架构设计中的问题

### 成本与风险

- 开发成本：15 天（比单独实现连接配置多 8.5 天）
- 需要 OpenFang 提供完整的 workflow 和 tool 支持
- 需要协调前后端、OA、OpenFang 多方开发

## 成功标准

**管理与配置验收**：
1. 管理员配置 OpenFang 实例 → 连接成功，显示在线状态
2. 管理员配置请假审批场景 → 映射成功，可启用/禁用
3. 管理员配置上下文需求 → 保存成功，可查询

**核心流程验收**：
4. 用户触发 AI 审批助手 → 任务创建成功，返回 task_projection_id
5. Agent 查询待办 → 返回 3 条请假
6. Agent 自动审批 ≤2 天请假 → 2 条通过，无需人工介入
7. Agent 对 >2 天请假发起审批 → BPM 流程创建，审批绑定正确
8. 人工审批通过 → 回调成功，Agent 继续执行
9. Agent 继续执行 → 第 3 条通过
10. 任务完成 → 状态更新为 COMPLETED

**可观测性验收**：
11. 任务列表显示所有任务 → 状态、创建时间、更新时间正确
12. 任务详情显示 trace_events → 可追溯所有 Tool 调用
13. 审批列表显示待审批项 → 可查看详情、审批、拒绝
14. 审计日志完整 → 记录任务创建、Tool 调用、审批请求、任务完成
15. 审计日志脱敏 → 密码、身份证、API Key 被脱敏

**异常场景验收**：
16. 重复创建任务 → 幂等返回，不创建重复记录
17. 审批回调失败 → 重试 5 次，失败后告警
18. OpenFang 离线 → 健康检查失败，停止轮询
19. 任务超时 → 24 小时无更新自动标记失败
20. 审批超时 → 24 小时未审批自动超时

**架构验证通过**：
- ✅ 8 个架构层全部实现
- ✅ OA 与 OpenFang 协议正确
- ✅ 审批真相在 OA，运行时投影在 OpenFang
- ✅ 所有关键操作有审计记录
