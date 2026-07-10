---
machine_kind: plan
machine_status: complete
machine_title: Business Booking Workflow Plan
machine_goal: Implement real reservation writes and lifecycle transitions for customers and owners using capacity-aware validation, explicit status changes, and separate read surfaces for customer and owner workflows.
---

# Business Booking Workflow Plan

## Status

Complete.

## Goal

Implement real reservation writes and lifecycle transitions for customers and owners using capacity-aware validation, explicit status changes, and separate read surfaces for customer and owner workflows.

## Parent Master Plan

- Master plan: `.agents/business-booking-implementation-master-plan.md`

## Continuation Rule

- Once this phase reaches its closeout gates, execution should continue automatically into `.agents/business-booking-hardening-and-integrations-plan.md` without a routine stop.
- Do not pause after this phase only to ask whether to continue unless a real blocker or approval need appears.

## Scope

- Included: booking table, booking creation, owner confirmation/rejection/cancellation/completion actions, customer cancellation, owner/customer booking reads, and booking transition validation.
- Excluded: payment, waitlist, multi-staff assignment, external calendar sync, and advanced notification fan-out.

## Why This Phase Exists

- This is the first phase where the booking module becomes operational rather than only descriptive.
- All prior phases establish the data and read boundaries needed to make booking writes safe.

## Phase Analysis

### Problem Being Solved

Without this phase, business booking remains only a catalog and schedule editor.

The write path needs to combine:

- owner/business permissions
- derived availability
- capacity checks
- booking policy enforcement
- explicit booking status transitions

### Architectural Decisions

- Add `business_booking` as the reservation root.
- Use explicit action endpoints for lifecycle transitions.
- Separate customer self-service reads from owner management reads.
- Put validation and occupancy checks in dedicated services, not in controllers.
- Keep booking transitions behind `*UseCase` services similar to `workmarket`.
- Persist booking timestamps as absolute values while treating owner availability input as local business-time rules.

### Main Risks

- Race conditions around capacity.
- Excessive logic concentration in one booking service facade.
- Ambiguous handling for `PENDING_CONFIRMATION` capacity reservation.
- Duplicate booking creation under retries or slow client/network behavior.

### Mitigations

- Centralize occupancy checks in one primitive/validation service.
- Introduce one use case per transition where needed.
- Make the policy for whether pending bookings consume capacity an explicit service rule.
- Treat idempotency guidance and transaction boundaries as part of the phase itself rather than postponing them.

### Locked Workflow Decisions

- `PENDING_CONFIRMATION` reservations consume capacity immediately.
- `booking_mode = INSTANT` writes directly to `CONFIRMED`.
- `booking_mode = REQUEST` writes to `PENDING_CONFIRMATION`.
- `INSTANT` and `REQUEST` bookings must pass the same availability, policy, and capacity validation; only the initial status differs.
- Owner-created reservations use the same `business_booking` table and lifecycle, with `booking_source = OWNER_CREATED`.
- Booking response DTOs are backend-owned and should expose `allowedActions`, `statusLabel`, and optional `blockingReason`.
- Booking DTOs should also return timezone context, and DST handling remains backend-owned.

## Proposed Schema Work

Primary migrations:

1. `V48__create_business_booking_table.sql`
2. optional early `V49__create_business_booking_audit_event_table.sql` if transition auditing is implemented together

Schema outcomes:

- `business_booking`
- optional `business_booking_audit_event`

Booking-history recommendation:

- keep room for snapshot fields such as `offering_title_snapshot`, `price_snapshot`, and `duration_snapshot_minutes` so historical bookings remain truthful after offering edits

## Proposed Backend Package Work

Models and enums:

- `business/model/BusinessBooking.java`
- `business/model/BusinessBookingStatus.java`
- `business/model/BusinessBookingSource.java`
- optional `business/model/BusinessBookingAuditEvent.java`

Repositories:

- `business/repository/BusinessBookingRepository.java`
- optional `business/repository/BusinessBookingAuditEventRepository.java`

Services:

