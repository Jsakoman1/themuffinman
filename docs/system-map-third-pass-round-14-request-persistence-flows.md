# System Map Third Pass — Round 14: Request-to-Persistence Flows

Date: 2026-07-22  
Status: **observed analysis**

## Executive finding

The backend has three distinct execution patterns rather than one universal
request pipeline:

1. **Domain facade + use case + execution primitive** for Workmarket and several
   lifecycle-heavy domains.
2. **Transactional use case + lock/idempotency + event publication** for Business
   bookings.
3. **Conversation/turn persistence + semantic orchestration + domain adapters**
   for Vision.

Read paths are generally separated into `@Transactional(readOnly = true)` read
services, repository fetch queries, and DTO/read-model assemblers. Controllers
remain thin and pass the authenticated principal into backend-owned services.

## Canonical flow shape

```text
Web/Vision client
    -> shared HTTP transport
    -> controller + authenticated principal
    -> domain facade/use case
    -> validation + permission + state rules
    -> repository fetch/save/delete
    -> mapper/presentation assembler
    -> DTO/action result
```

For workflows with side effects:

```text
transactional mutation
    -> aggregate state change
    -> audit/event/notification side effect
    -> DTO read-back/enrichment
```

The exact transaction boundary is service/use-case-owned, not client-owned.

## Flow A — Workmarket Quest creation

`POST /quests` enters `QuestController.createQuest`, which delegates to
`WorkmarketQuestService.createQuest`. The service forwards to
`WorkmarketCreateQuestUseCase`:

1. validate the request;
2. resolve creator, including guarded admin creator selection;
3. map DTO to `Quest` through `WorkmarketQuestMgr`;
4. apply default audience and selected circle visibility;
5. normalize assignee target and confirmed term fields;
6. apply location settings;
7. persist through `WorkmarketQuestExecutionPrimitiveService` and
   `WorkmarketQuestRepository.save`.

The controller returns an action result rather than the full aggregate. A later
read uses `WorkmarketQuestReadService`, repository fetch queries, visibility and
application context, then `QuestResponseFactory`/assemblers to build the DTO.

This is a strong separation between mutation and read assembly. The execution
primitive centralizes target resolution, authority, persistence, deletion data,
and selected workflow notifications.

## Flow B — Workmarket read and detail

`GET /quests`, `/quests/search`, `/{id}`, `/{id}/detail`, and `/preview` use
dedicated read services. Repository queries explicitly fetch creator, images, and
visible circles for list/detail shapes. The read service then adds viewer-specific
application/action context and maps to backend-prepared DTOs.

Observed implication: the API shape is not a direct entity serialization. It is a
viewer-aware read model assembled inside a read-only transaction. This protects
lazy relations and keeps permissions/action flags out of the frontend.

## Flow C — Business booking creation

`POST /business/bookings` enters `BusinessBookingController` and
`BusinessCreateBookingUseCase.createCustomerBooking` under `@Transactional`:

1. validate request presence;
2. check idempotency key for the current customer;
3. lock the offering row through `BusinessBookingPrimitiveService`;
4. resolve end time from offering duration when needed;
5. load booking policy;
6. validate actor, time range, capacity, and policy;
7. construct booking with lifecycle status and historical snapshots;
8. save booking;
9. publish `BusinessBookingCreatedEvent`;
10. map and enrich the response for the customer.

Owner-created booking follows the same pattern but checks business ownership and
resolves a customer user. The offering lock, idempotency check, capacity query,
and event publication make this flow materially stronger against duplicate and
concurrent writes than a simple save-through-controller flow.

The event publication is inside the transaction method; the exact listener
delivery/rollback semantics require the event round to map separately.

## Flow D — Things listing and borrow lifecycle

Things uses a broader `ThingSharingService` with read-only class-level
transactionality and method-level mutation transactions. Listing reads use
repository queries that fetch the owner and filter available/archived state.
Mutations load detailed listing/request graphs, enforce owner/borrower authority,
sanitize rich text, change lifecycle state, and save the affected record.

The borrow return path updates both request status and listing availability in one
transaction. This is a critical invariant: the relationship state and catalog
availability are coupled writes and must not be split across clients.

