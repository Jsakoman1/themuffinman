---
machine_kind: plan
machine_status: draft
machine_closeout_contract: 2
machine_baseline_ref: 0cf598d
machine_delivery_mode: implementation
machine_title: Vision Shared Design System Plan
machine_goal: Build a small shared presentation system that supports calm, device-aware surfaces without flattening module-specific tasks.
---

# Vision Shared Design System Plan

## Status

Draft. Starts only after Plan 1 defines public states and density rules.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: central tokens, shell/layout primitives, state/action primitives, accessibility and responsive density behavior.
- Out of scope: route-specific read models, Vision orchestration, and module business logic.
- Manifest decision: required
- Manifest path: .agents/feature-manifests/vision-shared-design-system-manifest.yaml
- Master plan: .agents/vision-adaptive-mobile-first-master-plan.md
- Depends on: Plan 1

## Current Findings

- `VisionTerminalRow` and `SurfaceContentView` currently carry more than one presentation responsibility.
- Shared shell and route registry exist, but descriptions and generic cards make unrelated modules look the same.
- Frontend duplication audit found no mutation/workflow overlap, so this plan must preserve that thin-client separation.

## Implementation Checkboxes

- [ ] Inventory existing CSS tokens, type, spacing, elevation, motion, safe-area, focus, and error/empty/loading patterns.
- [ ] Create central tokens and density modes for phone, watch companion, and web without introducing hardcoded module variants.
- [ ] Implement shared primitives: app frame, compact state strip, action row, entity row, section header, contextual disclosure, review strip, and state surfaces.
- [ ] Separate display-only components from Vision-specific conversation components and retire duplicate visual patterns.
- [ ] Define accessibility contracts for focus, reduced motion, contrast, dynamic text, and touch targets.

## Local Tools And Validation

- Read: `shellRouteRegistry.ts`, `shellDefinitions.ts`, `SurfaceContentView.vue`, Vision components, and existing frontend CSS.
- Run: `make audit-style-token-usage`, `make audit-frontend-usage-graph`, `npm run type-check`, `npm run build`.
- Acceptance: one centrally owned token system; no business rule moves into components; primitives work at narrow and desktop widths.

## Risks And Boundaries

- Standardize primitives and behavior, not every module layout.
- Avoid a generic card library that recreates the current text-heavy shell.
- Plan 3 and Plan 5 consume these primitives; they do not redefine them.

## Completion Evidence

- Status: draft
- Baseline ref: `0cf598d`
- Implemented code paths: TBD
- Changed files: TBD
- Validation evidence: TBD
- Doc delta summary: TBD
- Backlog update: TBD
- Residual risk: TBD
