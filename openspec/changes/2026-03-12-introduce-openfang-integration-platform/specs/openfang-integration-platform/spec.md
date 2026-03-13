## ADDED Requirements

### Requirement: 平台必须采用统一的四层架构

系统 MUST 将 OpenFang 集成设计为治理面、集成面、运行面、场景面四层结构，而不是按单一业务场景直接建设专用中间件。

#### Scenario: 设计新场景接入方案

- **GIVEN** 已存在 OA 与 OpenFang 的集成平台设计
- **WHEN** 团队新增一个业务场景，例如请假、报销或 CRM 分析
- **THEN** 该场景必须落在场景面
- **AND** 场景实现只能依赖集成面提供的公共能力
- **AND** 场景不得重新定义身份、审批、上下文组装和审计基础设施

### Requirement: OA 必须作为治理真相源

系统 MUST 由 OA 负责组织架构、人员岗位、角色权限、BPM 审批、业务单据、企业主数据和审计归档等治理真相。

#### Scenario: 查询治理数据

- **GIVEN** Agent 执行需要组织、角色或审批信息
- **WHEN** 系统组装运行所需治理数据
- **THEN** 相关真相数据必须来源于 OA
- **AND** OpenFang 不得自行维护一套等价治理真相

### Requirement: 审批决议真相与审批阻塞投影必须分离

系统 MUST 将审批决议真相保留在 OA BPM 中，同时允许 OpenFang 保留等待审批、`pending_approval_ids`、阻塞单元与任务关联等运行时投影，以保持现有 TaskRun 恢复和阻塞语义成立。

#### Scenario: 任务因审批进入阻塞

- **GIVEN** OpenFang 的 TaskRun 因高风险动作进入等待审批状态
- **WHEN** OA 查询该任务的运行状态
- **THEN** OA 可以看到该任务关联的审批绑定和状态投影
- **AND** OA 的审批结果必须仍然是唯一决议真相
- **AND** OpenFang 可以继续保留 `pending_approval_ids` 等运行时字段以支持阻塞、恢复和补偿

### Requirement: OpenFang 必须作为运行时真相源

系统 MUST 由 OpenFang 负责 Agent 定义、Skill 装配、TaskRun 执行、Tool 调用、中断恢复、失败重试和运行时审批阻塞等运行时能力。

#### Scenario: 查询任务执行状态

- **GIVEN** 用户在 OA 中查看一个 Agent 任务
- **WHEN** OA 展示任务状态
- **THEN** OA 只能保存该任务的外部标识与状态投影
- **AND** TaskRun 的内部状态机真相必须保持在 OpenFang

### Requirement: 平台必须提供统一的公共能力层

系统 MUST 提供统一的 Identity、Authorization、Tool Registry、Context Assembly、Task Orchestration、Approval Bridge、Audit & Observability、Scenario Kit 能力。

#### Scenario: 新场景复用平台能力

- **GIVEN** 团队接入一个新的审批类场景
- **WHEN** 开发该场景
- **THEN** 场景必须复用统一的身份、授权、审批、上下文和审计能力
- **AND** 不得为该场景单独建设重复的能力模块

### Requirement: AgentX 与 yudao-module-ai 的职责边界必须先冻结

系统 MUST 在建设 AgentX 集成层之前，先明确现有 `yudao-module-ai` 中哪些能力继续保留并复用，哪些能力由新增集成层承接，避免形成两套 workflow、两套 Tool Registry 或两套管理台。

#### Scenario: 规划平台模块落点

- **GIVEN** 仓库已存在 `yudao-module-ai` 的工作流管理、ToolFunction 和 ToolContext 能力
- **WHEN** 团队规划 AgentX 的模块边界
- **THEN** 必须先产出复用与新增边界清单
- **AND** 能复用的 AI 基础能力不得无故平行重建
- **AND** 新增集成层只承接 OA 治理约束与 OpenFang Runtime 之间的桥接职责

### Requirement: 平台必须采用 Java 为主的实现策略

系统 MUST 以 Java 作为集成平台第一阶段的主实现语言，并优先作为 OA 内部模块落地。

#### Scenario: 规划第一阶段实现技术栈

- **GIVEN** 团队需要确定平台第一阶段的技术实现路线
- **WHEN** 做出架构决策
- **THEN** 核心集成链路必须以 Java 实现
- **AND** Rust 不得承担第一阶段的核心业务中间层职责
- **AND** Rust 仅可作为后续高性能边缘组件的候选技术

### Requirement: OpenFang 只能通过受控集成能力访问 OA

系统 MUST 限制 OpenFang 只能访问集成面暴露的受控 Tool 和接口，禁止直接访问 OA 数据库或绕过审批控制执行写操作。

