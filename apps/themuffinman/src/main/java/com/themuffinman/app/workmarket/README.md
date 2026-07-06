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

- The dedicated `workmarket` package is being extracted from the overloaded `vision` implementation surface.
- Quest list/detail reads, application reads, and application mutations now route through `workmarket` facades.
- Dashboard reads and option catalogs now route through `workmarket` facades.
- Presentation formatting for workmarket quest/application/news surfaces now lives in a local `WorkmarketPresentationHelper`.
- The controller surface has been switched to the `workmarket` facade for quests, applications, dashboard reads, news, and reviews.
- Some legacy `vision` services are still present as migration scaffolding, but the workmarket boundary now owns the main read/write flow.

## Next Steps

- Finish cutting the remaining `vision`-only admin and legacy adapters out of the workmarket boundary.
- Decide whether the remaining shared helper services should stay in `vision` or get duplicated into `workmarket`.
- Switch the vision surface to consume the new workmarket package instead of owning domain logic directly.
