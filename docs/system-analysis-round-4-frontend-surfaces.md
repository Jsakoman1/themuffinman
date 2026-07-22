# System Analysis Round 4 — Frontend App-Shell and Surface Architecture

Status: completed analysis snapshot, 2026-07-22. This round analyzes route ownership, app-shell concentration, API linkage, action authority, state duplication, and frontend recovery surfaces.

## 1. Executive finding

The frontend is not organized as one independent Vue module per product capability. It is a shared authenticated workspace with most product views centralized in `modules/app-shell`, plus focused `identity` and `vision` module boundaries.

This is an intentional architecture: one shell owns navigation, account context, global actions, route metadata, dialogs, detail/utility rails, keyboard behavior, and shared surface primitives. Domain views remain route-owned and use domain APIs. The benefit is a consistent product grammar; the risk is that `app-shell` becomes a second monolith inside the monolith.

```text
router/auth/shell registry
          ↓
AuthenticatedShellView
          ├── shared navigation, search, create, Vision handoff
          ├── shared dialogs, rows, details, actions, status, loading/error
          └── route-owned app-shell views
                 ├── Work
                 ├── Business / Calendar
                 ├── Chat / Activity / Notifications
                 ├── Circles / People / Profile
                 └── Things / Rides

Vision remains a separate blank-canvas surface with typed shell handoffs.
```

## 2. Current frontend topology

The latest route-surface audit reports:

- 45 routes;
- 44 routes with concrete surfaces;
- one redirect route;
- zero placeholder module routes;
- no detached route surfaces.

The router uses native dynamic imports. The eager navigation core is limited to router, auth, and shell route registry. Substantial route components load when their canonical route is entered, and the loading policy explicitly preserves active collection context.

The route architecture has three important classes:

1. **Authenticated shell routes:** `/home`, `/work`, `/chat`, `/calendar`, `/business`, `/circles`, `/things`, `/rides`, `/profile`, and their details.
2. **Identity routes:** login, register, recovery, reset, onboarding.
3. **Vision routes/bridges:** `/vision` plus typed route redirects that carry prompt, autorun, context, source, and return-path metadata.

The generic route audit reports `/vision` with no detected surface because Vision's dynamic/component structure is outside the audit's simple reachable-surface heuristic. The dedicated Vision gateway and source tree confirm that `/vision` is a real route-owned surface, not a missing page. This is a useful example of why audit results require domain interpretation.

## 3. App-shell concentration

`modules/app-shell` contains approximately 82 files and owns most authenticated UI. It includes:

- shell layout and authenticated entry;
- primary/secondary navigation and module rail;
- route/surface definitions;
- user shell and workspace navigation APIs;
- shared buttons, dialogs, form fields, empty/loading/error states;
- action menus and object actions;
- detail surfaces, utility rails, collection toolbars, rows, metrics, rich text;
- keyboard shortcuts and focus handling;
- Vision-for-Web host/assistant bridges;
- domain views for Work, Business, Chat, Circles, People, Profile, Things, and Rides.

The `business`, `chat`, `rides`, and `things` directories under `frontend/src/modules` have little or no independent view implementation at this baseline because their route surfaces are intentionally app-shell-owned. This is not missing module implementation by itself.

The design boundary is healthy when app-shell components provide presentation and interaction primitives while domain views provide route-specific composition. It becomes unhealthy when app-shell begins to own domain validation, permissions, state transitions, or module-specific data-fetch policy.

## 4. Route and surface ownership

The shell route registry defines a canonical ownership matrix. The matrix separates:

- primary navigation surface ID;
- canonical entry route;
- optional canonical detail route;
- module-space rule;
- Vision rule;
- contextual Vision prompt;
- top-level navigation eligibility.

The navigation promotion policy requires a stable backend read model, a regular user journey, lower cognitive load, and fit within compact navigation. One-off utility surfaces, thin experiments, and surfaces without a stable read model are blocked from top-level promotion by default.

This is an important architecture rule: a route can exist without becoming a primary module, and Vision can be available without replacing the canonical Web route. Stable browse/detail ownership stays with Work, Chat, Business, Calendar, and the other domain surfaces; Vision is used for guided, semantic, or cross-module work.

## 5. Backend/API linkage findings

The current endpoint-callsite audit reports:

- 214 backend endpoints;
- 194 frontend client methods;
- 140 linked endpoints;
- 74 endpoints without a detected frontend callsite.

The 74 misses are a review queue, not a dead-code list. The sample includes admin user/application endpoints and the admin-agent execution endpoint, which are not necessarily part of the current public Web workspace. Other legitimate classes include native handoff, backend-only support, Vision transport, and intentionally unexposed operations.

The more useful next classification is:

```text
unlinked endpoint
  → admin-only
  → native/future-client
  → Vision/internal transport
  → backend-only operational/support
  → compatibility/legacy
  → true missing Web callsite
```

Only the final class should create a frontend implementation gap. The audit currently does not know this semantic ownership on its own.

The route audit confirms that active canonical routes have concrete API linkage. Work and shell surfaces show dense endpoint use because they assemble backend-prepared read models and actions rather than local domain state.

## 6. Action and authority model

The frontend action architecture is aligned with backend authority:

- backend DTOs provide allowed actions, primary actions, route keys, recovery options, and presentation hints;
- `useObjectActions` and shared action components render/dispatch those contracts;
- frontend display state can select rows, open dialogs, preview, focus, and change density;
- frontend must not invent permissions, collection membership, lifecycle transitions, or shared persistence.

