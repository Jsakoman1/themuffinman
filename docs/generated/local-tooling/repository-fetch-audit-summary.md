# Repository Fetch Audit

- Generated at: `2026-06-30T10:54:35Z`
- Repository methods scanned: `100`
- High risk: `0`
- Medium risk: `10`

## High-risk and medium-risk methods

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
- `AppUserRepository.countByLocationProviderPlaceIdIsNotNull` | risk=`low` | query=`derived_query` | fetch=none | lazy=none
- ... 88 more methods

## High-risk sample
