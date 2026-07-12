---
machine_kind: plan
machine_status: complete
machine_closeout_contract: 2
machine_baseline_ref: 0cf598d
machine_delivery_mode: implementation
machine_title: Implementation Closeout Hardening Plan
machine_goal: Close evidence-linkage and legacy-fallback gaps found in the implementation and closeout process review.
---

# Implementation Closeout Hardening Plan

## Status

Complete.

## Workflow Frame

- Feature tier: tier4-agent-tooling-workflow
- Scope: require the plan and manifest to name overlapping changed code paths, require matching baseline evidence, and remove legacy closeout-driver bypass behavior.
- Out of scope: product behavior, feature planning, and replacing the current batch ledger format.
- Manifest decision: required
- Manifest path: .agents/feature-manifests/implementation-closeout-hardening-manifest.yaml
- Master plan: none; this is a bounded hardening follow-up.

## Implementation Checkboxes

- [x] Require implemented plan paths to overlap manifest code paths and the changed baseline diff.
- [x] Require completion-evidence baseline text to match the machine baseline.
- [x] Remove the obsolete closeout-driver report fallback and document the strengthened proof chain.
- [x] Validate rejected and passing completion-audit paths plus agent documentation validation.

## Completion Evidence

- Status: complete
- Baseline ref: `0cf598d`
- Implemented code paths: `scripts/audits/audit-plan-completion.rb` and `scripts/feature-closeout-audit.sh`
- Changed files: the plan completion audit, feature closeout audit, agent validator coverage, closeout policy, and technical and validation documentation.
- Validation evidence: Ruby and Zsh syntax checks; prior completed plan passes the strengthened completion audit; active hardening plan is rejected before closeout; `make generate-agent-operating-model`; `make validation-memory-closeout-card`; `make audit-validation-memory-drift`; `AgentOperatingModelValidationTest`; and `make audit-agent-safety` passed.
- Doc delta summary: documented the exact shared evidence requirement and removal of legacy driver-report evidence.
- Backlog update: no backlog item is expected unless validation discovers a separate issue.
- Residual risk: evidence linkage proves shared declared paths, but it cannot determine whether a human-authored task description fully captures the semantic quality of those changes.
