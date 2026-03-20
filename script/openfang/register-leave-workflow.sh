#!/usr/bin/env bash
set -euo pipefail

OPENFANG_ENDPOINT="${OPENFANG_ENDPOINT:-http://127.0.0.1:4201}"
WORKFLOW_FILE="${WORKFLOW_FILE:-docs/agentx/openfang-workflows/leave-approval-assistant.openfang.json}"

if [[ -n "${OPENFANG_API_KEY:-}" ]]; then
  RESP="$(
    curl -fsSL -X POST "${OPENFANG_ENDPOINT}/api/workflows" \
      -H "Authorization: Bearer ${OPENFANG_API_KEY}" \
      -H "Content-Type: application/json" \
      --data-binary @"${WORKFLOW_FILE}"
  )"
else
  RESP="$(
    curl -fsSL -X POST "${OPENFANG_ENDPOINT}/api/workflows" \
      -H "Content-Type: application/json" \
      --data-binary @"${WORKFLOW_FILE}"
  )"
fi

WORKFLOW_ID="$(echo "${RESP}" | sed -n 's/.*"workflow_id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
if [[ -z "${WORKFLOW_ID}" ]]; then
  echo "workflow_id not found in response: ${RESP}"
  exit 1
fi

echo "Registered workflow from ${WORKFLOW_FILE}"
echo "workflow_id=${WORKFLOW_ID}"
