# Linear-inspired product application redesign analysis

## Scope and source boundary

This is a design-and-interaction reference program, not a request to copy Linear.
TheMuffinMan must retain its own name, Social Useful Network direction, domain model,
routes, backend authority, and user-facing language. Do not use Linear marks, icons,
copy, identifiers, assets, issue/project terminology, or exact screen layouts.

The supplied screenshots are references for interaction density, hierarchy, navigation,
and state presentation only. They were reviewed as eight desktop states: list, board,
timeline, project detail, task detail with a side panel, nested initiative list, inbox
split view, and an AI/conversation panel.

The public `linear/linear` repository is not the private Linear application client. Its
README describes SDK, import, and GraphQL code-generation packages, so it must not be
treated as implementation evidence for their application UI. The public product
documentation is the source for behavior observations below.

## Existing audit baseline and how it constrains this program

The repository audits run during this planning pass found 43 routes and 42 concrete
route surfaces. The authenticated shell's navigation, create handoffs, and action
rendering are connected. This makes the shell registry and current route surfaces the
reuse boundary; the redesign must not replace them with a second application frame.

The stale-surface audit reports one likely-unused `AppActionMenu.vue` and one stylesheet
that needs review. The object-action plan must decide whether to adopt the former as the
single action-menu primitive or remove it in an isolated cleanup; it must not introduce
a third menu system. The frontend state audit also reports existing overlapping
Circle-block/create-request workflows and repeated feedback signals. New object actions
must use the established backend action/mutation paths and improve consolidation only
where a focused child plan proves it safe.

The endpoint linker currently sees 200 backend endpoints, of which 128 are linked to
frontend clients. The other 72 cannot be assumed unavailable product features: many are
admin or backend-only. An interaction control is therefore eligible only when its
specific permitted client contract is identified, not merely because a similarly named
backend endpoint exists. The runtime-acceptance ledger currently has 27 passing scenarios
and zero pending; it is evidence bookkeeping and must be complemented by a real browser
run for every changed interactive path.

## Execution readiness and reuse boundary

This program starts from a verified first increment, not a blank frontend: the
`frontend-desktop-product-redesign-master` has already delivered the dark shell,
shared tokens, global search/create surfaces, and Work discovery treatment. Earlier
verified plans also already establish navigation information architecture, card/action
and preview semantics, universal search and create, business favorites, and global
Vision entry. Each new child is a gap-closure plan: it must name the foundation it
extends and may introduce a primitive only after the audit proves that no suitable
shared component exists.

`frontend-contract-action-dialog-standardization.yaml` is currently active and overlaps
the shared action-dialog, tokens, and action paths. It is an explicit start gate for
object-interaction and detail-panel work. Keep the Linear-inspired master as `draft`
until that plan is verified, or deliberately select a non-overlapping child first.

The current Git baseline is valid, but the working tree contains the verified foundation
changes and the new draft plans. Before activating a child, create a clean, intentional
commit boundary and refresh that child's baseline to the current committed revision.
This is required for `make work-verify` to correctly associate changed paths and evidence;
it is a control requirement, not a request to commit automatically.

## Evidence-based interaction model

### Object-first, view-second

The screenshots show the same work objects rendered as dense rows, grouped columns,
timeline bars, and detail context. Linear's documentation confirms that views are
different perspectives over the same underlying work rather than separate records.
Display settings change grouping, order, layout, and visible fields; filters change
membership. This is the key principle to reuse: a Work quest, Thing listing, Ride, or
booking has one authoritative backend DTO and can gain several read modes only when
the backend can prepare the required stable summary fields.

Sources:

- https://linear.app/docs/conceptual-model
- https://linear.app/docs/display-options
- https://linear.app/docs/custom-views

### Three-level navigation and location persistence

The screenshot rail has a stable global layer (Inbox, personal work, reviews), a
workspace/module layer, then personal favorites. Its current location is always visible
through the active rail item and a compact page bar. The top-left area combines workspace
switching, Search, and a global creation affordance. The app does not put all possible
destinations in the page body.

TheMuffinMan mapping is:

| Reference role | TheMuffinMan role | Existing authority |
| --- | --- | --- |
| attention inbox | Notifications and Activity | backend notification/activity DTOs |
| personal execution | My quests, applications, bookings, rides, borrowing | module read endpoints |
| workspace/module area | Work, Chat, Calendar, Business, Circles, Things, Rides | `shellRouteRegistry.ts` |
| favorites | user-owned saved routes/searches only after backend persistence exists | saved-search and future preference APIs |
| object detail | quest, application, conversation, listing, ride, booking, person | canonical detail routes |

The current authenticated shell already owns primary/secondary navigation and must be
extended, not replaced. A future favorite is never a frontend-only list of arbitrary
URLs; it must be a user-owned, safe, backend-readable preference.

Sources:

