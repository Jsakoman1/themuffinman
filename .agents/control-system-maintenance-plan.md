---
machine_kind: plan
machine_status: unknown
machine_title: Control System Maintenance Plan
---

# Control System Maintenance Plan

Purpose: keep the rebuilt control plane trimmed and accurate over time.

## Workflow Frame

- Feature tier: tier4-agent-tooling-workflow
- Scope: cleanup, pruning, index maintenance, status hygiene
- Out of scope: historical archive restoration
- Manifest decision: required
- Manifest path: `.agents/feature-manifests/control-system-maintenance-manifest.yaml`

## Implementation Slices

- [x] Slice 1: define how stale plans are archived or removed.
- [x] Slice 2: keep the open-plan index and plan-index summary aligned with active work.
- [x] Slice 3: prune old bootstrap notes once the first real workstream lands.

## Validation Plan

- Targeted checks: `make plan-index`, `make control-start`
- Broader checks: `make audit-plan-completion`, `make audit-todo`
- Skipped checks or reasons: none.

## Docs and Artifacts

- Expected docs: `docs/program-planning-model.md`, `docs/codex-fast-path.md`
- Expected generated artifacts: plan index, control-start snapshot, and any maintenance notes

## Closeout Gates

- Required closeout checks: stale items are either removed or explicitly archived
- Final response evidence: the control surface stays intentionally small

## Open Questions

- Resolver outputs still needed: whether archive markers should be standardized for future automation
- Risks or approvals: none

## Completion Evidence

- Status: complete
- Changed files: `.agents/control-system-maintenance-plan.md`, `.agents/open-plan-quick-index.md`, `docs/generated/local-tooling/plan-index.json`, `docs/generated/local-tooling/plan-index-summary.md`
- Validation evidence: `make plan-index`, `make control-start`, `make audit-todo`
- Doc delta summary: the compact open-plan routing surface is now current, and the quick index is explicitly treated as a convenience mirror of the generated routing snapshot
- Backlog update: none
- Residual risk: maintenance can drift if it is not periodically executed
