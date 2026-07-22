# System Map Third Pass — Round 17: Failure, Recovery, and Boundary Cases

Date: 2026-07-22  
Status: **observed analysis**

## Executive finding

Failure handling is strongest at the domain boundary: services validate actor,
state, time, capacity, consent, and target before mutating. Several important
workflows add row locks, idempotency, optimistic versioning, retryable flags,
provider-unavailable responses, and reconnect/read-back paths.

Recovery is less uniform across modules. Business bookings and rides have strong
concurrency protections; Vision and Chat have explicit blocked/retry/reconnect
contracts; ordinary cross-module idempotency, event replay, job replay, and
distributed recovery remain incomplete.

## Failure taxonomy

```text
input/validation
   -> authorization/visibility
      -> workflow/state conflict
         -> concurrency/idempotency
            -> provider/storage failure
               -> delivery/realtime failure
                  -> recovery/retry/read-back
```

The application generally uses typed service errors and backend-prepared blocking
messages rather than allowing clients to invent recovery semantics.

## Concurrency and idempotency

### Business bookings

Booking creation checks an idempotency key, validates that a repeated key carries
the same payload, locks the offering row, checks overlapping capacity, and saves
within a transaction. This protects duplicate submissions and concurrent slot
consumption. Booking rescheduling and lifecycle transitions use status/policy
validation and the same capacity primitives.

### Rides

Ride mutations load the offer with `findByIdForUpdate`. Join is intentionally
idempotent for retrying clients; passenger uniqueness and seat counts are checked
under the locked aggregate. `RideOffer` also has `@Version`, providing optimistic
version metadata in addition to pessimistic row locking.

### Things and Workmarket

Things protects listing/request transitions through transactional services and
state checks. Workmarket protects actor/state transitions and uses unique
application constraints, but a universal request idempotency contract is not
evidenced for all Quest/application mutations.

## Vision recovery model

Vision carries explicit conversation states, blocking reasons, failure codes,
retryable flags, review/confirmation stages, cancel/reset paths, and client
request identity. Provider unavailability preserves retryable conversation state
and avoids inventing a local semantic result in production mode.

Execution adapters generally catch runtime failures and return typed blocked
results. This is safe for client continuity, but it can hide root-cause detail
unless structured logging/metrics preserves the original exception and capability
context. Vision fails closed on missing owner, missing slot, invalid target, stale
conversation state, inaccessible entity, and disabled execution configuration.

## Chat recovery model

Chat uses backend rate limits, attachment lifecycle states, expiry rules, message
sync, refresh hints, WebSocket reconnect, and authoritative read-back. Realtime
delivery is best-effort; HTTP conversation/message state remains authoritative.
Attachment upload cancellation is state-sensitive, and expired or unauthorized
attachments return controlled outcomes.

## Provider and location failure

Location lookup uses in-memory rate-limit caches and explicit provider failure
handling. Forward lookup can return an unavailable response; reverse lookup maps
provider failure to a service-unavailable error. Voice/OpenAI and Vision providers
expose unavailable/retry behavior. Runtime evidence preserves the boundary between
development-only local fallback and production provider ownership.

## Authorization and stale-state recovery

Services reload exact targets before mutation and reject wrong actor, blocked
relationship, invalid lifecycle status, unavailable/archived resource, full or
departed ride, duplicate/incompatible idempotency key, last-admin destructive
action, stale Vision state, and missing/inaccessible entity.

The normal recovery is a backend error/blocking DTO followed by refresh,
clarification, review, retry, or a different allowed action. This is a strong
fail-closed architecture.

## Recovery matrix

| Failure class | Backend behavior | Client recovery | Evidence status |
|---|---|---|---|
| invalid input | validation error | correct/retry | source + tests |
| unauthorized target | forbidden/not found | choose accessible target | source + runtime boundaries |
| invalid state | conflict | refresh and choose allowed transition | source + selected runtime |
| duplicate booking | idempotent read-back or conflict | reuse result/correct payload | source + tests |
| concurrent capacity | row lock + conflict | refresh slot/retry | source, runtime depth limited |
| provider unavailable | typed unavailable/blocked state | retry or direct fallback surface | source + runtime evidence |
| WebSocket disconnect | transient delivery loss | reconnect + HTTP sync | source + runtime evidence |
| expired attachment | redaction/controlled access | refresh or accept placeholder | source + runtime evidence |
| scheduled job failure | logs/next schedule | operator action not evidenced | source only |
| event/listener failure | in-process exception semantics | transaction/retry behavior unclear | evidence gap |

## Risks and recommendations

1. Idempotency is explicit for bookings and selected ride operations but not a
   uniform cross-module contract.
2. Vision-safe blocked responses need structured error correlation so observability
   is not lost while user messaging remains safe.
3. Event and notification failure semantics should be made after-commit/outbox
   decisions rather than implicit in-process behavior.
4. Rate-limit caches and WebSocket sessions are process-local and need shared
   infrastructure before horizontal scaling.
5. Retention/compaction jobs need operator-visible outcome and replay evidence.
6. Failure-injection scenarios should cover lock contention, provider/event/
   storage failure, and reconnect/read-back.

## Source evidence

- `apps/themuffinman/src/main/java/com/themuffinman/app/business/service/BusinessCreateBookingUseCase.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/business/service/BusinessBookingPrimitiveService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/rides/service/RideOfferService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/rides/model/RideOffer.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/chat/service/ChatService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationLookupService.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionExecutionPlanner.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/*ExecutionAdapter.java`
- `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleRelationService.java`
- `docs/runtime-acceptance-matrix.yaml`

## Conclusion

Round 17 confirms a fail-closed domain architecture with strong booking/ride
concurrency controls and explicit Vision/Chat recovery contracts. The next
reliability improvement is standardization: request correlation, idempotency,
after-commit side effects, shared runtime state, and failure-injection evidence.
