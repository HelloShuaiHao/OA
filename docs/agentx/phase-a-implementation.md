# AgentX Phase A 实施说明

## 目标

Phase A 聚焦“降低复杂度”，让业务管理员在低门槛下完成数字员工配置与发布。

## 本次交付

1. Agent 管理中心（5 步向导、流程选择、规则配置）
2. 数字员工组织集成（`system_users.user_type/agent_id`）
3. 流程选择引擎（规则模式 + 自动模式降级）
4. 模板库后端化（模板表 + 模板列表 API）
5. 发布自动化（同步数字员工用户、配置版本快照、OpenFang 同步状态回写）
6. Flowable 桥接（事件监听器 + AI 选择/工具调用 Delegate）

## 新增 API

1. `GET /agentx/template/list`：模板列表
2. `GET /agentx/template/get?type=xxx`：模板详情
3. `POST /agentx/process/select`：按上下文选择流程
4. `POST /agentx/process/start`：选择并启动流程
5. `GET /system/dept/{id}/members?userType=all|human|agent`：部门成员查询（含数字员工）
6. `GET /system/user/agents`：数字员工用户分页

## 数据迁移

1. `V20260322_01__agentx_agent_management.sql`
2. `V20260322_02__agentx_phase_a_core.sql`

## 关键说明

1. Agent 激活与更新后会自动维护 `system_users` 映射记录。
2. 每次激活/配置变更会落一条 `agentx_agent_config_version` 快照，便于审计和回滚评估。
3. OpenFang 同步采用“最佳努力”，结果写入 `agentx_agent.last_sync_*` 字段。
