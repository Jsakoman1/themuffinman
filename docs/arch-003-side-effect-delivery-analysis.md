# ARCH-003 Side-Effect Delivery Analysis

## Scope

This analysis maps the current event, notification, realtime, and scheduled side-effect paths and defines the first safe reliability boundary. It is a source-trace analysis; it does not claim deployed or runtime delivery guarantees.

The workmarket application-news consumer now adds an event UUID to the in-process event and persists a unique delivery key on the news row. This closes duplicate database inserts for that consumer when the same event is delivered again, but it does not create a durable event ledger or guarantee recovery after a process crash.

## Current findings

| Surface | Current mechanism | Transaction relationship | Recovery/replay truth | Risk |
|---|---|---|---|---|
| Business booking audit | `DomainEventPublisher` -> Spring `@EventListener` -> transactional audit service | Listener runs during the caller's transaction | Audit rows are durable when the transaction commits; no universal event replay | A listener failure can affect the mutation transaction; event payloads may be detached after commit |
| Workmarket application news | `DomainEventPublisher` -> Spring `@EventListener` -> news row + realtime notification | Listener runs during the caller's transaction | News row is a database recovery anchor; WebSocket delivery is not replayable | Database and process-local delivery are coupled; no explicit after-commit phase |
| Business booking events | Created/status/rescheduled events | Callers are transactional | No outbox or durable event ledger evidenced | Same as above |
| Chat realtime | Direct `ChatRealtimeService` calls from `ChatService` | Calls occur from transactional methods | HTTP sync after reconnect; not event replay | A client can receive a message before commit or miss a process-local socket send |
| Scheduled cleanup/compaction | Spring `@Scheduled` methods | Service-level transaction only where declared | Run ledger, replay, alerting, and deployed execution evidence are unknown | Overlap, partial failure, and operator recovery are not standardized |
| Password recovery events | Direct Spring `ApplicationEventPublisher` usage | Publisher is called from transactional service methods | No delivery/replay contract evidenced | Separate event path bypasses the domain publisher contract |

## Reliability decision

1. In-process domain events that create database news/audit records must be published after a successful database commit. The first implementation changes the shared `SpringDomainEventPublisher` boundary and keeps no-transaction callers synchronous.
2. `AFTER_COMMIT` is not durable delivery. A process crash after commit can still lose the listener invocation. A durable outbox is required for guaranteed replay, but is not introduced in this slice because event identity, serialization, aggregate snapshots, and consumer idempotency are not yet universal.
3. WebSocket delivery remains best-effort transport. Persisted news/message state and HTTP resync remain authoritative recovery paths.
4. Scheduled jobs require a later job-ledger slice with run identity, lease/overlap policy, outcome, retry/replay reference, and operator evidence.
5. Existing direct `ApplicationEventPublisher` call sites must be migrated or explicitly classified in a later adoption pass; this slice does not silently claim they are covered.

## Important implementation constraint

Event objects currently contain domain entities. After-commit listeners execute outside the original transaction, so lazy relationship access is a concrete adoption risk. The shared boundary is therefore implemented and tested first, while each event family remains pending payload snapshotting or explicit listener transaction/read strategy.

## Evidence boundary

Static inspection proves source relationships and annotations only. It does not prove that production commits, crashes, retries, scheduler overlap, WebSocket reconnects, or replay behavior work as intended. Those remain runtime acceptance items.
