#!/bin/zsh
set -euo pipefail

if [[ $# -lt 1 || $# -gt 7 ]]; then
  echo "usage: scripts/bootstrap-feature-work.sh <short-feature-topic> [risk-tier] [mode] [change-impact] [comma-separated-profiles] [workflow-tier|auto] [manifest-decision|auto]" >&2
  exit 1
fi

topic="$1"
risk_tier="${2:-medium}"
input_mode="${3:-normal}"
change_impact="${4:-logic-drift}"
change_profiles="${5:-backend-logic}"
workflow_tier="${6:-auto}"
manifest_decision="${7:-auto}"

case "$risk_tier" in
  low|medium|high|executor-critical) ;;
  *)
    echo "invalid risk tier: $risk_tier" >&2
    exit 1
    ;;
esac

case "$input_mode" in
  tiny|normal|feature|agent-workflow|small-change|major-change) ;;
  *)
    echo "invalid mode: $input_mode" >&2
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

case "$workflow_tier" in
  auto|tier1-tiny-change|tier2-normal-feature|tier3-high-risk-multi-layer|tier4-agent-tooling-workflow) ;;
  *)
    echo "invalid workflow tier: $workflow_tier" >&2
    exit 1
    ;;
esac

case "$manifest_decision" in
  auto|required|optional|resolver_review) ;;
  *)
    echo "invalid manifest decision: $manifest_decision" >&2
    exit 1
    ;;
esac

case "$input_mode" in
  tiny|small-change)
    change_mode="small-change"
    ;;
  *)
    change_mode="major-change"
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

python3 - "$plan_path" "$manifest_template" "$manifest_path" "$slug" "$risk_tier" "$change_mode" "$input_mode" "$change_impact" "$change_profiles" "$workflow_tier" "$manifest_decision" <<'PY'
from pathlib import Path
import sys

plan_path = Path(sys.argv[1])
manifest_template_path = Path(sys.argv[2])
manifest_path = Path(sys.argv[3])
slug = sys.argv[4]
risk_tier = sys.argv[5]
change_mode = sys.argv[6]
input_mode = sys.argv[7]
change_impact = sys.argv[8]
profiles = [profile.strip() for profile in sys.argv[9].split(",") if profile.strip()]
workflow_tier = sys.argv[10]
manifest_decision = sys.argv[11]

title = slug.replace("-", " ").replace("_", " ").title()

if workflow_tier == "auto":
    if input_mode in {"tiny", "small-change"}:
        workflow_tier = "tier1-tiny-change"
    elif input_mode == "agent-workflow" or "agent-contract" in profiles:
        workflow_tier = "tier4-agent-tooling-workflow"
    elif risk_tier in {"high", "executor-critical"} or {"frontend-contract", "workflow-expansion"} & set(profiles):
        workflow_tier = "tier3-high-risk-multi-layer"
    else:
        workflow_tier = "tier2-normal-feature"

if manifest_decision == "auto":
    if workflow_tier == "tier1-tiny-change":
        manifest_decision = "optional"
    elif workflow_tier == "tier2-normal-feature":
        manifest_decision = "resolver_review"
    else:
        manifest_decision = "required"

context_commands = ["make codex-context topic=<topic> intent='<intent>'"]
routing_commands = ["make recommend-targeted-tests"]
validation_commands = ["make audit-todo"]
closeout_commands = ["make audit-todo"]
expected_docs = ["resolver-driven"]
expected_artifacts = ["resolver-driven"]
expected_tests = ["make recommend-targeted-tests"]
open_questions = ["Need changed-file routing to confirm docs, manifest, and validation scope."]
scope = "Replace with the concrete feature scope."
out_of_scope = "Replace with explicit exclusions if they matter."

if workflow_tier == "tier1-tiny-change":
    routing_commands = ["make recommend-targeted-tests"]
    validation_commands = ["make recommend-targeted-tests", "make audit-todo"]
    closeout_commands = ["make audit-todo"]
    expected_docs = ["Usually none beyond directly changed docs unless meaning changed."]
    expected_artifacts = ["None by default."]
    expected_tests = ["Cheapest relevant targeted check only."]
    open_questions = ["If meaning, contracts, or generated artifacts changed, escalate to a higher tier."]
elif workflow_tier == "tier2-normal-feature":
    routing_commands = [
        "make audit-router files=<csv>",
        "make audit-doc-sync-required-surfaces files=<csv>",
        "make audit-manifest-decision files=<csv>",
        "make recommend-validation-preset files=<csv>",
    ]
    validation_commands = ["make recommend-validation-preset files=<csv>"]
    closeout_commands = ["make audit-todo", f"make audit-plan-completion plan=.agents/{slug}-plan.md"]
    expected_docs = ["Use audit-doc-sync-required-surfaces output."]
    expected_artifacts = ["Refresh only generated artifacts that the resolver marks as affected."]
    expected_tests = ["Use recommend-validation-preset output."]
    open_questions = ["Resolver still needs to confirm whether a manifest is required."]