## Flow E — Rides lifecycle

Rides uses a service with read-only default transactionality and explicit mutation
transactions. Reads map repository projections/entities through `RideOfferMgr`.
Mutations perform actor/state checks, change offer or participant state, write
audit events, emit passenger notifications, and map the updated aggregate.

This is a compact example of a domain aggregate plus side effects in one service
boundary. It also demonstrates why later event mapping must inspect whether
notification failures can affect the primary lifecycle commit.

## Flow F — Chat message and realtime path

Chat HTTP controllers delegate to chat services for conversation access, message
creation, reactions, delivery/read state, attachment lifecycle, and presence.
Repository state is authoritative; WebSocket handling provides a realtime
transport and refresh/reconnect hints. Message/attachment mutations combine
membership authorization, rate limits, validation, persistence, and realtime
publication.

The protocol is therefore dual-path: HTTP is the authoritative command/read
boundary, while WebSocket is a delivery and synchronization surface. Round 15
must map the exact event ordering and recovery behavior.

## Flow G — Vision conversation turn

`POST /vision/conversations/turns` enters `VisionConversationService.processTurn`
under a conversation-oriented transaction boundary. The service:

1. validates authenticated access and Vision enabled state;
2. resolves action, prompt, conversation, and client request identity;
3. loads or creates conversation state;
4. invokes semantic/orchestration services and intent/slot resolution;
5. builds read-only previews or execution plans;
6. routes approved actions to domain-specific adapters/services;
7. persists conversation/turn/memory state;
8. assembles the typed conversation response.

Vision therefore has its own persistence state but does not own the target
product aggregate. The adapter path is the boundary between assistant intent and
domain mutation. Reset/cancel/recent/detail are separate lifecycle/query paths.

## Read versus mutation patterns

| Concern | Read path | Mutation path |
|---|---|---|
| Transaction | `readOnly = true` commonly used | explicit `@Transactional` use case/service |
| Repository | fetch joins, filtered queries, projections, pagination | entity load, lock where needed, save/delete |
| Authorization | viewer-aware read service and policy | actor ownership, role, state, and workflow checks |
| Mapping | DTO/read-model assembler | saved entity mapped and often enriched |
| Side effects | usually none, except read-side activity/context | audit, event, notification, realtime, derived state |
| Client responsibility | render prepared sections/actions | send typed intent/payload and handle result |

## Architectural strengths

- Controllers are thin and consistently inject the authenticated actor.
- Lifecycle mutation logic is increasingly decomposed into named use cases.
- Read surfaces use dedicated services and fetch plans rather than exposing
  entities directly.
- Business booking has explicit row locking and idempotency.
- Vision mutations pass through typed planning/review/adapters.
- DTO assembly is backend-centered and viewer-aware.

## Flow-level risks and evidence gaps

1. Event publication and listener transaction semantics are not yet mapped.
2. Some services remain large facades with many delegated operations; their real
   boundary is distributed across use cases and primitives.
3. Vision has a very high orchestration fan-in, so a single turn can cross many
   domain services and persistence state machines.
4. Realtime delivery and HTTP commit ordering need separate failure analysis.
5. Read fetch plans are explicit in several repositories, but complete query cost
   and N+1 analysis belongs to Round 16.
6. Idempotency is clearly present for Business bookings but is not yet shown as a
   uniform cross-module contract.

## Source evidence

- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/*`
- `apps/themuffinman/src/main/java/com/themuffinman/app/business/controller/BusinessBookingController.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/business/service/BusinessCreateBookingUseCase.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/business/service/BusinessBookingPrimitiveService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/things/service/ThingSharingService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/rides/service/RideOfferService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/chat/**`
- `apps/themuffinman/src/main/java/com/themuffinman/app/vision/controller/VisionConversationController.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationService.java`

## Conclusion

Round 14 confirms that the system is not a simple CRUD application. It has
separate read models, transactional use cases, locking/idempotency, audit/event
side effects, realtime transport, and assistant orchestration. The next pass must
trace the side effects and their failure semantics before drawing conclusions
about reliability or distributed extraction.