- https://linear.app/docs/inbox
- https://linear.app/docs/favorites
- https://linear.app/docs/conceptual-model

### Dense collection grammar

The row screenshots repeatedly use a stable scan order: compact state marker, small
identifier, title, then secondary property chips and people/date metadata aligned to the
right. Hierarchy is shown by indentation and faint connector lines, not by large cards.
The board screenshot preserves the same metadata, but groups it by state and gives each
group a visible count and an add action.

Adaptation rules:

- Prefer rows for high-volume Work, notifications, bookings, applications, Chat lists,
  Things, and Rides; reserve cards for genuinely visual comparison or compact board mode.
- Treat the colored dot/icon as an outcome/state signal with text still present; never
  use color alone for permissions, safety, or workflow state.
- Keep display fields separate from filters. A user may hide reward/location metadata
  only after a product-specific display preference contract exists; current backend
  filtering remains authoritative.
- Every clickable row has one primary open/preview target. Nested buttons, chips, and
  overflow actions must stop propagation and retain keyboard focus semantics.

### Detail, preview, and utility side panels

The detail screenshots use a scalable three-part arrangement: collection/navigation,
main object context, and a contextual utility pane. The utility pane contains mutable
properties, people, status, dates, resources, and related objects; the main pane holds
the title, meaningful body, activity, and composer. A side panel is toggled, not a
second route-level duplicate.

For TheMuffinMan, this becomes one shared detail-surface contract:

- main: backend-provided title, summary, activity, conversation, or rich content;
- utility: allowed actions and compact properties supplied by the existing DTO;
- preview: a route-preserving temporary view for a row where a canonical detail exists;
- mobile: utility content is an inline section or accessible sheet, never permanently
  hidden.

No frontend component may infer availability of an action. A missing action is a backend
contract gap and must be planned with backend work before the control appears.

### Filters, display controls, and saved views

The screenshots place filter, display, grouping, side-panel, and overflow controls in a
consistent top-right toolbar. Public docs show a strict distinction: display options
change layout/grouping/order/visible fields; filters determine which objects belong;
view preferences may be personal or workspace defaults. This should become a common
frontend interaction grammar, but not a generic frontend query builder.

For current scopes:

- Work keeps existing backend `q`, preset, sort, and scheduled parameters; display
  mode is local-only if it does not affect product truth.
- Calendar keeps its existing backend event data and client-only month/week/day layout.
- Business, Things, Rides, and People only gain filters exposed by their APIs.
- Saved searches remain server-owned; a future saved view requires a typed backend
  preference/read-model contract, visibility checks, and explicit sharing semantics.

Sources:

- https://linear.app/docs/display-options
- https://linear.app/docs/custom-views

### Action hierarchy, ellipsis, star, bell, and icon controls

The screenshots use icon controls as named, repeatable verbs rather than decoration:

- create icon: global direct creation entry;
- search icon: global retrieval entry;
- star: personal favorite toggle for the focused object/view;
- bell: notification/subscription state, only where the backend owns that state;
- side-panel icon: open/close contextual properties;
- filter/sliders/view icons: alter the current collection presentation;
- ellipsis: non-primary contextual actions, including destructive operations;
- small avatars: participants/owners, not generic decoration.

Every icon must have a visible tooltip, accessible name, keyboard focus, disabled state,
and a documented action boundary. Existing `AppIconButton`, `AppActionMenu`,
`AppActionDialog`, and `AccountMenu` are the implementation base. The design program
must not add a different icon library or an unlabelled icon-only control system.

### Creation, forms, drafts, and command input

Linear documents a global creation shortcut, a compact composer, full-screen option,
contextual property edits, and local draft preservation. It also documents a context-aware
command menu, where commands vary with the current screen/selection. This is a behavior
model, not a mandate to make every TheMuffinMan action keyboard-only.

Adaptation:

- `UniversalCreateMenu` stays an entry selector. Each selected domain uses its existing
  direct route or API-backed flow.
- Work creation retains `WorkQuestCreateView` and its backend validation. Introduce a
  shared compact composer only after it can hand off the same DTO/form contract and
  preserves accessible error recovery.
- Vision's prompt composer stays a distinct backend-governed conversational surface;
  it must not be conflated with a command palette. Its send affordance appears inside
  the input dock, supports Enter/modified-Enter only when documented, and shows
  listening/processing/applied-state signals from the backend.
- Draft persistence must be explicit. Local temporary drafts can be client-only;
  cross-device drafts require backend persistence, expiry, ownership, and recovery
  rules.

Sources:

- https://linear.app/docs/creating-issues
- https://linear.app/docs/conceptual-model

### Keyboard, selection, and motion

The public documentation describes shortcuts as parallel access paths to the same
actions: buttons, contextual menus, command menu, and keyboard all reach the same
backend-owned operation. It documents keyboard sequence navigation, context-sensitive
command results, selection, and undo. For TheMuffinMan, keyboard support must begin with
non-conflicting, discoverable shortcuts and never silently submit destructive or
state-changing actions.

