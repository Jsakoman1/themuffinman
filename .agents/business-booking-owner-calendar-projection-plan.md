---
machine_kind: plan
machine_status: complete
machine_title: Business Booking Owner Calendar Projection Plan
machine_goal: Add a dedicated owner calendar projection read surface so business owners can consume a backend-prepared calendar view derived from bookings.
---

# Business Booking Owner Calendar Projection Plan

## Status

Complete.

## Goal

Add a dedicated owner calendar projection read surface so business owners can consume a backend-prepared calendar view derived from bookings.

## Parent Master Plan

- `.agents/business-booking-surface-refinement-master-plan.md`

## Priority

- Must-have

## Scope

- Included: owner calendar projection DTO, backend read service, controller endpoint, and schedule/calendar tests.
- Excluded: frontend rendering, external calendar sync, and write-path changes.

## Slice Notes

- Reuse the same business booking read model and timezone semantics.
- Prefer calendar projection data over frontend grouping.
- Keep the response backend-prepared so future mobile clients can reuse it.

## Validation

- Calendar projection service tests.
- Controller contract tests.
- Timezone-aware projection checks.
