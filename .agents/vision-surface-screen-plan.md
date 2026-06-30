# Vision Surface Screen Plan

Purpose: add one experimental long-term screen that applies the product vision principles while keeping the current legacy routes active.

## Goal

Create a full-screen adaptive canvas route with parallel audio and visual feedback, dynamic result filtering, and a playful high-contrast visual language.

## Scope

- `apps/themuffinman/frontend/src/router.ts`
- `apps/themuffinman/frontend/src/modules/moduleRegistry.ts`
- `apps/themuffinman/frontend/src/modules/vision/views/VisionSurfaceView.vue`
- `apps/themuffinman/frontend/src/styles/vision-surface.css`
- `apps/themuffinman/frontend/src/style.css`
- `docs/business-logic.md`
- `docs/domain-technical.md`

## Checklist

- [x] Add a new authenticated route for the experimental vision surface.
- [x] Build one adaptive full-screen surface that demonstrates voice states, filters, and curated result presentation.
- [x] Keep legacy module routes active alongside the new screen.
- [x] Update living docs to reflect the experimental screen.

## Validation

- `npm --prefix apps/themuffinman/frontend run type-check`
- `npm --prefix apps/themuffinman/frontend run build`
- `make audit-documentation`

## Completion Evidence

- Status: complete
- Execution status: complete
- Persistent backlog item: none
- Validation evidence:
  - `npm --prefix apps/themuffinman/frontend run type-check`
  - `npm --prefix apps/themuffinman/frontend run build`
  - `make audit-documentation`
