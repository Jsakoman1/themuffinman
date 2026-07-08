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

if [[ -n "$files_csv" ]]; then
  run_make audit-router files="$files_csv"
  run_make audit-doc-sync-preflight files="$files_csv"
  run_make audit-doc-sync-required-surfaces files="$files_csv"
  run_make audit-manifest-decision files="$files_csv"
  run_make resolve-manifest-path files="$files_csv"
  run_make recommend-validation-preset files="$files_csv"
  run_make audit-generated-artifact-hygiene files="$files_csv"
else
  run_make audit-router
  run_make audit-generated-artifact-freshness
  run_make audit-doc-sync-preflight
  run_make audit-doc-sync-required-surfaces
  run_make audit-manifest-decision
  run_make resolve-manifest-path
  run_make recommend-validation-preset
fi

if [[ -n "$files_csv" ]]; then
  run_make recommend-feature-slices topic="$topic" files="$files_csv"
  run_make recommend-targeted-tests files="$files_csv"
else
  run_make recommend-feature-slices topic="$topic"
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

generated_artifact_hygiene_summary="docs/generated/local-tooling/generated-artifact-freshness-summary.md"
if [[ -n "$files_csv" ]]; then
  generated_artifact_hygiene_summary="docs/generated/local-tooling/generated-artifact-hygiene-summary.md"
fi

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
  "generatedArtifactHygiene": "$generated_artifact_hygiene_summary",
  "docSyncPreflight": "docs/generated/local-tooling/doc-sync-preflight-summary.md",
  "docSyncRequiredSurfaces": "docs/generated/local-tooling/doc-sync-required-surfaces-summary.md",
  "manifestDecision": "docs/generated/local-tooling/manifest-decision-summary.md",
  "manifestResolution": "docs/generated/local-tooling/manifest-path-resolution-summary.md",
  "validationPreset": "docs/generated/local-tooling/validation-preset-summary.md",
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
- Doc Sync Preflight: \`docs/generated/local-tooling/doc-sync-preflight-summary.md\`
- Doc Sync Required Surfaces: \`docs/generated/local-tooling/doc-sync-required-surfaces-summary.md\`
- Manifest Decision: \`docs/generated/local-tooling/manifest-decision-summary.md\`
- Manifest Resolution: \`docs/generated/local-tooling/manifest-path-resolution-summary.md\`
- Validation Preset: \`docs/generated/local-tooling/validation-preset-summary.md\`
- Generated Artifact Hygiene: \`$generated_artifact_hygiene_summary\`
- Feature Slices: \`docs/generated/local-tooling/feature-slices/${batch_slug}.json\`
- Targeted Tests: \`docs/generated/local-tooling/targeted-tests.json\`
- Closeout Status: \`audit-todo\`
EOF
