# Legacy Route Removal Plan

## Status

Complete.

## Parent

- Master plan: `.agents/legacy-frontend-decommission-master-plan.md`
- Depends on:
  - `.agents/legacy-frontend-inventory-and-gap-plan.md`
  - `.agents/legacy-frontend-freeze-and-boundary-plan.md`
  - `.agents/vision-capability-parity-plan.md`

## Objective

Define the safest route-first contraction sequence so the router, shell, and navigation model can shrink before deeper module deletion begins.

## Current Router And Shell Removal Facts

Observed files:

- [router.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/router.ts)
- [App.vue](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/App.vue)
- [moduleRegistry.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/moduleRegistry.ts)
- [createTopbarNavState.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/components/app/topbar/createTopbarNavState.ts)
- [style.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/style.css)

Key findings:

- the router still exposes alias, redirect, drop-now, bridge, admin, auth, and Vision routes in one flat graph
- `/vision` is already isolated from legacy chrome in `App.vue`
- topbar/module navigation still assumes a multi-module product shell
- `style.css` still imports route-era module styles for business, things, and rides
- route removal can therefore proceed before most deep code pruning, because the entry graph and visual shell are already separable

## Removal Buckets

### Bucket A: Immediate delete candidates

Safe to remove first:

- `/quests`
- `/app-users`

Why:

- both are compatibility redirects, not primary product surfaces
- removing them simplifies the router without touching surviving functionality

Required follow-up:

- remove redirect definitions from `router.ts`
- remove any code or docs that still advertise these entry paths

### Bucket B: Drop-now non-essential modules

Delete as early as possible:

- `/business`
- `/things`
- `/rides`

Why:

- parity plan classifies them as drop instead of migrate
- they are not part of the minimum surviving frontend surface
- their removal cuts router complexity, module imports, and style imports quickly

Required follow-up:

- remove lazy imports from `router.ts`
- remove module registry entries
- remove route-era CSS imports from `style.css`
- leave deeper file deletion to the later code-prune batch

### Bucket C: Legacy entry route replacement

Replace after one explicit switch:

- `/`
- `/work`

Target direction:

- `/` should eventually redirect to `/vision` for normal authenticated users
- `/work` should stop acting as the default authenticated user landing route

Why this is a distinct bucket:

- it changes the primary application entry model
- it must align with auth guard behavior and admin redirect behavior

Delete threshold:

- Vision discovery + create flow accepted as the new default user entry experience

Required follow-up:

- update route guards that still return `/work`
- update login/register post-auth redirects to prefer `/vision` for non-admin users
- ensure admin still lands on `/admin/work` or its later replacement

### Bucket D: Alias-detail cleanup

Delete once alias policy is settled:

- `/quests/:id`

Why:

- it is a duplicate detail alias, not a strategic bridge

Delete threshold:

- either redirect it to `/work/:id` temporarily or remove it entirely when direct quest-detail parity is planned

### Bucket E: Temporary bridge routes

Keep temporarily:

- `/work/:id`
- `/applications/:id`
- `/users/:id`
- `/settings`
- `/chat`

Why:

- parity plan marks these as still carrying meaningful read/action continuity that Vision does not yet own

Removal rule:

- each bridge route must have a named parity replacement before deletion
- bridges must not receive net-new workflow investment

### Bucket F: Admin enclave

Keep outside the first user-facing contraction wave:

- `/admin`
- `/admin/work`
- `/admin/quests`
- `/admin/users`
- `/admin/applications`
- `/admin/circles`
- `/admin/agent`

Why:

- admin is intentionally a separate operator boundary
- deleting user legacy routes should not destabilize operator workflows

Removal rule:

- treat admin simplification as a later, explicitly separate standardization pass

## Ordered Route Contraction Sequence

### Pass 1: Remove pure compatibility noise

Changes:

- remove `/quests`
- remove `/app-users`

Expected effect:

- lower route count with negligible behavioral risk

### Pass 2: Remove drop-now module entries

Changes:

- remove `/business`
- remove `/things`
- remove `/rides`
- remove matching module-registry entries
- remove corresponding route-era style imports

Expected effect:

- immediate router and shell simplification
- less product ambiguity around non-strategic modules

### Pass 3: Switch default user landing from work to Vision

Changes:

- change `/` and non-admin auth redirects from `/work` to `/vision`
- change non-admin unauthorized/admin fallback returns that still point to `/work`

Expected effect:

- Vision becomes the default authenticated user runtime
- `/work` becomes an explicit temporary legacy route rather than the main landing page

### Pass 4: Remove duplicate quest aliases

Changes:

- remove `/quests/:id`

Expected effect:

- lower duplicate detail-entry debt

### Pass 5: Collapse user route set to Vision plus explicit bridges

Changes:

- remove `/work` once Vision default entry is accepted
- keep only `/vision`, auth routes, temporary bridges, and admin routes

Expected effect:

- the router stops advertising the legacy module app as a primary user runtime

## Required Guard And Redirect Changes

When the route removal implementation begins, update these router behaviors together:

1. `to.meta.requiresAdmin && !isAdmin()` should no longer bounce to `/work`; it should bounce to the Vision-first authenticated entry.
2. `isLoggedIn() && (to.path === "/login" || to.path === "/register")` should redirect non-admin users to `/vision`, not `/work`.
3. `/` should stop redirecting to `/work` once Pass 3 starts.
4. the admin shortcut redirect can remain temporarily because admin is outside the first user contraction wave.

## Files Most Likely To Change During Implementation

- [router.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/router.ts)
- [App.vue](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/App.vue)
- [moduleRegistry.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/modules/moduleRegistry.ts)
- [createTopbarNavState.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/components/app/topbar/createTopbarNavState.ts)
- [style.css](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/style.css)

## Validation Strategy For The Removal Implementation

Each removal pass should prove:

1. auth entry still works
2. non-admin users land in Vision
3. admin users still reach the admin enclave
4. no removed route remains linked from the topbar/module registry
5. `npm run type-check` passes
6. `npm run build` passes

## Completion Note

The route-first contraction sequence is now explicit: remove redirects and drop-now modules first, then switch the default user landing to Vision, then keep only named detail/chat bridges plus the separate admin enclave until deeper parity work closes them.
