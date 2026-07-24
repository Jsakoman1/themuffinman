# Frontend app-remaster plan deep dive

Date: 2026-07-23

This document is the implementation-preparation companion for
`docs/work/frontend-app-remaster-master.yaml` and its 11 child plans. It does
not mark any implementation complete. It turns each broad child task into an
ordered implementation recipe with concrete ownership, edge cases, and proof.

## Program-wide implementation contract

### Source-of-truth precedence

When sources disagree, use this order:

1. backend DTOs and `allowedActions`;
2. capability inventory and System Map workflow/permission registries;
3. canonical route and client-surface registries;
4. existing verified frontend plans/evidence;
5. product vision and UX decisions;
6. local component assumptions.

The frontend may present, select, navigate, optimistically reflect, and recover.
It must not decide permission, visibility, consent, eligibility, workflow state,
collection membership, or persistence authority.

### Surface decision vocabulary

Every affected surface must have exactly one primary interaction shape:

- `collection`: many records, filtering, sorting, search, and one selected/open target;
- `split_context`: collection plus inspectable context rail, with explicit full-detail action;
- `canonical_detail`: route-owned detail for deep-linkable or action-heavy entities;
- `guided_editor`: progressive input, review, confirmation, and authoritative result;
- `drawer_or_sheet`: transient contextual inspection/editing that preserves the collection;
- `conversation`: persistent two-pane or thread workspace;
- `section_hub`: backend-prepared section navigation, not a second dashboard.

No implementation may introduce a new shape without recording why an existing
shape is insufficient.

### State contract

Each changed async surface must distinguish:

- initial loading;
- loading more or submitting;
- empty but healthy;
- stale data with a recovery affordance;
- unavailable/permission-denied/provider failure;
- mutation in progress;
- authoritative success/readback;
- recoverable failure with retry or correction;
- irreversible/destructive confirmation.

Do not use a generic error string to hide a partial response or a backend
permission result. Do not clear usable data merely because a secondary request
failed.

### Evidence contract

Every implementation child must produce:

- type-check and production build;
- `make audit-frontend`;
- current browser JSON for changed behavior;
- screenshots for changed visual/responsive surfaces;
- keyboard/focus/reduced-motion checks where shared interaction changes;
- `git diff --check`.

Evidence must be generated after the implementation change in the current run.
Prior evidence is baseline only.

## Plan 01 — System Map reconciliation

### Implementation sequence

1. Freeze the current git revision and record the dirty-worktree boundary.
2. Export the current route/surface inventory and list every affected route.
3. Join route rows to `docs/capability-inventory.yaml` by capability family and
   to `docs/client-surface-registry.yaml` by Web surface.
4. Join each candidate to the verified frontend plan that already covers it.
5. Mark each candidate as `baseline`, `residual_ui`, `residual_runtime_evidence`,
   `product_decision`, `backend_blocked`, or `out_of_scope`.
6. Classify endpoint-linker rows into Web UI, Vision, admin, aggregate shell,
   native/contract, or unresolved owner review.
7. Produce the residual matrix before any source implementation begins.

### Required matrix columns

`id`, `module`, `route`, `surface_file`, `capability_ids`, `mapped_actions`,
`verified_baseline`, `residual_problem`, `implementation_plan`,
`exact_source_paths`, `backend_authority`, `runtime_journey`,
`visual_evidence`, `decision_required`, `out_of_scope_reason`.

### Close criteria

- Every child plan 02–11 has at least one matrix row.
- Every implementation path in those plans is justified by a matrix row.
- No row claims a capability from a button, route, prompt, or mockup alone.
- Dirty-worktree System Map impact output is explicitly marked advisory and
  replaced by exact changed-file impact at implementation start.

## Plan 02 — UX architecture decisions

### Decision records to create

1. `route_context_matrix`: route, collection, selected item, overlay, full detail,
   mobile behavior, URL query state, browser back behavior.
2. `action_hierarchy`: primary, secondary, quiet, danger, overflow, Vision,
   disabled/loading/recovery behavior.
3. `form_progression`: minimum fields, optional fields, advanced disclosure,
   review step, dirty state, cancel/discard.
4. `turn_based_interaction`: intent, options, confirmation, execution,
   authoritative result, next action; direct UI remains complete.
5. `motion_accessibility`: transition durations, reduced-motion fallback,
   focus movement, live-region behavior, touch target minimums.
