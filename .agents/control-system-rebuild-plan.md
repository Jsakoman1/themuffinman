---
machine_kind: plan
machine_status: unknown
machine_title: Control System Rebuild Plan
---

# Control System Rebuild Plan

Purpose: restore the minimal control-system workflow after the total reset.

## Workflow Frame

- Feature tier: tier4-agent-tooling-workflow
- Scope: plan lifecycle, active-plan discovery, minimal closeout plumbing
- Out of scope: historical backlog replay, generated-cache archaeology, old manifest restoration
- Manifest decision: required
- Manifest path: `.agents/feature-manifests/control-system-rebuild-manifest.yaml`

## Implementation Slices

- [x] Slice 1: restore active-plan discovery and plan-index generation for the new scaffold.
- [x] Slice 2: restore control-start and the minimum generated local-tooling outputs.
- [x] Slice 3: define the minimal plan lifecycle and completion state transitions.

## Validation Plan

- Targeted checks: `make plan-index`, `make control-start`
- Broader checks: `make audit-plan-completion`, `make audit-todo`
- Skipped checks or reasons: historical cache regeneration is intentionally not restored.

## Docs and Artifacts

- Expected docs: `docs/program-planning-model.md`, `docs/codex-fast-path.md`, `docs/feature-delivery-workflow.md`
- Expected generated artifacts: `docs/generated/local-tooling/plan-index.*`, `docs/generated/local-tooling/control-start.*`

## Closeout Gates

- Required closeout checks: control-start snapshot exists, plan index is regenerated, open-plan index is current
- Final response evidence: minimal control-system surface is discoverable again

## Open Questions

- Resolver outputs still needed: whether a tiny active-plan registry should live in `.agents/tmp/` or only in generated output
- Risks or approvals: none

## Completion Evidence

- Status: complete
- Changed files: `.agents/control-system-rebuild-plan.md`, `docs/generated/local-tooling/plan-index.json`, `docs/generated/local-tooling/plan-index-summary.md`, `docs/generated/local-tooling/control-start.json`, `docs/generated/local-tooling/control-start-summary.md`
- Validation evidence: `make plan-index`, `make control-start`
- Doc delta summary: the rebuilt control-system slice is now discoverable through the compact plan index and control-start snapshot
- Backlog update: none
- Residual risk: system can sprawl again without discipline
