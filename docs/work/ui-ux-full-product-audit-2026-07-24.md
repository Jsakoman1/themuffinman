# Full product UI/UX audit — 2026-07-24

Status: analysis only. No UI implementation is included in this audit.

## Executive conclusion

The product has a coherent dark workspace shell, but the current interaction model is still collection-first and action-heavy. Many screens expose the complete data surface, multiple actions, explanatory copy, a preview rail, and a secondary navigation layer at the same time. This conflicts with the intended turn-based model: one current context, one next action, and progressive disclosure for everything else.

Business has an additional structural problem. The UI treats `activeBusinessProfileId` as one session-level value and spreads owner profile, offerings, schedule rules, booking queue, customer bookings, and public discovery across separate routes. That cannot scale to a user who owns or operates several businesses while also booking appointments as a customer. The product needs an explicit business workspace context and a unified personal/business calendar model before more Business features are added.

## Cross-product findings

### P0 — fix the interaction grammar before polishing individual pages

1. **Too many simultaneous actions.** The shell header exposes Create, Search, Vision, and account context; module hubs then repeat several actions; collection rows expose actions; right rails expose more actions. The user is repeatedly asked to choose from a menu instead of being guided toward the next step.
2. **The current route is not always the current task.** A collection page can show a list, preview, filters, empty-state instructions, and mutation controls together. Selection changes the preview, but not the task mode. Every surface needs an explicit mode: browse, inspect, create, review, or complete.
3. **Preview rails are overused.** The same list/preview structure appears in Work, Business discovery, availability, exceptions, bookings, notifications, and likely other surfaces. It is useful for comparison, but harmful when the user only needs to complete one action. On mobile it becomes a second navigation problem.
4. **Empty states are informational, not directional.** Several empty states explain what is missing but do not present one obvious next action or a meaningful “skip for now” path.
5. **Dense copy competes with object identity.** Eyebrow, title, description, toolbar title, helper paragraph, preview label, and backend-rule explanation often appear before the user can act. Most helper copy should be moved behind an info affordance or shown only when the user is blocked.
6. **Responsive behavior mostly collapses columns, not complexity.** A desktop two-column preview becomes a vertically stacked list plus context. That preserves the amount of information rather than simplifying the task for a narrow screen.
7. **Shared components do not yet enforce a shared task model.** `CollectionToolbar`, `SurfaceRow`, `AppDialog`, and preview rails standardize appearance, but each screen still decides independently how many controls, sections, and mutations are visible.

### P1 — shell and navigation

- The left rail contains module navigation, contextual child navigation, attention, activity, saved searches, pinned context, profile, and settings. This is a lot of navigation grammar before the main surface begins.
- The Business module is simultaneously a top-level module, owner workspace, discovery surface, customer booking surface, and calendar entry. It needs two explicit modes: **My schedule / bookings** and **Discover / book services**, with owner business selection inside the owner mode.
- The top header's Create, Search, and Vision controls are visually equal even though they represent different intents. Search is discovery, Create is mutation, and Vision is assistance; their visual hierarchy should not suggest equivalent frequency or risk.
- The shell should show only the active module's relevant child destinations by default. Low-frequency destinations such as availability exceptions, service schema, saved searches, and settings should be reachable through “More” or a contextual command, not always compete with the primary task.
- Attention should be a compact count and one next action, not another destination that duplicates Notifications and Activity.

### P1 — turn-based interaction model

Adopt one state machine for all module surfaces:

`orient → choose → inspect → act → review → complete`

Only one state should be expanded at a time. Examples:

- Browse Work: list and one selected row. Entering application is a deliberate transition to inspect/review.
- Create quest: ask for title, description, reward, and timing first; reveal visibility, location, and advanced options only after the core draft is valid.
- Book service: choose business → choose service → choose options/demand → choose date → choose available slot → review price/conditions → confirm.
- Owner booking: show “needs action” queue first; details and secondary lifecycle actions appear after selecting one booking.
- Settings: show one setting group at a time with a saved/unsaved state, not a long multi-purpose form.

## Business-specific audit

### Current structural issues

