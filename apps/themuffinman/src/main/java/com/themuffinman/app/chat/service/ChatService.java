package com.themuffinman.app.chat.service;

import com.themuffinman.app.chat.dto.ChatCircleOptionDTO;
import com.themuffinman.app.chat.dto.ChatContactDTO;
import com.themuffinman.app.chat.dto.ChatAttachmentUploadDTO;
import com.themuffinman.app.chat.dto.ChatAttachmentStorageStatusDTO;
import com.themuffinman.app.chat.dto.ChatConversationParticipantDTO;
import com.themuffinman.app.chat.dto.ChatConversationParticipantsRequestDTO;
import com.themuffinman.app.chat.dto.ChatConversationListDTO;
import com.themuffinman.app.chat.dto.ChatConversationRoleRequestDTO;
import com.themuffinman.app.chat.dto.ChatConversationStateRequestDTO;
import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
import com.themuffinman.app.chat.dto.ChatConversationSyncDTO;
import com.themuffinman.app.chat.dto.ChatConversationTitleRequestDTO;
import com.themuffinman.app.chat.dto.ChatCreateGroupConversationRequestDTO;
import com.themuffinman.app.chat.dto.ChatMarkReadRequestDTO;
import com.themuffinman.app.chat.dto.ChatMessageDTO;
import com.themuffinman.app.chat.dto.ChatMessagePageDTO;
import com.themuffinman.app.chat.dto.ChatMessageReactionDTO;
import com.themuffinman.app.chat.dto.ChatMessageReactionRequestDTO;
import com.themuffinman.app.chat.dto.ChatMessageRequestDTO;
import com.themuffinman.app.chat.dto.ChatMessageUpdateRequestDTO;
import com.themuffinman.app.chat.dto.ChatOpenConversationRequestDTO;
import com.themuffinman.app.chat.dto.ChatReceiptRequestDTO;
import com.themuffinman.app.chat.dto.ChatWorkspaceDTO;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
import com.themuffinman.app.chat.model.ChatAttachmentUpload;
import com.themuffinman.app.chat.model.ChatConversation;
import com.themuffinman.app.chat.model.ChatConversationContextType;
import com.themuffinman.app.chat.model.ChatConversationMemberState;
import com.themuffinman.app.chat.model.ChatConversationParticipant;
import com.themuffinman.app.chat.model.ChatConversationParticipantRole;
import com.themuffinman.app.chat.model.ChatConversationType;
import com.themuffinman.app.chat.model.ChatMessage;
import com.themuffinman.app.chat.model.ChatMessageReaction;
import com.themuffinman.app.chat.model.ChatPresence;
import com.themuffinman.app.chat.model.ChatAuditEventType;
import com.themuffinman.app.chat.repository.ChatAttachmentUploadRepository;
import com.themuffinman.app.chat.repository.ChatConversationParticipantRepository;
import com.themuffinman.app.chat.repository.ChatConversationRepository;
import com.themuffinman.app.chat.repository.ChatMessageReactionRepository;
import com.themuffinman.app.chat.repository.ChatMessageRepository;
import com.themuffinman.app.chat.repository.ChatPresenceRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.ChatProperties;
import com.themuffinman.app.config.ObjectStorageProperties;
import com.themuffinman.app.config.RetentionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.AppUserLookupService;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.model.CircleMembership;
import com.themuffinman.app.social.repository.CircleGroupRepository;
import com.themuffinman.app.social.service.CircleMembershipService;
import com.themuffinman.app.social.service.CircleRelationService;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import com.themuffinman.app.workmarket.service.WorkmarketQuestAccessPolicyService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestExecutionPrimitiveService;
import com.themuffinman.app.storage.ObjectStorageAccess;
import com.themuffinman.app.storage.ObjectStorageService;
import com.themuffinman.app.storage.StoredObject;
import com.themuffinman.app.storage.StoredObjectPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int MESSAGE_PREVIEW_LIMIT = 140;

    private final ChatConversationRepository chatConversationRepository;
    private final ChatConversationParticipantRepository chatConversationParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageReactionRepository chatMessageReactionRepository;
    private final ChatAttachmentUploadRepository chatAttachmentUploadRepository;
    private final ChatPresenceRepository chatPresenceRepository;
    private final AppUserLookupService appUserLookupService;
    private final CircleRelationService circleRelationService;
    private final CircleMembershipService circleMembershipService;
    private final CircleGroupRepository circleGroupRepository;
    private final WorkmarketQuestExecutionPrimitiveService workmarketQuestExecutionPrimitiveService;
    private final WorkmarketQuestApplicationRepository workmarketQuestApplicationRepository;
    private final WorkmarketQuestAccessPolicyService workmarketQuestAccessPolicyService;
    private final ChatProperties chatProperties;
    private final ChatPresenceService chatPresenceService;
    private final ChatRealtimeService chatRealtimeService;
    private final ChatRateLimitService chatRateLimitService;
    private final ChatConversationStateService chatConversationStateService;
    private final ChatAuditService chatAuditService;
    private final RetentionProperties retentionProperties;
    private final ObjectStorageProperties objectStorageProperties;
    private final ObjectStorageService objectStorageService;

    @Transactional(readOnly = true)
    public ChatWorkspaceDTO getWorkspace(AppUser currentUser) {
        return getWorkspace(currentUser, null, false);
    }

    @Transactional(readOnly = true)
    public ChatWorkspaceDTO getWorkspace(AppUser currentUser, Integer requestedConversationLimit) {
        return getWorkspace(currentUser, requestedConversationLimit, false);
    }

    @Transactional(readOnly = true)
    public ChatWorkspaceDTO getWorkspace(AppUser currentUser, Integer requestedConversationLimit, boolean includeArchived) {
        List<ChatConversation> activeConversations = chatConversationRepository.findDetailedByParticipantId(currentUser.getId()).stream()
                .filter(conversation -> canAccessConversation(currentUser, conversation))
                .toList();
        Map<Long, ChatConversationMemberState> stateByConversationId = chatConversationStateService.getStatesByConversationId(
                activeConversations.stream().map(ChatConversation::getId).toList(),
                currentUser
        );
        List<ChatConversation> visibleConversations = activeConversations.stream()
                .filter(conversation -> includeArchived || !chatConversationStateService.isArchived(stateByConversationId.get(conversation.getId())))
                .toList();

        int conversationLimit = normalizeConversationLimit(requestedConversationLimit);
        List<ChatConversation> limitedConversations = visibleConversations.stream()
                .limit(conversationLimit)
                .toList();
        List<Long> conversationIds = limitedConversations.stream()
                .map(ChatConversation::getId)
                .toList();
        Map<Long, Long> unreadCountsByConversationId = chatMessageRepository.findUnreadCountsByConversationIds(conversationIds, currentUser.getId()).stream()
                .collect(Collectors.toMap(ChatMessageRepository.UnreadCountRow::getConversationId, ChatMessageRepository.UnreadCountRow::getUnreadCount));

        Map<Long, List<CircleMembership>> membershipsByUserId = circleMembershipService.getMembershipsByUserIdForOwner(currentUser.getId()).entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .filter(entry -> isCurrentChatContact(currentUser, entry.getValue().getFirst().getMember()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        List<CircleGroup> ownedCircles = membershipsByUserId.values().stream()
                .flatMap(List::stream)
                .map(CircleMembership::getCircle)
                .distinct()
                .sorted(Comparator.comparing(CircleGroup::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        Map<Long, AppUser> contactByUserId = new LinkedHashMap<>();
        membershipsByUserId.forEach((userId, memberships) -> {
            if (!memberships.isEmpty()) {
                contactByUserId.put(userId, memberships.getFirst().getMember());
            }
        });

        Set<Long> presenceUserIds = new LinkedHashSet<>(contactByUserId.keySet());
        for (ChatConversation conversation : limitedConversations) {
            for (ChatConversationParticipant participant : effectiveParticipants(conversation)) {
                if (!Objects.equals(participant.getUser().getId(), currentUser.getId())) {
                    presenceUserIds.add(participant.getUser().getId());
                }
            }
        }
        Map<Long, ChatPresence> presenceByUserId = loadPresenceByUserId(new ArrayList<>(presenceUserIds));
        Instant now = Instant.now();

        List<ChatConversationSummaryDTO> conversationSummaries = limitedConversations.stream()
                .map(conversation -> toConversationSummary(
                        conversation,
                        currentUser,
                        unreadCountsByConversationId.getOrDefault(conversation.getId(), 0L),
                        stateByConversationId.get(conversation.getId()),
                        presenceByUserId,
                        now
                ))
                .toList();

        List<ChatContactDTO> contacts = contactByUserId.values().stream()
                .sorted(Comparator.comparing(AppUser::getUsername, String.CASE_INSENSITIVE_ORDER))
                .map(contact -> toContact(contact, membershipsByUserId.getOrDefault(contact.getId(), List.of()), presenceByUserId.get(contact.getId()), now))
                .toList();

        List<ChatCircleOptionDTO> circles = ownedCircles.stream()
                .map(circle -> ChatCircleOptionDTO.builder()
                        .id(circle.getId())
                        .name(circle.getName())
                        .build())
                .toList();

        long unreadConversationCount = unreadCountsByConversationId.values().stream()
                .filter(count -> count > 0)
                .count();
        long onlineContactCount = contacts.stream()
                .filter(ChatContactDTO::isOnline)
                .count();

        return ChatWorkspaceDTO.builder()
                .conversations(conversationSummaries)
                .contacts(contacts)
                .circles(circles)
                .unreadConversationCount(unreadConversationCount)
                .onlineContactCount(onlineContactCount)
                .conversationLimit(conversationLimit)
                .build();
    }

    @Transactional(readOnly = true)
    public ChatConversationListDTO listConversations(
            AppUser currentUser,
            String requestedConversationType,
            String requestedContextType,
            Long requestedContextId,
            String requestedQuery,
            Integer requestedLimit,
            Integer requestedPage,
            boolean includeArchived
    ) {
        ChatConversationType conversationType = parseConversationType(requestedConversationType);
        ChatConversationContextType contextType = parseConversationContextType(requestedContextType);
        if (contextType == null && requestedContextId != null) {
            throw ServiceErrors.badRequest("Context type is required when context id is provided");
        }

        List<ChatConversation> activeConversations = chatConversationRepository.findDetailedByParticipantId(currentUser.getId()).stream()
                .filter(conversation -> canAccessConversation(currentUser, conversation))
                .filter(conversation -> conversationType == null || conversation.getConversationType() == conversationType)
                .filter(conversation -> contextType == null || conversation.getContextType() == contextType)
                .filter(conversation -> requestedContextId == null || Objects.equals(conversation.getContextId(), requestedContextId))
                .toList();
        Map<Long, ChatConversationMemberState> stateByConversationId = chatConversationStateService.getStatesByConversationId(
                activeConversations.stream().map(ChatConversation::getId).toList(),
                currentUser
        );
        String normalizedQuery = normalizeText(requestedQuery);
        List<ChatConversation> visibleConversations = activeConversations.stream()
                .filter(conversation -> includeArchived || !chatConversationStateService.isArchived(stateByConversationId.get(conversation.getId())))
                .filter(conversation -> matchesConversationQuery(conversation, currentUser, normalizedQuery))
                .toList();

        int limit = normalizeConversationLimit(requestedLimit);
        int page = normalizePageIndex(requestedPage);
        int offset = page * limit;
        boolean hasMore = visibleConversations.size() > offset + limit;
        List<ChatConversation> limitedConversations = visibleConversations.stream()
                .skip(offset)
                .limit(limit)
                .toList();
        Map<Long, Long> unreadCountsByConversationId = chatMessageRepository.findUnreadCountsByConversationIds(
                        limitedConversations.stream().map(ChatConversation::getId).toList(),
                        currentUser.getId()
                ).stream()
                .collect(Collectors.toMap(ChatMessageRepository.UnreadCountRow::getConversationId, ChatMessageRepository.UnreadCountRow::getUnreadCount));
        Map<Long, ChatPresence> presenceByUserId = loadPresenceByUserId(
                limitedConversations.stream()
                        .flatMap(conversation -> effectiveParticipants(conversation).stream())
                        .map(ChatConversationParticipant::getUser)
                        .filter(user -> !Objects.equals(user.getId(), currentUser.getId()))
                        .map(AppUser::getId)
                        .distinct()
                        .toList()
        );
        Instant now = Instant.now();

        return ChatConversationListDTO.builder()
                .conversations(limitedConversations.stream()
                        .map(conversation -> toConversationSummary(
                                conversation,
                                currentUser,
                                unreadCountsByConversationId.getOrDefault(conversation.getId(), 0L),
                                stateByConversationId.get(conversation.getId()),
                                presenceByUserId,
                                now
                        ))
                .toList())
                .filteredCount(visibleConversations.size())
                .limit(limit)
                .page(page)
                .hasMore(hasMore)
                .includeArchived(includeArchived)
                .conversationType(conversationType == null ? null : conversationType.name())
                .contextType(contextType == null ? null : contextType.name())
                .contextId(requestedContextId)
                .query(normalizedQuery)
                .build();
    }

    @Transactional
    public ChatConversationSummaryDTO openConversation(ChatOpenConversationRequestDTO request, AppUser currentUser) {
        chatRateLimitService.assertWithinPerMinuteLimit(
                "chat-open",
                currentUser.getId().toString(),
                chatProperties.getMessages().getOpenLimitPerMinute(),
                "Too many chat open requests. Please wait a moment and try again."
        );
        AppUser otherUser = requireChatCounterpart(currentUser, request.getOtherUserId());
        ChatConversation conversation = findOrCreateDirectConversation(currentUser, otherUser);
        return openConversationSummary(conversation, currentUser);
    }

    @Transactional
    public ChatConversationSummaryDTO createGroupConversation(ChatCreateGroupConversationRequestDTO request, AppUser currentUser) {
        chatRateLimitService.assertWithinPerMinuteLimit(
                "chat-group-open",
                currentUser.getId().toString(),
                chatProperties.getMessages().getOpenLimitPerMinute(),
                "Too many group chat requests. Please wait a moment and try again."
        );
        List<AppUser> otherParticipants = requireGroupParticipants(currentUser, request);
        ChatConversation conversation = new ChatConversation();
        conversation.setConversationType(ChatConversationType.GROUP);
        conversation.setTitle(normalizeConversationTitle(request.getTitle()));
        conversation.setOwner(currentUser);
        conversation.setCreatedBy(currentUser);
        conversation = chatConversationRepository.save(conversation);
        syncConversationParticipants(
                conversation,
                buildGroupParticipantSeeds(currentUser, otherParticipants)
        );
        recordConversationAudit(ChatAuditEventType.GROUP_CONVERSATION_CREATED, currentUser, conversation, Map.of(
                "participantCount", effectiveParticipants(conversation).size(),
                "title", conversation.getTitle() == null ? "" : conversation.getTitle()
        ));
        return openConversationSummary(conversation, currentUser);
    }

    @Transactional
    public ChatConversationSummaryDTO renameGroupConversation(Long conversationId, ChatConversationTitleRequestDTO request, AppUser currentUser) {
        ChatConversation conversation = requireManageableGroupConversation(conversationId, currentUser);
        conversation.setTitle(normalizeConversationTitle(request.getTitle()));
        chatConversationRepository.save(conversation);
        recordConversationAudit(ChatAuditEventType.GROUP_CONVERSATION_RENAMED, currentUser, conversation, Map.of(
                "title", conversation.getTitle() == null ? "" : conversation.getTitle()
        ));
        return publishConversationMembershipUpdate(conversation, currentUser, Set.of());
    }

    @Transactional
    public ChatConversationSummaryDTO addGroupParticipants(Long conversationId, ChatConversationParticipantsRequestDTO request, AppUser currentUser) {
        ChatConversation conversation = requireManageableGroupConversation(conversationId, currentUser);
        List<AppUser> newParticipants = requireAdditionalGroupParticipants(conversation, currentUser, request);
        List<ParticipantSeed> seeds = effectiveParticipants(conversation).stream()
                .map(participant -> new ParticipantSeed(participant.getUser(), participant.getRole(), participant.getAddedBy()))
                .collect(Collectors.toCollection(ArrayList::new));
        newParticipants.forEach(user -> seeds.add(new ParticipantSeed(user, ChatConversationParticipantRole.MEMBER, currentUser)));
        syncConversationParticipants(conversation, seeds);
        recordConversationAudit(ChatAuditEventType.GROUP_PARTICIPANTS_ADDED, currentUser, conversation, Map.of(
                "participantUserIds", newParticipants.stream().map(AppUser::getId).toList()
        ));
        return publishConversationMembershipUpdate(conversation, currentUser, Set.of());
    }

    @Transactional
    public ChatConversationSummaryDTO updateGroupParticipantRole(
            Long conversationId,
            Long participantUserId,
            ChatConversationRoleRequestDTO request,
            AppUser currentUser
    ) {
        ChatConversation conversation = requireManageableGroupConversation(conversationId, currentUser);
        ChatConversationParticipant actorParticipant = requireParticipant(conversation, currentUser.getId());
        ChatConversationParticipant targetParticipant = requireParticipant(conversation, participantUserId);
        ChatConversationParticipantRole requestedRole = parseParticipantRole(request.getRole());

        if (Objects.equals(currentUser.getId(), participantUserId) && requestedRole != ChatConversationParticipantRole.OWNER) {
            throw ServiceErrors.badRequest("Use the leave conversation endpoint instead of changing your own role");
        }

        if (requestedRole == ChatConversationParticipantRole.OWNER) {
            requireConversationOwner(conversation, currentUser);
            if (Objects.equals(currentUser.getId(), participantUserId)) {
                throw ServiceErrors.badRequest("You already own this conversation");
            }
            ChatConversationParticipant currentOwnerParticipant = requireParticipant(conversation, currentUser.getId());
            currentOwnerParticipant.setRole(ChatConversationParticipantRole.ADMIN);
            targetParticipant.setRole(ChatConversationParticipantRole.OWNER);
            targetParticipant.setAddedBy(currentUser);
            conversation.setOwner(targetParticipant.getUser());
            recordConversationAudit(ChatAuditEventType.GROUP_OWNERSHIP_TRANSFERRED, currentUser, conversation, Map.of(
                    "fromUserId", currentUser.getId(),
                    "toUserId", targetParticipant.getUser().getId()
            ));
        } else {
            if (targetParticipant.getRole() == ChatConversationParticipantRole.OWNER) {
                throw ServiceErrors.badRequest("Transfer conversation ownership before changing the owner role");
            }
            if (actorParticipant.getRole() != ChatConversationParticipantRole.OWNER
                    && targetParticipant.getRole() == ChatConversationParticipantRole.ADMIN) {
                throw ServiceErrors.forbidden("Only the conversation owner can change another admin role");
            }
            if (actorParticipant.getRole() != ChatConversationParticipantRole.OWNER
                    && requestedRole == ChatConversationParticipantRole.ADMIN) {
                throw ServiceErrors.forbidden("Only the conversation owner can grant admin role");
            }
            targetParticipant.setRole(requestedRole);
            targetParticipant.setAddedBy(currentUser);
        }

        chatConversationRepository.save(conversation);
        recordConversationAudit(ChatAuditEventType.GROUP_PARTICIPANT_ROLE_UPDATED, currentUser, conversation, Map.of(
                "participantUserId", targetParticipant.getUser().getId(),
                "role", targetParticipant.getRole().name()
        ));
        return publishConversationMembershipUpdate(conversation, currentUser, Set.of());
    }

    @Transactional
    public ChatConversationSummaryDTO removeGroupParticipant(Long conversationId, Long participantUserId, AppUser currentUser) {
        ChatConversation conversation = requireManageableGroupConversation(conversationId, currentUser);
        ChatConversationParticipant actorParticipant = requireParticipant(conversation, currentUser.getId());
        ChatConversationParticipant targetParticipant = requireParticipant(conversation, participantUserId);
        if (Objects.equals(currentUser.getId(), participantUserId)) {
            throw ServiceErrors.badRequest("Use the leave conversation endpoint to remove yourself");
        }
        if (targetParticipant.getRole() == ChatConversationParticipantRole.OWNER) {
            throw ServiceErrors.badRequest("Transfer conversation ownership before removing the owner");
        }
        if (actorParticipant.getRole() != ChatConversationParticipantRole.OWNER
                && targetParticipant.getRole() == ChatConversationParticipantRole.ADMIN) {
            throw ServiceErrors.forbidden("Only the conversation owner can remove another admin");
        }
        ensureMinimumParticipantsAfterRemoval(conversation);
        Set<Long> removedUserIds = Set.of(targetParticipant.getUser().getId());
        conversation.getParticipants().removeIf(participant -> Objects.equals(participant.getUser().getId(), participantUserId));
        chatConversationRepository.save(conversation);
        recordConversationAudit(ChatAuditEventType.GROUP_PARTICIPANT_REMOVED, currentUser, conversation, Map.of(
                "participantUserId", participantUserId
        ));
        return publishConversationMembershipUpdate(conversation, currentUser, removedUserIds);
    }

    @Transactional
    public void leaveConversation(Long conversationId, AppUser currentUser) {
        ChatConversation conversation = requireAccessibleConversation(conversationId, currentUser);
        if (conversation.getConversationType() != ChatConversationType.GROUP) {
            throw ServiceErrors.badRequest("Only group conversations support leaving");
        }
        ChatConversationParticipant leavingParticipant = requireParticipant(conversation, currentUser.getId());
        ensureMinimumParticipantsAfterRemoval(conversation);
        if (leavingParticipant.getRole() == ChatConversationParticipantRole.OWNER) {
            ChatConversationParticipant replacementOwner = selectReplacementOwner(conversation, currentUser.getId());
            replacementOwner.setRole(ChatConversationParticipantRole.OWNER);
            replacementOwner.setAddedBy(currentUser);
            conversation.setOwner(replacementOwner.getUser());
        }
        conversation.getParticipants().removeIf(participant -> Objects.equals(participant.getUser().getId(), currentUser.getId()));
        chatConversationRepository.save(conversation);
        recordConversationAudit(ChatAuditEventType.GROUP_PARTICIPANT_LEFT, currentUser, conversation, Map.of(
                "participantUserId", currentUser.getId()
        ));
        publishConversationMembershipUpdate(conversation, currentUser, Set.of(currentUser.getId()));
    }

    @Transactional
    public ChatConversationSummaryDTO openCircleRoom(Long circleId, AppUser currentUser) {
        CircleGroup circle = requireCircleRoomAccess(circleId, currentUser);
        ChatConversation conversation = findOrCreateContextConversation(
                ChatConversationType.CIRCLE_ROOM,
                ChatConversationContextType.CIRCLE,
                circle.getId(),
                circle.getName() + " room",
                circle.getOwner(),
                currentUser
        );
        syncConversationParticipants(conversation, buildCircleRoomParticipantSeeds(circle));
        return openConversationSummary(conversation, currentUser);
    }

    @Transactional(readOnly = true)
    public ChatConversationSummaryDTO getCircleRoom(Long circleId, AppUser currentUser) {
        requireCircleRoomAccess(circleId, currentUser);
        return getExistingContextConversationSummary(ChatConversationContextType.CIRCLE, circleId, currentUser);
    }

    @Transactional
    public ChatConversationSummaryDTO openQuestThread(Long questId, AppUser currentUser) {
        Quest quest = workmarketQuestExecutionPrimitiveService.resolveTarget(questId);
        validateQuestThreadAccess(quest, currentUser);
        ChatConversation conversation = findOrCreateContextConversation(
                ChatConversationType.QUEST_THREAD,
                ChatConversationContextType.QUEST,
                quest.getId(),
                quest.getTitle(),
                quest.getCreator(),
                currentUser
        );
        syncConversationParticipants(conversation, buildQuestThreadParticipantSeeds(quest));
        return openConversationSummary(conversation, currentUser);
    }

    @Transactional(readOnly = true)
    public ChatConversationSummaryDTO getQuestThread(Long questId, AppUser currentUser) {
        Quest quest = workmarketQuestExecutionPrimitiveService.resolveTarget(questId);
        validateQuestThreadAccess(quest, currentUser);
        return getExistingContextConversationSummary(ChatConversationContextType.QUEST, questId, currentUser);
    }

    @Transactional
    public ChatConversationSummaryDTO openApplicationThread(Long applicationId, AppUser currentUser) {
        QuestApplication application = requireApplicationThreadAccess(applicationId, currentUser);
        ChatConversation conversation = findOrCreateContextConversation(
                ChatConversationType.APPLICATION_THREAD,
                ChatConversationContextType.APPLICATION,
                application.getId(),
                "Application: " + application.getQuest().getTitle(),
                application.getQuest().getCreator(),
                currentUser
        );
        syncConversationParticipants(conversation, buildApplicationThreadParticipantSeeds(application));
        return openConversationSummary(conversation, currentUser);
    }

    @Transactional(readOnly = true)
    public ChatConversationSummaryDTO getApplicationThread(Long applicationId, AppUser currentUser) {
        requireApplicationThreadAccess(applicationId, currentUser);
        return getExistingContextConversationSummary(ChatConversationContextType.APPLICATION, applicationId, currentUser);
    }

    @Transactional
    public ChatMessagePageDTO getConversationMessages(Long conversationId, Long beforeMessageId, Integer requestedLimit, AppUser currentUser) {
        ChatConversation conversation = requireAccessibleConversation(conversationId, currentUser);
        Instant now = Instant.now();
        chatConversationStateService.markConversationOpened(conversation, currentUser, now, false);
        int limit = normalizePageSize(requestedLimit);
        List<ChatMessage> page = chatMessageRepository.findDetailedPageByConversationId(conversation.getId(), beforeMessageId, PageRequest.of(0, limit + 1));
        boolean hasMore = page.size() > limit;
        if (hasMore) {
            page = page.subList(0, limit);
        }
        Collections.reverse(page);
        Map<Long, List<ChatMessageReaction>> reactionsByMessageId = groupReactionsByMessageId(page);
        List<ChatMessageDTO> messages = page.stream()
                .map(message -> toMessageDto(message, currentUser, reactionsByMessageId.getOrDefault(message.getId(), List.of())))
                .toList();
        Long nextBeforeMessageId = hasMore && !page.isEmpty() ? page.getLast().getId() : null;
        return ChatMessagePageDTO.builder()
                .messages(messages)
                .limit(limit)
                .hasMore(hasMore)
                .nextBeforeMessageId(nextBeforeMessageId)
                .build();
    }

    @Transactional(readOnly = true)
    public ChatConversationSyncDTO getConversationSync(Long conversationId, Long afterMessageId, Integer requestedLimit, AppUser currentUser) {
        ChatConversation conversation = requireAccessibleConversation(conversationId, currentUser);
        int limit = normalizePageSize(requestedLimit);
        Long effectiveAfterMessageId = afterMessageId != null && afterMessageId > 0 ? afterMessageId : null;
        List<ChatMessage> messages = effectiveAfterMessageId == null
                ? new ArrayList<>(chatMessageRepository.findLatestDetailedByConversationId(conversationId, PageRequest.of(0, limit)))
                : chatMessageRepository.findDetailedSinceMessageIdByConversationId(conversationId, effectiveAfterMessageId, PageRequest.of(0, limit));
        if (effectiveAfterMessageId == null) {
            Collections.reverse(messages);
        }
        Map<Long, List<ChatMessageReaction>> reactionsByMessageId = groupReactionsByMessageId(messages);
        Instant now = Instant.now();
        ChatConversationMemberState memberState = chatConversationStateService.getStatesByConversationId(List.of(conversationId), currentUser)
                .get(conversationId);
        return ChatConversationSyncDTO.builder()
                .conversation(toConversationSummary(
                        conversation,
                        currentUser,
                        unreadCount(conversation, currentUser),
                        memberState,
                        loadPresenceByUserId(otherParticipantIds(conversation, currentUser)),
                        now
                ))
                .messages(messages.stream()
                        .map(message -> toMessageDto(message, currentUser, reactionsByMessageId.getOrDefault(message.getId(), List.of())))
                        .toList())
                .afterMessageId(effectiveAfterMessageId)
                .latestMessageId(conversation.getLastMessageId())
                .activeTypingUserIds(chatRealtimeService.getActiveTypingUserIds(
                        conversationId,
                        currentUser.getId(),
                        chatProperties.getTyping().getTimeoutSeconds()
                ))
                .typingTimeoutSeconds(chatProperties.getTyping().getTimeoutSeconds())
                .build();
    }

    @Transactional
    public ChatAttachmentUploadDTO uploadAttachment(MultipartFile file, AppUser currentUser) {
        if (currentUser == null) {
            throw ServiceErrors.forbidden("Authentication is required");
        }
        ValidatedAttachment attachment = validateAndBuildAttachment(file);
        StoredObject storedObject = objectStorageService.store(
                buildAttachmentObjectKey(currentUser.getId(), attachment.name()),
                attachment.mimeType(),
                attachment.bytes()
        );
        ChatAttachmentUpload upload = new ChatAttachmentUpload();
        upload.setUploadedBy(currentUser);
        upload.setStorageProvider(storedObject.provider());
        upload.setStorageKey(storedObject.storageKey());
        upload.setAttachmentName(attachment.name());
        upload.setAttachmentMimeType(attachment.mimeType());
        upload.setAttachmentSizeBytes(attachment.sizeBytes());
        ChatAttachmentUpload savedUpload = chatAttachmentUploadRepository.save(upload);
        ObjectStorageAccess access = objectStorageService.resolve(savedUpload.getStorageKey());
        return ChatAttachmentUploadDTO.builder()
                .uploadId(savedUpload.getId())
                .attachmentName(attachment.name())
                .attachmentMimeType(attachment.mimeType())
                .attachmentSizeBytes(attachment.sizeBytes())
                .attachmentStorageProvider(storedObject.provider())
                .attachmentStorageKey(storedObject.storageKey())
                .attachmentUrl(access.url())
                .attachmentUrlExpiresAt(access.expiresAt() == null ? null : access.expiresAt().toString())
                .build();
    }

    @Transactional(readOnly = true)
    public ChatAttachmentStorageStatusDTO getAttachmentStorageStatus(AppUser currentUser) {
        if (currentUser == null) {
            throw ServiceErrors.forbidden("Authentication is required");
        }
        boolean enabled = objectStorageProperties.isEnabled();
        String provider = enabled ? objectStorageProperties.getProvider() : "disabled";
        String mode = enabled && "local".equalsIgnoreCase(provider) ? "local-disk" : enabled ? "external" : "disabled";
        return ChatAttachmentStorageStatusDTO.builder()
                .enabled(enabled)
                .provider(provider)
                .mode(mode)
                .endpoint(enabled && !"local".equalsIgnoreCase(provider) ? objectStorageProperties.getEndpoint() : objectStorageProperties.getLocalProxyBaseUrl())
                .bucket(enabled && !"local".equalsIgnoreCase(provider) ? objectStorageProperties.getBucket() : null)
                .localBasePath(enabled && "local".equalsIgnoreCase(provider) ? objectStorageProperties.getLocalBasePath() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public StoredObjectPayload getAttachmentObject(String storageKey, AppUser currentUser) {
        if (currentUser == null) {
            throw ServiceErrors.forbidden("Authentication is required");
        }
        String normalizedStorageKey = normalizeText(storageKey);
        if (normalizedStorageKey == null) {
            throw ServiceErrors.badRequest("Attachment storage key is required");
        }
        ChatMessage message = chatMessageRepository.findDetailedByAttachmentStorageKey(normalizedStorageKey)
                .orElse(null);
        if (message != null) {
            requireAccessibleConversation(message.getConversation().getId(), currentUser);
            return objectStorageService.load(normalizedStorageKey);
        }
        ChatAttachmentUpload upload = chatAttachmentUploadRepository.findByStorageKey(normalizedStorageKey)
                .orElseThrow(() -> ServiceErrors.notFound("Chat attachment object not found"));
        if (!Objects.equals(upload.getUploadedBy().getId(), currentUser.getId())) {
            throw ServiceErrors.forbidden("You are not allowed to access this chat attachment");
        }
        return objectStorageService.load(normalizedStorageKey);
    }

    @Transactional
    public ChatMessageDTO sendMessage(Long conversationId, ChatMessageRequestDTO request, AppUser currentUser) {
        ChatConversation conversation = requireAccessibleConversation(conversationId, currentUser);
        requireMessageContent(request);
        chatRateLimitService.assertWithinPerMinuteLimit(
                "chat-send",
                currentUser.getId().toString(),
                chatProperties.getMessages().getSendLimitPerMinute(),
                "Too many chat messages. Please wait a moment and try again."
        );

        String clientMessageId = normalizeClientMessageId(request.getClientMessageId());
        if (clientMessageId != null) {
            ChatMessage existing = chatMessageRepository.findByConversationIdAndSenderIdAndClientMessageId(conversationId, currentUser.getId(), clientMessageId)
                    .orElse(null);
            if (existing != null) {
                return toMessageDto(existing, currentUser);
            }
        }

        validateImagePayload(request.getImageDataUrl());
        ChatMessage replyToMessage = requireReplyTarget(conversation, request.getReplyToMessageId());
        ChatAttachmentUpload attachmentUpload = requireAttachmentUpload(request, currentUser);

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(currentUser);
        message.setMessageBody(normalizeText(request.getMessageBody()));
        message.setImageDataUrl(normalizeText(request.getImageDataUrl()));
        message.setAttachmentName(attachmentUpload == null ? null : attachmentUpload.getAttachmentName());
        message.setAttachmentMimeType(attachmentUpload == null ? null : attachmentUpload.getAttachmentMimeType());
        message.setAttachmentDataUrl(null);
        message.setAttachmentSizeBytes(attachmentUpload == null ? null : attachmentUpload.getAttachmentSizeBytes());
        message.setAttachmentStorageProvider(attachmentUpload == null ? null : attachmentUpload.getStorageProvider());
        message.setAttachmentStorageKey(attachmentUpload == null ? null : attachmentUpload.getStorageKey());
        message.setReplyToMessageId(replyToMessage == null ? null : replyToMessage.getId());
        message.setClientMessageId(clientMessageId);
        message.setUpdatedAt(Instant.now());
        ChatMessage saved;
        try {
            saved = chatMessageRepository.save(message);
        } catch (DataIntegrityViolationException exception) {
            if (clientMessageId == null) {
                throw exception;
            }
            saved = chatMessageRepository.findByConversationIdAndSenderIdAndClientMessageId(conversationId, currentUser.getId(), clientMessageId)
                    .orElseThrow(() -> exception);
        }
        if (attachmentUpload != null) {
            attachmentUpload.setConsumedMessageId(saved.getId());
            attachmentUpload.setConsumedAt(Instant.now());
            chatAttachmentUploadRepository.save(attachmentUpload);
        }

        refreshConversationPreview(conversation);
        Instant now = Instant.now();
        chatConversationStateService.markConversationOpened(conversation, currentUser, now, true);
        ChatMessageDTO messageDto = toMessageDto(saved, currentUser);
        Map<Long, ChatConversationSummaryDTO> summariesByUserId = buildRealtimeConversationSummaries(conversation, now);
        Map<Long, ChatMessageDTO> messageDtosByUserId = buildRealtimeMessageDtos(saved, conversation);
        chatRealtimeService.notifyMessageCreated(conversation, currentUser.getId(), summariesByUserId, messageDtosByUserId);
        return messageDto;
    }

    @Transactional
    public ChatMessageDTO addReaction(Long conversationId, Long messageId, ChatMessageReactionRequestDTO request, AppUser currentUser) {
        chatRateLimitService.assertWithinPerMinuteLimit(
                "chat-reaction",
                currentUser.getId().toString(),
                chatProperties.getModeration().getReactionLimitPerMinute(),
                "Too many chat reactions. Please wait a moment and try again."
        );
        ChatMessage message = requireAccessibleMessage(conversationId, messageId, currentUser);
        String emoji = normalizeEmoji(request == null ? null : request.getEmoji());
        chatMessageReactionRepository.findDetailedByMessageIdAndUserIdAndEmoji(messageId, currentUser.getId(), emoji)
                .orElseGet(() -> {
                    ChatMessageReaction reaction = new ChatMessageReaction();
                    reaction.setMessage(message);
                    reaction.setUser(currentUser);
                    reaction.setEmoji(emoji);
                    reaction.setCreatedAt(Instant.now());
                    return chatMessageReactionRepository.save(reaction);
                });
        recordConversationAudit(ChatAuditEventType.MESSAGE_REACTION_ADDED, currentUser, message.getConversation(), Map.of(
                "messageId", messageId,
                "emoji", emoji
        ));
        ChatMessageDTO dto = toMessageDto(message, currentUser);
        Map<Long, ChatConversationSummaryDTO> summariesByUserId = buildRealtimeConversationSummaries(message.getConversation(), Instant.now());
        Map<Long, ChatMessageDTO> messageDtosByUserId = buildRealtimeMessageDtos(message, message.getConversation());
        chatRealtimeService.notifyMessageUpdated(message.getConversation(), currentUser.getId(), summariesByUserId, messageDtosByUserId);
        return dto;
    }

    @Transactional
    public ChatMessageDTO removeReaction(Long conversationId, Long messageId, String emoji, AppUser currentUser) {
        ChatMessage message = requireAccessibleMessage(conversationId, messageId, currentUser);
        String normalizedEmoji = normalizeEmoji(emoji);
        chatMessageReactionRepository.findDetailedByMessageIdAndUserIdAndEmoji(messageId, currentUser.getId(), normalizedEmoji)
                .ifPresent(chatMessageReactionRepository::delete);
        recordConversationAudit(ChatAuditEventType.MESSAGE_REACTION_REMOVED, currentUser, message.getConversation(), Map.of(
                "messageId", messageId,
                "emoji", normalizedEmoji
        ));
        ChatMessageDTO dto = toMessageDto(message, currentUser);
        Map<Long, ChatConversationSummaryDTO> summariesByUserId = buildRealtimeConversationSummaries(message.getConversation(), Instant.now());
        Map<Long, ChatMessageDTO> messageDtosByUserId = buildRealtimeMessageDtos(message, message.getConversation());
        chatRealtimeService.notifyMessageUpdated(message.getConversation(), currentUser.getId(), summariesByUserId, messageDtosByUserId);
        return dto;
    }

    @Transactional
    public ChatMessageDTO updateMessage(Long conversationId, Long messageId, ChatMessageUpdateRequestDTO request, AppUser currentUser) {
        ChatConversation conversation = requireAccessibleConversation(conversationId, currentUser);
        ChatMessage message = requireOwnMessage(conversation.getId(), messageId, currentUser);
        if (message.getDeletedAt() != null) {
            throw ServiceErrors.badRequest("Deleted chat messages cannot be edited");
        }
        Instant now = Instant.now();
        if (message.getCreatedAt().isBefore(now.minusSeconds(chatProperties.getMessages().getEditWindowSeconds()))) {
            throw ServiceErrors.badRequest("Chat message edit window has expired");
        }

        message.setMessageBody(normalizeText(request.getMessageBody()));
        message.setEditedAt(now);
        message.setUpdatedAt(now);
        ChatMessage saved = chatMessageRepository.save(message);
        refreshConversationPreview(conversation);
        chatConversationStateService.markConversationOpened(conversation, currentUser, now, false);
        ChatMessageDTO messageDto = toMessageDto(saved, currentUser);
        Map<Long, ChatConversationSummaryDTO> summariesByUserId = buildRealtimeConversationSummaries(conversation, now);
        Map<Long, ChatMessageDTO> messageDtosByUserId = buildRealtimeMessageDtos(saved, conversation);
        chatRealtimeService.notifyMessageUpdated(conversation, currentUser.getId(), summariesByUserId, messageDtosByUserId);
        return messageDto;
    }

    @Transactional
    public void deleteMessage(Long conversationId, Long messageId, AppUser currentUser) {
        ChatConversation conversation = requireAccessibleConversation(conversationId, currentUser);
        ChatMessage message = requireOwnMessage(conversation.getId(), messageId, currentUser);
        if (message.getDeletedAt() != null) {
            return;
        }
        Instant now = Instant.now();
        if (message.getCreatedAt().isBefore(now.minusSeconds(chatProperties.getMessages().getDeleteWindowSeconds()))) {
            throw ServiceErrors.badRequest("Chat message delete window has expired");
        }

        message.setMessageBody(null);
        message.setImageDataUrl(null);
        message.setAttachmentDataUrl(null);
        message.setAttachmentName(null);
        message.setAttachmentMimeType(null);
        message.setAttachmentSizeBytes(null);
        message.setAttachmentStorageProvider(null);
        message.setAttachmentStorageKey(null);
        message.setDeletedAt(now);
        message.setUpdatedAt(now);
        ChatMessage saved = chatMessageRepository.save(message);
        refreshConversationPreview(conversation);
        chatConversationStateService.markConversationOpened(conversation, currentUser, now, false);
        Map<Long, ChatConversationSummaryDTO> summariesByUserId = buildRealtimeConversationSummaries(conversation, now);
        Map<Long, ChatMessageDTO> messageDtosByUserId = buildRealtimeMessageDtos(saved, conversation);
        chatRealtimeService.notifyMessageDeleted(conversation, currentUser.getId(), summariesByUserId, messageDtosByUserId);
    }

    @Transactional
    public ChatConversationSummaryDTO updateConversationState(Long conversationId, ChatConversationStateRequestDTO request, AppUser currentUser) {
        ChatConversation conversation = requireAccessibleConversation(conversationId, currentUser);
        Instant now = Instant.now();
        ChatConversationMemberState memberState = chatConversationStateService.updateConversationState(conversation, currentUser, request, now);
        ChatConversationSummaryDTO summary = toConversationSummary(
                conversation,
                currentUser,
                unreadCount(conversation, currentUser),
                memberState,
                loadPresenceByUserId(otherParticipantIds(conversation, currentUser)),
                now
        );
        Map<Long, ChatConversationSummaryDTO> summariesByUserId = buildRealtimeConversationSummaries(conversation, now);
        chatRealtimeService.notifyConversationStateUpdated(conversation, currentUser.getId(), summariesByUserId);
        return summary;
    }

    @Transactional
    public void markConversationDelivered(Long conversationId, ChatReceiptRequestDTO request, AppUser currentUser) {
        chatRateLimitService.assertWithinPerMinuteLimit(
                "chat-delivery",
                currentUser.getId().toString(),
                chatProperties.getMessages().getDeliveryLimitPerMinute(),
                "Too many chat delivery updates. Please wait a moment and try again."
        );
        ChatConversation conversation = requireAccessibleConversation(conversationId, currentUser);
        Instant now = Instant.now();
        Long upToMessageId = request == null ? null : request.getUpToMessageId();
        Long effectiveDeliveredUpToMessageId = chatConversationStateService.markDelivered(conversation, currentUser, upToMessageId, now);
        if (effectiveDeliveredUpToMessageId != null) {
            chatConversationStateService.markConversationOpened(conversation, currentUser, now, false);
            Map<Long, ChatConversationSummaryDTO> summariesByUserId = buildRealtimeConversationSummaries(conversation, now);
            chatRealtimeService.notifyConversationDelivered(conversation, currentUser.getId(), summariesByUserId, effectiveDeliveredUpToMessageId);
        }
    }

    @Transactional
    public void markConversationRead(Long conversationId, ChatMarkReadRequestDTO request, AppUser currentUser) {
        chatRateLimitService.assertWithinPerMinuteLimit(
                "chat-read",
                currentUser.getId().toString(),
                chatProperties.getMessages().getReadLimitPerMinute(),
                "Too many chat read updates. Please wait a moment and try again."
        );
        ChatConversation conversation = requireAccessibleConversation(conversationId, currentUser);
        Instant now = Instant.now();
        Long upToMessageId = request == null ? null : request.getUpToMessageId();
        Long effectiveSeenUpToMessageId = chatConversationStateService.markSeen(conversation, currentUser, upToMessageId, now);
        if (effectiveSeenUpToMessageId != null) {
            chatConversationStateService.markConversationOpened(conversation, currentUser, now, false);
            Map<Long, ChatConversationSummaryDTO> summariesByUserId = buildRealtimeConversationSummaries(conversation, now);
            chatRealtimeService.notifyConversationSeen(conversation, currentUser.getId(), summariesByUserId, effectiveSeenUpToMessageId);
        }
    }

    @Transactional
    public void heartbeat(AppUser currentUser) {
        chatRateLimitService.assertWithinPerMinuteLimit(
                "chat-heartbeat",
                currentUser.getId().toString(),
                chatProperties.getPresence().getHeartbeatLimitPerMinute(),
                "Too many chat presence updates. Please wait a moment and try again."
        );
        chatPresenceService.markActive(currentUser);
    }

    @Transactional
    public void updateTypingState(Long conversationId, boolean typing, AppUser currentUser) {
        chatRateLimitService.assertWithinPerMinuteLimit(
                "chat-typing",
                currentUser.getId().toString(),
                chatProperties.getTyping().getUpdateLimitPerMinute(),
                "Too many chat typing updates. Please wait a moment and try again."
        );
        ChatConversation conversation = requireAccessibleConversation(conversationId, currentUser);
        chatRealtimeService.notifyTypingChanged(conversation, currentUser.getId(), typing);
    }

    private ChatConversationSummaryDTO openConversationSummary(ChatConversation conversation, AppUser currentUser) {
        Instant now = Instant.now();
        ChatConversationMemberState memberState = chatConversationStateService.markConversationOpened(conversation, currentUser, now, true);
        return toConversationSummary(
                conversation,
                currentUser,
                unreadCount(conversation, currentUser),
                memberState,
                loadPresenceByUserId(otherParticipantIds(conversation, currentUser)),
                now
        );
    }

    private ChatConversation findOrCreateDirectConversation(AppUser currentUser, AppUser otherUser) {
        Long leftId = normalizedLeftId(currentUser.getId(), otherUser.getId());
        Long rightId = normalizedRightId(currentUser.getId(), otherUser.getId());
        return chatConversationRepository.findByLeftParticipantIdAndRightParticipantIdAndConversationType(leftId, rightId, ChatConversationType.DIRECT)
                .orElseGet(() -> {
                    try {
                        return createDirectConversation(currentUser, otherUser);
                    } catch (DataIntegrityViolationException exception) {
                        return chatConversationRepository.findByLeftParticipantIdAndRightParticipantIdAndConversationType(leftId, rightId, ChatConversationType.DIRECT)
                                .orElseThrow(() -> exception);
                    }
                });
    }

    private ChatConversation createDirectConversation(AppUser currentUser, AppUser otherUser) {
        ChatConversation conversation = new ChatConversation();
        AppUser left = currentUser.getId() < otherUser.getId() ? currentUser : otherUser;
        AppUser right = currentUser.getId() < otherUser.getId() ? otherUser : currentUser;
        conversation.setConversationType(ChatConversationType.DIRECT);
        conversation.setLeftParticipant(left);
        conversation.setRightParticipant(right);
        conversation.setOwner(currentUser);
        conversation.setCreatedBy(currentUser);
        conversation = chatConversationRepository.save(conversation);
        syncConversationParticipants(conversation, List.of(
                new ParticipantSeed(left, ChatConversationParticipantRole.OWNER, currentUser),
                new ParticipantSeed(right, ChatConversationParticipantRole.MEMBER, currentUser)
        ));
        return conversation;
    }

    private ChatConversation findOrCreateContextConversation(
            ChatConversationType conversationType,
            ChatConversationContextType contextType,
            Long contextId,
            String title,
            AppUser owner,
            AppUser createdBy
    ) {
        ChatConversation existing = chatConversationRepository.findByContextTypeAndContextId(contextType, contextId).orElse(null);
        if (existing != null) {
            existing.setConversationType(conversationType);
            existing.setTitle(normalizeConversationTitle(title));
            existing.setOwner(owner);
            if (existing.getCreatedBy() == null) {
                existing.setCreatedBy(createdBy);
            }
            return chatConversationRepository.save(existing);
        }

        ChatConversation conversation = new ChatConversation();
        conversation.setConversationType(conversationType);
        conversation.setContextType(contextType);
        conversation.setContextId(contextId);
        conversation.setTitle(normalizeConversationTitle(title));
        conversation.setOwner(owner);
        conversation.setCreatedBy(createdBy);
        return chatConversationRepository.save(conversation);
    }

    private ChatConversationSummaryDTO getExistingContextConversationSummary(
            ChatConversationContextType contextType,
            Long contextId,
            AppUser currentUser
    ) {
        ChatConversation conversation = chatConversationRepository.findDetailedByContextTypeAndContextId(contextType, contextId)
                .orElseThrow(() -> ServiceErrors.notFound("Chat conversation not found for requested context"));
        if (!canAccessConversation(currentUser, conversation)) {
            throw ServiceErrors.forbidden("You are not allowed to access this chat conversation");
        }
        return toConversationSummary(
                conversation,
                currentUser,
                unreadCount(conversation, currentUser),
                chatConversationStateService.getStatesByConversationId(List.of(conversation.getId()), currentUser).get(conversation.getId()),
                loadPresenceByUserId(otherParticipantIds(conversation, currentUser)),
                Instant.now()
        );
    }

    private boolean matchesConversationQuery(ChatConversation conversation, AppUser currentUser, String normalizedQuery) {
        if (normalizedQuery == null) {
            return true;
        }
        String query = normalizedQuery.toLowerCase();
        AppUser otherUser = conversation.getConversationType() == ChatConversationType.DIRECT
                ? otherParticipant(conversation, currentUser)
                : null;
        String title = resolveConversationTitle(conversation, currentUser, otherUser, effectiveParticipants(conversation));
        if (title != null && title.toLowerCase().contains(query)) {
            return true;
        }
        return effectiveParticipants(conversation).stream()
                .map(ChatConversationParticipant::getUser)
                .filter(Objects::nonNull)
                .map(AppUser::getUsername)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .anyMatch(username -> username.contains(query));
    }

    private CircleGroup requireCircleRoomAccess(Long circleId, AppUser currentUser) {
        CircleGroup circle = circleGroupRepository.findById(circleId)
                .orElseThrow(() -> ServiceErrors.notFound("Circle not found with id " + circleId));
        if (Objects.equals(circle.getOwner().getId(), currentUser.getId())) {
            return circle;
        }
        if (circleMembershipService.isCircleMember(circleId, currentUser.getId())) {
            return circle;
        }
        throw ServiceErrors.forbidden("You are not allowed to access this circle room");
    }

    private void validateQuestThreadAccess(Quest quest, AppUser currentUser) {
        if (workmarketQuestAccessPolicyService.canManageQuest(quest, currentUser)) {
            return;
        }
        boolean approvedApplicant = workmarketQuestApplicationRepository.findByQuestIdAndApplicantIdAndStatus(
                quest.getId(),
                currentUser.getId(),
                QuestApplicationStatus.APPROVED
        ).isPresent();
        if (!approvedApplicant) {
            throw ServiceErrors.forbidden("You are not allowed to access this quest thread");
        }
    }

    private QuestApplication requireApplicationThreadAccess(Long applicationId, AppUser currentUser) {
        QuestApplication application = workmarketQuestApplicationRepository.findByIdDetailed(applicationId)
                .orElseThrow(() -> ServiceErrors.notFound("Quest application not found with id " + applicationId));
        if (!workmarketQuestAccessPolicyService.canViewQuestApplication(application, application.getQuest(), currentUser)) {
            throw ServiceErrors.forbidden("You are not allowed to access this application thread");
        }
        return application;
    }

    private List<ParticipantSeed> buildGroupParticipantSeeds(AppUser currentUser, List<AppUser> otherParticipants) {
        List<ParticipantSeed> seeds = new ArrayList<>();
        seeds.add(new ParticipantSeed(currentUser, ChatConversationParticipantRole.OWNER, currentUser));
        otherParticipants.forEach(user -> seeds.add(new ParticipantSeed(user, ChatConversationParticipantRole.MEMBER, currentUser)));
        return seeds;
    }

    private List<ParticipantSeed> buildCircleRoomParticipantSeeds(CircleGroup circle) {
        CircleGroup detailedCircle = circleGroupRepository.findAllDetailedByOwnerId(circle.getOwner().getId()).stream()
                .filter(candidate -> Objects.equals(candidate.getId(), circle.getId()))
                .findFirst()
                .orElse(circle);
        List<ParticipantSeed> seeds = new ArrayList<>();
        seeds.add(new ParticipantSeed(detailedCircle.getOwner(), ChatConversationParticipantRole.OWNER, detailedCircle.getOwner()));
        detailedCircle.getMemberships().stream()
                .map(CircleMembership::getMember)
                .filter(member -> !Objects.equals(member.getId(), detailedCircle.getOwner().getId()))
                .sorted(Comparator.comparing(AppUser::getId))
                .forEach(member -> seeds.add(new ParticipantSeed(member, ChatConversationParticipantRole.MEMBER, detailedCircle.getOwner())));
        return seeds;
    }

    private List<ParticipantSeed> buildQuestThreadParticipantSeeds(Quest quest) {
        List<ParticipantSeed> seeds = new ArrayList<>();
        seeds.add(new ParticipantSeed(quest.getCreator(), ChatConversationParticipantRole.OWNER, quest.getCreator()));
        workmarketQuestApplicationRepository.findByQuestIdAndStatus(quest.getId(), QuestApplicationStatus.APPROVED).stream()
                .map(QuestApplication::getApplicant)
                .filter(applicant -> !Objects.equals(applicant.getId(), quest.getCreator().getId()))
                .sorted(Comparator.comparing(AppUser::getId))
                .forEach(applicant -> seeds.add(new ParticipantSeed(applicant, ChatConversationParticipantRole.MEMBER, quest.getCreator())));
        return seeds;
    }

    private List<ParticipantSeed> buildApplicationThreadParticipantSeeds(QuestApplication application) {
        return List.of(
                new ParticipantSeed(application.getQuest().getCreator(), ChatConversationParticipantRole.OWNER, application.getQuest().getCreator()),
                new ParticipantSeed(application.getApplicant(), ChatConversationParticipantRole.MEMBER, application.getQuest().getCreator())
        );
    }

    private void syncConversationParticipants(ChatConversation conversation, List<ParticipantSeed> seeds) {
        Map<Long, ChatConversationParticipant> existingByUserId = effectiveParticipants(conversation).stream()
                .collect(Collectors.toMap(participant -> participant.getUser().getId(), participant -> participant, (left, right) -> left, LinkedHashMap::new));
        Set<Long> requestedUserIds = seeds.stream()
                .map(seed -> seed.user().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (ChatConversationParticipant participant : List.copyOf(conversation.getParticipants())) {
            if (!requestedUserIds.contains(participant.getUser().getId())) {
                conversation.getParticipants().remove(participant);
            }
        }

        for (ParticipantSeed seed : seeds) {
            ChatConversationParticipant participant = existingByUserId.get(seed.user().getId());
            if (participant == null || participant.getId() == null) {
                participant = chatConversationParticipantRepository.findByConversationIdAndUserId(conversation.getId(), seed.user().getId())
                        .orElseGet(ChatConversationParticipant::new);
                participant.setConversation(conversation);
                participant.setUser(seed.user());
                if (participant.getJoinedAt() == null) {
                    participant.setJoinedAt(Instant.now());
                }
                conversation.getParticipants().add(participant);
            }
            participant.setRole(seed.role());
            participant.setAddedBy(seed.addedBy());
        }

        chatConversationRepository.save(conversation);
    }

    private List<AppUser> requireGroupParticipants(AppUser currentUser, ChatCreateGroupConversationRequestDTO request) {
        if (request == null || request.getParticipantUserIds() == null || request.getParticipantUserIds().isEmpty()) {
            throw ServiceErrors.badRequest("Group chat participants are required");
        }
        Set<Long> participantIds = request.getParticipantUserIds().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (participantIds.contains(currentUser.getId())) {
            throw ServiceErrors.badRequest("Do not include yourself in the group chat participant list");
        }
        if (participantIds.size() < 2) {
            throw ServiceErrors.badRequest("Group chat must include at least two other participants");
        }
        List<AppUser> participants = participantIds.stream()
                .map(appUserLookupService::requireById)
                .toList();
        boolean allContacts = participants.stream().allMatch(user -> isCurrentChatContact(currentUser, user));
        if (!allContacts) {
            throw ServiceErrors.forbidden("You can only start group chats with people in your circles");
        }
        return participants;
    }

    private List<AppUser> requireAdditionalGroupParticipants(
            ChatConversation conversation,
            AppUser currentUser,
            ChatConversationParticipantsRequestDTO request
    ) {
        if (request == null || request.getParticipantUserIds() == null || request.getParticipantUserIds().isEmpty()) {
            throw ServiceErrors.badRequest("Group chat participants are required");
        }
        Set<Long> existingParticipantIds = effectiveParticipants(conversation).stream()
                .map(ChatConversationParticipant::getUser)
                .map(AppUser::getId)
                .collect(Collectors.toSet());
        Set<Long> requestedParticipantIds = request.getParticipantUserIds().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (requestedParticipantIds.isEmpty()) {
            throw ServiceErrors.badRequest("Group chat participants are required");
        }
        if (requestedParticipantIds.contains(currentUser.getId())) {
            throw ServiceErrors.badRequest("You are already part of this group conversation");
        }
        List<AppUser> participants = requestedParticipantIds.stream()
                .filter(userId -> !existingParticipantIds.contains(userId))
                .map(appUserLookupService::requireById)
                .toList();
        if (participants.isEmpty()) {
            throw ServiceErrors.badRequest("All selected users are already in this group conversation");
        }
        boolean allContacts = participants.stream().allMatch(user -> isCurrentChatContact(currentUser, user));
        if (!allContacts) {
            throw ServiceErrors.forbidden("You can only add people from your circles");
        }
        return participants;
    }

    private AppUser requireChatCounterpart(AppUser currentUser, Long otherUserId) {
        if (otherUserId == null) {
            throw ServiceErrors.badRequest("Other user is required");
        }
        if (Objects.equals(currentUser.getId(), otherUserId)) {
            throw ServiceErrors.badRequest("You cannot start a chat with yourself");
        }
        AppUser otherUser = appUserLookupService.requireById(otherUserId);
        if (!circleRelationService.isCircleBetween(currentUser, otherUser)) {
            throw ServiceErrors.forbidden("You can only chat with people in your circles");
        }
        return otherUser;
    }

    private boolean isCurrentChatContact(AppUser currentUser, AppUser otherUser) {
        return otherUser != null && circleRelationService.isCircleBetween(currentUser, otherUser);
    }

    private ChatConversation requireManageableGroupConversation(Long conversationId, AppUser currentUser) {
        ChatConversation conversation = requireAccessibleConversation(conversationId, currentUser);
        if (conversation.getConversationType() != ChatConversationType.GROUP) {
            throw ServiceErrors.badRequest("Only group conversations support participant management");
        }
        ChatConversationParticipant actorParticipant = requireParticipant(conversation, currentUser.getId());
        if (actorParticipant.getRole() != ChatConversationParticipantRole.OWNER
                && actorParticipant.getRole() != ChatConversationParticipantRole.ADMIN) {
            throw ServiceErrors.forbidden("You are not allowed to manage this group conversation");
        }
        return conversation;
    }

    private void requireConversationOwner(ChatConversation conversation, AppUser currentUser) {
        if (conversation.getOwner() == null || !Objects.equals(conversation.getOwner().getId(), currentUser.getId())) {
            throw ServiceErrors.forbidden("Only the conversation owner can perform this action");
        }
    }

    private ChatConversation requireAccessibleConversation(Long conversationId, AppUser currentUser) {
        ChatConversation conversation = chatConversationRepository.findDetailedById(conversationId)
                .orElseThrow(() -> ServiceErrors.notFound("Chat conversation not found with id " + conversationId));
        if (!isParticipant(conversation, currentUser.getId())) {
            throw ServiceErrors.forbidden("You can only access your own chat conversations");
        }
        if (!canAccessConversation(currentUser, conversation)) {
            throw ServiceErrors.forbidden("You are not allowed to access this chat conversation");
        }
        return conversation;
    }

    private boolean canAccessConversation(AppUser currentUser, ChatConversation conversation) {
        if (!isParticipant(conversation, currentUser.getId())) {
            return false;
        }
        return switch (conversation.getConversationType()) {
            case DIRECT -> {
                AppUser otherUser = otherParticipant(conversation, currentUser);
                yield otherUser != null && circleRelationService.isCircleBetween(currentUser, otherUser);
            }
            case GROUP -> true;
            case CIRCLE_ROOM -> canAccessCircleConversation(currentUser, conversation);
            case QUEST_THREAD -> canAccessQuestConversation(currentUser, conversation);
            case APPLICATION_THREAD -> canAccessApplicationConversation(currentUser, conversation);
        };
    }

    private boolean canAccessCircleConversation(AppUser currentUser, ChatConversation conversation) {
        Long circleId = conversation.getContextId();
        if (circleId == null) {
            return false;
        }
        CircleGroup circle = circleGroupRepository.findById(circleId).orElse(null);
        if (circle == null) {
            return false;
        }
        return Objects.equals(circle.getOwner().getId(), currentUser.getId())
                || circleMembershipService.isCircleMember(circleId, currentUser.getId());
    }

    private boolean canAccessQuestConversation(AppUser currentUser, ChatConversation conversation) {
        Long questId = conversation.getContextId();
        if (questId == null) {
            return false;
        }
        Quest quest;
        try {
            quest = workmarketQuestExecutionPrimitiveService.resolveTarget(questId);
        } catch (RuntimeException exception) {
            return false;
        }
        if (workmarketQuestAccessPolicyService.canManageQuest(quest, currentUser)) {
            return true;
        }
        return workmarketQuestApplicationRepository.findByQuestIdAndApplicantIdAndStatus(
                questId,
                currentUser.getId(),
                QuestApplicationStatus.APPROVED
        ).isPresent();
    }

    private boolean canAccessApplicationConversation(AppUser currentUser, ChatConversation conversation) {
        Long applicationId = conversation.getContextId();
        if (applicationId == null) {
            return false;
        }
        QuestApplication application = workmarketQuestApplicationRepository.findByIdDetailed(applicationId).orElse(null);
        return application != null
                && workmarketQuestAccessPolicyService.canViewQuestApplication(application, application.getQuest(), currentUser);
    }

    private boolean isParticipant(ChatConversation conversation, Long userId) {
        return effectiveParticipants(conversation).stream()
                .map(ChatConversationParticipant::getUser)
                .filter(Objects::nonNull)
                .anyMatch(user -> Objects.equals(user.getId(), userId));
    }

    private ChatConversationParticipant requireParticipant(ChatConversation conversation, Long userId) {
        return effectiveParticipants(conversation).stream()
                .filter(participant -> Objects.equals(participant.getUser().getId(), userId))
                .findFirst()
                .orElseThrow(() -> ServiceErrors.notFound("Chat participant not found with user id " + userId));
    }

    private ChatConversationParticipantRole parseParticipantRole(String role) {
        String normalized = normalizeText(role);
        if (normalized == null) {
            throw ServiceErrors.badRequest("Participant role is required");
        }
        try {
            return ChatConversationParticipantRole.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw ServiceErrors.badRequest("Unsupported participant role");
        }
    }

    private ChatConversationType parseConversationType(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            return null;
        }
        try {
            return ChatConversationType.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw ServiceErrors.badRequest("Unsupported conversation type");
        }
    }

    private ChatConversationContextType parseConversationContextType(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            return null;
        }
        try {
            return ChatConversationContextType.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw ServiceErrors.badRequest("Unsupported conversation context type");
        }
    }

    private List<ChatConversationParticipant> effectiveParticipants(ChatConversation conversation) {
        if (conversation.getParticipants() != null && !conversation.getParticipants().isEmpty()) {
            return conversation.getParticipants().stream()
                    .filter(participant -> participant.getUser() != null)
                    .sorted(Comparator.comparing(participant -> participant.getUser().getId()))
                    .toList();
        }
        List<ChatConversationParticipant> participants = new ArrayList<>();
        if (conversation.getLeftParticipant() != null) {
            ChatConversationParticipant left = new ChatConversationParticipant();
            left.setConversation(conversation);
            left.setUser(conversation.getLeftParticipant());
            left.setRole(ChatConversationParticipantRole.OWNER);
            participants.add(left);
        }
        if (conversation.getRightParticipant() != null) {
            ChatConversationParticipant right = new ChatConversationParticipant();
            right.setConversation(conversation);
            right.setUser(conversation.getRightParticipant());
            right.setRole(ChatConversationParticipantRole.MEMBER);
            participants.add(right);
        }
        return participants;
    }

    private void ensureMinimumParticipantsAfterRemoval(ChatConversation conversation) {
        if (effectiveParticipants(conversation).size() <= 2) {
            throw ServiceErrors.badRequest("Group conversations must keep at least two participants");
        }
    }

    private ChatConversationParticipant selectReplacementOwner(ChatConversation conversation, Long excludedUserId) {
        return effectiveParticipants(conversation).stream()
                .filter(participant -> !Objects.equals(participant.getUser().getId(), excludedUserId))
                .min(Comparator.comparingInt(this::participantOwnershipPriority)
                        .thenComparing(participant -> participant.getUser().getId()))
                .orElseThrow(() -> ServiceErrors.badRequest("Conversation owner transfer requires another participant"));
    }

    private int participantOwnershipPriority(ChatConversationParticipant participant) {
        return switch (participant.getRole()) {
            case ADMIN -> 0;
            case MEMBER -> 1;
            case OWNER -> 2;
        };
    }

    private AppUser otherParticipant(ChatConversation conversation, AppUser currentUser) {
        return effectiveParticipants(conversation).stream()
                .map(ChatConversationParticipant::getUser)
                .filter(user -> !Objects.equals(user.getId(), currentUser.getId()))
                .findFirst()
                .orElse(null);
    }

    private List<Long> otherParticipantIds(ChatConversation conversation, AppUser currentUser) {
        return effectiveParticipants(conversation).stream()
                .map(ChatConversationParticipant::getUser)
                .filter(user -> !Objects.equals(user.getId(), currentUser.getId()))
                .map(AppUser::getId)
                .distinct()
                .toList();
    }

    private ChatConversationSummaryDTO toConversationSummary(
            ChatConversation conversation,
            AppUser currentUser,
            long unreadCount,
            ChatConversationMemberState memberState,
            Map<Long, ChatPresence> presenceByUserId,
            Instant now
    ) {
        AppUser otherUser = conversation.getConversationType() == ChatConversationType.DIRECT
                ? otherParticipant(conversation, currentUser)
                : null;
        List<ChatConversationParticipant> participants = effectiveParticipants(conversation);
        List<ChatConversationParticipantDTO> participantDtos = participants.stream()
                .map(participant -> toConversationParticipantDto(participant, presenceByUserId.get(participant.getUser().getId()), now))
                .toList();
        ChatConversationParticipant selfParticipant = participants.stream()
                .filter(participant -> Objects.equals(participant.getUser().getId(), currentUser.getId()))
                .findFirst()
                .orElse(null);
        String title = resolveConversationTitle(conversation, currentUser, otherUser, participants);
        return ChatConversationSummaryDTO.builder()
                .conversationId(conversation.getId())
                .conversationType(conversation.getConversationType().name())
                .contextType(conversation.getContextType() == null ? null : conversation.getContextType().name())
                .contextId(conversation.getContextId())
                .title(title)
                .ownerUserId(conversation.getOwner() == null ? null : conversation.getOwner().getId())
                .participantCount(participants.size())
                .canManageParticipants(selfParticipant != null && (selfParticipant.getRole() == ChatConversationParticipantRole.OWNER
                        || selfParticipant.getRole() == ChatConversationParticipantRole.ADMIN))
                .participants(participantDtos)
                .otherUserId(otherUser == null ? null : otherUser.getId())
                .otherUsername(otherUser == null ? null : otherUser.getUsername())
                .resolutionKey(resolveConversationResolutionKey(conversation))
                .resolutionLabel(title)
                .exactResolutionEligible(true)
                .otherUserProfileDescription(otherUser == null ? null : otherUser.getProfileDescription())
                .otherUserAvatarDataUrl(otherUser == null ? null : otherUser.getProfileAvatarDataUrl())
                .otherUserOnline(otherUser != null && isOnline(presenceByUserId.get(otherUser.getId()), now))
                .otherUserLastActiveAt(otherUser == null || presenceByUserId.get(otherUser.getId()) == null
                        ? null
                        : presenceByUserId.get(otherUser.getId()).getLastActiveAt().toString())
                .lastMessageId(conversation.getLastMessageId())
                .lastMessagePreview(conversation.getLastMessagePreview())
                .lastMessageAt(conversation.getLastMessageAt() == null ? null : conversation.getLastMessageAt().toString())
                .lastMessageFromCurrentUser(conversation.getLastMessageSender() != null
                        && Objects.equals(conversation.getLastMessageSender().getId(), currentUser.getId()))
                .lastMessageHasImage(conversation.isLastMessageHasImage())
                .lastMessageDeleted(conversation.isLastMessageDeleted())
                .muted(chatConversationStateService.isMuted(memberState, now))
                .mutedUntil(memberState == null || memberState.getMutedUntil() == null ? null : memberState.getMutedUntil().toString())
                .archived(chatConversationStateService.isArchived(memberState))
                .archivedAt(memberState == null || memberState.getArchivedAt() == null ? null : memberState.getArchivedAt().toString())
                .lastDeliveredMessageId(memberState == null ? null : memberState.getLastDeliveredMessageId())
                .lastSeenMessageId(memberState == null ? null : memberState.getLastSeenMessageId())
                .unreadCount(unreadCount)
                .build();
    }

    private String resolveConversationTitle(
            ChatConversation conversation,
            AppUser currentUser,
            AppUser otherUser,
            List<ChatConversationParticipant> participants
    ) {
        if (conversation.getConversationType() == ChatConversationType.DIRECT && otherUser != null) {
            return "Chat with " + otherUser.getUsername();
        }
        String title = normalizeConversationTitle(conversation.getTitle());
        if (title != null) {
            return title;
        }
        List<String> participantNames = participants.stream()
                .map(ChatConversationParticipant::getUser)
                .filter(user -> !Objects.equals(user.getId(), currentUser.getId()))
                .map(AppUser::getUsername)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        return participantNames.isEmpty() ? "Chat" : String.join(", ", participantNames);
    }

    private String resolveConversationResolutionKey(ChatConversation conversation) {
        if (conversation.getContextType() != null && conversation.getContextId() != null) {
            return "chat-context:" + conversation.getContextType().name().toLowerCase() + ":" + conversation.getContextId();
        }
        return "conversation:" + conversation.getId();
    }

    private ChatConversationParticipantDTO toConversationParticipantDto(ChatConversationParticipant participant, ChatPresence presence, Instant now) {
        return ChatConversationParticipantDTO.builder()
                .userId(participant.getUser().getId())
                .username(participant.getUser().getUsername())
                .role(participant.getRole().name())
                .avatarDataUrl(participant.getUser().getProfileAvatarDataUrl())
                .online(isOnline(presence, now))
                .lastActiveAt(presence == null ? null : presence.getLastActiveAt().toString())
                .build();
    }

    private ChatContactDTO toContact(AppUser contact, List<CircleMembership> memberships, ChatPresence presence, Instant now) {
        List<CircleMembership> sortedMemberships = memberships.stream()
                .sorted(Comparator.comparing(membership -> membership.getCircle().getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
        return ChatContactDTO.builder()
                .userId(contact.getId())
                .username(contact.getUsername())
                .resolutionKey("chat-contact:" + contact.getId())
                .resolutionLabel(contact.getUsername())
                .exactResolutionEligible(true)
                .profileDescription(contact.getProfileDescription())
                .profileAvatarDataUrl(contact.getProfileAvatarDataUrl())
                .circleIds(sortedMemberships.stream().map(membership -> membership.getCircle().getId()).toList())
                .circleNames(sortedMemberships.stream().map(membership -> membership.getCircle().getName()).toList())
                .online(isOnline(presence, now))
                .lastActiveAt(presence == null ? null : presence.getLastActiveAt().toString())
                .build();
    }

    private ChatMessageDTO toMessageDto(ChatMessage message, AppUser currentUser) {
        return toMessageDto(message, currentUser, groupReactionsByMessageId(List.of(message)).getOrDefault(message.getId(), List.of()));
    }

    private ChatMessageDTO toMessageDto(ChatMessage message, AppUser currentUser, List<ChatMessageReaction> reactions) {
        String seenAt = message.getSeenAt() == null ? null : message.getSeenAt().toString();
        ResolvedMessageAttachment attachment = resolveMessageAttachment(message);
        return ChatMessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .conversationType(message.getConversation().getConversationType().name())
                .senderUserId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .senderAvatarDataUrl(message.getSender().getProfileAvatarDataUrl())
                .messageBody(message.getDeletedAt() == null ? message.getMessageBody() : null)
                .imageDataUrl(message.getDeletedAt() == null ? message.getImageDataUrl() : null)
                .attachmentName(attachment.attachmentName())
                .attachmentMimeType(attachment.attachmentMimeType())
                .attachmentStorageProvider(attachment.storageProvider())
                .attachmentStorageKey(attachment.storageKey())
                .attachmentUrl(attachment.url())
                .attachmentUrlExpiresAt(attachment.expiresAt())
                .attachmentSizeBytes(attachment.sizeBytes())
                .replyToMessageId(message.getReplyToMessageId())
                .clientMessageId(message.getClientMessageId())
                .createdAt(message.getCreatedAt().toString())
                .updatedAt(message.getUpdatedAt().toString())
                .editedAt(message.getEditedAt() == null ? null : message.getEditedAt().toString())
                .deletedAt(message.getDeletedAt() == null ? null : message.getDeletedAt().toString())
                .readAt(seenAt)
                .deliveredAt(message.getDeliveredAt() == null ? null : message.getDeliveredAt().toString())
                .seenAt(seenAt)
                .reactions(reactions.stream()
                        .map(reaction -> toReactionDto(reaction, currentUser))
                        .toList())
                .edited(message.getEditedAt() != null)
                .deleted(message.getDeletedAt() != null)
                .ownMessage(Objects.equals(message.getSender().getId(), currentUser.getId()))
                .build();
    }

    private ChatMessageReactionDTO toReactionDto(ChatMessageReaction reaction, AppUser currentUser) {
        return ChatMessageReactionDTO.builder()
                .id(reaction.getId())
                .userId(reaction.getUser().getId())
                .username(reaction.getUser().getUsername())
                .emoji(reaction.getEmoji())
                .createdAt(reaction.getCreatedAt().toString())
                .ownReaction(Objects.equals(reaction.getUser().getId(), currentUser.getId()))
                .build();
    }

    private Map<Long, ChatPresence> loadPresenceByUserId(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return chatPresenceRepository.findByUserIds(userIds.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(presence -> presence.getUser().getId(), presence -> presence));
    }

    private Map<Long, ChatConversationSummaryDTO> buildRealtimeConversationSummaries(ChatConversation conversation, Instant now) {
        List<ChatConversationParticipant> participants = effectiveParticipants(conversation);
        Map<Long, ChatPresence> presenceByUserId = loadPresenceByUserId(
                participants.stream().map(participant -> participant.getUser().getId()).toList()
        );
        return participants.stream().collect(Collectors.toMap(
                participant -> participant.getUser().getId(),
                participant -> {
                    ChatConversationMemberState memberState = chatConversationStateService.getStatesByConversationId(
                                    List.of(conversation.getId()),
                                    participant.getUser()
                            )
                            .get(conversation.getId());
                    return toConversationSummary(
                            conversation,
                            participant.getUser(),
                            unreadCount(conversation, participant.getUser()),
                            memberState,
                            presenceByUserId,
                            now
                    );
                }
        ));
    }

    private ChatConversationSummaryDTO publishConversationMembershipUpdate(
            ChatConversation conversation,
            AppUser actor,
            Set<Long> extraWorkspaceRefreshUserIds
    ) {
        Instant now = Instant.now();
        Map<Long, ChatConversationSummaryDTO> summariesByUserId = buildRealtimeConversationSummaries(conversation, now);
        chatRealtimeService.notifyConversationStateUpdated(conversation, actor.getId(), summariesByUserId);
        extraWorkspaceRefreshUserIds.forEach(userId ->
                chatRealtimeService.notifyWorkspaceChanged(userId, conversation.getId(), actor.getId(), "conversation_membership_updated"));
        return toConversationSummary(
                conversation,
                actor,
                unreadCount(conversation, actor),
                chatConversationStateService.getStatesByConversationId(List.of(conversation.getId()), actor).get(conversation.getId()),
                loadPresenceByUserId(otherParticipantIds(conversation, actor)),
                now
        );
    }

    private Map<Long, ChatMessageDTO> buildRealtimeMessageDtos(ChatMessage message, ChatConversation conversation) {
        List<ChatMessageReaction> reactions = groupReactionsByMessageId(List.of(message)).getOrDefault(message.getId(), List.of());
        return effectiveParticipants(conversation).stream()
                .map(ChatConversationParticipant::getUser)
                .collect(Collectors.toMap(AppUser::getId, participant -> toMessageDto(message, participant, reactions)));
    }

    private boolean isOnline(ChatPresence presence, Instant now) {
        return chatPresenceService.isOnline(presence, now);
    }

    private long unreadCount(ChatConversation conversation, AppUser currentUser) {
        return chatMessageRepository.findUnreadCountsByConversationIds(List.of(conversation.getId()), currentUser.getId()).stream()
                .findFirst()
                .map(ChatMessageRepository.UnreadCountRow::getUnreadCount)
                .orElse(0L);
    }

    private void requireMessageContent(ChatMessageRequestDTO request) {
        if (request == null) {
            throw ServiceErrors.badRequest("Chat message is required");
        }
        String messageBody = normalizeText(request.getMessageBody());
        String imageDataUrl = normalizeText(request.getImageDataUrl());
        Long attachmentUploadId = normalizeAttachmentUploadId(request.getAttachmentUploadId());
        if (messageBody == null && imageDataUrl == null && attachmentUploadId == null) {
            throw ServiceErrors.badRequest("Chat message, image, or attachment is required");
        }
        validateAttachmentRequest(request);
    }

    private ChatMessage requireReplyTarget(ChatConversation conversation, Long replyToMessageId) {
        if (replyToMessageId == null) {
            return null;
        }
        ChatMessage replyTarget = chatMessageRepository.findDetailedById(replyToMessageId)
                .orElseThrow(() -> ServiceErrors.notFound("Chat reply target not found with id " + replyToMessageId));
        if (!Objects.equals(replyTarget.getConversation().getId(), conversation.getId())) {
            throw ServiceErrors.badRequest("Reply target must belong to the same conversation");
        }
        return replyTarget;
    }

    private ChatMessage requireAccessibleMessage(Long conversationId, Long messageId, AppUser currentUser) {
        ChatConversation conversation = requireAccessibleConversation(conversationId, currentUser);
        ChatMessage message = chatMessageRepository.findDetailedById(messageId)
                .orElseThrow(() -> ServiceErrors.notFound("Chat message not found with id " + messageId));
        if (!Objects.equals(message.getConversation().getId(), conversation.getId())) {
            throw ServiceErrors.notFound("Chat message not found with id " + messageId);
        }
        return message;
    }

    private ChatMessage requireOwnMessage(Long conversationId, Long messageId, AppUser currentUser) {
        ChatMessage message = chatMessageRepository.findDetailedById(messageId)
                .orElseThrow(() -> ServiceErrors.notFound("Chat message not found with id " + messageId));
        if (!Objects.equals(message.getConversation().getId(), conversationId)) {
            throw ServiceErrors.notFound("Chat message not found with id " + messageId);
        }
        if (!Objects.equals(message.getSender().getId(), currentUser.getId())) {
            throw ServiceErrors.forbidden("You can only modify your own chat messages");
        }
        return message;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeConversationTitle(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            return null;
        }
        return normalized.length() <= 160 ? normalized : normalized.substring(0, 160);
    }

    private String normalizeClientMessageId(String value) {
        String normalized = normalizeText(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    private String normalizeAttachmentName(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            return null;
        }
        return normalized.length() <= 255 ? normalized : normalized.substring(0, 255);
    }

    private String normalizeAttachmentMimeType(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            return null;
        }
        return normalized.length() <= 120 ? normalized.toLowerCase() : normalized.substring(0, 120).toLowerCase();
    }

    private Long normalizeAttachmentUploadId(Long value) {
        if (value == null || value <= 0) {
            return null;
        }
        return value;
    }

    private Integer normalizeAttachmentSizeBytes(Integer value) {
        if (value == null || value <= 0) {
            return null;
        }
        return value;
    }

    private String normalizeEmoji(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            throw ServiceErrors.badRequest("Emoji is required");
        }
        return normalized.length() <= 32 ? normalized : normalized.substring(0, 32);
    }

    private String buildPreview(ChatMessage message) {
        if (message.getDeletedAt() != null) {
            return chatProperties.getMessages().getDeletedMessagePlaceholder();
        }
        String messageBody = normalizeText(message.getMessageBody());
        if (messageBody != null) {
            return messageBody.length() <= MESSAGE_PREVIEW_LIMIT
                    ? messageBody
                    : messageBody.substring(0, MESSAGE_PREVIEW_LIMIT - 1) + "…";
        }
        if (message.getAttachmentName() != null) {
            return "Attachment: " + message.getAttachmentName();
        }
        return "Photo";
    }

    private Long normalizedLeftId(Long leftUserId, Long rightUserId) {
        return Math.min(leftUserId, rightUserId);
    }

    private Long normalizedRightId(Long leftUserId, Long rightUserId) {
        return Math.max(leftUserId, rightUserId);
    }

    private int normalizePageSize(Integer requestedLimit) {
        int defaultSize = Math.max(chatProperties.getMessages().getDefaultPageSize(), 1);
        int maxSize = Math.max(chatProperties.getMessages().getMaxPageSize(), defaultSize);
        if (requestedLimit == null) {
            return defaultSize;
        }
        return Math.min(Math.max(requestedLimit, 1), maxSize);
    }

    private int normalizeConversationLimit(Integer requestedConversationLimit) {
        int maxSize = Math.max(chatProperties.getMessages().getMaxPageSize(), 1);
        if (requestedConversationLimit == null) {
            return maxSize;
        }
        return Math.min(Math.max(requestedConversationLimit, 1), maxSize);
    }

    private int normalizePageIndex(Integer requestedPage) {
        return requestedPage == null ? 0 : Math.max(requestedPage, 0);
    }

    private void validateImagePayload(String imageDataUrl) {
        String normalized = normalizeText(imageDataUrl);
        if (normalized == null) {
            return;
        }
        int separatorIndex = normalized.indexOf(',');
        if (!normalized.startsWith("data:") || separatorIndex < 0) {
            throw ServiceErrors.badRequest("Chat image must be a valid image data URL");
        }
        String metadata = normalized.substring(5, separatorIndex);
        String mimeType = TextValueNormalizer.lowerTrimToEmpty(metadata.split(";")[0]);
        if (!chatProperties.getAttachments().getAllowedImageMimeTypes().contains(mimeType)) {
            throw ServiceErrors.badRequest("Chat image type is not allowed");
        }
        if (!TextValueNormalizer.lowerToEmpty(metadata).contains(";base64")) {
            throw ServiceErrors.badRequest("Chat image must be base64 encoded");
        }
        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getDecoder().decode(normalized.substring(separatorIndex + 1).getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException exception) {
            throw ServiceErrors.badRequest("Chat image payload is invalid");
        }
        if (decodedBytes.length > chatProperties.getAttachments().getMaxImageBytes()) {
            throw ServiceErrors.badRequest("Chat image is too large");
        }
    }

    private void validateAttachmentRequest(ChatMessageRequestDTO request) {
        String attachmentName = normalizeAttachmentName(request.getAttachmentName());
        String attachmentMimeType = normalizeAttachmentMimeType(request.getAttachmentMimeType());
        Long attachmentUploadId = normalizeAttachmentUploadId(request.getAttachmentUploadId());
        if (attachmentUploadId == null && attachmentName == null && attachmentMimeType == null) {
            return;
        }
        if (attachmentName != null || attachmentMimeType != null) {
            throw ServiceErrors.badRequest("Attachment metadata is managed by the backend upload flow");
        }
        if (attachmentUploadId == null) {
            throw ServiceErrors.badRequest("Attachment upload id is required");
        }
    }

    private ValidatedAttachment validateAndBuildAttachment(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ServiceErrors.badRequest("Attachment file is required");
        }
        String attachmentName = normalizeAttachmentName(file.getOriginalFilename());
        String attachmentMimeType = normalizeAttachmentMimeType(file.getContentType());
        if (attachmentName == null) {
            throw ServiceErrors.badRequest("Attachment filename is required");
        }
        if (attachmentMimeType == null) {
            throw ServiceErrors.badRequest("Attachment mime type is required");
        }
        if (!chatProperties.getAttachments().getAllowedAttachmentMimeTypes().contains(attachmentMimeType)) {
            throw ServiceErrors.badRequest("Attachment type is not allowed");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (java.io.IOException exception) {
            throw ServiceErrors.badRequest("Attachment upload could not be read");
        }
        if (bytes.length > chatProperties.getAttachments().getMaxAttachmentBytes()) {
            throw ServiceErrors.badRequest("Attachment is too large");
        }
        return new ValidatedAttachment(attachmentName, attachmentMimeType, bytes, bytes.length);
    }

    private ChatAttachmentUpload requireAttachmentUpload(ChatMessageRequestDTO request, AppUser currentUser) {
        Long attachmentUploadId = normalizeAttachmentUploadId(request.getAttachmentUploadId());
        if (attachmentUploadId == null) {
            return null;
        }
        ChatAttachmentUpload upload = chatAttachmentUploadRepository.findById(attachmentUploadId)
                .orElseThrow(() -> ServiceErrors.notFound("Chat attachment upload not found with id " + attachmentUploadId));
        if (!Objects.equals(upload.getUploadedBy().getId(), currentUser.getId())) {
            throw ServiceErrors.forbidden("You can only attach your own uploaded files");
        }
        if (upload.getConsumedAt() != null) {
            throw ServiceErrors.badRequest("Chat attachment upload has already been used");
        }
        return upload;
    }

    private ResolvedMessageAttachment resolveMessageAttachment(ChatMessage message) {
        if (message.getDeletedAt() != null) {
            return ResolvedMessageAttachment.empty();
        }
        if (message.getAttachmentStorageKey() != null) {
            ObjectStorageAccess access = objectStorageService.resolve(message.getAttachmentStorageKey());
            return new ResolvedMessageAttachment(
                    message.getAttachmentName(),
                    message.getAttachmentMimeType(),
                    message.getAttachmentSizeBytes(),
                    access.provider(),
                    access.storageKey(),
                    access.url(),
                    access.expiresAt() == null ? null : access.expiresAt().toString()
            );
        }
        if (message.getAttachmentDataUrl() != null) {
            return new ResolvedMessageAttachment(
                    message.getAttachmentName(),
                    message.getAttachmentMimeType(),
                    message.getAttachmentSizeBytes(),
                    "INLINE",
                    null,
                    message.getAttachmentDataUrl(),
                    null
            );
        }
        return ResolvedMessageAttachment.empty();
    }

    private String buildAttachmentObjectKey(Long userId, String attachmentName) {
        Instant now = Instant.now();
        String keyPrefix = sanitizeStoragePathSegment(objectStorageProperties.getKeyPrefix());
        String datePartition = now.toString().substring(0, 10);
        return keyPrefix + "/" + userId + "/" + datePartition + "/" + UUID.randomUUID() + "-" + sanitizeFilename(attachmentName);
    }

    private String sanitizeStoragePathSegment(String value) {
        if (value == null || value.isBlank()) {
            return "chat";
        }
        return value.trim().replaceAll("[^a-zA-Z0-9/_-]+", "-");
    }

    private String sanitizeFilename(String filename) {
        String normalized = filename == null ? "attachment" : filename.trim();
        if (normalized.isBlank()) {
            normalized = "attachment";
        }
        String sanitized = normalized.replaceAll("[^a-zA-Z0-9._-]+", "-");
        return sanitized.length() <= 120 ? sanitized : sanitized.substring(0, 120);
    }

    @Transactional
    public int redactExpiredImages() {
        int retentionDays = Math.max(retentionProperties.getChat().getImageDays(), 1);
        Instant cutoff = Instant.now().minusSeconds(retentionDays * 86_400L);
        List<Long> conversationIds = chatMessageRepository.findConversationIdsWithExpiredImages(cutoff);
        int updatedCount = chatMessageRepository.redactExpiredImages(cutoff, retentionProperties.getChat().getExpiredImagePlaceholder());
        refreshConversationPreviews(conversationIds);
        return updatedCount;
    }

    @Transactional
    public int deleteExpiredMessages() {
        int retentionDays = Math.max(retentionProperties.getChat().getMessageDays(), 1);
        Instant cutoff = Instant.now().minusSeconds(retentionDays * 86_400L);
        List<Long> conversationIds = chatMessageRepository.findConversationIdsWithExpiredMessages(cutoff);
        int deletedCount = chatMessageRepository.deleteByCreatedAtBefore(cutoff);
        refreshConversationPreviews(conversationIds);
        return deletedCount;
    }

    private void refreshConversationPreviews(List<Long> conversationIds) {
        if (conversationIds == null || conversationIds.isEmpty()) {
            return;
        }

        for (Long conversationId : conversationIds.stream().distinct().toList()) {
            chatConversationRepository.findDetailedById(conversationId).ifPresent(this::refreshConversationPreview);
        }
    }

    private void refreshConversationPreview(ChatConversation conversation) {
        List<ChatMessage> latestMessages = chatMessageRepository.findLatestDetailedByConversationId(conversation.getId(), PageRequest.of(0, 1));
        if (latestMessages.isEmpty()) {
            conversation.setLastMessageAt(null);
            conversation.setLastMessageId(null);
            conversation.setLastMessageSender(null);
            conversation.setLastMessagePreview(null);
            conversation.setLastMessageHasImage(false);
            conversation.setLastMessageDeleted(false);
            chatConversationRepository.save(conversation);
            return;
        }

        ChatMessage latestMessage = latestMessages.getFirst();
        conversation.setLastMessageAt(latestMessage.getCreatedAt());
        conversation.setLastMessageId(latestMessage.getId());
        conversation.setLastMessageSender(latestMessage.getSender());
        conversation.setLastMessagePreview(buildPreview(latestMessage));
        conversation.setLastMessageHasImage(latestMessage.getDeletedAt() == null && latestMessage.getImageDataUrl() != null);
        conversation.setLastMessageDeleted(latestMessage.getDeletedAt() != null);
        chatConversationRepository.save(conversation);
    }

    private Map<Long, List<ChatMessageReaction>> groupReactionsByMessageId(List<ChatMessage> messages) {
        List<Long> messageIds = messages.stream()
                .map(ChatMessage::getId)
                .filter(Objects::nonNull)
                .toList();
        if (messageIds.isEmpty()) {
            return Map.of();
        }
        return chatMessageReactionRepository.findDetailedByMessageIdIn(messageIds).stream()
                .collect(Collectors.groupingBy(reaction -> reaction.getMessage().getId(), LinkedHashMap::new, Collectors.toList()));
    }

    private void recordConversationAudit(
            ChatAuditEventType eventType,
            AppUser actor,
            ChatConversation conversation,
            Map<String, Object> details
    ) {
        chatAuditService.record(eventType, actor, conversation, null, null, null, details);
    }

    private record ParticipantSeed(AppUser user, ChatConversationParticipantRole role, AppUser addedBy) {
    }

    private record ValidatedAttachment(String name, String mimeType, byte[] bytes, int sizeBytes) {
    }

    private record ResolvedMessageAttachment(
            String attachmentName,
            String attachmentMimeType,
            Integer sizeBytes,
            String storageProvider,
            String storageKey,
            String url,
            String expiresAt
    ) {
        private static ResolvedMessageAttachment empty() {
            return new ResolvedMessageAttachment(null, null, null, null, null, null, null);
        }
    }
}
