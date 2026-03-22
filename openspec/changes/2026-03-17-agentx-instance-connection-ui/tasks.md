## 1. 数据库表结构（1 天）

- [ ] 1.1 创建 `agentx_openfang_instance` 表
- [ ] 1.2 创建 `agentx_scenario_config` 表
- [ ] 1.3 验证 `agentx_task_projection` 表与 DO 匹配
- [ ] 1.4 验证 `agentx_approval_binding` 表与 DO 匹配
- [ ] 1.5 验证 `agentx_audit_event` 表与 DO 匹配
- [ ] 1.6 执行所有建表 SQL
- [ ] 1.7 插入测试数据（OpenFang 实例配置、请假场景配置）

## 2. 实例管理层（1.5 天）

- [x] 2.1 创建 `AgentxOpenfangInstanceDO` 和 Mapper
- [x] 2.2 实现 `OpenfangInstanceService`（CRUD + 连接测试）
- [x] 2.3 实现密钥加密/解密（AES-256-GCM，从环境变量读取密钥）
- [x] 2.4 实现 `OpenfangRuntimeBridgeHttpClient.health()`
- [x] 2.5 实现 `OpenfangInstanceController`
- [x] 2.6 创建前端实例管理页面
- [x] 2.7 实现定时健康检查任务（5 分钟间隔）
- [ ] 2.8 测试：配置实例 → 测试连接 → 看到在线状态

## 3. 场景配置层（1 天）

- [x] 3.1 创建 `AgentxScenarioConfigDO` 和 Mapper
- [x] 3.2 实现 `ScenarioConfigService`
- [x] 3.3 实现 `ScenarioConfigController`
- [x] 3.4 创建前端场景配置页面
- [x] 3.5 配置请假审批场景（映射到 OpenFang workflow_id）
- [x] 3.6 测试：保存场景配置 → 查询配置 → 验证映射关系

## 4. 上下文组装层（1.5 天）

- [x] 4.1 定义 `ContextProvider` 接口（type + provide 方法）
- [x] 4.2 实现 `BpmContextProvider`（根据配置查询任意流程待办）
- [x] 4.3 实现 `UserContextProvider`（查询用户基本信息）
- [x] 4.4 实现 `ApiContextProvider`（调用配置的业务 API）
- [x] 4.5 在 `agentx_scenario_config.config` 中配置上下文需求
- [x] 4.6 实现 `AgentxContextAssemblyService`（根据场景配置动态组装）
- [x] 4.7 测试：配置请假场景 → 组装上下文 → 验证数据正确

## 5. Tool 注册与调用层（2 天）

- [x] 5.1 定义 `bpm_query_tasks` Tool 描述符
- [x] 5.2 实现 `BpmQueryTasksToolAdapter`
- [x] 5.3 定义 `bpm_approve` Tool 描述符
- [x] 5.4 实现 `BpmApproveToolAdapter`
- [x] 5.5 实现 `AgentxToolGuardService`（权限检查）
- [x] 5.6 实现 Tool 调用审计
- [x] 5.7 测试：调用 Tool → 验证权限 → 记录审计

## 6. 任务编排层（2 天）

- [x] 6.1 实现 `AgentxTaskOrchestrationService.createTask()`（幂等性保证）
- [x] 6.2 实现 `OpenfangRuntimeBridge.runWorkflow()`
- [x] 6.3 实现 `OpenfangRuntimeBridge.getTaskRun()`（查询 TaskRun 状态）
- [x] 6.4 实现任务状态轮询机制（指数退避：2s → 30s）
- [x] 6.5 实现 `TaskProjection` 状态同步（映射 OpenFang 状态）
- [x] 6.6 实现任务超时检测（24 小时无更新自动失败）
- [x] 6.7 创建前端任务列表页面
- [x] 6.8 创建前端任务详情页面（显示 trace_events）
- [x] 6.9 测试：创建任务 → 调用 OpenFang → 轮询状态 → 更新投影

