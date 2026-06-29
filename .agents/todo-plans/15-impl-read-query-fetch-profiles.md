# IMPL-READ-QUERY-FETCH-PROFILES Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `medium`
Master order: 15 of 82

## Backlog Item

Introduce named repository fetch profiles or query methods for each read surface instead of ad hoc entity loading inside services.

Source notes:
  Goal: reduce LazyInitializationException risk and make DTO read paths predictable.

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
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: `QuestRepository.java`, `QuestApplicationRepository.java`, `ThingListingRepository.java`, `ThingBorrowRequestRepository.java`, workmarket and thing-sharing read services/use cases, affected backend tests, `RepositoryFetchProfileContractTest.java`, `docs/business-logic.md`, `docs/domain-technical.md`, `docs/implementation-backlog.md`
- Validation evidence: targeted backend fetch-profile/read-path suite passed with 81 tests; full `cd apps/themuffinman && ./mvnw test` passed with 261 tests; `cd apps/themuffinman/frontend && npm run type-check` passed; `cd apps/themuffinman/frontend && npm run build` passed; `make audit-documentation` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `IMPL-READ-QUERY-FETCH-PROFILES` from `docs/implementation-backlog.md`.
- Residual risk: compatibility delegates remain for older repository method names, but production read paths now use named fetch profiles and the contract test protects the current profile set.
