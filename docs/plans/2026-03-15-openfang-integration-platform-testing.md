# OpenFang Integration Platform Testing Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为 `openspec/changes/2026-03-12-introduce-openfang-integration-platform` 建立一条可重复执行的测试路径，先验证治理基线，再验证交付物，再验证 OpenFang 联调与请假场景端到端链路。

**Architecture:** 测试分三层推进。第一层只验证仓库内的治理约束与场景基线，不依赖外部 OpenFang；第二层补齐并核对交付文档；第三层针对 OpenFang REST 接口做契约联调和审批链路验收。这样可以先确认规范没有漂移，再确认文档和接口没有脱节。

**Tech Stack:** Maven, JUnit 5, Java 8, OpenSpec, OpenFang REST API

### Task 1: 先确认治理基线还活着

**Files:**
- Test: `yudao-module-agentx/src/test/java/cn/iocoder/yudao/module/agentx/service/governance/AgentxPhase1GovernanceTest.java`
- Test: `yudao-module-agentx/src/test/java/cn/iocoder/yudao/module/agentx/service/governance/AgentxPhase1GuardrailsTest.java`
- Test: `yudao-module-agentx/src/test/java/cn/iocoder/yudao/module/agentx/service/governance/AgentxModuleBlueprintTest.java`
- Test: `yudao-module-agentx/src/test/java/cn/iocoder/yudao/module/agentx/service/governance/AgentxObjectModelCatalogTest.java`
- Test: `yudao-module-agentx/src/test/java/cn/iocoder/yudao/module/agentx/service/governance/AgentxDeliveryPackageTest.java`
- Test: `yudao-module-agentx/src/test/java/cn/iocoder/yudao/module/agentx/service/governance/AgentxAcceptanceBaselineTest.java`
- Test: `yudao-module-agentx/src/test/java/cn/iocoder/yudao/module/agentx/scenario/leave/LeaveFirstScenarioTest.java`

**Step 1: 运行治理基线测试**

Run: `mvn -pl yudao-module-agentx -am -Dtest=AgentxPhase1GovernanceTest,AgentxPhase1GuardrailsTest,AgentxModuleBlueprintTest,AgentxObjectModelCatalogTest,AgentxDeliveryPackageTest,AgentxAcceptanceBaselineTest,LeaveFirstScenarioTest -Dsurefire.failIfNoSpecifiedTests=false -DforkCount=0 test`

Expected: `BUILD SUCCESS`，7 个测试全部通过。

**Step 2: 记录这层测试实际覆盖什么**

- `AgentxPhase1GovernanceTest` 验证职责边界、真相归属、Phase 1 只走 REST。
- `AgentxPhase1GuardrailsTest` 验证禁止事项和首阶段技术边界。
- `AgentxModuleBlueprintTest` 验证模块职责和依赖边界。
- `AgentxObjectModelCatalogTest` 验证对象模型与跨系统映射。
- `AgentxDeliveryPackageTest` 验证预期交付物清单。
- `AgentxAcceptanceBaselineTest` 验证平台验收门槛声明。
- `LeaveFirstScenarioTest` 验证“请假场景只是首个验证插件，不反向扩边界”。

### Task 2: 补齐并核对交付文档

**Files:**
- Modify or Create: `docs/deliverables/2026-03-12-openfang-boundary-document.md`
- Modify or Create: `docs/deliverables/2026-03-12-openfang-object-model-document.md`
- Modify or Create: `docs/deliverables/2026-03-12-openfang-interface-document.md`
- Modify or Create: `docs/deliverables/2026-03-12-openfang-implementation-roadmap.md`
- Modify or Create: `docs/deliverables/2026-03-12-openfang-first-scenario-acceptance-checklist.md`

**Step 1: 创建或归档到规范要求的交付路径**

Expected: 上述 5 个文档都真实存在，而不是只在测试里以字符串方式声明。

**Step 2: 按规范逐项核对内容**

