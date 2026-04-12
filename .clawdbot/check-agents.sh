#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
TASKS_FILE="$ROOT_DIR/.clawdbot/active-tasks.json"
FEISHU_WEBHOOK_URL="${FEISHU_WEBHOOK_URL:-}"

command -v jq >/dev/null || { echo "jq not found" >&2; exit 1; }

notify_feishu() {
  local title="$1"
  local branch="$2"
  local scope="$3"
  local pr="$4"
  local ci_state="$5"
  local ui_flag="$6"
  [[ -n "$FEISHU_WEBHOOK_URL" ]] || return 0

  curl -sS -X POST "$FEISHU_WEBHOOK_URL" \
    -H 'Content-Type: application/json' \
    -d "$(jq -nc \
      --arg title "$title" \
      --arg branch "$branch" \
      --arg scope "$scope" \
      --arg pr "$pr" \
      --arg ci "$ci_state" \
      --arg ui "$ui_flag" '
      {
        msg_type:"interactive",
        card:{
          header:{title:{content:"PR Ready for Review",tag:"plain_text"}},
          elements:[
            {tag:"div",text:{tag:"lark_md",content:("**" + $title + "**\nBranch: `" + $branch + "`\nScope: `" + $scope + "`\nPR: " + (if $pr == "" then "not created" else ("#" + $pr) end) + "\nCI: `" + $ci + "`\nUI screenshots required: `" + $ui + "`")}}
          ]
        }
      }')" >/dev/null
}

set_task_fields() {
  local id="$1"
  local status="$2"
  local pr="$3"
  local note="$4"
  local ci_status="$5"
  local checks_json="$6"
  local tmp
  tmp="$(mktemp)"
  jq \
    --arg id "$id" \
    --arg status "$status" \
    --arg pr "$pr" \
    --arg note "$note" \
    --arg ciStatus "$ci_status" \
    --argjson checks "$checks_json" '
      map(if .id == $id
          then .status = $status
          | .prNumber = $pr
          | .note = $note
          | .ciStatus = $ciStatus
          | .checks = $checks
          else . end)
    ' "$TASKS_FILE" > "$tmp"
  mv "$tmp" "$TASKS_FILE"
}

has_ui_changes() {
  local worktree="$1"
  git -C "$worktree" diff --name-only HEAD~1..HEAD 2>/dev/null | grep -Eq '^(yudao-ui/|.*\.(vue|ts|tsx|js|css|scss|less))$'
}

pr_number_for_branch() {
  local branch="$1"
  gh pr list --head "$branch" --json number --jq '.[0].number // ""' 2>/dev/null || true
}

pr_body_for_number() {
  local pr="$1"
  gh pr view "$pr" --json body --jq '.body // ""' 2>/dev/null || true
}

ci_state_for_pr() {
  local pr="$1"
  gh pr checks "$pr" --json bucket --jq '
    if length == 0 then "missing"
    elif any(.[]; .bucket == "fail") then "fail"
    elif any(.[]; .bucket == "pending") then "pending"
    elif all(.[]; .bucket == "pass" or .bucket == "skipping") then "pass"
    else "unknown" end
  ' 2>/dev/null || true
}

count="$(jq 'length' "$TASKS_FILE")"
if [[ "$count" == "0" ]]; then
  echo "No active tasks"
  exit 0
fi

while IFS= read -r row; do
  id="$(jq -r '.id' <<<"$row")"
  status="$(jq -r '.status' <<<"$row")"
  session="$(jq -r '.tmuxSession' <<<"$row")"
  worktree="$(jq -r '.worktree' <<<"$row")"
  branch="$(jq -r '.branch' <<<"$row")"
  scope="$(jq -r '.scope' <<<"$row")"
  name="$(jq -r '.name' <<<"$row")"
  notify_done="$(jq -r '.notifyOnComplete // true' <<<"$row")"

  if [[ ! -d "$worktree" ]]; then
    set_task_fields "$id" "failed" "" "worktree missing" "missing" '{"worktree":false}'
    echo "Task $id failed: worktree missing"
    continue
  fi

  if [[ "$status" == "running" ]]; then
    if tmux has-session -t "$session" 2>/dev/null; then
      echo "Task $id still running in tmux session $session"
      continue
    fi
  fi

  pr=""
  ci_state="not-checked"
  pr_created=false
  ui_changed=false
  screenshots_ok=true
  dod=false
  note=""

  if command -v gh >/dev/null; then
    pr="$(pr_number_for_branch "$branch")"
  fi

  if [[ -n "$pr" ]]; then
    pr_created=true
    ci_state="$(ci_state_for_pr "$pr")"
  fi

  if has_ui_changes "$worktree"; then
    ui_changed=true
    screenshots_ok=false
    if [[ -n "$pr" ]]; then
      pr_body="$(pr_body_for_number "$pr")"
      if grep -Eiq '(screenshot|截图|!\[|https?://)' <<<"$pr_body"; then
        screenshots_ok=true
      fi
    fi
  fi

  if [[ "$pr_created" == true && "$ci_state" == "pass" && "$screenshots_ok" == true ]]; then
    dod=true
  fi

  checks_json="$(jq -nc \
    --argjson prCreated "$pr_created" \
    --argjson uiChanged "$ui_changed" \
    --argjson screenshotsOk "$screenshots_ok" \
    --arg ci "$ci_state" \
    --argjson dod "$dod" \
    '{prCreated:$prCreated, uiChanged:$uiChanged, screenshotsOk:$screenshotsOk, ci:$ci, definitionOfDone:$dod}')"

  if [[ "$dod" == true ]]; then
    note="definition of done met"
    set_task_fields "$id" "done" "$pr" "$note" "$ci_state" "$checks_json"
    if [[ "$notify_done" == "true" ]]; then
      notify_feishu "$name" "$branch" "$scope" "$pr" "$ci_state" "$ui_changed"
    fi
    echo "Task $id done"
  elif [[ "$pr_created" == true ]]; then
    note="pr exists; waiting for CI or screenshot requirements"
    set_task_fields "$id" "awaiting-review" "$pr" "$note" "$ci_state" "$checks_json"
    echo "Task $id awaiting review"
  else
    note="agent session ended but no PR detected yet"
    set_task_fields "$id" "awaiting-pr" "$pr" "$note" "$ci_state" "$checks_json"
    echo "Task $id awaiting PR"
  fi
done < <(jq -rc '.[]' "$TASKS_FILE")
