# System Map Hardening Preflight

This preflight defines the next complete mapping pass. It expands the System Map by
coverage and traceability, not by adding aspirational architecture.

The deep-dive review and readiness record is `docs/system-map-hardening-deepdive-2026-07-22.md`.

## Scope

The pass covers Identity/Security, Circles/Visibility/Consent, Work, Business,
Things, Rides, Chat, Notifications/Activity, Vision, native handoff contracts,
shared backend services, scheduled jobs, storage, providers, configuration, and the
Web/frontend app shell.

## Required mapping edge

Every important surface should be traceable as:

`product capability -> module owner -> data owner -> workflow/state -> endpoint/service -> client -> test -> runtime evidence -> operational boundary`

An unavailable edge is recorded as a typed gap with owner, required evidence, next
action, and review date. It is never silently inferred from a nearby module.

## Source-reuse rule

Existing endpoint, capability, runtime, dependency, truth, workflow, visibility,
side-effect, security, provider, and native-client registries remain authoritative
for the facts they own. The hardening registries are indexes and reconciliations;
they must not create duplicate status authorities.

## Serial dependency rule

The child plans run in this order: topology, interface evidence, dependencies/data,
workflows/permissions, side-effects/operations, client/external boundaries, and
closeout. Later plans may reference earlier outputs but may not silently complete
their work out of order.

## Module completion rule

Every module must be assessed across topology, ownership, dependencies, schema,
endpoints, capabilities, clients, workflows, permissions, tests, runtime evidence,
side-effects, jobs, providers/storage, configuration, impact, and drift. `mapped`,
`partial`, `planned`, `deferred`, and `unknown` remain distinct statuses.

## Hardening questions

- Which module owns each entity, state transition, permission, and side-effect?
- Which dependencies are allowed, forbidden, or temporarily accepted?
- Which Web and Vision actions use the same backend authority?
- Which events are durable, process-local, synchronous, scheduled, or external?
- What survives a timeout, provider failure, transaction rollback, process crash,
  reconnect, retention job, or object-storage outage?
- Which claims are current truth, historical analysis, product intent, or runtime
  evidence?
- What must be rechecked when a shared concept such as users, circles, scheduling,
  visibility, bookings, or messaging changes?

## Completion boundary

The result is a stronger operating map, not a claim that every capability or runtime
scenario is complete. The closeout must preserve product gaps, native/device gaps,
external-provider gaps, and runtime evidence gaps separately.
