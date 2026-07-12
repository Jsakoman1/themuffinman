---
machine_kind: plan
machine_status: draft
machine_closeout_contract: 2
machine_baseline_ref: 0cf598d
machine_delivery_mode: implementation
machine_title: Adaptive Surface Migration And Closeout Plan
machine_goal: Migrate approved Vision and workspace slices safely, remove superseded surfaces, and prove accessibility, performance, and cross-route consistency.
---

# Adaptive Surface Migration And Closeout Plan

## Status

Draft. This is the final program slice and starts only when Plans 1-6 are independently complete.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: route-by-route migration, feature flags where needed, removal of obsolete support/placeholder UI, accessibility, performance, device smoke, and final documentation consistency.
- Out of scope: new product capabilities outside the seven-plan program.
- Manifest decision: required
- Manifest path: .agents/feature-manifests/adaptive-surface-migration-closeout-manifest.yaml
- Master plan: .agents/vision-adaptive-mobile-first-master-plan.md
- Depends on: Plans 1-6

## Current Findings

- Vision still has duplicated support/debug/preview surfaces and workspace routes still share generic shell rendering.
- Existing generated contracts, route audits, frontend graph, and plan closeout controls can provide migration evidence.
- The product needs visual validation on authenticated web, narrow phone viewport, and Watch contract fixtures, not screenshot-only acceptance.

## Implementation Checkboxes

- [ ] Create a migration order and feature-flag/rollback plan for each route family and Vision surface replacement.
- [ ] Migrate one independently usable route family at a time, then delete its superseded placeholder/support surface and stale route registry content.
- [ ] Run accessibility checks for keyboard, screen reader, contrast, reduced motion, dynamic text, touch targets, consent, and voice failure states.
- [ ] Run performance and consistency checks for empty/loading/error, navigation return paths, device density, generated contracts, and API callsites.
- [ ] Complete final product, technical, Vision architecture, operating-model, regression, and generated artifact synchronization before master closeout.

## Local Tools And Validation

- Run: `make audit-frontend-route-surfaces`, `make audit-frontend-state-logic-duplication`, `make audit-frontend-usage-graph`, `make audit-frontend-dead-code`, `make audit-api-contract-drift`, `npm run type-check`, `npm run build`, backend/API suites, and device/route smoke.
- Run final: manifest-backed closeout, generated artifact freshness, accessibility evidence, and master child completion audit.
- Acceptance: no route family relies on removed support or placeholder UI; one end-to-end priority journey works across Vision, workspace, phone policy, and Watch handoff.

## Risks And Boundaries

- Do not delay deletion indefinitely; every replacement must name the obsolete files/routes it removes.
- Preserve rollback only behind explicit flags or independently working route boundaries, never duplicate business logic.
- Master cannot close until this plan and every prior child audit pass.

## Completion Evidence

- Status: draft
- Baseline ref: `0cf598d`
- Implemented code paths: TBD
- Changed files: TBD
- Validation evidence: TBD
- Doc delta summary: TBD
- Backlog update: TBD
- Residual risk: TBD
