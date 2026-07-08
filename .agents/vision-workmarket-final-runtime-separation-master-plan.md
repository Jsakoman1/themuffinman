---
machine_kind: master-plan
machine_status: complete
machine_title: Vision Workmarket Final Runtime Separation Master Plan
machine_goal: Finish the remaining safe runtime separation so vision capability orchestration reads work-domain data through workmarket ownership while explicit legacy adapters stay narrow and intentional.
---

# Vision Workmarket Final Runtime Separation Master Plan

## Status

Complete.

## Goal

Finish the remaining safe runtime separation so vision capability orchestration reads work-domain data through workmarket ownership while explicit legacy adapters stay narrow and intentional.

## Scope

- Included: capability preview/search/discovery/entity-resolution extraction, compatibility-helper audit, docs sync, and validation.
- Excluded: deleting all legacy `vision` quest/application DTO-model copies in one unsafe pass while compatibility services still have live callers.

## Child Plans

1. Capability extraction
- Plan: `.agents/vision-workmarket-capability-extraction-plan.md`
- Role: move quest/application/news runtime reads in capability, discovery, and entity-resolution flows onto workmarket-owned services and DTOs.
- Status: complete

2. Bridge reduction
- Plan: `.agents/vision-workmarket-bridge-reduction-plan.md`
- Role: audit and narrow the remaining compatibility helpers so only live adapter paths stay in place.
- Status: complete

3. Final closeout
- Role: docs sync, compile/test validation, and plan audits.
- Status: complete

## Validation

- `./mvnw -q -DskipTests compile`
- `./mvnw -q -Dtest=VisionCapabilityPreviewSupportTest,VisionSearchDiscoveryServiceTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionConversationServiceTest test`
- `./mvnw -q -Dtest=QuestServiceTest,QuestApplicationServiceTest,QuestWorkflowScenarioTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,VisionConversationServiceTest,AdminAgentExecutionServiceTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest,LocationLookupServiceTest,WorkmarketVisionContractBridgeTest test`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-workmarket-capability-extraction-plan.md`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-workmarket-bridge-reduction-plan.md`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-workmarket-final-runtime-separation-master-plan.md`

## Completion Evidence

- Status: complete
- `vision` capability preview, search discovery, quest discovery, and entity-resolution flows now consume workmarket-owned quest/application/news services and DTOs directly instead of routing those runtime reads through legacy `vision` work DTO copies.
- The remaining legacy compatibility boundary is now explicit: `QuestService`, `QuestReadService`, `QuestApplicationService`, and some quest-location overloads still exist because they have live callers, not because work-domain ownership is still mixed.
- Documentation now records that capability/search/runtime reads are workmarket-owned while the remaining compatibility surface is narrow and intentional.
- Validation passed on `2026-07-07`:
  - `./mvnw -q -DskipTests compile`
  - `./mvnw -q -Dtest=VisionCapabilityPreviewSupportTest,VisionSearchDiscoveryServiceTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionConversationServiceTest test`
  - `./mvnw -q -Dtest=QuestServiceTest,QuestApplicationServiceTest,QuestWorkflowScenarioTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionSearchDiscoveryServiceTest,VisionConversationServiceTest,AdminAgentExecutionServiceTest,ServiceTransactionConfigurationTest,AgentOperatingModelValidationTest,LocationLookupServiceTest,WorkmarketVisionContractBridgeTest test`
