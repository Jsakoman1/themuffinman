---
machine_kind: plan
machine_status: complete
machine_title: Vision Workmarket Bridge Reduction Plan
machine_goal: Audit the remaining workmarket-to-vision compatibility helpers and retain only the conversions that still have live runtime callers.
---

# Vision Workmarket Bridge Reduction Plan

## Status

Complete.

## Goal

Audit the remaining workmarket-to-vision compatibility helpers and retain only the conversions that still have live runtime callers.

## Scope

- Included: `WorkmarketQuestMgr` compatibility conversions, capability-notification bridge usage, location quest compatibility overload audit, docs sync.
- Excluded: deleting active legacy compatibility helpers that still back `QuestService`, `QuestReadService`, or location compatibility paths.

## Slices

- [x] Audit `WorkmarketQuestMgr` legacy entity conversion helpers and retain only the ones that still have live runtime callers.
- [x] Audit `vision` location helper overloads and confirm whether they are still active compatibility surface.
- [x] Narrow notification bridging to one explicit compatibility mapping point in the feed preview renderer.
- [x] Update docs for the reduced compatibility surface and re-audit the plan.

## Validation

- `./mvnw -q -DskipTests compile`
- `./mvnw -q -Dtest=QuestServiceTest,QuestApplicationServiceTest,QuestWorkflowScenarioTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,VisionConversationServiceTest,AdminAgentExecutionServiceTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest,LocationLookupServiceTest,WorkmarketVisionContractBridgeTest test`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-workmarket-bridge-reduction-plan.md`

## Completion Evidence

- Status: complete
- `WorkmarketQuestMgr.toVisionEntity(...)` and `toWorkmarketEntity(...)` were audited and intentionally retained because `QuestService` and `QuestReadService` still use them as active compatibility adapters.
- Quest-location compatibility overloads in `location` helpers were audited and intentionally retained because active legacy `vision` quest services still call them.
- The capability notification path no longer relies on generic contract-bridge object conversion; it now uses one explicit manual mapping in `VisionFeedPreviewRenderer`.
- Validation passed on `2026-07-07`:
  - `./mvnw -q -DskipTests compile`
  - `./mvnw -q -Dtest=QuestServiceTest,QuestApplicationServiceTest,QuestWorkflowScenarioTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,VisionConversationServiceTest,AdminAgentExecutionServiceTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest,LocationLookupServiceTest,WorkmarketVisionContractBridgeTest test`
