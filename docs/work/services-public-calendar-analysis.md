# Public Services calendar analysis

## Product decision

Build a public availability calendar only after a visitor selects a service. Month answers which days are available, Week shows safe availability blocks, and Day exposes exact clickable appointment times. This is not the owner's calendar.

## Privacy boundary

Public data may contain business-local date/time, `AVAILABLE`, `LIMITED`, or `UNAVAILABLE`, aggregate slot count, and bookable slot instants. It must never contain an existing booking, customer identity, note, service demand, owner exception reason, resource identity, or capacity consumption detail.

## Architecture decision

`BusinessAvailabilityReadService` remains the authority and uses the existing availability/resource computation. A bounded public projection groups safe derived windows into business-local day and time-bucket state. The Web calendar owns only view cursor, Day/Week/Month presentation, and selected slot. Quote, preview, and booking create revalidate the slot.

## Delivery sequence

1. Atomic-task hardening.
2. Backend public projection, contract generation, and privacy tests.
3. Reusable accessible Month/Week/Day UI.
4. Booking handoff, selected-service continuity, and stale-slot recovery.
5. Documentation, responsive checks, browser evidence, and truth closeout.

The execution inventory is intentionally strict: no contract, UI, or booking change starts before its immediately preceding verifier evidence is recorded.

## Explicit non-goals

Owner calendar reuse, event detail, calendar sharing, payments, availability reason disclosure, review moderation, and native calendar implementation.
