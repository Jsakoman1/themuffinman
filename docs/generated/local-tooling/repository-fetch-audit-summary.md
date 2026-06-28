# Repository Fetch Audit

- Generated at: `2026-06-28T20:30:13Z`
- Repository methods scanned: `70`
- High risk: `0`
- Medium risk: `10`

## `AppUserRepository.findByEmail`

- Returned entity: `AppUser`
- Query style: `jpql_query`
- Fetch relations: none
- Likely lazy relations touched downstream: `role`, `servletRequest`
- Risk: `medium`

## `ChatConversationRepository.findByLeftParticipantIdAndRightParticipantId`

- Returned entity: `ChatConversation`
- Query style: `derived_query`
- Fetch relations: none
- Likely lazy relations touched downstream: none
- Risk: `medium`

## `ChatMessageRepository.findConversationIdsWithExpiredImages`

- Returned entity: `Long`
- Query style: `jpql_query`
- Fetch relations: none
- Likely lazy relations touched downstream: `chat`
- Risk: `medium`

## `ChatMessageRepository.findConversationIdsWithExpiredMessages`

- Returned entity: `Long`
- Query style: `jpql_query`
- Fetch relations: none
- Likely lazy relations touched downstream: `chat`
- Risk: `medium`

## `ChatPresenceRepository.findByUserId`

- Returned entity: `ChatPresence`
- Query style: `derived_query`
- Fetch relations: none
- Likely lazy relations touched downstream: none
- Risk: `medium`

## `CircleGroupRepository.findAllByOwnerIdAndIdIn`

- Returned entity: `CircleGroup`
- Query style: `jpql_query`
- Fetch relations: none
- Likely lazy relations touched downstream: `circle`
- Risk: `medium`

## `QuestApplicationRepository.findByIdDetailed`

- Returned entity: `QuestApplication`
- Query style: `jpql_query`
- Fetch relations: `applicant`, `quest`
- Likely lazy relations touched downstream: `proposedPrice`
- Risk: `medium`

## `QuestApplicationRepository.findByQuestIdAndApplicantIdAndStatus`

- Returned entity: `QuestApplication`
- Query style: `jpql_query`
- Fetch relations: `applicant`, `quest`
- Likely lazy relations touched downstream: `creator`
- Risk: `medium`

## `QuestNewsRepository.findByRecipientUserIdOrderByCreatedAtDesc`

- Returned entity: `QuestNewsItem`
- Query style: `derived_query`
- Fetch relations: none
- Likely lazy relations touched downstream: none
- Risk: `medium`

## `UserReviewRepository.findByQuestIdAndReviewerIdAndReviewedUserId`

- Returned entity: `UserReview`
- Query style: `derived_query`
- Fetch relations: none
- Likely lazy relations touched downstream: none
- Risk: `medium`

## `AppUserRepository.countByLocationLatitudeIsNotNullAndLocationLongitudeIsNotNull`

- Returned entity: _non-entity_
- Query style: `derived_query`
- Fetch relations: none
- Likely lazy relations touched downstream: none
- Risk: `low`

## `AppUserRepository.countByLocationProviderPlaceIdIsNotNull`

- Returned entity: _non-entity_
- Query style: `derived_query`
- Fetch relations: none
- Likely lazy relations touched downstream: none
- Risk: `low`

## `AppUserRepository.countByRole`

- Returned entity: _non-entity_
- Query style: `derived_query`
- Fetch relations: none
- Likely lazy relations touched downstream: none
- Risk: `low`

## `AppUserRepository.existsByEmail`

- Returned entity: _non-entity_
- Query style: `jpql_query`
- Fetch relations: none
- Likely lazy relations touched downstream: none
- Risk: `low`

## `AppUserRepository.existsByEmailAndIdNot`

- Returned entity: _non-entity_
- Query style: `jpql_query`
- Fetch relations: none
- Likely lazy relations touched downstream: none
- Risk: `low`

## `AppUserRepository.searchByUsernameOrEmail`

- Returned entity: `AppUser`
- Query style: `jpql_query`
- Fetch relations: none
- Likely lazy relations touched downstream: none
- Risk: `low`

## `ChatConversationRepository.findDetailedById`

- Returned entity: `ChatConversation`
- Query style: `jpql_query`
- Fetch relations: `lastMessageSender`, `leftParticipant`, `rightParticipant`
- Likely lazy relations touched downstream: none
- Risk: `low`

## `ChatConversationRepository.findDetailedByParticipantId`

- Returned entity: `ChatConversation`
- Query style: `jpql_query`
- Fetch relations: `lastMessageSender`, `leftParticipant`, `rightParticipant`
- Likely lazy relations touched downstream: none
- Risk: `low`

## `ChatMessageRepository.deleteByCreatedAtBefore`

- Returned entity: _non-entity_
- Query style: `jpql_query`
- Fetch relations: none
- Likely lazy relations touched downstream: `chat`
- Risk: `low`

## `ChatMessageRepository.findDetailedByConversationId`

- Returned entity: `ChatMessage`
- Query style: `jpql_query`
- Fetch relations: `sender`
- Likely lazy relations touched downstream: none
- Risk: `low`

## `ChatMessageRepository.findLatestDetailedByConversationId`

- Returned entity: `ChatMessage`
- Query style: `jpql_query`
- Fetch relations: `sender`
- Likely lazy relations touched downstream: none
- Risk: `low`

## `ChatMessageRepository.findUnreadCountsByConversationIds`

- Returned entity: _non-entity_
- Query style: `jpql_query`
- Fetch relations: none
- Likely lazy relations touched downstream: none
- Risk: `low`

## `ChatMessageRepository.findUnreadIncomingByConversationId`

- Returned entity: `ChatMessage`
- Query style: `jpql_query`
- Fetch relations: none
- Likely lazy relations touched downstream: none
- Risk: `low`

## `ChatMessageRepository.getConversationId`

- Returned entity: _non-entity_
- Query style: `derived_query`
- Fetch relations: none
- Likely lazy relations touched downstream: none
- Risk: `low`

## `ChatMessageRepository.getUnreadCount`

- Returned entity: _non-entity_
- Query style: `derived_query`
- Fetch relations: none
- Likely lazy relations touched downstream: none
- Risk: `low`
