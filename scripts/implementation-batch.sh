#!/bin/zsh
set -euo pipefail

if [[ $# -lt 1 || $# -gt 3 ]]; then
  echo "usage: scripts/implementation-batch.sh <topic> [comma-separated-files] [manifest-file]" >&2
  exit 1
fi

topic="$1"
files_csv="${2:-}"
manifest="${3:-}"
repo_root="$(cd "$(dirname "$0")/.." && pwd)"
plan_path="$repo_root/.agents/${topic}-plan.md"
intent="implementation batch for ${topic}"
generated_root="$repo_root/docs/generated/local-tooling/implementation-batches"
batch_slug="${topic:l}"
batch_slug="${batch_slug// /-}"
batch_slug="${batch_slug//[^a-z0-9_-]/-}"
batch_slug="${batch_slug//--/-}"
json_path="$generated_root/${batch_slug}-batch.json"
summary_path="$generated_root/${batch_slug}-batch-summary.md"
created_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
plan_completion="skipped"

run_make() {
  (cd "$repo_root" && make "$@")
}

mkdir -p "$generated_root"

run_make control-start
run_make codex-context topic="$topic" intent="$intent"
run_make audit-generated-artifact-hygiene files="$files_csv"

if [[ -n "$files_csv" ]]; then
  run_make recommend-feature-slices topic="$topic" files="$files_csv"
  run_make recommend-targeted-tests files="$files_csv"
else
  run_make recommend-targeted-tests
fi

if [[ -f "$plan_path" ]]; then
  if [[ -n "$manifest" ]]; then
    run_make audit-plan-completion plan="$plan_path" manifest="$manifest"
  else
    run_make audit-plan-completion plan="$plan_path"
  fi
  plan_completion="attempted"
fi

run_make audit-todo

cat >"$json_path" <<EOF
{
  "generatedAt": "$created_at",
  "topic": "$topic",
  "filesCsv": "${files_csv}",
  "manifest": "${manifest}",
  "planPath": "${plan_path}",
  "planCompletion": "${plan_completion}",
  "controlStart": "docs/generated/local-tooling/control-start.json",
  "codexContext": "docs/generated/local-tooling/codex-context/latest.review.md",
  "generatedArtifactHygiene": "docs/generated/local-tooling/generated-artifact-hygiene-summary.md",
  "featureSlices": "docs/generated/local-tooling/feature-slices/${batch_slug}.json",
  "targetedTests": "docs/generated/local-tooling/targeted-tests.json",
  "closeoutStatus": "audit-todo"
}
EOF

cat >"$summary_path" <<EOF
# Implementation Batch ${topic}

- Generated At: \`$created_at\`
- Topic: \`$topic\`
- Files CSV: \`$files_csv\`
- Manifest: \`${manifest:-none}\`
- Plan Path: \`$plan_path\`
- Plan Completion: \`$plan_completion\`
- Control Start: \`docs/generated/local-tooling/control-start.json\`
- Codex Context: \`docs/generated/local-tooling/codex-context/latest.review.md\`
- Generated Artifact Hygiene: \`docs/generated/local-tooling/generated-artifact-hygiene-summary.md\`
- Feature Slices: \`docs/generated/local-tooling/feature-slices/${batch_slug}.json\`
- Targeted Tests: \`docs/generated/local-tooling/targeted-tests.json\`
- Closeout Status: \`audit-todo\`
EOF
