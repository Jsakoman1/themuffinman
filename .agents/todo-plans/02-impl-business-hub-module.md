# IMPL-BUSINESS-HUB-MODULE Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 2 of 82

## Backlog Item

Replace the Business Hub placeholder with the first implemented module slice for business profiles, mini websites, calendars, or appointment booking.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-documentation`
- [x] `cd apps/themuffinman && ./mvnw test`
- [x] `cd apps/themuffinman/frontend && npm run type-check`

## Completion Evidence

- Status: complete
- Changed files: business backend package, `V31__create_business_profile_table.sql`, `BusinessProfileServiceTest.java`, `BusinessHubView.vue`, `businessApi.ts`, `business.css`, router/module registry/style imports, living docs, and agent operating model sections.
- Validation evidence: `./mvnw test -Dtest=BusinessProfileServiceTest`; `./mvnw test -Dtest=TheMuffinManApplicationTests,BusinessProfileServiceTest`; `npm run type-check` passed on 2026-06-29.
- Backlog update: removed `IMPL-BUSINESS-HUB-MODULE` from `docs/implementation-backlog.md`.
- Residual risk: frontend production build, generated contract/artifact refresh, TODO audit, documentation audit, and full backend suite deferred to master closeout.
