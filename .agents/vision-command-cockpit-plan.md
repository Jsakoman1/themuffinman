---
machine_kind: plan
machine_status: complete
machine_title: Vision Command Cockpit Plan
machine_goal: Reshape the frontend shell into a mobile-first command cockpit with
  terminal, mode control, and morphing preview.
---

# Vision Command Cockpit Plan

## Status

Complete.

## Goal

Reshape the frontend shell into a mobile-first command cockpit with terminal, mode control, and morphing preview.

## Workflow Frame

- Feature tier: tier3-high-risk-multi-layer
- Scope: shell layout, mode selector, session rail, preview placement, responsive density
- Out of scope: backend workflow changes
- Manifest decision: required when implementation starts
- Manifest path: TBD
- Master plan: `.agents/vision-next-experience-master-plan.md`

## Implementation Slices

- [x] Slice 1: make the cockpit layout work on iPhone dimensions first.
- [x] Slice 2: keep the terminal the main control surface while adding explicit mode switching and session anchors.
- [x] Slice 3: keep the morphing preview visible as the system state changes, without turning the surface into a generic dashboard.

## Validation Plan

- Targeted checks: frontend type-check and build
- Broader checks: responsive layout checks on iPhone-like widths
- Skipped checks or reasons: none

## Docs and Artifacts

- Expected docs: `docs/product-vision.md`, `docs/vision-architecture-patterns.md`, `docs/vision-status-ledger.md`
- Expected generated artifacts: generated contract output if the cockpit consumes new fields
- Temporary work products: layout sketch and responsive density notes under `.agents/tmp/`

## Closeout Gates

- Required closeout checks: the shell still reads as one surface, not a stack of unrelated panels
- Final response evidence: the cockpit makes it easier to switch between chat, quest, business, and other modes without leaving `/vision`
- Backlog follow-up rule: any layout/control affordance gap gets recorded before closeout

## Open Questions

- Resolver outputs still needed: exact order of the cockpit controls on small screens
- Risks or approvals: none yet

## Completion Evidence

- Status: complete
- Changed files:
  - `apps/themuffinman/frontend/src/modules/vision/components/VisionCanvasRenderer.vue`
  - `apps/themuffinman/frontend/src/modules/vision/views/VisionSurfaceModernView.vue`
  - `apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts`
- Validation evidence:
  - `npm run type-check`
  - `npm run build`
  - `make audit-generated-artifact-freshness`
- Doc delta summary:
  - `docs/vision-status-ledger.md`
  - `docs/vision-architecture-patterns.md`
- Backlog update: none required for the cockpit slice
- Residual risk: the shell still does not expose a dedicated watch-native surface, only watch-friendly cues and compact mobile density
