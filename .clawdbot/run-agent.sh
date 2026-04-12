#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
TASKS_FILE="$ROOT_DIR/.clawdbot/active-tasks.json"
PROMPTS_DIR="$ROOT_DIR/.clawdbot/prompts"
WORKTREE_BASE="${WORKTREE_BASE:-$ROOT_DIR/.worktrees}"
DEFAULT_BASE_BRANCH="${BASE_BRANCH:-$(git -C "$ROOT_DIR" branch --show-current 2>/dev/null || echo main)}"
FRONTEND_DIR="$ROOT_DIR/yudao-ui/yudao-ui-admin-vue3"

usage() {
  cat <<'EOF'
Usage:
  .clawdbot/run-agent.sh --type <feature|bugfix|review> --scope <backend|frontend|fullstack> \
    --name <task-name> [--targets <path1,path2>] [--base <branch>] [--prompt <text>] [--no-install]

Example:
  .clawdbot/run-agent.sh --type bugfix --scope backend --name bpm-timeout-fix \
    --targets yudao-module-bpm,yudao-server --prompt "Fix timeout handling for approval callbacks"
EOF
}

TYPE=""
SCOPE=""
NAME=""
TARGETS=""
BASE_BRANCH="$DEFAULT_BASE_BRANCH"
EXTRA_PROMPT=""
NO_INSTALL="false"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --type) TYPE="$2"; shift 2 ;;
    --scope) SCOPE="$2"; shift 2 ;;
    --name) NAME="$2"; shift 2 ;;
    --targets) TARGETS="$2"; shift 2 ;;
    --base) BASE_BRANCH="$2"; shift 2 ;;
    --prompt) EXTRA_PROMPT="$2"; shift 2 ;;
    --no-install) NO_INSTALL="true"; shift 1 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown arg: $1" >&2; usage; exit 1 ;;
  esac
done

[[ -n "$TYPE" && -n "$SCOPE" && -n "$NAME" ]] || { usage; exit 1; }
command -v codex >/dev/null || { echo "codex not found in PATH" >&2; exit 1; }
command -v jq >/dev/null || { echo "jq not found in PATH" >&2; exit 1; }
command -v tmux >/dev/null || { echo "tmux not found in PATH" >&2; exit 1; }
command -v python3 >/dev/null || { echo "python3 not found in PATH" >&2; exit 1; }

case "$TYPE" in
  feature|bugfix|review) ;;
  *) echo "Unsupported type: $TYPE" >&2; exit 1 ;;
esac
case "$SCOPE" in
  backend|frontend|fullstack) ;;
  *) echo "Unsupported scope: $SCOPE" >&2; exit 1 ;;
esac

slugify() {
  printf '%s' "$1" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/-/g; s/^-+|-+$//g'
}

ensure_json_registry() {
  if [[ ! -f "$TASKS_FILE" ]]; then
    printf '[]\n' > "$TASKS_FILE"
  fi
  jq empty "$TASKS_FILE" >/dev/null
}

pick_verify_commands() {
  case "$SCOPE" in
    backend)
      printf '%s' 'mvn -pl yudao-server -am -DskipITs test'
      ;;
    frontend)
      printf '%s' 'cd yudao-ui/yudao-ui-admin-vue3 && pnpm ts:check && pnpm build:dev'
      ;;
    fullstack)
      printf '%s' 'mvn -pl yudao-server -am -DskipITs test && cd yudao-ui/yudao-ui-admin-vue3 && pnpm ts:check && pnpm build:dev'
      ;;
  esac
}

cleanup_worktree_on_error() {
  if [[ -n "${WORKTREE:-}" && -d "$WORKTREE" ]]; then
    git -C "$ROOT_DIR" worktree remove --force "$WORKTREE" >/dev/null 2>&1 || true
  fi
}

ensure_json_registry

TASK_SLUG="$(slugify "$NAME")"
TASK_ID="$(date +%Y%m%d-%H%M%S)-$TASK_SLUG"
BRANCH="agent/${TYPE}/${TASK_SLUG}"
WORKTREE="$WORKTREE_BASE/$TASK_ID"
SESSION_BASE="codex-${TASK_SLUG}"
SESSION="$SESSION_BASE"
PROMPT_TEMPLATE="$PROMPTS_DIR/$TYPE.md"
PROMPT_FILE="$WORKTREE/.clawdbot-prompt.txt"
VERIFY_COMMANDS="$(pick_verify_commands)"

