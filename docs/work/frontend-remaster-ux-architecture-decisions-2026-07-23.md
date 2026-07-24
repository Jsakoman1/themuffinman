# Frontend App Remaster — UX Architecture Decisions

Date: 2026-07-23  
Status: approved decision baseline for implementation

## Product-level rules

The authenticated Web client behaves like a focused application workspace: a stable shell, one active surface, clear context, and a small number of deliberate actions. Existing capabilities remain the source of truth. The remaster changes how a user reaches and understands those capabilities, not what the product is allowed to do.

Every authenticated surface uses the same hierarchy:

1. Shell: stable module rail and session-level navigation.
2. Surface header: title, context, primary action, and optional utility actions.
3. Main content: collection, detail, guided flow, or conversation.
4. Context utility: filters, metadata, related actions, or secondary information.
5. Feedback: loading, empty, error, success, and recovery in the same surface.

The primary action is singular and backend-derived where an `allowedActions` or equivalent contract exists. Secondary actions stay visible but quiet. Destructive actions require explicit confirmation and explain the consequence. A disabled action explains why when the existing contract provides a reason; the UI does not infer permission.

## Surface decision matrix

| Surface need | Default presentation | Use a tab when | Use a drawer/sheet when | Use a dialog when |
| --- | --- | --- | --- | --- |
| Browse many records | Collection surface with toolbar and filters | A user needs two independent collections open in parallel | A compact filter/context view is needed on narrow screens | Never for the collection itself |
| Inspect one record | Same-route detail surface with back/context preservation | The product already exposes independent subcontexts | Related metadata or a short action context should remain beside the detail | Only for a short confirmation or focused utility |
| Edit an already open record | Inline edit on the detail surface | Editing truly independent contexts is already supported | A small secondary form must preserve the current record context | Only when the operation is confirmation-sensitive or has no useful parent surface |
| Create a mapped entity | Dedicated route when the flow is multi-step; guided intake when short | Independent draft contexts are supported by the current contract | A compact create flow should keep the source collection visible | Only for a small, self-contained action with no meaningful navigation context |
| Filters/search refinement | Inline toolbar on desktop; sheet on mobile | Independent saved searches are being compared | The filter set is temporary and must return to the same collection | Never for ordinary filtering |
| Confirmation/destructive action | Inline feedback after action | Never | Never | Focused confirmation dialog with cancel as the safe default |
| Help/keyboard/context explanation | Inline utility or popover | Never | On mobile, a bottom sheet may host longer utility content | Dialog only when focus must be trapped |
| Chat conversation | Workspace/detail split with composer | Only for independent conversations or existing route semantics | Mobile conversation list becomes a sheet/back layer | Never for normal message composition |
| VisionForWeb | Inline assistant host within the current Web surface | Never as a shell destination | May collapse into a responsive panel if the host contract supports it | Never launch the detached Vision terminal |

Tabs are therefore reserved for independent, parallel contexts. They are not a replacement for route navigation, browser history, or a way to hide complex state. A detail surface keeps its canonical route and preserves return context through existing router/query state.

## Route and context rules

- Canonical routes remain unchanged unless a child plan proves an existing route is detached or duplicated.
- A collection-to-detail journey uses the existing route target and preserves the collection’s search/filter context where the current contract supports it.
- Back returns to the prior meaningful context, not automatically to the application root.
- Create and edit surfaces do not perform a full-page reload. On success they update the owning surface using the existing query/invalidation pattern.
- Links and buttons must have one semantic job. A row may be clickable, but nested buttons must stop propagation and expose their own label.
- A route without a mapped Web surface is not made reachable by presentation work.
- The detached Vision terminal remains unreachable from Web UI. Inline assistance is only `VisionForWebHost` and its existing handoff contract.

## Turn-based interaction rules

Turn-based interaction is a guidance layer for intent discovery and multi-step user journeys. It is not a second mutation API. A turn may:

- explain the current surface and suggest the next mapped action;
- open an existing route, collection, detail surface, or inline host;
- prefill an existing form only where the current contract explicitly permits it;
- show a confirmation or ask for missing context before a mapped action.

Direct UI remains available for known actions. Every turn-based handoff resolves to an existing route, action ID, API contract, or VisionForWeb inline host. Unknown or unsupported intent receives an honest recovery state with a useful next action; it must not fabricate a button or capability.

## Responsive rules

- Desktop: stable rail, main surface, and optional utility rail; preserve a readable maximum content width.
- Tablet: rail may collapse to an icon rail; utility context becomes an overlay or sheet without changing the route.
- Mobile: one primary column, explicit back affordance, bottom sheet for temporary filters/context, and sticky primary action only when it does not obscure content or keyboard focus.
- Tables become cards or stacked labeled rows when horizontal scrolling would hide the primary action. Existing data meaning and action ordering remain unchanged.
- Dialogs and sheets must fit the viewport, respect safe areas, and restore focus to the invoking control on close.

## Motion, focus, and recovery rules

- Motion communicates hierarchy and state change: short surface transitions, subtle hover/press feedback, and restrained list insertion/removal.
- No motion is required to understand content or complete an action.
- `prefers-reduced-motion: reduce` disables nonessential transforms and uses immediate state changes.
- Keyboard focus is always visible; dialogs/sheets trap focus while open and return it on close. Escape closes only the topmost temporary surface.
- Loading preserves layout where possible. Empty states explain what is empty and offer the mapped next action. Errors preserve entered data and expose retry/recovery without discarding context.
- Success feedback is local to the action and does not force a navigation unless the existing workflow requires it.
- Async actions expose pending state and prevent duplicate submits while pending.

## Standardized action hierarchy

Each surface may have one primary action, a small set of secondary actions, and an overflow for low-frequency utilities. Actions are ordered by user goal, not by API order. Presentation components consume backend-provided action availability; duplicated local gates are audit targets, not new authority.

## Implementation handoff

The shared foundation child plan should provide the primitives needed by this decision record: `SurfaceHeader`, collection toolbar/context, detail header/utility rail, dialog/sheet behavior, inline edit, guided intake, state feedback, focus handling, motion tokens, and responsive tokens. Domain slices then apply these primitives without creating module-specific shell variants.

Validation for each implementation slice must cover type-check/build, interaction-contract and route audits, keyboard/focus behavior, reduced motion, narrow viewport behavior, and at least one collection-to-detail-to-action journey. Any finding that changes a route, capability, permission, workflow, or Vision boundary returns to the System Map and relevant living documentation before implementation continues.

