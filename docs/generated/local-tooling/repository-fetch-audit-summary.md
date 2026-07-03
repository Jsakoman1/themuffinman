# Repository Fetch Audit

- Generated at: `2026-07-03T09:17:21Z`
- Repository methods scanned: `105`
- High risk: `1`
- Medium risk: `10`

## High-risk and medium-risk methods

- `AppUserRepository.searchByUsernameOrEmail` | risk=`high` | query=`jpql_query` | fetch=none | lazy=`username`
  - `VisionCapabilityPreviewService.resolveUserProfileTarget` in `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewService.java` | helper=none | lazy=`username`
  - `VisionCapabilityPreviewService.resolveCircleRequestRecipient` in `apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewService.java` | helper=none | lazy=`username`
- `AppUserRepository.findByEmail` | risk=`medium` | query=`jpql_query` | fetch=none | lazy=`role`, `servletRequest`
- `BusinessProfileRepository.findByOwnerId` | risk=`medium` | query=`jpql_query` | fetch=`owner` | lazy=`slug`
- `ChatConversationRepository.findByLeftParticipantIdAndRightParticipantId` | risk=`medium` | query=`derived_query` | fetch=none | lazy=none
- `ChatMessageRepository.findConversationIdsWithExpiredImages` | risk=`medium` | query=`jpql_query` | fetch=none | lazy=`chat`
- `ChatMessageRepository.findConversationIdsWithExpiredMessages` | risk=`medium` | query=`jpql_query` | fetch=none | lazy=`chat`
- `ChatPresenceRepository.findByUserId` | risk=`medium` | query=`derived_query` | fetch=none | lazy=none
- `CircleGroupRepository.findAllByOwnerIdAndIdIn` | risk=`medium` | query=`jpql_query` | fetch=none | lazy=`circle`
- `QuestApplicationRepository.findForApplicationDetail` | risk=`medium` | query=`jpql_query` | fetch=`applicant`, `creator`, `quest` | lazy=`proposedPrice`
- `QuestNewsRepository.findByRecipientUserIdOrderByCreatedAtDesc` | risk=`medium` | query=`derived_query` | fetch=none | lazy=none
- `UserReviewRepository.findByQuestIdAndReviewerIdAndReviewedUserId` | risk=`medium` | query=`derived_query` | fetch=none | lazy=none
- `AppUserRepository.countByLocationLatitudeIsNotNullAndLocationLongitudeIsNotNull` | risk=`low` | query=`derived_query` | fetch=none | lazy=none
- ... 93 more methods

## High-risk sample

- `AppUserRepository.searchByUsernameOrEmail` -> `AppUser`