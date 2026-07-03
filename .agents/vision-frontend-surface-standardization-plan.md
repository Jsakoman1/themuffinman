---
machine_kind: plan
machine_status: unknown
machine_title: Vision Frontend Surface Standardization Plan
machine_goal: Replace the current route-level all-in-one implementation with a stable
  structure that is easier to extend toward the long-term blank-canvas vision surface.
---

# Vision Frontend Surface Standardization Plan

## Scope

Standardize the current `/vision` frontend surface around reusable components that match the backend-driven architecture already established in the conversation API.

## Goal

Replace the current route-level all-in-one implementation with a stable structure that is easier to extend toward the long-term blank-canvas vision surface.

## Locked Decisions

- Backend response shape stays authoritative.
- This batch does not change the `/vision` backend contract.
- The route view should become a thin shell.
- The first extracted reusable surfaces are:
  - animated agent component
  - canvas renderer
  - prompt dock
- Review actions and block rendering stay backend-driven.

## Planned Work

1. Extract an animated agent component from the route view.
2. Extract a canvas renderer that consumes existing `VisionCanvasBlock[]`.
3. Extract a prompt dock that owns the visible prompt-entry UI only.
4. Keep interaction handlers in the composable and route shell.
5. Update vision docs so the actual frontend shape matches the architecture guide.

## Validation

- `npm run type-check`
- `npm run build`

## Closeout Rule

This plan is complete only when the route view is materially thinner, the extracted components are reusable, and the frontend still validates without backend contract changes.

## Completion Evidence

- Status: complete
- Changed files: `frontend/src/modules/vision/views/VisionSurfaceModernView.vue`, `frontend/src/modules/vision/composables/useVisionSurfaceState.ts`, `docs/vision-status-ledger.md`, `docs/product-memory.md`
- Validation evidence: `npm run type-check` passed; `npm run build` passed
- Doc delta summary: moved shell-level surface state into a reusable composable so the route view reads as a thinner adaptive shell
- Residual risk: the remaining context and continuation experience still needs broader long-session hardening in later slices
