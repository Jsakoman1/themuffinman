---
machine_kind: plan
machine_status: completed
machine_title: Vision Surface Simplification Plan
machine_goal: Keep the frontend calm, thin, and backend-governed as Vision grows.
---

# Vision Surface Simplification Plan

Purpose: keep the Vision frontend readable while the backend semantics expand.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: route shell, canvas blocks, presentation simplification, and preview coherence
- Out of scope: backend capability rules
- Manifest decision: required
- Manifest path: `.agents/feature-manifests/vision-surface-simplification-manifest.yaml`

## Implementation Slices

- [x] Slice 1: keep shell and canvas responsibilities sharply separated.
- [x] Slice 2: centralize shared presentation maps so preview and conversation surfaces do not drift.
- [x] Slice 3: simplify status and preview rendering where backend state already carries the meaning.

## Validation Plan

- Targeted checks: frontend type-check and build
- Broader checks: visual and interaction regressions on the primary route surfaces
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/vision-architecture-patterns.md`, `docs/vision-status-ledger.md`, `docs/domain-technical.md`
- Expected generated artifacts: frontend contract outputs and any route-surface inventories that change

## Closeout Gates

- Required closeout checks: the shell remains thin and the canvas vocabulary stays small
- Final response evidence: the frontend still reads like one calm surface with less duplicated presentation logic

## Open Questions

- Resolver outputs still needed: whether any remaining block types can be collapsed further
- Risks or approvals: none

## Completion Evidence

- Status: completed
- Changed files: frontend vision presentation config and the conversation/preview consumers
- Validation evidence: frontend type-check passed and frontend build passed
- Doc delta summary: the shared presentation maps now live in one frontend module instead of being duplicated, and family resolution now flows through one shared helper
- Backlog update: none
- Residual risk: status and preview rendering can still grow again if future blocks reintroduce duplicated family lookups
