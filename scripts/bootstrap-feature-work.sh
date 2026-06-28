#!/bin/zsh
set -euo pipefail

if [[ $# -lt 1 || $# -gt 2 ]]; then
  echo "usage: scripts/bootstrap-feature-work.sh <short-feature-topic> [risk-tier]" >&2
  exit 1
fi

topic="$1"
risk_tier="${2:-medium}"

case "$risk_tier" in
  low|medium|high|executor-critical) ;;
  *)
    echo "invalid risk tier: $risk_tier" >&2
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

python3 - "$plan_path" "$manifest_path" "$slug" "$risk_tier" <<'PY'
from pathlib import Path
import sys

plan_path = Path(sys.argv[1])
manifest_path = Path(sys.argv[2])
slug = sys.argv[3]
risk_tier = sys.argv[4]

title = slug.replace("-", " ").replace("_", " ").title()

plan_text = plan_path.read_text()
plan_text = plan_text.replace("# Feature Implementation Plan", f"# {title} Plan")
plan_path.write_text(plan_text)

manifest_text = manifest_path.read_text()
manifest_text = manifest_text.replace("sample_feature", slug.replace("-", "_"))
manifest_text = manifest_text.replace("Sample feature completion manifest", title)
manifest_text = manifest_text.replace("riskTier: medium", f"riskTier: {risk_tier}")
manifest_text = manifest_text.replace(".agents/sample-feature-plan.md", f".agents/{slug}-plan.md")
manifest_path.write_text(manifest_text)
PY

echo "Created:"
echo "  $plan_path"
echo "  $manifest_path"
