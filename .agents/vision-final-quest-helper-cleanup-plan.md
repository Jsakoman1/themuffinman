---
machine_kind: plan
machine_status: complete
machine_title: Vision Final Quest Helper Cleanup Plan
machine_goal: Remove or explicitly justify the last legacy quest/application helper
  and compatibility-overload surfaces after service shutdown.
---

# Vision Final Quest Helper Cleanup Plan

## Status

Completed.

## Goal

Remove or explicitly justify the last legacy quest/application helper and compatibility-overload surfaces after service shutdown.

## Slices

- [x] Re-audit `QuestReadService`, `QuestApplicationReadService`, mapper/entity conversions, and location quest compatibility overloads.
- [x] Delete dead helper paths and document any retained compatibility edge with a live caller inventory.
- [x] Refresh docs/generated artifacts/tests to the final retained surface.
- [x] Validate and audit the cleanup slice.

## Outcome

- Deleted `QuestReadService` as part of the compatibility-service shutdown because it had no remaining live runtime caller.
- Retained `QuestApplicationReadService` because it still has a live compatibility role for legacy vision-facing response shaping.
- Retained location compatibility overloads in `LocationSettingsService` and `LocationQuestPresentationService` because the remaining legacy vision quest creation/update/presentation paths still call them.
- Regenerated machine-operational artifacts after the cleanup so source-of-truth and validation inventories reflect the deleted compatibility layer.

## Retained Live Compatibility Edges

- `QuestApplicationReadService`
  - still exists in `main`
  - still serves legacy vision-facing read assembly
- `LocationSettingsService` vision-quest overloads
  - still used by remaining vision quest create/update paths
- `LocationQuestPresentationService` vision-quest overloads
  - still used by remaining legacy vision presentation/mapping paths

## Validation

- `make generate-agent-operating-model`
- `make generate-agent-artifacts`
- `cd apps/themuffinman && ./mvnw -q -Dtest=QuestWorkflowScenarioTest,VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,AgentOperatingModelValidationTest test`

## Completion Evidence

- Status: completed
- Deleted dead helper paths and documented retained live compatibility edges (`QuestApplicationReadService` and location overloads).
- Validation:
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
  - `cd apps/themuffinman && ./mvnw -q -Dtest=QuestWorkflowScenarioTest,VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,AgentOperatingModelValidationTest test`
