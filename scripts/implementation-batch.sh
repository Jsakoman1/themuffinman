#!/bin/zsh
set -euo pipefail

if [[ $# -lt 1 || $# -gt 5 ]]; then
  echo "usage: scripts/implementation-batch.sh <topic> [comma-separated-files] [manifest-file] [plan-file] [closeout=true|false]" >&2
  exit 1
fi

topic="$1"
files_csv="${2:-}"
manifest="${3:-}"
repo_root="$(cd "$(dirname "$0")/.." && pwd)"
plan_input="${4:-.agents/${topic}-plan.md}"
plan_path="$repo_root/$plan_input"
closeout_requested="${5:-false}"
case "$closeout_requested" in
  true|false) ;;
  *)
    echo "closeout must be true or false" >&2
    exit 1
    ;;
esac
intent="implementation batch for ${topic}"
generated_root="$repo_root/docs/generated/local-tooling/implementation-batches"
batch_slug="${topic:l}"
batch_slug="${batch_slug// /-}"
batch_slug="${batch_slug//[^a-z0-9_-]/-}"
batch_slug="${batch_slug//--/-}"
json_path="$generated_root/${batch_slug}-batch.json"
summary_path="$generated_root/${batch_slug}-batch-summary.md"
ledger_jsonl_path="$generated_root/${batch_slug}-ledger.jsonl"
ledger_json_path="$generated_root/${batch_slug}-ledger.json"
ledger_summary_path="$generated_root/${batch_slug}-ledger-summary.md"
created_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
run_id="${created_at//[-:]/}"
run_id="${run_id/. /}"
run_id="${run_id//./}"
plan_completion="not_requested"
ledger_entries=()

run_make() {
  (cd "$repo_root" && make "$@")
}

append_ledger_entry() {
  local label="$1"
  local entry_status="$2"
  local command_text="$3"
  local note="${4:-}"
  local recorded_at
  recorded_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  ruby -rjson -e 'puts JSON.generate({"runId" => ARGV[0], "label" => ARGV[1], "status" => ARGV[2], "command" => ARGV[3], "note" => ARGV[4], "recordedAt" => ARGV[5]})' "$run_id" "$label" "$entry_status" "$command_text" "$note" "$recorded_at" >> "$ledger_jsonl_path"
}

run_recorded_make() {
  local label="$1"
  shift
  local command_text="make $*"
  if run_make "$@"; then
    append_ledger_entry "$label" "executed" "$command_text"
  else
    append_ledger_entry "$label" "blocked" "$command_text" "command failed"
    write_ledger_outputs
    exit 1
  fi
}

write_ledger_outputs() {
  ruby -rjson -e '
    ledger_jsonl_path, ledger_json_path, ledger_summary_path, run_json_path, created_at, run_id, topic, plan_path, manifest, plan_completion, closeout_requested = ARGV
    history_entries = File.exist?(ledger_jsonl_path) ? File.readlines(ledger_jsonl_path).map(&:strip).reject(&:empty?).map { |line| JSON.parse(line) } : []
    entries = history_entries.select { |entry| entry["runId"] == run_id }
    payload = {
      "generatedAt" => created_at,
      "runId" => run_id,
      "topic" => topic,
      "manifest" => manifest,
      "planPath" => plan_path,
      "planCompletion" => plan_completion,
      "closeoutRequested" => closeout_requested == "true",
      "entries" => entries,
      "historyEntryCount" => history_entries.size
    }
    File.write(ledger_json_path, JSON.pretty_generate(payload) + "\n")
    File.write(run_json_path, JSON.pretty_generate(payload) + "\n")
    counts = entries.group_by { |entry| entry["status"] }.transform_values(&:count)
    lines = []
    lines << "# Implementation Batch Ledger #{topic}"
    lines << ""
    lines << "- Generated At: `#{created_at}`"
    lines << "- Run ID: `#{run_id}`"
    lines << "- Topic: `#{topic}`"
    lines << "- Plan Path: `#{plan_path}`"
    lines << "- Manifest: `#{manifest.empty? ? "none" : manifest}`"
    lines << "- Plan Completion: `#{plan_completion}`"
    lines << "- Closeout Requested: `#{closeout_requested}`"
    lines << "- Executed: `#{counts.fetch(%q{executed}, 0)}`"
    lines << "- Deferred: `#{counts.fetch(%q{deferred}, 0)}`"
    lines << "- Blocked: `#{counts.fetch(%q{blocked}, 0)}`"
    lines << ""
    lines << "## Entries"
    lines << ""
    entries.each do |entry|
      note = entry["note"].to_s.empty? ? "" : " (#{entry["note"]})"
      lines << "- `#{entry["status"]}` #{entry["label"]}: #{entry["command"]}#{note}"
    end
    File.write(ledger_summary_path, lines.join("\n") + "\n")
  ' "$ledger_jsonl_path" "$ledger_json_path" "$ledger_summary_path" "$generated_root/runs/${run_id}.json" "$created_at" "$run_id" "$topic" "$plan_path" "$manifest" "$plan_completion" "$closeout_requested"
}

mkdir -p "$generated_root/runs"

run_recorded_make control-start control-start
run_recorded_make codex-context codex-context topic="$topic" intent="$intent"

