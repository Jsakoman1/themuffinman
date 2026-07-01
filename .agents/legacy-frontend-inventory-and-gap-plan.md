# Legacy Frontend Inventory And Gap Plan

## Status

Complete.

## Goal

Produce the precise dependency map of legacy routes, modules, API wrappers, shared components, style files, and generated contracts so legacy frontend decommission can proceed by route family instead of guesswork.

## Findings Summary

The frontend is already split into two broad worlds:

- strategic runtime: `frontend/src/modules/vision`
- route-era application shell: everything else

The route-era shell still owns:

- topbar/module navigation
- chat tray
- notifications panel
- work dashboard
- quest/app/profile/admin detail pages
- business/things/rides standalone module screens

The legacy boundary is not cleanly module-scoped. The heaviest coupling points are:

- `frontend/src/router.ts`
- `frontend/src/App.vue`
- `frontend/src/components/app/*`
- `frontend/src/modules/workmarket/api/workmarketApi.ts`
- `frontend/src/modules/workmarket/api/clients/*`

That means decommission should happen in this order:

1. route and shell contraction
2. module removal
3. backend contract cleanup

not module deletion first.

## Route To Module Inventory

Source router: [router.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/router.ts)

### Legacy User Routes

- `/work`
  - module: `modules/workmarket/pages/QuestsPage.vue`
  - dependency type: heavy dashboard/state surface
  - class: migrate first

- `/quests`
  - redirect alias to `/work`
  - class: delete now after redirect policy decision

- `/circles`
  - module: `modules/social/views/CirclesView.vue`
  - dependency type: standalone social management surface
  - class: migrate first

- `/work/:id`
  - module: `modules/workmarket/views/QuestDetailView.vue`
  - dependency type: quest detail/read+action surface
  - class: preserve temporarily or migrate first

- `/quests/:id`
  - alias of quest detail
  - class: delete now after detail-route policy decision

- `/applications/:id`
  - module: `modules/workmarket/views/ApplicationDetailView.vue`
  - dependency type: application detail/read+action surface
  - class: migrate first

- `/users/:id`
  - module: `modules/social/views/UserProfileView.vue`
  - dependency type: profile read/action surface
  - class: preserve temporarily or migrate first

- `/settings`
  - module: `modules/social/views/UserSettingsView.vue`
  - dependency type: authenticated account/profile settings
  - class: preserve temporarily or migrate first

- `/business`
  - module: `modules/business/views/BusinessHubView.vue`
  - dependency type: standalone module screen
  - class: drop or migrate later

- `/things`
  - module: `modules/things/views/ThingSharingView.vue`
  - dependency type: standalone module screen
  - class: drop or migrate later

- `/rides`
  - module: `modules/rides/views/RideSharingView.vue`
  - dependency type: standalone module screen
  - class: drop or migrate later

- `/chat`
  - module: `modules/chat/views/ChatWorkspaceView.vue`
  - dependency type: standalone chat workspace
  - class: preserve temporarily or migrate first

### Legacy Admin Routes

- `/admin/work`
  - module: `modules/workmarket/pages/AdminOverviewPage.vue`
  - class: preserve temporarily

- `/admin/quests`
  - module: `modules/workmarket/pages/AdminQuestsPage.vue`
  - class: preserve temporarily

- `/admin/users`
  - module: `modules/workmarket/pages/AdminUsersPage.vue`
  - class: preserve temporarily

- `/admin/applications`
  - module: `modules/workmarket/pages/AdminApplicationsPage.vue`
  - class: preserve temporarily

- `/admin/circles`
  - module: `modules/social/pages/AdminCirclesPage.vue`
  - class: preserve temporarily

- `/admin/agent`
  - module: `modules/workmarket/pages/AdminAgentPage.vue`
  - class: preserve temporarily and likely keep longest

### Vision Route

