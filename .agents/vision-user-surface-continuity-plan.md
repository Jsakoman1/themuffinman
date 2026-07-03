---
machine_kind: plan
machine_status: complete
machine_title: Vision User Surface Continuity Plan
machine_goal: Move the remaining user-scoped legacy route families into Vision so
  profile, settings, circles, and chat no longer depend on the route-era shell.
---

# Vision User Surface Continuity Plan

## Status

Completed.

## Goal

Move the remaining user-scoped legacy route families into Vision so profile, settings, circles, and chat no longer depend on the route-era shell.

## Scope

- Add Vision-native routes for profile, settings, circles, and chat.
- Redirect the legacy user routes into the Vision routes.
- Remove the old route view entry files once the new routes validate.
- Update route-linked navigation helpers to use the new Vision paths.

## Validation

- `npm run type-check`
- `npm run build`
- `git diff --check`

## Result

- Vision-native routes now cover profile, settings, circles, and chat.
- The legacy user-surface view entry files were removed from the route graph.