if [[ -n "$files_csv" ]]; then
  run_recorded_make audit-router audit-router files="$files_csv"
  run_recorded_make audit-doc-sync-preflight audit-doc-sync-preflight files="$files_csv"
  run_recorded_make audit-doc-sync-required-surfaces audit-doc-sync-required-surfaces files="$files_csv"
  run_recorded_make audit-manifest-decision audit-manifest-decision files="$files_csv"
  run_recorded_make resolve-manifest-path resolve-manifest-path files="$files_csv"
  run_recorded_make recommend-validation-preset recommend-validation-preset files="$files_csv"
  run_recorded_make audit-generated-artifact-hygiene audit-generated-artifact-hygiene files="$files_csv"
else
  run_recorded_make audit-router audit-router
  run_recorded_make audit-generated-artifact-freshness audit-generated-artifact-freshness
  run_recorded_make audit-doc-sync-preflight audit-doc-sync-preflight
  run_recorded_make audit-doc-sync-required-surfaces audit-doc-sync-required-surfaces
  run_recorded_make audit-manifest-decision audit-manifest-decision
  run_recorded_make resolve-manifest-path resolve-manifest-path
  run_recorded_make recommend-validation-preset recommend-validation-preset
fi

if [[ -n "$files_csv" ]]; then
  run_recorded_make recommend-feature-slices recommend-feature-slices topic="$topic" files="$files_csv"
  run_recorded_make recommend-targeted-tests recommend-targeted-tests files="$files_csv"
else
  run_recorded_make recommend-feature-slices recommend-feature-slices topic="$topic"
  run_recorded_make recommend-targeted-tests recommend-targeted-tests
fi

if [[ "$closeout_requested" == "true" ]]; then
  if [[ ! -f "$plan_path" ]]; then
    echo "closeout requires an existing plan: $plan_input" >&2
    exit 1
  fi
  if [[ -z "$manifest" ]]; then
    echo "closeout requires manifest=<manifest-file>" >&2
    exit 1
  fi
  run_recorded_make cleanup-generated-history cleanup-generated-history
  run_recorded_make audit-plan-completion audit-plan-completion plan="$plan_input" manifest="$manifest"
  run_recorded_make feature-closeout-audit feature-closeout-audit manifest="$manifest"
  plan_completion="passed"
fi

run_recorded_make audit-todo audit-todo

generated_artifact_hygiene_summary="docs/generated/local-tooling/generated-artifact-freshness-summary.md"
generated_history_cleanup_summary="not run during preparation"
if [[ -n "$files_csv" ]]; then
  generated_artifact_hygiene_summary="docs/generated/local-tooling/generated-artifact-hygiene-summary.md"
fi
if [[ "$closeout_requested" == "true" ]]; then
  generated_history_cleanup_summary="docs/generated/local-tooling/cleanup-generated-history-summary.md"
fi

cat >"$json_path" <<EOF
{
  "generatedAt": "$created_at",
  "runId": "$run_id",
  "topic": "$topic",
  "filesCsv": "${files_csv}",
  "manifest": "${manifest}",
  "planPath": "${plan_input}",
  "planCompletion": "${plan_completion}",
  "closeoutRequested": ${closeout_requested},
  "controlStart": "docs/generated/local-tooling/control-start.json",
  "codexContext": "docs/generated/local-tooling/codex-context/latest.review.md",
  "generatedHistoryCleanup": "$generated_history_cleanup_summary",
  "generatedArtifactHygiene": "$generated_artifact_hygiene_summary",
  "docSyncPreflight": "docs/generated/local-tooling/doc-sync-preflight-summary.md",
  "docSyncRequiredSurfaces": "docs/generated/local-tooling/doc-sync-required-surfaces-summary.md",
  "manifestDecision": "docs/generated/local-tooling/manifest-decision-summary.md",
  "manifestResolution": "docs/generated/local-tooling/manifest-path-resolution-summary.md",
  "validationPreset": "docs/generated/local-tooling/validation-preset-summary.md",
  "featureSlices": "docs/generated/local-tooling/feature-slices/${batch_slug}.json",
  "targetedTests": "docs/generated/local-tooling/targeted-tests.json",
  "runLedger": "docs/generated/local-tooling/implementation-batches/runs/${run_id}.json",
  "closeoutStatus": "${plan_completion}"
}
EOF

cat >"$summary_path" <<EOF
# Implementation Batch ${topic}

- Generated At: \`$created_at\`
- Run ID: \`$run_id\`
- Topic: \`$topic\`
- Files CSV: \`$files_csv\`
- Manifest: \`${manifest:-none}\`
- Plan Path: \`$plan_input\`
- Plan Completion: \`$plan_completion\`
- Closeout Requested: \`$closeout_requested\`
- Control Start: \`docs/generated/local-tooling/control-start.json\`
- Codex Context: \`docs/generated/local-tooling/codex-context/latest.review.md\`
- Generated History Cleanup: \`$generated_history_cleanup_summary\`
- Doc Sync Preflight: \`docs/generated/local-tooling/doc-sync-preflight-summary.md\`
- Doc Sync Required Surfaces: \`docs/generated/local-tooling/doc-sync-required-surfaces-summary.md\`
- Manifest Decision: \`docs/generated/local-tooling/manifest-decision-summary.md\`
- Manifest Resolution: \`docs/generated/local-tooling/manifest-path-resolution-summary.md\`
- Validation Preset: \`docs/generated/local-tooling/validation-preset-summary.md\`
- Generated Artifact Hygiene: \`$generated_artifact_hygiene_summary\`
- Feature Slices: \`docs/generated/local-tooling/feature-slices/${batch_slug}.json\`
- Targeted Tests: \`docs/generated/local-tooling/targeted-tests.json\`
- Run Ledger: \`docs/generated/local-tooling/implementation-batches/runs/${run_id}.json\`
- Closeout Status: \`$plan_completion\`
EOF

write_ledger_outputs