- `/vision`
  - module: `modules/vision/views/VisionSurfaceModernView.vue`
  - dependency type: strategic runtime
  - class: keep

### Auth Routes

- `/login`
  - module: `modules/identity/views/LoginView.vue`
  - class: keep as minimum surviving surface

- `/register`
  - module: `modules/identity/views/RegisterView.vue`
  - class: keep as minimum surviving surface

### Redirect Routes

- `/`
  - redirects to `/work`
  - class: rewrite during decommission

- `/admin`
  - redirects to `/admin/work`
  - class: preserve temporarily

- `/app-users`
  - redirects to `/admin/users`
  - class: delete now after admin redirect cleanup

## Module To Endpoint Inventory

### Vision Module

API root: `modules/vision/api/visionApi.ts`

Endpoints:

- `POST /vision/conversations/turns`
- `POST /vision/conversations/{conversationId}/reset`
- `POST /vision/conversations/{conversationId}/cancel`
- `GET /vision/conversations/recent`
- `GET /vision/conversations/{conversationId}`
- `GET /dashboard/me/voice-config`
- `POST /dashboard/me/voice/transcribe`
- `POST /dashboard/me/voice/speak`

Assessment:

- strategic keep
- already aligned with product direction

### Identity Module

API root: `modules/identity/api/authApi.ts`

Endpoints:

- `POST /auth/login`
- `POST /auth/register`
- `GET /auth/me`

Assessment:

- minimum surviving surface
- should stay even in a Vision-dominant frontend unless auth is redesigned

### Workmarket Legacy Module

API aggregation root: `modules/workmarket/api/workmarketApi.ts`

Underlying endpoint families:

- dashboard
  - `/dashboard/me`
  - `/dashboard/me/summary`
  - old dashboard voice/vision prompt endpoints
- quests
  - `/quests`
  - `/quests/search`
  - `/quests/presets/*`
  - `/quests/{id}`
  - `/quests/{id}/detail`
  - mutation endpoints for create/update/delete/start/complete/term confirm/reject
- applications
  - quest application mutation/admin endpoints
- circles
  - `/circles/*`
- users/profile
  - `/app_users/*`
- location
  - `/location/*`
- news/notifications
  - `/news/me*`
- admin agent/admin data
  - `/admin/agent/*`
  - `/admin/applications`
  - `/circles/admin/overview`
  - `/location/admin/status`

Assessment:

- largest legacy dependency hub
- contains both removable legacy surface dependencies and reusable backend capability clients
- must be split before deletion into:
  - keep-temporary admin/operator clients
  - keep-temporary auth/profile/detail clients
  - removable dashboard-era clients

### Social Legacy Module

API shape:

- no dedicated single `socialApi.ts`
- depends on workmarket-era clients for:
  - circles
  - user profile view
  - current app-user settings/profile data

Assessment:

- social legacy routes are coupled through workmarket API clients
- decommission work must untangle these from workmarket naming and dashboard assumptions

### Chat Legacy Module

API root: `modules/chat/api/chatApi.ts`

Endpoints:

- `GET /chat/workspace`
- `POST /chat/conversations/open`
- `GET /chat/conversations/{id}/messages`
- `POST /chat/conversations/{id}/messages`
- `PATCH /chat/conversations/{id}/read`
- `POST /chat/presence/heartbeat`

Assessment:

- standalone route may be removable later
- backend chat capability itself is still needed
- likely temporary survivor until Vision chat coverage becomes sufficient

### Business Module

API root: `modules/business/api/businessApi.ts`

Endpoints:

- `GET /business/profiles`
- `GET /business/profiles/{slug}`
- `GET /business/profiles/me`
- `PUT /business/profiles/me`

Assessment:

- standalone module route only
- no sign of Vision parity yet
- likely drop-or-defer candidate rather than immediate migration priority

### Things Module

API root: `modules/things/api/thingsApi.ts`

Endpoints:

