---
machine_kind: plan
machine_status: complete
machine_title: Vision Validation And Ledger Plan
machine_goal: Keep docs, ledgers, generated artifacts, and tests synchronized while
  the next Vision experience lands.
---

# Vision Validation And Ledger Plan

## Status

Complete.

## Goal

Keep docs, ledgers, generated artifacts, and tests synchronized while the next Vision experience lands.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: docs sync, ledger updates, generated contracts, validation evidence
- Out of scope: feature redesign
- Manifest decision: required when implementation starts
- Manifest path: TBD
- Master plan: `.agents/vision-next-experience-master-plan.md`

## Implementation Slices

- [x] Slice 1: update the vision durable docs and status ledger for the new runtime contract.
- [x] Slice 2: keep generated contracts and frontend artifacts in sync with the cockpit/audio changes.
- [x] Slice 3: close out validation and capture any follow-up work that was discovered.

## Validation Plan

- Targeted checks: the contract, the cockpit, and the audio/runtime slices each get focused tests
- Broader checks: backend suite, frontend type-check, frontend build
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/product-vision.md`, `docs/vision-architecture-patterns.md`, `docs/vision-status-ledger.md`, `docs/business-logic.md`, `docs/domain-technical.md`
- Expected generated artifacts: generated contract output, any machine-operational docs that change, and any route inventories that drift
- Temporary work products: validation matrix and docs-delta notes under `.agents/tmp/`

## Closeout Gates

- Required closeout checks: the ledger matches the implemented surface and the docs describe the same interaction model
- Final response evidence: all required validation commands passed or were explicitly skipped with recorded reasons
- Backlog follow-up rule: any open risk or deferred follow-up is recorded before closeout

## Open Questions

- Resolver outputs still needed: whether the closeout should also refresh any generated mobile-view notes
- Risks or approvals: none yet

## Completion Evidence

- Status: complete
- Changed files:
  - `docs/product-vision.md`
  - `docs/vision-architecture-patterns.md`
  - `docs/vision-status-ledger.md`
  - `docs/implementation-backlog.md`
  - `docs/generated/source-of-truth-audit.json`
  - `docs/generated/local-tooling/plan-index.json`
  - `docs/generated/local-tooling/plan-index-summary.md`
  - `docs/generated/local-tooling/control-start-summary.md`
- Validation evidence:
  - `./mvnw test -Dtest=VisionConversationSnapshotSupportTest,VisionCanvasAssemblerTest,VisionConversationServiceTest`
  - `npm run type-check`
  - `npm run build`
  - `make audit-generated-artifact-freshness`
- Doc delta summary:
  - business read surfaces are now explicitly recorded in the Vision ledger
  - the runtime contract, cockpit, and shared guidance changes are reflected in the durable docs
- Backlog update:
  - added `VISION-BUSINESS-BOOKING-INTENT-01` to capture the remaining dedicated booking-intent follow-up
- Residual risk:
  - dedicated business booking guidance still uses shared read surfaces and route prompts rather than its own Vision booking-intent family
