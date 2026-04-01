# Identity + Entitlement Context 机制

通用的身份与权限上下文机制，为 Agent 提供用户身份感知和权限边界控制。

## 概述

### 问题
- Agent 不知道当前用户的业务身份（部门、岗位、职责）
- Agent 不知道用户的权限边界（可访问的资源范围、可执行的操作）
- 存在信息泄露风险（Agent 可能暴露其他绑定用户的信息）

### 解决方案
双层上下文机制：
- **Model-Visible Context**: 给 Agent 看的身份和能力摘要
- **System-Enforced Context**: 系统强制校验的精确权限

### 核心特性
- ✅ 通用机制：所有 Agent 共用，不做特化
- ✅ 防泄露：永远只返回当前用户的上下文
- ✅ 强制校验：Tool 调用前系统层拦截
- ✅ 会话隔离：按 channel_user_id + agent_id + conversation_scope 绑定
- ✅ 可审计：所有决策可追溯

## 文档结构

```
2026-04-01-identity-entitlement-context/
├── README.md                    # 本文件
├── proposal.md                  # 设计提案
├── api-spec.yaml                # OpenAPI 规范
├── database-design.md           # 数据库设计
├── tasks.md                     # 实施任务
└── examples/                    # 示例
    ├── envelope-example.json    # Envelope 示例
    └── config-example.json      # 权限配置示例
```

## 快速开始

### 1. 数据库迁移

```bash
# 执行迁移脚本
mysql -u root -p < yudao-server/src/main/resources/db/migration/V20260401_01__agentx_identity_entitlement_context.sql
```

### 2. 配置

```yaml
yudao:
  agentx:
    entitlement:
      signature-secret: ${AGENTX_SIGNATURE_SECRET:change-me-in-prod}
      cache:
        enabled: true
        ttl-minutes: 5
      policy-version: "v2026.04.01"
      audit:
        enabled: true
        async: true
```

### 3. 创建权限配置

```bash
curl -X POST http://localhost:48080/admin-api/agentx/entitlement/config \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 12345,
    "agentId": "dispatch-assistant",
    "deptName": "物流调度部",
    "jobTitle": "调度员",
    "roleTags": ["dispatcher", "regional_ops"],
    "workRegion": "华东区",
    "allowedActions": ["route.read", "route.plan.create"],
    "resourceFilters": {
      "region_codes": ["east"],
      "warehouse_ids": [101, 102, 103]
    }
  }'
```

### 4. 评估访问权限

```bash
curl -X POST http://localhost:48080/admin-api/agentx/channel/access/evaluate \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "channelUserId": "telegram:987654321",
    "agentId": "dispatch-assistant",
    "conversationScope": "session_abc123"
  }'
```

## 核心流程

```
用户消息
  ↓
OF 识别身份
  ↓
调用 OA 鉴权接口
  ↓
获取 Envelope (Model-Visible + System-Enforced)
  ↓
注入 System Prompt + 缓存 (1-5min)
  ↓
Agent 处理
  ↓
Tool 调用前校验权限
  ↓
应用资源过滤
  ↓
记录审计日志
```

## 实施计划

- **Phase 1** (Week 1-2): 基础设施 - 数据库、Service、Controller
- **Phase 2** (Week 3): OF 集成 - 鉴权客户端、缓存、System Prompt 注入
- **Phase 3** (Week 4): Tool 权限校验 - 拦截器、资源过滤、审计日志
- **Phase 4** (Week 5): 安全加固 - Prompt Guardrail、红队测试、性能测试
- **Phase 5** (Week 6): 生产部署 - 监控、灰度发布、运维文档

## 验收标准

### 功能
- ✅ 已绑定用户问"我是谁/我能做什么"，回答准确
- ✅ 用户调用超权限 Tool，拒绝并给出友好提示
- ✅ 用户尝试访问其他区域数据，自动过滤

### 安全
- ✅ 提示注入攻击无法获取他人信息
- ✅ Tool 参数无法绕过资源过滤
- ✅ Envelope 签名篡改被拒绝

### 性能
- ✅ 鉴权接口 P99 < 100ms
- ✅ 缓存命中率 > 95%
- ✅ Tool 权限校验开销 < 10ms

## 相关文档

- [Phase B 实施说明](../../docs/agentx/phase-b-implementation.md)
- [渠道绑定流程](../../docs/agentx/channel-binding-flow.md)

## 联系方式

- 负责人：AgentX Team
- 提案日期：2026-04-01
