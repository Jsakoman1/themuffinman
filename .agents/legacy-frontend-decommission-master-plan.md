# Legacy Frontend Decommission Master Plan

## Status

Active.

## Goal

Remove the current multi-route legacy frontend and converge development onto a Vision-first frontend model while the product is still in dev phase and no real users depend on the legacy screens.

The target end state is:

- `/vision` as the primary and eventually only authenticated user runtime
- no continued investment in legacy module screens, dashboards, or route-specific frontend state systems
- backend-owned workflow logic preserved and reused through Vision instead of duplicated in route-specific Vue surfaces

## Why This Plan Exists

The current frontend still carries a classic multi-route application shape:

- work dashboard and quest pages
- social/circles/profile pages
- business/things/rides pages
- chat workspace page
- admin overview/admin data pages
- admin agent page
- settings page

That route stack is now mostly a development artifact, not a product requirement.

Given the current reality:

- legacy frontend is not used by real users
- `/vision` already owns the strategic interaction model
- backend orchestration, prompt understanding, slot collection, review, execution planning, and execution adapters are increasingly Vision-owned
- keeping the old frontend alive adds drag, duplicate state logic, route complexity, and cleanup cost

the correct move is to treat the legacy frontend as removable infrastructure, not as a permanent parallel surface.

## Parent Context

- Vision God Plan: `.agents/god-plans/vision-god-plan.md`
- Vision machine-readable plan: `.agents/god-plans/vision-god-plan.yaml`
- Related master plans:
  - `.agents/vision-adaptive-architecture-master-plan.md`
  - `.agents/agent-surface-standardization-master-plan.md`
  - `.agents/vision-modern-prompt-master-plan.md`

## Scope

- Included:
  - route inventory and decommission sequencing
  - legacy view/module freeze policy
  - frontend dependency graph reduction
  - migration requirements for flows that must survive in Vision
  - deletion batches for routes, pages, components, composables, styles, and obsolete API wrappers
  - backend and docs cleanup caused by frontend removal
  - validation strategy for a Vision-only or Vision-dominant frontend
- Excluded:
  - replacing all product capabilities in one unsafe batch without capability triage
  - backend business-rule rewrites that are unrelated to frontend removal
  - production rollout or user migration work, because this is still dev-phase

## Current Route Inventory

Current router file: [router.ts](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/frontend/src/router.ts)

Authenticated legacy user routes:

- `/work`
- `/quests`
- `/circles`
- `/work/:id`
- `/quests/:id`
- `/applications/:id`
- `/users/:id`
- `/settings`
- `/business`
- `/things`
- `/rides`
- `/chat`

Authenticated admin legacy routes:

- `/admin/work`
- `/admin/quests`
- `/admin/users`
- `/admin/applications`
- `/admin/circles`
- `/admin/agent`

Vision route:

- `/vision`

Auth bootstrap routes:

- `/login`
- `/register`

Redirect routes:

- `/`
- `/admin`
- `/app-users`

## Current Frontend Surface Inventory

Primary legacy module roots:

- `frontend/src/modules/workmarket`
- `frontend/src/modules/social`
- `frontend/src/modules/business`
- `frontend/src/modules/things`
- `frontend/src/modules/rides`
- `frontend/src/modules/chat`
- `frontend/src/components/app`
- large shared route-era style files under `frontend/src/styles/`

Primary Vision root:

- `frontend/src/modules/vision`

Primary likely survivors even after legacy decommission:

- app bootstrap: `App.vue`, `main.ts`, auth gate, HTTP client, generated contracts
- minimal route shell
- `modules/vision`
- possibly a minimal auth surface

## Locked Strategic Decisions

1. Legacy frontend enters `maintenance-only` mode immediately.
2. No new product capability should be added only to legacy routes.
3. Vision is the target runtime for all normal authenticated user interactions.
4. Any legacy screen that still exposes unique capability must be evaluated as:
   - migrate to Vision
   - replace with a minimal temporary non-legacy bridge
   - explicitly drop as non-essential
5. Route-specific frontend business logic should be treated as debt to remove, not as a structure to preserve.

## Required Decommission Decisions

This plan must explicitly settle these before final delete batches:

1. Auth bootstrap surface
- Keep `login` and `register` as minimal standalone pages, or fold them into a Vision-compatible shell.

2. Admin operator surface
- Either preserve a narrow admin operator/runtime surface temporarily, or migrate `/admin/agent` and any essential admin actions into a Vision-adjacent admin surface before broad deletion.

3. Quest detail and profile detail access
- Decide whether these survive temporarily as dedicated read routes or are fully absorbed into Vision result/review/detail blocks.

4. Chat access
- Decide whether the standalone `/chat` page survives temporarily or whether chat is considered Vision-only once `OPEN_CHAT` and follow-up chat flows become sufficient.

5. Kill-switch threshold
- Define the minimum set of capabilities Vision must support before deleting each legacy route family.

## Child Plans

1. `.agents/legacy-frontend-inventory-and-gap-plan.md`
- Role: produce the precise dependency map of legacy routes, modules, API wrappers, shared components, style files, and generated contracts.
- Deliverables:
  - route-to-module inventory
  - module-to-endpoint inventory
  - “unique capability still only in legacy” list
  - “safe to delete immediately” list
- Status: complete

2. `.agents/legacy-frontend-freeze-and-boundary-plan.md`
- Role: enforce the no-new-logic boundary and identify the minimal surviving frontend surface during transition.
- Deliverables:
  - explicit freeze policy in docs/plans
  - allowed surviving surface list
  - temporary bridge rules for auth/admin/detail/chat if needed
- Status: complete

3. `.agents/vision-capability-parity-plan.md`
- Role: define the minimum Vision capability set required to replace each legacy route family.
- Deliverables:
  - parity matrix for workmarket/social/chat/admin/business/things/rides
  - hard blockers vs optional parity
  - delete threshold per route family
- Status: complete

4. `.agents/legacy-route-removal-plan.md`
- Role: remove legacy routes and module entrypoints in controlled route-family batches.
- Deliverables:
  - router simplification
  - dead lazy imports removed
  - stale nav/topbar/app-shell features removed
  - fallback redirects rewritten around Vision
- Status: complete

5. `.agents/legacy-module-code-prune-plan.md`
- Role: delete orphaned legacy pages, views, components, composables, styles, and API wrappers after route removal.
- Deliverables:
  - module-by-module delete batches
  - shared utility retention vs removal audit
  - final dead-code cleanup
- Status: complete

6. `.agents/legacy-backend-surface-cleanup-plan.md`
- Role: remove or downgrade backend/frontend contracts that only existed to serve deleted legacy screens.
- Deliverables:
  - endpoint audit for frontend-only dashboard/admin surfaces
  - DTO/read-model cleanup list
  - doc and agent-operating cleanup for removed surfaces
- Status: complete

## Decommission Phases

### Phase 0: Freeze And Inventory

Objective:
- stop legacy expansion
- map exactly what exists

Tasks:
- freeze new feature work on legacy routes
- capture route inventory and ownership
- capture module and shared-style inventory
- capture endpoint/dependency map
- tag each route/module as:
  - delete now
  - migrate first
  - preserve temporarily

Exit criteria:
- full inventory exists
- no unresolved ambiguity about what counts as “legacy”

### Phase 1: Minimum Surviving Frontend Definition

Objective:
- define what remains while deletion is in progress

Candidate minimum surface:

- minimal app bootstrap
- auth entry
- `/vision`
- optional temporary admin operator surface

Tasks:
- decide whether `/login` and `/register` remain standalone
- decide whether `/admin/agent` remains temporarily
- decide whether any read-only detail routes survive during transition
- document the temporary survival set

Exit criteria:
- one explicit answer for each survival decision

### Phase 2: Capability Parity Thresholds

Objective:
- avoid deleting useful dev workflows blindly

Tasks:
- map each legacy route family to the Vision capability that must replace it
- separate “must-have for continued development” from “nice to have”
- explicitly mark modules that can be dropped instead of migrated

Suggested route-family buckets:

- workmarket dashboard and quest detail
- social/circles/profile/settings
- chat workspace
- admin operator/admin data pages
- business
- things
- rides

Exit criteria:
- parity matrix exists with delete thresholds

### Phase 3: Router And Shell Contraction

