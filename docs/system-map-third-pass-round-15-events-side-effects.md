# System Map Third Pass — Round 15: Events, Notifications, and Async Side Effects

Date: 2026-07-22  
Status: **observed analysis**

## Executive finding

The application uses several side-effect mechanisms rather than one event bus:

- synchronous domain notification services called inside lifecycle services;
- Spring `@EventListener` handlers for selected Workmarket and Business events;
- Chat realtime delivery through in-memory WebSocket session registries;
- scheduled cleanup/compaction jobs;
- activity/news records that become the durable user-facing side-effect log.

The system is intentionally simple and local, but side effects are not uniformly
after-commit or externally queued. A primary mutation can therefore have a
durable database outcome, a durable news/audit outcome, and a transient realtime
delivery outcome with different failure semantics.

## Side-effect graph

```text
domain mutation
   |
   +--> aggregate/audit persistence
   |
   +--> Spring domain event --> event handler --> news/activity record
   |                                           \
   |                                            --> unread count/realtime signal
   |
   +--> direct notification service ----------------> news/activity
   |
   +--> Chat service -------------------------------> WebSocket sessions

scheduled jobs --> retention deletes/redaction and Vision memory compaction
```

## Workmarket events and news

Application lifecycle operations publish `WorkmarketQuestApplicationNewsEvent`.
The `WorkmarketQuestApplicationNewsEventHandler` listens synchronously and maps
created, updated, withdrawn, approved, and declined events to
`WorkmarketQuestNewsService` operations.

Other Quest lifecycle operations call
`WorkmarketQuestWorkflowNotificationService` directly. It creates news for
creators, applicants, workers, releases, reassignment, cancellation, and
deletion. The news service persists the durable record and asks
`ChatRealtimeService` to notify unread-count/workspace changes.

This gives users durable read-back even when a WebSocket client is disconnected.
The WebSocket notification is an acceleration layer, not the source of truth.

## Business booking events

Booking create, status-change, and reschedule use cases publish typed events:

- `BusinessBookingCreatedEvent`;
- `BusinessBookingStatusChangedEvent`;
- `BusinessBookingRescheduledEvent`.

`BusinessBookingEventHandler` listens to these events and delegates to booking
audit/notification behavior. Because events are published from transactional use
cases and handlers are regular `@EventListener` methods, the current design must
be understood as in-process synchronous event handling unless a different Spring
configuration is introduced. There is no observed outbox, broker, or durable
event queue.

## Rides side effects

Ride mutations directly write ride audit events and call Quest news services for
driver/passenger notifications. Join/leave/cancel/start/complete each update
ride state and may notify affected actors. This is easy to follow and keeps the
aggregate transition close to the side effect, but it means notification
exceptions need explicit treatment so they do not unexpectedly invalidate the
primary ride mutation.

## Chat realtime side effects

Chat persists messages and state through HTTP service methods, then invokes
`ChatRealtimeService` for message-created/updated/deleted, conversation state,
delivery, seen, typing, presence, workspace, and news events.

The realtime service stores active sessions in an in-memory
`ConcurrentHashMap<Long, Set<WebSocketSession>>`. It sends only to currently
connected sessions. Reconnect and sync endpoints provide authoritative recovery.

Implications:

- realtime delivery is process-local;
- multiple application instances would need shared pub/sub or sticky topology
  for complete cross-instance delivery;
- disconnected clients depend on HTTP sync/read-back;
- send failure removes or ignores the transient delivery path without replacing
  the durable message state.

## Scheduled side effects

Observed scheduled jobs include:

- Chat retention cleanup: redacts expired images and deletes old messages;
- Vision memory compaction: compacts persisted learning/memory state;
- authentication-token cleanup: removes expired/revoked token records;
- notification retention cleanup configured through retention properties.

These jobs are local process schedulers. No distributed lock, job execution
record, queue, or operator replay protocol was found in the inspected surface.

## Side-effect durability classes

| Side effect | Durability | Recovery path |
|---|---|---|
| Aggregate state | database | authoritative read/retry according to workflow |
| Audit row | database when written | admin/domain audit read |
| News/activity record | database | inbox/activity reload |
| Unread/realtime signal | transient WebSocket plus derived count | reconnect and authoritative read |
| In-process event | process-local synchronous dispatch | rollback/error handling depends on listener behavior |
| Retention/compaction job | database mutation | next schedule; manual replay not evidenced |

## Risks

1. **No outbox boundary.** Database commit and external delivery are not
   represented by an explicit outbox record.
2. **Synchronous listener coupling.** An event handler failure may affect the
   originating transaction unless handlers deliberately isolate failures.
3. **Duplicate side-effect risk.** Direct notification calls and event handlers
   coexist; each workflow needs a single-owner review to avoid duplicate news.
4. **Process-local realtime.** WebSocket sessions do not automatically scale
   across instances.
5. **Job observability gap.** Scheduled cleanup has logs/configuration but no
   canonical success/failure ledger.
6. **Unread count consistency.** Durable news and transient count notifications
   need reconciliation after reconnect or missed events.

## Recommendations

1. Catalog every event publisher, handler, transaction boundary, and side-effect
   owner.
2. Decide explicitly which workflows need an outbox and which can remain
   synchronous in-process.
3. Add job execution evidence and idempotent replay semantics.
4. Define multi-instance WebSocket delivery/reconnect architecture before
   horizontal scaling.
5. Add tests for notification failure, duplicate event delivery, and reconnect
   read-back.

## Source evidence

- `apps/themuffinman/src/main/java/com/themuffinman/app/common/event/*`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/event/*`
- `apps/themuffinman/src/main/java/com/themuffinman/app/business/event/*`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketQuestNewsService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WorkmarketQuestWorkflowNotificationService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/rides/service/RideOfferService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/chat/service/ChatRealtimeService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/chat/websocket/*`
- `apps/themuffinman/src/main/java/com/themuffinman/app/**/service/*RetentionService.java`

## Conclusion

Round 15 confirms that durable database/news state and transient realtime
delivery are deliberately related but not identical. The next architecture
question is not whether the system has events; it is which side effects require
after-commit guarantees, replay, deduplication, and multi-instance delivery.
