# Vision Blank Canvas 001 Plan

## Goal

Deliver the first concrete `/vision` blank-canvas slice so the route shell feels quieter, more adaptive, and less like a normal page with visible chrome.

## Scope

1. remove persistent route-level chrome that keeps the surface feeling like a standard page
2. keep the animated agent as the visual anchor
3. move status and recent-task context into secondary reveal surfaces instead of always-open sections
4. keep the prompt dock as the main structured input surface
5. avoid backend contract changes in this slice

## Locked Decisions

- backend-owned canvas blocks remain the source of task meaning
- this slice is frontend-shell work, not a new executor or orchestration change
- blank-canvas behavior should hide support context until it is useful
- recent tasks remain available, but should not dominate the default visual state

## Validation

- `cd apps/themuffinman/frontend && npm run type-check`
- `cd apps/themuffinman/frontend && npm run build`

## Expected Follow-Ons

- stronger adaptive reveal/hide behavior tied to canvas mode
- more atmospheric orb/canvas motion
- eventual convergence toward one legacy-free vision surface