6. `mobile_surface_matrix`: rail, bottom nav, drawer, sheet, full route,
   sticky action footer, keyboard/viewport behavior.

### Required decisions by surface

- Work discovery: collection or split-context; selected quest URL state;
  full quest route remains canonical.
- Work applications: split-context plus explicit application detail route;
  owner-side review remains separate from viewer-owned applications.
- Chat: intentional conversation exception; preserve list/thread continuity.
- Circles/People: context surface unless a dedicated circle detail decision is
  approved; privacy explanation must remain visible at the decision point.
- Things/Rides: context/detail choice must preserve lifecycle action visibility.
- Business: public profile, owner operations, calendar, and booking review must
  remain distinct surfaces.
- Home/Attention: orientation and next actions only; no duplicate module lists.
- Profile/Settings: progressive disclosure; safety/privacy explanations cannot
  be hidden behind a generic “advanced” control.

### Close criteria

The decision document must include examples for desktop and mobile, route/query
examples, back/close behavior, focus return target, and an explicit “not a
popup/tab/chat” reason for every surface that stays route-based.

## Plan 03 — Shared surface, motion, and feedback foundation

### Work order

1. Inventory current token names and duplicate values; do not rename blindly.
2. Establish the minimum shared token contract in `style.css`; review `base.css`
   before moving or deleting rules.
3. Normalize `AppButton`, `AppIconButton`, `AppStatus`, and form footer states.
4. Normalize dialog/sheet behavior only if the architecture decision requires a
   missing variant; preserve existing focus trap and close semantics.
5. Normalize `SurfaceRow`, `CollectionToolbar`, context rail, and detail frame
   selected/hover/focus/loading states.
6. Add functional motion and reduced-motion fallback.
7. Migrate one representative collection and one detail surface before broad
   adoption.
8. Capture desktop/mobile evidence and run the full frontend audit.

### Component contracts

- `AppButton`: existing tone/loading/disabled semantics remain stable; no local
  button CSS should override action meaning.
- `AppStatus`: one status role, explicit tone, optional retry, busy state, and
  accessible announcement; no silent error swallowing.
- `AppDialog`: focus enters the dialog, Escape/close returns focus, destructive
  actions remain explicit, mobile width is viewport-safe.
- `SurfaceRow`: selection is not implicit navigation; action slots stop event
  propagation; keyboard selection matches pointer selection.
- `CollectionContextRail`: context is optional, has empty state, and exposes a
  full-detail route when the entity has one.
- `DetailSurface`: detail actions come from DTO/action flags and remain usable
  when the utility rail collapses on mobile.

### Stop conditions

Stop if a shared primitive requires domain-specific status logic, a new backend
contract, or a hidden permission rule. Create a module-specific adapter or a
product decision instead.

## Plan 04 — Work context and guided flow

### User journeys

1. Find work: open `/work/find`, search/filter, select a row, inspect context,
   open canonical quest detail, return without losing search context.
2. My work: open `/work/quests`, verify the backend-owned scope, inspect and
   execute one allowed lifecycle action, see authoritative readback.
3. Offer work: open `/work/offer`, complete minimum guided intake, review,
   publish, see result and next action.
4. Apply: open a permitted quest detail, enter message/price, submit, recover
   from validation/error, see application status.
5. Review applications: owner opens quest applications, selects an applicant,
   compares available backend-prepared information, executes only allowed action.
6. My applications: edit or withdraw only when `allowedActions` permits it;
   open full application detail from context.

### Exact implementation focus

- reduce `WorkDiscoveryView.vue` density without changing `AVAILABLE` versus
  `MY_VISIBLE` request scopes;
- remove duplicated status/permission interpretation in the three audit
  candidates, replacing it with DTO presentation and shared action helpers;
- make context/full-detail transitions explicit;
- keep `WorkQuestDetailView.vue` action utility collapsible but keyboard usable;
- keep create/apply/review forms progressive and reviewable;
- ensure all destructive/lifecycle actions have confirmation and authoritative
  reload/readback.

### Edge cases

- selected record disappears after mutation;
- stale list while detail is current;
- action becomes unavailable after another user changes state;
- empty owned list versus empty discoverable list;
- application detail target is missing or inaccessible;
- mobile action rail cannot fit all actions.

## Plan 05 — Chat workspace and recovery

### User journeys

1. Open `/chat` with no selected thread and see a useful empty state.
2. Select `/chat/:conversationId`, load messages, and preserve selection on
   refresh/back navigation.
