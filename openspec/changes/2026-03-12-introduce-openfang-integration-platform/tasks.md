## 1. 先冻结三个硬边界，不允许继续空谈架构

- [x] 1.1 产出一页职责边界表，明确 OA、OpenFang、AgentX 平台分别负责什么、不负责什么
- [x] 1.2 明确真相归属：组织和审批决议真相在 OA，TaskRun 真相在 OpenFang；审批阻塞、`pending_approval_ids`、恢复/补偿状态属于 OpenFang 运行时投影
- [x] 1.3 产出 `yudao-module-ai` 与 AgentX 能力分层表，逐项判定 workflow、tool registry、console、ToolContext、租户/用户上下文哪些复用、哪些新增
- [x] 1.4 明确 Phase 1 协议边界：基于现有 REST 接口落地，禁止把外部 webhook/outbox 当作前置条件
- [x] 1.5 明确 workflow definition 真相归属：Phase 1 由 OpenFang 保存可执行 workflow definition，OA / AgentX 只保存场景映射和版本投影
- [x] 1.6 列出禁止事项并写进规范：禁止 OpenFang 直连 OA 数据库，禁止场景模块自建权限体系，禁止双边维护互相争夺真相的审批状态机
- [x] 1.7 明确首阶段技术边界：平台主实现使用 Java，Rust 不进入第一阶段核心链路
- [x] 1.8 给出首阶段部署边界：OA 侧优先在现有模块内增量落地集成能力，OpenFang 保持独立运行，通过已存在的 REST 接口交互

## 2. 先定义对象模型，再允许任何人写代码

- [x] 2.1 定义身份模型：`AgentPrincipal`、`HumanPrincipal`、`DelegationGrant`、`ExecutionIdentity`
- [x] 2.2 定义能力模型：`Capability`、`ToolCapability`、`DataAccessPolicy`、`ScenarioPolicy`
- [x] 2.3 定义任务模型：`ScenarioTaskRequest`、`TaskBinding`、`TaskProjection`、`TaskCheckpoint`
- [x] 2.4 定义审批模型：`ApprovalBinding`、`ApprovalRequest`、`ApprovalDecision`
- [x] 2.5 定义审计模型：`AuditEvent`、`ToolAuditEvent`、`BusinessWriteAuditEvent`、`RiskControlEvent`
- [x] 2.6 为上述对象补齐字段级说明：主键、外部标识、状态枚举、幂等键、创建者、时间戳、租户边界
- [x] 2.7 为所有跨系统对象补齐映射关系：OA 内部 ID、OpenFang 外部 ID、审批实例 ID、业务单据 ID
- [x] 2.8 单独区分“审批决议对象”和“审批阻塞投影对象”，避免把 OA 决议真相和 OpenFang gate 状态混成一个对象
- [x] 2.9 输出对象关系图，明确哪些对象可持久化，哪些对象只存在于运行态

## 3. 模块拆分必须先定清楚，否则后面一定长成一坨

- [x] 3.1 先产出“逻辑职责模块图”，不要先急着建一堆物理 Maven module
- [x] 3.2 定义 `agentx-domain`：只放领域对象、枚举、协议 DTO、领域服务接口，禁止放 Controller 和基础设施实现
- [x] 3.3 定义 `agentx-identity`：负责 Agent 身份、委托关系、执行主体生成，禁止把授权判断塞进这里
- [x] 3.4 定义 `agentx-authorization`：负责能力求交、Tool 权限、数据域控制、场景策略评估
- [x] 3.5 明确 `tool registry` 是否直接复用 `yudao-module-ai` 的 ToolFunction、ToolContext 和安全上下文注入机制；只有复用不了的部分才新增集成包装层
- [x] 3.6 明确 workflow 配置落点：定义场景配置表如何保存 `scenario_code -> openfang_workflow_id -> expected_workflow_version` 映射
- [x] 3.7 定义 `agentx-context`：负责跨 BPM、System、ERP、CRM 的上下文组装，统一产出 `BusinessContextBundle`
- [x] 3.8 定义 `agentx-runtime-bridge`：负责调用 OpenFang 创建任务、查询状态、执行审批结果回推，不允许夹带业务规则
- [x] 3.9 定义 `agentx-approval-bridge`：负责高风险动作审批映射、审批绑定关系、审批结果回推
- [x] 3.10 明确 workflow 管理台和运营台是否直接落在现有 AI 管理能力上；禁止同时建设第二套 workflow console
- [x] 3.11 定义 `agentx-audit`：负责标准化审计事件、Tool 留痕、Prompt/Decision 轨迹归档、风险事件记录
- [x] 3.12 定义 `agentx-scenario`：只允许放场景装配代码，不允许下沉公共中间件逻辑
- [x] 3.13 画出模块依赖图，明确允许依赖和禁止依赖，防止 `scenario -> bpm/system/erp` 直接乱连

