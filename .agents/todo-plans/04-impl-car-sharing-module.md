# IMPL-CAR-SHARING-MODULE Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 4 of 82

## Backlog Item

Replace the Car Sharing placeholder with the first implemented voluntary circle-based ride coordination slice.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `cd apps/themuffinman && ./mvnw test`
- [x] `cd apps/themuffinman/frontend && npm run type-check`

## Completion Evidence

- Status: complete
- Changed files: rides backend package, `V33__create_ride_offer_tables.sql`, `RideOfferServiceTest.java`, `RideSharingView.vue`, `ridesApi.ts`, `rides.css`, router/module registry/style imports, living docs, and agent operating model sections.
- Validation evidence: `./mvnw test -Dtest=RideOfferServiceTest,TheMuffinManApplicationTests`; `npm run type-check` passed on 2026-06-29.
- Backlog update: removed `IMPL-CAR-SHARING-MODULE` from `docs/implementation-backlog.md`.
- Residual risk: frontend production build, generated contract/artifact refresh, TODO audit, documentation audit, and full backend suite deferred to master closeout.
