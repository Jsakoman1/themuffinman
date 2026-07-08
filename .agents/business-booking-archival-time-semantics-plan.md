---
machine_kind: plan
machine_status: complete
machine_title: Business Booking Archival And Time Semantics Plan
machine_goal: Preserve booking history correctness with snapshot fields, archival-safe offering handling, and explicit timezone/DST rules.
---

# Business Booking Archival And Time Semantics Plan

## Status

Complete.

## Goal

Preserve booking history correctness with snapshot fields, archival-safe offering handling, and explicit timezone/DST rules.

## Parent Master Plan

- `.agents/business-booking-improvement-master-plan.md`

## Priority

- Must-have

## Scope

- Included: booking snapshot fields, offering deactivation/archival behavior, historical read fidelity, timezone context in DTOs, and DST edge-case tests.
- Excluded: pricing engine changes, payment flow, and external calendar synchronization.

## Slice Notes

- Keep historical bookings readable even if the offering is later edited or disabled.
- Persist absolute timestamps for bookings while collecting business-local availability rules.
- Treat DST as a backend rule, not a frontend concern.

## Validation

- Timezone and DST boundary tests.
- Snapshot field persistence tests.
- Archival/deactivation behavior tests for offerings and related bookings.
