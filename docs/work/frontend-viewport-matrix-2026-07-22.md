# Frontend viewport matrix — 2026-07-22

Status: reviewed during the P3 viewport task before CSS consolidation.

The matrix is intentionally a review artifact: it records the target behavior and the evidence dimensions that the browser walkthrough must confirm.

| Viewport | Shared shell expectation | Collection/detail expectation | Evidence target |
|---|---|---|---|
| 1440px | Full app shell, context navigation, comfortable rail width | Two-column detail/context surfaces where the view supports them | Desktop screenshot and browser JSON |
| 980px | Full shell with reduced content width | Two-column surfaces remain usable; actions may wrap | Tablet screenshot and browser JSON |
| 700px | Shell remains navigable without horizontal overflow | Detail/context surfaces may stack; dialog utility rail stacks below body | Compact tablet screenshot and browser JSON |
| 390px | Header actions remain reachable and do not collide | Single-column content, wrapped form actions, no clipped surface rows | Mobile screenshot and browser JSON |

## Review rules

- A breakpoint is retained only when it protects readable content, reachable actions, or a valid two-column-to-stack transition.
- A view-specific breakpoint without a documented reason is a consolidation candidate, not an automatic removal.
- Browser evidence records overflow, clipped controls, duplicate navigation, and surface stacking separately.

## Latest browser result

The authenticated walkthrough passed at all four widths and on `/work`, `/chat`, `/things`, and `/rides`: no horizontal overflow, no page errors, and no real request failures. Expected route-change aborts are recorded separately in `docs/runtime-evidence/frontend-viewport-matrix-2026-07-22.json`.
