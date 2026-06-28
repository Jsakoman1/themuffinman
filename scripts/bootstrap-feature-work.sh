#!/bin/zsh
set -euo pipefail

if [[ $# -lt 1 || $# -gt 5 ]]; then
  echo "usage: scripts/bootstrap-feature-work.sh <short-feature-topic> [risk-tier] [change-mode] [change-impact] [comma-separated-profiles]" >&2
  exit 1
fi

topic="$1"
risk_tier="${2:-medium}"
change_mode="${3:-major-change}"
change_impact="${4:-logic-drift}"
change_profiles="${5:-backend-logic}"

case "$risk_tier" in
  low|medium|high|executor-critical) ;;
  *)
    echo "invalid risk tier: $risk_tier" >&2
    exit 1
    ;;
esac

case "$change_mode" in
  small-change|major-change) ;;
  *)
    echo "invalid change mode: $change_mode" >&2
    exit 1
    ;;
esac

case "$change_impact" in
  cosmetic|contract-neutral-refactor|logic-drift) ;;
  *)
    echo "invalid change impact: $change_impact" >&2
    exit 1
    ;;
esac

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
slug="${topic:l}"
slug="${slug// /-}"
slug="${slug//[^a-z0-9_-]/-}"
slug="${slug//--/-}"

plan_path="$repo_root/.agents/${slug}-plan.md"
manifest_path="$repo_root/.agents/feature-manifests/${slug}-manifest.yaml"
plan_template="$repo_root/.agents/templates/feature-implementation-plan.template.md"
manifest_template="$repo_root/.agents/templates/feature-completion-manifest.template.yaml"

if [[ -e "$plan_path" || -e "$manifest_path" ]]; then
  echo "plan or manifest already exists for topic: $slug" >&2
  exit 1
fi

mkdir -p "$repo_root/.agents/feature-manifests"
cp "$plan_template" "$plan_path"
cp "$manifest_template" "$manifest_path"

python3 - "$plan_path" "$manifest_path" "$slug" "$risk_tier" "$change_mode" "$change_impact" "$change_profiles" <<'PY'
from pathlib import Path
import sys

plan_path = Path(sys.argv[1])
manifest_path = Path(sys.argv[2])
slug = sys.argv[3]
risk_tier = sys.argv[4]
change_mode = sys.argv[5]
change_impact = sys.argv[6]
change_profiles = [profile.strip() for profile in sys.argv[7].split(",") if profile.strip()]

title = slug.replace("-", " ").replace("_", " ").title()

plan_text = plan_path.read_text()
plan_text = plan_text.replace("# Feature Implementation Plan", f"# {title} Plan")
plan_path.write_text(plan_text)

manifest_text = manifest_path.read_text()
manifest_text = manifest_text.replace("sample_feature", slug.replace("-", "_"))
manifest_text = manifest_text.replace("Sample feature completion manifest", title)
manifest_text = manifest_text.replace("riskTier: medium", f"riskTier: {risk_tier}")
manifest_text = manifest_text.replace("changeMode: major-change", f"changeMode: {change_mode}")
manifest_text = manifest_text.replace("changeImpact: logic-drift", f"changeImpact: {change_impact}")
manifest_text = manifest_text.replace("  - backend-logic", "\n".join(f"  - {profile}" for profile in change_profiles))
manifest_text = manifest_text.replace(".agents/sample-feature-plan.md", f".agents/{slug}-plan.md")
manifest_path.write_text(manifest_text)
PY

echo "Created:"
echo "  $plan_path"
echo "  $manifest_path"
