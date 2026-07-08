---
machine_kind: plan
machine_status: complete
machine_title: Business Booking Future Hooks Plan
machine_goal: Add future-facing integration boundaries for vision capabilities, notifications, and admin or ops support without introducing a second business model.
---

# Business Booking Future Hooks Plan

## Status

Complete.

## Goal

Add future-facing integration boundaries for vision capabilities, notifications, and admin or ops support without introducing a second business model.

## Parent Master Plan

- `.agents/business-booking-improvement-master-plan.md`

## Priority

- Nice-to-have

## Scope

- Included: capability publishing notes for `/vision`, notification vocabulary, admin/ops support hooks, and media/gallery refinements that stay decoupled from core booking logic.
- Excluded: full notification delivery implementation, full admin tooling, and any direct coupling to a new frontend.

## Slice Notes

- Keep future integrations as capability boundaries.
- Reuse the same backend read models for operations and support.
- Do not let future hooks mutate the booking root into a multi-purpose module.

## Validation

- Boundary tests for capability publication.
- Vocabulary consistency checks for future notifications.
- Optional integration tests once a downstream consumer exists.
