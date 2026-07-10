---
machine_kind: plan
machine_status: unknown
machine_title: Vision Capability Expansion Plan
---

# Vision Capability Expansion Plan

Purpose: expand `/vision` one capability family at a time.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: capability routing, DTO mapping, validators, executor adapters
- Out of scope: arbitrary new surface redesign
- Manifest decision: required
- Manifest path: `.agents/feature-manifests/vision-capability-expansion-manifest.yaml`

## Implementation Slices

- [x] Slice 1: finish the next capability routing batch behind typed contracts and route-catalog checks.
- [x] Slice 2: harden the corresponding entity resolver, DTO mapping, and executor adapter path.
- [x] Slice 3: add integration tests for the capability family and its review/clarification flow.

## Validation Plan

- Targeted checks: capability-specific service tests and scenario tests
- Broader checks: vision conversation regression coverage
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/vision-status-ledger.md`, `docs/vision-architecture-patterns.md`, `docs/domain-technical.md`
- Expected generated artifacts: route catalog updates, feature manifest updates, and any related test inventory updates

## Closeout Gates

- Required closeout checks: new capability appears in the route catalog, DTO mapping, executor wiring, and tests
- Final response evidence: the capability is available without compromising the semantic boundary or the backend-first execution rules

## Open Questions

- Resolver outputs still needed: which capability family should follow the current mutation pilots next
- Risks or approvals: none

## Completion Evidence

- Status: completed
- Changed files: vision route catalog, prompt router, conversation service, capability preview service, frontend preview maps, generated prompt contract, and regression tests
- Validation evidence: targeted backend regression tests passed, full backend test suite passed, frontend type-check passed, and frontend build passed
- Doc delta summary: the next capability family now routes through a typed read-only quest-news preview path
- Backlog update: none
- Residual risk: broader regression coverage still needs to confirm the new capability does not drift from the semantic boundary
