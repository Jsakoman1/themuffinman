# Frontend app-remaster System Map and audit reconciliation

Date: 2026-07-23

Status: planning analysis only. This document identifies implementation classes
and validation boundaries for the draft master plan. It does not claim that any
remaster work is implemented or verified.

## Source and evidence boundary

Canonical sources reviewed:

- `docs/product-vision.md`
- `docs/product-memory.md`
- `docs/vision-architecture-patterns.md`
- `docs/system-map.md`
- `docs/system-map-module-registry.yaml`
- `docs/system-map-change-impact-registry.yaml`
- `docs/system-map-dependency-registry.yaml`
- `docs/system-map-workflow-registry.yaml`
- `docs/system-map-coverage-registry.yaml`
- `docs/client-surface-registry.yaml`
- `docs/capability-inventory.yaml`
- `docs/target-capability-catalog.yaml`
- `docs/endpoint-capability-traceability-registry.yaml`
- verified frontend standardization plans referenced by `docs/work/frontend-app-remaster-master.yaml`

Audits executed during this pass:

- `make audit-frontend`
- `make system-map-impact`

The System Map impact report currently sees the entire dirty worktree, not only
the future remaster slice. Its reported 76 changed files must therefore be
treated as advisory and re-generated with exact child-plan changed paths when
implementation begins. It is not evidence that all of those files belong to
this remaster.

## Current audit findings that affect the remaster

| Finding | Evidence | Remaster consequence |
| --- | --- | --- |
| Interaction contract passes | `audit-frontend-interaction-contract.rb` | Preserve current row/scope/application/chat/popover contracts; improve only through explicit surface decisions. |
| UI entrypoints pass | `audit-ui-entrypoints.rb` | Do not invent new navigation entrypoints before checking existing canonical routes and shell actions. |
| 46 routes, 44 concrete surfaces, 1 redirect, 0 placeholders | `frontend-route-surface-inventory` | The remaster is a surface/interaction migration, not a route completion program. |
| 6 likely-unused components | `frontend-stale-surface-audit.json` | Ownership review required before removal or reuse. |
| 1 detached Vision view | `VisionSurfaceModernView.vue` | Keep detached from Web; do not reattach as an authenticated Web surface. |
| `base.css` requires review | `frontend-stale-surface-audit.json` | Determine whether it is an obsolete stylesheet or an indirect/global entry before deleting. |
| 3 duplicated circle workflow actions across 2 files | `frontend-state-logic-duplication-audit.json` | Centralize presentation/action adapter only if backend action authority remains unchanged. |
| 4 duplicate-logic review candidates | Work applications/detail and Chat | Use these as first implementation inspection targets, not as proof of a bug. |
| 215 backend endpoints, 194 frontend client methods, 140 linked, 75 unlinked | endpoint linker output | Unlinked endpoints include admin/non-Web/aggregate rows; do not convert the count into UI work without capability and consumer evidence. |
| Permission duplication audit reports 1 local gate and 3 backend flags | permission audit output | Audit the local gate and remove/invert only if it duplicates backend authority; preserve allowedActions passthrough. |

## Phase-to-class map

### Phase 1 — System Map reconciliation

Purpose: freeze the exact baseline and residual scope before implementation.

Inspect and update only planning/evidence surfaces:

- `docs/work/frontend-app-remaster-draft.yaml`
- `docs/work/frontend-app-remaster-master.yaml`
- `docs/work/frontend-app-remaster-master-execution-inventory.yaml`
- `docs/system-map-change-impact-registry.yaml`
- `docs/client-surface-registry.yaml`
- `docs/capability-inventory.yaml`
- `docs/target-capability-catalog.yaml`
- `docs/endpoint-capability-traceability-registry.yaml`
- `docs/audit-output/frontend-route-surface-inventory.json`
- `docs/audit-output/frontend-stale-surface-audit.json`

Frontend sources to reconcile against the registries:

- `apps/themuffinman/frontend/src/router.ts`
- `apps/themuffinman/frontend/src/modules/app-shell/shellRouteRegistry.ts`
- `apps/themuffinman/frontend/src/modules/app-shell/shellDefinitions.ts`
- `apps/themuffinman/frontend/src/modules/app-shell/shellSurfaceData.ts`
- `apps/themuffinman/frontend/src/modules/app-shell/api/userShellApi.ts`
- `apps/themuffinman/frontend/src/modules/app-shell/api/workspaceNavigationApi.ts`
- `apps/themuffinman/frontend/src/modules/app-shell/visionHandoff.ts`
- `apps/themuffinman/frontend/src/modules/app-shell/visionWebAction.ts`

Required output:

