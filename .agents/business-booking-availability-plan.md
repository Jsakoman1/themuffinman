---
machine_kind: plan
machine_status: complete
machine_title: Business Booking Availability Plan
machine_goal: Introduce recurring availability rules and exceptions plus a backend-owned
  availability computation surface so the system can derive bookable windows for offerings
  before real reservation writes begin.
---

# Business Booking Availability Plan

## Status

Complete.

## Goal

Introduce recurring availability rules and exceptions plus a backend-owned availability computation surface so the system can derive bookable windows for offerings before real reservation writes begin.

## Parent Master Plan

- Master plan: `.agents/business-booking-implementation-master-plan.md`

## Continuation Rule

- Once this phase reaches its closeout gates, execution should continue automatically into `.agents/business-booking-public-read-plan.md` without a routine stop.
- Do not pause after this phase only to ask whether to continue unless a real blocker or approval need appears.

## Scope

- Included: recurring availability rules, one-off availability exceptions, owner CRUD for schedule data, and a read-only availability computation service for bounded date ranges.
- Excluded: booking creation, booking status transitions, notification events, gallery media, and advanced staff/resource scheduling.

## Why This Phase Exists

- Booking writes should not exist before availability truth exists.
- The proposal intentionally chose rule-based scheduling instead of pre-generated slots, so this phase is where that core decision becomes real code.
- Availability must be derived by the backend, not improvised by frontend heuristics.

## Phase Analysis

### Problem Being Solved

Without an explicit scheduling layer:

- owner offerings have no real bookable meaning
- future booking validation would hardcode time checks into write services
- support for single-capacity and shared-capacity services would drift into controller logic

### Architectural Decisions

- Add `business_availability_rule` for recurring windows.
- Add `business_availability_exception` for closures and special windows.
- Keep default business-level schedule rows separate from optional offering-specific override rows.
- Add a dedicated `BusinessAvailabilityComputationService` and `BusinessAvailabilityReadService`.

### Main Risks

- Under-specifying time semantics early.
- Designing rule rows that cannot express group offerings or all-day offerings.
- Allowing owner APIs to write inconsistent overlapping or invalid windows.

### Mitigations

- Require one explicit timezone on `business_profile`.
- Store rule windows in local business time and derive API responses with timezone context.
- Validate window ranges, granularity, and replacement semantics in one dedicated service.

## Proposed Schema Work

Primary migration:

1. `V47__create_business_availability_tables.sql`

Schema outcomes:

- `business_availability_rule`
- `business_availability_exception`

Recommended early constraints:

- `end_time_local > start_time_local`
- `slot_granularity_minutes >= 5`
- `replacement_end_time_local > replacement_start_time_local` when replacement window is present

## Proposed Backend Package Work

Models:

- `business/model/BusinessAvailabilityRule.java`
- `business/model/BusinessAvailabilityException.java`
- `business/model/BusinessAvailabilityExceptionType.java`

Repositories:

- `business/repository/BusinessAvailabilityRuleRepository.java`
- `business/repository/BusinessAvailabilityExceptionRepository.java`

Services:

- `business/service/BusinessAvailabilityComputationService.java`
- `business/service/BusinessAvailabilityReadService.java`
- `business/service/BusinessAvailabilityValidationService.java`
- optional `business/service/BusinessAvailabilityPolicyService.java`

Controllers:

- `business/controller/BusinessAvailabilityController.java`

DTOs:

- owner rule CRUD DTOs
- owner exception CRUD DTOs
- public/internal availability window DTOs

## API Surface For This Phase

Owner CRUD:

- `GET /business/availability-rules/me`
- `POST /business/availability-rules/me`
- `PUT /business/availability-rules/{ruleId}/me`
- `DELETE /business/availability-rules/{ruleId}/me`
- `GET /business/availability-exceptions/me`
- `POST /business/availability-exceptions/me`
- `PUT /business/availability-exceptions/{exceptionId}/me`
- `DELETE /business/availability-exceptions/{exceptionId}/me`

Read-only derived availability:

- `GET /business/public/{slug}/availability`

At this phase the derived availability endpoint may exist for diagnostics or owner preview even if customer booking is not yet enabled.

## Implementation Slices

- [ ] Slice 1: implement recurring rule and exception entities, repositories, and validation helpers.
- [ ] Slice 2: add owner CRUD endpoints for rules and exceptions.
- [ ] Slice 3: implement bounded-range availability derivation that merges defaults, offering overrides, and exceptions.
- [ ] Slice 4: expose derived availability DTOs for one business and optional one offering.
- [ ] Slice 5: document rule semantics and validate representative schedule edge cases.

## Validation Plan

- Focused service tests for:
  - granularity validation
  - invalid windows
  - replacement exceptions
  - offering override precedence
- Repository/query tests if custom fetch or filtering methods become non-trivial
- Controller tests for owner authorization and basic query parameters

## Expected Docs and Artifacts

- `apps/themuffinman/src/main/java/com/themuffinman/app/business/README.md`
- `docs/business-logic.md`
- `docs/domain-technical.md`
- `docs/source-of-truth-inventory.md`

## Closeout Gates

- Derived availability is backend-owned and based on rules plus exceptions.
- Offering-specific schedule overrides do not require a separate scheduling model.
- The code path already understands future capacity-aware windows even before booking writes exist.
