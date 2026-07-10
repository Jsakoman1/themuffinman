---
machine_kind: plan
machine_status: complete
machine_title: Vision UI Layout Plan
machine_goal: Convert the `/vision` surface into a true two-column desktop layout
  with the terminal on the left and a contextual preview rail on the right, while
  preserving a clean stacked fallback on smaller screens.
---

# Vision UI Layout Plan

## Status

Completed.

## Goal

Convert the `/vision` surface into a true two-column desktop layout with the terminal on the left and a contextual preview rail on the right, while preserving a clean stacked fallback on smaller screens.

## Scope

- Included: route-shell grid, terminal column sizing, preview rail placement, and mobile fallback behavior.
- Excluded: backend behavior, worklist redesign, and copy rewriting.

## Steps

1. Replace the current overlay-based preview positioning with a stable two-column layout.
2. Keep the terminal visually dominant and readable as the primary surface.
3. Preserve a mobile fallback that stacks the preview below or above the terminal without changing the information hierarchy.

## Validation

- `npm run type-check`
- `npm run build`

## Completion

- The `/vision` route now uses a stable terminal-left and preview-right layout.
- Mobile fallback preserves the same information hierarchy in a stacked flow.