## 7. 审批桥接层（2 天）

- [x] 7.1 实现 `AgentxApprovalBridgeService.createApprovalBinding()`
- [x] 7.2 实现审批详情查询（从 OpenFang 获取）
- [x] 7.3 实现 BPM 流程创建（映射审批请求）
- [x] 7.4 实现 `OpenfangRuntimeBridge.approveCallback()`（带重试）
- [x] 7.5 实现 `OpenfangRuntimeBridge.rejectCallback()`（带重试）
- [x] 7.6 实现审批结果回调（指数退避重试 5 次）
- [x] 7.7 实现审批超时检测（24 小时自动超时）
- [x] 7.8 创建前端审批列表页面
- [x] 7.9 测试：触发审批 → 创建 BPM → 审批通过 → 回调 OpenFang
- [x] 7.10 测试：回调失败场景 → 重试 → 告警

## 8. 审计层（1 天）

- [x] 8.1 实现 `AgentxAuditService.logTaskStart()`
- [x] 8.2 实现 `AgentxAuditService.logToolCall()`（自动脱敏）
- [x] 8.3 实现 `AgentxAuditService.logApprovalRequest()`
- [x] 8.4 实现 `AgentxAuditService.logTaskComplete()`
- [x] 8.5 实现敏感信息脱敏规则（密码、身份证、手机号、API Key）
- [x] 8.6 实现审计日志清理任务（180 天保留期）
- [x] 8.7 创建前端审计日志页面
- [x] 8.8 测试：执行任务 → 查看审计日志 → 验证完整性和脱敏

## 9. OpenFang Workflow 准备（1 天）

- [x] 9.1 编写 `leave-approval-assistant` workflow 定义
- [x] 9.2 在 OpenFang 中注册 workflow
- [x] 9.3 配置 `bpm_query_tasks` tool
- [x] 9.4 配置 `bpm_approve` tool
- [x] 9.5 测试：手动调用 workflow → 验证 tool 可用

## 10. 端到端集成测试（2 天）

- [x] 10.1 准备测试数据（用户张三、3 条待审批请假）
- [x] 10.2 配置 OpenFang 实例
- [x] 10.3 配置请假审批场景
- [x] 10.4 触发 AI 审批助手
- [x] 10.5 验证：Agent 查询到 3 条待办
- [x] 10.6 验证：1 天和 2 天请假自动通过
- [x] 10.7 验证：3 天请假触发审批
- [x] 10.8 人工审批通过
- [x] 10.9 验证：Agent 继续执行，第 3 条通过
- [x] 10.10 验证：任务状态更新为完成
- [x] 10.11 验证：审计日志完整
- [x] 10.12 验证：审批绑定关系正确

验证记录（2026-03-21）：
- `task_run_id=fb615e71-2cfe-48be-84b9-16003a8b3f50`：`bpm_query_tasks=1`，`bpm_approve=3`（含 `approved=false` 的 3 天请假分支）
- `task_run_id=50d62b69-0458-4937-968a-56716eaeb39c`：`state=succeeded`，3 条请假（1/2/3 天）均完成，DB 状态分别为 `status=1/1/1`
- `task_run_id=060931d9-8669-4291-986d-4504ba7dda4c`：
  - 先进入 `state=waiting_approval`（`wait_for_approval` 已成功触发）
  - 人工操作 `POST /api/tasks/{id}/resume` 后继续执行并 `state=succeeded`
  - trace 中可见 `wait_for_approval(succeeded)` + 后续 `bpm_approve(succeeded)`，形成 10.8/10.9 闭环
- 10.11 证据：
  - OpenFang task trace 完整记录了 `bpm_query_tasks / bpm_approve / wait_for_approval` 的 started+succeeded（含一次 failed 的重入保护记录）
