# Vision Legacy Migration Candidate Map

## Status

Draft.

## Purpose

Record which remaining legacy frontend route families can realistically move into the Vision runtime, which ones belong in the separate admin playground, and which ones should stay temporarily as bridges.

## Source Of Truth

- `docs/product-vision.md`
- `docs/vision-architecture-patterns.md`
- `docs/vision-status-ledger.md`
- `.agents/vision-capability-parity-plan.md`
- `.agents/legacy-frontend-decommission-master-plan.md`

## Decision Boundary

- Vision is user-scoped.
- Admin Playground is admin-scoped.
- User-scoped legacy screens can move to Vision when the canvas can express the same intent, review, result, or detail flow.
- Admin-scoped legacy screens should move to the admin playground, not into user Vision.

## Candidate Map

### 1. Strong Vision Candidates

- `/work`
  - target: Vision discovery and create flow
  - status: already partially covered by create_quest, discovery, and execution planning

- `/quests`
  - target: Vision discovery
  - status: can likely be removed once `/work` no longer needs the alias

- `/work/:id`
  - target: Vision quest detail / review continuation
  - status: strong candidate for the next Vision detail slice

- `/quests/:id`
  - target: same as `/work/:id`
  - status: alias bridge, should disappear with the main detail migration

- `/applications/:id`
  - target: Vision application detail / review surface
  - status: strong candidate because Vision already has review and application-oriented blocks

- `/chat`
  - target: Vision open-chat and chat continuation
  - status: medium candidate; the open-chat boundary exists, but the standalone workspace may still be useful during transition

### 2. Vision-Or-Bridge Candidates

- `/users/:id`
  - target: Vision profile / social detail surface
  - status: can move only if Vision gains a profile-style detail view or if we keep a temporary bridge

- `/settings`
  - target: Vision account/profile settings surface
  - status: can move only after profile-edit and settings editing are represented in Vision

- `/circles`
  - target: Vision social context surface
  - status: not yet a clean Vision fit without a dedicated social capability slice

### 3. Not Vision Candidates

- `/admin/work`
- `/admin/quests`
- `/admin/users`
- `/admin/applications`
- `/admin/circles`
- `/admin/agent`
  - target: Admin Playground or a separate admin operator runtime
  - status: keep out of user Vision because the authority boundary is explicit

- `/business`
- `/things`
- `/rides`
  - target: product decision pending
  - status: these are not currently supported by Vision and are better treated as drop-later or separate-module migration work

## Recommended Next Migration Order

1. Finish Vision quest detail continuation so `/work/:id` and `/quests/:id` can disappear.
2. Add Vision application detail continuation so `/applications/:id` can disappear.
3. Add a minimal Vision profile/account surface so `/users/:id` and `/settings` can be reduced to temporary bridges.
4. Decide whether `/chat` should remain a bridge or become a Vision-native continuation surface.
5. Keep admin pages on the separate admin playground track.

## Notes

- The current Vision surface already covers create_quest, discovery, review, result summaries, open-chat routing, voice input/output, and resumable conversations.
- The current legacy frontend still contains unique detail and account surfaces that are not yet fully replaceable by the Vision runtime.
- Removing the legacy frontend completely will require a second user-scoped detail/account slice plus a separate admin operator migration track.
