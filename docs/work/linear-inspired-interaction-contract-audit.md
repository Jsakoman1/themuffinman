# Linear-inspired interaction contract audit

## Evidence baseline

- Generated frontend inventory: 43 routes, 42 concrete route surfaces, and one redirect.
- The authenticated shell, primary navigation, route entries, and create handoffs pass `make audit-frontend`.
- `AppActionMenu.vue` was a likely-unused surface at the audit baseline. Shell navigation now adopts it for personal shortcuts; later object-action work must extend this primitive rather than add another menu.
- Existing Circle block/request workflows and feedback handling overlap across active surfaces. New UI invokes existing backend-authorized actions and may not add a second client mutation or permission policy.

## Interaction matrix

| Proposed interaction | Canonical route/surface | Read model and permitted action source | Safe first scope | Fallback or block |
| --- | --- | --- | --- | --- |
| persistent shell and global destinations | authenticated `AuthenticatedShellView`, `shellRouteRegistry.ts` | route metadata plus `surfaceOwnershipMatrix`; no object action is implied | active rail, compact page context, existing search/create entries | no workspace switcher or arbitrary favorites until a user-owned API exists |
| global command and search | `GlobalSearchEntry`, `/search/saved`, `/vision` | typed search and `SavedSearchIntentService`; result source route remains backend-owned | separate destinations/actions from find-in-view and Vision prompt | no client result ranking, private search terms in URLs, or shared saved views |
| Work collection and preview | `/work`, `/work/find`, `/work/quests` | `QuestListResponseDTO` / `QuestPresentationDTO`; `WorkmarketQuestAccessPolicyService` and list/detail presentation assemblers | dense row, one canonical open target, local display-only preference | board, drag/drop, bulk mutation, and shared views blocked without explicit read/mutation contracts |
| Work detail and applications | `/work/quests/:questId`, `/work/applications/:applicationId` | `QuestDetailResponseDTO.sections`, `QuestApplicationResponseDTO.allowedActions`, backend execution/management sections | utility rail for fields/actions already prepared by DTO | no inferred action visibility or second edit workflow |
| attention and notifications | `/notifications`, `/activity` | `GET /attention/me` and `ActivityReadService` viewer-scoped projections | list selection and allowed destination opening | no snooze, bulk read, subscription bell, or generic inbox action until endpoints exist |
| Chat thread and composer | `/chat`, `/chat/:conversationId` | conversation/message DTOs and participant-authorized chat services | split-view selection, anchored existing composer, reply/edit/delete only where current DTO/service permits | no local participant matching, fake delivery state, or generic assistant composer |
| Business booking operations | `/business/bookings`, `/business/my-bookings`, `/business/calendar` | `BusinessBookingResponseDTO.allowedActions` and `actions`, `BusinessBookingPresentationService`, calendar item actions | row/detail utility actions and backend booking preview | no local duration derivation, availability calculation, or unsupported bulk scheduling |
| Things and rides | `/things`, `/things/:listingId`, `/rides`, `/rides/:rideId` | `ThingListingResponseDTO` / borrower request state; `RideOfferResponseDTO` viewer-scoped service results | canonical detail opening and existing lifecycle buttons | no local visibility, capacity, matching, or batch state decisions |
| people and Circles | `/people`, `/people/:userId`, `/circles` | profile/circle DTO `primaryAction` and backend relationship services | compact row actions and canonical profile detail | no client trust, membership, block, or invite eligibility rules |
| Vision | `/vision` plus explicit bridge redirects | typed Vision conversation/runtime response, backend confirmation gate | anchored prompt, transcript/processing/review evidence | no command-palette substitution, inferred capability, or unconfirmed mutation |

## State and URL rules

- Backend query parameters remain membership truth. Local presentation state may change row density, visible metadata, selected preview, or panel openness only.
- A preview never replaces a canonical detail route. It is dismissed with `Esc`, and a nested action must not activate the parent row/card.
- URL state may contain an existing canonical route and supported query parameter only. It may not contain private object data, a form draft, permission state, or a locally inferred workflow transition.
- Route-scoped display density, selected object, and scroll restoration are local session state keyed by viewer and normalized backend query. A successful response discards a selected object that is no longer available in that response.
- Global shortcuts are inactive in inputs, selects, rich editors, and Vision composers.

## Explicitly blocked capability gates

| Capability | Gate required before UI is offered |
| --- | --- |
| favorites beyond existing business favorites/saved searches | user-owned persistence, target allow-list, visibility/read contract, removal semantics |
| bell/subscriptions | viewer-owned subscription/read-state API and notification delivery semantics |
| saved or shared views | typed preference model, ownership, sharing, expiry, and supported filter/display schema |
| board drag/drop or timeline editing | stable grouping/date fields, ordering semantics, backend mutation and partial-failure contract |
| multi-select and bulk actions | backend batch endpoint with authorization, result ordering, partial failure, refresh, and undo policy |
| virtualization, infinite scroll, optimistic mutations, prefetch | measured need plus stable pagination/order, accessibility, cache invalidation, and failure rules |

## Required evidence before implementation children

Run `make audit-frontend` before each child and attach its generated route, stale-surface, state-duplication, and permission reports to the implementation decision. Browser evidence must test the changed direct flow plus loading, empty, error, cancellation/confirmation, and reduced-motion behavior. Agent-started services are stopped and port-checked after the trace.