[[ -f "$PROMPT_TEMPLATE" ]] || { echo "Missing prompt template: $PROMPT_TEMPLATE" >&2; exit 1; }
[[ -d "$FRONTEND_DIR" ]] || { echo "Missing frontend directory: $FRONTEND_DIR" >&2; exit 1; }
mkdir -p "$WORKTREE_BASE"

if git -C "$ROOT_DIR" show-ref --verify --quiet "refs/heads/$BRANCH"; then
  BRANCH="${BRANCH}-$(date +%H%M%S)"
fi

suffix=1
while tmux has-session -t "$SESSION" 2>/dev/null; do
  SESSION="${SESSION_BASE}-${suffix}"
  suffix=$((suffix + 1))
done

trap cleanup_worktree_on_error ERR

git -C "$ROOT_DIR" fetch --all --prune || true
git -C "$ROOT_DIR" worktree add "$WORKTREE" -b "$BRANCH" "$BASE_BRANCH"
mkdir -p "$WORKTREE/.clawdbot"

TARGET_TEXT="${TARGETS:-auto-detect from task scope}"
PROMPT_BODY="$(cat "$PROMPT_TEMPLATE")"
PROMPT_BODY="${PROMPT_BODY//\{\{SCOPE\}\}/$SCOPE}"
PROMPT_BODY="${PROMPT_BODY//\{\{TARGETS\}\}/$TARGET_TEXT}"
PROMPT_BODY="${PROMPT_BODY//\{\{BRANCH\}\}/$BRANCH}"
PROMPT_BODY="${PROMPT_BODY//\{\{WORKTREE\}\}/$WORKTREE}"

cat > "$PROMPT_FILE" <<EOF
$PROMPT_BODY

Repo-specific verification guidance:
- Preferred verification commands for this scope: $VERIFY_COMMANDS
- If you only touch a narrower module than the default scope, you may further narrow checks.
- Do not run broad whole-repo cleanup.

Task request:
$EXTRA_PROMPT
EOF

FRONTEND_SETUP=""
if [[ "$NO_INSTALL" != "true" && ( "$SCOPE" == "frontend" || "$SCOPE" == "fullstack" ) ]]; then
  FRONTEND_SETUP="if [ ! -d 'yudao-ui/yudao-ui-admin-vue3/node_modules' ]; then cd 'yudao-ui/yudao-ui-admin-vue3' && pnpm install && cd - >/dev/null; fi && "
fi

export PROMPT_FILE
PROMPT_ESCAPED="$(python3 - <<'PY'
import os
from pathlib import Path
text = Path(os.environ['PROMPT_FILE']).read_text()
text = text.replace('\\', '\\\\').replace('"', '\\"').replace('$', '\\$').replace('`', '\\`')
print(text)
PY
)"

TMUX_CMD="cd '$WORKTREE' && ${FRONTEND_SETUP}codex exec --full-auto \"$PROMPT_ESCAPED\""

tmux new-session -d -s "$SESSION" -c "$WORKTREE" "$TMUX_CMD"

TMP_JSON="$(mktemp)"
jq --arg id "$TASK_ID" \
   --arg type "$TYPE" \
   --arg scope "$SCOPE" \
   --arg name "$NAME" \
   --arg repo "$ROOT_DIR" \
   --arg worktree "$WORKTREE" \
   --arg branch "$BRANCH" \
   --arg tmuxSession "$SESSION" \
   --arg targets "$TARGETS" \
   --arg verifyCommands "$VERIFY_COMMANDS" \
   --arg startedAt "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
   '. += [{id:$id,type:$type,scope:$scope,name:$name,repo:$repo,worktree:$worktree,branch:$branch,tmuxSession:$tmuxSession,targets:$targets,verifyCommands:$verifyCommands,status:"running",retryCount:0,prNumber:"",notifyOnComplete:true,startedAt:$startedAt}]' \
   "$TASKS_FILE" > "$TMP_JSON"
mv "$TMP_JSON" "$TASKS_FILE"

trap - ERR

echo "Started task: $TASK_ID"
echo "  branch: $BRANCH"
echo "  worktree: $WORKTREE"
echo "  tmux: $SESSION"
echo "  verify: $VERIFY_COMMANDS"
