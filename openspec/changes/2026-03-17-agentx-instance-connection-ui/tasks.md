## 1. 数据库表结构（1 天）

- [ ] 1.1 创建 `agentx_openfang_instance` 表
- [ ] 1.2 创建 `agentx_scenario_config` 表
- [ ] 1.3 验证 `agentx_task_projection` 表与 DO 匹配
- [ ] 1.4 验证 `agentx_approval_binding` 表与 DO 匹配
- [ ] 1.5 验证 `agentx_audit_event` 表与 DO 匹配
- [ ] 1.6 执行所有建表 SQL
- [ ] 1.7 插入测试数据（OpenFang 实例配置、请假场景配置）

## 2. 实例管理层（1.5 天）

- [ ] 2.1 创建 `AgentxOpenfangInstanceDO` 和 Mapper
- [ ] 2.2 实现 `OpenfangInstanceService`（CRUD + 连接测试）
- [ ] 2.3 实现密钥加密/解密（AES-256-GCM，从环境变量读取密钥）
- [ ] 2.4 实现 `OpenfangRuntimeBridgeHttpClient.health()`
- [ ] 2.5 实现 `OpenfangInstanceController`
- [ ] 2.6 创建前端实例管理页面
- [ ] 2.7 实现定时健康检查任务（5 分钟间隔）
- [ ] 2.8 测试：配置实例 → 测试连接 → 看到在线状态

## 3. 场景配置层（1 天）

- [ ] 3.1 创建 `AgentxScenarioConfigDO` 和 Mapper
- [ ] 3.2 实现 `ScenarioConfigService`
- [ ] 3.3 实现 `ScenarioConfigController`
- [ ] 3.4 创建前端场景配置页面
- [ ] 3.5 配置请假审批场景（映射到 OpenFang workflow_id）
- [ ] 3.6 测试：保存场景配置 → 查询配置 → 验证映射关系

## 4. 上下文组装层（1.5 天）

- [ ] 4.1 定义 `ContextProvider` 接口（type + provide 方法）
- [ ] 4.2 实现 `BpmContextProvider`（根据配置查询任意流程待办）
- [ ] 4.3 实现 `UserContextProvider`（查询用户基本信息）
- [ ] 4.4 实现 `ApiContextProvider`（调用配置的业务 API）
- [ ] 4.5 在 `agentx_scenario_config.config` 中配置上下文需求
- [ ] 4.6 实现 `AgentxContextAssemblyService`（根据场景配置动态组装）
- [ ] 4.7 测试：配置请假场景 → 组装上下文 → 验证数据正确

## 5. Tool 注册与调用层（2 天）

- [ ] 5.1 定义 `bpm_query_tasks` Tool 描述符
- [ ] 5.2 实现 `BpmQueryTasksToolAdapter`
- [ ] 5.3 定义 `bpm_approve` Tool 描述符
- [ ] 5.4 实现 `BpmApproveToolAdapter`
- [ ] 5.5 实现 `AgentxToolGuardService`（权限检查）
- [ ] 5.6 实现 Tool 调用审计
- [ ] 5.7 测试：调用 Tool → 验证权限 → 记录审计

## 6. 任务编排层（2 天）

- [ ] 6.1 实现 `AgentxTaskOrchestrationService.createTask()`（幂等性保证）
- [ ] 6.2 实现 `OpenfangRuntimeBridge.runWorkflow()`
- [ ] 6.3 实现 `OpenfangRuntimeBridge.getTaskRun()`（查询 TaskRun 状态）
- [ ] 6.4 实现任务状态轮询机制（指数退避：2s → 30s）
- [ ] 6.5 实现 `TaskProjection` 状态同步（映射 OpenFang 状态）
- [ ] 6.6 实现任务超时检测（24 小时无更新自动失败）
- [ ] 6.7 创建前端任务列表页面
- [ ] 6.8 创建前端任务详情页面（显示 trace_events）
- [ ] 6.9 测试：创建任务 → 调用 OpenFang → 轮询状态 → 更新投影

## 7. 审批桥接层（2 天）

- [ ] 7.1 实现 `AgentxApprovalBridgeService.createApprovalBinding()`
- [ ] 7.2 实现审批详情查询（从 OpenFang 获取）
- [ ] 7.3 实现 BPM 流程创建（映射审批请求）
- [ ] 7.4 实现 `OpenfangRuntimeBridge.approveCallback()`（带重试）
- [ ] 7.5 实现 `OpenfangRuntimeBridge.rejectCallback()`（带重试）
- [ ] 7.6 实现审批结果回调（指数退避重试 5 次）
- [ ] 7.7 实现审批超时检测（24 小时自动超时）
- [ ] 7.8 创建前端审批列表页面
- [ ] 7.9 测试：触发审批 → 创建 BPM → 审批通过 → 回调 OpenFang
- [ ] 7.10 测试：回调失败场景 → 重试 → 告警

