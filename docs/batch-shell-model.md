# Batch Shell Model

This file is the compact inventory for the new shell execution model.

## Canonical Shell Surfaces

- Home: `/home`
- Work: `/work`
- Work quest detail: canonical Vision-native detail, with thin hosted list entry on `/work/quests`
- Work application detail: canonical Vision-native detail, with thin hosted list entry on `/work/applications`
- Chat: `/chat`
- Chat thread detail: chat-owned route continuation
- Calendar: `/calendar`
- Business: `/business`
- Profile: `/profile`

## Canonical Ownership Rules

- `apps/themuffinman/frontend/src/modules/app-shell/shellRouteRegistry.ts` owns the route ownership matrix.
- `apps/themuffinman/frontend/src/modules/app-shell/shellSurfaceData.ts` owns the backend-prepared shell summaries.
- `apps/themuffinman/frontend/src/modules/app-shell/visionHandoff.ts` owns the typed shell-to-Vision handoff contract.
- `apps/themuffinman/frontend/src/router.ts` owns the actual route registration.

## Promotion Policy

Top-level navigation should only grow when the surface has:

- a stable backend read model
- a regular user journey
- lower cognitive load than direct Vision entry
- a clean mobile fit

## Archived Legacy References

Older planning artifacts may still appear in completed manifest history, generated audit archives, or retrospective
outputs. Those references are traceability only and are not part of the live planning model.

## Active Batch Plans

None at the moment.
