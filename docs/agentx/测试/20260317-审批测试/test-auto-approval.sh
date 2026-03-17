#!/bin/bash
# 自动化审批测试脚本

echo "=== OpenFang 自动化审批测试 ==="

# 1. 获取 OA 登录 token
echo "步骤 1: 获取审批人 token"
TOKEN=$(curl -s -X POST http://localhost:48080/admin-api/system/auth/login -H "Content-Type: application/json" -H "tenant-id: 1" -d '{"username":"e1221805","password":"123456"}' | jq -r '.data.accessToken')

echo "Token: $TOKEN"

# 2. 查询待办任务
echo -e "\n步骤 2: 查询待办任务"
curl -s "http://localhost:48080/admin-api/bpm/task/todo-page?pageNo=1&pageSize=10" -H "Authorization: Bearer $TOKEN" -H "tenant-id: 1" | jq '.data.list[] | {id: .id, name: .name, day: .processInstance.businessKey}'

# 3. 通过 OpenFang API 发送消息给 agent
echo -e "\n步骤 3: 让 agent 处理审批"
AGENT_ID="c6cc5a85-7a91-4623-9a66-8f34334b8ad5"

# 发送消息：查询待办
curl -X POST "http://localhost:4201/api/agents/$AGENT_ID/message" -H "Content-Type: application/json" -d "{\"message\": \"查询我的待办任务，token: $TOKEN\"}"

sleep 3

# 发送消息：自动审批
curl -X POST "http://localhost:4201/api/agents/$AGENT_ID/message" -H "Content-Type: application/json" -d '{"message": "按照规则自动处理：2天以内通过，其他的告诉我详情"}'

# 4. 等待处理完成
echo -e "\n步骤 4: 等待 agent 处理..."
sleep 10

# 5. 验证结果
echo -e "\n步骤 5: 验证审批结果"
curl -s "http://localhost:48080/admin-api/bpm/task/done-page?pageNo=1&pageSize=10" -H "Authorization: Bearer $TOKEN" -H "tenant-id: 1" | jq '.data.list[] | {name: .name, result: .result}'

echo -e "\n=== 测试完成 ==="
