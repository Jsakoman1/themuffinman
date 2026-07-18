# Cross-module workspace adoption audit

## Scope

This audit covers the existing authenticated Business booking routes and Rides routes before their workspace presentation is changed. It is an implementation boundary, not a replacement product design.

| Route family | Existing authoritative read/action contract | Web adoption gap | Safe implementation boundary |
| --- | --- | --- | --- |
| Business owner bookings | `BusinessBookingReadService` and `BusinessBookingPresentationService` prepare booking labels, allowed actions, and blocking reasons. Owner mutation and reschedule use cases enforce policy, capacity, authorization, audit, and event behavior. | `BusinessBookingsView` uses a route-specific row layout and inline action cluster rather than the shared workspace row grammar. | Recompose metadata and backend-provided actions into dense workspace rows; keep each mutation routed through the existing typed API and reload after completion. |
| Customer bookings | Customer booking reads expose the same backend-prepared labels and allowed actions, while cancellation/reschedule are participant-authorized backend operations. | `BusinessMyBookingsView` has an isolated list and styling, with no shared row hierarchy. | Adopt the same compact row hierarchy and visible recovery state; do not infer cancellability or rescheduling eligibility. |
| Ride discovery and owner rides | `RideOfferService` owns visibility, lifecycle, capacity, joins, start/complete, cancellation, and audit/notification fanout. Viewer DTOs prepare permitted actions. | `RidesView` mixes discovery, commute preferences, form state, and lifecycle actions into an isolated surface. | Improve hierarchy and row density while retaining the current direct routes, forms, backend filters, and reload-on-mutation flow. |

## Required invariants

- No client-side filtering, permission derivation, lifecycle transition, optimistic mutation, bulk action, or visibility inference is introduced.
- Booking reschedule continues to send only typed start/end values to existing backend use cases.
- Ride matching stays server-filtered and circle visibility stays enforced by the ride service.
- Empty, loading, error, and post-action reload states remain explicit on all three routes.
- The next implementation slice changes all named route surfaces and `SurfaceRow`; it does not add boards, subscriptions, saved views, or generic action authority.
