---
machine_kind: plan
machine_status: draft
machine_closeout_contract: 2
machine_baseline_ref: 0cf598d
machine_delivery_mode: implementation
machine_title: Web Workspace Modernization Plan
machine_goal: Replace explanatory generic shell pages with compact, route-owned workspace archetypes backed by real read APIs.
---

# Web Workspace Modernization Plan

## Status

Draft. Starts after route ownership and shared primitives are approved.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: shell simplification, route-owned workspace views, priority route migration, public business and booking entry flow, and removal of hardcoded demo actions.
- Out of scope: frontend-owned workflow decisions and any API/read model not required by a defined route.
- Manifest decision: required
- Manifest path: .agents/feature-manifests/web-workspace-modernization-manifest.yaml
- Master plan: .agents/vision-adaptive-mobile-first-master-plan.md
- Depends on: Plans 1-2

## Current Findings

- Route inventory shows 18 concrete routes but most use `SectionHubView.vue` with no API client or endpoint link.
- `shellDefinitions.ts` contains architecture/phase commentary and fixed entity examples that should not ship as product UI.
- Current route registry and `visionHandoff.ts` are useful central migration points.

## Implementation Checkboxes

- [ ] Replace shell copy with compact navigation, current location, one contextual action, and account access.
- [ ] Define route-owned archetypes for work list, inbox/thread, schedule timeline, booking queue, business identity/offering, profile/settings, and public business booking.
- [ ] Deliver routes in dependency order: Home/My Work and Find Work; Chat; Profile; owner Business; public Business and guest booking; Calendar lens.
- [ ] Connect each migrated route only to backend-prepared sections/actions; use Plan 6 when a required read model is absent.
- [ ] Remove generic cards, fixed IDs, placeholder action text, duplicate Vision launch explanations, and superseded `SectionHubView` ownership route by route.

## Local Tools And Validation

- Read: `shellDefinitions.ts`, `shellRouteRegistry.ts`, `shellSurfaceData.ts`, `userShellApi.ts`, route inventory, and domain endpoint packs.
- Run: `make audit-frontend-route-surfaces`, `make audit-frontend-usage-graph`, `npm run validate:contracts`, `npm run type-check`, `npm run build`, and authenticated route smoke checks.
- Acceptance: every migrated priority route has a dedicated surface, a real API/read-model link, a clear empty/loading/error state, and one compact Vision handoff where useful.

## Risks And Boundaries

- Do not replace one generic component with many unshared one-off layouts.
- Do not create a parallel detail/mutation workflow outside Vision where Plan 1 assigns Vision ownership.
- Public booking must not depend on owner-shell authentication or frontend permission inference.

## Completion Evidence

- Status: draft
- Baseline ref: `0cf598d`
- Implemented code paths: TBD
- Changed files: TBD
- Validation evidence: TBD
- Doc delta summary: TBD
- Backlog update: TBD
- Residual risk: TBD