#### Scenario: 执行高风险写操作

- **GIVEN** Agent 需要执行一个会修改 OA 业务数据的高风险动作
- **WHEN** OpenFang 发起该动作
- **THEN** 该动作必须通过已注册 Tool 或受控接口发起
- **AND** 必须按策略进入审批桥接流程
- **AND** 不得直接写入 OA 数据库表

### Requirement: Phase 1 必须以现有 REST 接口为准

系统 MUST 在 Phase 1 基于 OpenFang 当前已公开的 REST 接口完成集成，不得把尚未定义的外部 webhook 或 outbox 契约作为默认前提。

#### Scenario: 制定第一阶段协议方案

- **GIVEN** OpenFang 当前对外公开了 `workflow run`、`GET /api/tasks/{id}`、`resume`、`compensate` 与审批相关 REST 接口
- **WHEN** 团队制定 Phase 1 集成方案
- **THEN** 必须以这些现有接口作为第一阶段集成基线
- **AND** OA 应通过任务创建接口获取 `task_run_id`
- **AND** OA 应通过 `GET /api/tasks/{id}` 读取任务真相与状态投影
- **AND** 如需 webhook 或 outbox 外部契约，必须另行定义并单独验收

### Requirement: OA 必须通过审批详情接口补齐 BPM 建单信息

系统 MUST 规定 OA 先通过 `GET /api/tasks/{id}` 获取 `pending_approval_ids`，再通过精确审批详情读取能力获取对应 `ApprovalRequest` 明细，用于创建 OA BPM 审批单。`GET /api/tasks/{id}` 本身不得被假定为包含 BPM 建单所需的全部审批详情，扫描全量 pending approvals 也不得作为默认正路。

#### Scenario: OA 为待审批任务创建 BPM 审批单

- **GIVEN** OA 已通过 `GET /api/tasks/{id}` 读取到某个任务的 `pending_approval_ids`
- **WHEN** OA 需要为该待审批动作创建 BPM 审批单
- **THEN** OA 必须再读取对应审批详情
- **AND** 审批标题、原因、风险等级、动作摘要等建单字段必须来自 `ApprovalRequest` 明细
- **AND** OA 必须按 `approval_id` 与 `task_run_id` 建立审批绑定关系

#### Scenario: 当前外部接口不支持精确审批查询

- **GIVEN** OpenFang 当前只提供全量 pending approvals 列表而不提供按 `approval_id` 或 `task_run_id` 的精确查询接口
- **WHEN** 团队设计 Phase 1 审批桥
- **THEN** 必须二选一
- **AND** 要么新增外部精确查询接口并纳入本次变更
- **AND** 要么由 AgentX 维护本地待审批缓存或索引
- **AND** 不得把“扫描全量 pending approvals 再自行匹配”写成默认链路

### Requirement: POST /api/approvals 不得作为 OA 常规审批桥链路

系统 MUST 将 `POST /api/approvals` 视为外部系统向 OpenFang 手动注入 gate 的例外接口，而不是 OA 常规审批桥的默认步骤。

#### Scenario: OA 处理 OpenFang 产生的待审批 gate

- **GIVEN** OpenFang 已经为某个任务产生待审批 gate
- **WHEN** OA 接入该审批流程
- **THEN** OA 应读取待审批详情并在 OA BPM 建单
- **AND** OA 最终只能通过 approve/reject 类接口回写审批决议
- **AND** OA 不得再次调用 `POST /api/approvals` 创建第二个平行 gate

### Requirement: workflow definition 与 workflow_id 的真相归属必须单一

系统 MUST 在 Phase 1 规定 OpenFang 为可执行 workflow definition 与 `workflow_id` 的真相源。OA / AgentX 只能保存场景到 OpenFang workflow 的映射和版本投影，不得维护第二套可执行 workflow definition 真相。

#### Scenario: OA 发起场景任务

- **GIVEN** OA 侧某个业务场景需要发起 OpenFang 执行
- **WHEN** AgentX 组装任务请求
- **THEN** AgentX 必须先从本地场景映射配置中解析出目标 `openfang_workflow_id`
- **AND** 映射配置中必须包含期望的 workflow 版本或版本约束
- **AND** 最终执行时使用的 `workflow_id` 真相必须来自 OpenFang

#### Scenario: 评估复用 yudao-module-ai workflow 能力

- **GIVEN** 团队希望复用 `yudao-module-ai` 的 workflow 管理能力
- **WHEN** 进行 Phase 1 方案设计
- **THEN** 可以复用 UI、配置管理或元数据维护能力
- **AND** 但不得让 OA 或 `yudao-module-ai` 成为 OpenFang 可执行 workflow_id 的第二真相源
- **AND** 如需定义从 OA 向 OpenFang 发布 workflow 的机制，必须另行立项并定义发布与版本协议