3. Send text, attachment, reply, edit, reaction, and delete with visible state.
4. Lose realtime connection, see reconnecting/disconnected state, retry/sync,
   and avoid duplicate messages.
5. Create direct/group conversation from the same workspace without a second
   navigation grammar.
6. Leave a group and recover when membership transition makes the thread
   unavailable.

### Implementation focus

- separate conversation index loading from thread loading and message sync;
- make `realtimeStatus`, `syncStatus`, `error`, and upload state visually distinct;
- centralize message action feedback without changing `useChatRealtime` authority;
- make mobile list/thread transition reversible and preserve composer draft;
- ensure attachment unavailable/expired state never exposes storage internals;
- keep `ChatSurfaceView.vue` as an intentional exception in the surface matrix.

### Edge cases

- conversation deleted or membership revoked while open;
- offline send and duplicate retry;
- upload succeeds but message send fails;
- reconnect delivers an event already present;
- long message/attachment names overflow;
- keyboard opens on mobile and hides composer.

## Plan 06 — Social, sharing, rides, and business sweep

This plan should be executed as four bounded sub-slices inside its single serial
task, in this order: Circles/People, Things, Rides, Business. Each sub-slice
must retain its own browser journey in the evidence JSON.

### Circles/People

- inspect and deduplicate `blockCircleUser`, `createCircleRequest`, and
  `unblockCircleUser` presentation logic;
- keep visibility/consent explanations adjacent to invite, remove, leave, and
  block actions;
- decide circle detail route versus context surface before implementation;
- preserve server-owned membership states and unavailable-target recovery.

### Things

- preserve listing, own-listing, borrow-request, decide, cancel, and return
  action mapping;
- separate discovery list, selected listing context, and borrow request review;
- use existing `AppFormField`/`AppFormFooter` contract;
- show partial listing/request failures without erasing usable listings.

### Rides

- keep offer/discover/join/update/leave/cancel/complete routes/actions;
- make date/time, seats, circle visibility, and lifecycle status scannable;
- distinguish “no matching ride” from failed match request;
- preserve owner/member action differences from DTOs.

### Business

- keep public discovery/profile separate from owner profile/offerings/bookings;
- make booking request/review/confirm/cancel/reschedule states explicit;
- keep timezone and availability semantics visible;
- simplify gallery management without claiming upload/reorder completion beyond
  current runtime evidence;
- preserve independent failure states for profile, gallery, availability, and bookings.

## Plan 07 — CSS and obsolete surface review

### Work order

1. Generate an import/callsite inventory for all stale candidates.
2. For each candidate record `retain`, `remove`, `replace`, or `defer` with owner,
   reason, and evidence.
3. Audit `style.css` and `base.css` load ownership before deleting any selector.
4. Extract repeated token/value patterns from changed surfaces.
5. Consolidate only proven shared semantics.
6. Run visual regression screenshots for surfaces touched by CSS changes.

### Special boundary

`VisionSurfaceModernView.vue` is detached by design from authenticated Web. Its
review outcome should normally be `retain-as-detached-boundary` or
`defer-with-product-decision`, not deletion by stale-component count.

## Plan 08 — Runtime, visual, and accessibility closeout

### Evidence matrix

The closeout document must map each changed child to:

- route and authenticated setup;
- user journey;
- expected backend/API responses;
- screenshot widths 1440, 980, 700, 390 where relevant;
- keyboard/focus checks;
- reduced-motion check;
- error/retry or stale-state check;
- runtime artifact path and timestamp.

### Required representative journeys

- shell → Home/Attention → canonical module destination;
- Work discovery/context/detail/action;
- Work create or apply/review;
- Chat select/send/reconnect or attachment recovery;
- Circle/person privacy-aware action;
- Things request or Rides join lifecycle;
- Business discovery/booking or owner booking review;
- Profile visibility/location recovery;
- Search result open/compare/save and Create menu route;
- VisionForWeb prompt → canonical Web surface.

Closeout must not claim every capability runtime-proven if only representative
journeys were exercised; update capability evidence only for exact covered rows.

## Plan 09 — Personal context and command center

### User journeys

1. Home loads backend-prepared metrics and exposes one next action per row.
2. Attention opens a notification/activity destination supplied by backend.
3. Activity resumes or dismisses an item without inventing a route.
4. Search opens command catalog, searches permitted records, filters, compares,
   opens canonical destination, and saves a search.
