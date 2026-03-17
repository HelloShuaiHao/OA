# AgentX Phase 1 端到端技术规范

## 1. 核心验收用例

### 1.1 用例描述

**场景**：请假审批助手自动处理待办

**前置条件**：
- OpenFang 实例运行在 `http://localhost:4201`
- 已部署 `leave-approval-assistant` workflow
- 用户张三（ID: 100）有 3 条待审批请假：
  - 请假 1：1 天（应自动通过）
  - 请假 2：2 天（应自动通过）
  - 请假 3：3 天（应触发人工审批）

**执行流程**：
```
1. 管理员配置 OpenFang 实例
   → POST /admin-api/agentx/instance/create
   → 测试连接成功

2. 管理员配置请假场景
   → POST /admin-api/agentx/scenario/create
   → 映射到 workflow: leave-approval-assistant

3. 张三触发 AI 助手
   → POST /admin-api/agentx/task/start
   → 返回 task_projection_id: 1

4. 系统创建任务
   → 组装上下文（BPM + User + Leave）
   → 调用 OpenFang: POST /api/workflows/{id}/run
   → 返回 task_run_id: tr_abc123
   → 更新 TaskProjection 状态为 RUNNING

5. Agent 执行（OpenFang 内部）
   → 调用 bpm_query_tasks
   → 返回 3 条待办
   → 应用规则：days <= 2 自动通过
   → 调用 bpm_approve(请假1)
   → 调用 bpm_approve(请假2)
   → 检测请假3需要审批
   → 创建 approval gate

6. OA 轮询检测到审批
   → GET /api/tasks/tr_abc123
   → 发现 pending_approval_ids: [ap_xyz789]
   → 查询审批详情
   → 创建 ApprovalBinding
   → 启动 BPM 流程

7. 张三审批通过
   → BPM 流程完成
   → 回调 OpenFang: POST /api/approvals/ap_xyz789/approve

8. Agent 继续执行
   → 调用 bpm_approve(请假3)
   → 任务完成

9. OA 同步结果
   → 轮询发现任务完成
   → 更新 TaskProjection 状态为 COMPLETED
   → 记录审计日志
```

**验收标准**：
- ✅ 管理员可以配置 OpenFang 实例（endpoint、API Key）
- ✅ 管理员可以测试连接（健康检查成功）
- ✅ 管理员可以看到实例在线状态（定时刷新）
- ✅ 管理员可以配置请假审批场景（映射 workflow_id）
- ✅ 管理员可以配置上下文需求（contextProviders）
- ✅ 管理员可以启用/禁用场景
- ✅ 用户可以触发 AI 审批助手
- ✅ 1 天和 2 天请假自动通过（无需人工介入）
- ✅ 3 天请假经过 BPM 审批后通过
- ✅ OA 可以看到任务状态（PENDING → RUNNING → BLOCKED → RUNNING → COMPLETED）
- ✅ OA 可以看到任务详情（trace_events、workflow_projections）
- ✅ OA 可以看到审批列表（待审批、已审批、已拒绝）
- ✅ 审计日志记录所有关键操作（任务创建、Tool 调用、审批请求、任务完成）
- ✅ 审计日志自动脱敏（密码、身份证、API Key）
- ✅ 审批绑定关系正确（openfang_approval_id ↔ bpm_process_instance_id）
- ✅ 幂等性保证（重复请求返回相同结果）
- ✅ 回调失败有重试和告警

## 2. 数据模型

### 2.1 实例配置

```java
@Data
public class AgentxOpenfangInstanceDO {
    private Long id;
    private Long tenantId;
    private String instanceName;      // "本地开发实例"
    private String endpoint;          // "http://localhost:4201"
    private String apiKeyEncrypted;   // AES 加密
    private Integer status;           // 0=离线 1=在线
    private String version;           // "0.3.0"
    private LocalDateTime lastHeartbeat;
}
```

### 2.2 场景配置