- one row per changed surface with route, capability family, existing action IDs,
  current verified baseline, residual UX gap, exact implementation files, and
  required runtime evidence;
- explicit classification of route-surface inventory rows that are aggregate
  shell consumers rather than direct user journeys;
- no new capability IDs.

### Phase 2 — UX architecture decisions

Purpose: decide the interaction grammar before adding primitives.

Primary classes:

- `AuthenticatedShellView.vue`
- `WorkspaceModuleRail.vue`
- `WorkspaceSurfaceView.vue`
- `SurfaceHeader.vue`
- `SurfaceContentView.vue`
- `CollectionToolbar.vue`
- `CollectionContextRail.vue`
- `DetailSurface.vue`
- `DetailSurfaceHeader.vue`
- `DetailUtilityRail.vue`
- `AppDialog.vue`
- `AppActionDialog.vue`
- `InlineEditText.vue`
- `GuidedIntakePanel.vue`
- `AppActionMenu.vue` and `QuickSwitcher.vue` only as ownership/reuse candidates
- `shellDefinitions.ts`
- `shellRouteRegistry.ts`
- `shellSurfaceData.ts`
- `router.ts`
- `visionHandoff.ts`
- `VisionForWebHost.vue`

Decisions that must be recorded before implementation:

- which collection/detail surfaces use direct route, context rail, drawer, or sheet;
- whether selected item state is URL-persisted for each affected collection;
- when a detail must remain a canonical route;
- tabs only for independent parallel contexts, never for ordinary sibling navigation;
- primary/secondary/quiet/danger/Vision action hierarchy;
- mobile behavior for rail, context, detail, dialog, and sheet;
- whether a command surface is existing navigation presentation or a new product capability;
- how VisionForWeb opens a Web surface without becoming a second navigation system.

### Phase 3 — Shared surface, motion, and interaction foundation

Purpose: implement missing shared behavior only after the verified baseline is
compared with the decisions from Phase 2.

Likely implementation classes:

- `AppButton.vue`
- `AppIconButton.vue`
- `AppFormField.vue`
- `AppFormFooter.vue`
- `AppStatus.vue`
- `AppLoadingState.vue`
- `AppEmptyState.vue`
- `AppDialog.vue`
- `AppActionDialog.vue`
- `CollectionToolbar.vue`
- `SurfaceHeader.vue`
- `SurfaceRow.vue`
- `CollectionContextRail.vue`
- `DetailSurface.vue`
- `DetailSurfaceHeader.vue`
- `DetailUtilityRail.vue`
- `InlineEditText.vue`
- `GuidedIntakePanel.vue`
- `useActionDialog.ts`
- `useAsyncAction.ts`
- `useObjectActions.ts`
- `useSurfaceViewState.ts`
- `apps/themuffinman/frontend/src/style.css`
- `apps/themuffinman/frontend/src/styles/base.css` after ownership review
- `services/appearanceTheme.ts` and `services/formatters.ts` only when token or formatting ownership is affected

Rules:

- extend existing primitives before creating `AppTurnBar`, `AppSheet`, or similar;
- do not add a shared primitive solely because it has a desirable name;
- a new primitive needs at least two real Web consumers or a documented shell contract;
- motion must be functional, short, and reduced-motion aware;
- state feedback must show authoritative readback or an explicit retry/recovery path.

### Phase 4 — Work context and guided flow

Mapped capability family: `work.*`; existing Web surfaces remain canonical.

Exact primary classes:

- `WorkDiscoveryView.vue`
- `WorkQuestCreateView.vue`
- `WorkQuestDetailView.vue`
- `WorkQuestApplicationsView.vue`
- `WorkApplicationsView.vue`
- `WorkApplicationDetailView.vue`
- `CollectionToolbar.vue`
- `SurfaceRow.vue`
- `CollectionContextRail.vue`
- `ObjectPreviewPanel.vue`
- `DetailSurface.vue`
- `DetailSurfaceHeader.vue`
- `DetailUtilityRail.vue`
- `RichTextEditor.vue`
- `RichTextPreview.vue`
- `useObjectActions.ts`
- `useSurfaceViewState.ts`
- `useActionDialog.ts`
- `shellDefinitions.ts`
- `shellRouteRegistry.ts`

Current audit focus:

- `WorkQuestApplicationsView.vue` score 6 duplicate-logic review candidate;
- `WorkQuestDetailView.vue` score 6 duplicate-logic review candidate;
- `WorkApplicationsView.vue` score 5 duplicate-logic review candidate;
- `WorkDiscoveryView.vue` is the largest collection surface and the primary
  density/comparison/mobile candidate;
- application context already uses `CollectionContextRail`; preserve the
  explicit full-detail route and do not silently turn row selection into route navigation.