## 8. 审计层（1 天）

- [ ] 8.1 实现 `AgentxAuditService.logTaskStart()`
- [ ] 8.2 实现 `AgentxAuditService.logToolCall()`（自动脱敏）
- [ ] 8.3 实现 `AgentxAuditService.logApprovalRequest()`
- [ ] 8.4 实现 `AgentxAuditService.logTaskComplete()`
- [ ] 8.5 实现敏感信息脱敏规则（密码、身份证、手机号、API Key）
- [ ] 8.6 实现审计日志清理任务（180 天保留期）
- [ ] 8.7 创建前端审计日志页面
- [ ] 8.8 测试：执行任务 → 查看审计日志 → 验证完整性和脱敏

## 9. OpenFang Workflow 准备（1 天）

- [ ] 9.1 编写 `leave-approval-assistant` workflow 定义
- [ ] 9.2 在 OpenFang 中注册 workflow
- [ ] 9.3 配置 `bpm_query_tasks` tool
- [ ] 9.4 配置 `bpm_approve` tool
- [ ] 9.5 测试：手动调用 workflow → 验证 tool 可用

## 10. 端到端集成测试（2 天）

- [ ] 10.1 准备测试数据（用户张三、3 条待审批请假）
- [ ] 10.2 配置 OpenFang 实例
- [ ] 10.3 配置请假审批场景
- [ ] 10.4 触发 AI 审批助手
- [ ] 10.5 验证：Agent 查询到 3 条待办
- [ ] 10.6 验证：1 天和 2 天请假自动通过
- [ ] 10.7 验证：3 天请假触发审批
- [ ] 10.8 人工审批通过
- [ ] 10.9 验证：Agent 继续执行，第 3 条通过
- [ ] 10.10 验证：任务状态更新为完成
- [ ] 10.11 验证：审计日志完整
- [ ] 10.12 验证：审批绑定关系正确

## 11. 异常场景测试（1 天）

- [ ] 11.1 测试：OpenFang 离线场景（健康检查失败）
- [ ] 11.2 测试：网络超时场景（轮询超时、回调超时）
- [ ] 11.3 测试：权限不足场景（Tool 调用被拒绝）
- [ ] 11.4 测试：审批拒绝场景
- [ ] 11.5 测试：审批超时场景（24 小时未审批）
- [ ] 11.6 测试：回调失败场景（5 次重试全部失败）
- [ ] 11.7 测试：任务超时场景（24 小时无更新）
- [ ] 11.8 测试：重复请求场景（幂等性验证）
- [ ] 11.9 测试：并发创建任务（唯一性约束）
- [ ] 11.10 测试：上下文组装超时（单个 Provider 超时）

## 12. 文档完善（1 天）

- [ ] 12.1 更新架构设计文档
- [ ] 12.2 编写部署文档
- [ ] 12.3 编写用户手册
- [ ] 12.4 编写开发者文档
- [ ] 12.5 录制演示视频

## 验收标准（必须全部通过）

### 功能验收
- [ ] 管理员可以配置 OpenFang 实例并测试连接
- [ ] 管理员可以配置请假审批场景
- [ ] 用户可以触发 AI 审批助手
- [ ] Agent 可以查询待办任务
- [ ] Agent 可以自动审批 ≤2 天请假
- [ ] Agent 可以对 >2 天请假发起审批
- [ ] 人工审批可以正常流转
- [ ] 审批结果可以回调 OpenFang
- [ ] 任务状态可以正确更新
- [ ] 审计日志记录完整

### 架构验收
- [ ] 8 个架构层全部实现
- [ ] OA 与 OpenFang 协议正确
- [ ] 审批真相在 OA
- [ ] 运行时投影在 OpenFang
- [ ] 所有 Tool 调用有权限检查
- [ ] 所有关键操作有审计记录
- [ ] 幂等性保证
- [ ] 错误处理完善

### 性能验收
- [ ] 任务创建响应时间 < 2 秒
- [ ] 状态轮询间隔 5 秒
- [ ] 审批回调响应时间 < 1 秒
- [ ] 审计日志写入不阻塞主流程

**总计：15 天**
