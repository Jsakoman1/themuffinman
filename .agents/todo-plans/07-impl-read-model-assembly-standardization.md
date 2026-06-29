# IMPL-READ-MODEL-ASSEMBLY-STANDARDIZATION Plan

Source: `docs/implementation-backlog.md`
Group: `implementation`
Risk: `medium`
Master order: 7 of 82

## Backlog Item

Standardize one service-level DTO/read-model assembly path per viewer role for high-traffic surfaces such as dashboard, quest detail, application detail, and admin views.

Source notes:
  Goal: reduce duplicated mapper/action wiring and lower the risk of LazyInitializationException fixes missing sibling read paths.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `cd apps/themuffinman && ./mvnw test -Dtest=QuestApplicationServiceTest,QuestServiceTest,DashboardServiceTest,QuestWorkflowScenarioTest`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: `QuestApplicationViewAssembler`, `QuestApplicationService`, `QuestService`, `QuestApplicationServiceTest`, `QuestServiceTest`, `docs/domain-technical.md`, `docs/implementation-backlog.md`
- Validation evidence: `cd apps/themuffinman && ./mvnw test -Dtest=QuestApplicationServiceTest,QuestServiceTest,DashboardServiceTest,QuestWorkflowScenarioTest` passed; `ruby scripts/todo-audit.rb` passed.
- Backlog update: removed `IMPL-READ-MODEL-ASSEMBLY-STANDARDIZATION` from `docs/implementation-backlog.md`.
- Residual risk: frontend validations and full backend suite deferred to master closeout; no frontend code changed in this slice.