1. `/business` is a section hub with many links, not an operational dashboard. It does not answer: “Which business am I operating?”, “What is next?”, or “What appointments need attention?”.
2. `sessionStorage.activeBusinessProfileId` is a single implicit context. It is not visible enough, is not a robust multi-business selector, and cannot represent parallel work across two businesses.
3. `/business/calendar` currently maps to `BusinessAvailabilityView`, which edits schedule rules. It is not a calendar of actual bookings. `/business/bookings` is a separate queue, so the user must mentally combine schedule rules and appointments.
4. Owner profile, offerings, service setup, availability, exceptions, bookings, and calendar are separate routes with repeated business context. The user loses the selected business and often has to rediscover the relevant offering.
5. Customer bookings are under `/business/my-bookings`, while owner bookings are under `/business/bookings`; both are booking timelines but have different navigation and visual grammar. A user who is both owner and customer needs a combined “My schedule” surface with role filters.
6. Public business booking begins from a dense profile/detail page and opens a large dialog containing demand, date-time, availability, quote, note, and actions. The domain is flexible, but the UI presents all possible concepts in one form instead of progressively asking only what this offering requires.
7. The public page selects an offering, but the business/service/date context is not represented as a persistent step header. Backtracking and changing service/date are therefore more expensive than necessary.
8. The owner service setup page combines fulfillment, quantity, typed schema display, pricing display, and resource creation. It is powerful but not a usable setup flow for a non-technical business owner. It needs a guided setup sequence and previews of the resulting customer booking experience.
9. Calendar and availability are conceptually mixed. Availability rules, exceptions, capacity, resources, and actual bookings need separate layers in one calendar workspace rather than separate unrelated pages.

### Target Business information architecture

Top-level Business should expose only:

- **My schedule** — all appointments the user owns, operates, or booked; filters for business, role, status, service, and date.
- **Businesses** — owned/managed businesses with a clear active-business selector.
- **Discover** — public businesses and services to book.

Inside an active owned business:

- **Today / Overview** — next appointments, requests needing action, occupancy/capacity signal, and one primary action.
- **Calendar** — bookings as the main surface; availability, exceptions, resources, and capacity as togglable layers.
- **Services** — offerings and guided service setup.
- **Business profile** — identity, public page, visibility, and gallery.

Advanced settings should not be first-level navigation. They belong inside Services or Calendar as progressive configuration steps.

### Target booking flow

`Business/service card → service configuration → date range → available slots → review → confirmed booking`

The slot picker must be service-aware:

- fixed-duration service: only valid block sizes appear;
- quantity/capacity service: slots show remaining capacity;
- resource service: slots are filtered by resource availability;
- multi-step or field service: the UI shows the selected duration, travel/quantity implications, and any required demand before the slot is considered valid.

The user should never see a generic calendar grid that allows invalid choices and then discovers the rule after submission.

## Page-by-page findings and proposed direction

