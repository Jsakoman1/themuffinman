# IMPL-WORKMARKET-USE-CASE-BOUNDARIES Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `high`
Master order: 6 of 82

## Backlog Item

Continue extracting workmarket mutations into narrow use-case services with one public orchestration path per workflow.

Source notes:
  Goal: make future Codex changes cheaper by reducing the number of service methods that must be read before safely changing quest or application behavior.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `cd apps/themuffinman && ./mvnw test -Dtest=QuestApplicationServiceTest,QuestWorkflowScenarioTest`

## Completion Evidence

- Status: complete
- Changed files: `QuestApplicationService`, new application workflow use cases, `QuestApplicationServiceTest`, `docs/domain-technical.md`, `docs/implementation-backlog.md`
- Validation evidence: `cd apps/themuffinman && ./mvnw test -Dtest=QuestApplicationServiceTest,QuestWorkflowScenarioTest` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `IMPL-WORKMARKET-USE-CASE-BOUNDARIES` from `docs/implementation-backlog.md`.
- Residual risk: full backend suite and TODO audit deferred to master closeout.
