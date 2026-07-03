---
machine_kind: plan
machine_status: complete
machine_title: Legacy Frontend Freeze And Boundary Plan
machine_goal: Lock the transition boundary between the strategic Vision runtime and
  the route-era legacy frontend so the remaining decommission work can proceed without
  accidental reinvestment in the old application shell.
---

# Legacy Frontend Freeze And Boundary Plan

## Status

Complete.

## Parent

- Master plan: `.agents/legacy-frontend-decommission-master-plan.md`
- Vision God Plan: `.agents/god-plans/vision-god-plan.md`

## Objective

Lock the transition boundary between the strategic Vision runtime and the route-era legacy frontend so the remaining decommission work can proceed without accidental reinvestment in the old application shell.

## Current Boundary Findings

### 1. Auth bootstrap is already small and portable

Observed files:

- [auth.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/auth.ts)
- [modules/identity/auth.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/identity/auth.ts)
- [sessionService.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/services/sessionService.ts)

Findings:

- frontend auth state is already concentrated in `sessionService.ts`
- `frontend/src/auth.ts` is only a re-export bridge into the identity module
- the runtime contract is small: current user, token, login, logout, auth header, admin check
- this surface is not coupled to the legacy topbar, dashboard, or route-era app shell

Boundary decision:

- keep auth/session bootstrap as a product-level shared runtime surface
- do not treat auth bootstrap as legacy
- allow internal refactors only when they reduce coupling to old route-era pages

### 2. Vision is already separated from legacy chrome

Observed file:

- [App.vue](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/App.vue)

Findings:

- `showLegacyChrome` is disabled for `/vision`
- `AppTopbar`, `AppChatTray`, and the footer do not mount on the Vision route
- the session refresh bootstrap in `App.vue` is generic and should survive

Boundary decision:

- preserve the bootstrap role of `App.vue`
- treat the topbar, chat tray, and footer as legacy chrome
- no new Vision capability may depend on `AppTopbar`, `AppChatTray`, or route-era footer behavior

### 3. Legacy chrome is a frozen shell cluster

Observed files:

- [useAppTopbarState.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/components/app/useAppTopbarState.ts)
- [router.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/router.ts)

Findings:

- topbar state owns polling, notifications, profile navigation, settings navigation, and logout entrypoints
- topbar navigation still assumes module-based route navigation
- legacy shell value is operational convenience only, not strategic product direction

Boundary decision:

- classify `frontend/src/components/app/**`, `frontend/src/modules/moduleRegistry.ts`, and route-era shell styling as frozen legacy shell infrastructure
- permit only defect fixes that unblock existing login, admin access, or temporary detail-bridge flows
- forbid any new user workflow, discovery pattern, or navigation investment in this shell

### 4. Keep vision-specific surfaces separate from the route-era shell

Boundary decision:

- preserve only the minimum route-era shell needed to keep login and transition flows working
- do not use shell survival as justification for keeping user-facing legacy shell/module flows

## Freeze Policy

Effective immediately, the following rules apply to the route-era frontend:

1. No new product capability may launch only on legacy user routes.
2. No new shared UI primitives may be introduced under `frontend/src/components/app` for user-facing product work.
3. No new route-era navigation, module tabs, dashboard panels, or shell orchestration may be added.
4. Legacy pages may receive only:
   - bug fixes required to keep dev workflows functional during migration
   - compatibility fixes required for auth/bootstrap/admin continuity
   - delete-preparation refactors that reduce coupling or isolate surviving code
5. Any new user-facing capability must target Vision first, even if a temporary fallback route still exists.

## Allowed Surviving Frontend Surface During Transition

### Strategic keep

- app bootstrap and mount flow
- session persistence and token bootstrap
- HTTP client and auth header helpers
- identity auth views needed for login/register
- `modules/vision/**`

### Temporary keep

- `/admin/**` operator routes, especially `/admin/agent`
- minimal read/detail bridges only if Vision parity is not ready yet:
  - `/work/:id`
  - `/applications/:id`
  - `/users/:id`
  - `/settings`
  - `/chat`

### Explicitly legacy and non-strategic

- `/work`
- `/quests`
- `/circles`
- `/business`
- `/things`
- `/rides`
- module-registry navigation model
- topbar notifications workspace
- floating chat tray shell behavior

## Temporary Bridge Rules

### Auth bridge

- `login` and `register` remain allowed until a Vision-compatible auth shell exists
- auth code may be simplified, but not expanded into another route-era shell investment

### Admin bridge

- admin routes remain operational while backend agent/admin workflows are still stabilizing
- admin work should converge toward a standardized operator surface, not toward the legacy user shell

### Detail bridge

- dedicated detail routes may remain only where Vision cannot yet present equivalent read/review/edit depth
- once Vision detail parity exists for a route family, the legacy detail route becomes a delete candidate immediately

### Chat bridge

- `/chat` may survive only as a temporary operator/read surface until Vision-native chat continuation is sufficient
- no new feature work should target the standalone chat workspace first

## Minimum Surviving Frontend Definition

If the decommission proceeds successfully, the minimum acceptable interim frontend is:

- `App.vue` bootstrap without reliance on legacy chrome for Vision
- auth entry pages
- `/vision`
- optional `/admin/**` enclave
- a very small number of explicit temporary detail bridges, if still required

This is the boundary that all remaining decommission plans should optimize toward.

## Implications For Next Plans

1. The capability-parity plan should treat `/work`, `/circles`, `/business`, `/things`, and `/rides` as replace-or-drop surfaces, not preserve surfaces.
2. The route-removal plan should delete alias and navigation debt early because it is outside the allowed survivor set.
3. The code-prune plan should target the legacy chrome cluster as a first-class removal batch after route thresholds are satisfied.
4. The backend cleanup plan should split truly shared auth/admin contracts from dashboard-era read models so the frontend deletion does not drag shared runtime concerns with it.

## Completion Note

The boundary is now explicit: shared auth bootstrap survives, Vision is strategic, admin is a temporary operator enclave, and the route-era user shell is frozen legacy infrastructure awaiting parity-based removal.
