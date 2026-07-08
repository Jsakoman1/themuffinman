---
machine_kind: plan
machine_status: proposed
machine_title: Business Booking Foundation Plan
machine_goal: Establish the booking-ready business root by extending business profiles and introducing owner-managed offerings and booking policy defaults without opening real reservations yet.
---

# Business Booking Foundation Plan

## Status

Proposed.

## Goal

Establish the booking-ready business root by extending `business_profile` and introducing owner-managed `business_offering` plus `business_booking_policy` ownership so later availability and reservation phases have a stable domain base.

## Parent Master Plan

- Master plan: `.agents/business-booking-implementation-master-plan.md`

## Continuation Rule

- Once this phase reaches its closeout gates, execution should continue automatically into `.agents/business-booking-availability-plan.md` without a routine stop.
- Do not pause after this phase only to ask whether to continue unless a real blocker or approval need appears.

## Scope

- Included: profile schema extension, offering model, owner offering CRUD, business booking policy defaults, backend package bootstrap, and source-of-truth updates for the new business boundary.
- Excluded: recurring availability rules, availability derivation, customer reservations, owner booking actions, notifications, gallery media, and calendar-style owner dashboards.

## Why This Phase Exists

- The current `business` module only has a profile root. Booking should not be layered directly on top of profile rows.
- `business_offering` is the true bookable root. Without it, later scheduling and booking work would couple too much logic to the business profile itself.
- A dedicated `business_booking_policy` row avoids scattering lead-time and cancellation defaults across multiple future services.

## Phase Analysis

### Problem Being Solved

The current business capsule is too thin for booking:

- no service catalog
- no business-level booking defaults
- no owner-side modular CRUD for bookable services

If booking started directly from `business_profile`, the module would immediately mix public identity, scheduling rules, and reservation policy in one table and one service.

### Architectural Decisions

- Keep `BusinessProfile` as the business identity root.
- Add `BusinessOffering` as the first bookable entity root.
- Add `BusinessBookingPolicy` as one row per business for operational defaults.
- Add typed `business/config` operational properties early so lead-time, max-advance, cancellation, dashboard staleness, and list-size defaults do not spread through future services.
- Treat offering removal as archival-safe by default so later bookings are not left without readable business context.
- Keep controller and DTO surfaces owner-focused in this phase.
- Follow `workmarket`-style service layering from the start rather than adding one oversized `BusinessService`.

### Main Risks

- Overfilling `business_profile` with booking-specific state.
- Introducing owner CRUD without stable DTO boundaries.
- Committing too early to overly specific offering enums that later block simple business types.

### Mitigations

- Limit `business_profile` additions to stable public identity and booking switches.
- Put pricing, duration, capacity, and booking mode only on `business_offering`.
- Keep policy defaults in a separate table and service.

## Proposed Schema Work

Primary migrations:

1. `V44__extend_business_profile_for_booking.sql`
2. `V45__create_business_offering_table.sql`
3. `V46__create_business_booking_policy_table.sql`

Schema outcomes:

- `business_profile` gains `timezone`, `booking_enabled`, and a small set of public booking-facing fields.
- `business_offering` becomes the owner-managed service catalog.
- `business_booking_policy` stores stable defaults for future scheduling and cancellation logic.

## Proposed Backend Package Work

Model and enum roots:

- `business/model/BusinessOffering.java`
- `business/model/BusinessBookingPolicy.java`
- `business/model/BusinessOfferingPricingType.java`
- `business/model/BusinessOfferingDurationMode.java`
- `business/model/BusinessOfferingCapacityMode.java`
- `business/model/BusinessOfferingBookingMode.java`

Repositories:

- `business/repository/BusinessOfferingRepository.java`
- `business/repository/BusinessBookingPolicyRepository.java`

Services:

- `business/service/BusinessOfferingService.java`
- `business/service/BusinessBookingPolicyService.java`
- optional `business/service/BusinessProfilePolicyService.java`

Config:

- `business/config/BusinessProperties.java`

Controllers:

- `business/controller/BusinessOfferingController.java`
- optional `business/controller/BusinessBookingPolicyController.java`

DTOs:

- owner CRUD request/response DTOs for offerings
- owner policy read/update DTOs

Offering lifecycle note:

- owner-facing delete behavior should be evaluated as deactivate/archive first, not hard-delete by default

## API Surface For This Phase

Keep public profile routes stable:

- `GET /business/profiles`
- `GET /business/profiles/{slug}`
- `GET /business/profiles/me`
- `PUT /business/profiles/me`

Add owner-only offering routes:

- `GET /business/offerings/me`
- `POST /business/offerings/me`
- `PUT /business/offerings/{offeringId}/me`
- `DELETE /business/offerings/{offeringId}/me`

Add owner-only policy routes:

- `GET /business/booking-policy/me`
- `PUT /business/booking-policy/me`

## Implementation Slices

- [ ] Slice 1: extend `business_profile` with stable booking-facing fields and update profile DTO/service mapping.
- [ ] Slice 2: introduce `business_offering` model, repository, enums, DTOs, service, and owner CRUD endpoints.
- [ ] Slice 3: introduce `business_booking_policy` model, repository, DTOs, service, and owner policy endpoints.
- [ ] Slice 4: introduce typed `business/config` operational properties for default lead time, max advance horizon, default cancellation window, dashboard staleness thresholds, and booking list sizing.
- [ ] Slice 5: lock offering deactivation or archival-safe semantics so later bookings cannot lose readable offering context.
- [ ] Slice 6: align `README`, source-of-truth docs, and package maps with the expanded business boundary.
- [ ] Slice 7: validate focused service and controller behavior plus migration bootstrapping.

## Validation Plan

- Migration boot test or backend startup coverage for new business tables.
- Focused service tests for offering create/update validation and policy ownership.
- Controller tests for owner-only access and profile/offering DTO shape.
- `AgentOperatingModelValidationTest` only if machine-readable operating docs or protected phrases are touched.

## Expected Docs and Artifacts

- `apps/themuffinman/src/main/java/com/themuffinman/app/business/README.md`
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/source-of-truth-inventory.md`

## Closeout Gates

- `business_profile` remains the identity root, not the booking workflow root.
- `business_offering` and `business_booking_policy` exist as first-class module-owned entities.
- Operational defaults are centralized in typed `business/config` properties instead of future scattered service constants or `@Value` fields.
- Offering lifecycle is archival-safe enough that later booking history will not depend on hard-deleting offering meaning.
- Owner CRUD and policy endpoints are stable enough for later availability work.
- No scheduling or booking logic is prematurely embedded in profile service methods.
