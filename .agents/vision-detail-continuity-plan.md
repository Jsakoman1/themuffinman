# Vision Detail Continuity Plan

## Status

In progress.

## Goal

Move the remaining user-scoped quest and application detail entry points into Vision so the legacy frontend can lose its last important read/detail bridges.

## Scope

- Add Vision-native detail routes for quests and applications.
- Redirect the legacy detail routes into the Vision routes.
- Keep the existing domain data sources and validation logic.
- Preserve current user actions until the new shell is proven.

## Implementation Slices

1. Add Vision detail route wrappers and a thin Vision detail shell.
2. Point quest/application navigation targets at the new Vision routes.
3. Redirect legacy detail routes to the Vision routes.
4. Update vision status and product memory with the new continuity boundary.

## Validation

- `npm run type-check`
- `npm run build`
- `git diff --check`