```java
@Data
public class AgentxScenarioConfigDO {
    private Long id;
    private String scenarioCode;      // "oa.leave.approval"
    private String scenarioName;      // "请假审批助手"
    private String openfangWorkflowId; // "leave-approval-assistant"
    private String workflowVersion;   // "1.0.0"
    private Boolean enabled;
    private String config;            // JSON: 包含上下文配置和业务配置
}
```

**config 字段示例**：
```json
{
  "autoApproveMaxDays": 2,
  "contextProviders": [
    {
      "type": "bpm_tasks",
      "filter": {"processKey": "leave", "status": "pending"}
    },
    {
      "type": "user_profile",
      "fields": ["name", "dept", "position"]
    },
    {
      "type": "api",
      "url": "/admin-api/bpm/leave/balance",
      "method": "GET",
      "params": {"userId": "${user.id}"}
    }
  ]
}
```

### 2.3 任务投影

```java
@Data
public class AgentxTaskProjectionDO {
    private Long id;
    private String scenarioCode;
    private String businessKey;       // "user:100"
    private String idempotencyKey;    // UUID
    private String openfangTaskRunId; // "tr_abc123"
    private Integer projectionStatus; // 0=PENDING 1=RUNNING 2=BLOCKED 3=COMPLETED 4=FAILED
    private String resultSummary;     // "已处理 3 条请假，2 条自动通过，1 条人工审批"
}
```

### 2.4 审批绑定

```java
@Data
public class AgentxApprovalBindingDO {
    private Long id;
    private Long taskProjectionId;
    private String openfangApprovalId;    // "ap_xyz789"
    private String bpmProcessInstanceId;  // BPM 流程实例 ID
    private Integer approvalStatus;       // 0=PENDING 1=APPROVED 2=REJECTED 3=TIMEOUT 4=CALLBACK_FAILED
    private String toolName;              // "bpm_approve"
    private String toolParams;            // JSON
    private Boolean callbackFailed;       // 回调是否失败
    private Integer callbackRetryCount;   // 回调重试次数
}
```

## 3. API 协议

### 3.1 OA → OpenFang

#### 创建任务
```
POST /api/workflows/{workflow_id}/run
Headers:
  Authorization: Bearer {api_key}
  X-Tenant-ID: 0

Request:
{
  "input": {
    "user": {
      "user_id": 100,
      "username": "zhangsan"
    },
    "bpm": {
      "pending_tasks": [...]
    }
  }
}

Response:
{
  "run_id": "wfr_abc123",           # workflow run id（投影）
  "task_run_id": "tr_xyz789",       # task run id（主真相）
  "status": "running"
}
```

**幂等性保证**：
- OA 在创建 `TaskProjection` 时生成 UUID 作为 `idempotency_key`
- 存入数据库前检查 `uk_idempotency` 约束
- 如果重复请求，返回已存在的 `task_projection_id`
- OpenFang 内部不处理幂等性（由 OA 保证）

#### 查询任务状态
```
GET /api/tasks/{task_run_id}

Response:
{
  "task_run_id": "tr_xyz789",
  "current_state": "waiting_for_approval",  # TaskRunState 枚举
  "state_version": 5,                       # 乐观并发版本号
  "pending_approval_ids": ["ap_abc123"],
  "workflow_projections": [
    {
      "workflow_run_id": "wfr_abc123",
      "active": false,
      "finished_at": "2026-03-17T08:00:00Z"
    }
  ],
  "trace_events": [
    {
      "timestamp": "2026-03-17T07:55:00Z",
      "event_type": "tool_call",
      "data": {"tool": "bpm_query_tasks", "result": "success"}
    }
  ],
  "last_error": null,
  "created_at": "2026-03-17T07:50:00Z",
  "updated_at": "2026-03-17T08:00:00Z"
}
```

**轮询策略**：
- 初始间隔：2 秒
- 指数退避：最大 30 秒
- 超时时间：24 小时（审批场景）
- 失败重试：3 次后标记 OpenFang 离线
- 终态检测：`succeeded`、`failed_terminal`、`cancelled`、`compensated` 停止轮询

