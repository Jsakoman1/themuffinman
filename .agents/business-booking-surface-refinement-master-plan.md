---
machine_kind: master-plan
machine_status: complete
machine_title: Business Booking Surface Refinement Master Plan
machine_goal: Tighten the remaining backend read surfaces for business bookings by adding an owner calendar projection, harmonizing booking read contracts, and closing the docs and validation loop.
---

# Business Booking Surface Refinement Master Plan

## Status

Complete.

## Goal

Tighten the remaining backend read surfaces for business bookings by adding an owner calendar projection, harmonizing booking read contracts, and closing the docs and validation loop.

## Parent God Plan

- God plan: none yet
- Machine-readable path: none

## Scope

- Included: owner calendar projection read model, shared booking read contract reuse, backend-prepared query/page defaults, docs updates, and validation coverage.
- Excluded: frontend changes, payment flows, notification delivery, external calendar sync, and any new mutating Vision execution adapter.

## Child Plans

1. `.agents/business-booking-owner-calendar-projection-plan.md`
2. `.agents/business-booking-read-contract-reuse-plan.md`
3. `.agents/business-booking-docs-validation-closeout-plan.md`

## Execution Order

1. Owner calendar projection
- Priority: must-have
- Reason: the owner dashboard currently has a schedule summary, but not a dedicated calendar projection read surface.

2. Read contract reuse
- Priority: must-have
- Reason: the business booking read layer should keep pagination, query normalization, and page defaults in one reusable backend path.

3. Docs and validation closeout
- Priority: should-have
- Reason: the remaining read-surface rules need to stay aligned in docs and tests before the batch closes.

## Continuation Rule

- After one child plan closes, continue automatically into the next child plan in the declared order without pausing for routine confirmation.
- Stop early only for a real blocker, conflicting user changes, required approval, or a genuine scope change.

## Dependencies

- Existing business booking entities, repositories, and presentation services.
- Existing backend pagination helpers.
- Existing business and domain technical docs.

## Validation

- Targeted business booking service and controller tests.
- Calendar projection tests.
- Read contract reuse tests.
- Final `./mvnw test`.

## Completion Evidence

- Status: complete
- Child plan status: complete
- Validation evidence: `./mvnw test` passed with 585 tests and 0 failures.
- Doc delta summary: added owner calendar projection contract, shared booking read-support helper, updated business/domain docs, and synced agent operating model sections plus endpoint inventory.
- Deferred work: future mutating Vision booking execution remains outside this batch
