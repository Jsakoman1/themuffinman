# Mapper Usage Audit

- Generated at: `2026-06-29T19:54:42Z`
- Mappers scanned: `10`

## `AppUserMgr`

- Risk flags: `navigation_logic`, `relation_dereference`, `rich_text_sanitization`, `service_backed`
- Usage count: `5`
- `AppUserController.getAppUser` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java` -> `withProfileStats` (`controller_facing`)
- `AdminUserDetailService.getDetail` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AdminUserDetailService.java` -> `toDto` (`read_oriented`)
- `AdminUserDetailService.getDetail` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/AdminUserDetailService.java` -> `withProfileStats` (`read_oriented`)
- `UserProfileViewService.getProfileView` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java` -> `toDto` (`read_oriented`)
- `UserProfileViewService.getProfileView` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/UserProfileViewService.java` -> `withProfileStats` (`read_oriented`)

## `QuestApplicationMgr`

- Risk flags: `navigation_logic`, `relation_dereference`, `rich_text_sanitization`
- Usage count: `4`
- `ApplyForQuestUseCase.execute` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java` -> `toEntity` (`supporting`)
- `QuestApplicationViewAssembler.toApplicantResponse` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationViewAssembler.java` -> `toDto` (`read_oriented`)
- `QuestApplicationViewAssembler.toManagementResponse` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationViewAssembler.java` -> `toDto` (`read_oriented`)
- `QuestApplicationViewAssembler.toPublicResponse` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationViewAssembler.java` -> `toDto` (`read_oriented`)

## `ThingSharingMgr`

- Risk flags: `relation_dereference`, `rich_text_sanitization`
- Usage count: `4`
- `ThingSharingService.getAvailableListings` in `apps/themuffinman/src/main/java/com/themuffinman/app/things/service/ThingSharingService.java` -> `toListingDto` (`read_oriented`)
- `ThingSharingService.getMyListings` in `apps/themuffinman/src/main/java/com/themuffinman/app/things/service/ThingSharingService.java` -> `toListingDto` (`read_oriented`)
- `ThingSharingService.requestBorrow` in `apps/themuffinman/src/main/java/com/themuffinman/app/things/service/ThingSharingService.java` -> `toBorrowRequestDto` (`supporting`)
- `ThingSharingService.saveMyListing` in `apps/themuffinman/src/main/java/com/themuffinman/app/things/service/ThingSharingService.java` -> `toListingDto` (`supporting`)

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

- Risk flags: `navigation_logic`, `relation_dereference`, `rich_text_sanitization`
- Usage count: `3`
- `CreateQuestUseCase.execute` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java` -> `toEntity` (`supporting`)
- `QuestViewAssembler.toResponse` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java` -> `toDto` (`read_oriented`)
- `QuestViewAssembler.toResponse` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestViewAssembler.java` -> `withViewerContext` (`read_oriented`)

## `BusinessProfileMgr`

- Risk flags: `relation_dereference`, `rich_text_sanitization`
- Usage count: `2`
- `BusinessProfileService.getProfileBySlug` in `apps/themuffinman/src/main/java/com/themuffinman/app/business/service/BusinessProfileService.java` -> `toDto` (`read_oriented`)
- `BusinessProfileService.saveMyProfile` in `apps/themuffinman/src/main/java/com/themuffinman/app/business/service/BusinessProfileService.java` -> `toDto` (`supporting`)

## `RideOfferMgr`

- Risk flags: `relation_dereference`, `rich_text_sanitization`
- Usage count: `1`
- `RideOfferService.createOffer` in `apps/themuffinman/src/main/java/com/themuffinman/app/rides/service/RideOfferService.java` -> `toDto` (`mutating`)

## `UserReviewMgr`

- Risk flags: `navigation_logic`, `relation_dereference`
- Usage count: `1`
- `UserReviewService.createOrUpdateReview` in `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/UserReviewService.java` -> `toDto` (`mutating`)

## `QuestNewsMgr`

- Risk flags: `navigation_logic`
- Usage count: `0`
