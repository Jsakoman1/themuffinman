---
machine_kind: plan
machine_status: draft
machine_closeout_contract: 2
machine_baseline_ref: 0cf598d
machine_delivery_mode: implementation
machine_title: Vision IA And Presentation Contract Plan
machine_goal: Define one backend-owned, minimal presentation contract for Vision, workspaces, iPhone, and Watch before component or API changes.
---

# Vision IA And Presentation Contract Plan

## Status

Draft. No implementation starts before the state vocabulary and route-ownership matrix are accepted.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: public Vision state vocabulary, copy/density policy, diagnostics boundary, typed Vision-to-workspace handoff, and route ownership for priority journeys.
- Out of scope: component restyling, native application implementation, and new domain mutations.
- Manifest decision: required
- Manifest path: .agents/feature-manifests/vision-ia-presentation-contract-manifest.yaml
- Master plan: .agents/vision-adaptive-mobile-first-master-plan.md
- Depends on: none

## Current Findings

- `VisionRuntimeContextDTO` already carries device, attention, cue, consent, and action-hint data, but the frontend currently exposes much of it directly.
- `shellDefinitions.ts` mixes user-facing copy, phase commentary, and route ownership notes.
- Route inventory reports 18 concrete surfaces, but most workspace routes currently resolve to `SectionHubView.vue` without API links.

## Implementation Checkboxes

- [ ] Produce a state-to-archetype matrix for idle, listening, working, needs-input, review, complete, blocked, and discovery states.
- [ ] Define a compact backend-owned public presentation projection and explicitly classify every current runtime field as public, conditional, native-only, or diagnostic-only.
- [ ] Define copy budgets and a `glance -> act -> inspect` density rule for phone, watch, and web.
- [ ] Define typed Vision handoff input, return target, and workspace ownership for Find Work, My Work, Chat, Profile, Business, Calendar, and guest booking.
- [ ] Capture acceptance scenarios and API contract deltas required by later plans.

## Local Tools And Validation

- Read: `docs/product-vision.md`, `docs/product-memory.md`, `docs/vision-architecture-patterns.md`, Vision endpoint pack, and route-surface inventory.
- Run: `make audit-frontend-route-surfaces`, `make audit-api-contract-drift`, `make audit-documentation`, and `make audit-doc-canonical-phrases`.
- Acceptance: every priority journey has one primary owner, one handoff shape, one public state vocabulary, and no diagnostic field is required for normal rendering.

## Risks And Boundaries

- Do not turn presentation states into a frontend workflow engine; backend services remain authoritative.
- Do not add DTO fields without a defined renderer need from Plans 3-6.
- Resolve route ownership conflicts before Plan 5 adds real route components.

## Completion Evidence

- Status: draft
- Baseline ref: `0cf598d`
- Implemented code paths: TBD
- Changed files: TBD
- Validation evidence: TBD
- Doc delta summary: TBD
- Backlog update: TBD
- Residual risk: TBD
