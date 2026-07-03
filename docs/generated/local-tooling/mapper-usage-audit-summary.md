# Mapper Usage Audit

- Generated at: `2026-07-03T09:17:20Z`
- Mappers scanned: `10`

## `AppUserMgr`

- Risk flags: `navigation_logic`, `relation_dereference`, `rich_text_sanitization`, `service_backed`
- Usage count: `13`
- `AppUserController.getAppUser` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/controller/AppUserController.java` -> `withProfileStats` (`controller_facing`)
- `IdentityUserSummaryAssembler.buildProfileSummary` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/IdentityUserSummaryAssembler.java` -> `toDto` (`read_oriented`)
- `IdentityUserSummaryAssembler.buildProfileSummary` in `apps/themuffinman/src/main/java/com/themuffinman/app/identity/service/IdentityUserSummaryAssembler.java` -> `withProfileStats` (`read_oriented`)
- `VisionCapabilityPreviewService.previewProfile` in `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewService.java` -> `toDto` (`supporting`)
- `VisionCapabilityPreviewService.previewProfile` in `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewService.java` -> `withProfileStats` (`supporting`)
- ... 8 more callers

## `QuestApplicationMgr`

- Risk flags: `navigation_logic`, `relation_dereference`, `rich_text_sanitization`
- Usage count: `4`
- `ApplyForQuestUseCase.execute` in `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/ApplyForQuestUseCase.java` -> `toEntity` (`supporting`)
- `QuestApplicationViewAssembler.toApplicantResponse` in `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/QuestApplicationViewAssembler.java` -> `toDto` (`read_oriented`)
- `QuestApplicationViewAssembler.toManagementResponse` in `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/QuestApplicationViewAssembler.java` -> `toDto` (`read_oriented`)
- `QuestApplicationViewAssembler.toPublicResponse` in `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/QuestApplicationViewAssembler.java` -> `toDto` (`read_oriented`)

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
- `CreateQuestUseCase.execute` in `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/CreateQuestUseCase.java` -> `toEntity` (`supporting`)
- `QuestViewAssembler.toResponse` in `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/QuestViewAssembler.java` -> `toDto` (`read_oriented`)
- `QuestViewAssembler.toResponse` in `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/QuestViewAssembler.java` -> `withViewerContext` (`read_oriented`)

## `BusinessProfileMgr`

- Risk flags: `relation_dereference`, `rich_text_sanitization`
- Usage count: `2`
- `BusinessProfileService.getProfileBySlug` in `apps/themuffinman/src/main/java/com/themuffinman/app/business/service/BusinessProfileService.java` -> `toDto` (`read_oriented`)
- `BusinessProfileService.saveMyProfile` in `apps/themuffinman/src/main/java/com/themuffinman/app/business/service/BusinessProfileService.java` -> `toDto` (`supporting`)

## `RideOfferMgr`

- Risk flags: `relation_dereference`, `rich_text_sanitization`
- Usage count: `1`
- `RideOfferService.createOffer` in `apps/themuffinman/src/main/java/com/themuffinman/app/rides/service/RideOfferService.java` -> `toDto` (`mutating`)

- ... 2 more mappers