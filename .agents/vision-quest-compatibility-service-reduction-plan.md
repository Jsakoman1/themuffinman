---
machine_kind: plan
machine_status: complete
machine_title: Vision Quest Compatibility Service Reduction Plan
machine_goal: Reduce the remaining legacy quest, quest-read, and quest-application services to compatibility adapters over workmarket-owned services.
---

# Vision Quest Compatibility Service Reduction Plan

## Status

Complete.

## Goal

Reduce the remaining legacy quest, quest-read, and quest-application services to compatibility adapters over workmarket-owned services.

## Scope

- Included: `QuestReadService`, `QuestService`, `QuestApplicationService`, related tests, and supporting compatibility bridge updates.
- Excluded: preview/discovery caller rewrites beyond what is required to keep them green against the new adapter services.

## Slices

- [x] Convert `QuestReadService` to read through workmarket-owned quest and application read services.
- [x] Convert `QuestService` and `QuestApplicationService` to compatibility adapters over workmarket-owned services.
- [x] Update quest-related tests and dependent capability tests to the new adapter behavior.
- [x] Validate and close the plan only after audit passes.

## Validation

- `./mvnw -q -DskipTests compile`
- `./mvnw -q -Dtest=QuestServiceTest,QuestApplicationServiceTest,QuestWorkflowScenarioTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,AdminAgentExecutionServiceTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest test`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-quest-compatibility-service-reduction-plan.md`

## Completion Evidence

- Status: complete
- `QuestReadService`, `QuestService`, and `QuestApplicationService` now act as compatibility adapters over workmarket-owned quest and application services instead of keeping parallel quest workflow logic in `vision`.
- Adapter tests were rewritten around delegation and compatibility mapping so the legacy `vision` quest facade is only validating adapter behavior while workflow behavior remains covered through the existing scenario tests.
- The compatibility bridge now maps quest/application response containers explicitly for time-heavy DTOs, and the legacy services clear the persistence context after workmarket mutations so same-transaction `vision` reads do not observe stale entity state.
- Validation passed on `2026-07-07`:
  - `./mvnw -q -DskipTests compile`
  - `./mvnw -q -Dtest=QuestServiceTest,QuestApplicationServiceTest,QuestWorkflowScenarioTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,AdminAgentExecutionServiceTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest,LocationLookupServiceTest,WorkmarketVisionContractBridgeTest test`
