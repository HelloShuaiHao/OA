# Identity + Entitlement Context 机制

**提案日期：** 2026-04-01
**状态：** Draft
**负责人：** AgentX Team
**预计工期：** 6 周（分 5 个 Phase）

---

## 1. 背景与问题

### 1.1 当前问题

Agent 在与已绑定用户交互时，缺乏对用户身份和权限的感知能力：

1. **身份盲区**
   - Agent 不知道当前用户的业务身份（部门、岗位、职责）
   - 无法根据用户角色提供个性化服务

2. **权限盲区**
   - Agent 不知道用户的权限边界（可访问的资源范围、可执行的操作）
   - 可能执行超出用户权限的操作

3. **信息泄露风险**
   - Agent 可能暴露其他绑定用户的信息
   - 缺少防泄露机制

### 1.2 业务影响

- **安全风险**：用户可能通过 Agent 访问未授权的数据
- **合规问题**：无法满足数据访问审计要求
- **用户体验差**：Agent 无法提供个性化服务

### 1.3 解决方案核心思路

1. **双层上下文**：Model-Visible（给 Agent 看）+ System-Enforced（系统强制）
2. **通用机制**：所有 Agent 共用，不做特化
3. **默认拒绝**：未明确授权的操作一律拒绝
4. **会话隔离**：权限上下文按用户+Agent+会话绑定

---

## 2. 设计原则

### 2.1 双层上下文

**Model-Visible Context**（给 Agent 看）：
- 业务身份：姓名、部门、岗位、角色标签
- 能力摘要：能做什么、不能做什么、需要审批的操作

**System-Enforced Context**（系统强制）：
- 精确权限：allowed_actions、resource_filters、obligations
- 不给 Agent 看，由系统在 Tool 调用前强制校验

### 2.2 防泄露机制

- 永远只返回当前用户的上下文
- 禁止查询他人信息
- 会话隔离，不跨会话复用
- Prompt Guardrail 硬规则

### 2.3 强制校验

- 入口层：是否可访问该 Agent
- 执行层：每次 Tool 调用前校验 action 权限
- 数据层：查询时强制拼接 resource_filters

---

## 3. 核心流程

```
用户消息 → OF 识别身份 → 调用 OA 鉴权接口 → 获取 Envelope
→ 注入 Model-Visible 到 System Prompt
→ 缓存 System-Enforced（1-5min TTL）
→ Agent 处理
→ Tool 调用前校验权限
→ 应用资源过滤
→ 记录审计日志
```

---

## 4. 实施计划

### Phase 1: 基础设施（Week 1-2）
- 数据库表：`agentx_user_entitlement`, `agentx_access_audit`
- OA 鉴权接口：`POST /agentx/channel/access/evaluate`
- 权限配置管理接口（CRUD）

### Phase 2: OF 集成（Week 3）
- OF 鉴权客户端
- 上下文缓存机制（Redis）
- System Prompt 注入

### Phase 3: Tool 权限校验（Week 4）
- Tool Manifest 扩展 `required_actions`
- Tool 权限拦截器
- 资源过滤自动应用

### Phase 4: 安全加固（Week 5）
- Prompt Guardrail 基线规则
- 红队测试（提示注入、越权访问）
- 性能测试

### Phase 5: 生产部署（Week 6）
- 监控指标和告警
- 灰度发布
- 运维文档

---

## 5. 验收标准

### 功能验收
- 已绑定用户问"我是谁/我能做什么"，回答准确
- 用户调用超权限 Tool，拒绝并给出友好提示
- 用户尝试访问其他区域数据，自动过滤
- 同一用户切换不同 Agent，能力描述不同

### 安全验收
- 提示注入攻击无法获取他人信息
- Tool 参数无法绕过资源过滤
- Envelope 签名篡改被拒绝
- 群聊中无法查询他人权限

### 性能验收
- 鉴权接口 P99 < 100ms
- 缓存命中率 > 95%
- Tool 权限校验开销 < 10ms

---

## 6. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| 性能开销 | 每次请求增加鉴权调用 | Redis 缓存，TTL 1-5min |
| 缓存一致性 | 权限更新不及时生效 | policy_version 强制失效 |
| 提示注入 | Agent 被诱导泄露信息 | Prompt Guardrail + 红队测试 |
| 复杂度增加 | 开发和维护成本 | 通用机制，所有 Agent 共用 |
