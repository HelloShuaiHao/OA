# OpenFang Integration Platform Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在 OA 仓库中落下 OpenFang 集成平台的第一批可编译骨架，实现 AgentX 领域模型、场景到 OpenFang workflow 的映射解析、受控运行时桥接口与审批详情读取策略约束。

**Architecture:** 新增 `yudao-module-agentx` 作为集成面骨架模块，只承载 OA 治理约束到 OpenFang Runtime 之间的桥接职责，不复制 `yudao-module-ai` 的 workflow/tool/console 真相。首批实现聚焦领域对象、策略枚举、映射解析服务与 REST bridge 协议 DTO，不引入场景专属业务逻辑。

**Tech Stack:** Java 8, Spring Boot 2.7, Lombok, JUnit 5, Maven

### Task 1: 建立 AgentX 模块骨架

**Files:**
- Create: `yudao-module-agentx/pom.xml`
- Modify: `pom.xml`
- Modify: `yudao-server/pom.xml`

**Step 1: 创建模块 POM 与聚合依赖接线**

新增 `yudao-module-agentx` Maven 模块，依赖 `yudao-module-system`、`yudao-module-bpm`、`yudao-spring-boot-starter-web`、`yudao-spring-boot-starter-security`、`yudao-spring-boot-starter-protection` 与测试 starter。

**Step 2: 将模块挂入根项目**

在根 `pom.xml` 增加 `<module>yudao-module-agentx</module>`，并在 `yudao-server/pom.xml` 中引入该模块依赖。

**Step 3: 运行模块测试命令**

Run: `mvn -pl yudao-module-agentx test`

Expected: 模块能被 Maven 识别，即使测试暂未通过，也应进入 Surefire 阶段而不是聚合失败。

### Task 2: 先写失败测试，冻结首批平台规则

**Files:**
- Create: `yudao-module-agentx/src/test/java/cn/iocoder/yudao/module/agentx/service/workflow/AgentxWorkflowResolverTest.java`
- Create: `yudao-module-agentx/src/test/java/cn/iocoder/yudao/module/agentx/service/authorization/AgentxAuthorizationServiceTest.java`

**Step 1: 为场景映射解析写失败测试**

测试以下行为：
- 能从 `scenario_code -> openfang_workflow_id -> expected_workflow_version` 映射中解析 workflow
- 场景禁用时拒绝执行
- workflow 版本不匹配时拒绝执行

**Step 2: 为权限求交写失败测试**

测试以下行为：
- 实际权限 = Agent 能力 ∩ 用户委托范围 ∩ 场景策略
- 任一维度不允许的 capability 不得出现在结果中

**Step 3: 运行测试并确认失败原因正确**

Run: `mvn -pl yudao-module-agentx -Dtest=AgentxWorkflowResolverTest,AgentxAuthorizationServiceTest test`

Expected: FAIL，原因是相关类尚未实现，而不是测试配置错误。

### Task 3: 实现领域对象与策略枚举

**Files:**
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/dal/dataobject/package-info.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/enums/package-info.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/dal/dataobject/workflow/AgentxScenarioWorkflowMappingDO.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/dal/dataobject/task/AgentxTaskProjectionDO.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/dal/dataobject/approval/AgentxApprovalBindingDO.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/dal/dataobject/audit/AgentxAuditEventDO.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/enums/AgentxApprovalDetailFetchModeEnum.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/enums/AgentxTaskProjectionStatusEnum.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/enums/AgentxRiskLevelEnum.java`

**Step 1: 建模跨系统投影对象**

补齐租户边界、外部 ID、版本投影、幂等键、风险等级、审批绑定与审计摘要等字段。

**Step 2: 固化审批详情读取策略**

用枚举限制为 `EXACT_QUERY` 或 `LOCAL_INDEX`，避免 Phase 1 默认退化为全量扫描 pending approvals。

### Task 4: 实现场景解析与授权求交服务

**Files:**
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/service/workflow/AgentxWorkflowResolver.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/service/workflow/AgentxWorkflowResolution.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/service/authorization/AgentxAuthorizationService.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/service/authorization/AgentxAuthorizationSnapshot.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/service/authorization/AgentxCapability.java`

**Step 1: 实现场景映射解析**

按场景编码、启用状态、期望版本与实际版本校验输出 `AgentxWorkflowResolution`。

**Step 2: 实现能力求交**

输入 Agent 能力、用户委托能力、场景策略能力，输出最终 capability 集合。

**Step 3: 重新运行测试**

Run: `mvn -pl yudao-module-agentx -Dtest=AgentxWorkflowResolverTest,AgentxAuthorizationServiceTest test`

Expected: PASS

### Task 5: 实现 OpenFang Runtime Bridge 协议骨架

**Files:**
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/framework/openfang/config/AgentxOpenfangProperties.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/framework/openfang/client/OpenfangRuntimeBridge.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/framework/openfang/dto/OpenfangWorkflowRunReqDTO.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/framework/openfang/dto/OpenfangWorkflowRunRespDTO.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/framework/openfang/dto/OpenfangTaskRespDTO.java`
- Create: `yudao-module-agentx/src/main/java/cn/iocoder/yudao/module/agentx/framework/openfang/dto/OpenfangApprovalDetailRespDTO.java`

**Step 1: 只定义受控接口，不夹带业务规则**

接口覆盖 `workflow run`、`task query`、审批 approve/reject 所需的请求响应骨架，并显式表达 `pendingApprovalIds`、结果摘要与审计摘要的投影边界。

**Step 2: 保持 OA 与 OpenFang 真相分离**

DTO 不暴露 prompt loop、memory 结构或推理细节。

### Task 6: 同步 OpenSpec 任务进度并做基础验证

**Files:**
- Modify: `openspec/changes/2026-03-12-introduce-openfang-integration-platform/tasks.md`

**Step 1: 勾选本轮已完成的骨架任务**

只勾选确实已落地的条目，例如模块边界、workflow 映射落点、审批读取策略边界、首阶段部署边界的对应实现。

**Step 2: 运行模块测试**

Run: `mvn -pl yudao-module-agentx test`

Expected: PASS
