# AgentX Flowable 扩展节点使用指南（Phase C）

## 适用范围

本指南用于 BPMN 设计器中的 AgentX 扩展 ServiceTask 节点配置与运行排查。

## 节点类型

1. AI 决策节点（`AI_DECISION`）
2. Tool 调用节点（`TOOL_CALL`）
3. 数据查询节点（`DATA_QUERY`）
4. 外部 API 节点（`EXTERNAL_API`）

## 配置要点

### AI 决策节点

- `Prompt 模板` 必填，支持变量占位。
- `输入变量` 支持逗号分隔。
- `超时(秒)` 必须为正整数。

### Tool 调用节点

- `Tool 名称` 必填。
- `参数(JSON)` 需为合法 JSON。
- `重试次数` 需为非负整数。

### 数据查询节点

- `查询模式` 支持 `sql` / `api`。
- SQL 模式仅允许 `SELECT` 语句。
- `参数` 需为合法 JSON（可选）。

### 外部 API 节点

- `API URL` 必填。
- `HTTP 方法` 支持 GET/POST/PUT/DELETE。
- 启用认证时必须填写 Token。
- `重试次数` 为非负整数，`超时(秒)` 为正整数。

## 运行与排查

1. 节点配置保存后会写入 BPMN 扩展属性（`prefix:Properties`）。
2. 执行时按 `delegateExpression` 路由到后端 Delegate。
3. 失败时优先查看：
   - 节点扩展属性是否完整
   - 参数 JSON 是否合法
   - 外部依赖（数据库/API/OpenFang）是否可用
4. OpenFang 不稳定时，系统会触发熔断降级并记录告警日志。
