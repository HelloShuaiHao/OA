# 提案：建设 OpenFang 统一集成平台

## 概要

建设一个面向平台的 OA 与 OpenFang 集成层，而不是继续围绕单一业务场景构建专用 Agent 接入。该平台负责把 OA 的治理约束翻译为 OpenFang 可执行的 Agent 能力，为审批、分析、知识和经营类场景提供统一底座，同时避免与现有 `yudao-module-ai` 能力重复建设。

## 问题背景

当前方案更适合验证一个小型业务闭环，但不足以支撑后续几年持续扩展的平台化建设。如果系统继续按场景推进，每增加一个场景都会重复建设以下能力：

- 身份与委托关系处理
- 权限与数据域控制
- Tool 注册与访问控制
- 业务上下文组装
- 审批桥接
- 审计与运行轨迹留痕

最终会导致安全边界不一致、代码重复、审计口径不统一，系统演进成本持续上升。当前还存在三个必须先钉死的现实边界：审批真相与运行时投影的精确定义、AgentX 与现有 `yudao-module-ai` 的关系、以及 Phase 1 真实可落地的协议形态。

## 变更内容

### 1. 建立四层总体架构

- `治理面`：由 OA 负责，作为组织、角色、BPM、业务单据、主数据和审计归档的真相源，同时作为审批决议真相源。
- `集成面`：新增平台公共层，负责身份映射、权限映射、任务绑定、上下文组装、审批桥接、状态投影同步、审计标准化和场景编排。
- `运行面`：由 OpenFang 负责 Agent 定义、Skill 装配、任务执行、Tool 调用、中断恢复、失败重试，以及审批阻塞、`pending_approval_ids`、任务关联等运行时投影。
- `场景面`：各业务场景只保留最薄的装配层，不再重复定义基础设施。

### 2. 采用 Java 为主的落地策略

- 集成平台主实现采用 Java。
- 平台不默认从零起一整套平行 AI 基础设施，必须先冻结与 `yudao-module-ai` 的边界。
- 若启用现有 `yudao-module-ai`，优先复用其已具备的工作流管理、Tool 函数、ToolContext、租户与登录用户上下文能力；新增模块只承接 OA 治理约束与 OpenFang Runtime 之间的集成职责。
- 只有当现有 AI 模块无法承载特定治理职责时，才新增 `yudao-module-openfang` 或 `yudao-module-agentx` 作为集成模块。
- 第一阶段不将核心集成链路建立在 Rust 之上。
- Rust 作为后续高性能边缘组件的补充选项，用于沙箱执行器、协议网关或隔离型 Tool 代理。

### 3. 固化平台核心能力

平台必须提供以下可复用能力抽象，但这些能力不等于全部都要新建模块实现：

- Identity
- Authorization
- Tool Registry
- Context Assembly
- Task Orchestration
- Approval Bridge
- Audit & Observability
- Scenario Kit

### 4. 固化模块边界

建议先按职责边界拆分，而不是立即按物理模块全量落地：

- `agentx-domain`
- `agentx-identity`
- `agentx-authorization`
- `agentx-context`
- `agentx-runtime-bridge`
- `agentx-approval-bridge`
- `agentx-audit`
- `agentx-scenario`

其中 `workflow / tool registry / console` 必须优先评估是否直接复用 `yudao-module-ai` 已有能力，避免形成两套工作流、两套 Tool Registry、两套管理台。

### 5. 明确 OA 与 OpenFang 协议边界

OA 只保存外部任务标识、状态投影、风险等级、审批绑定、审批决议结果、结果摘要和审计摘要。OpenFang 保留 `TaskRun`、审批阻塞状态、`pending_approval_ids`、恢复/补偿语义等运行时投影。OpenFang 不得直接访问 OA 数据库表，不得绕过已注册 Tool 和审批控制直接执行写操作。

Phase 1 的协议形态明确采用 REST 主导，而不是预设外部 webhook 或事件总线集成：