| Surface | Current problem | Proposed simplification |
|---|---|---|
| Home | Several attention categories are presented as equal rows; it is a routing table rather than a next-action surface. | One “Next up” item, then compact grouped counts; open the relevant module directly. |
| Work / Find work | List, search, options, Vision, offer action, and context rail compete. | Default to ranked list + one selected item; move filters into a single filter drawer and make “Apply” the only transition. |
| Work / My work | Same discovery grammar is reused for owned work even though owner actions differ. | Separate “needs action”, “active”, and “completed” modes; show one lifecycle action group on detail. |
| Work / applications | Application status and actions are likely spread between list, preview, and detail routes. | Make application queue decision-first: waiting on me, waiting on them, completed. |
| Work / quest detail | Large detail surface can expose many actions at once. | Pin one allowed next action; place other lifecycle actions under overflow with confirmation. |
| Chat | Conversation list and empty thread create a large unused workspace; New chat/New group/Person input all compete. | Start with recent/unread conversations; use a focused composer and an explicit empty-state action. On mobile, list and thread should be separate states. |
| Calendar | Current hub is too generic and Business calendar is not an actual booking calendar. | Make Calendar the cross-product personal schedule; use event source filters and a simple day/week agenda. |
| Business hub | Many links, no active business, no operational summary. | Replace with active business switcher + Today/next booking + “Open schedule” as primary action. |
| Business discovery | List + context rail is acceptable for comparison but lacks service/date intent. | Let users search by service, location, date, and availability; show “Book” only after a business/service is selected. |
| Public business page | Profile, offerings, favorite, description, and booking dialog are all visible at once. | Public profile first; service cards second; booking becomes a guided route/sheet with persistent progress. |
| My bookings | Customer bookings are isolated from the personal calendar and owner schedule. | Merge into My schedule with role and business filters. |
| Owner bookings | Lifecycle actions are all visible in row actions and details. | Default queue: requests needing action; one primary action per selected booking, overflow for the rest. |
| Owner calendar/availability | Schedule-rule editor is presented as Calendar. | Calendar shows bookings first; availability/resource layers are side panels or an edit mode. |
| Service setup | Schema, pricing, resource setup, and fulfillment are too technical and simultaneous. | Guided steps: service basics → how it consumes time/capacity → customer questions/options → price → resources → preview/publish. |
| Availability exceptions | Complex rule form appears in one dialog. | Use “Block time” / “Replace capacity” intent first, reveal advanced fields only for replacement windows. |
| Circles | Trust explanation, search, requests, and circle list all compete. | Requests needing decision first; circles second; people search as a separate mode. |
| People discovery/profile | Search, trust context, and actions can become a directory plus profile at once. | Search result first, inspect selected person second, one primary relationship action. |
| Things | Discovery, mine, requests, lending actions share a broad surface. | Separate “Find”, “My listings”, and “Requests” states with a persistent object filter, not mixed controls. |
| Thing detail | Detail and borrow decision can be heavy. | Show availability/trust first, then one borrow/request action; advanced lending terms after intent. |
| Rides | Discovery and offering a ride are different tasks but share a dense surface. | Default to “Find a ride”; offer flow becomes a focused guided creation state. |
| Profile | Profile hub, settings, location, visibility, gallery, and preferences are spread across dense surfaces. | Profile overview with grouped settings; each group opens a focused editor with save state. |
| Location settings | Form combines identity, visibility, gallery, lookup, and privacy explanation. | Split into Profile identity, Location, Visibility, and Gallery; preserve a compact privacy summary. |
| Notifications | Attention center, notification list, preview, mark-read, and activity overlap. | One attention inbox; classify actionable vs informational and route directly to the next action. |
| Activity | Resume items and history are mixed. | “Continue” queue separate from archival activity. |
| VisionForWeb | Global assistant is another prominent action beside Create/Search and opens a popover with another interaction grammar. | Keep it contextual and quiet; when opened, it should inherit the active surface and offer the next action, not a second navigation system. |
| Authentication/onboarding | Forms are simple, but onboarding and recovery should preserve a clear next step and avoid extra shell chrome. | Keep focused; use one action and short progress/feedback. |

## Shared component changes needed

1. Create a `TaskSurface` contract with `mode`, `primaryAction`, `secondaryActions`, `status`, and optional `contextRail`.
2. Create a shared `ContextSwitcher` for active business, role, circle, and other workspace scopes. It must be visible, URL/state-safe, and backend-backed.
3. Create a shared `ProgressivePanel` or step surface for guided flows instead of putting every field in dialogs.
4. Create a shared `ActionOverflow` component. Row actions should default to one primary action and place lifecycle/destructive actions behind a deliberate menu.
5. Create a shared `AttentionQueue` to replace duplicated Home, Notifications, Activity, and owner booking summaries.
6. Define responsive behavior as task transformation: desktop split view → mobile list/detail route or drawer, never just stacked desktop columns.
7. Add density rules to the shell: one page title, one supporting sentence, one toolbar, one primary action group. Everything else is conditional.
8. Add empty-state contracts: explain the state, provide one primary next action, and provide a secondary discovery/skip action only when useful.

## Recommended implementation order

### Phase 1 — interaction foundation

Audit and standardize shell hierarchy, active context switching, task modes, primary-action rules, overflow actions, attention queue, and responsive list/detail behavior.

### Phase 2 — Business workspace redesign

Implement multi-business context, unified My Schedule, owner Today view, real booking calendar, calendar layers, and role/business/date filters.

### Phase 3 — Business guided setup and booking

Convert service setup and public booking into progressive steps. Keep the backend flexible model unchanged; make the UI adapt to the selected offering schema, capacity mode, resource requirements, and pricing model.

### Phase 4 — shared module migration

Migrate Work, Chat, Circles, Things, Rides, Profile, Notifications, and Activity onto the same task-surface and attention patterns. Do not rewrite all modules at once; migrate one surface archetype at a time.

### Phase 5 — runtime UX verification

Verify each route at desktop and narrow widths, including empty/loading/error/recovery states, keyboard navigation, active context persistence, and complete turn-based happy paths. Capture visual evidence only for the current implementation state.

## Explicit non-goals

- Do not make Vision the only way to use the product.
- Do not move domain rules into the frontend to simplify the UI.
- Do not add more navigation items to compensate for missing context.
- Do not solve density by shrinking text or controls; solve it by progressive disclosure and task sequencing.
- Do not create a dashboard full of independent cards. The primary surface should still be one calm workspace with one next action.