Allowed remaster scope:

- density, hierarchy, selection, context/detail presentation, guided creation,
  review/confirmation, mobile layout, loading/error/stale/success feedback;
- reuse existing mapped actions such as `work.quest.create`,
  `work.application.create`, `work.application.update`, `work.application.decide`,
  and `work.quest.*` action flags.

Not allowed:

- deriving eligibility or transition availability from status text;
- adding new quest/application mutations;
- merging owned and discoverable Work scopes in the frontend.

### Phase 5 — Chat workspace and recovery

Mapped capability family: `chat.*`.

Exact primary classes:

- `ChatSurfaceView.vue`
- `useChatRealtime.ts`
- `userShellApi.ts`
- `AppButton.vue`
- `AppIconButton.vue`
- `AppFormField.vue`
- `AppFormFooter.vue`
- `AppStatus.vue`
- `AppDialog.vue` if group/direct creation is moved to a shared overlay pattern
- `services/formatters.ts`

Current audit focus:

- `ChatSurfaceView.vue` score 4 duplicate-logic review candidate;
- the two-pane layout is an intentional exception and should not be flattened
  into the generic collection/detail pattern;
- realtime status, reconnect, attachment upload, send, edit, reply, reaction,
  leave, and unavailable attachment states already have mapped backend actions;
- simplify presentation and recovery without changing websocket or server
  authority.

### Phase 6 — Social, sharing, rides, and business sweep

This phase is a targeted sweep, not a blanket rewrite.

Circles and People:

- `CirclesView.vue`
- `PeopleDiscoveryView.vue`
- `PeopleProfileView.vue`
- `GuidedIntakePanel.vue`
- `AppDialog.vue`
- `DetailSurface.vue`
- `DetailSurfaceHeader.vue`
- `DetailUtilityRail.vue`
- `userShellApi.ts`

Audit targets:

- duplicated `blockCircleUser`, `createCircleRequest`, and `unblockCircleUser`
  presentation/action logic;
- circle-specific invite picker gap;
- privacy/visibility explanations;
- decision between dedicated circle detail route and context surface;
- mobile presentation.

Things:

- `ThingsDiscoveryView.vue`
- `ThingDetailView.vue`
- `apps/themuffinman/frontend/src/modules/things/api/thingsApi.ts`
- `GuidedIntakePanel.vue`
- `AppDialog.vue`
- `DetailSurface.vue`
- `DetailSurfaceHeader.vue`
- `AppFormField.vue`
- `AppFormFooter.vue`

Audit targets:

- preserve the existing shared form primitives and borrow-request actions;
- simplify listing/request context and explicit request/decision/return turns;
- add only named retry, stale, mobile, and runtime gaps.

Rides:

- `RidesView.vue`
- `RideDetailView.vue`
- `apps/themuffinman/frontend/src/modules/rides/api/ridesApi.ts`
- `GuidedIntakePanel.vue`
- `DetailSurface.vue`
- `DetailSurfaceHeader.vue`
- `DetailUtilityRail.vue`

Audit targets:

- compact offer/discover/join/update/leave/cancel/complete status presentation;
- circle-scoped visibility and lifecycle clarity;
- preserve existing ride action methods and route ownership.

Business:

- `BusinessDiscoveryView.vue`
- `BusinessPublicView.vue`
- `BusinessProfileView.vue`
- `BusinessOfferingsView.vue`
- `BusinessBookingsView.vue`
- `BusinessMyBookingsView.vue`
- `BusinessAvailabilityView.vue`
- `BusinessAvailabilityExceptionsView.vue`
- `AppDialog.vue`
- `DetailSurface.vue`
- `DetailUtilityRail.vue`
- `GuidedIntakePanel.vue`

Audit targets:

- consistent booking/detail grammar;
- compact calendar/availability presentation;
- gallery upload/reorder/delete recovery;
- public discovery versus owner operations separation;
- mobile presentation and browser evidence for existing booking actions.

### Phase 7 — CSS cleanup and obsolete-surface review

Exact review set:

- `apps/themuffinman/frontend/src/style.css`
- `apps/themuffinman/frontend/src/styles/base.css`
- all scoped styles in the views named in Phases 3–6;
- `ActivityRail.vue`
- `AppActionMenu.vue`
- `GlobalVisionEntry.vue`
- `QuickSwitcher.vue`
- `WorkspaceContextNav.vue`
- `WorkspaceKeyboardHelp.vue`
- `apps/themuffinman/frontend/src/modules/vision/views/VisionSurfaceModernView.vue`

Required decisions:

