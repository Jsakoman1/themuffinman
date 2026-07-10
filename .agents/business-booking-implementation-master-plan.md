---
machine_kind: master-plan
machine_status: complete
machine_title: Business Booking Implementation Master Plan
machine_goal: Implement the business booking backend as a modular service-booking
  domain built on top of the current `business_profile` root while reusing the repository's
  existing backend-centric layering patterns from `workmarket`, `identity`, and other
  module boundaries.
---

# Business Booking Implementation Master Plan

## Status

Complete.

## Goal

Implement the business booking backend as a modular service-booking domain built on top of the current `business_profile` root while reusing the repository's existing backend-centric layering patterns from `workmarket`, `identity`, and other module boundaries.

## Findings

- The current `business` module is only a profile capsule and does not yet contain offerings, scheduling, booking policy, or reservation workflows.
- The safest root split is:
  - `business_profile` as public identity root
  - `business_offering` as bookable service root
  - availability rules plus exceptions as scheduling truth
  - `business_booking` as reservation root
- The repository already has a good service-layering precedent in `workmarket`; the business booking implementation should reuse that structure rather than introducing one large all-purpose business service.
- A rule-based availability model is a better long-term base than pre-generated slots because it supports both single-capacity and shared-capacity businesses without immediately forcing a caching or calendar expansion design.
- Owner booking list and owner dashboard should later share one explicit owner schedule read-model so "what is happening today" does not drift across multiple read surfaces.
- Business operational defaults and thresholds should be centralized in typed config under `business/config` rather than spreading through future services as scattered `@Value` fields.
- Booking DTOs should be backend-prepared and carry `allowedActions`, `statusLabel`, and optional `blockingReason`.
- Capacity-aware booking needs explicit idempotency and concurrency guidance as part of the implementation program.
- Future `/vision` integration should consume business-owned capabilities instead of inventing a second runtime business model.
- Time semantics should be explicitly backend-owned: owner availability is entered in local business time, bookings persist absolute timestamps, DTOs return timezone context, and DST handling stays a backend rule.
- Booking history should survive offering edits or deactivation through archival-safe offering handling and booking snapshot fields.
- Booking read surfaces should start with pagination and filter contracts so owner/customer lists can grow without contract breakage.
- Future admin/ops support will likely need search hooks by customer, business, and date window, so the plan should preserve room for those queries.
- Future notification/event vocabulary should be named early even if delivery is deferred.
- Business booking should eventually gain docs-as-contract scenario coverage because its policy rules are too dense to rely on code intent alone.

## Implemented Snapshot

The repository now already contains the core business-booking implementation that this master plan originally aimed to deliver:

- `business_profile` now carries booking-facing identity fields such as `bookingEnabled` and timezone context.
- `business_offering` exists as the bookable service root with owner CRUD and booking-mode semantics.
- `business_booking_policy` exists as the owner policy row with typed operational defaults.
- Rule-based availability and exceptions are implemented as backend-owned schedule truth.
- Public booking reads exist for the business mini-site, active offerings, availability preview, and gallery images.
- Reservation writes exist for customer and owner flows, including confirm/reject/cancel/complete/no-show transitions.
- Booking DTOs are backend-prepared with allowed actions, labels, timezone context, and snapshot semantics.
- Owner schedule, calendar, dashboard, audit, and event hooks are present as separate read and integration surfaces.
- The business README and domain docs already describe the implemented backend capsule rather than only the proposal.

## Pre-Implementation Decisions Still To Lock

These are the main open decisions that should be confirmed before implementation begins, even though the plan already assumes a safe default direction:

1. Pending-capacity policy
- Decide whether `PENDING_CONFIRMATION` bookings reserve capacity immediately or only `CONFIRMED` bookings consume occupancy.
- Recommended default: reserve capacity immediately for `PENDING_CONFIRMATION` so owner approval does not oversell scarce slots.

2. Instant-vs-request workflow
- Decide whether `booking_mode = INSTANT` writes `CONFIRMED` immediately and bypasses owner approval.
- Locked decision: yes, `INSTANT` writes directly to `CONFIRMED`, while `REQUEST` writes `PENDING_CONFIRMATION`.

