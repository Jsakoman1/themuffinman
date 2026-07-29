# Business product-alignment preflight

Date: 2026-07-29

## Product decision

Business is a small public business website plus owner-operated appointment workflow.
Customers may discover an active business, inspect active services, receive an
authoritative quote and availability, then request one appointment. Owners operate
one selected business at a time through URL-scoped Overview, Calendar, Bookings,
Services, and Settings. Aggregate personal scheduling remains on the global Calendar.

## Residual defects

1. `BusinessBookingsView` depends on active session storage instead of receiving the
   selected `businessId` from the owner URL, and its Calendar handoff loses context.
2. Service setup opens without the selected offering id, so it can default to the
   first offering rather than the service the owner chose.
3. Public booking exposes recurrence, recipient, and file controls, but the request
   DTO has no recurrence, recipient, consent, or attachment lifecycle fields. The
   controls are removed in this program rather than silently encoded into a note.
4. The owner month view is a 35-day/five-column grid rather than calendar weeks.
5. Discovery exposes several intents explicitly known not to be present in the
   public read model. Only backed filters remain visible.

## Atomic inventory check

Each delivery item has one observable result, bounded required paths, one leaf
validation, and explicit dependencies. The calendar task is the only visual/runtime
task and creates fresh evidence. Public booking remains a single booking capability;
new recurrence, delegated booking, or attachment lifecycle work requires a future
domain proposal, migration, permission model, DTO contract, and dedicated plan.

Verified before implementation: the owner-context task changes only URL/API handoff
paths; the public-booking task deliberately removes UI-only promises without changing
the booking DTO; calendar and discovery each have separate client outcomes. No task
relies on a historical screenshot or broad aggregate validation as new evidence.

## Baseline reuse

The verified Business workspace repair is reused for selected-owner dashboard,
calendar endpoint ownership, booking state transitions, public quote, availability,
and existing runtime smoke. None are counted as new completion. This program only
retests routes or public booking runtime when its own client contract changes.
