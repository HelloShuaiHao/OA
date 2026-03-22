#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:48080}"
PROM_URL="${PROM_URL:-}"
DB_NAME="${DB_NAME:-ruoyi-vue-pro}"
MYSQL_CMD="${MYSQL_CMD:-}"

if ! command -v curl >/dev/null 2>&1; then
  echo "curl 未安装，无法读取 Prometheus 指标"
  exit 2
fi

if ! command -v awk >/dev/null 2>&1; then
  echo "awk 未安装，无法计算统计结果"
  exit 2
fi

resolve_prometheus_url() {
  if [[ -n "$PROM_URL" ]]; then
    echo "$PROM_URL"
    return
  fi
  local candidates=(
    "${BASE_URL}/actuator/prometheus"
    "${BASE_URL}/admin-api/actuator/prometheus"
    "${BASE_URL}/infra/actuator/prometheus"
    "${BASE_URL}/admin-api/infra/actuator/prometheus"
  )
  local candidate
  for candidate in "${candidates[@]}"; do
    if curl -fsS -m 2 -o /dev/null "$candidate" >/dev/null 2>&1; then
      echo "$candidate"
      return
    fi
  done
  echo "未找到可用的 Prometheus 端点，请通过 PROM_URL 指定。" >&2
  return 1
}

PROM_URL_RESOLVED=$(resolve_prometheus_url)
prom_raw=$(curl -fsSL "$PROM_URL_RESOLVED")
api_availability=$(echo "$prom_raw" | awk '/^agentx_api_availability(\{| )/{v=$NF} END{if(v=="") v=1; print v}')
context_count=$(echo "$prom_raw" | awk '/^agentx_context_assembly_duration_seconds_count(\{| )/{v=$NF} END{if(v=="") v=0; print v}')
context_sum=$(echo "$prom_raw" | awk '/^agentx_context_assembly_duration_seconds_sum(\{| )/{v=$NF} END{if(v=="") v=0; print v}')
context_avg_ms=$(awk -v c="$context_count" -v s="$context_sum" 'BEGIN { if (c <= 0) print "0.000"; else printf "%.3f", (s / c) * 1000 }')

pool_active=$(echo "$prom_raw" | awk '/^druid_datasource_active_count(\{| )/{v=$NF} END{if(v=="") v=""; print v}')
pool_max=$(echo "$prom_raw" | awk '/^druid_datasource_max_active(\{| )/{v=$NF} END{if(v=="") v=""; print v}')
pool_usage_pct="N/A"
if [[ -n "$pool_active" && -n "$pool_max" ]]; then
  pool_usage_pct=$(awk -v a="$pool_active" -v m="$pool_max" 'BEGIN { if (m <= 0) print "0.00"; else printf "%.2f", (a / m) * 100 }')
fi

mysql_query() {
  local sql="$1"
  if [[ -n "$MYSQL_CMD" ]]; then
    eval "$MYSQL_CMD -Nse \"$sql\""
    return
  fi
  if command -v mysql >/dev/null 2>&1; then
    local host="${MYSQL_HOST:-127.0.0.1}"
    local port="${MYSQL_PORT:-3306}"
    local user="${MYSQL_USER:-root}"
    local password="${MYSQL_PASSWORD:-}"
    MYSQL_PWD="$password" mysql -h "$host" -P "$port" -u "$user" "$DB_NAME" -Nse "$sql"
    return
  fi
  return 1
}

slow_query_ms="N/A"
if slow_val=$(mysql_query "SELECT IFNULL(MAX(AVG_TIMER_WAIT/1000000000),0) FROM performance_schema.events_statements_summary_by_digest WHERE SCHEMA_NAME='${DB_NAME}' AND COUNT_STAR > 0 AND UPPER(DIGEST_TEXT) LIKE '%AGENTX_%' AND (UPPER(DIGEST_TEXT) LIKE 'SELECT %' OR UPPER(DIGEST_TEXT) LIKE 'UPDATE %' OR UPPER(DIGEST_TEXT) LIKE 'INSERT %' OR UPPER(DIGEST_TEXT) LIKE 'DELETE %');" 2>/dev/null); then
  slow_query_ms=$(awk -v v="$slow_val" 'BEGIN { printf "%.3f", v }')
fi

if [[ "$pool_usage_pct" == "N/A" ]]; then
  if threads_connected_line=$(mysql_query "SHOW GLOBAL STATUS LIKE 'Threads_connected';" 2>/dev/null); then
    threads_connected=$(echo "$threads_connected_line" | awk '{print $2}')
    if max_connections_line=$(mysql_query "SHOW VARIABLES LIKE 'max_connections';" 2>/dev/null); then
      max_connections=$(echo "$max_connections_line" | awk '{print $2}')
      if [[ -n "$threads_connected" && -n "$max_connections" ]]; then
        pool_usage_pct=$(awk -v a="$threads_connected" -v m="$max_connections" 'BEGIN { if (m <= 0) print "0.00"; else printf "%.2f", (a / m) * 100 }')
      fi
    fi
  fi
fi

api_ok=$(awk -v r="$api_availability" 'BEGIN { exit !(r >= 0.99) }' && echo "PASS" || echo "FAIL")
slow_ok="UNKNOWN"
if [[ "$slow_query_ms" != "N/A" ]]; then
  slow_ok=$(awk -v v="$slow_query_ms" 'BEGIN { exit !(v < 100) }' && echo "PASS" || echo "FAIL")
fi
pool_ok="UNKNOWN"
if [[ "$pool_usage_pct" != "N/A" ]]; then
  pool_ok=$(awk -v v="$pool_usage_pct" 'BEGIN { exit !(v < 80) }' && echo "PASS" || echo "FAIL")
fi

cat <<REPORT
=== AgentX Phase C SLO Check ===
Prometheus URL: $PROM_URL_RESOLVED
DB Name: $DB_NAME

[API Availability]
agentx_api_availability: $api_availability
threshold: >= 0.99
result: $api_ok

[Context Assembly]
agentx_context_assembly_duration_seconds_count: $context_count
agentx_context_assembly_duration_seconds_avg_ms: $context_avg_ms

[Database]
slow_query_max_avg_ms: $slow_query_ms
threshold: < 100ms
result: $slow_ok

db_pool_usage_pct: $pool_usage_pct
threshold: < 80%
result: $pool_ok
REPORT

if [[ "$api_ok" != "PASS" ]]; then
  exit 1
fi
