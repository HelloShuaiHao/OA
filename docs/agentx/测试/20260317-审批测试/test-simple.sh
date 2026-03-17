#!/bin/bash
# 简化测试：验证 OpenFang agent 能否接收消息

echo "=== OpenFang Agent 通信测试 ==="

AGENT_ID="05ea9016-b608-40d2-b6f0-7ff3e04497b8"

echo "步骤 1: 发送测试消息到 agent"
RESPONSE=$(curl -s -X POST "http://localhost:4201/api/agents/$AGENT_ID/message" \
  -H "Content-Type: application/json" \
  -d '{"message": "你好，请介绍一下你自己"}')

echo "Agent 响应: $RESPONSE"

TASK_ID=$(echo $RESPONSE | jq -r '.task_id')
echo "Task ID: $TASK_ID"

echo -e "\n步骤 2: 等待 agent 处理..."
sleep 5

echo -e "\n步骤 3: 查询任务结果"
curl -s "http://localhost:4201/api/agents/$AGENT_ID/tasks/$TASK_ID" | jq '.'

echo -e "\n=== 测试完成 ==="