#### 审批回调
```
POST /api/approvals/{approval_id}/approve
Request:
{
  "comment": "同意",
  "approved_by": "zhangsan"
}

Response:
{
  "success": true,
  "task_run_id": "tr_xyz789",
  "new_state": "running"
}
```

**回调可靠性保证**：
- 重试策略：指数退避，最多 5 次（1s, 2s, 4s, 8s, 16s）
- 超时时间：每次请求 10 秒
- 失败处理：
  - 如果 5 次重试全部失败，标记 `ApprovalBinding.callback_failed = true`
  - 记录审计日志：`APPROVAL_CALLBACK_FAILED`
  - 发送告警通知管理员
  - 任务状态保持 `waiting_for_approval`（不自动失败）
- 幂等性：OpenFang 检查 approval 状态，已处理则返回成功

### 3.2 OpenFang → OA

#### Tool 调用：查询待办
```
POST /admin-api/agentx/tools/bpm_query_tasks/invoke
Request:
{
  "agent_id": "leave-approval-assistant",
  "task_run_id": "tr_abc123",
  "params": {
    "user_id": 100
  }
}

Response:
{
  "success": true,
  "data": [
    {"task_id": 1, "type": "leave", "days": 1},
    {"task_id": 2, "type": "leave", "days": 2},
    {"task_id": 3, "type": "leave", "days": 3}
  ]
}
```

#### Tool 调用：审批
```
POST /admin-api/agentx/tools/bpm_approve/invoke
Request:
{
  "agent_id": "leave-approval-assistant",
  "task_run_id": "tr_abc123",
  "params": {
    "task_id": 1,
    "approved": true,
    "reason": "自动审批通过"
  }
}

Response:
{
  "success": true,
  "data": {
    "approved": true
  }
}
```

## 4. 状态机

### 4.1 TaskProjection 状态转换（基于 OpenFang TaskRunState）

```
Created → Ready → Running → Succeeded
                     ↓
                  WaitingForApproval → Ready (审批通过后恢复)
                     ↓
                  FailedRecoverable → Ready (可重试)
                     ↓
                  FailedTerminal (终态)
                     ↓
                  Cancelling → Cancelled (终态)
```

**状态映射**（OpenFang → OA）：
- `created` → `PENDING`
- `ready`, `running` → `RUNNING`
- `waiting_for_approval` → `BLOCKED`
- `succeeded` → `COMPLETED`
- `failed_recoverable`, `failed_terminal` → `FAILED`
- `cancelled` → `CANCELLED`

**异常场景处理**：
- Agent 崩溃：状态停留在 `RUNNING`，24 小时后自动标记 `FAILED`
- 用户取消：调用 `POST /api/tasks/{id}/cancel`，状态变为 `CANCELLING` → `CANCELLED`
- 审批超时：24 小时未审批，状态变为 `FAILED_TERMINAL`
- OpenFang 离线：停止轮询，任务状态保持不变，等待恢复

### 4.2 ApprovalBinding 状态转换

```
PENDING → APPROVED → (回调成功)
   ↓
REJECTED → (回调成功)
   ↓
TIMEOUT → (24小时未审批)
   ↓
CALLBACK_FAILED → (回调失败，需人工介入)
```

**状态字段**：
- `approval_status`: 0=PENDING, 1=APPROVED, 2=REJECTED, 3=TIMEOUT, 4=CALLBACK_FAILED
- `callback_retry_count`: 回调重试次数
- `callback_failed`: 是否回调失败（需告警）

## 5. 权限模型

### 5.1 权限检查边界

**OA 侧检查**（Tool 调用前）：
- 检查 Agent 是否有该 Tool 的调用权限
- 检查用户是否委托了该权限给 Agent
- 检查业务规则（如：审批金额限制）

**OpenFang 侧**：
- 不做权限检查，完全信任 OA
- 只负责执行 Tool 调用

