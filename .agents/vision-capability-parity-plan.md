---
machine_kind: plan
machine_status: complete
machine_title: Vision Capability Parity Plan
machine_goal: Define the minimum `/vision` capability coverage required to remove
  each legacy route family without deleting still-useful development workflows blindly.
---

# Vision Capability Parity Plan

## Status

Complete.

## Parent

- Master plan: `.agents/legacy-frontend-decommission-master-plan.md`
- Vision God Plan: `.agents/god-plans/vision-god-plan.md`
- Architecture reference: [vision-architecture-patterns.md](/Users/jsakoman/Desktop/themuffinman/docs/vision-architecture-patterns.md)

## Objective

Define the minimum `/vision` capability coverage required to remove each legacy route family without deleting still-useful development workflows blindly.

## Current Vision Capability Baseline

Based on the current `/vision` backend and frontend surfaces, Vision already provides:

- backend-owned prompt understanding with shared semantics
- persisted conversations and resume continuity
- text and voice prompt entry
- `CREATE_QUEST` slot collection
- split schedule collection via `scheduled_date` and `scheduled_time`
- review, typed review edits, and explicit confirmation flow
- typed execution planning for `create_quest`
- feature-flagged `create_quest` execution
- read-only `DISCOVER_QUESTS` routing and ranked quest discovery results
- `OPEN_CHAT` intent routing to the existing chat boundary
- adaptive context/recent-session surface inside one route

Vision does not yet provide broad parity for:

- circles management
- profile and settings management
- application-detail workflows
- quest-detail read depth and mutation follow-up flows
- standalone chat workspace depth
- admin data surfaces
- business/things/rides module behaviors

## Route-Family Parity Matrix

### 1. Workmarket dashboard and entry routes

Legacy surfaces:

- `/work`
- `/quests`

Legacy value today:

- broad quest browsing/discovery entry
- dashboard-style work entrypoint

Current Vision coverage:

- partial
- `DISCOVER_QUESTS` provides a read-only replacement for browse/search intent
- `CREATE_QUEST` provides a mutation path for one important workflow

Still missing for comfortable deletion:

- stronger quest discovery result actions and follow-up navigation
- smoother transition from discovery results into detail/review/action
- explicit support for “show my quests”, “show my applications”, or equivalent personal work summaries if those remain needed

Delete threshold:

- `/quests` alias can be deleted immediately
- `/work` can be deleted once Vision can cover:
  - discover quests
  - create quest
  - at least one coherent follow-up path from discovery to next action

Priority:

- high

Decision:

- replace with Vision, not preserve

### 2. Quest detail and application detail

Legacy surfaces:

- `/work/:id`
- `/quests/:id`
- `/applications/:id`

Legacy value today:

- deeper read detail
- application-specific context and action surfaces

Current Vision coverage:

- low
- discovery exists, but detail/read depth is still legacy-owned

Still missing for comfortable deletion:

- quest detail read block inside Vision
- application detail read block or equivalent review surface
- explicit actions from discovery or follow-up prompts into those details

Delete threshold:

- `/quests/:id` alias can be deleted once redirect policy is finalized
- `/work/:id` and `/applications/:id` should survive temporarily until Vision can:
  - resolve a specific quest or application target
  - render the important read context
  - expose the next allowed action inside the adaptive surface

Priority:

- high

Decision:

- preserve temporarily, then migrate into Vision

### 3. Social, profile, and settings

Legacy surfaces:

- `/circles`
- `/users/:id`
- `/settings`

Legacy value today:

- circles browsing and membership actions
- standalone user profile view
- account/settings editing

Current Vision coverage:

- low
- no meaningful parity yet beyond shared auth/session continuity

Still missing for comfortable deletion:

- read-only profile retrieval inside Vision
- profile/settings edit flows
- circle discovery/membership flows
- social follow-up prompts and target resolution rules

Delete threshold:

- `/circles` should stay until Vision gains at least one intentional social capability slice
- `/users/:id` and `/settings` may survive as temporary bridges until profile/settings flows are implemented in Vision

Priority:

- high

Decision:

