# Core Product Stabilization and Business/Rides Preflight

This program is maintenance-first. It reuses verified repair, Business, and Rides
plans and focuses only on residual integration, correctness, privacy, concurrency,
recovery, and release evidence gaps.

The deep-dive readiness review is recorded in
`docs/core-product-stabilization-business-rides-deepdive-2026-07-22.md`.

## Core journey denominator

The minimum product loop is authentication → app shell → discovery → domain action →
authoritative readback → notification/activity → recovery. The denominator includes
Work, Business, Rides, Chat, visibility/consent, and Vision handoffs where the
existing product contract exposes them.

This is a reconciliation denominator, not a request to repeat every historical
test. Login, standalone Chat, core Work lifecycle, and basic notification delivery
remain baseline-only unless a declared retest trigger exists. New execution is
limited to Business/Rides integration and newly observed regressions.

## Reuse and non-duplication

Verified repair and production-completion plans are baseline inputs. New work must
target residual gaps, cross-surface integration, or stronger evidence. Existing
capability and runtime statuses remain owned by their canonical registries.

The master and each child plan must reference
`docs/plan-scope-control-standard.yaml` and explicitly declare reused plans,
baseline-only surfaces, residual scope, retest triggers, and the non-duplication
rule. A plan is not ready for goal pursuing if any of these fields is missing or
if a child silently expands the master scope.

## Business hardening questions

- Is availability effective for the requested timezone and exception state?
- Can two customers create an invalid capacity overlap?
- Are owner and customer actions distinct and backend-authorized?
- Do stale, duplicate, unavailable, and partial-failure paths recover safely?
- Does calendar focus open the correct booking or explicit filtered collection?
- Are booking audit and notification side-effects classified honestly?

## Rides hardening questions

- Are rides circle-scoped and privacy-safe for every viewer variant?
- Is capacity concurrency-safe under duplicate or competing joins?
- Are join, leave, start, complete, cancel, and stale transitions authoritative?
- Do owner and participant surfaces expose the same allowed-action contract?
- Are commute preferences separate from discovery and correctly privacy-bounded?
- Are Web, Vision, and future native evidence classes kept separate?

## Completion boundary

Passing unit tests or a normal success flow does not prove concurrency, privacy,
rollback, provider failure, process replay, or production operation behavior. Those
remain explicit evidence classes and are either captured or recorded as typed gaps.

## Verifier boundary

Implementation tasks must change their declared source/test paths. The runtime task
must change its declared runtime evidence artifact. Build, type-check, and static
audits cannot substitute for browser/runtime evidence.