**权限变更处理**：
- 权限变更立即生效
- 正在运行的任务：下次 Tool 调用时检查新权限
- 如果权限不足，Tool 调用失败，任务进入 `FailedRecoverable` 状态

### 5.2 Agent 能力

```java
public enum AgentCapability {
    BPM_READ,           // 读取 BPM 数据
    BPM_APPROVE_LEAVE,  // 审批请假（≤2天）
    BPM_APPROVE_ALL     // 审批所有（需人工确认）
}
```

### 5.3 权限计算

```
实际权限 = Agent 能力 ∩ 用户委托范围

示例：
- Agent 能力: [BPM_READ, BPM_APPROVE_LEAVE]
- 用户委托: [BPM_READ, BPM_APPROVE_LEAVE, BPM_APPROVE_ALL]
- 实际权限: [BPM_READ, BPM_APPROVE_LEAVE]

结果：可以自动审批 ≤2 天，>2 天需要人工审批
```

## 5.5 密钥管理

**加密方案**：
- 算法：AES-256-GCM
- 密钥来源：环境变量 `AGENTX_ENCRYPTION_KEY`（32 字节 base64）
- 存储格式：`{iv}:{ciphertext}:{tag}` (base64 编码)

**密钥轮换**：
- 支持多版本密钥（`AGENTX_ENCRYPTION_KEY_V1`, `V2`...）
- 解密时尝试所有版本
- 后台任务定期重新加密旧密钥数据

**访问控制**：
- 只有 `OpenfangInstanceService` 可以解密
- 解密后的 API Key 不缓存，用完立即清零
- 审计日志不记录明文 API Key

## 6. 审计要求

每个关键操作必须记录审计日志：

```java
@Data
public class AgentxAuditEventDO {
    private String eventType;     // TASK_START, TOOL_CALL, APPROVAL_REQUEST, TASK_COMPLETE
    private String agentCode;     // "leave-approval-assistant"
    private Long taskProjectionId;
    private Long userId;
    private String action;        // "bpm_approve"
    private String result;        // "SUCCESS"
    private String details;       // JSON
}
```

### 6.1 审计日志格式

**details 字段结构**（JSON）：
```json
{
  "tool_name": "bpm_approve",
  "params": {
    "task_id": 123,
    "approved": true
  },
  "result": {
    "success": true
  },
  "duration_ms": 150
}
```

### 6.2 敏感信息脱敏

**自动脱敏规则**：
- 密码字段：`password`, `passwd`, `pwd` → `***`
- 身份证号：18 位数字 → 前 6 位 + `****` + 后 4 位
- 手机号：11 位数字 → 前 3 位 + `****` + 后 4 位
- API Key：任何包含 `key`, `token`, `secret` 的字段 → `***`

### 6.3 数据保留策略

- 保留期限：180 天
- 归档策略：90 天后归档到对象存储
- 清理任务：每天凌晨 2 点执行
- 关键事件：`APPROVAL_REQUEST` 永久保留

## 7. 幂等性保证

### 7.1 任务创建幂等性

```java
public TaskProjectionDO createTask(CreateTaskRequest req) {
    String idempotencyKey = UUID.randomUUID().toString();

    // 1. 先检查是否已存在
    TaskProjectionDO existing = mapper.selectByIdempotencyKey(idempotencyKey);
    if (existing != null) {
        return existing;  // 幂等返回
    }

    // 2. 创建新任务
    TaskProjectionDO task = new TaskProjectionDO();
    task.setIdempotencyKey(idempotencyKey);
    task.setProjectionStatus(TaskProjectionStatus.PENDING);

    try {
        mapper.insert(task);  // uk_idempotency 约束保证唯一性
    } catch (DuplicateKeyException e) {
        // 并发创建，返回已存在的
        return mapper.selectByIdempotencyKey(idempotencyKey);
    }

    return task;
}
```

### 7.2 Tool 调用幂等性

- OpenFang 内部保证（基于 workflow run 的执行日志）
- OA 侧不需要额外处理

