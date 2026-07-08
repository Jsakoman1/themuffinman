---
machine_kind: master-plan
machine_status: complete
machine_title: Business Booking Improvement Master Plan
machine_goal: Harden and extend the business booking domain after the core backend implementation by sequencing the remaining work into must-have, should-have, and nice-to-have slices.
---

# Business Booking Improvement Master Plan

## Status

Complete.

## Goal

Harden and extend the business booking domain after the core backend implementation by sequencing the remaining work into `must-have`, `should-have`, and `nice-to-have` slices that stay backend-centric and reuse the existing modular architecture.

## Parent God Plan

- God plan: none yet
- Machine-readable path: none

## Scope

- Included: concurrency hardening, read-model contract cleanup, historical integrity, timezone/DST edge coverage, pagination/filter contracts, operational config consolidation, API contract polish, search hooks, docs-as-contract reinforcement, and future integration boundaries.
- Excluded: frontend implementation, payment flows, external calendar sync, multi-staff assignment, marketplace ranking, and any second business runtime model.

## Priority Slices

### Must Have

- Concurrency hardening and race coverage.
- Read-model contract standardization for owner and customer booking lists.
- Historical booking integrity through snapshot fields and archival-safe offering handling.
- Time semantics and DST edge-case hardening.

### Should Have

- Operational config consolidation and policy defaults.
- Docs-as-contract and regression-scenario coverage for booking workflows.
- Searchability hooks for customer, business, and date-window support.
- API contract polish for richer backend-prepared responses and error semantics.

### Nice To Have

- Future `/vision` capability publishing hooks.
- Notification vocabulary wiring for later delivery channels.
- Admin and ops support surfaces that sit on top of the same backend read models.
- Media and gallery refinements that do not distort the booking core.

## Child Plans

1. `.agents/business-booking-concurrency-hardening-plan.md`
2. `.agents/business-booking-read-model-contract-plan.md`
3. `.agents/business-booking-archival-time-semantics-plan.md`
4. `.agents/business-booking-operational-contract-plan.md`
5. `.agents/business-booking-api-contract-observability-plan.md`
6. `.agents/business-booking-future-hooks-plan.md`

## Execution Order

1. Concurrency hardening
- Priority: must-have
- Reason: occupancy, retries, and parallel writes are the highest-risk failure mode in booking systems.

2. Read-model contract standardization
- Priority: must-have
- Reason: owner and customer lists must share one stable contract before more surfaces reuse the same data.

3. Historical integrity and time semantics
- Priority: must-have
- Reason: booking history and timezone behavior must remain correct before the module grows further.

4. Operational contract hardening
- Priority: should-have
- Reason: central config, documentation rules, and search hooks reduce future drift and scattered defaults.

5. API contract and observability polish
- Priority: should-have
- Reason: backend-prepared responses and stable error semantics make mobile and future clients easier to support.

6. Future hooks and integration boundaries
- Priority: nice-to-have
- Reason: integration hooks should stay modular and only land once the core contract is stable.

## Execution Continuation Rule

- After one child plan closes, continue automatically into the next child plan in the declared order without pausing for routine confirmation.
- Stop early only for a real blocker, conflicting user changes, required approval, or a genuine scope change.

## Cross-Phase Dependency Map

### Concurrency -> Read Models

- Parallel booking protections must be in place before list and schedule contracts are trusted as operational truth.

### Read Models -> Historical Integrity

- Query contracts need to stabilize before snapshot and archival behavior is finalized across booking history.

### Historical Integrity -> Operational Contract

- Historical safety and timezone semantics should be locked before config defaults and docs-as-contract rules are polished.

### Operational Contract -> API Polish

- Centralized defaults and search hooks should exist before the API contract is tuned for future clients.

### API Polish -> Future Hooks

- Future capability publishing and notification vocabulary should only sit on top of a stable booking contract.

## Risks

### Risk 1: oversell under retry or parallel traffic

Mitigation:

- lock down transaction boundaries
- add duplicate booking retry tests
- define explicit idempotency behavior on create

### Risk 2: list and dashboard surfaces drift apart

Mitigation:

- keep one shared read-model path for owner schedule summaries
- standardize filter and pagination contracts early

### Risk 3: historical bookings lose meaning after changes

Mitigation:

- keep snapshot fields on booking rows
- require archival-safe offering handling
- cover timezone and DST edge cases in tests

### Risk 4: future integrations distort the core model

Mitigation:

- delay notification, `/vision`, and admin hooks to the final slice
- keep them as capability boundaries, not new domain roots

## Validation Strategy

- Must-have slices:
  - race-condition and duplicate-create tests
  - read-model contract tests for pagination and filtering
  - timezone and DST boundary tests
- Should-have slices:
  - config and docs tests where operational defaults are centralized
  - regression scenarios for workflow rules and response contracts
- Nice-to-have slices:
  - targeted integration tests only when future hooks become real behavior

## Completion Evidence

- Status: complete
- Child plan status: complete
- Validation evidence: `./mvnw test -Dtest='Business*Test'` and `./mvnw test` passed in `apps/themuffinman`
- Doc delta summary: business docs now note side-effect-free policy resolution for read/presentation paths
- Deferred work: future `/vision`, notification delivery, and admin/ops support remain in the nice-to-have bucket for a later implementation batch