## 4. 把 OA 与 OpenFang 的协议钉死，别把系统边界做成空气

- [x] 4.1 以 OpenFang 当前已公开接口为基线，列出 Phase 1 真正使用的接口：`POST /api/workflows/{id}/run`、`GET /api/tasks/{id}`、`POST /api/tasks/{id}/resume`、`POST /api/tasks/{id}/compensate`、`GET /api/approvals`、审批 approve/reject 接口
- [x] 4.2 定义任务创建协议：请求头、执行身份、场景标识、上下文摘要、能力边界、幂等键，并约定如何映射到 `workflow run` 请求
- [x] 4.3 明确定义 workflow 选择协议：OA 不直接拼 `workflow_id`，而是先按场景配置表解析出 `openfang_workflow_id` 与期望版本，再发起 `workflow run`
- [x] 4.4 定义 workflow 版本校验规则：OpenFang workflow definition 为真相，OA / AgentX 只校验映射版本是否匹配，不维护第二套可执行 definition
- [x] 4.5 定义任务查询协议：外部任务 ID、状态投影、阶段信息、最终结果摘要、失败原因摘要，明确以 `GET /api/tasks/{id}` 为真相读取入口
- [x] 4.6 删除“Phase 1 默认外部事件回流协议”表述，改为“Phase 1 采用轮询读取任务投影”；如需 webhook/outbox，对外集成能力另开 change
- [x] 4.7 将审批详情读取方案钉死为二选一：新增按 `approval_id` / `task_run_id` 精确查询接口，或在 AgentX 本地建立待审批缓存/索引；禁止把扫描全量 `GET /api/approvals` 作为默认正路
- [x] 4.8 若选择新增 OpenFang 查询接口，定义其查询键、返回字段、租户隔离、权限控制和分页/过滤契约
- [x] 4.9 若选择 AgentX 本地索引，定义索引构建、失效、补偿、租户隔离和一致性修复策略
- [x] 4.10 定义审批桥接协议：审批申请载荷、风险级别、动作摘要、审批实例绑定、结果通知，并明确 OpenFang 审批结果写回只走现有 approve/reject 接口
- [x] 4.11 明确 `POST /api/approvals` 不属于 OA 常规审批桥链路，只用于外部系统手动注入 OpenFang gate 的例外场景
- [x] 4.12 定义审计上报协议：谁发起、谁代理、调用了什么 Tool、读取了什么数据域、写入了什么业务动作
- [x] 4.13 明确 OA 可见字段与不可见字段，禁止把 OpenFang 内部 prompt loop、memory 结构和推理细节泄漏给 OA
- [x] 4.14 为所有跨系统写操作定义幂等与重试策略，避免网络抖动造成重复审批、重复写单、重复回调

## 5. 先把身份和授权做对，不然这个平台天生不安全

- [x] 5.1 定义 Agent 不是 OA 普通用户的建模方式，避免直接复用人类账号执行系统动作
- [x] 5.2 定义委托执行模型，明确谁授权、授权范围、授权时效、撤销机制、审计留痕
- [x] 5.3 定义权限计算公式：`实际权限 = Agent 能力 ∩ 用户委托范围 ∩ 场景策略`
- [x] 5.4 定义 Tool 权限与菜单权限的边界，禁止把菜单可见性错误映射成执行能力
- [x] 5.5 定义数据域控制策略，明确组织范围、业务单据范围、租户范围和字段脱敏规则
- [x] 5.6 定义高风险能力清单，例如提交审批、修改单据、创建业务记录、触发外部通知
- [x] 5.7 为授权失败、越权调用、委托过期、场景禁用补齐标准错误码与审计事件

## 6. Tool Registry 不是工具列表，它是安全边界

- [x] 6.1 定义 `ToolDescriptor`、`ToolSchema`、`ToolPolicy`、`ToolAdapter` 的元数据结构
- [x] 6.2 规定 Tool 只能面向受控服务能力，禁止直接暴露数据库表和内部 Mapper
- [x] 6.3 为每个 Tool 定义输入输出 Schema、权限要求、风险等级、审计标签、审批策略
- [x] 6.4 定义 Tool 注册流程：注册、审核、启用、禁用、版本变更、废弃
- [x] 6.5 定义 Tool 调用前校验链：身份校验、授权求交、参数校验、风险评估、审批判断
- [x] 6.6 定义 Tool 调用后留痕：请求摘要、结果摘要、耗时、错误码、业务影响范围

## 7. 上下文组装要平台化，不能每个场景自己拼接口

- [x] 7.1 定义 `ContextProvider`、`ContextSnapshot`、`BusinessContextBundle` 的标准接口
- [x] 7.2 约定上下文来源：BPM、System、ERP、CRM、知识库、用户档案、组织信息
- [x] 7.3 明确上下文分层：必需上下文、可选上下文、敏感上下文、摘要上下文
- [x] 7.4 为上下文组装增加权限过滤和数据脱敏，避免把超范围数据喂给 Runtime
- [x] 7.5 定义上下文缓存、快照和失效策略，避免长任务执行过程中上下文漂移不可追踪
- [x] 7.6 定义上下文审计要求，记录上下文来源、装配时间、装配人、装配规则版本