- preserve selected detail bridges temporarily; migrate core social/account workflows later

### 4. Chat workspace

Legacy surface:

- `/chat`

Legacy value today:

- full chat workspace and conversation continuation

Current Vision coverage:

- partial
- `OPEN_CHAT` routes intent to the existing chat boundary

Still missing for comfortable deletion:

- Vision-native chat continuation after the initial open action
- message history and send/reply flow inside Vision, or a deliberate standardized bridge

Delete threshold:

- `/chat` can remain as a temporary bridge
- delete only once Vision either:
  - owns message continuation directly, or
  - owns a clean handoff pattern that makes the standalone workspace unnecessary

Priority:

- medium-high

Decision:

- preserve temporarily, but do not expand

### 5. Admin operator and admin data

Legacy surfaces:

- `/admin/work`
- `/admin/quests`
- `/admin/users`
- `/admin/applications`
- `/admin/circles`
- `/admin/agent`

Legacy value today:

- operator visibility
- admin read/detail workflows
- guarded agent playground and execution testing

Current Vision coverage:

- very low for admin data pages
- none for operator dashboards
- shared semantics exist with Admin Playground, but the runtime boundary is intentionally separate

Still missing for comfortable deletion:

- standardized admin operator surface strategy
- explicit parity decision about which admin actions stay page-based versus move to an admin adaptive surface

Delete threshold:

- admin routes are not part of the first user-facing legacy deletion wave
- `/admin/agent` should remain until a replacement admin operator surface exists
- admin overview/data pages can be reduced later based on operator need, not based on user Vision readiness

Priority:

- medium

Decision:

- preserve as separate temporary enclave

### 6. Business, things, and rides

Legacy surfaces:

- `/business`
- `/things`
- `/rides`

Legacy value today:

- placeholder or early module surfaces

Current Vision coverage:

- none

Still missing for comfortable deletion:

- nothing required if product direction accepts dropping these routes for now

Delete threshold:

- these can be dropped without Vision parity if they are confirmed non-essential during dev phase

Priority:

- highest delete leverage, lowest migration need

Decision:

- drop instead of migrate unless product direction revives them

## Hard Blockers Vs Optional Parity

### Hard blockers before broad user-facing legacy contraction

- Vision must remain stable for:
  - create quest
  - quest discovery
  - conversation resume
  - typed review/edit/confirm
  - voice/text prompt entry
- `/work/:id`, `/applications/:id`, `/users/:id`, `/settings`, and `/chat` should not be deleted until equivalent read/action continuity exists

### Optional parity for first major contraction wave

- complete social parity
- full chat-native continuation
- business/things/rides migration
- admin data-page migration

These do not block early route deletion if the routes are explicitly dropped or intentionally preserved as narrow bridges.

## Recommended Deletion Sequence From The Parity View

1. Delete `/quests` immediately.
2. Delete `/app-users` redirect immediately.
3. Delete `/quests/:id` once its alias/redirect policy is settled.
4. Delete `/business`, `/things`, and `/rides` as non-essential legacy module entries.
5. Replace `/work` with `/vision` once discovery-plus-create coverage is accepted as the new entry model.
6. Keep `/work/:id`, `/applications/:id`, `/users/:id`, `/settings`, and `/chat` only as explicit temporary bridges.
7. Migrate or standardize admin separately from the user-facing decommission sequence.

## Implications For The Next Child Plans

1. The route-removal plan should start with aliases, redirects, and drop-now modules instead of touching admin or detail bridges first.
2. The code-prune plan should separate “drop without parity” modules from “bridge until parity” modules.
3. The backend cleanup plan must not remove detail/admin endpoints prematurely, because several temporary bridge routes still depend on them.
4. The Vision adaptive architecture plan should add the next capability slices in this order:
   - quest/detail follow-up depth
   - profile/settings read-edit depth
   - chat continuation depth

## Completion Note

The parity boundary is now explicit: Vision is already strong enough to replace legacy entry and creation flows, not yet strong enough to replace all detail/social/chat/admin surfaces, and several route families should be dropped outright instead of waiting for unnecessary migration work.
