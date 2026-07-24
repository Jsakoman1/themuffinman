# UI linking and Vision inline residual analysis

Baseline checks already pass: frontend type-check, production build, web-surface contract, admin-agent UI validation, and the UI entry-point audit. The existing UI action-integrity and browser-evidence masters are treated as baseline-only evidence.

Residual findings for this master:

- Vision and backend publish `/things/requests`, but the frontend router has no matching route.
- Vision publishes `/business/availability` in one read-only path while the frontend canonical surface is `/business/calendar`.
- Work, Things, People, and Rides collection rows commonly select a preview through a root click and expose detail only in a secondary context link; the primary object label is not itself a canonical destination.
- The modern-surface validator reports five failures. Some are stale source-shape assertions (helper/computed implementation details); each must be classified as a real UX gap or a validator defect before repair.
- The requested Vision inline journeys are: quest resolution and open, guided quest creation with review/confirmation, owner-scoped circles opening, navigation-only circles handoff, and recipient-resolved direct message sending with confirmation.

## Per-plan analysis

### 01 — route foundation

This is a real implementation slice. `/things/requests` has a backend publisher and a Vision route contract but no Vue route. `/business/availability` is an obsolete publisher path because the canonical Web surface and current Vision route matrix use `/business/calendar`. The component chosen for `/things/requests` must expose borrower requests intentionally; simply aliasing the URL to the discovery list without request scope is not sufficient. The modern-surface validator must be changed only after each failed assertion is classified as either a current UX invariant or a stale source-shape assertion.

### 02 — collection actions

This is a shared-surface slice with broad regression impact. Work, Things, Rides, and People currently attach selection behavior to a `SurfaceRow` while the canonical detail link sits in a separate context panel. The plan must preserve action buttons, keyboard selection, preview state, and query context while making the object itself a clear primary destination. The child must not redesign every module; it should standardize the shared row contract and change only affected views.

### 03 — Vision quest and circles

The base capabilities are already covered by verified Vision plans: `VIEW_QUEST_DETAIL`, `CREATE_QUEST`, `VIEW_CIRCLES`, candidate resolution, and the inline shell host. This child therefore owns residual prompt-level acceptance and any defects found while replaying the exact requests. “Open quest Suitcases” requires unique visible quest resolution and safe ambiguity handling. “Create new work” must remain the existing guided review/confirmation flow. “Open my circles” and “go to circles” must remain distinct semantic intents even though both may end at `/circles`; the former proves viewer-scoped content and the latter proves navigation-only handoff.

### 04 — Vision chat

Existing verified work supports opening a chat with a resolved person through `VisionChatExecutionService`. It does not prove sending a message: current prefix handling can interpret “send message to” as a chat-opening request, but no message body collection, message preview, confirmation, or send execution is present in that service. This child must extend the existing boundary rather than create a parallel chat API. It must preserve privacy-safe ambiguity handling and end at the authorized `/chat/:conversationId` route.

### 05 — runtime closeout

This is the only place where the five exact user prompts are counted as acceptance. Source tests and build output cannot prove inline behavior. Runtime evidence must record prompt, resolved intent/target, action or review state, final route or mutation result, confirmation, and recovery for ambiguous/missing targets.

The plan deliberately does not re-open already verified module-wide action behavior unless a changed route, shared row primitive, Vision contract, or affected runtime scenario triggers the retest.

## Second-pass preflight findings

- All planned source files currently exist. The only intentionally missing path is the fresh runtime artifact `docs/runtime-evidence/ui-linking-vision-inline-runtime.json`, which must be created by the runtime child.
- The current route comparison confirms `/things/requests` is absent from the Vue router and `/business/availability` is absent while `/business/calendar` is canonical.
- Child and inventory identifiers now match one-to-one, and all strict implementation tasks use concrete file paths rather than directories or globs.
- The new master overlaps with verified Vision plans only through explicit baseline references; it does not re-enqueue their completed child tasks.
- The closeout owns the living-document update and stable backlog IDs so route and Vision behavior changes cannot close with code-only evidence.
- `make system-map-impact` was run at `2026-07-23T16:24:47Z`; its advisory recommendations for the System Truth Registry, System Drift Control Registry, and System Map are reviewed in the master plan. It does not promote this draft to active or verified status.
