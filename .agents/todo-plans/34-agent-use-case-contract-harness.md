# AGENT-USE-CASE-CONTRACT-HARNESS Plan

Source: `docs/agent-improvement-backlog.md`
Group: `agent-improvement`
Risk: `medium`
Master order: 34 of 82

## Backlog Item

Standardize use-case workflow contract harness coverage across more mutation surfaces.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-agent-safety`
- [x] `cd apps/themuffinman && ./mvnw test`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: added `QuestApplicationUseCaseContractTest` for applicant-side apply/update/withdraw use cases, registered it in source-of-truth/documentation coverage/service workflow inventory, and updated operating docs/current state.
- Validation evidence: `./mvnw test -Dtest=QuestApplicationUseCaseContractTest` passed with 4 tests; `make audit-agent-safety` passed; `cd apps/themuffinman && ./mvnw test` passed with 273 tests; `cd apps/themuffinman/frontend && npm run type-check` passed; `cd apps/themuffinman/frontend && npm run build` passed; `ruby scripts/todo-audit.rb` passed with 26 open backlog items remaining.
- Backlog update: removed `AGENT-USE-CASE-CONTRACT-HARNESS`.
- Residual risk: contract harness now covers applicant-side application use cases; owner/admin application use cases remain covered through service-level tests and can be split later if needed by a narrower backlog item.
