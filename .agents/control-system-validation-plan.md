---
machine_kind: plan
machine_status: unknown
machine_title: Control System Validation Plan
---

# Control System Validation Plan

Purpose: define the smallest useful validation and closeout set for the rebuilt control plane.

## Workflow Frame

- Feature tier: tier4-agent-tooling-workflow
- Scope: validation commands, closeout commands, failure signals
- Out of scope: full historical audit replay
- Manifest decision: required
- Manifest path: `.agents/feature-manifests/control-system-validation-manifest.yaml`

## Implementation Slices

- [x] Slice 1: define the canonical validation commands for the new plan system.
- [x] Slice 2: ensure the validation-memory and closeout helpers reflect the new minimal scaffold.
- [x] Slice 3: wire the validation outputs into the control-start and plan-index snapshots.

## Validation Plan

- Targeted checks: `make audit-plan-completion`, `make audit-validation-evidence-quality`
- Broader checks: `make feature-closeout-audit`, `make closeout-report`
- Skipped checks or reasons: none once the rebuilt scaffold is active.

## Docs and Artifacts

- Expected docs: `docs/validation-memory.md`, `docs/validation-memory.json`, `docs/change-completion-checklist.md`
- Expected generated artifacts: validation closeout summaries and control-start snapshots

## Closeout Gates

- Required closeout checks: plan completion audit passes, closeout rule is explicit, validation memory is current
- Final response evidence: the control plane can validate itself without old history

## Open Questions

- Resolver outputs still needed: whether a compact validator checklist should be surfaced in a separate generated note
- Risks or approvals: none

## Completion Evidence

- Status: complete
- Changed files: `.agents/control-system-validation-plan.md`, `docs/generated/local-tooling/audit-summary-index.json`, `docs/generated/local-tooling/audit-summary-index.md`, `docs/generated/local-tooling/control-start.json`, `docs/generated/local-tooling/control-start-summary.md`
- Validation evidence: `make audit-validation-memory-drift`, `make validation-memory-closeout-card`
- Doc delta summary: validation checks now surface through the compact control snapshot and the closeout workflow
- Backlog update: none
- Residual risk: validation can become ceremonial if not enforced
