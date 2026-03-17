# AgentX 集成平台架构总览

## 1. 文档导航

本目录包含 OA 与 OpenFang 集成平台的完整架构设计：

- **[20260312-初步设计.md](./20260312-初步设计.md)** - 总体架构、技术选型、模块划分
- **[20260317-服务感知与配置同步.md](./20260317-服务感知与配置同步.md)** - 服务发现、配置管理、健康检查
- **[20260317-任务编排与生命周期.md](./20260317-任务编排与生命周期.md)** - 任务投递、状态同步、控制机制
- **[20260317-身份与权限模型.md](./20260317-身份与权限模型.md)** - Agent 身份、权限计算、多租户隔离
- **[20260317-审批桥接.md](./20260317-审批桥接.md)** - 审批触发、流程对接、结果回调
- **[20260317-上下文组装.md](./20260317-上下文组装.md)** - 业务上下文、场景化组装、缓存策略
- **[20260317-Tool注册与协议.md](./20260317-Tool注册与协议.md)** - Tool 定义、注册机制、调用协议
- **[20260317-SaaS多租户架构.md](./20260317-SaaS多租户架构.md)** - 租户隔离、配额管理、实例分配

## 2. 核心设计原则

### 2.1 单一真相源

```
OA 负责：
- 组织架构
- 人员权限
- BPM 审批
- 业务单据
- 审计归档

OpenFang 负责：
- Agent 运行时
- TaskRun 状态
- Tool 执行
- 多轮对话
- Memory 管理

集成层负责：
- 身份映射
- 权限映射
- 任务投递
- 审批桥接
- 事件同步
```

### 2.2 边界清晰

- OA 不理解 OpenFang 内部状态机
- OpenFang 不直接访问 OA 数据库
- 所有交互通过明确的 API 协议
- 审批真相只在 OA BPM

### 2.3 平台化设计

- 公共能力抽象到平台层
- 场景只做装配，不重造基础设施
- 新场景接入成本低

## 3. 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                         OA 系统                              │
├─────────────────────────────────────────────────────────────┤
│  现有模块                                                     │
│  ├─ System (用户、组织、权限)                                │
│  ├─ BPM (工作流、审批)                                       │
│  ├─ CRM (客户管理)                                           │
│  └─ ERP (采购、库存)                                         │
├─────────────────────────────────────────────────────────────┤
│  AgentX 集成平台 (新增)                                      │
│  ├─ agentx-identity        (身份管理)                        │
│  ├─ agentx-authorization   (权限控制)                        │
│  ├─ agentx-tool-registry   (Tool 注册)                       │
│  ├─ agentx-context         (上下文组装)                      │
│  ├─ agentx-runtime-bridge  (运行时桥接)                      │
│  ├─ agentx-approval-bridge (审批桥接)                        │
│  ├─ agentx-audit           (审计日志)                        │
│  └─ agentx-scenario        (场景装配)                        │
└─────────────────────────────────────────────────────────────┘
                            ↕ HTTP/WebSocket
┌─────────────────────────────────────────────────────────────┐
│                    OpenFang Runtime                          │
├─────────────────────────────────────────────────────────────┤
│  ├─ Agent 定义与运行                                         │
│  ├─ Skill & Tool 执行                                        │
│  ├─ TaskRun 状态机                                           │
│  ├─ Memory & Context                                         │
│  └─ Channel 接入 (IM/Email/API)                              │
└─────────────────────────────────────────────────────────────┘
```

## 4. 关键流程

### 4.1 任务执行流程

```
1. 用户发起请求 (OA 页面/IM)
   ↓
2. OA 创建 TaskBinding
   ↓
3. agentx-context 组装业务上下文
   ↓
4. agentx-authorization 计算权限范围
   ↓
5. agentx-runtime-bridge 调用 OpenFang 创建 TaskRun
   ↓
6. OpenFang 执行任务
   ├─ 调用 Tool (通过 OA API)
   ├─ 高风险操作触发审批
   └─ 更新进度
   ↓
7. 审批流程 (如需要)
   ├─ OpenFang 请求审批
   ├─ OA BPM 处理审批
   └─ 结果回调 OpenFang
   ↓
8. 任务完成
   ├─ 结果回写 OA
   └─ 审计日志归档
