# AgentX Phase B 实施说明（身份与渠道）

## 1. 渠道配置

- 后端接口：`/admin-api/agentx/channel/config/*`
- 支持渠道类型：`telegram`、`wecom`、`dingtalk`
- 保存时 Bot 凭据加密落库（`bot_token_encrypted`）
- 更新渠道时，Bot Token 可留空，系统保留历史密文
- 测试连接支持两种方式：
  - 传入明文 `botToken`
  - 传入 `channelId`，服务端使用已保存密文解密后测试

### 凭据格式约定

- `telegram`: 直接填写 bot token
- `wecom`: 使用 `corpId:corpSecret`
- `dingtalk`: 使用 `appKey:appSecret`

## 2. 身份绑定

- 绑定链接生成接口：`POST /admin-api/agentx/channel/bind/generate`
- 绑定确认接口：`GET /admin-api/agentx/channel/bind/confirm?token=...`
- 前端绑定页：`/agentx/bind?token=...`
- Token 默认 10 分钟过期，可通过配置覆盖
- 绑定管理：
  - 个人查看：`GET /admin-api/agentx/channel/binding/my`
  - 个人解绑：`DELETE /admin-api/agentx/channel/binding/unbind?id=...`
  - 管理分页：`GET /admin-api/agentx/channel/binding/page`
  - 管理解绑：`DELETE /admin-api/agentx/channel/binding/admin-unbind?id=...`

## 3. 角色解析

- 接口：`RoleResolver#resolveRole(String roleType, Map<String, Object> context)`
- 缓存：Redis，默认 5 分钟
- 默认审批人改为配置项，不再硬编码

## 4. Prometheus 指标

当前已暴露：

- `agentx_agent_total`
- `agentx_task_total`
- `agentx_task_success_rate`
- `agentx_approval_total`
- `agentx_approval_pass_rate`
- `agentx_api_duration_seconds`（Timer）
- `agentx_openfang_call_total`
- `agentx_openfang_call_success_rate`

Grafana 模板：

- `docs/agentx/grafana-agentx-phase-b-dashboard.json`

## 5. 配置项

```yaml
yudao:
  agentx:
    bind:
      secret: agentx-bind-secret
      base-url: http://localhost:48080
      expire-minutes: 10
    role:
      default-approver-id: 1
      cache-minutes: 5
```