### 7.3 审批回调幂等性

```java
public void approveCallback(String approvalId, ApprovalResult result) {
    ApprovalBindingDO binding = mapper.selectByApprovalId(approvalId);

    // 幂等检查
    if (binding.getApprovalStatus() != ApprovalStatus.PENDING) {
        log.info("Approval already processed: {}", approvalId);
        return;  // 已处理，直接返回
    }

    // 更新状态
    binding.setApprovalStatus(result.isApproved()
        ? ApprovalStatus.APPROVED
        : ApprovalStatus.REJECTED);
    mapper.updateById(binding);

    // 回调 OpenFang（带重试）
    callbackWithRetry(approvalId, result);
}
```

## 8. 上下文组装层设计

### 8.1 核心接口

```java
public interface ContextProvider {
    String getType();  // "bpm_tasks", "user_profile", "api"
    Map<String, Object> provide(ContextRequest request);
}

@Data
public class ContextRequest {
    private Long userId;
    private String scenarioCode;
    private Map<String, Object> params;  // 从配置中解析的参数
}
```

### 8.2 内置 Provider

**BpmContextProvider**：查询任意流程的待办任务
```java
@Component
public class BpmContextProvider implements ContextProvider {
    public String getType() { return "bpm_tasks"; }

    public Map<String, Object> provide(ContextRequest req) {
        String processKey = (String) req.getParams().get("processKey");
        List<Task> tasks = taskService.createTaskQuery()
            .processDefinitionKey(processKey)
            .taskAssignee(req.getUserId().toString())
            .list();
        return Map.of("tasks", tasks);
    }
}
```

**UserContextProvider**：查询用户信息
```java
@Component
public class UserContextProvider implements ContextProvider {
    public String getType() { return "user_profile"; }

    public Map<String, Object> provide(ContextRequest req) {
        AdminUserDO user = userService.getUser(req.getUserId());
        List<String> fields = (List) req.getParams().get("fields");
        return filterFields(user, fields);
    }
}
```

**ApiContextProvider**：调用配置的业务 API
```java
@Component
public class ApiContextProvider implements ContextProvider {
    public String getType() { return "api"; }

    public Map<String, Object> provide(ContextRequest req) {
        String url = (String) req.getParams().get("url");
        String method = (String) req.getParams().get("method");
        Map<String, Object> params = resolveParams(req);
        return httpClient.call(method, url, params);
    }
}
```

### 8.3 动态组装

```java
@Service
public class AgentxContextAssemblyService {
    @Autowired
    private List<ContextProvider> providers;

    public Map<String, Object> assemble(String scenarioCode, Long userId) {
        ScenarioConfigDO config = getConfig(scenarioCode);
        List<ContextConfig> contextConfigs = parseContextConfigs(config.getConfig());

        return contextConfigs.parallelStream()
            .collect(Collectors.toMap(
                ContextConfig::getType,
                cc -> findProvider(cc.getType())
                    .provide(buildRequest(userId, cc))
            ));
    }
}
```

### 8.5 超时和降级策略

```java
@Service
public class AgentxContextAssemblyService {
    private static final int PROVIDER_TIMEOUT_MS = 5000;  // 单个 Provider 超时 5 秒

    public Map<String, Object> assemble(String scenarioCode, Long userId) {
        ScenarioConfigDO config = getConfig(scenarioCode);
        List<ContextConfig> contextConfigs = parseContextConfigs(config.getConfig());

        // 并行组装，带超时控制
        CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() ->
            contextConfigs.parallelStream()
                .collect(Collectors.toMap(
                    ContextConfig::getType,
                    cc -> provideWithTimeout(cc, userId)
                ))
        );

        try {
            return future.get(15, TimeUnit.SECONDS);  // 总超时 15 秒
        } catch (TimeoutException e) {
            log.error("Context assembly timeout", e);
            throw new ContextAssemblyException("上下文组装超时");
        }
    }

    private Map<String, Object> provideWithTimeout(ContextConfig cc, Long userId) {
        try {
            CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() ->
                findProvider(cc.getType()).provide(buildRequest(userId, cc))
            );
            return future.get(PROVIDER_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.warn("Provider timeout: {}", cc.getType());
            // 降级：返回空数据，不阻塞整个流程
            return Map.of("error", "timeout", "type", cc.getType());
        } catch (Exception e) {
            log.error("Provider failed: {}", cc.getType(), e);
            // 降级：返回错误信息
            return Map.of("error", e.getMessage(), "type", cc.getType());
        }
    }
}
```

