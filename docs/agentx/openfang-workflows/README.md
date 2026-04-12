# OpenFang Workflow（AgentX）

## 文件
- `leave-approval-assistant.yaml`：Phase 1 请假审批助手 workflow 定义。
- `leave-approval-assistant.openfang.json`：适配 OpenFang v0.3.3 API 的 workflow 定义。

## 健康检查
```bash
curl "http://127.0.0.1:4201/api/health"
```

## 配置工具能力（9.3 / 9.4）
```bash
bash script/openfang/configure-leave-tools.sh
```

## 注册 workflow（9.2）
```bash
bash script/openfang/register-leave-workflow.sh
```

## Smoke 测试（9.5）
```bash
bash script/openfang/smoke-test-leave-workflow.sh
```

> 说明：
> - 当 OpenFang 未启用鉴权时，可不设置 `OPENFANG_API_KEY`；
> - 启用鉴权时需设置 `OPENFANG_API_KEY`；
> - 默认 endpoint 为 `http://127.0.0.1:4201`，可通过 `OPENFANG_ENDPOINT` 覆盖。
> - `smoke-test-leave-workflow.sh` 默认 `STRICT_TOOL_CHECK=true`，若 `bpm_query_tasks/bpm_approve` 不在 `/api/tools` 中会以退出码 `2` 失败。
>
> 故障排查：
> - 本仓库 agent 工作流的 GitHub PR 检测必须指向 `HelloShuaiHao/OA`（远端 `OA`），不要默认使用 `origin` 对应的 `YunaiV/ruoyi-vue-pro`，否则 `.clawdbot/check-agents.sh` 可能查不到已经存在的 PR。
> - 可先执行 `gh repo set-default HelloShuaiHao/OA`，或在调用检查脚本前临时设置 `GH_REPO=HelloShuaiHao/OA`。
