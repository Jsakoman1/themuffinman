---
machine_kind: plan
machine_status: complete
machine_title: Vision Guided Task Flow Coordination Plan
machine_goal: Coordinate the quest, business booking, side-job search, and person
  outreach guided-flow slices.
---

# Vision Guided Task Flow Coordination Plan

## Status

Complete.

## Goal

Coordinate the quest, business booking, side-job search, and person outreach guided-flow slices.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: shared guidance conventions, flow ordering, cross-flow consistency, and validation alignment
- Out of scope: new domain models
- Manifest decision: required when implementation starts
- Manifest path: TBD
- Master plan: `.agents/vision-next-experience-master-plan.md`

## Implementation Slices

- [x] Slice 1: keep the quest, business booking, side-job search, and person outreach flows aligned on shared next-step conventions.
- [x] Slice 2: keep cross-flow validation and docs aligned when one guided slice changes shared semantics.
- [x] Slice 3: record any shared guidance gaps that should become new child-plan work or persistent backlog items.

## Validation Plan

- Targeted checks: coordination checks across the four child plans
- Broader checks: scenario tests that span multiple guided flows
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/product-vision.md`, `docs/vision-architecture-patterns.md`, `docs/vision-status-ledger.md`, `docs/domain-technical.md`, `docs/business-logic.md`
- Expected generated artifacts: route catalog updates, contract refreshes, and any scenario inventory updates
- Temporary work products: task-flow matrix and interaction notes under `.agents/tmp/`

## Closeout Gates

- Required closeout checks: the four focused guided-flow plans all stay aligned on the same backend conventions
- Final response evidence: create work, business scheduling, side-job search, and person outreach still feel like one conversation system
- Backlog follow-up rule: shared guidance ambiguities become persistent backlog items before closeout

## Open Questions

- Resolver outputs still needed: whether any shared helper should be promoted into a backend service after the four child plans settle
- Risks or approvals: none yet

## Completion Evidence

- Status: complete
- Changed files:
  - `apps/themuffinman/frontend/src/modules/app-shell/visionHandoff.ts`
  - `apps/themuffinman/frontend/src/modules/app-shell/shellDefinitions.ts`
  - `docs/vision-status-ledger.md`
  - `docs/implementation-backlog.md`
- Validation evidence:
  - `npm run type-check`
  - `npm run build`
  - `make audit-generated-artifact-freshness`
- Doc delta summary:
  - shared quest, business booking, side-job search, and person outreach guidance now uses a tighter common prompt language
  - the remaining business booking-intent gap is recorded as a durable backlog item
- Backlog update:
  - added `VISION-BUSINESS-BOOKING-INTENT-01`
- Residual risk:
  - business booking still lacks a dedicated Vision booking-intent family and depends on shared business read surfaces for now
