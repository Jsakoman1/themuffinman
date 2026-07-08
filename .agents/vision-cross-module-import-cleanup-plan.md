---
machine_kind: plan
machine_status: complete
machine_title: Vision Cross Module Import Cleanup Plan
machine_goal: Remove the remaining direct work-domain vision imports from non-vision packages once the quest compatibility services are reduced.
---

# Vision Cross Module Import Cleanup Plan

## Status

Complete.

## Goal

Remove the remaining direct work-domain vision imports from non-vision packages once the quest compatibility services are reduced.

## Scope

- Included: `agent`, `location`, and any remaining non-vision work-domain imports that can safely move to workmarket-owned contracts or overloaded helpers.
- Excluded: capability-owned prompt, conversation, and semantic vision surfaces.

## Slices

- [x] Rewire admin synthetic quest execution and other non-vision work-domain callers away from `vision` work contracts where workmarket ownership now exists.
- [x] Tighten `location` and related helper signatures so work-domain overloads default to workmarket ownership where safe.
- [x] Sync docs/control surfaces to the cleaned import boundary.
- [x] Validate and audit the plan after the quest compatibility slice lands.

## Validation

- `./mvnw -q -DskipTests compile`
- `./mvnw -q -Dtest=AdminAgentExecutionServiceTest,LocationSettingsServiceTest,LocationLookupServiceTest,AgentOperatingModelValidationTest test`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-cross-module-import-cleanup-plan.md`

## Completion Evidence

- Status: complete
- `AdminAgentExecutionService` now writes synthetic quests through workmarket-owned quest request/service contracts instead of the legacy `vision` quest facade contracts.
- `LocationLookupService` now uses `WorkmarketQuestRepository` for quest-location metrics, and the location helper overloads now default to workmarket-owned quest contracts outside the remaining legacy `vision` compatibility boundary.
- The workmarket capsule README and source-of-truth inventory now record the cleaned non-vision quest import boundary for `agent` and `location`.
- Validation passed on `2026-07-07`:
  - `./mvnw -q -DskipTests compile`
  - `./mvnw -q -Dtest=QuestServiceTest,QuestApplicationServiceTest,QuestWorkflowScenarioTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,AdminAgentExecutionServiceTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest,LocationLookupServiceTest,WorkmarketVisionContractBridgeTest test`
  - `npm --prefix apps/themuffinman/frontend run generate:contracts`
  - `make generate-agent-operating-model generate-agent-artifacts`
  - `make control-refresh-full`