- `GET /things/listings`
- `GET /things/listings/me`
- `POST /things/listings`
- `POST /things/listings/{id}/borrow-requests`

Assessment:

- standalone module route only
- likely drop-or-defer candidate

### Rides Module

API root: `modules/rides/api/ridesApi.ts`

Endpoints:

- `GET /rides/offers`
- `GET /rides/offers/me`
- `POST /rides/offers`

Assessment:

- standalone module route only
- likely drop-or-defer candidate

## Shared Legacy Shell Dependencies

These are not module-local and will block deletion if left untreated:

- `frontend/src/App.vue`
  - toggles legacy chrome via route path checks
  - mounts `AppTopbar` and `AppChatTray`

- `frontend/src/components/app/AppTopbar.vue`
- `frontend/src/components/app/AppModuleNav.vue`
- `frontend/src/components/app/AppNotificationsPanel.vue`
- `frontend/src/components/app/AppChatTray.vue`
- `frontend/src/components/app/useAppTopbarState.ts`
- `frontend/src/components/app/topbar/*`
- `frontend/src/modules/moduleRegistry.ts`

Assessment:

- this is the main legacy shell cluster
- once non-Vision routes are removed, most of this becomes dead immediately

## Style Surface Inventory

Likely legacy-route style debt:

- `styles/topbar.css`
- `styles/dashboard-components.css`
- `styles/dashboard-layout.css`
- `styles/chat.css`
- `styles/chat-module.css`
- `styles/modules.css`
- `styles/business.css`
- `styles/calendar.css`
- `styles/things.css`
- `styles/profile.css`
- `styles/rides.css`
- `styles/circles.css`
- `styles/detail-surfaces.css`
- `styles/dialogs.css`
- `styles/forms-feedback.css`
- `styles/notifications.css`

Likely survivor:

- `styles/vision-surface.css` only if still actually referenced after current scoped Vision CSS review
- base global styles required by app bootstrap

## Unique Capability Still Only In Legacy

These still appear to exist only through legacy non-Vision routes:

- workmarket dashboard tabbed workflow
- quest detail page and application detail page
- standalone user profile view
- standalone settings surface
- standalone chat workspace
- admin overview/admin CRUD pages
- business hub
- things sharing route
- rides route
- topbar notifications workflow
- app chat tray workflow

## Immediate Safe Delete Candidates

These look deletable early once redirect policy is approved:

- `/quests` alias route
- `/quests/:id` alias route if `/work/:id` or Vision detail bridge remains
- `/app-users` redirect route
- `modules/moduleRegistry.ts` after topbar/module-nav removal
- legacy route labels that only exist to support module navigation

These are not safe yet but are likely early delete waves after parity/bridge decisions:

- business/things/rides standalone routes
- dashboard topbar/module navigation shell
- notifications panel if not intentionally preserved

## Temporary Survivor Candidates

Most likely temporary non-Vision survivors during decommission:

- `/login`
- `/register`
- `/admin/agent`
- possibly `/admin/work`, `/admin/users`, `/admin/applications`, `/admin/circles`
- possibly one temporary detail/profile/chat bridge if Vision parity is not immediate

## Recommended Delete Order Based On Inventory

1. Freeze and classify all routes using this inventory.
2. Remove alias/duplicate redirects.
3. Remove module navigation and route-era shell once only temporary survivors remain.
4. Drop business/things/rides if they are not migration priorities.
5. Preserve only auth + Vision + explicit temporary admin/detail/chat bridges.
6. Remove remaining workmarket/social/chat legacy routes after Vision parity slices land.
7. Prune backend contracts that existed only for deleted route-era screens.

## Result

The inventory confirms that legacy frontend decommission is feasible, but the decommission hinge is not the module screens themselves. The hinge is the shared route-era shell and workmarket API aggregation layer.

That means the next child plan should focus on boundary freezing and the minimum surviving frontend surface before any delete batch starts.
