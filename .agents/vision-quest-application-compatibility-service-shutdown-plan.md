---
machine_kind: plan
machine_status: complete
machine_title: Vision Quest Application Compatibility Service Shutdown Plan
machine_goal: Remove the remaining runtime need for `QuestService` and `QuestApplicationService`
  after dashboard migration.
---

# Vision Quest Application Compatibility Service Shutdown Plan

## Status

Completed.

## Goal

Remove the remaining runtime need for `QuestService` and `QuestApplicationService` after dashboard migration.

## Slices

- [x] Refresh runtime caller inventory for `QuestService` and `QuestApplicationService` after the dashboard migration lands.
- [x] Remove or reduce remaining runtime call paths.
- [x] Update transaction coverage/tests and docs to the final retained surface.
- [x] Validate that no undeferred runtime caller remains.

## Outcome

- After the dashboard dead-code removal, `QuestService`, `QuestReadService`, and `QuestApplicationService` had no remaining `main` runtime callers.
- Deleted `QuestService`, `QuestReadService`, and `QuestApplicationService` instead of keeping zero-caller compatibility stubs.
- Removed the obsolete unit tests for those deleted adapters.
- Migrated `QuestWorkflowScenarioTest` to direct workmarket ownership (`WorkmarketQuestService`, `WorkmarketQuestApplicationService`, `WorkmarketQuestReadService`, and workmarket repositories).
- Updated `ServiceTransactionConfigurationTest` to cover `WorkmarketQuestApplicationReadService` instead of the deleted compatibility class.
- Updated workmarket and control-surface docs to stop describing the deleted services as live runtime boundaries.

## Validation

- `cd apps/themuffinman && ./mvnw -q -DskipTests compile`
- `cd apps/themuffinman && ./mvnw -q -Dtest=QuestUseCaseContractTest,QuestApplicationUseCaseContractTest,QuestWorkflowScenarioTest,VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,ServiceTransactionConfigurationTest test`

## Completion Evidence

- Status: completed
- Deleted `QuestService`, `QuestReadService`, and `QuestApplicationService` after proving they had no remaining `main` runtime callers.
- Validation:
  - `cd apps/themuffinman && ./mvnw -q -DskipTests compile`
  - `cd apps/themuffinman && ./mvnw -q -Dtest=QuestUseCaseContractTest,QuestApplicationUseCaseContractTest,QuestWorkflowScenarioTest,VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,ServiceTransactionConfigurationTest test`
