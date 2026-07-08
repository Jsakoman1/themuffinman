---
machine_kind: master-plan
machine_status: complete
machine_title: Vision Workmarket Final Separation Master Plan
machine_goal: Finish the remaining compatibility cuts so work marketplace business and read ownership live in workmarket while vision keeps only capability-specific surfaces and explicit adapters.
---

# Vision Workmarket Final Separation Master Plan

## Status

Complete.

## Goal

Finish the remaining compatibility cuts so work marketplace business and read ownership live in workmarket while vision keeps only capability-specific surfaces and explicit adapters.

## Scope

- Included: legacy quest/dashboard/application service reduction, remaining non-vision import cleanup in `agent` and `location`, docs/control-surface sync, generated artifacts, and plan audits.
- Excluded: deleting all legacy vision DTO/model copies in one unsafe pass when active capability consumers still depend on them.

## Child Plans

1. Quest compatibility service reduction
- Plan: `.agents/vision-quest-compatibility-service-reduction-plan.md`
- Role: reduce `QuestReadService`, `QuestService`, and `QuestApplicationService` to compatibility adapters over workmarket-owned services while preserving legacy capability callers.
- Status: complete

2. Cross-module import cleanup
- Plan: `.agents/vision-cross-module-import-cleanup-plan.md`
- Role: remove the remaining direct `vision` work-domain imports from `agent`, `location`, and other non-vision packages where workmarket-owned contracts or overloaded helpers can replace them.
- Status: complete

3. Final closeout
- Role: docs sync, generated artifacts, targeted validation, freshness checks, and completion audits for both child plans plus the master plan.
- Status: complete

## Validation

- `./mvnw -q -DskipTests compile`
- `./mvnw -q -Dtest=QuestServiceTest,QuestApplicationServiceTest,QuestWorkflowScenarioTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,AdminAgentExecutionServiceTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest test`
- `npm --prefix apps/themuffinman/frontend run generate:contracts`
- `make generate-agent-operating-model generate-agent-artifacts`
- `make control-refresh-full`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-quest-compatibility-service-reduction-plan.md`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-cross-module-import-cleanup-plan.md`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-workmarket-final-separation-master-plan.md`

## Completion Evidence

- Status: complete
- The remaining legacy quest compatibility surface in `vision` now delegates to workmarket-owned read and mutation services, while workmarket owns the business and read boundary for quests, applications, dashboard, reviews, and news.
- Non-vision work-domain callers in `agent` and `location` now default to workmarket-owned quest contracts, leaving `vision` as an explicit compatibility layer instead of a mixed ownership boundary.
- Generated control artifacts, frontend contracts, and source-of-truth docs were refreshed after the boundary move so the closeout evidence matches the implemented state.
- Validation passed on `2026-07-07`:
  - `./mvnw -q -DskipTests compile`
  - `./mvnw -q -Dtest=QuestServiceTest,QuestApplicationServiceTest,QuestWorkflowScenarioTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,AdminAgentExecutionServiceTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest,LocationLookupServiceTest,WorkmarketVisionContractBridgeTest test`
  - `npm --prefix apps/themuffinman/frontend run generate:contracts`
  - `make generate-agent-operating-model generate-agent-artifacts`
  - `make control-refresh-full`
