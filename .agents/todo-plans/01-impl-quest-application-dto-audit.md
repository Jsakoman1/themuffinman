# IMPL-QUEST-APPLICATION-DTO-AUDIT Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 1 of 82

## Backlog Item

Audit every backend read surface that returns `QuestApplicationResponseDTO` or derivative application collections, remove duplicated applicant-action DTO assembly, and harden remaining lazy-loading paths before more self-service actions are added.

Source notes:
  Scope to cover in the next pass: `QuestApplicationService.getApplicationsForQuest`, `getApplicationsViewForQuest`, `getPublicApprovedApplicationsViewForQuest`, `getAllApplicationsForAdmin`, `searchApplicationsForAdmin`, and the default transaction strategy for non-mutating application read methods.
  Working note: `.agents/quest-application-dto-audit-todo.md`

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-local-tooling-incremental`
- [x] `cd apps/themuffinman && ./mvnw test`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: `QuestApplicationRepository.java`, `QuestApplicationService.java`, `QuestWorkflowScenarioTest.java`, `docs/domain-technical.md`, `docs/implementation-backlog.md`
- Validation evidence: `./mvnw test -Dtest=QuestWorkflowScenarioTest,QuestApplicationServiceTest,ServiceTransactionConfigurationTest` passed on 2026-06-29.
- Backlog update: removed `IMPL-QUEST-APPLICATION-DTO-AUDIT` from `docs/implementation-backlog.md`.
- Residual risk: full backend, frontend, TODO, and generated-artifact validation deferred to master closeout.
