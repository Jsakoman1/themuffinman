# Workmarket Backend Capsule

## Responsibility

Owns the quest, application, dashboard, and review domain boundary for the work marketplace.

## Main Entry Points

- `service/WorkmarketCreateQuestUseCase.java`
- `service/WorkmarketQuestService.java`
- `service/WorkmarketQuestApplicationService.java`
- `service/WorkmarketDashboardService.java`
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
- `VisionQuestFacadeService` and `VisionQuestApplicationFacadeService` now act as thin adapters that delegate to workmarket services.
- Quest and application reads stay inside the dedicated workmarket read services, while mutation services keep the domain rules and state transitions.
- `WorkmarketPresentationHelper` owns the local workmarket presentation formatting for quest, application, dashboard, and news surfaces.
- Domain side effects are published through explicit workmarket events and handled in the workmarket package.
- The workmarket boundary still shares the existing frontend DTO contract types with the `vision` layer, but the business logic now lives in workmarket services.

## Next Steps

- Keep the remaining boundary adapters narrow and explicit.
- Continue tightening workmarket-native read and write surfaces where the dedicated package can own more of the assembly directly.
- Keep the documentation and validation inventory aligned when the boundary moves again.
