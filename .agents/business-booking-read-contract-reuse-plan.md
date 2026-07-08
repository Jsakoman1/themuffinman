---
machine_kind: plan
machine_status: complete
machine_title: Business Booking Read Contract Reuse Plan
machine_goal: Centralize booking read query normalization, pagination defaults, and shared filtering so customer and owner booking lists stay aligned.
---

# Business Booking Read Contract Reuse Plan

## Status

Complete.

## Goal

Centralize booking read query normalization, pagination defaults, and shared filtering so customer and owner booking lists stay aligned.

## Parent Master Plan

- `.agents/business-booking-surface-refinement-master-plan.md`

## Priority

- Must-have

## Scope

- Included: shared query helper, page/size clipping, normalized search handling, and reuse across booking read surfaces.
- Excluded: frontend changes, booking write logic, and new read models outside business bookings.

## Slice Notes

- Keep query handling backend-owned.
- Avoid duplicating the same safe page/size and query normalization rules in multiple services.
- Preserve the existing DTO shape and pagination envelope.

## Validation

- Unit tests for shared query helper behavior.
- Customer and owner list contract tests.