3. Owner-created bookings
- Decide whether owner-created reservations use the same `business_booking` lifecycle as customer bookings.
- Locked decision: yes, keep one booking table and one lifecycle, with `booking_source = OWNER_CREATED`.

4. Public route strategy
- Decide whether the richer public business page replaces `GET /business/profiles/{slug}` or is introduced as a new explicit route.
- Locked decision: keep the legacy slug route stable and add `GET /business/public/{slug}` as the booking-facing contract.

5. Media timing
- Decide whether gallery/media metadata is required before the first booking rollout or can remain in the hardening phase.
- Locked decision: keep gallery in the hardening phase unless a later implementation slice proves that launch quality or conversion needs require it earlier.

6. Booking DTO contract style
- Decide whether status meaning and next allowed actions are backend-owned or inferred in clients.
- Locked decision: booking DTOs are backend-owned and should expose `allowedActions`, `statusLabel`, and optional `blockingReason`.

7. Time semantics
- Decide whether timezone and DST handling are backend-owned or partly delegated to clients.
- Locked decision: owner availability rules are entered in local business time, bookings persist `starts_at` and `ends_at` as absolute timestamps, DTOs return timezone context, and DST handling is a backend rule.

8. Offering deletion semantics
- Decide whether removing an offering may erase historical booking meaning.
- Locked decision: historical bookings must retain readable offering context; offering removal should behave as deactivation/archival-safe lifecycle rather than destructive deletion of business meaning.

## Scope

- Included: the phased backend implementation path for schema, services, package layout, API surfaces, docs, and validation strategy needed to deliver business booking.
- Excluded: frontend implementation, payment flows, multi-staff assignment, external calendar sync, and broad marketplace search/ranking beyond current business directory scope.

## Child Plans

1. `.agents/business-booking-foundation-plan.md`
2. `.agents/business-booking-availability-plan.md`
3. `.agents/business-booking-public-read-plan.md`
4. `.agents/business-booking-workflow-plan.md`
5. `.agents/business-booking-hardening-and-integrations-plan.md`

## Execution Order

1. Foundation
- Reason: creates the stable business root extensions, service catalog, and policy defaults the rest of the booking model depends on.

2. Availability
- Reason: booking writes should not exist before recurring rules and exception semantics are real backend concepts.

3. Public read
- Reason: public business page and active offering contracts should be stabilized before customer reservation writes target them.

4. Workflow
- Reason: booking creation and lifecycle transitions depend on offerings, policy defaults, and derived availability already existing.

5. Hardening and integrations
- Reason: audit, dashboard, media, and event hooks should be added after the core workflow stabilizes so integrations do not distort the base domain too early.

## Execution Continuation Rule

- This master plan is intended for uninterrupted execution across all child phases.
- After one child plan reaches its closeout gates, execution should continue automatically into the next child plan in the declared order without pausing for routine confirmation.
- Do not stop between phases only to ask whether implementation should continue.
- Stop early only for a real blocker, conflicting user changes, required sandbox or external approval, destructive-risk approval needs, or a genuine scope change from the user.

## Why This Sequence Is Preferred

- It keeps schema growth aligned with actual domain boundaries.
- It prevents direct booking logic from leaking into profile services.
- It allows public and owner reads to stabilize before mutation complexity increases.
- It keeps advanced operational concerns until the base reservation workflow is already real and testable.

## Cross-Phase Dependency Map

### Foundation -> Availability

- `business_profile.timezone` and `booking_enabled` should exist before real schedule logic is trusted.
- `business_offering` must exist before offering-level schedule overrides exist.
- `business_booking_policy` provides the defaults that later availability and booking logic will consume.

### Availability -> Public Read

- Public availability windows should come from backend derivation, not raw rows.
- Public booking-facing surfaces need schedule semantics to be stable before they become shared contracts.

### Public Read -> Workflow

- Booking creation should target a stable public business and offering surface.
- Customer-facing booking confirmation and validation messages will depend on the same read-model contract.

### Workflow -> Hardening