The permission-rule audit reports 26 backend permission sources, four backend presentation-flag sources, 15 frontend passthrough gates, and one local frontend gate. This is a healthy direction: most frontend gates are passthrough/presentation use, not independent authorization logic.

The remaining overlap shortlist includes `BLOCK` and `PRIMARY_ACTION`. These should be reviewed to ensure local checks are accessibility/interaction guards or safe absence handling rather than duplicated authorization. Removing them blindly could remove recovery or discoverability; retaining them without backend authority could create drift.

## 7. State and logic duplication

The latest frontend audits report:

- zero mutation-runner overlaps;
- three workflow-action overlaps;
- one feedback/error overlap category across many files;
- four generic duplicate-logic candidates;
- one likely-unused `ActivityRail.vue`;
- one stylesheet requiring import review (`styles/base.css`).

Workflow overlap candidates are circle actions duplicated in two files: `blockCircleUser`, `createCircleRequest`, and `unblockCircleUser`. Generic duplicate candidates are Work application/detail views and Chat surface. These findings indicate maintenance opportunities, not confirmed correctness bugs.

The strongest existing reuse is at the primitive/composable level: action dialogs, async actions, object actions, surface state, chat realtime, Vision handoff, shared form fields, detail surfaces, rows, and status components. The remaining duplication is mostly at route composition and feedback assignment, where different surfaces may need slightly different recovery copy.

## 8. Surface integrity and recovery

The static web contract preflight passed canonical route ordering, visible entry actions, Calendar modes/timezone labels, and Chat/Circles recovery affordances. UI entrypoint audit also passed navigation, routes, create handoffs, and action rendering connectivity.

The source architecture protects several important invariants:

- preview complements but does not replace canonical detail routes;
- nested row actions do not activate the containing row;
- collection URL state contains supported backend query inputs only;
- display/selection/scroll state is discarded when its backend context no longer contains the selected object;
- keyboard shortcuts do not intercept editable controls or Vision composers;
- dialogs support close, backdrop, and Escape recovery;
- reduced-motion and forced-colors states retain visible operation/focus feedback;
- Chat forbidden/not-found sync is treated as membership recovery, not ordinary pagination;
- Vision action execution requires explicit backend-owned confirmation/review state.

These are product interaction contracts, not cosmetic preferences. They are represented in `docs/work/action-contract-matrix.yaml`, regression scenarios, frontend validators, and backend tests.

## 9. Architecture strengths

1. **One authenticated shell:** shared navigation and interaction grammar reduce module drift.
2. **Canonical route ownership:** known objects retain stable detail routes; Vision handoffs are typed.
3. **Dynamic route loading:** large surfaces do not inflate the eager navigation core.
4. **Backend-prepared actions:** frontend remains thin around permissions and workflow meaning.
5. **Shared surface primitives:** dialogs, rows, details, fields, loading/error states, and action menus are standardized.
6. **Static contract preflight:** route/action/recovery regressions can be caught before browser execution.

## 10. Architecture risks

### 10.1 App-shell as a second monolith

At 82 files, app-shell is a significant subsystem. The risk is not file count by itself; it is domain-specific logic migrating into shared components because they are convenient. New app-shell code should be classified as shell infrastructure, surface composition, or domain-specific view logic. Domain behavior belongs in backend/API contracts.

### 10.2 Audit heuristics versus semantic ownership

The generic route audit misses the Vision surface, and the endpoint linker cannot distinguish admin/native/backend-only endpoints. Future audit improvements should add explicit ownership metadata instead of relying only on filename/import heuristics.

### 10.3 Shared feedback logic

Error assignment appears across many active files. Some repetition may be appropriate because surfaces need context-specific recovery; some may be a shared error-normalization opportunity. This should be analyzed with API error taxonomy before extracting a generic frontend abstraction.

### 10.4 Local action/permission drift

The small remaining local permission/action gate is safe only if it is a presentation or availability guard. Backend action keys must remain authoritative, and any local gate should have a documented reason.

### 10.5 Detached-looking assets

`ActivityRail.vue` is flagged likely unused and `styles/base.css` has no detected importer. They may be intentional transitional assets, but should be resolved in a focused cleanup slice rather than silently deleted.

## 11. Recommended controls

- Keep `shellRouteRegistry.ts` as the route ownership source and avoid route metadata duplication in views.
- Keep domain-specific API methods and state in the owning feature/API boundary even when the view lives in app-shell.
- Add explicit endpoint ownership categories to the API/capability inventory to reduce false-positive unlinked endpoint findings.
- Require a backend action contract for new mutating/detail actions; avoid frontend-derived status-to-action maps.
- Review duplicate circle actions and Work/Chat composition only after checking shared composable boundaries and error taxonomy.
- Resolve or document `ActivityRail.vue` and `styles/base.css` before treating stale-surface audits as fully clean.
- Keep `/vision` as an explicit special route in the route audit contract so generic surface discovery does not misclassify it.

## 12. Round 4 conclusion

The frontend architecture is a shared workspace shell with canonical route ownership and backend-driven action authority. It is functionally connected and static contracts pass. The main maintainability question is how to keep the large app-shell from absorbing domain logic. The next useful work is semantic classification of endpoint misses and targeted cleanup of duplicate action/error composition, not a broad module-directory rewrite.

No production code or route contract was changed in this analysis round.
