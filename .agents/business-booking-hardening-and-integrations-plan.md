---
machine_kind: plan
machine_status: complete
machine_title: Business Booking Hardening And Integrations Plan
machine_goal: Harden the business booking domain with audit history, public media support, event hooks, operational reads, and integration-ready boundaries after the core reservation workflow is stable.
---

# Business Booking Hardening And Integrations Plan

## Status

Complete.

## Goal

Harden the business booking domain with audit history, public media support, event hooks, operational reads, and integration-ready boundaries after the core reservation workflow is stable.

## Parent Master Plan

- Master plan: `.agents/business-booking-implementation-master-plan.md`

## Continuation Rule

- This is the final child phase in the declared sequence.
- After this phase, run the full master-plan closeout pass instead of pausing after local implementation slices.
- Do not stop before the master-plan closeout unless a real blocker or approval need appears.

## Scope

- Included: booking audit trail, gallery/media rows, owner dashboard summary reads, domain events for booking side effects, and future integration hooks for chat, admin tools, or `/vision`.
- Excluded: full payment integration, external calendar sync, multi-resource staffing, and marketplace-wide search ranking.

## Why This Phase Exists

- Core booking logic alone is not enough for operational support and future module reuse.
- Audit, event, and dashboard surfaces should be added after the core workflow is stable so the team does not optimize the wrong abstractions too early.
- Gallery/media support is intentionally deferred here so the first booking rollout is not blocked by storage and upload concerns.

## Phase Analysis

### Problem Being Solved

Without this phase:

- booking history is harder to support or debug
- public business pages stay visually shallow
- future notification/chat hooks would require intrusive rewiring of core services
- owners would lack compact operational summary surfaces

### Architectural Decisions

- Add `business_booking_audit_event` as explicit history rather than hiding change data only in mutable rows.
- Add `business_gallery_image` as a business-owned media table and not as ad-hoc JSON inside profile description.
- Publish explicit domain events for booking creation and status changes.
- Keep dashboard reads separate from raw booking list endpoints.
- Add an explicit owner schedule read-model layer so owner booking list and owner dashboard summary do not drift into separate "today/upcoming" interpretations.
- Preserve a future `/vision` boundary where `business` can later publish `view_business`, `view_business_availability`, and `create_booking` capabilities without a second business runtime model.
- Lock a future notification vocabulary early even if delivery and channels remain out of scope in this phase.

### Main Risks

- Pulling non-essential features into the workflow phase too early.
- Leaking integration concerns into core booking services.
- Making media storage assumptions before the storage abstraction is ready.

### Mitigations

- Treat this as a post-core hardening phase.
- Keep events explicit and synchronous at first, matching existing repository conventions.
- Allow `image_url` transitional support if object storage abstraction is not yet finalized.

## Proposed Schema Work

Primary migrations:

1. `V49__create_business_booking_audit_event_table.sql`
2. `V50__create_business_gallery_image_table.sql`
3. optional `V51__tighten_business_booking_constraints.sql`

## Proposed Backend Package Work

Models:

- `business/model/BusinessBookingAuditEvent.java`
- `business/model/BusinessGalleryImage.java`

Repositories:

- `business/repository/BusinessBookingAuditEventRepository.java`
- `business/repository/BusinessGalleryImageRepository.java`

Services:

- `business/service/BusinessOwnerDashboardReadService.java`
- `business/service/BusinessOwnerScheduleReadService.java`
- `business/event/BusinessBookingCreatedEvent.java`
- `business/event/BusinessBookingStatusChangedEvent.java`
- `business/event/BusinessBookingEventHandler.java`

Event vocabulary note:

- reserve stable business event names or concepts for:
  - booking created
  - booking confirmed
  - booking rejected
  - booking cancelled
  - booking upcoming reminder

Controllers:

- optional `business/controller/BusinessOwnerDashboardController.java`
- optional gallery/media management controller if backend media metadata ownership is needed immediately

## API Surface For This Phase

Owner dashboard:

- `GET /business/dashboard/me`

Owner schedule summary:

- optional `GET /business/bookings/owner/schedule`

Optional media routes:

- `GET /business/gallery/me`
- `POST /business/gallery/me`
- `PUT /business/gallery/{imageId}/me`
- `DELETE /business/gallery/{imageId}/me`

The exact media upload flow may depend on the platform's storage strategy and can be deferred even if the table exists.

## Implementation Slices

- [ ] Slice 1: add audit event persistence and wire booking transitions to emit audit records.
- [ ] Slice 2: introduce domain events for booking created and status changed side effects.
- [ ] Slice 3: add one explicit owner schedule read-model layer for calendar projection or schedule summary.
- [ ] Slice 4: add owner dashboard summary reads over bookings, offerings, pending actions, and owner schedule summary inputs.
- [ ] Slice 5: add gallery/media metadata support for public business pages if the storage direction is ready.
- [ ] Slice 6: record future `/vision` capability publishing requirements plus stable notification vocabulary for later side effects.
- [ ] Slice 7: add docs-as-contract and regression-scenario notes for booking policy workflows once the core lifecycle is implemented.
- [ ] Slice 8: tighten constraints, docs, and integration notes after the first stable booking workflow exists.

## Validation Plan

- Service tests for audit emission on booking transitions
- Event handler tests if notifications or side effects are added
- Owner dashboard and owner schedule read tests for pending/upcoming counts, summary sections, and shared schedule semantics
- Optional media tests if image metadata becomes part of public page DTOs
- Docs-as-contract follow-up should link booking workflow scenarios to focused tests once the workflow phase lands

## Expected Docs and Artifacts

- `apps/themuffinman/src/main/java/com/themuffinman/app/business/README.md`
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/source-of-truth-inventory.md`

## Closeout Gates

- Core booking services do not need to know downstream notification or UI concerns directly.
- Audit history exists for operational troubleshooting.
- Owner summary and owner schedule surfaces are backend-prepared, stable, and sourced from one shared schedule read-model layer.
- Future `/vision` integration can consume business capabilities without introducing a separate business runtime model.
- Future booking notifications can reuse a named event vocabulary instead of inventing ad-hoc event meanings later.
- Business booking is ready for later docs-as-contract and regression-scenario linkage around policy-heavy workflows.
- Media and event hooks stay modular instead of distorting the core reservation model.