- OA 通过 `POST /api/workflows/{id}/run` 创建执行，并获取 `task_run_id`
- OA 通过 `GET /api/tasks/{id}` 拉取 TaskRun 真相和运行时投影，并从 `pending_approval_ids` 获取待处理审批 ID 列表
- Phase 1 不允许把“扫描 `GET /api/approvals` 全量 pending 列表再本地匹配”作为默认正路；必须二选一：
  - 为 OpenFang 新增按 `approval_id` / `task_run_id` 精确查询审批详情的外部接口，并将其纳入 Phase 1 基线
  - 或由 AgentX 维护一层待审批缓存/索引，将 `pending_approval_ids` 与审批详情做本地可查询映射
- OA 只有在拿到精确审批详情后，才能建立 `approval_id` 与 `task_run_id` 绑定，并补齐 BPM 建单所需的标题、原因、风险等级、动作摘要等字段
- OA 通过 OpenFang 已有审批批准/拒绝接口回写审批决议
- `POST /api/approvals` 不属于 OA 常规审批桥链路，它只用于外部系统向 OpenFang 手动注入 gate 的例外场景
- 是否新增 webhook / outbox 对外集成能力，放到后续阶段单独提案，不作为 Phase 1 前置条件

Phase 1 同时必须明确 `workflow_id` 与 workflow definition 的真相归属：

- Phase 1 的可执行 workflow definition 真相源在 OpenFang，而不在 OA
- OA / AgentX 只维护“场景 -> OpenFang workflow_id -> 版本约束”的映射配置与投影，不维护第二套可执行 workflow 定义
- `yudao-module-ai` 在 Phase 1 可以复用管理台、Tool、上下文等能力，但不作为 OpenFang 运行时 workflow_id 的真相源
- 如果后续要把 workflow definition 从 OA 同步到 OpenFang，必须单独立项定义发布、版本和回滚协议

### 6. 统一分阶段实施路径

- `Phase 0`：先冻结核心对象模型、审批真相与运行时投影边界、`yudao-module-ai` / AgentX 边界、Phase 1 REST 协议边界
- `Phase 1`：基于现有 REST 接口实现平台骨架与状态投影
- `Phase 2`：将请假审批助理作为首个场景插件接入
- `Phase 3`：扩展到报销、出差、采购等同类审批场景
- `Phase 4`：扩展到 CRM、ERP、知识和经营分析类场景

## 非目标

- 不为每个场景分别建设一套专用中间件
- 不允许 OpenFang 直接访问 OA 持久层
- 不在 OA 与 OpenFang 两侧各自维护互相争夺真相的一套审批状态机
- 不在未完成 `yudao-module-ai` 边界冻结前，直接新建一整套平行 workflow、tool registry、console
- 不把尚不存在的外部任务 webhook 当作 Phase 1 默认前提
- 不让场景模块反向定义平台边界

## 影响分析

### 预期收益

- 将分散的 Agent 功能沉淀为可复用的平台能力
- 保持 OA 负责治理真相，OpenFang 负责运行时真相
- 将审批决议真相与审批阻塞投影分离，避免破坏 OpenFang 现有恢复/阻塞语义
- 复用现有 AI 模块可复用能力，减少重复建设风险
- 统一审批、审计、授权和上下文装配模型
- 降低后续新场景的接入与维护成本

### 成本与风险

- 需要在大规模场景落地前先投入边界和模型设计成本
- 需要先梳理 `yudao-module-ai` 与集成平台的职责边界，否则容易出现重复建设
- 需要在后续开发中严格限制场景代码绕过平台抽象
- Phase 1 先采用轮询式 REST 集成，状态同步时效和压力需要单独评估

## 成功标准

- OA 与 OpenFang 的职责边界被明确并达成共识
- 审批决议真相在 OA BPM，审批阻塞与 `pending_approval_ids` 投影保留在 OpenFang 运行时
- `yudao-module-ai` 与 AgentX 的职责分层在落代码前完成冻结
- Phase 1 明确以现有 REST 接口可完成任务创建、状态查询、审批决议回推
- 平台核心抽象和模块边界在场景扩展前完成确认
- 请假审批能够作为平台上的首个薄场景插件实现
- 后续审批类和经营分析类场景能够复用统一的身份、授权、审批、上下文和审计基础设施