Objective:
- remove route-level complexity first

Tasks:
- simplify `frontend/src/router.ts`
- remove dead redirects
- remove legacy app-shell navigation and topbar features that only serve deleted routes
- redirect former module entry routes into Vision or temporary bridge routes

Exit criteria:
- router only exposes surviving surfaces
- dead lazy imports are gone

### Phase 4: Legacy Module Removal

Objective:
- remove the code behind deleted routes

Tasks:
- delete obsolete pages/views/components/composables per module
- prune `components/app` if app-shell chrome is no longer needed
- remove module-specific legacy style sheets
- delete dead frontend API wrappers
- remove shared helpers that no longer have call sites

Exit criteria:
- legacy modules no longer exist except explicitly preserved temporary surfaces

### Phase 5: Backend Contract Cleanup

Objective:
- stop carrying backend/frontend contracts created only for old screens

Tasks:
- audit dashboard/admin/read endpoints that only existed for legacy views
- remove or de-prioritize obsolete DTOs and frontend-oriented read models
- update docs and agent-operating artifacts so removed surfaces are no longer treated as active

Exit criteria:
- backend no longer exposes stale frontend support surfaces without reason

### Phase 6: Vision-Only Validation Pass

Objective:
- verify the repository is coherent after decommission

Tasks:
- validate route boot flow
- validate auth entry flow
- validate `/vision` text, voice, review, execution, resume, and admin/operator paths that still remain
- run dead-code and build checks

Exit criteria:
- project builds cleanly
- removed surfaces do not leak through routes, imports, docs, or contracts

## Practical Delete Order

Recommended order, from safest to riskiest:

1. Freeze policy and inventory
2. Remove unused redirects and duplicate aliases
3. Remove unused non-Vision module nav/topbar chrome
4. Remove route families already marked “drop instead of migrate”
5. Keep only temporary bridges for must-have dev flows
6. Migrate final must-have flows into Vision
7. Delete the remaining legacy module roots
8. Prune backend contracts and docs

## Success Criteria

The master plan is complete when:

- the router no longer treats legacy modules as first-class product surfaces
- no new product work depends on route-specific legacy UI
- Vision is the only strategic authenticated user surface
- any temporary surviving non-Vision surface is explicitly documented and justified
- obsolete legacy modules, styles, app shell features, and API wrappers are removed
- related backend/frontend/docs artifacts are reconciled

## Risks

- deleting route families too early can remove dev-only workflows that Vision still lacks
- app-shell/shared-component cleanup can accidentally remove dependencies Vision still uses
- backend contract cleanup can break admin or detail tooling if frontend dependency mapping is incomplete
- preserving too many temporary bridges can quietly recreate the legacy app under a new name

## Mitigations

- perform route-family parity gating before deletion
- delete in route-first, module-second order
- keep one explicit “temporary survivors” list
- require targeted validation after each delete batch
- update docs and God Plan status with every major contraction pass

## Validation

- Targeted checks:
  - frontend route smoke validation
  - `npm run type-check`
  - `npm run build`
  - targeted backend tests for any touched Vision/admin/auth flows
- Broader checks:
  - `git diff --check`
  - dead-code or audit helpers when available
  - documentation sync across Vision and planning surfaces
- Closeout checks:
  - `make audit-todo`
  - `make audit-plan-completion plan=.agents/legacy-frontend-decommission-master-plan.md`

## Completion Evidence

- Status: active
- Child plan status: all six child plans complete; route contraction, shell simplification, and drop-now module pruning are in progress
- Validation evidence: `npm run type-check`, `npm run build`, `git diff --check`, and YAML validation for `.agents/god-plans/vision-god-plan.yaml` passed after the route and shell cleanup batches
- Doc delta summary: the full planning pass showed that the decommission hinge is the shared route-era shell plus the workmarket API aggregation layer, and the implementation batches have now removed `/quests`, `/app-users`, `/business`, `/things`, and `/rides` from the active user route graph while also dropping the global chat tray, module nav shell, placeholder module surface, `/work` landing page, and the last explicit legacy shell CSS imports from the legacy entry model
- Deferred work: if any temporary non-Vision bridge survives, it must be tracked explicitly as deferred removal rather than silently accepted as the new normal