Motion should be short and explanatory: a panel moves from its invoking edge, row
selection changes surface emphasis, a composer grows with input, and async state replaces
the action with a spinner/status. It must respect `prefers-reduced-motion` and never
cover a failure with an optimistic animation.

Sources:

- https://linear.app/docs/conceptual-model
- https://linear.app/docs/inbox

### Selection is a state machine, not a styling effect

The screenshots distinguish a hovered row, keyboard focus, an open detail, an active
tab, a selected item in a split view, and a board card. These are separate states:

| State | User intent | TheMuffinMan rule |
| --- | --- | --- |
| hover | discover an available action | reveal only secondary affordances; do not select |
| focus | keyboard target | show a focus ring and retain native tab order |
| preview | inspect one object without losing list context | only where a canonical detail route exists |
| selected | choose one object for a local action | make selection visible and escapable with `Esc` |
| multi-selected | apply a batch action | do not build until a backend batch contract exists |
| editing | change one explicit permitted field | lock only the edited control and show recovery |

The first safe scope is keyboard focus plus one selected row/preview. Multi-select,
bulk actions, board drag-and-drop, and undo are blocked until backend actions define
permission, partial-failure, ordering, and refresh semantics.

Sources:

- https://linear.app/docs/conceptual-model
- https://linear.app/docs/editing-issues
- https://linear.app/docs/board-layout

### Surface toolbar and action registry

Each collection surface should reserve a consistent toolbar sequence: local find-in-view,
backend filter, display/group/sort preference, side panel, then overflow. A control is
hidden rather than disabled when the endpoint cannot support it. Local find-in-view only
searches loaded rows; it is never presented as universal search.

Buttons, context menus, keyboard shortcuts, and command palette entries must use a
single action descriptor: identifier, label, icon token, shortcut, tone, confirmation,
DTO/API enabled state, invoking surface, and success/failure refresh behavior. The
descriptor cannot infer a permission or workflow transition.

The initial registry is limited to current actions: quest lifecycle, application
decisions, saved-search pause/delete, notification state, chat message actions, and
existing module create routes.

### Additional visual rules from the screenshots

- The desktop canvas uses a framed main surface and interior dividers before cards.
- Three type levels are sufficient: muted metadata, readable body/row text, and one
  dominant page/object heading.
- Status, priority, category, source, and people are compact semantic tokens; color is
  supplementary and never their only meaning.
- Avatars represent ownership or relationship context only and need accessible names
  when they are actionable.
- Hover, selected, disabled, loading, error, and success use distinct treatments.
- Shadows belong to popovers and dialogs, not every row.
- Vision/assistant output is a participant surface with evidence and next actions; its
  composer remains anchored to the active context.

## Reference screen inventory

| Screenshot pattern | Reusable interaction lesson | TheMuffinMan candidate |
| --- | --- | --- |
| My issues list | dense rows, tabs, right-aligned metadata, quick filters | Work, applications, bookings, notifications |
| Board | same object schema in grouped columns | Work status board only after backend exposes an unambiguous state/read model |
| Timeline | dates and dependencies as a spatial mode | Calendar and later rides/bookings; no fake timeline from incomplete data |
| Initiative tree/table | indentation, rollups, state chips, sticky headers | Business operations and future circle/work coordination summaries |
| Detail with utility rail | canonical main context plus toggled properties | Quest, Thing, Ride, booking, conversation detail |
| Inbox split view | list selection retains context while reading/acting | Notifications then Chat only where backend detail supports it |
| AI/output panel | input anchored at bottom, streamed/structured evidence above | Vision only, using current typed response contract |
| Favorite/side rail | personal shortcuts and compact global actions | persisted saved searches first; broader favorites only after contract work |

## Delivery decision

The existing verified `frontend-desktop-product-redesign-master` remains historical
foundation work. The new master below is a draft delivery program. It starts with an
architecture and interaction-contract audit, then shared shell and command primitives,
then collection/detail/composer/inbox applications. It deliberately leaves backend
gaps as explicit gates rather than recreating Linear's product model in Vue.

## Fast continuity is the remaining high-value interaction gap

The strongest remaining application-quality improvement is continuity rather than a
new visual effect: a user should open an object, inspect it, and return to the same
permitted collection context without re-finding their place. Existing search is typed,
ranked, paginated, and backend visibility-filtered; saved searches are already
server-owned. What is not yet standardized is route-scoped handling of selected preview,
scroll position, local display preference, stale response discard, and evidence for
those transitions.

The corresponding child plan is deliberately conservative. It may preserve supported
backend query parameters in the URL and local presentation state in memory, but may not
invent an offline cache, optimistic mutation, virtualization, infinite scroll, private
URL payload, or a new saved-view model. Those additions require independent API,
ordering, accessibility, ownership, and recovery contracts.
