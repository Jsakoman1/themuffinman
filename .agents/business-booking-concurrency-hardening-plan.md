---
machine_kind: plan
machine_status: complete
machine_title: Business Booking Concurrency Hardening Plan
machine_goal: Harden booking create and lifecycle transitions against duplicate writes, parallel races, and retry-driven oversells.
---

# Business Booking Concurrency Hardening Plan

## Status

Complete.

## Goal

Harden booking create and lifecycle transitions against duplicate writes, parallel races, and retry-driven oversells.

## Parent Master Plan

- `.agents/business-booking-improvement-master-plan.md`

## Priority

- Must-have

## Scope

- Included: occupancy check strategy, transaction boundary review, lock strategy, idempotency semantics, duplicate create tests, parallel booking tests, and confirm/reject/cancel race coverage.
- Excluded: frontend behavior, payment, notification channels, and any new booking model root.

## Slice Notes

- Keep the booking write path backend-owned.
- Make retry behavior explicit so slow networks do not create duplicate reservations.
- Prefer small transactional slices over one large booking service.

## Validation

- Duplicate create tests.
- Parallel request race tests.
- Capacity boundary tests for single-slot and shared-capacity offerings.
- Locking/transaction tests for the booking write path.