**降级策略**：
- 单个 Provider 失败：记录错误，继续组装其他上下文
- 关键 Provider 失败（如 BPM）：抛出异常，任务创建失败
- 可选 Provider 失败（如业务 API）：降级为空数据，任务继续执行

**无需写代码**，只需在场景配置中添加：
```json
{
  "scenarioCode": "oa.expense.approval",
  "contextProviders": [
    {
      "type": "bpm_tasks",
      "filter": {"processKey": "expense"}
    },
    {
      "type": "api",
      "url": "/admin-api/finance/budget/remaining",
      "method": "GET"
    }
  ]
}
```

## 9. 错误处理

### 9.1 Tool 调用失败
```java
if (!toolResult.isSuccess()) {
    auditService.log(TOOL_CALL_FAILED, toolName, toolResult.getError());
    throw new ToolInvocationException(toolResult.getError());
}
```

### 9.2 审批超时
```java
@Scheduled(fixedDelay = 60000)
public void checkApprovalTimeout() {
    List<ApprovalBindingDO> pending = mapper.selectPendingApprovals();
    LocalDateTime now = LocalDateTime.now();

    for (ApprovalBindingDO binding : pending) {
        if (Duration.between(binding.getCreateTime(), now).toHours() > 24) {
            binding.setApprovalStatus(ApprovalStatus.TIMEOUT);
            mapper.updateById(binding);

            // 通知 OpenFang 审批超时
            openfangBridge.rejectApproval(binding.getOpenfangApprovalId(), "审批超时");

            // 记录审计
            auditService.log(APPROVAL_TIMEOUT, binding);
        }
    }
}
```

### 9.3 OpenFang 离线
```java
if (!instanceService.isOnline(instanceId)) {
    throw new OpenfangOfflineException("OpenFang 实例离线: " + instanceId);
}
```

### 9.4 任务超时检测
```java
@Scheduled(fixedDelay = 300000)  // 每 5 分钟
public void checkTaskTimeout() {
    List<TaskProjectionDO> running = mapper.selectRunningTasks();
    LocalDateTime now = LocalDateTime.now();

    for (TaskProjectionDO task : running) {
        if (Duration.between(task.getUpdateTime(), now).toHours() > 24) {
            task.setProjectionStatus(TaskProjectionStatus.FAILED);
            task.setFailureSummary("任务执行超时（24小时无更新）");
            mapper.updateById(task);

            auditService.log(TASK_TIMEOUT, task);
        }
    }
}
```

### 9.5 回调失败处理
```java
private void callbackWithRetry(String approvalId, ApprovalResult result) {
    int maxRetries = 5;
    int[] delays = {1000, 2000, 4000, 8000, 16000};  // 指数退避

    for (int i = 0; i < maxRetries; i++) {
        try {
            openfangBridge.approveCallback(approvalId, result);
            return;  // 成功
        } catch (Exception e) {
            log.warn("Callback failed, retry {}/{}: {}", i + 1, maxRetries, e.getMessage());
            if (i < maxRetries - 1) {
                Thread.sleep(delays[i]);
            }
        }
    }

    // 所有重试失败
    ApprovalBindingDO binding = mapper.selectByApprovalId(approvalId);
    binding.setCallbackFailed(true);
    mapper.updateById(binding);

    auditService.log(APPROVAL_CALLBACK_FAILED, binding);
    alertService.sendAlert("审批回调失败", binding);
}
```
