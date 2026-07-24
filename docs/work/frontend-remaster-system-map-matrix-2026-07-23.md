# Frontend App Remaster — System Map Matrix

Date: 2026-07-23  
Status: analysis baseline for goal pursuing  
Baseline revision: `dcb983f4ad2b0354101caa8c1cdc47c624fb8b71`

## Baseline boundary

The repository was already dirty when this goal started. Existing Vision runtime/prompt work, generated evidence, contract changes, and the previously prepared remaster plans are preserved. They are not owned by this remaster unless a child plan changes the exact file intentionally. Any System Map impact report produced from the whole dirty worktree is advisory; implementation evidence must be generated from the exact child-plan path set.

The remaster is a frontend presentation and interaction modernization. It may simplify navigation, standardize shell/surface grammar, improve state handling, consolidate CSS, and remove or quarantine stale presentation surfaces. It must not invent capabilities, move permission decisions into Vue, expose the detached Vision terminal in Web UI, or replace backend-owned DTO/action/visibility/consent/workflow authority.

## Source precedence

1. `docs/system-map.md` and the System Map registries define product/module ownership and dependency impact.
2. `docs/capability-inventory.yaml`, `docs/client-surface-registry.yaml`, and endpoint traceability define what is actually mapped and callable.
3. Route/surface audit output and the verified child-plan paths define the current Web entry points.
4. Product intent in `docs/product-memory.md` and `docs/product-vision.md` guides presentation decisions when the mapped capability already exists.
5. A UI change is accepted only when the route, surface, capability family, backend authority, and runtime evidence are explicit.

## Residual matrix

| Seq | Child plan | Surface / route anchors | Exact implementation scope | Backend-owned boundary | Required evidence / decision |
| --- | --- | --- | --- | --- | --- |
| 01 | System Map reconciliation | All remaster candidates; route registry and surface registry | Freeze this matrix, classify residuals, reconcile stale audit output, and name exact paths for later plans. No product code. | No capability or permission changes. | Matrix and audit baseline; every candidate linked or explicitly deferred. |
| 02 | UX architecture decisions | Authenticated shell, workspace routes, `/vision` Web host | `AuthenticatedShellView.vue`, `WorkspaceModuleRail.vue`, `WorkspaceSurfaceView.vue`, `SurfaceHeader.vue`, `SurfaceContentView.vue`, `CollectionToolbar.vue`, `CollectionContextRail.vue`, `DetailSurface*.vue`, dialogs, inline edit, guided intake, `shellDefinitions.ts`, `shellRouteRegistry.ts`, `shellSurfaceData.ts`, `router.ts`, `visionHandoff.ts`, `VisionForWebHost.vue`. | VisionForWeb remains the only inline Web assistant; detached Vision console remains outside Web navigation. | Approved surface grammar, navigation rules, popup/tab policy, responsive rules, and route ownership. |
| 03 | Shared foundation | All authenticated surfaces | Shared primitives/tokens, loading/empty/error/success states, motion, focus, responsive layout, and shared CSS. Keep domain actions in existing views/composables. | No frontend permission reimplementation; preserve `allowedActions`, DTOs, visibility, consent, and workflow state. | Type-check/build plus interaction-contract and visual/state checks for representative surfaces. |
| 04 | Formatting/state consistency | Work, Chat, module detail/list surfaces | Centralize date/time/number/status formatting and repeated state/action presentation. Review residuals in `RideDetailView.vue`, `ThingDetailView.vue`, `ChatSurfaceView.vue`, notifications, business bookings/availability, work applications/discovery/detail, shell data/content. | Formatting must not alter backend state semantics or action availability. | Residual grep/audit is zero or explicitly recorded; representative state transitions remain intact. |
| 05 | Identity and onboarding | Public login/register/recovery/reset and authenticated `/onboarding` | `LoginView.vue`, `RegisterView.vue`, `PasswordRecoveryView.vue`, `PasswordResetView.vue`, `OnboardingView.vue`, `authApi.ts`, `auth.ts`, `sessionService.ts`, shared form components. | Authentication, recovery, session, onboarding progress, validation, and redirect authority stay in API/session/backend services. | Anonymous/authenticated/error/expired-session journeys; no shell leakage into public routes. |
| 06 | Work context-guided flow | `/work`, `/work/discover`, `/work/applications`, quest detail/application routes | Work discovery, applications, quest detail, guided intake, action menus, and context-preserving detail/edit surfaces; use existing Work views/composables and API contracts. | Quest/application status, allowed actions, permissions, and workflow transitions remain backend-owned. | Discover → detail → apply/manage → return journey; inline edit and empty/error/loading states. |
| 07 | Chat workspace recovery | `/chat`, conversation/detail/message surfaces | `ChatSurfaceView.vue`, chat composables/API, shared collection/detail/message controls; stabilize composer, selection, unread/loading/error, and responsive workspace behavior. | Membership, visibility, message persistence, send permissions, and realtime authority remain backend-owned. | Open conversation, send message, recover refresh/error, narrow viewport, and keyboard/focus journey. |
| 08 | Personal context and command center | Home, attention/activity, notifications, saved search, global search, create, authenticated shell | `App.vue`, Home/Attention/Activity/Notifications/SavedSearch views, `GlobalSearch.vue`, universal/create surfaces, shell activity/action/context components. | Feed/query results, counts, saved-search behavior, creation capabilities, and permissions remain mapped services. | Global navigation and command actions route only to registered surfaces; no dead links or full-page reloads. |
| 09 | Profile, visibility, location | Profile and settings routes | Profile/identity settings, visibility/location, notification preferences and related forms. Standardize sections, inline edit, validation feedback, and detail utility actions. | Profile fields, visibility, consent, location/privacy rules, and mutations remain backend-owned. | View/edit/save/cancel/error plus privacy and responsive evidence; no duplicated local permission gates. |
| 10 | Mapped module sweep | Circles/People, Things, Rides, Business routes | `PeopleDiscoveryView.vue`, circle surfaces, thing discovery/detail, rides, business/bookings/availability surfaces; apply shared grammar without inventing cross-module behavior. | Circle membership/consent, lending/rides availability, booking rules, and business permissions remain backend-owned. | One collection/detail/action journey per mapped module and explicit unsupported-state handling. |
| 11 | CSS and obsolete-surface review | Shared styles and audit candidates | Review `base.css` and shared module styles; consolidate tokens/selectors and classify `ActivityRail.vue`, `AppActionMenu.vue`, `GlobalVisionEntry.vue`, `QuickSwitcher.vue`, `WorkspaceContextNav.vue`, `WorkspaceKeyboardHelp.vue`. Review detached `VisionSurfaceModernView.vue` as a boundary artifact. | Removal/quarantine cannot remove a mapped capability or reconnect the detached Vision terminal. | Stale-surface audit, CSS/build/type checks, route inventory, and explicit keep/remove/quarantine decisions. |
| 12 | Runtime visual closeout | Representative journeys across all remastered surfaces | Browser/runtime, visual, responsive, accessibility, link/button, no-reload, and VisionForWeb boundary verification. Documentation/evidence only unless a finding returns to the owning child plan. | Runtime verifies existing contracts; it does not authorize capability changes. | Chromium/Playwright journeys, screenshots/visual notes, keyboard/a11y checks, type-check/build, final verifier. |

