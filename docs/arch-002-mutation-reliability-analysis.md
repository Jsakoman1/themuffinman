# ARCH-002 Mutation Reliability Analysis

Status: completed analysis and implementation-plan preparation. Reviewed: 2026-07-22.

## Executive conclusion

The system has strong local reliability patterns but no uniform cross-module
mutation contract. Business bookings are the clearest idempotency reference;
rides combine pessimistic locking, optimistic versioning, and domain-level
idempotent join behavior. Vision carries request and correlation concepts inside
its orchestration model. These mechanisms are not currently standardized at the
HTTP transport, error envelope, audit, or side-effect boundary.

The risk is not that every mutation is currently unsafe. The risk is that Web,
Vision, retrying clients, and future native clients cannot rely on one predictable
contract for deciding when a mutation can be retried, replayed, rejected as stale,
or correlated with its side effects.

## Current evidence

| Concern | Current state | Confidence |
|---|---|---|
| Transport request context | No shared request/correlation filter or documented header contract was evidenced. | High |
| Error envelope | `GlobalExceptionHandler` returns code/message/field errors, but no correlation ID, operation key, or retryability field. | High |
| Booking idempotency | Customer and owner booking create accept `idempotencyKey`, read back same-actor records, validate payload compatibility, and return the existing result. | High |
| Ride idempotency | Ride join returns the existing joined result; row locking and `@Version` protect concurrency. | High |
| Workmarket idempotency | State/unique constraints exist, but universal client idempotency for quest/application mutations is not evidenced. | Medium-high |
| Things/Social/Chat idempotency | Domain state checks exist; a uniform client request key contract is not evidenced. | Medium |
| Vision correlation | `VisionRuntimeContextDTO.correlationId`, `VisionCanvasAssembler` correlation values, and candidate `requestId` exist inside Vision flows. | High |
| Frontend propagation | Generated contracts contain selected `requestId`/`correlationId` fields, but the shared Axios client does not inject or expose a mutation reliability context. | High |
| Resource versioning | `RideOffer` uses optimistic version metadata; coverage is not uniform across mutable aggregates. | Medium-high |
| Side-effect correlation | Domain events and notifications are published from services, but a universal originating correlation contract is not evidenced. | Medium |
| Audit trail | Ride and Chat have domain-specific audit records; there is no cross-module mutation audit record. | High |

## Module coverage matrix

| Module | Mutation examples | Idempotency | Stale/conflict handling | Correlation | Main gap |
|---|---|---|---|---|---|
| Business | booking create, lifecycle, reschedule | explicit booking key | locks, capacity/status checks | local event only | generalize key/fingerprint and response metadata |
| Sharing/Rides | create/update/join/leave/cancel | join behavior is domain-idempotent | row lock plus `@Version` | audit fields are local | transport request context and shared policy |
| Work | quest/application/worker transitions | constraints and state checks | conflict/state checks | Vision request context only when delegated | mutation key/version policy |
| Things | listing and borrow transitions | duplicate pending rules | state/ownership checks | no universal context | key and stale policy |
| Circles | relation/request/block mutations | state checks | authorization/state checks | no universal context | replay and audit contract |
| Chat | send/edit/delete/read/reaction/upload | selected state-safe operations | lifecycle/rate-limit checks | WebSocket session context only | message/upload deduplication and correlation |
| Vision | delegated domain commands | conversation/request identity | stale conversation and blocked states | strongest local correlation | propagate to domain and side effects |

## Failure and retry model

The target contract distinguishes five outcomes:

1. `completed`: domain mutation committed and authoritative result available.
2. `replayed`: same logical operation was safely retried and original result was returned.
3. `conflict`: request was understood but cannot apply to current state or key fingerprint.
4. `retryable_failure`: no committed mutation is proven and retry is safe under operation policy.
5. `unknown_outcome`: transport/provider failure leaves commit state uncertain; client must read back before retrying.

The current application usually collapses these distinctions into HTTP status,
message, or local retryable fields. That is safe for many user flows but weak for
diagnostics, automation, and cross-client replay.

## Concrete risks

- A network timeout after a successful non-booking mutation can cause a client to
  submit a second logical mutation because no shared key/replay contract exists.
- A conflict response does not consistently identify stale resource versus invalid
  lifecycle state versus duplicate logical operation.
- `GlobalExceptionHandler` cannot connect a user-visible error to service logs or
  side-effect delivery without correlation metadata.
- Domain events published inside transaction-scoped services do not universally
  declare whether a listener runs after commit or how a failed delivery is replayed.
- Vision has internal request identity, but adapters do not yet guarantee that the
  same identity reaches every domain mutation and downstream notification.
- Native clients would need to invent their own retry behavior unless the backend
  contract is stabilized first.

## Recommended target architecture

```text
client request_id / Idempotency-Key
              ↓
transport request context + correlation_id
              ↓
operation policy resolver
              ↓
domain service mutation + version/key checks
              ↓
authoritative result or typed conflict/error
              ↓
after-commit side effects carrying correlation_id
              ↓
audit/observability/replay evidence
```

The contract is defined in [`mutation-reliability-contract.yaml`](mutation-reliability-contract.yaml).
It is a target contract, not proof that the current application already implements
all fields.

## Implementation sequence

The execution-ready plan is [`work/arch-002-mutation-reliability-master.yaml`](work/arch-002-mutation-reliability-master.yaml).
It should be implemented in this order:

1. common transport context and error metadata
2. operation-policy registry and shared idempotency primitives
3. reference adapters for booking and rides
4. adoption across Work, Business, Things, Circles, Chat, and Vision
5. side-effect propagation and audit records
6. failure-injection/runtime evidence

No capability status is changed by this analysis.

The evidence boundary is recorded in `docs/mutation-reliability-evidence.yaml`:
transport, primitive, booking, and ride contract tests are validated; browser
failure-injection and uncertain-outcome scenarios remain pending runtime.

Implementation status: the transport context, structured error metadata, operation
policy primitives, and booking/ride reference adoption are now master-verified.
The adoption matrix intentionally keeps Work, Things, Circles, Chat, and Vision
partial until operation-specific replay and side-effect semantics are implemented.