- `business/service/BusinessBookingService.java`
- `business/service/BusinessBookingReadService.java`
- `business/service/BusinessBookingPrimitiveService.java`
- `business/service/BusinessBookingValidationService.java`
- optional `business/service/BusinessBookingConcurrencySupport.java`
- `business/service/BusinessCreateBookingUseCase.java`
- `business/service/BusinessConfirmBookingUseCase.java`
- `business/service/BusinessRejectBookingUseCase.java`
- `business/service/BusinessCancelBookingUseCase.java`
- `business/service/BusinessCompleteBookingUseCase.java`
- `business/service/BusinessNoShowBookingUseCase.java`

Controllers:

- `business/controller/BusinessBookingController.java`

DTOs:

- `BusinessBookingRequestDTO`
- `BusinessBookingResponseDTO`
- `BusinessBookingListResponseDTO`
- owner booking detail and filter DTOs if needed

Contract requirement:

- booking DTOs should expose backend-prepared `allowedActions`, `statusLabel`, and optional `blockingReason`
- booking list endpoints should start with pagination and filter contracts even if the first UI uses only a small subset

## API Surface For This Phase

Customer:

- `POST /business/bookings`
- `GET /business/bookings/me`
- `GET /business/bookings/me/{bookingId}`
- `POST /business/bookings/me/{bookingId}/cancel`

Owner:

- `GET /business/bookings/owner`
- `GET /business/bookings/owner/{bookingId}`
- `POST /business/bookings/owner/{bookingId}/confirm`
- `POST /business/bookings/owner/{bookingId}/reject`
- `POST /business/bookings/owner/{bookingId}/cancel`
- `POST /business/bookings/owner/{bookingId}/complete`
- `POST /business/bookings/owner/{bookingId}/mark-no-show`

## Key Implementation Decisions To Finalize During This Phase

- Exact cancellation rules for owner and customer when a booking is near start time.

## Concurrency And Retry Requirements

- The booking create path should include an explicit idempotency direction so customer retries do not easily create duplicate reservations.
- The workflow implementation should define one occupancy check strategy and one transaction boundary for create and confirm paths.
- Parallel booking race tests should be part of this phase for scarce-capacity offerings.

## Searchability Hooks To Preserve

- future support queries should remain easy to add for:
  - customer username or email
  - business slug
  - date window

## Implementation Slices

- [ ] Slice 1: implement booking model, repository, and policy-aware validation primitives.
- [ ] Slice 2: define occupancy check strategy, transaction boundary, and booking-create idempotency direction.
- [ ] Slice 3: decide whether booking snapshot fields land in the first booking schema or in a near-follow-up hardening migration.
- [ ] Slice 4: implement booking creation with availability and capacity enforcement.
- [ ] Slice 5: implement customer self-service reads and customer cancellation with pagination or filter contracts.
- [ ] Slice 6: implement owner booking list/detail reads and transition actions with pagination or filter contracts.
- [ ] Slice 7: align DTO actions, docs, and focused workflow plus parallel-race tests with the real lifecycle.

## Validation Plan

- Service tests for:
  - self-booking forbidden
  - out-of-window booking rejected
  - over-capacity booking rejected
  - instant booking path vs request booking path
  - pending/confirmed cancellation rules
  - owner confirmation/rejection gates
- Focused retry and race coverage for:
  - duplicate create attempts for the same customer input
  - parallel booking attempts against limited capacity
  - transaction-safe occupancy evaluation around `PENDING_CONFIRMATION` and `CONFIRMED`
- Controller tests for customer vs owner access boundaries
- Repository/query tests for owner/customer list reads if custom fetch profiles become necessary

## Expected Docs and Artifacts

- `apps/themuffinman/src/main/java/com/themuffinman/app/business/README.md`
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/source-of-truth-inventory.md`

## Closeout Gates

- Booking writes are validated against derived availability and business policy.
- Transition rules are backend-owned and explicit.
- Booking DTOs expose backend-prepared actions and status meaning.
- Capacity-sensitive write paths have an explicit concurrency and retry strategy.
- Timezone and DST semantics stay backend-owned across booking persistence and read DTOs.
- Booking list contracts are ready to scale through pagination and filtering.
- Customer and owner read surfaces are separate and stable enough for later dashboard or vision integration.