## Audit classifications

The frontend audit baseline reported 46 routes, 44 concrete surfaces, one redirect, and no placeholders. It reported 215 backend endpoints, 194 client methods, 140 linked methods, and 75 unlinked endpoint rows. The unlinked count is not a UI backlog: rows must first be classified as admin-only, non-Web, generated/indirect, intentionally unmapped, or a real mapped Web gap. No new capability ID is created from that count.

The stale-surface audit identified six likely-unused presentation candidates: `ActivityRail.vue`, `AppActionMenu.vue`, `GlobalVisionEntry.vue`, `QuickSwitcher.vue`, `WorkspaceContextNav.vue`, and `WorkspaceKeyboardHelp.vue`. They are review candidates, not automatic deletion targets. `VisionSurfaceModernView.vue` is a detached Vision console boundary and must remain unreachable from Web UI; `base.css` is a review-needed shared-style surface.

The state-logic audit found three duplicated action overlaps (`blockCircleUser`, `createCircleRequest`, `unblockCircleUser`) and the permission audit found local/backend gate discrepancies for `BLOCK` and `PRIMARY_ACTION`. These are consistency-review inputs only. The remaster may consolidate presentation and action wiring, but must not make the frontend a second authorization source.

## Explicit non-goals

- New domain capabilities, new backend endpoints, or new permission/visibility rules.
- Reattaching or linking the Vision terminal page from Web UI.
- Replacing backend DTO assembly with frontend-derived product logic.
- Native/mobile redesign in this batch.
- Broad deletion of dirty-worktree files or unrelated Vision work.

## Child-plan handoff rule

Before each child implementation, rerun the System Map impact/audit command against that child’s exact changed paths. Record route, surface, capability family, affected registries/docs, validation commands, and runtime journey in the child closeout. If a discovery changes capability or workflow meaning, stop the UI slice and update the relevant business/domain/agent documents before continuing.