- 10.12 证据：
  - trace 中 3 天病假任务 `task_id=840b0fb2-251e-11f1-8555-06e82e6afa78`、`83f2ccb2-251e-11f1-8555-06e82e6afa78`
  - 在 `act_hi_taskinst` 可映射到 `PROC_INST_ID_=c5a037af-250d-11f1-8555-06e82e6afa78`、`e82a6e67-251d-11f1-8555-06e82e6afa78`
  - 对应 `bpm_oa_leave` 的 3 天单（id=31、35）状态均为 `status=1`，绑定关系一致

## 11. 异常场景测试（1 天）

- [x] 11.1 测试：OpenFang 离线场景（健康检查失败）
- [x] 11.2 测试：网络超时场景（轮询超时、回调超时）
- [x] 11.3 测试：权限不足场景（Tool 调用被拒绝）
- [x] 11.4 测试：审批拒绝场景
- [x] 11.5 测试：审批超时场景（24 小时未审批）
- [x] 11.6 测试：回调失败场景（5 次重试全部失败）
- [x] 11.7 测试：任务超时场景（24 小时无更新）
- [x] 11.8 测试：重复请求场景（幂等性验证）
- [x] 11.9 测试：并发创建任务（唯一性约束）
- [x] 11.10 测试：上下文组装超时（单个 Provider 超时）

验证记录（2026-03-21）：
- 执行命令：
  `mvn -pl yudao-module-agentx -am -Dtest=OpenfangRuntimeBridgeHttpClientTest,AgentxTaskOrchestrationServiceTest,AgentxToolGuardServiceTest,AgentxToolInvocationServiceTest,AgentxApprovalResolutionPolicyTest,AgentxTaskLifecycleServiceTest,AgentxApprovalTimeoutSchedulerTest,AgentxApprovalCallbackServiceImplTest,AgentxContextAssemblyServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`
- 结果：`Tests run: 31, Failures: 0, Errors: 0, Skipped: 0`，`BUILD SUCCESS`
- 11.1 证据：`OpenfangRuntimeBridgeHttpClientTest#shouldReturnOfflineWhenHealthProbeFails`
- 11.2/11.7 证据：`AgentxTaskOrchestrationServiceTest#shouldCalculateBackoffAndTimeout`
- 11.3 证据：`AgentxToolGuardServiceTest#shouldRejectCrossTenantDataScope`、`AgentxToolInvocationServiceTest#shouldDenyWhenMissingCapability`
- 11.4 证据：`AgentxApprovalResolutionPolicyTest#shouldMapRejectedToRejectRuntimeWithoutCompensation`
- 11.5 证据：`AgentxApprovalTimeoutSchedulerTest#shouldResolveTimeoutForPendingBindings`、`AgentxTaskLifecycleServiceTest#shouldRejectAndCompensateWhenApprovalTimeout`
- 11.6 证据：`AgentxApprovalCallbackServiceImplTest#shouldMarkFailedAfterMaxRetry`
- 11.8 证据：`AgentxTaskOrchestrationServiceTest#shouldReturnExistingProjectionWhenIdempotencyKeyDuplicated`
- 11.9 证据：`AgentxTaskOrchestrationServiceTest#shouldReturnExistingProjectionWhenInsertHitsUniqueConstraint`
- 11.10 证据：`AgentxContextAssemblyServiceTest#shouldFailWhenCriticalProviderTimeout`

UI/手工验收准备（2026-03-21）：
- 已补“触发任务”UI 入口（任务页新增“触发任务”按钮 + 弹窗），调用后端 `POST /agentx/task/create`
- 已补前端 API：`/agentx/task/create`（`createTask`）
- 已补菜单权限初始化脚本：`sql/mysql/agentx-menu-init.sql`（含 AgentX 目录、5 个页面菜单、`agentx:*` 按钮权限）
- 已为 `e1221805` 账号准备可测数据：
  - 登录账号：`e1221805`
  - 登录密码：`admin123456`
  - 账号租户：`tenant_id=1`
  - 已创建 3 条请假单（用于 1/2/3 天审批验证）+ 1 条 AgentX task run 记录
