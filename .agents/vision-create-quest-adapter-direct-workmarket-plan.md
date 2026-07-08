---
machine_kind: plan
machine_status: complete
machine_title: Vision Create Quest Adapter Direct Workmarket Plan
machine_goal: Remove the remaining runtime dependency from VisionCreateQuestExecutionAdapter to the legacy QuestService compatibility adapter.
---

# Vision Create Quest Adapter Direct Workmarket Plan

## Status

Complete.

## Goal

Remove the remaining runtime dependency from `VisionCreateQuestExecutionAdapter` to the legacy `QuestService` compatibility adapter.

## Scope

- Included: `VisionCreateQuestExecutionAdapter`, its tests and docs, and a caller inventory refresh.
- Excluded: dashboard legacy read-model migration.

## Slices

- [x] Switch create-quest execution to `WorkmarketQuestService` plus explicit compatibility mapping.
- [x] Preserve current `VisionExecutionResult` quest payload shape for downstream vision callers.
- [x] Validate and record the reduced runtime caller inventory.

## Validation

- `./mvnw -q -DskipTests compile`
- `./mvnw -q -Dtest=VisionExecutionServiceTest,VisionConversationServiceTest,QuestServiceTest,QuestApplicationServiceTest test`

## Completion Evidence

- Status: complete
- `VisionCreateQuestExecutionAdapter` now depends on `WorkmarketQuestService` and `WorkmarketQuestMgr` directly instead of routing create-quest execution through `QuestService`.
- The adapter still returns a legacy `vision.model.Quest` payload so `VisionExecutionResult` and downstream review/conversation flows stay compatible.
- Main-code caller inventory on `2026-07-07` shows the remaining runtime callers of `QuestService` and `QuestApplicationService` concentrated in `DashboardReadQueryService`.
- Validation passed on `2026-07-07`:
  - `./mvnw -q -DskipTests compile`
  - `./mvnw -q -Dtest=VisionExecutionServiceTest,VisionConversationServiceTest,QuestServiceTest,QuestApplicationServiceTest test`
