# Workmarket Backend Capsule

## Responsibility

Owns the quest, application, dashboard, and review domain boundary for the work marketplace.

## Main Entry Points

- `controller/QuestController.java`
- `controller/QuestApplicationController.java`
- `controller/DashboardController.java`
- `controller/QuestNewsController.java`
- `controller/UserReviewController.java`
- `service/WorkmarketCreateQuestUseCase.java`
- `service/WorkmarketQuestService.java`
- `service/WorkmarketQuestApplicationService.java`
- `service/WorkmarketDashboardReadService.java`
- `service/WorkmarketQuestDetailSectionsFactory.java`
- `service/WorkmarketQuestSearchScopeService.java`
- `service/WorkmarketQuestListPresetResolver.java`
- `service/WorkmarketQuestResponseFactory.java`
- `service/WorkmarketQuestViewerApplicationMapFactory.java`
- `service/WorkmarketQuestNewsService.java`
- `service/WorkmarketUserReviewService.java`
- `service/WorkmarketQuestAccessPolicyService.java`
- `service/WorkmarketQuestQueryService.java`
- `model/Quest.java`
- `model/QuestApplication.java`
- `repository/WorkmarketQuestRepository.java`
- `repository/WorkmarketQuestApplicationRepository.java`

## Current State

- The workmarket package now owns the quest, application, dashboard, review, and notification boundary for the work marketplace.
- The quest, quest-application, dashboard, news, and review HTTP entrypoints now live under `workmarket.controller` while keeping the existing routes stable.
- The legacy `vision` marketplace facade services have been removed; runtime quest, application, dashboard, news, and review ownership now stays in workmarket services plus the narrow `/vision` conversation-orchestration layer.
- The old `QuestService`, `QuestReadService`, `QuestApplicationService`, `DashboardService`, `QuestNewsService`, `UserReviewService`, and `VisionOptionsService` compatibility adapters have been removed.
- `VisionCapabilityPreviewService`, `VisionSearchDiscoveryService`, `VisionQuestDiscoveryService`, and `VisionCapabilityEntityResolutionSupport` now read quest/application/news data through workmarket-owned services and DTOs instead of the legacy `vision` quest/application contract copies.
- `VisionCreateQuestExecutionAdapter` now creates quests through `WorkmarketQuestService` directly and returns the workmarket-owned quest entity to the `/vision` execution boundary.
- Quest and application reads stay inside the dedicated workmarket read services, while mutation services keep the domain rules and state transitions.
- `WorkmarketQuestSearchScopeService`, `WorkmarketQuestListPresetResolver`, `WorkmarketQuestResponseFactory`, and `WorkmarketQuestViewerApplicationMapFactory` now keep viewer search scope loading, preset selection, response assembly, and applicant-context mapping out of the main read facade so `WorkmarketQuestReadService` stays focused on owner-side query orchestration.
- `WorkmarketPresentationHelper` owns the local workmarket presentation formatting for quest, application, dashboard, and news surfaces.
- Repository fetch-profile coverage now treats `WorkmarketQuestRepository` and `WorkmarketQuestApplicationRepository` as the canonical read-surface ownership boundary.
- Domain side effects are published through explicit workmarket events and handled in the workmarket package.
- Workmarket now owns the mutation request DTOs and the main quest, application, dashboard, news, review, and options read/response DTOs for its HTTP boundary.
- Identity profile, admin-detail, and options surfaces now also consume those workmarket-owned read contracts directly instead of routing through legacy `vision.dto` read types.
- Admin synthetic quest execution and location metrics/helpers now default to workmarket-owned quest contracts outside the remaining legacy `vision` compatibility boundary.
- The legacy `vision` package still keeps only the conversation, preview, and semantic-orchestration surfaces plus the DTO copies they still render.
- Workmarket-owned services, repositories, mappers, and models now form the canonical marketplace runtime boundary.

## Next Steps

- Keep the remaining boundary adapters narrow and explicit.
- Continue tightening workmarket-native read and write surfaces where the dedicated package can own more of the assembly directly.
- Keep the documentation and validation inventory aligned when the boundary moves again.
