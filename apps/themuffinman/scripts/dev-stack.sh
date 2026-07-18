#!/bin/zsh
set -euo pipefail

project_dir="${0:A:h:h}"
state_dir="$project_dir/tmp"
state_file="$state_dir/dev-stack.state"

is_workspace_process() {
  local pid="$1"
  [[ "$pid" == <-> ]] || return 1
  kill -0 "$pid" 2>/dev/null || return 1
  local command
  command="$(ps -p "$pid" -o command= 2>/dev/null || true)"
  [[ "$command" == *"$project_dir"* ]]
}

stop_process_tree() {
  local pid="$1"
  local trusted_child="${2:-0}"
  [[ "$pid" == <-> ]] || return 0
  kill -0 "$pid" 2>/dev/null || return 0
  if [[ "$trusted_child" != "1" ]]; then
    is_workspace_process "$pid" || return 0
  fi
  local child
  for child in $(pgrep -P "$pid" 2>/dev/null || true); do
    stop_process_tree "$child" 1
  done
  kill -TERM "$pid" 2>/dev/null || true
}

stop_owned_stack() {
  [[ -f "$state_file" ]] || return 0
  local state_project stack_pid backend_pid frontend_pid
  IFS='|' read -r state_project stack_pid backend_pid frontend_pid < "$state_file" || true
  if [[ "$state_project" != "$project_dir" ]]; then
    echo "[dev] refusing a state file owned by another workspace: $state_file" >&2
    return 1
  fi
  stop_process_tree "$backend_pid"
  stop_process_tree "$frontend_pid"
  stop_process_tree "$stack_pid"
  local attempt
  for attempt in {1..20}; do
    local backend_alive=0 frontend_alive=0
    kill -0 "$backend_pid" 2>/dev/null && backend_alive=1 || true
    kill -0 "$frontend_pid" 2>/dev/null && frontend_alive=1 || true
    (( backend_alive == 0 && frontend_alive == 0 )) && break
    sleep 0.1
  done
  rm -f "$state_file"
  if port_is_busy 8080 || port_is_busy 5173; then
    echo "[dev] stop requested, but one or more development ports remain occupied; run 'make dev-doctor' and stop only the owning stack." >&2
    return 1
  fi
}

port_is_busy() {
  lsof -nP -iTCP:"$1" -sTCP:LISTEN >/dev/null 2>&1
}

start_stack() {
  mkdir -p "$state_dir"
  stop_owned_stack
  if port_is_busy 8080 || port_is_busy 5173; then
    echo "[dev] required port is already occupied; run 'make dev-doctor' and stop only the owning stack before retrying." >&2
    exit 1
  fi

  set -a
  [[ -f "$project_dir/.env.backend.dev" ]] && source "$project_dir/.env.backend.dev"
  [[ -f "$project_dir/.env.backend.dev.local" ]] && source "$project_dir/.env.backend.dev.local"
  set +a
  export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"
  export SIDEQUEST_JWT_SECRET="${SIDEQUEST_JWT_SECRET:-sidequest-dev-secret-sidequest-dev-secret-123456}"
  export SIDEQUEST_CORS_ALLOWED_ORIGINS="${SIDEQUEST_CORS_ALLOWED_ORIGINS:-http://localhost:5173}"

  echo "[dev] object storage enabled=${SIDEQUEST_OBJECT_STORAGE_ENABLED:-false} provider=${SIDEQUEST_OBJECT_STORAGE_PROVIDER:-local}"
  if [[ "${SIDEQUEST_OBJECT_STORAGE_ENABLED:-false}" == "true" && "${SIDEQUEST_OBJECT_STORAGE_PROVIDER:-local}" == "s3" ]]; then
    echo "[dev] starting local MinIO because object storage provider is s3"
    make -C "$project_dir" dev-storage
  elif [[ "${SIDEQUEST_OBJECT_STORAGE_ENABLED:-false}" == "true" && "${SIDEQUEST_OBJECT_STORAGE_PROVIDER:-local}" == "local" ]]; then
    echo "[dev] using local-disk object storage; no MinIO container needed"
  else
    echo "[dev] chat attachments storage is disabled"
  fi

  (cd "$project_dir" && ./mvnw spring-boot:run) &
  local backend_pid=$!
  (cd "$project_dir/frontend" && exec node "$project_dir/frontend/node_modules/vite/bin/vite.js") &
  local frontend_pid=$!
  print -r -- "$project_dir|$$|$backend_pid|$frontend_pid" > "$state_file"
  cleanup() { stop_owned_stack }
  trap cleanup INT TERM EXIT
  wait "$backend_pid" "$frontend_pid"
}

case "${1:-start}" in
  start) start_stack ;;
  stop) stop_owned_stack ;;
  *) echo "usage: $0 [start|stop]" >&2; exit 2 ;;
esac
