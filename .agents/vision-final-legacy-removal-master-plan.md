---
machine_kind: master-plan
machine_status: complete
machine_title: Vision Final Legacy Removal Master Plan
machine_goal: Remove the remaining legacy `vision` work compatibility layer by migrating
  the last dashboard read-model island, then deleting compatibility services and their
  leftover helper surfaces only after all live callers are gone.
---

# Vision Final Legacy Removal Master Plan

## Status

Completed.

## Goal

Remove the remaining legacy `vision` work compatibility layer by migrating the last dashboard read-model island, then deleting compatibility services and their leftover helper surfaces only after all live callers are gone.

## Why This Plan Exists

- The remaining runtime callers of `QuestService` and `QuestApplicationService` in `main` code were expected to be concentrated in `DashboardReadQueryService`, but the dashboard legacy island was confirmed to be dead runtime code and removed directly.
- `VisionCreateQuestExecutionAdapter`, capability preview/search/discovery, and facade surfaces already read or mutate through workmarket ownership.
- The next safe deletion step is not “remove services now”, but “migrate the last live dashboard island first, then prove no caller remains, then delete”.

## Scope

- Included: dashboard legacy read-model migration, compatibility service caller elimination, legacy quest/application helper cleanup, docs/control-surface sync, and validation/audits.
- Excluded: deleting legacy DTO/model copies that still serve non-work capability surfaces until all real callers are proven gone.

## Current Remaining Legacy Surface

1. Legacy helper and compatibility-overload trail that had to be re-audited after caller removal
- [QuestApplicationReadService.java](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/QuestApplicationReadService.java)
- [LocationSettingsService.java](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationSettingsService.java)
- [LocationQuestPresentationService.java](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationQuestPresentationService.java)
- [LocationGeoService.java](/Users/jsakoman/Desktop/themuffinman/apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationGeoService.java)

## Child Plans

1. Dashboard legacy read-model migration
- Plan: `.agents/vision-dashboard-legacy-read-model-removal-plan.md`
- Role: move dashboard read/query assembly off legacy `vision.model.Quest` / `QuestApplication` and legacy quest/application compatibility services onto workmarket-owned read contracts.
- Exit criteria:
  - `DashboardReadQueryService` no longer depends on `QuestService` or `QuestApplicationService`.
  - dashboard read model and summary/section assembly consume workmarket-owned quest/application/news DTOs or an explicit workmarket-native dashboard read shape.
  - dashboard tests are rewritten to the new ownership boundary.
- Status: completed

2. Compatibility service shutdown
- Plan: `.agents/vision-quest-application-compatibility-service-shutdown-plan.md`
- Role: once dashboard no longer calls them, remove `QuestService` and `QuestApplicationService` runtime usage, then either delete or reduce them to zero-live-caller compatibility stubs scheduled for deletion.
- Exit criteria:
  - no `main` runtime caller remains for `QuestService` or `QuestApplicationService`, except an explicitly documented deferred edge if one exists.
  - transaction coverage/tests are updated away from the removed services or intentionally narrowed.
  - docs no longer describe those services as active runtime compatibility surfaces.
- Status: completed

3. Final quest legacy helper cleanup
- Plan: `.agents/vision-final-quest-helper-cleanup-plan.md`
- Role: audit and remove any now-dead legacy read helpers, mapper/entity conversions, and location compatibility overloads left behind after the service shutdown.
- Exit criteria:
  - each retained helper/conversion has a live caller inventory, or it is deleted.
  - `QuestReadService`, `QuestApplicationReadService`, and location compatibility overloads are either removed or justified as still live non-runtime compatibility edges.
  - generated docs and inventories match the final retained surface.
- Status: completed

4. Final closeout
- Role: compile, targeted regressions, generated artifact freshness, plan audits, and closeout evidence.
- Exit criteria:
  - all child plans are truly complete
  - validations are green
  - no plan is marked complete with open undeferred tasks
- Status: completed

## Required Validation Envelope

- `./mvnw -q -DskipTests compile`
- targeted dashboard, quest, vision conversation, and compatibility regression tests
- `make audit-generated-artifact-freshness`
- `make audit-plan-completion plan=<each-child-plan>`
- `make audit-plan-completion plan=.agents/vision-final-legacy-removal-master-plan.md`

## Non-Negotiable Completion Rules

- Do not mark any child plan or this master plan complete until the covered code is actually migrated or removed, tests pass or are explicitly skipped with reason, and the completion evidence matches reality.
- If dashboard migration reveals another hidden runtime caller, add it to the appropriate child plan before any service-deletion claim.
- If any helper or overload still has a live caller after the migration, keep it explicit and documented instead of deleting it speculatively.

## Closeout Result

- Removed the entire dead dashboard legacy read-model island.
- Removed the last `vision` quest/application compatibility services: `QuestService`, `QuestReadService`, and `QuestApplicationService`.
- Migrated the remaining scenario coverage to direct workmarket ownership.
- Kept only explicitly live compatibility helpers, with retained-caller reasoning recorded in the helper cleanup child plan.
- Regenerated machine-operational artifacts after the deletions so docs, inventories, and validation surfaces align with the actual runtime boundary.

## Closeout Validation

- `cd apps/themuffinman && ./mvnw -q -DskipTests compile`
- `make generate-agent-operating-model`
- `make generate-agent-artifacts`
- `cd apps/themuffinman && ./mvnw -q -Dtest=QuestUseCaseContractTest,QuestApplicationUseCaseContractTest,QuestWorkflowScenarioTest,VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,DashboardServiceTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest test`
- `make audit-generated-artifact-freshness`

## Completion Evidence

- Status: completed
- Child plan status: completed
- Removed the dead dashboard legacy island and the last quest/application compatibility-service runtime layer.
- Remaining legacy helpers were explicitly re-audited and retained only where live callers still exist.
- Validation:
  - `cd apps/themuffinman && ./mvnw -q -DskipTests compile`
  - `make generate-agent-operating-model`
  - `make generate-agent-artifacts`
  - `cd apps/themuffinman && ./mvnw -q -Dtest=QuestUseCaseContractTest,QuestApplicationUseCaseContractTest,QuestWorkflowScenarioTest,VisionQuestFacadeServiceTest,VisionQuestApplicationFacadeServiceTest,DashboardServiceTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest test`
  - `make audit-generated-artifact-freshness`
