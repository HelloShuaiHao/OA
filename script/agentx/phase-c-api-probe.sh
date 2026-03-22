#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:48080}"
TARGET_PATH="${TARGET_PATH:-/admin-api/agentx/task/page?pageNo=1&pageSize=10}"
REQUESTS="${REQUESTS:-200}"
INTERVAL_MS="${INTERVAL_MS:-50}"
TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-3}"

if ! command -v curl >/dev/null 2>&1; then
  echo "curl 未安装，无法执行探测"
  exit 2
fi

if ! command -v awk >/dev/null 2>&1; then
  echo "awk 未安装，无法计算统计结果"
  exit 2
fi

url="${BASE_URL}${TARGET_PATH}"
body_file="/tmp/agentx_probe_body.$$"
trap 'rm -f "$body_file"' EXIT
success=0
failed=0
total_ms=0
max_ms=0

for ((i = 1; i <= REQUESTS; i++)); do
  result=$(curl -sS -o "$body_file" -m "${TIMEOUT_SECONDS}" -w "%{http_code} %{time_total}" "$url" || echo "000 0")
  code=$(echo "$result" | awk '{print $1}')
  seconds=$(echo "$result" | awk '{print $2}')
  ms=$(awk -v s="$seconds" 'BEGIN { printf "%.3f", s * 1000 }')

  total_ms=$(awk -v t="$total_ms" -v m="$ms" 'BEGIN { printf "%.3f", t + m }')
  max_ms=$(awk -v a="$max_ms" -v b="$ms" 'BEGIN { if (b > a) printf "%.3f", b; else printf "%.3f", a }')

  if [[ "$code" -ge 200 && "$code" -lt 500 ]]; then
    success=$((success + 1))
  else
    failed=$((failed + 1))
  fi

  if [[ "$INTERVAL_MS" -gt 0 ]]; then
    sleep "$(awk -v ms="$INTERVAL_MS" 'BEGIN { printf "%.3f", ms / 1000 }')"
  fi
done

availability=$(awk -v ok="$success" -v total="$REQUESTS" 'BEGIN { if (total == 0) print "0"; else printf "%.6f", ok / total }')
availability_pct=$(awk -v r="$availability" 'BEGIN { printf "%.2f", r * 100 }')
avg_ms=$(awk -v t="$total_ms" -v total="$REQUESTS" 'BEGIN { if (total == 0) print "0"; else printf "%.3f", t / total }')

cat <<REPORT
=== AgentX API Probe Report ===
URL: $url
Requests: $REQUESTS
Success(2xx~4xx): $success
Failed(5xx/timeout/network): $failed
Availability: ${availability_pct}%
Avg Latency: ${avg_ms} ms
Max Latency: ${max_ms} ms
REPORT

if awk -v r="$availability" 'BEGIN { exit !(r >= 0.99) }'; then
  echo "SLO CHECK: PASS (availability >= 99%)"
else
  echo "SLO CHECK: FAIL (availability < 99%)"
  exit 1
fi
