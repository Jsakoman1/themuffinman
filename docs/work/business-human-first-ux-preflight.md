# Human-first Business UX preflight

Status: atomic hardening in progress; the next implementation task remains locked until this preflight is verifier-verified.

### Follow-up: one booking-rules save action

The settings visual pass exposed two separate save actions on the same owner form. This small follow-up is intentionally atomic: it updates only profile/policy save orchestration and its browser trace, then proves that one visible action persists the current booking settings and displays success.

The program keeps booking permissions, price calculation, availability conflict checks,
and selected-business routing backend-owned. It changes how those capabilities are
explained and sequenced in the Web UI.

Product boundaries:

- A normal business must never need to understand slugs, coordinates, fulfillment
  modes, capacity windows, raw schema rows, or pricing-rule keys to publish a basic
  appointment service.
- Advanced service configuration must be genuinely editable through the existing
  schema contract or clearly labelled as unavailable/deferred; it must not look
  editable when it is read-only.
- The existing resource configuration has no typed complete Web edit contract;
  advanced resource editing remains bounded by `ENTITY-SURFACE-RESOURCE-CONTRACT-001`.
- Customer booking remains one persisted booking per request. This program does not
  introduce recurring series, third-party booking, attachment, or payment domains.
- Browser evidence must use the owned local stack and fresh JSON/screenshot paths
  named by each atomic task.

## Atomic-task analysis

| Queue item | Verified capability base | Human-first delivery decision | Boundary before start |
| --- | --- | --- | --- |
| Owner onboarding | Profile creation accepts name, public summary, area, timezone, and active/booking state. | Move creation from Settings to My businesses; create a draft business, then show a setup checklist. | Do not ask for slug, coordinates, images, policy, or availability during creation. |
| Basic service | Offering writes already support all normal and advanced fields. | Use conservative defaults and ask only name, summary, duration, price mode, and confirmation behavior. | Advanced values must retain backend-safe defaults and stay disclosed, not deleted. |
| Advanced service | Owner schema `GET/PUT` already replaces demand fields, options, and pricing rules. | Build a truthful editor for those three schema lists. | Resource endpoints are raw map/create-only and have no complete typed update/delete contract; retain `ENTITY-SURFACE-RESOURCE-CONTRACT-001` as a visible deferred boundary. |
| Settings | Profile, policy, weekly availability, and exceptions are independently persisted. | Group them as Public page, Hours, Booking rules, and Advanced without changing backend ownership. | Archive remains a confirm-required Advanced action; no policy field may silently change. |
| Working hours | Availability rules and exceptions are service-scoped and backend-validated. | Use words such as working hours, appointment spacing, days off, and special hours. | Capacity stays advanced unless the owner explicitly enables group appointments. |
| Customer booking | Quote and availability read endpoints exist; availability currently accepts raw instant ranges. | Add a business-local date read boundary so the client does not calculate another business's timezone day. | Keep one booking per request; no payment, recurrence, third-party booking, or attachment domain is introduced. |
| Completion | Booking create returns a server-prepared booking response and customer history exists. | Preserve the response as a completion state with next action, rather than reset the form. | Copy must distinguish confirmed bookings from pending owner confirmation and quote-required outcomes. |

## Evidence design

- `business-human-first-runtime.mjs` will be created in the onboarding task and will
  accept `WEB_RUNTIME_EVIDENCE_PATH` and `WEB_VISUAL_EVIDENCE_PATH`; later tasks
  extend it for their own selected route and assertion.
- Every visual task changes a task-specific JSON and PNG after its `work-start`
  snapshot. The browser trace must confirm the selected business/offer and the
  relevant backend API response, not only the route.
- The date-first task adds a focused `BusinessAvailabilityReadServiceTest` because
  the new business-local-date API behavior is backend logic. No other planned task
  changes backend behavior merely to improve presentation.

## Readiness decision

The plan is ready for goal pursuit after the hardening task verifies these boundaries.
There is no unresolved product decision that blocks the first slice.

Hardening assertions confirmed:

- Every implementation item has one observable user outcome, exact required paths,
  serial dependency, leaf validation command, and evidence boundary.
- Owner/service/settings tasks are presentation-led unless they change a named
  backend contract; the only planned new backend behavior is business-local date
  resolution for public availability.
- Resource editing is not silently expanded beyond the existing create-only raw-map
  API, and the deferred contract has a stable backlog ID.

## Workspace boundary

The preflight System Map run observed 73 already changed worktree files. They include
prior Business and unrelated product work. This program must preserve them: every
atomic task will use `make work-start` and the verifier's required-path snapshot so
only its explicitly listed paths count as new work or evidence.
