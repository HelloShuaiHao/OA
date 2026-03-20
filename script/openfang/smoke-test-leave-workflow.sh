#!/usr/bin/env bash
set -euo pipefail

OPENFANG_ENDPOINT="${OPENFANG_ENDPOINT:-http://127.0.0.1:4201}"
WORKFLOW_NAME="${WORKFLOW_NAME:-leave-approval-assistant}"
WORKFLOW_ID="${WORKFLOW_ID:-}"
USER_ID="${USER_ID:-100}"
SCENARIO_CODE="${SCENARIO_CODE:-oa.leave.approval}"
BUSINESS_KEY="${BUSINESS_KEY:-leave:smoke}"
STRICT_TOOL_CHECK="${STRICT_TOOL_CHECK:-true}"

if [[ -n "${OPENFANG_API_KEY:-}" ]]; then
  TOOLS_JSON="$(curl -fsSL "${OPENFANG_ENDPOINT}/api/tools" -H "Authorization: Bearer ${OPENFANG_API_KEY}")"
else
  TOOLS_JSON="$(curl -fsSL "${OPENFANG_ENDPOINT}/api/tools")"
fi

if ! TOOLS_JSON="${TOOLS_JSON}" python3 - <<'PY'
import json
import os
data = json.loads(os.environ["TOOLS_JSON"] or "{}")
names = {x.get("name") for x in data.get("tools", [])}
need = {"bpm_query_tasks", "bpm_approve"}
missing = sorted(need - names)
if missing:
    print(",".join(missing))
    raise SystemExit(1)
PY
then
  echo "required tools missing in OpenFang tool registry: bpm_query_tasks,bpm_approve"
  if [[ "${STRICT_TOOL_CHECK}" == "true" ]]; then
    exit 2
  fi
fi

if [[ -z "${WORKFLOW_ID}" ]]; then
  if [[ -n "${OPENFANG_API_KEY:-}" ]]; then
    WORKFLOW_LIST="$(curl -fsSL "${OPENFANG_ENDPOINT}/api/workflows" -H "Authorization: Bearer ${OPENFANG_API_KEY}")"
  else
    WORKFLOW_LIST="$(curl -fsSL "${OPENFANG_ENDPOINT}/api/workflows")"
  fi
  WORKFLOW_ID="$(WORKFLOW_LIST="${WORKFLOW_LIST}" WORKFLOW_NAME="${WORKFLOW_NAME}" python3 - <<'PY'
import json
import os
workflow_name = os.environ["WORKFLOW_NAME"]
items = json.loads(os.environ["WORKFLOW_LIST"] or "[]")
for item in items:
    if item.get("name") == workflow_name:
        print(item.get("id", ""))
        break
PY
)"
fi

if [[ -z "${WORKFLOW_ID}" ]]; then
  echo "workflow not found by name=${WORKFLOW_NAME}, please register first"
  exit 1
fi

echo "[1/2] trigger workflow run"
if [[ -n "${OPENFANG_API_KEY:-}" ]]; then
  RUN_RESP="$(curl -fsSL -X POST "${OPENFANG_ENDPOINT}/api/workflows/${WORKFLOW_ID}/run" \
    -H "Authorization: Bearer ${OPENFANG_API_KEY}" \
    -H "Content-Type: application/json" \
    -d "{
      \"input\": {
        \"user_id\": ${USER_ID},
        \"scenario_code\": \"${SCENARIO_CODE}\",
        \"business_key\": \"${BUSINESS_KEY}\"
      }
    }")"
else
  RUN_RESP="$(curl -fsSL -X POST "${OPENFANG_ENDPOINT}/api/workflows/${WORKFLOW_ID}/run" \
    -H "Content-Type: application/json" \
    -d "{
      \"input\": {
        \"user_id\": ${USER_ID},
        \"scenario_code\": \"${SCENARIO_CODE}\",
        \"business_key\": \"${BUSINESS_KEY}\"
      }
    }")"
fi
echo "${RUN_RESP}"

TASK_RUN_ID="$(echo "${RUN_RESP}" | sed -n 's/.*"task_run_id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
if [[ -z "${TASK_RUN_ID}" ]]; then
  echo "task_run_id not found in response"
  exit 1
fi

echo "[2/2] query task run: ${TASK_RUN_ID}"
if [[ -n "${OPENFANG_API_KEY:-}" ]]; then
  curl -fsSL "${OPENFANG_ENDPOINT}/api/tasks/${TASK_RUN_ID}" \
    -H "Authorization: Bearer ${OPENFANG_API_KEY}"
else
  curl -fsSL "${OPENFANG_ENDPOINT}/api/tasks/${TASK_RUN_ID}"
fi
echo
