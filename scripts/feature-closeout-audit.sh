#!/bin/zsh
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "usage: scripts/feature-closeout-audit.sh <manifest-file>" >&2
  exit 1
fi

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
manifest_path="$repo_root/$1"

if [[ ! -f "$manifest_path" ]]; then
  echo "manifest not found: $manifest_path" >&2
  exit 1
fi

plan_file="$(sed -n 's/^planFile: //p' "$manifest_path" | head -n 1)"
manifest_status="$(sed -n 's/^status: //p' "$manifest_path" | head -n 1)"
risk_tier="$(sed -n 's/^riskTier: //p' "$manifest_path" | head -n 1)"

if [[ -z "$plan_file" ]]; then
  echo "manifest is missing planFile: $manifest_path" >&2
  exit 1
fi

if [[ ! -f "$repo_root/$plan_file" ]]; then
  echo "referenced plan file is missing: $repo_root/$plan_file" >&2
  exit 1
fi

echo "Feature close-out audit"
echo "  Manifest: $manifest_path"
echo "  Plan:     $repo_root/$plan_file"
echo "  Status:   ${manifest_status:-unknown}"
echo "  Risk:     ${risk_tier:-unknown}"

required_commands=()
if [[ "$risk_tier" == "executor-critical" || "$risk_tier" == "high" ]]; then
  required_commands+=("make audit-agent-safety")
fi

if grep -q "frontendValidationPassed: true" "$manifest_path"; then
  required_commands+=("npm run type-check")
fi

echo "Required checks:"
for command in "${required_commands[@]}"; do
  echo "  - $command"
done

echo "Checklist status:"
grep -E '^  (tempPlanCreated|codeImplemented|backendTestsPassed|frontendValidationPassed|docsSynced|agentModelSynced|destructivePolicyChecked|multilingualCoverageChecked): ' "$manifest_path" || true