- prove whether each likely-unused component has an intended future owner;
- remove only components confirmed obsolete and record the decision;
- do not delete `VisionSurfaceModernView.vue` merely because it is detached;
  its non-Web/terminal boundary must remain explicit in product and architecture docs;
- identify duplicate spacing, color, radius, typography, layout, and motion values;
- consolidate only styles that have shared semantic meaning, not module-specific
  domain presentation.

### Phase 8 — Runtime, visual, and accessibility closeout

Representative browser journeys should cover:

- authenticated shell and module navigation;
- Work discovery → context/detail → existing action;
- Work creation/review where action flags permit it;
- Chat list → conversation → send/reconnect/attachment recovery;
- Circles/People privacy-aware surface and invite flow;
- Things request flow;
- Rides offer/join lifecycle;
- Business discovery/booking or owner booking review;
- VisionForWeb opening a canonical Web surface from the shell.

Required validation classes:

- `npm --prefix apps/themuffinman/frontend run type-check`
- `npm --prefix apps/themuffinman/frontend run build`
- `make audit-frontend`
- `make audit-plan-coverage` after child plans exist
- current browser runtime JSON for changed journeys
- changed screenshots at desktop and mobile representative widths
- keyboard/focus/reduced-motion/accessibility checks
- `git diff --check`

## Child-plan readiness changes

Before creating the eight child plans, each plan must replace broad wording with:

- exact `paths` and `required_paths`;
- named route/surface/capability rows;
- one or more concrete user journeys;
- explicit backend baseline and frontend residual behavior;
- runtime and visual evidence paths when applicable;
- leaf validation commands only;
- a stop condition for backend contract or product decisions.

The next safe action is to create the eight child work plans from this map, then
run plan-coverage validation. No frontend implementation should begin before
the child plans are materialized and the first reconciliation child is verified.

## Additional repo-wide findings

The second repo-wide pass found three additional residual areas that are real
frontend remaster scope and are now represented by child plans 09–11.

### Personal context and command center

The existing shell already owns these related surfaces, but the original master
did not give them an implementation slice:

- `HomeHubView.vue` — backend-prepared summary and next actions;
- `NotificationsView.vue` — notifications, Attention Center, selected preview,
  read state, and canonical destinations;
- `ActivityView.vue` — viewer-scoped history and resumable tasks;
- `SavedSearchIntentsView.vue` — pause/resume/delete existing saved search actions;
- `GlobalSearchEntry.vue` — permitted command catalog, search, filter, compare,
  save-search, and canonical result destinations;
- `UniversalCreateMenu.vue` — backend-published direct create options;
- `WorkspaceModuleRail.vue` and `AuthenticatedShellView.vue` — personal context,
  unread attention, pinned shortcuts, mobile drawer, and shell placement;
- `shellSurfaceData.ts` — backend-prepared summary/section data consumed by shell
  surfaces.

This is UX remaster scope because it concerns orientation, next actions,
progressive disclosure, and navigation continuity. It must not become a second
dashboard or local count/permission system.

### Profile, visibility, consent, and location settings

`ProfileLocationSettingsView.vue` is a large, multi-purpose settings surface with
existing capability inventory gaps for inline profile editing, visibility
explanation, location/provider recovery, mobile lifecycle, and runtime proof. It
contains profile editing, avatar/gallery, circle/user visibility selection,
location lookup/current location, provider outcomes, appearance, and notification
entry. These are existing mapped capabilities, not new feature requests, but the
surface needs its own simplification and progressive-disclosure plan.

Primary classes:

- `ProfileLocationSettingsView.vue`
- `NotificationPreferencesView.vue`
- `InlineEditText.vue`
- `GuidedIntakePanel.vue`
- `AppFormField.vue`
- `AppFormFooter.vue`
- `AppStatus.vue`
- `visionHandoff.ts`

### Residual date/time and state drift

The repo still contains local date/time formatters despite
`services/formatters.ts` and prior state/time standardization evidence. Examples
include `RideDetailView.vue`, `ThingDetailView.vue`, `ChatSurfaceView.vue`,
`NotificationsView.vue`, `BusinessBookingsView.vue`,
`BusinessMyBookingsView.vue`, `BusinessAvailabilityExceptionsView.vue`,
`WorkApplicationsView.vue`, `WorkApplicationDetailView.vue`,
`WorkDiscoveryView.vue`, `shellSurfaceData.ts`, and `SurfaceContentView.vue`.

The residual plan must first produce a matrix of actual user-visible differences,
then consolidate only those differences that are truly formatting/state drift. It
must preserve backend-provided status semantics and explicit calendar/booking
timezone meaning.

These findings extend the master from 8 to 11 child plans. They do not expand the
capability inventory or reopen the Vision console boundary.
