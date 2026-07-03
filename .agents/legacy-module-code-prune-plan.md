---
machine_kind: plan
machine_status: complete
machine_title: Legacy Module Code Prune Plan
machine_goal: Define the post-route-removal delete batches for legacy frontend code
  so dead pages, components, styles, composables, and API wrappers are removed in
  a controlled and auditable order.
---

# Legacy Module Code Prune Plan

## Status

Complete.

## Parent

- Master plan: `.agents/legacy-frontend-decommission-master-plan.md`
- Depends on:
  - `.agents/legacy-frontend-inventory-and-gap-plan.md`
  - `.agents/legacy-route-removal-plan.md`

## Objective

Define the post-route-removal delete batches for legacy frontend code so dead pages, components, styles, composables, and API wrappers are removed in a controlled and auditable order.

## Prune Principle

Delete by dependency cone, not by folder name alone.

That means:

1. remove route entrypoints first
2. remove shell/navigation dependencies second
3. remove module-specific views/components/composables third
4. remove dead API wrappers and styles last within each batch

This avoids false deletions while temporary bridges and admin enclaves still exist.

## Prune Batches

### Batch 1: Drop-now module code

Delete after route removal of `/business`, `/things`, and `/rides`:

- [modules/business](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/business)
- [modules/things](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/things)
- [modules/rides](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/rides)
- [business.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/styles/business.css)
- [things.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/styles/things.css)
- [rides.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/styles/rides.css)

Reason:

- these modules are classified as drop instead of migrate
- they offer the highest code reduction with the lowest parity dependency

### Batch 2: Legacy shell cluster

Delete after top-level route contraction leaves Vision as default:

- [AppTopbar.vue](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/components/app/AppTopbar.vue)
- [AppNotificationsPanel.vue](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/components/app/AppNotificationsPanel.vue)
- [AppChatTray.vue](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/components/app/AppChatTray.vue)
- [AppModuleNav.vue](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/components/app/AppModuleNav.vue)
- [useAppTopbarState.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/components/app/useAppTopbarState.ts)
- [createTopbarNavState.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/components/app/topbar/createTopbarNavState.ts)
- [useTopbarMenus.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/components/app/topbar/useTopbarMenus.ts)
- [useTopbarNotifications.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/components/app/topbar/useTopbarNotifications.ts)
- [moduleRegistry.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/moduleRegistry.ts)
- [topbar.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/styles/topbar.css)
- [notifications.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/styles/notifications.css)
- [modules.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/styles/modules.css)

Reason:

- this is the main route-era product shell
- once the router no longer presents a multi-module user runtime, this cluster becomes pure debt

### Batch 3: Legacy work entry surfaces

Delete after `/work` and duplicate aliases are removed:

- legacy work dashboard pages and supporting composables that only exist for the route-era entry flow
- dashboard-only styles such as:
  - [dashboard-layout.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/styles/dashboard-layout.css)
  - [dashboard-components.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/styles/dashboard-components.css)

Keep temporarily if still referenced:

- admin pages
- detail views
- any shared UI used by surviving bridges

Reason:

- workmarket code is not one delete batch; it must split between route-era dashboard code and still-live admin/detail dependencies

### Batch 4: Social and chat bridge cleanup

Delete only after Vision parity closes the corresponding bridges:

- [modules/social/views/CirclesView.vue](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/social/views/CirclesView.vue)
- [modules/social/views/UserProfileView.vue](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/social/views/UserProfileView.vue)
- [modules/social/views/UserSettingsView.vue](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/social/views/UserSettingsView.vue)
- [modules/social/components](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/social/components)
- [modules/social/composables](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/social/composables)
- [modules/chat/views/ChatWorkspaceView.vue](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/chat/views/ChatWorkspaceView.vue)
- [modules/chat/composables/useAppChat.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/chat/composables/useAppChat.ts)
- [circles.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/styles/circles.css)
- [profile.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/styles/profile.css)
- [chat.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/styles/chat.css)
- [chat-module.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/styles/chat-module.css)

Keep separately:

- [modules/social/pages/AdminCirclesPage.vue](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue) while admin enclave survives
- [modules/chat/api/chatApi.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/chat/api/chatApi.ts) if still used by Vision or a temporary bridge

### Batch 5: Workmarket API aggregation split

Refactor before delete:

- [workmarketApi.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/workmarket/api/workmarketApi.ts)

Likely delete or shrink heavily:

- dashboard clients
- route-era user page clients
- legacy circles/user/profile helpers that remain only because of the old app shell

Likely temporary survivors:

- detail-related clients still backing bridge routes

Reason:

- `workmarketApi.ts` is the highest-risk frontend delete surface because it mixes removable and surviving concerns

## Shared Style Audit Rules

Before deleting any remaining stylesheet, prove one of these:

1. the route and components it served are already gone
2. Vision does not import or visually depend on it
3. admin enclave does not import or visually depend on it

Candidate later-delete styles once bridges collapse:

- [detail-surfaces.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/styles/detail-surfaces.css)
- [dialogs.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/styles/dialogs.css)
- [calendar.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/styles/calendar.css)

## Validation Rules For Each Prune Batch

1. `rg` should show no remaining imports of deleted files
2. `npm run type-check` must pass
3. `npm run build` must pass
4. admin and temporary bridge routes must still render if they were intentionally preserved

## Completion Note

The prune strategy is now explicit: drop-now modules first, legacy shell second, route-era work entry code third, social/chat bridges fourth, and the mixed workmarket API aggregation layer only after surviving admin/detail dependencies are split cleanly.
