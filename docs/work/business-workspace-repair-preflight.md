# Business Workspace Repair Preflight

Status: prepared for strict serial execution. This document is planning evidence, not implementation completion.

Hardening confirmation: the strict execution inventory started on 2026-07-29 with
`harden-business-repair-scope`; no Business application code was changed before this
preflight boundary was reviewed.

## Product decision locked for this program

`/business/*` is an owner workspace for exactly one owned `businessProfileId`.
The selected id is carried in the URL query and is the authoritative client context.
`sessionStorage` may mirror the selection for existing API helpers, but it must never
override the URL. The aggregate, cross-module schedule remains `/calendar`; the owner
workspace does not introduce a competing “All businesses” calendar.

Customer behavior stays separate:

- `/business/find` and `/business/public/:slug` are discovery and booking surfaces.
- `/business/my-bookings` is the customer's appointment history and action surface.
- An owner can also be a customer, but owner and customer permissions continue to come
  from backend booking presentation rather than the browser.

## Confirmed current-state defects

1. `BusinessOwnerPage` maps its Calendar tab to `BusinessAvailabilityView`, so
   `/business/calendar` is an availability-rules editor instead of a calendar.
2. The owner Overview tab maps to `BusinessOverviewView`, which is the list/picker of
   businesses rather than the selected-business operational dashboard.
3. `BusinessOwnerCalendarReadService.getMyCalendar(...)` chooses a profile through
   `findByOwnerId` and reads all owner bookings. Its controller accepts no profile id.
4. `BusinessOwnerDashboardReadService.getMyDashboard(...)` and its controller have
   the same owner-only shape, so dashboard counts cannot follow the selected profile.
5. `BusinessWorkspaceContextService` already supports selected and aggregate profile
   reads. It is the reference ownership rule, but the owner dashboard/calendar reads
   do not yet reuse that rule.
6. `BusinessServiceSchemaView` contains template selection and override inputs that do
   not participate in `saveOfferingRules`; they look persistent but are local-only.
7. Availability, exceptions, profile policy, resources, and service setup are spread
   across routes that do not align with the owner tab model.

## Atomic execution analysis

### 1. Hardening scope

Required outcome: create this preflight and confirm no application code changes occur
before the active task is verifier-started.

Exact boundaries:

- Preserve the public booking request, quote, booking-preview, and availability APIs.
- Preserve global `/calendar` aggregation and source filters.
- Do not alter booking status transitions or client-generated permission logic.

Leaf validation: `make audit-plan-coverage`.

### 2. Selected-business dashboard contract

Required outcome: `GET /business/dashboard/me` receives an owned `businessProfileId`
and returns metrics only for that profile. A missing profile id is not silently mapped
to the first profile; the owner workspace always supplies one.

Likely implementation owners:

- `BusinessOwnerDashboardController`
- `BusinessOwnerDashboardReadService`
- the shared schedule-summary read dependency, if it needs a profile-aware method
- `BusinessOwnerDashboardReadServiceTest`

Required regression cases:

- one owner with two profiles receives different counts per selected profile;
- a profile not owned by the viewer is rejected;
- no-profile owner gets the existing recoverable setup state;
- dashboard profile identity matches the requested profile id.

### 3. Owner navigation and overview surface

Required outcome: Business tab labels map to real owner tasks:

- Overview: selected-business dashboard;
- Calendar: booking schedule;
- Bookings: owner booking queue;
- Services: service catalog/editor entry;
- Settings: business identity, policy, availability, exceptions, gallery, and resources.

The business picker remains in the shell/owner header and in the left rail as a direct
firm switcher. It is not repeated inside Overview.

Required frontend invariants:

- URL change reloads the selected view and clears stale selections;
- child links retain `businessId`;
- a user with no businesses sees one setup state and a create action;
- routing `/business` remains the chooser/list entry, while `/business/profile?businessId=X`
  is the selected owner workspace.

### 4. Selected-business owner calendar contract

Required outcome: `GET /business/bookings/owner/calendar` accepts the selected profile
id, validates ownership, filters bookings at repository/query level, and returns the
selected profile timezone. It must not use `findByOwnerId` as a proxy for active context.

Required regression cases:

- two profiles owned by one user return only selected-profile bookings;
- unknown or another owner's profile id is rejected;
- local-day grouping uses the selected profile timezone;
- booking `allowedActions` remain backend-enriched;
- a calendar range still enforces existing limits.

### 5. Owner calendar surface

Required outcome: a new owner calendar view consumes the owner projection and provides
day, week, and month. The visual surface must be a booking schedule, not availability
configuration.

Interaction model:

- default: week time grid;
- controls: previous, Today, next, day/week/month;
- event selection: shared preview panel showing customer, service, time, status and
  backend-provided allowed actions;
- details/actions: use the existing booking mutation endpoints and refresh the current
  projection after success;
- empty calendar: still render its date/time structure with a setup hint.

Browser evidence must cover selected business A versus B, all three modes, a selected
booking, and the mobile sheet. The view may share presentation primitives with
`CalendarPage`, but must not change global Calendar behavior in this slice.

### 6. Configuration information architecture

Required outcome: no owner configuration control looks saved unless it is persisted.

Target destinations:

- Services: service list, create/edit service, and one focused configuration flow;
- Settings / Business: profile, public visibility/contact/gallery, booking policy,
  recurring availability, exceptions, and resources;
- Calendar: links to availability only as a setup/recovery action.

Decision rules:

- remove local-only template controls unless their values are applied through real
  offering mutations and backend-tested;
- do not add frontend-derived pricing, capacity, slot, or booking rules;
- keep availability exception precedence in the existing backend computation service;
- retain direct deep links, but redirect legacy routes into the selected owner context.

### 7. Closeout

Update `docs/business-logic.md` first, then `docs/domain-technical.md`, with the final
owner/customer distinction, selected-business rule, calendar scope, and configuration
destinations. Record any deferred product work in `docs/implementation-backlog.md`.

Required final validation:

- targeted JUnit tests for dashboard and calendar profile context;
- `./mvnw test` after backend changes;
- `npm run type-check`, `npm run build`, and web-surface validation;
- local browser evidence via `make dev` / `make dev-stop`;
- verifier-driven closeout for every child and the master.

## Execution gates

1. Start only `harden-business-repair-scope` first.
2. Do not start a later task while the inventory marks an earlier item pending or in
   progress.
3. Treat new API response fields as generated-contract changes and run the contract
   freshness check as part of frontend build.
4. If the existing owner schedule-summary service cannot be safely made profile-aware
   without changing unrelated dashboard semantics, stop after the dashboard-contract
   task and split that dependency into a new atomic child plan.
5. Do not claim visual completion from builds; capture the declared browser evidence.