- `boundary-document` 要覆盖 OA、OpenFang、AgentX 的职责边界和禁止事项。
- `object-model-document` 要覆盖对象、字段、生命周期、ID 映射。
- `interface-document` 要覆盖 workflow run、task query、审批详情读取、approve/reject。
- `implementation-roadmap` 要覆盖 Phase 0 到 Phase 4。
- `first-scenario-acceptance-checklist` 至少覆盖成功流、拒绝流、越权流、乱序回调流、重复请求流。

**Step 3: 如果内容实际还在 `docs/openfang 集成/` 下，决定是迁移还是建立索引**

Expected: 不再出现“规范写的是 `docs/deliverables/...`，仓库里实际在别处”的分裂状态。

### Task 3: 做 OpenFang REST 契约联调

**Files:**
- Reference: `openspec/changes/2026-03-12-introduce-openfang-integration-platform/specs/openfang-integration-platform/spec.md`
- Reference: `docs/deliverables/2026-03-12-openfang-interface-document.md`
- Reference: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/framework/openfang/client/OpenfangRuntimeBridge.java`
- Reference: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/framework/openfang/client/dto/`

**Step 1: 启动或模拟一个 OpenFang 实例**

Expected: 可以访问至少这些接口：
- `POST /api/workflows/{id}/run`
- `GET /api/tasks/{id}`
- 审批详情精确查询接口，或 `GET /api/approvals` 加本地索引方案
- 审批 approve/reject 接口

**Step 2: 手工或脚本验证 DTO 契约**

重点检查：
- `task_run_id` 能否被稳定返回
- `pendingApprovalIds` 命名和结构是否匹配
- 结果摘要、失败摘要、阶段信息是否足够支持 OA 投影
- 响应中没有把 OpenFang 内部推理细节暴露给 OA

**Step 3: 确认审批详情获取路径被钉死**

Expected: 只能是以下两种之一：
- 精确审批详情查询接口
- AgentX 本地待审批索引

Expected: 不能把“扫描全量 pending approvals 后再模糊匹配”当成默认正路。

### Task 4: 用请假场景跑端到端链路

**Files:**
- Reference: `yudao-module-agentx/src/test/java/cn/iocoder/yudao/module/agentx/scenario/leave/LeaveFirstScenarioTest.java`
- Reference: `docs/deliverables/2026-03-12-openfang-first-scenario-acceptance-checklist.md`

**Step 1: 发起请假场景任务**

Run: 通过 AgentX 按 `scenario_code -> workflow_id -> expected_version` 解析后调用 `POST /api/workflows/{id}/run`

Expected: 拿到 `task_run_id`，并在 OA 侧形成任务投影。

**Step 2: 轮询任务状态**

Run: `GET /api/tasks/{task_run_id}`

Expected: 能读到状态投影、`pendingApprovalIds`、阶段信息、结果摘要。

**Step 3: 建立审批绑定并回推决议**

Run: 基于精确审批详情或本地索引生成 `ApprovalRequest`，在 OA BPM 建单，再通过 approve/reject 回写 OpenFang。

Expected: `ApprovalBinding`、审批实例、`task_run_id`、`approval_id` 绑定一致。

**Step 4: 验证结果投影和审计**

Expected:
- OA 保留审批决议真相
- OpenFang 保留等待审批和恢复语义
- 关键动作存在审计记录

### Task 5: 补一轮异常与幂等验收

**Files:**
- Reference: `docs/deliverables/2026-03-12-openfang-first-scenario-acceptance-checklist.md`

**Step 1: 模拟越权调用**

Expected: 被授权层或 Tool Guard 拦截，并留下审计事件。

**Step 2: 模拟重复请求**

Expected: 幂等键生效，不会重复建任务、重复建单或重复回写审批。

**Step 3: 模拟审批回调乱序或重复**

Expected: 最终状态稳定，`ApprovalBinding` 和任务投影不发生错误覆盖。

**Step 4: 用验收清单逐项打勾**

Expected: 至少完成成功流、拒绝流、越权流、乱序回调流、重复请求流 5 类检查。