- Audit/event/media/dashboard work needs the final booking root and lifecycle transitions to already exist.
- Domain events are easier to wire cleanly when the final transition boundaries are explicit.

### Foundation -> Workflow

- Typed business operational config should exist before workflow services accumulate scattered defaults for lead time, cancellation, list sizing, and dashboard thresholds.

### Workflow -> Future Vision Integration

- If workflow and booking DTO boundaries stay canonical inside `business`, future `/vision` integration can publish business capabilities without a compatibility-only business model.

## Implementation Risks Across The Whole Program

### Risk 1: one oversized business service

Why it matters:

- It would repeat the kind of service bloat the repository has already been working to avoid.

Program-level mitigation:

- Keep child plans aligned with `*UseCase`, `*PolicyService`, `*ReadService`, and `*PrimitiveService` layering from the start.

### Risk 2: booking coupled too tightly to public profile rows

Why it matters:

- It would block modular growth for capacity rules, public page evolution, and future staff/resource support.

Program-level mitigation:

- Make `business_offering` the first true bookable root and keep booking rules off `business_profile`.

### Risk 3: availability semantics drift between public reads and booking writes

Why it matters:

- Customers could see slots that the write path later rejects, or owners could configure schedules that render inconsistently.

Program-level mitigation:

- Use one availability derivation service as the shared truth for both public preview and booking validation.

### Risk 4: duplicate or over-capacity bookings under retries or parallel writes

Why it matters:

- Capacity booking often looks correct in sequential local flows and fails under retries or parallel traffic.

Program-level mitigation:

- Put explicit workflow slices around occupancy check strategy, transaction boundary, idempotency guidance, and parallel booking race coverage.

### Risk 5: historical bookings lose meaning after offering edits

Why it matters:

- If offering title, duration, or price change later, past bookings can become misleading unless they keep stable historical context.

Program-level mitigation:

- Keep the plan ready for booking snapshot fields and archival-safe offering behavior instead of assuming live offering rows are the only source of truth.

### Risk 6: future integrations distort the core model

Why it matters:

- Notifications, gallery, or `/vision` hooks can push premature abstractions into the reservation core.

Program-level mitigation:

- Delay those concerns to the hardening/integration phase and keep explicit domain events at the boundary.

## Migration Roadmap

Expected migration sequence for the full program:

1. `V44__extend_business_profile_for_booking.sql`
2. `V45__create_business_offering_table.sql`
3. `V46__create_business_booking_policy_table.sql`
4. `V47__create_business_availability_tables.sql`
5. `V48__create_business_booking_table.sql`
6. `V49__create_business_booking_audit_event_table.sql`
7. `V50__create_business_gallery_image_table.sql`
8. `V51__tighten_business_booking_constraints.sql`

Sequence note:

- `V49` and `V50` may swap if audit is deferred and gallery arrives first, but the default recommendation is to keep audit ahead of media because it is closer to core operational support.

## Program-Level Validation Strategy

### Phase-focused validation

- Each child phase should close with targeted service/controller tests plus any migration/bootstrap coverage directly affected by that slice.

### Broader validation

- Workflow phase should run broader `./mvnw test` coverage because that is the first point where the booking domain becomes operationally significant.

### Concurrency validation

- Workflow phase should include focused coverage for duplicate create attempts, limited-capacity parallel booking races, and transaction-safe occupancy handling.

### Documentation validation

- Update `docs/business-logic.md`, `docs/domain-technical.md`, `docs/source-of-truth-inventory.md`, and `business/README.md` when a phase changes domain meaning or ownership.
- If machine-readable agent-operating artifacts or protected doc phrases are touched, run the corresponding validation test and regeneration path.

### Docs-as-contract validation

- Once the workflow phase is implemented, add business-booking policy scenarios to the docs-as-contract and regression-scenario layers so booking rules, cancellation windows, and capacity semantics are proven rather than only described.

## Backend-Centric Guardrails

