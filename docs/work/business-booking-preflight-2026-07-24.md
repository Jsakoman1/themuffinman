# Business flexible booking preflight — 2026-07-24

Status: planning preflight, not implementation evidence.

## Findings from the current repository

1. The current `BusinessOffering` supports only one base price, one duration mode, one capacity mode, one integer slot capacity, booking mode, and minute buffers.
2. `BusinessBooking` persists only `startsAt`, `endsAt`, timezone, one price snapshot, one duration snapshot, notes, and offering title. It cannot yet snapshot quantity, options, demand fields, assigned resources, calculated price lines, or quote state.
3. `BusinessBookingPreviewRequestDTO` accepts only offering id and start time. The current preview cannot receive quantity, participants, animals, vehicles, dimensions, options, or customer answers.
4. `BusinessAvailabilityWindowDTO` represents only a time interval and effective capacity. It cannot represent an arrival window, all-day stay, remaining quantity, resource candidates, quote-required state, or clarification-required state.
5. Existing availability computation creates minute-based windows from recurring rules. This must remain a reusable primitive for appointment services, not the universal model for all services.
6. There are no current Business resource, staff-pool, equipment, offering-option, demand-schema, pricing-rule, quote, or booking-snapshot entities.
7. Web already has separate Business profile, offering, availability, exception, public profile, booking, and calendar routes. The flexible setup should extend those surfaces and add focused sub-surfaces only where a collection becomes too complex.
8. Vision already has Business profile, offering, availability, booking lifecycle, discovery, and calendar route support. It has no dedicated generic quote/demand/resource schema route yet.
9. The current migration head is `V81__add_workmarket_news_outbox_operations.sql`. The flexible Business model must use separate forward-only migrations starting at V82, and the plan must not edit existing migrations.

## Scope decision before goal pursuing

The first implementation uses a typed primitive catalog, not an arbitrary formula or scripting engine.

Required first-release fulfillment primitives:

- exact appointment
- arrival/time window
- all-day or multi-day stay
- quantity/capacity reservation
- package with typed options/add-ons
- quote/project request with optional preferred windows

Queue/drop-off is represented as a bounded arrival window or quote/project request first. A true queue algorithm is a separate follow-up capability and must not be implied by the first release.

Required first-release pricing primitives:

- fixed amount
- per minute/hour/day
- per quantity/unit
- per participant/animal/item
- package price
- tiered quantity pricing
- option/add-on modifiers
- minimum charge
- free
- quote/manual review

Arbitrary formulas, payments, tax, external workforce routing, and automated optimization remain explicit follow-up scope. The backend contract must expose enough typed metadata to add them later without corrupting bookings.

## Required implementation boundaries

- `BusinessOffering` remains the published service identity and archival root.
- New typed child records describe options, demand fields, pricing rules, resources, and resource requirements.
- Booking creation first produces a backend quote/availability preview, then atomically validates and snapshots demand, price lines, capacity consumption, resource assignment, conditions, and schedule.
- Existing appointment bookings remain readable through compatibility mapping while the new booking snapshot is introduced.
- Web and Vision consume the same backend-prepared preview DTO and never calculate capacity or price independently.
- Business-authored free text is sanitized and displayed as copy; only typed fields participate in validation.

## Plan changes required

- Split the planned single `V82` migration into explicit forward-only migrations for service configuration, options/demand, resources, pricing, and booking snapshots.
- Add resource and pricing/demand ownership paths to the domain and owner setup plans.
- Add quote/preview DTOs and a typed result contract to the public Web plan.
- Add Vision route metadata and execution adapters for quote/demand configuration and selection.
- Add scenario coverage for cleaning, hairdresser, dog daycare, car wash, and repair/project before runtime closeout.
