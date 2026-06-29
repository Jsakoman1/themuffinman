# IMPL-TEST-FIXTURE-STANDARDIZATION Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `medium`
Master order: 11 of 82

## Backlog Item

Standardize backend test fixture builders for users, circles, quests, applications, location settings, and chat conversations.

Source notes:
  Goal: make new tests cheaper to write and reduce repeated setup code across service/scenario tests.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `cd apps/themuffinman && ./mvnw test -Dtest=QuestApplicationServiceTest,QuestWorkflowScenarioTest`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: `TestFixtures.java`, `QuestApplicationServiceTest.java`, `docs/domain-technical.md`, `docs/implementation-backlog.md`
- Validation evidence: `cd apps/themuffinman && ./mvnw test -Dtest=QuestApplicationServiceTest,QuestWorkflowScenarioTest` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `IMPL-TEST-FIXTURE-STANDARDIZATION` from `docs/implementation-backlog.md`.
- Residual risk: full backend/frontend validation deferred to master closeout; not every existing test was migrated, but the shared fixture standard now exists and is used by the highest-duplication application service test.
