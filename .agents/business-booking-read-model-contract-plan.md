---
machine_kind: plan
machine_status: complete
machine_title: Business Booking Read Model Contract Plan
machine_goal: Standardize owner and customer booking read surfaces so lists, schedule summaries, and dashboard projections share one contract.
---

# Business Booking Read Model Contract Plan

## Status

Complete.

## Goal

Standardize owner and customer booking read surfaces so lists, schedule summaries, and dashboard projections share one contract.

## Parent Master Plan

- `.agents/business-booking-improvement-master-plan.md`

## Priority

- Must-have

## Scope

- Included: pagination/filter contracts, shared schedule summary DTOs, owner/customer list response shape, backend-prepared status metadata, and query surface alignment.
- Excluded: UI composition and any direct client-side derivation of booking rules.

## Slice Notes

- Keep `allowedActions`, `statusLabel`, and `blockingReason` backend-owned.
- Make owner dashboard and owner schedule read from one projection path.
- Keep customer and owner list DTOs stable as booking volume grows.

## Validation

- Pagination contract tests.
- Filter contract tests.
- Owner/customer list shape tests.
- Schedule summary consistency tests.
