# Modern Minimal Adaptive Product Assessment

Status: assessment for the next implementation master plan

Baseline: `b3a130c` (`main` after the recovered adaptive mobile-first program)

## Executive finding

The previous master plan improved contracts, validation, route ownership, and device policy, but it did not create a materially new product surface. The authenticated routes still render one generic `SurfaceContentView` grammar. The new archetype value mainly changes grid columns; it does not give Work, Chat, Calendar, Business, Circles, or Profile a distinct interaction model.

The product is therefore technically more standardized but visually still too verbose and too dashboard-like. The next master plan must change the visible hierarchy, not only the supporting contracts.

## Current-state evidence

### 1. The shell explains itself too many times

`AuthenticatedShellView.vue` currently renders all of the following at once:

- brand copy explaining the product
- navigation label and description for every primary route
- page eyebrow, title, and description
- a full Vision input and a second Vision hint
- a contextual Vision link
- account label and logout control

The same route meaning is repeated in the rail, header, hero, action cards, and content sections. Navigation is understandable only after reading a large amount of copy, which is the opposite of the intended calm, adaptive surface.

### 2. Every workspace route still uses one generic visual grammar

`WorkspaceSurfaceView.vue` loads a surface-specific backend model, but all routes render the same `SurfaceContentView.vue` structure:

1. large hero card
2. action cards with label and description
3. metric cards with label, value, and detail
4. status card
5. three-column section cards
6. rows with title, description, metadata, badge, Open, and Vision actions

The current `archetype` class changes a few grid rules. It does not change ownership, hierarchy, interaction, or information density. This is why the pages feel the same.

### 3. Static copy is mixed into product configuration

`shellDefinitions.ts` contains more than one hundred title, description, supporting-text, section-description, and action-description values. Many are implementation explanations such as “phase-one rule”, “canonical detail ownership”, or “this route proves”. These are useful planning notes but should never be rendered as end-user UI.

The route configuration should contain compact user-facing labels, navigation metadata, and action intent only. Product rules belong in backend contracts and living documentation.

### 4. Work cannot support high-volume discovery

`shellSurfaceData.ts` truncates available work and owned work to four rows. Chat is truncated to six conversations and four contacts; business and calendar are also truncated to small slices. The backend already has a paginated `/quests/search` contract with query, date, radius, schedule, and sort inputs, but the shell does not expose that contract as a real discovery experience.

The current product is therefore good for a summary but poor for finding one item among hundreds or thousands. The next Work surface needs backend pagination, compact visual rows, filters, sorting, saved context, and a focused detail surface. Voice remains the fastest way to express intent; visual browsing becomes the confirmation and comparison layer.

### 5. Vision is voice-capable but still console-shaped

Vision already has backend-owned runtime context, voice controls, field requests, review actions, canvas blocks, and typed shell handoff. That is valuable infrastructure. The visible renderer still exposes terminal-like feed lines, status notes, runtime labels, hint rails, navigator pills, and handoff chrome in one surface.

The next Vision surface should show only the current task, the last meaningful result, one next action, and optional progressive detail. Voice and prompt remain primary; visual feedback should make listening, processing, selected values, proposed changes, review, and completion immediately understandable without turning the screen into a transcript log.

### 6. Mobile is responsive, not yet mobile-first

The current mobile behavior hides the rail and exposes a horizontally scrollable bottom navigation with all primary routes plus Vision. The same verbose header and generic cards remain. Safe-area, focus, and reduced-motion foundations exist, but mobile density, gesture behavior, keyboard behavior, and the visual priority model are not yet designed as a first-class experience.

Watch policy exists as a backend contract, but there is not yet a concrete compact visual handoff contract or native-client-ready surface model for “glance, acknowledge, confirm, hand off”.

## Product target

The next version should feel like a quiet, intelligent tool:

- one primary question or action per surface
- navigation communicates location with labels and active state, not explanations
- descriptions are hidden by default and appear only when ambiguity or risk requires them
- lists show the few fields needed for scanning; detail opens only on intent
- every screen has a clear current location, next action, and escape path
- Voice/Vision is the primary command surface, while visual UI provides confirmation, comparison, selection, and progress
- backend DTOs own permissions, labels, state, density hints, pagination, and action availability
- web, iPhone, and Watch consume the same semantic surface contracts with device-specific density

## Non-negotiable information hierarchy

Every surface must answer these questions in this order:

1. Where am I?
2. What matters now?
3. What can I do next?
4. What additional detail is available if I ask for it?

If a text block does not answer one of those questions, it is not shown in the default surface.

## Proposed surface archetypes

- `command`: one prompt, one voice action, one current state
- `focus-list`: compact high-volume list with search, filters, and one selected item
- `inbox-detail`: list and selected conversation/detail pane
- `timeline`: date or time grouped items with compact next-action cards
- `operations`: one attention summary plus operational queues
- `profile`: identity summary plus a short set of direct actions
- `review`: compact proposed-change summary with inline field corrections and one confirmation action

These are real interaction archetypes, not only CSS modifiers.

## Backend and client boundary

The frontend should not derive business meaning from raw entities. Backend read models must provide:

- compact display title and optional supporting label
- viewer-safe status and attention state
- action availability and action intent
- pagination/cursor metadata
- device density and field-visibility hints
- stable deep-link/navigation targets
- voice/prompt handoff context

The frontend may choose layout, interaction affordance, and animation, but it must not recreate permissions, workflow rules, statuses, ranking, or visibility decisions.

## Success criteria for the next program

- A first-time user can identify the current surface and primary action without reading explanatory paragraphs.
- Work can browse and narrow a large result set without loading the whole dashboard or relying on terminal-style prompts.
- Chat, Calendar, and Business have visibly different interaction models and priorities.
- Voice commands always have a compact visual state and a recoverable next action.
- Mobile exposes no more than the essential navigation and preserves the same semantic state as web.
- Watch receives only glance-safe summaries and safe actions, with explicit phone handoff for detail.
- Default screens contain no planning language, internal ownership explanations, or duplicated descriptions.
- Every child plan has route-level acceptance evidence; no plan can become complete because only a shared component or build changed.
