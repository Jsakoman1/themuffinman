---
machine_kind: plan
machine_status: complete
machine_title: Vision Validation Reintegration Plan
---

# Vision Validation Reintegration Plan

Purpose: bring the minimum validation loop back into the rebuilt plan system for Vision work.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: targeted tests, generated artifacts, closeout evidence
- Out of scope: historical manifest replay
- Manifest decision: required
- Manifest path: `.agents/feature-manifests/vision-validation-reintegration-manifest.yaml`

## Implementation Slices

- [x] Slice 1: define the current minimum Vision validation set.
- [x] Slice 2: wire the required generated-artifact refresh commands into the closeout path.
- [x] Slice 3: keep the closeout record aligned with the current Vision surface and the current shared config.

## Validation Plan

- Targeted checks: semantic tests, executor tests, and the current route-focused regression suite
- Broader checks: frontend build and relevant generated-artifact refresh commands
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/vision-status-ledger.md`, `docs/vision-architecture-patterns.md`, `docs/validation-memory.md`, `docs/domain-technical.md`
- Expected generated artifacts: any route catalog, semantic, or closeout artifacts that the current batch touches

## Closeout Gates

- Required closeout checks: validation evidence exists and the ledger matches the implementation state
- Final response evidence: the plan system can safely close Vision slices again without losing validation or generated-artifact updates

## Open Questions

- Resolver outputs still needed: whether the vision validation baseline should be split by capability family
- Risks or approvals: none

## Completion Evidence

- Status: completed
- Changed files: generated backend audit inventory, generated source-of-truth audit, generated frontend contract, generated local-tooling freshness and control-start summaries
- Validation evidence: targeted backend regression tests passed, full backend test suite passed, frontend type-check passed, frontend build passed, and generated artifact freshness audit passed
- Doc delta summary: the minimum Vision validation set is now reattached to the current surface and the closeout freshness loop is back in sync
- Backlog update: none
- Residual risk: future code changes still need the same refresh loop to avoid stale generated artifacts