```

### 4.2 权限检查流程

```
实际权限 = Agent 基础能力 ∩ 用户委托范围 ∩ 场景策略 ∩ 数据权限

示例：
- Agent 能力: [bpm:*, crm:read]
- 用户委托: [bpm:approve:leave, bpm:read]
- 场景策略: [amount <= 5000]
- 数据权限: [dept_id = 100]

→ 最终权限: 只能审批本部门金额 ≤5000 的请假
```

## 5. 数据模型核心表

```sql
-- 租户
agentx_tenant
agentx_tenant_quota
agentx_tenant_usage

-- 身份与权限
agentx_agent_principal
agentx_delegation_grant
agentx_tool_permission

-- 任务
agentx_task_binding
agentx_approval_binding

-- Tool
agentx_tool_registry

-- 审计
agentx_audit_log

-- OpenFang 实例
agentx_openfang_instance
```

## 6. API 协议

### 6.1 OA → OpenFang

```
POST   /api/tenants/{tenantId}/tasks                    创建任务
GET    /api/tenants/{tenantId}/tasks/{taskRunId}        查询任务
POST   /api/tenants/{tenantId}/tasks/{taskRunId}/cancel 取消任务
POST   /api/tenants/{tenantId}/tasks/{taskRunId}/resume 恢复任务
PUT    /api/tenants/{tenantId}/agents/{agentId}         更新 Agent 配置
```

### 6.2 OpenFang → OA

```
POST   /api/agentx/tasks/{taskId}/status                更新任务状态
POST   /api/agentx/approval/request                     请求审批
POST   /api/agentx/tools/{toolName}/invoke              调用 Tool
```

## 7. 实施路线图

### Phase 0: 边界确认 (1 周)
- ✅ 核心对象模型
- ✅ 模块边界
- ✅ 协议边界
- ✅ 架构设计文档

### Phase 1: 平台骨架 (4 周)
- 数据库表结构
- agentx-identity (身份管理)
- agentx-authorization (权限控制)
- agentx-runtime-bridge (基础对接)
- 单租户支持

### Phase 2: 核心能力 (4 周)
- agentx-tool-registry (Tool 注册)
- agentx-context (上下文组装)
- agentx-approval-bridge (审批桥接)
- agentx-audit (审计日志)

### Phase 3: 第一个场景 (2 周)
- 请假审批助手
- 端到端验证
- 性能测试

### Phase 4: 多租户 (4 周)
- 租户隔离
- 配额管理
- 实例分配
- 计费统计

### Phase 5: 场景扩展 (持续)
- 报销审批
- CRM 助手
- 周报分析
- ...

## 8. 技术栈

### OA 侧 (Java)
- Spring Boot 2.7.x
- MyBatis Plus
- Flowable (BPM)
- Redis (缓存)
- MySQL 8.0

### OpenFang 侧 (Rust)
- Tokio (异步运行时)
- Axum (Web 框架)
- SQLite/PostgreSQL
- LLM SDK (OpenAI/Anthropic)

### 通信协议
- HTTP/REST
- WebSocket (实时通知)
- JSON (数据格式)

## 9. 安全考虑

- Agent 独立安全主体，不冒充用户
- 所有请求携带租户标识
- 权限取交集，不直接继承
- Tool 调用审计留痕
- 敏感数据加密存储
- API 调用限流
- 跨租户访问拒绝

## 10. 监控与运维

- 健康检查 (30s 间隔)
- 任务超时监控
- 配额使用告警
- 审批超时提醒
- Tool 调用统计
- 错误率监控
- 性能指标采集

## 11. 下一步行动

1. **评审架构设计** - 团队评审本设计文档
2. **确认技术选型** - 确认 Java 为主、Rust 为辅的策略
3. **创建数据库表** - 根据设计创建表结构
4. **搭建模块骨架** - 创建 yudao-module-agentx 模块
5. **实现第一个 API** - OA 调用 OpenFang 创建任务
6. **端到端验证** - 跑通最小化场景

## 12. 参考资料

- [OpenFang 文档](https://github.com/openfang/openfang)
- [Flowable 文档](https://www.flowable.com/open-source/docs)
- [若依框架文档](https://doc.iocoder.cn)