elif workflow_tier == "tier3-high-risk-multi-layer":
    routing_commands = [
        "make audit-router files=<csv>",
        "make audit-doc-sync-required-surfaces files=<csv>",
        "make audit-manifest-decision files=<csv>",
        "make resolve-manifest-path files=<csv>",
        "make recommend-validation-preset files=<csv>",
    ]
    validation_commands = [
        "make recommend-validation-preset files=<csv>",
        f"make record-validation manifest=.agents/feature-manifests/{slug}-manifest.yaml command='<command>'",
    ]
    closeout_commands = [
        f"make autofill-feature-closeout manifest=.agents/feature-manifests/{slug}-manifest.yaml files=<csv> generated=<csv> docs=<csv>",
        "make audit-todo",
        f"make audit-plan-completion plan=.agents/{slug}-plan.md manifest=.agents/feature-manifests/{slug}-manifest.yaml",
        "make audit-validation-evidence-quality",
        f"make feature-closeout-audit manifest=.agents/feature-manifests/{slug}-manifest.yaml",
        f"make closeout-report manifest=.agents/feature-manifests/{slug}-manifest.yaml",
    ]
    expected_docs = ["Use audit-doc-sync-required-surfaces output.", "Expect workflow and closeout docs to move when process rules changed."]
    expected_artifacts = ["Manifest-backed evidence, generated artifacts, and resolver-reported outputs."]
    expected_tests = ["Targeted checks plus any risk-required broader validation."]
    open_questions = ["Resolver still needs the changed-file list to lock docs, artifacts, and validation scope."]
else:
    routing_commands = [
        "make audit-router files=<csv>",
        "make audit-doc-sync-required-surfaces files=<csv>",
        "make audit-manifest-decision files=<csv>",
        "make resolve-manifest-path files=<csv>",
        "make recommend-validation-preset files=<csv>",
    ]
    validation_commands = [
        "ruby scripts/generate-agent-operating-model.rb",
        "make audit-agent-safety",
        "make recommend-validation-preset files=<csv>",
    ]
    closeout_commands = [
        f"make autofill-feature-closeout manifest=.agents/feature-manifests/{slug}-manifest.yaml files=<csv> generated=<csv> docs=<csv>",
        "make audit-todo",
        f"make audit-plan-completion plan=.agents/{slug}-plan.md manifest=.agents/feature-manifests/{slug}-manifest.yaml",
        "make audit-validation-evidence-quality",
        f"make feature-closeout-audit manifest=.agents/feature-manifests/{slug}-manifest.yaml",
        f"make closeout-report manifest=.agents/feature-manifests/{slug}-manifest.yaml",
    ]
    expected_docs = [
        "AGENTS.md",
        "docs/codex-fast-path.md",
        "docs/feature-delivery-workflow.md",
        "docs/documentation-sync-policy.md",
        "docs/change-completion-checklist.md",
        "docs/agent-operating-model.md",
        "docs/agent-operating-model.yaml",
    ]
    expected_artifacts = ["Generated agent-operating-model and resolver-reported artifacts."]
    expected_tests = ["Agent operating model validation and agent-safety audits."]
    open_questions = ["If the batch is broad, split it into a master plan and narrower child plans before substantial edits."]

manifest_path_text = (
    f".agents/feature-manifests/{slug}-manifest.yaml"
    if manifest_decision == "required"
    else "create only if the resolver or actual scope requires it"
)

plan_text = plan_path.read_text()
plan_text = plan_text.replace("# Feature Implementation Plan", f"# {title} Plan")
plan_text = plan_text.replace("- Feature tier: TBD", f"- Feature tier: {workflow_tier}")
plan_text = plan_text.replace("- Scope: TBD", f"- Scope: {scope}")
plan_text = plan_text.replace("- Out of scope: TBD", f"- Out of scope: {out_of_scope}")
plan_text = plan_text.replace("- Manifest decision: TBD", f"- Manifest decision: {manifest_decision}")
plan_text = plan_text.replace("- Manifest path: TBD", f"- Manifest path: {manifest_path_text}")
plan_text = plan_text.replace("- Context commands: TBD", "- Context commands: " + "; ".join(context_commands))
plan_text = plan_text.replace("- Routing commands: TBD", "- Routing commands: " + "; ".join(routing_commands))
plan_text = plan_text.replace("- Validation commands: TBD", "- Validation commands: " + "; ".join(validation_commands))
plan_text = plan_text.replace("- Closeout commands: TBD", "- Closeout commands: " + "; ".join(closeout_commands))
plan_text = plan_text.replace("- Expected docs: TBD", "- Expected docs: " + "; ".join(expected_docs))
plan_text = plan_text.replace("- Expected generated artifacts: TBD", "- Expected generated artifacts: " + "; ".join(expected_artifacts))
plan_text = plan_text.replace("- Expected tests: TBD", "- Expected tests: " + "; ".join(expected_tests))
plan_text = plan_text.replace("- Resolver outputs still needed: TBD", "- Resolver outputs still needed: " + "; ".join(open_questions))
plan_text = plan_text.replace("- Risks or approvals: TBD", "- Risks or approvals: none identified yet")
plan_path.write_text(plan_text)

if manifest_decision == "required":
    manifest_text = manifest_template_path.read_text()
    manifest_text = manifest_text.replace("sample_feature", slug.replace("-", "_"))
    manifest_text = manifest_text.replace("Sample feature completion manifest", title)
    manifest_text = manifest_text.replace("workflowTier: tier3-high-risk-multi-layer", f"workflowTier: {workflow_tier}")
    manifest_text = manifest_text.replace("manifestDecision: required", f"manifestDecision: {manifest_decision}")
    manifest_text = manifest_text.replace("riskTier: medium", f"riskTier: {risk_tier}")
    manifest_text = manifest_text.replace("changeMode: major-change", f"changeMode: {change_mode}")
    manifest_text = manifest_text.replace("changeImpact: logic-drift", f"changeImpact: {change_impact}")
    manifest_text = manifest_text.replace("  - backend-logic", "\n".join(f"  - {profile}" for profile in profiles))
    manifest_text = manifest_text.replace(".agents/sample-feature-plan.md", f".agents/{slug}-plan.md")
    manifest_path.write_text(manifest_text)
PY

echo "Created:"
echo "  $plan_path"
if [[ -f "$manifest_path" ]]; then
  echo "  $manifest_path"
else
  echo "  manifest skipped by default"
fi