5. Create loads backend-published options and opens existing direct flows.
6. Saved search pauses/resumes/deletes with confirmation and recovery.
7. Mobile shell opens/closes drawer with focus restoration and no duplicate menu.

The shell-wide implementation also includes `App.vue` for session restoration,
appearance bootstrap, global command/create shortcuts, and unauthenticated
redirect recovery. These remain shell concerns, not a second auth system.

### Edge cases

- attention/context request partially fails;
- command catalog unavailable but direct navigation remains usable;
- search returns no permitted results versus provider/backend failure;
- result has no canonical detail route;
- saved search mutation succeeds but reload fails;
- unread count changes from realtime event while the user is on another surface.

## Plan 10 — Profile, visibility, consent, and location

### Subsections and disclosure order

1. Profile identity and description.
2. Avatar/gallery.
3. Profile field visibility explanation and circle/user selection.
4. Location preference and locality label.
5. Exact location visibility and current-location flow.
6. Appearance and notification preferences.

Each section needs independent loading/error/success state where the backend
already treats the data independently. Advanced circle/user selectors should be
collapsed only after the current visibility mode is explained.

### User journeys

- edit username/description and save inline;
- change avatar/gallery with validation and recovery;
- set public/private/circle/user visibility and verify confirmation copy;
- resolve a location by search and recover provider failure;
- use current location and handle browser unavailable/denied/provider unavailable;
- follow VisionForWeb settings handoff with query context;
- update notification preferences without losing profile changes.

### Safety constraints

- never display a local claim that a user can see another user's private field;
- never persist circle/user selection through an unapproved client shortcut;
- explain that backend consent/redaction controls actual visibility;
- preserve existing location retention and provider-error semantics.

## Plan 12 — Identity entry and onboarding

### User journeys

1. Login with valid credentials, enter the workspace, and preserve redirect behavior.
2. Submit invalid credentials and receive safe actionable feedback.
3. Register with normalized email and continue to the workspace.
4. Request recovery and distinguish safe success from retryable failure.
5. Open reset with missing, invalid, expired, or valid token and receive the correct next action.
6. Load optional onboarding, continue, skip, reset, complete, and recover from failed progress save.

### Implementation focus

- unify `LoginView.vue`, `RegisterView.vue`, `PasswordRecoveryView.vue`, and
  `PasswordResetView.vue` around the shared form/status/footer grammar;
- replace native primary button styling where `AppButton` adoption does not alter
  submit behavior;
- keep public identity surfaces visually related to the workspace but less dense;
- show onboarding progress from backend state and never treat row selection as
  progress mutation;
- preserve token/session semantics and the optional nature of onboarding.

### Edge cases

- authenticated user visits login/register;
- expired session during `App.vue` `authApi.me()` restore;
- recovery succeeds while delivery remains externally unknown;
- reset token is absent from the URL;
- onboarding unavailable while authenticated shell remains usable;
- user skips onboarding and later resumes it.

## Plan 11 — Formatting and state consistency

### Work order

1. Build the formatting/state matrix with current output, desired shared utility,
   locale/timezone rule, and affected route.
2. Extend `services/formatters.ts` only for proven missing variants.
3. Replace local `Intl.DateTimeFormat`, `toLocaleDateString`, and hardcoded locale
   calls in the matrix.
4. Reconcile `SurfaceContentView.vue` calendar labels with booking/business
   timezone rules.
5. Normalize visible state copy and retry affordances without changing DTO status.
6. Capture before/after representative screenshots and browser evidence.

### Required checks

- invalid/null timestamps have deterministic fallback copy;
- user locale is respected where the product contract allows it;
- business/calendar timezone is explicit where backend data requires it;
- status labels remain backend/presentation-DTO owned;
- no formatter change alters request serialization or mutation payloads.

## Final implementation order

The serial queue should be:

1. System Map reconciliation;
2. UX architecture decisions;
3. shared surface/motion foundation;
4. formatting/state consistency;
5. identity entry/onboarding;
6. Work;
7. Chat;
8. personal context/command center;
9. profile/visibility/location;
10. social/sharing/rides/business sweep;
11. CSS/obsolete-surface cleanup;
12. runtime/visual/accessibility closeout.

This order ensures shared visual/state contracts stabilize before module work,
and cleanup happens only after all consumers have had a chance to migrate.
