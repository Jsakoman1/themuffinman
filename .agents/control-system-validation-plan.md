---
machine_kind: plan
machine_status: draft
machine_title: Control System Validation Plan
machine_goal: Reintroduce the minimal checks that keep the planning system honest.
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

- [ ] Slice 1: define the canonical validation commands for the new plan system.
- [ ] Slice 2: ensure the validation-memory and closeout helpers reflect the new minimal scaffold.
- [ ] Slice 3: wire the validation outputs into the control-start and plan-index snapshots.

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

- Status: draft
- Changed files: none yet
- Validation evidence: not run
- Doc delta summary: defines the control validation slice
- Backlog update: none
- Residual risk: validation can become ceremonial if not enforced