### Requirement: 场景模块必须保持薄装配

系统 MUST 将场景模块限制为入口定义、上下文声明、Tool 集声明和风险策略声明，不得在场景模块中建设平台基础设施。

#### Scenario: 实现请假审批助理

- **GIVEN** 平台已经具备统一公共能力
- **WHEN** 团队实现“请假审批助理”
- **THEN** 该场景只能实现类似 `LeaveScenarioDefinition`、`LeaveContextProvider`、`LeaveToolSet`、`LeaveApprovalPolicy` 的装配层内容
- **AND** 不得在该场景中内嵌专用身份、权限、审批和审计基础设施
- **AND** 不得为了该场景再平行建设第二套 workflow、tool registry 或 console

### Requirement: 平台必须支持分阶段落地

系统 MUST 按 Phase 0 至 Phase 4 的路径推进，先冻结边界与模型，再落平台骨架，再接入首个场景，最后逐步扩展到更多审批类和经营类场景。

#### Scenario: 规划平台实施顺序

- **GIVEN** 团队准备启动 OpenFang 集成建设
- **WHEN** 制定实施计划
- **THEN** 必须先完成边界、模型和协议冻结
- **AND** 然后建设平台骨架
- **AND** 首个场景只能作为平台插件接入，而不能反向定义平台结构

## 指南：OpenFang 连接与测试流程

### 连接概览

- AgentX 通过 `framework/openfang/client/OpenfangRuntimeBridge.java` 及其 DTO（`framework/openfang/client/dto/`）调用已有的 OpenFang REST 接口：`POST /api/workflows/{id}/run`、`GET /api/tasks/{id}`、审批详情查询（或 `GET /api/approvals` 结合本地过滤）及 approve/reject 回调，完成任务创建、状态读取和审批回写。
- `AgentxWorkflowResolver` 把场景编码解析成目标 `openfang_workflow_id + 版本`，`AgentxAuthorizationService` 在任何桥接调用前先求交 Agent 能力、用户委托和场景策略；高风险动作还由 `AgentxToolGuardService` 参考 `AgentxToolDescriptor`、`AgentxToolPolicy` 和风险控制目录进行额外筛选。
- 审批桥由 `AgentxApprovalBridgeServiceImpl` 串联，它按照 `docs/deliverables/2026-03-12-openfang-interface-document.md` 描述的审批细节链路构建 `ApprovalRequest`、关联 `ApprovalBinding`、通过桥接写回决议，确保 OA 是审批真相的唯一源。
- 上下文组装、审计和交付预期都在交付文档（`docs/deliverables/…`）中记录，确保每一个集成点都有书面契约。

### 测试流程

1. 本地先跑治理全量命令（`mvn -pl yudao-module-agentx -am -Dtest=AgentxPhase1GovernanceTest,AgentxPhase1GuardrailsTest,AgentxModuleBlueprintTest,AgentxObjectModelCatalogTest,AgentxDeliveryPackageTest,AgentxAcceptanceBaselineTest,LeaveFirstScenarioTest -Dsurefire.failIfNoSpecifiedTests=false -DforkCount=0 test`），确保治理对象、交付包与验收基线在不依赖 OpenFang 接口的情况下都通过。
2. 部署或模拟一个 OpenFang 实例，调用文档中的接口确认 DTO 与实际 payload 匹配。
3. 请假场景端到端演练：
   - AgentX 通过 `POST /api/workflows/{workflowId}/run` 创建任务，拿到 `task_run_id` 并写入 `AgentxTaskProjectionDO`。
   - 持续轮询 `GET /api/tasks/{task_run_id}`，检查 `pendingApprovalIds`、阶段信息和结果摘要，同时确保 `AgentxContextSnapshot`/`AgentxAuditEventDO` 字段被填充。
   - 通过精确查询或 AgentX 索引获取审批详情，构建 `ApprovalRequest` 并通过 approve/reject 接口提交决议。
   - 校验 `ApprovalBinding`、审计与 `AgentxAuditService` 记录是否与文档中描述的一致。
4. 压力/异常测试：模拟授权失败、重复请求（幂等键）、审批回调乱序，验证 `AgentxToolGuardService`、`AgentxAuditService` 和 `AgentxApprovalWaitPolicy` 能正常处理。
5. 用 `docs/deliverables/2026-03-12-openfang-first-scenario-acceptance-checklist.md` 的验收点逐项打勾，确认平台达标后再推进下一个场景。