## 8. 审批桥必须是单轨，不允许两头各玩一套

- [x] 8.1 定义哪些动作必须进入审批，给出风险分级和触发条件
- [x] 8.2 定义 `ApprovalRequest` 到 OA BPM 的映射规则，包括标题、摘要、审批人来源、业务关联
- [x] 8.3 定义 `ApprovalBinding` 持久化规则，绑定 OA 审批实例、OpenFang `task_run_id`、OpenFang `approval_id`、业务动作和状态投影
- [x] 8.4 明确定义 OA 建单数据来源：标题、原因、风险等级、动作摘要等来自“精确审批详情读取能力”返回的 `ApprovalRequest` 明细，不从 `GET /api/tasks/{id}` 直接取；该能力可由新增 OpenFang 查询接口或 AgentX 本地索引实现
- [x] 8.5 明确审批状态双层语义：OA 保存审批决议真相，OpenFang 保存等待审批/已解除阻塞等运行时投影
- [x] 8.6 定义审批结果回流处理：通过、拒绝、撤回、超时、取消、异常补偿
- [x] 8.7 定义 Runtime 在等待审批时的行为：阻塞、超时、恢复、终止、重试，并保持与 OpenFang 已有 gate 语义兼容
- [x] 8.8 定义审批重复回调和乱序回调处理策略，保证状态转换幂等

## 9. 审计必须从第一天就接上，不要等出事了再补

- [x] 9.1 定义统一审计事件模型，覆盖任务生命周期、Tool 调用、审批动作、业务写入、权限失败
- [x] 9.2 定义最小审计字段：发起人、代理人、场景、任务 ID、Tool 名称、数据域、风险等级、结果
- [x] 9.3 定义 PromptTrace 和 DecisionTrace 的归档边界，只保留必要摘要，避免泄漏不该暴露的信息
- [x] 9.4 定义审计检索维度：按用户、Agent、业务单据、审批实例、任务状态、风险等级查询
- [x] 9.5 定义高风险事件告警规则，例如越权尝试、绕过审批、重复写入、异常重试风暴

## 10. 第一个场景只能当插件，不准反向绑架平台

- [x] 10.1 选定“请假审批助理”作为首个落地场景，只验证平台链路，不扩写平台边界
- [x] 10.2 为请假场景定义最薄装配：`LeaveScenarioDefinition`、`LeaveContextProvider`、`LeaveToolSet`、`LeaveApprovalPolicy`
- [x] 10.3 明确请假场景复用的平台能力，不允许在场景模块内复制身份、审批、审计逻辑，也不允许新建第二套 workflow/tool registry
- [x] 10.4 用请假场景走通端到端链路：OA 按场景映射解析 `openfang_workflow_id`、OpenFang 返回 `task_run_id`、OA 轮询读取 `GET /api/tasks/{id}`、再通过“精确审批详情查询”或“AgentX 本地待审批索引”补齐 BPM 建单信息、审批决议回推、结果投影、审计留痕
- [x] 10.5 验证请假场景在 `yudao-module-ai` 已有能力与新增集成层之间没有重复建设
- [x] 10.6 用复盘结果修正平台抽象，再考虑扩展报销、出差、采购等同类场景

## 11. 交付物必须具体，不接受“后续补充”

- [x] 11.1 交付边界文档：职责边界、禁止事项、模块依赖、协议边界
- [x] 11.2 交付对象模型文档：类图、字段定义、状态机、ID 映射关系
- [x] 11.3 交付接口文档：任务创建、状态查询、审批桥接、审计上报，以及 Phase 1 明确不做的 webhook/outbox 外部契约
- [x] 11.4 交付实施顺序文档：Phase 0 到 Phase 4 的里程碑、前置依赖、验收标准
- [x] 11.5 交付首场景验收清单：至少覆盖成功流、审批拒绝流、越权流、回调乱序流、重复请求流

## 12. 验收标准，不达标就不要宣布平台成立

- [x] 12.1 新增一个审批类场景时，不需要重写身份、授权、审批桥、上下文组装、审计基础设施
- [x] 12.2 OpenFang 无法直接访问 OA 数据库，也无法绕过审批执行高风险写操作
- [x] 12.3 OA 中只能看到任务投影、审批绑定和审计摘要，不能反向侵入 OpenFang 内部状态机实现
- [x] 12.4 审批决议真相只在 OA，OpenFang 仍保留等待审批与恢复语义所需的运行时投影
- [x] 12.5 请假审批场景可以完整跑通，并且所有关键动作都有审计记录和绑定关系
- [x] 12.6 平台抽象能够直接支撑下一个同类场景接入，而不是再次改写架构