- Keep booking, availability, capacity, cancellation, and transition rules in backend services rather than client flows.
- Prefer backend-prepared DTOs with labels, flags, timezone context, and allowed actions over frontend-derived workflow meaning.
- Do not let Vue-specific convenience logic become the only place that knows whether an action is allowed.
- Treat web, iPhone, and future `/vision` clients as equal consumers of the same backend contract; avoid introducing web-only business semantics.
- If a client needs to decide something important, first ask whether the backend should return that answer directly.
- Prefer one canonical read-model path per viewer or use case instead of multiple clients rebuilding the same domain meaning from raw rows.
- Keep timezone, DST, occupancy, and availability semantics server-owned so mobile clients do not need parallel scheduling logic.
- Resist pushing retry, concurrency, or capacity assumptions into the frontend; the write path must remain safe when called by any client.

## Implementation Readiness Checklist

- The child plan sequence is stable and does not depend on the deleted proposal artifacts.
- The migration order is defined and coherent with the current Flyway numbering.
- The previously open behavior and routing decisions are now locked in this master plan.
- The `INSTANT` vs `REQUEST` starting-status decision is locked: `INSTANT -> CONFIRMED`, `REQUEST -> PENDING_CONFIRMATION`.
- Owner-created bookings are locked to the same booking table and lifecycle with `booking_source = OWNER_CREATED`.
- Public booking-facing reads are locked to a dedicated `GET /business/public/{slug}` family while legacy profile slug reads stay stable.
- Gallery/media stays in the hardening phase by default.
- Booking DTO contract ownership is locked to the backend via `allowedActions`, `statusLabel`, and optional `blockingReason`.
- Time semantics are locked as backend-owned with local business availability input, absolute persisted booking timestamps, returned timezone context, and backend DST handling.
- Historical booking meaning is protected through archival-safe offering behavior and a plan-ready booking snapshot direction.
- The implementation plan explicitly includes typed `business/config`, owner schedule read-model unification, idempotency guidance, concurrency handling, and future `/vision` capability publishing expectations.
- The implementation also has explicit backend-centric guardrails so future web and iPhone clients consume one backend-owned business contract.
- The implementation can now start directly from the foundation phase without another design pass.

## Closeout Requirements For The Whole Program

- `business` owns its runtime profile, offering, availability, booking, and booking-operation boundaries directly.
- Scheduling and booking rules are backend-owned and not reconstructed in frontend code.
- Public business page, customer booking reads, and owner management reads are separate DTO surfaces.
- Booking transitions are explicit and validated by service-layer workflows.
- Audit and event hooks are modular and do not require core services to call downstream side effects directly.
- Living docs reflect the real implemented state rather than the proposal only.
- The master plan is not complete until every child phase in the declared order is either implemented and validated or explicitly blocked by a real blocking condition.

## Completion Evidence

- Status: complete
- Child plan status:
  - foundation complete
  - availability complete
  - public read complete
  - workflow complete
  - hardening and integrations complete
- Validation:
  - `./mvnw -q -Dtest=BusinessProfileServiceTest,BusinessOfferingServiceTest,BusinessBookingPolicyServiceTest,BusinessAvailabilityComputationServiceTest,BusinessPublicReadServiceTest,BusinessCreateBookingUseCaseTest,BusinessBookingValidationServiceTest,BusinessCancelBookingUseCaseTest,BusinessBookingReadServiceTest,BusinessOwnerScheduleReadServiceTest,BusinessOwnerCalendarReadServiceTest,BusinessGalleryServiceTest,BusinessBookingControllerTest test`
  - `make control-start`
  - `ruby scripts/audits/audit-generated-artifact-freshness.rb`
- Doc delta summary:
  - The business capsule already owns the profile, offering, availability, booking, gallery, dashboard, and audit boundaries documented here.
  - The plan now records the shipped implementation state instead of only the initial rollout proposal.

## Expected Outcome

At the end of this master plan, `business` should no longer be only a static profile directory. It should become a modular booking domain that supports:

- one public business identity per owner
- many offerings per business
- recurring and exceptional availability
- capacity-aware booking
- owner-managed booking lifecycle
- future-safe audit, dashboard, and integration boundaries

without abandoning the repository's current backend-centric architecture.
