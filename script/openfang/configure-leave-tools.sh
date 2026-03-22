#!/usr/bin/env bash
set -euo pipefail

OPENFANG_ENDPOINT="${OPENFANG_ENDPOINT:-http://127.0.0.1:4201}"
AGENT_NAME="${AGENT_NAME:-oa-leave-workflow-runner}"
MODEL_PROVIDER="${MODEL_PROVIDER:-openai}"
MODEL_NAME="${MODEL_NAME:-qwen3.5-plus}"

MANIFEST_FILE="$(mktemp)"
cat >"${MANIFEST_FILE}" <<EOF
name = "${AGENT_NAME}"
version = "0.1.0"
description = "AgentX leave approval workflow runner"
author = "agentx"
module = "builtin:chat"

[model]
provider = "${MODEL_PROVIDER}"
model = "${MODEL_NAME}"

[capabilities]
tools = ["bpm_query_tasks", "bpm_approve", "wait_for_approval", "web_fetch"]
memory_read = ["*"]
memory_write = ["self.*"]
EOF

REQ_FILE="$(mktemp)"
python3 - <<PY
import json
from pathlib import Path
manifest = Path("${MANIFEST_FILE}").read_text()
Path("${REQ_FILE}").write_text(json.dumps({"manifest_toml": manifest}))
PY

echo "[1/3] spawn agent with bpm tools"
if [[ -n "${OPENFANG_API_KEY:-}" ]]; then
  AGENTS_JSON="$(curl -fsSL "${OPENFANG_ENDPOINT}/api/agents" -H "Authorization: Bearer ${OPENFANG_API_KEY}")"
else
  AGENTS_JSON="$(curl -fsSL "${OPENFANG_ENDPOINT}/api/agents")"
fi

EXISTING_AGENT_ID="$(AGENTS_JSON="${AGENTS_JSON}" AGENT_NAME="${AGENT_NAME}" python3 - <<'PY'
import json
import os
items = json.loads(os.environ["AGENTS_JSON"] or "[]")
for item in items:
    if item.get("name") == os.environ["AGENT_NAME"]:
        print(item.get("id", ""))
        break
PY
)"

if [[ -n "${EXISTING_AGENT_ID}" ]]; then
  AGENT_ID="${EXISTING_AGENT_ID}"
  echo "agent already exists: ${AGENT_ID}"
else
  if [[ -n "${OPENFANG_API_KEY:-}" ]]; then
    SPAWN_RESP="$(
      curl -fsSL -X POST "${OPENFANG_ENDPOINT}/api/agents" \
        -H "Authorization: Bearer ${OPENFANG_API_KEY}" \
        -H "Content-Type: application/json" \
        --data-binary @"${REQ_FILE}"
    )"
  else
    SPAWN_RESP="$(
      curl -fsSL -X POST "${OPENFANG_ENDPOINT}/api/agents" \
        -H "Content-Type: application/json" \
        --data-binary @"${REQ_FILE}"
    )"
  fi
  echo "${SPAWN_RESP}"
  AGENT_ID="$(echo "${SPAWN_RESP}" | sed -n 's/.*"agent_id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
fi

if [[ -z "${AGENT_ID}" ]]; then
  echo "agent_id not found"
  exit 1
fi

echo "[2/3] set tool allowlist"
if [[ -n "${OPENFANG_API_KEY:-}" ]]; then
  curl -fsSL -X PUT "${OPENFANG_ENDPOINT}/api/agents/${AGENT_ID}/tools" \
    -H "Authorization: Bearer ${OPENFANG_API_KEY}" \
    -H "Content-Type: application/json" \
    -d '{"tool_allowlist":["bpm_query_tasks","bpm_approve","wait_for_approval","web_fetch"],"tool_blocklist":[]}'
else
  curl -fsSL -X PUT "${OPENFANG_ENDPOINT}/api/agents/${AGENT_ID}/tools" \
    -H "Content-Type: application/json" \
    -d '{"tool_allowlist":["bpm_query_tasks","bpm_approve","wait_for_approval","web_fetch"],"tool_blocklist":[]}'
fi
echo

echo "[3/3] verify configured tools"
if [[ -n "${OPENFANG_API_KEY:-}" ]]; then
  curl -fsSL "${OPENFANG_ENDPOINT}/api/agents/${AGENT_ID}" \
    -H "Authorization: Bearer ${OPENFANG_API_KEY}"
  echo
  curl -fsSL "${OPENFANG_ENDPOINT}/api/agents/${AGENT_ID}/tools" \
    -H "Authorization: Bearer ${OPENFANG_API_KEY}"
else
  curl -fsSL "${OPENFANG_ENDPOINT}/api/agents/${AGENT_ID}"
  echo
  curl -fsSL "${OPENFANG_ENDPOINT}/api/agents/${AGENT_ID}/tools"
fi
echo
