# Flexible Business service and booking model analysis

This is a planning analysis for `business-booking-end-to-end-master`. It is not runtime evidence or completion state.

## Product principle

Business is not an appointment type. It is a configurable service, demand, resource, pricing, and fulfillment system. Time is one possible constraint; quantity, people, animals, vehicles, area, equipment, staff, and project scope are other constraints.

## Supported business patterns

| Business | Demand | Resources/capacity | Schedule | Pricing |
| --- | --- | --- | --- | --- |
| Cleaning company | rooms, hours, area, add-ons | one or more employees/teams, travel buffer | appointment or arrival window | fixed, per hour, per room, area, quote |
| Hairdresser | one customer, service variant | one stylist or stylist pool | exact slot, fixed duration | per service, duration-based, add-ons |
| Dog daycare | dogs, nights/days, care options | concurrent animal capacity, staff ratio, space | stay/window, all-day, recurring | per dog/day, tiered quantity, extras |
| Car wash | vehicles, wash package | bays, machines, workers | exact slot or queue/window | per vehicle, package, size modifier |
| Repair/project service | issue, parts, estimated effort | technician/team, equipment | request/quote or scheduled block | quote, fixed package, hourly, parts |

## Canonical model direction

- `Offering` describes what the business sells.
- `Demand specification` describes what the customer supplies: quantity, participants, vehicle/item details, dimensions, options, and notes.
- `Fulfillment mode` describes whether the request is an exact appointment, a time window, an all-day stay, a quantity reservation, a queue/drop-off, a quote request, or a project.
- `Resource requirements` describe required staff roles, staff pools, rooms, bays, equipment, space, or generic capacity pools. A booking may require one resource from a pool or consume N units of a shared pool.
- `Availability rules` apply to a resource or pool and can be recurring, date-specific, blocked, replaced, or capacity-limited.
- `Pricing rules` calculate a preview from base price, billing unit, quantity, duration, options, tiers, minimums, surcharges, discounts, and quote/manual-review state. The final calculated price is always snapshotted on the booking.
- `Booking conditions` are business-authored requirements such as notice, cancellation, deposit, age/size limits, required information, consent, documents, service area, or owner approval. Conditions are structured where they affect validation; copy-only guidance remains presentation text.
- `Booking` stores immutable snapshots of the selected offering, demand, resources/capacity, schedule, pricing calculation, conditions, timezone, and status so later edits cannot rewrite history.

## Guardrails

Do not create a generic expression language or let frontend/Vision invent rules. Start with typed primitives and a backend-published schema. Extend the primitive catalog when a new business pattern is real. Every computed slot and price must be explainable in the preview DTO and revalidated atomically during booking creation.

## Required examples

- A cleaning company can request three rooms and two cleaners; price and duration can depend on quantity and options.
- A hairdresser can choose a 30-minute men's service or a 120-minute women's service; a stylist pool can assign one available stylist.
- Dog daycare can accept ten dogs concurrently, with per-dog quantity and a staff/space capacity rule; it is not modeled as ten fake appointments owned by one employee.
- Car wash can reserve one bay and one worker per vehicle, with package and vehicle-size modifiers.
- A repair company can expose a quote request with optional preferred windows and no fake free slot until an owner confirms the estimate.

## Decision boundary

The first implementation should support the typed primitives above and the examples in this document. Complex arbitrary formulas, payments, tax, and external workforce routing remain explicit follow-up capabilities unless existing product requirements require them. They must not be silently approximated as fixed minute slots.
