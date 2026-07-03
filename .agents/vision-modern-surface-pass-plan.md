---
machine_kind: plan
machine_status: unknown
machine_title: Vision Modern Surface Pass Plan
---

# Vision Modern Surface Pass Plan

Purpose: tighten the current `/vision` modern surface so the blank state and active state both read as one adaptive agent surface instead of stacked chrome.

## Workflow Frame

- Feature tier: tier2-frontend-focused
- Scope: `/vision` modern surface hero, prompt dock framing, and small doc reconciliation
- Out of scope: backend orchestration changes, new intents, or new execution paths
- Manifest decision: skipped with reason; this slice changes frontend presentation and small living-doc notes only
- Manifest path: not used

## Routing Snapshot

- Context commands: inspect the current modern vision shell, canvas renderer, prompt dock, and active vision God Plan
- Routing commands: manual routing under the active `vision-modern-prompt` master plan
- Validation commands: `npm run type-check`, `npm run build`
- Closeout commands: `make audit-todo`, `make audit-plan-completion plan=.agents/vision-modern-surface-pass-plan.md`

## Implementation Slices

- [x] Slice 1: remove the redundant blank-state hero copy from the shell and let the prompt dock carry the idle entry surface.
- [x] Slice 2: give active states a clearer hero signal so review, discovery, and clarification feel intentional instead of generic.
- [x] Slice 3: update small vision living-doc notes if the surface behavior meaning changes.

## Validation Plan

- Targeted checks: `npm run type-check`, `npm run build`
- Broader checks: backend tests not expected because this slice is frontend-only

## Closeout Gates

- Required closeout checks: `npm run type-check`, `npm run build`, `make audit-todo`, `make audit-plan-completion plan=.agents/vision-modern-surface-pass-plan.md`

## Completion Evidence

- Status: complete
- Changed files: `VisionSurfaceModernView.vue`, `VisionCanvasRenderer.vue`, `VisionPromptDock.vue`, `docs/vision-status-ledger.md`, `docs/product-memory.md`
- Validation evidence: `npm run type-check` passed; `npm run build` passed
- Doc delta summary: updated the status ledger and product memory to reflect the inline idle composer and mode-specific active hero states
- Residual risk: the context strip is still a separate revealed region inside the same shell, so another pass may still be useful if it continues to read as a secondary window
