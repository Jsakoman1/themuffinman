# Exhaustive Capability Decomposition Review

## Review result

The first catalog contained 62 target records and linked them to 24 broad current
inventory IDs. That mapping was useful for navigation but overstated detail: a
single current record such as `work.application` represented several different
user actions and lifecycle states.

The source review found explicit behavior for Work execution and reviews, Circle
request lifecycle, Chat read/reconnect/reaction/attachment flows, Business
availability and booking transitions, Thing owner and borrow flows, Ride
lifecycle, profile/location privacy, native handoff, adaptive shell, and
traceability.

The catalog now contains 123 atomic records across all eight documented modules.

## Mapping rule

Target capabilities remain separate from current implementation status. A target
record linked to a broad inventory record is marked `mapping_quality: broad` in
the generated coverage report. Broad mapping is not evidence that the detailed
action is implemented; it is an explicit decomposition gap.

The next implementation slices should replace broad links with exact current
inventory records or work-plan evidence as each behavior becomes independently
audited.

## Deliberate boundaries

- Acceptance statements remain inside each capability but are not automatically
  promoted to separate capabilities.
- Admin-only operations are included only when they change a product workflow.
- Mobile and Watch target requirements are recorded even when no current client
  evidence exists.
