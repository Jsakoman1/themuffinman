# Rides production completion analysis

The repository currently contains only a partial `RideOffer` model and offer/list API. It has no participant entity, lifecycle state machine, capacity reservation, update/cancel/start/complete operations, notification or audit history, Web surface, Vision route coverage, or browser runtime evidence.

The canonical product contract already defines the required capability set: offer, discover, join, update, leave, cancel, and complete. This workstream extends that contract with an explicit `IN_PROGRESS` start transition because a complete ride lifecycle needs a state between reservation and completion.

The implementation must preserve the product boundary that rides are voluntary circle-scoped coordination, not commercial Business bookings. Route visibility, participant consent, exact location policy, seat capacity, departure cutoffs, and state transitions remain backend-owned.

## Canonical contract matrix

| Capability | Actor | Preconditions | Success state |
|---|---|---|---|
| `sharing.ride.create` | authenticated member | future departure, valid route, 1–8 seats, owned visibility circles | `OPEN` |
| `sharing.ride.discover` | permitted viewer | public or circle membership; active offer | visible `OPEN`/`FULL` read model |
| `sharing.ride.join` | permitted member | not driver, future departure, available capacity, consent | participant `JOINED`; `OPEN → FULL` |
| `sharing.ride.update` | driver | `OPEN` or `FULL`, future departure | updated offer |
| `sharing.ride.leave` | joined passenger | before start | participant `LEFT`; `FULL → OPEN` |
| `sharing.ride.cancel` | driver | `OPEN` or `FULL` | `CANCELLED`, inactive |
| `sharing.ride.complete` | driver | `IN_PROGRESS` | `COMPLETED`, immutable |

The internal `start` operation is also required (`FULL → IN_PROGRESS`) even though it is not a separate target capability. `DRAFT` is reserved for future staged Vision composition; confirmed create persists directly as `OPEN`.

## Safety and invariants

- The ride row is pessimistically locked during join/leave and the participant pair is unique, so active participants cannot exceed seats.
- Circle visibility is an allow-list: empty means authenticated-public; selected circles require membership. Visibility is never inferred from route proximity.
- The driver cannot join or leave their own ride. Completed and cancelled rides are immutable through normal lifecycle actions.
- Exact addresses, live vehicle location, and continuous tracking are outside this module; route text is coarse user-provided context.
- Every state or participant mutation writes an audit event. Notification delivery must not weaken authorization or transaction boundaries.

## Required lifecycle

`OPEN -> FULL -> IN_PROGRESS -> COMPLETED` is the successful path. `OPEN` and `FULL` may transition to `CANCELLED`; a participant may leave before the ride starts. Capacity must be enforced transactionally and completed rides must be immutable.

## Required evidence

Every capability needs backend tests, Web route/action/recovery evidence, Vision review/confirmation evidence, and a machine-readable runtime acceptance scenario. Static build output is not sufficient.