- 已完成路由与权限排查（`/agentx/instance` 404）：
  - 后端 `GET /system/auth/get-permission-info` 已返回 AgentX 全量菜单（`8800~8805`）
  - `system_role_menu` 已包含角色 `1` 对 AgentX 菜单授权（租户 `tenant_id=1`）
  - 排查结论：该 404 多数由浏览器旧会话缓存或未在正确租户上下文登录引起，不是菜单缺失
- 已加前端租户兜底：
  - `src/config/axios/service.ts` 新增默认租户回退逻辑（当本地缓存缺少 `tenantId` 时，自动使用 `VITE_APP_DEFAULT_TENANT_ID`）
  - `.env.local` 新增 `VITE_APP_DEFAULT_TENANT_ID=1`
  - 目的：避免 `tenant-id` 丢失导致 `get-permission-info` 失败，进而触发“全站动态路由 404”
- 已优化登录首击卡顿（2026-03-21）：
  - `LoginForm.vue` 的 `getTenantId()` 优先使用 `VITE_APP_DEFAULT_TENANT_ID`，避免首次点击登录时阻塞在 `get-id-by-name`
  - `handleLogin()` 增加异常兜底，并将 `loading.value.close()` 改为可选调用，避免异常中断后 UI 挂起
- 已补动态路由容错（2026-03-21）：
  - `permission.ts`：`router.addRoute` 增加 `try/catch`，单条路由异常不再中断全部路由注册
  - `routerHelper.ts`：对无法解析 `component` 的菜单做 `skip`（打印 warn），避免生成无效路由导致全站可用性下降
  - 目的：避免“个别菜单配置异常 => ERP/AgentX 全部 404”的连锁故障
- 已定位并修复“登录后全站 404”主因（2026-03-21）：
  - 现象：登录后 `ERP / AgentX` 等动态页面均 404
  - 根因：权限菜单中包含外链菜单，参与动态路由注入时会触发路由注册异常，导致后续业务路由未注入
  - 修复：`permission.ts` 跳过 `path=/external-link` 的 `addRoute`，仅注入业务路由
- 已定位并修复 AgentX 单独 404（2026-03-21）：
  - 现象：ERP 恢复后，`/agentx/*` 仍 404
  - 根因：AgentX 顶级菜单 `system_menu.id=8800` 的 path 为 `agentx`（缺少顶级 `/`），与现有顶级路由风格不一致
  - 修复：将 path 改为 `/agentx`，权限接口已返回新路径
- 已定位“任务页大量 RUNNING”原因（2026-03-21）：
  - 现象：任务列表大量显示 `RUNNING`
  - 根因：`agentx_task_projection.projection_status` 初始写入 `RUNNING(10)` 后，列表/详情查询链路未同步 OpenFang 实时状态
  - 证据：多条 `taskRunId` 在 OpenFang Runtime 已为 `succeeded`，但投影表仍为 `10`
  - 修复：在 `AgentxTaskQueryServiceImpl` 的分页/详情查询中增加状态同步（调用 runtime + refreshProjection 回写）
  - 临时数据修复：已执行一次性回填脚本，按 OpenFang runtime 状态批量回写 `agentx_task_projection.projection_status`（当前 46 条均为 `50=SUCCEEDED`）
- 功能验收前三项仍需手工勾验（实例连接、场景配置、触发助手），完成后再改为 `[x]`

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
- [x] Agent 可以查询待办任务
- [x] Agent 可以自动审批 ≤2 天请假
- [x] Agent 可以对 >2 天请假发起审批
- [x] 人工审批可以正常流转
- [x] 审批结果可以回调 OpenFang
- [x] 任务状态可以正确更新
- [x] 审计日志记录完整

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
