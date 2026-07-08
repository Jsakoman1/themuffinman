---
machine_kind: plan
machine_status: complete
machine_title: Business Booking Operational Contract Plan
machine_goal: Centralize booking operational defaults and formalize the supporting contract around search hooks, docs-as-contract, and workflow policy.
---

# Business Booking Operational Contract Plan

## Status

Complete.

## Goal

Centralize booking operational defaults and formalize the supporting contract around search hooks, docs-as-contract, and workflow policy.

## Parent Master Plan

- `.agents/business-booking-improvement-master-plan.md`

## Priority

- Should-have

## Scope

- Included: typed config consolidation, default lead time and cancellation windows, max advance horizon, list page size defaults, searchability hooks, and workflow documentation scenarios.
- Excluded: frontend copy work and any new public routes that do not improve backend clarity.

## Slice Notes

- Keep operational knobs in one typed config area.
- Preserve search hooks for customer, business slug, and date window support.
- Add docs-as-contract and regression scenarios for the policy-heavy booking paths.

## Validation

- Config binding tests.
- Search hook query tests.
- Docs-as-contract scenario coverage.
