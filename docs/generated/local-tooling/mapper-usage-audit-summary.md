# Mapper Usage Audit

- Generated at: `2026-06-28T20:30:13Z`
- Mappers scanned: `7`

## `QuestApplicationMgr`

- Risk flags: `navigation_logic`, `relation_dereference`, `rich_text_sanitization`
- Usage count: `7`
- `QuestApplicationService.applyForQuest` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java` -> `toEntity` (`mutating`)
- `QuestApplicationService.getAllApplicationsForAdmin` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java` -> `toDto` (`read_oriented`)
- `QuestApplicationService.getApplicationsForQuest` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java` -> `toDto` (`read_oriented`)
- `QuestApplicationService.getApplicationsViewForQuest` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java` -> `toDto` (`read_oriented`)
- `QuestApplicationService.getPublicApprovedApplicationsViewForQuest` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java` -> `toDto` (`read_oriented`)
- `QuestApplicationService.searchApplicationsForAdmin` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java` -> `toDto` (`read_oriented`)
- `QuestApplicationService.toApplicantResponse` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java` -> `toDto` (`read_oriented`)

## `AppUserMgr`

- Risk flags: `navigation_logic`, `relation_dereference`, `rich_text_sanitization`, `service_backed`
- Usage count: `5`
- `AppUserController.getAppUser` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java` -> `withProfileStats` (`controller_facing`)
- `AdminUserDetailService.getDetail` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AdminUserDetailService.java` -> `toDto` (`read_oriented`)
- `AdminUserDetailService.getDetail` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AdminUserDetailService.java` -> `withProfileStats` (`read_oriented`)
- `UserProfileViewService.getProfileView` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java` -> `toDto` (`read_oriented`)
- `UserProfileViewService.getProfileView` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java` -> `withProfileStats` (`read_oriented`)

## `AuthMgr`

- Risk flags: `relation_dereference`, `rich_text_sanitization`
- Usage count: `3`
- `AuthService.login` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AuthService.java` -> `toResponse` (`supporting`)
- `AuthService.me` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AuthService.java` -> `toResponse` (`supporting`)
- `AuthService.register` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AuthService.java` -> `toResponse` (`supporting`)

## `CircleRequestMgr`

- Risk flags: `relation_dereference`, `rich_text_sanitization`
- Usage count: `3`
- `CircleRelationService.acceptCircleRequest` in `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleRelationService.java` -> `toDto` (`mutating`)
- `CircleRelationService.blockCircleUser` in `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleRelationService.java` -> `toDto` (`mutating`)
- `CircleRelationService.createCircleRequest` in `apps/themuffinman/src/main/java/com/themuffinman/app/social/service/CircleRelationService.java` -> `toDto` (`mutating`)

## `QuestMgr`

- Risk flags: `navigation_logic`, `relation_dereference`, `rich_text_sanitization`, `service_backed`
- Usage count: `3`
- `CreateQuestUseCase.execute` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java` -> `toEntity` (`supporting`)
- `QuestViewAssembler.toResponse` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java` -> `toDto` (`read_oriented`)
- `QuestViewAssembler.toResponse` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java` -> `withViewerContext` (`read_oriented`)

## `UserReviewMgr`

- Risk flags: `navigation_logic`, `relation_dereference`
- Usage count: `1`
- `UserReviewService.createOrUpdateReview` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/UserReviewService.java` -> `toDto` (`mutating`)

## `QuestNewsMgr`

- Risk flags: `navigation_logic`
- Usage count: `0`
