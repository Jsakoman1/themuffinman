---
machine_kind: plan
machine_status: complete
machine_title: Vision Workmarket Capability Extraction Plan
machine_goal: Move capability-facing quest/application preview, search, and entity-resolution flows onto workmarket-owned services and DTOs.
---

# Vision Workmarket Capability Extraction Plan

## Status

Complete.

## Goal

Move capability-facing quest/application preview, search, and entity-resolution flows onto workmarket-owned services and DTOs.

## Scope

- Included: `VisionCapabilityPreviewService`, `VisionSearchDiscoveryService`, `VisionQuestDiscoveryService`, `VisionCapabilityEntityResolutionSupport`, `VisionFeedPreviewRenderer`, and the affected tests.
- Excluded: deleting legacy `vision` compatibility services and DTO copies that still back older vision-facing consumers.

## Slices

- [x] Switch preview/runtime quest and application reads to workmarket-owned services and DTOs.
- [x] Switch search/discovery and entity-resolution flows to workmarket-owned quest/application services and DTOs.
- [x] Keep a narrow compatibility mapping only where dashboard notifications still expect legacy `vision` notification DTOs.
- [x] Update tests and validate the extracted capability/search/runtime slice.

## Validation

- `./mvnw -q -DskipTests compile`
- `./mvnw -q -Dtest=VisionCapabilityPreviewSupportTest,VisionSearchDiscoveryServiceTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionConversationServiceTest test`
- `ruby scripts/audits/audit-plan-completion.rb plan=.agents/vision-workmarket-capability-extraction-plan.md`

## Completion Evidence

- Status: complete
- `VisionCapabilityPreviewService` now depends on `WorkmarketQuestReadService`, `WorkmarketQuestApplicationReadService`, `WorkmarketQuestApplicationService`, and `WorkmarketQuestNewsService` for quest/application/news runtime behavior.
- `VisionSearchDiscoveryService` and `VisionQuestDiscoveryService` now use workmarket-owned read services and DTOs directly for discovery behavior.
- `VisionCapabilityEntityResolutionSupport` now resolves quest/application targets from workmarket-owned read services and DTOs instead of legacy `vision` work DTOs.
- `VisionFeedPreviewRenderer` and `VisionCapabilityPreviewSupport` now operate on workmarket-owned quest/application/news DTOs, keeping only a narrow manual notification mapping where the dashboard notification assembler still expects legacy `vision` notification DTOs.
- Validation passed on `2026-07-07`:
  - `./mvnw -q -DskipTests compile`
  - `./mvnw -q -Dtest=VisionCapabilityPreviewSupportTest,VisionSearchDiscoveryServiceTest,VisionCapabilityPreviewServiceTest,VisionCapabilityPreviewServiceAliasResolutionTest,VisionConversationServiceTest test`
