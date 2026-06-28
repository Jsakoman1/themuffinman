package com.themuffinman.app.chat.service;

import com.themuffinman.app.chat.dto.ChatCircleOptionDTO;
import com.themuffinman.app.chat.dto.ChatContactDTO;
import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
import com.themuffinman.app.chat.dto.ChatMessageDTO;
import com.themuffinman.app.chat.dto.ChatMessageRequestDTO;
import com.themuffinman.app.chat.dto.ChatOpenConversationRequestDTO;
import com.themuffinman.app.chat.dto.ChatWorkspaceDTO;
import com.themuffinman.app.chat.model.ChatConversation;
import com.themuffinman.app.chat.model.ChatMessage;
import com.themuffinman.app.chat.model.ChatPresence;
import com.themuffinman.app.chat.repository.ChatConversationRepository;
import com.themuffinman.app.chat.repository.ChatMessageRepository;
import com.themuffinman.app.chat.repository.ChatPresenceRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.RetentionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.AppUserLookupService;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.model.CircleMembership;
import com.themuffinman.app.social.service.CircleMembershipService;
import com.themuffinman.app.social.service.CircleRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int MESSAGE_PREVIEW_LIMIT = 140;

    private final ChatConversationRepository chatConversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatPresenceRepository chatPresenceRepository;
    private final AppUserLookupService appUserLookupService;
    private final CircleRelationService circleRelationService;
    private final CircleMembershipService circleMembershipService;
    private final ChatPresenceService chatPresenceService;
    private final ChatRealtimeService chatRealtimeService;
    private final RetentionProperties retentionProperties;

    @Transactional(readOnly = true)
    public ChatWorkspaceDTO getWorkspace(AppUser currentUser) {
        List<ChatConversation> conversations = chatConversationRepository.findDetailedByParticipantId(currentUser.getId()).stream()
                .filter(conversation -> isCurrentChatContact(currentUser, otherParticipant(conversation, currentUser)))
                .toList();
        List<Long> conversationIds = conversations.stream()
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
        for (ChatConversation conversation : conversations) {
            AppUser otherParticipant = otherParticipant(conversation, currentUser);
            if (isCurrentChatContact(currentUser, otherParticipant)) {
                contactByUserId.putIfAbsent(otherParticipant.getId(), otherParticipant);
            }
        }

        Map<Long, ChatPresence> presenceByUserId = loadPresenceByUserId(contactByUserId.keySet().stream().toList());
        Instant now = Instant.now();

        List<ChatConversationSummaryDTO> conversationSummaries = conversations.stream()
                .map(conversation -> toConversationSummary(conversation, currentUser, unreadCountsByConversationId.getOrDefault(conversation.getId(), 0L), presenceByUserId, now))
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
                .build();
    }

    @Transactional
    public ChatConversationSummaryDTO openConversation(ChatOpenConversationRequestDTO request, AppUser currentUser) {
        AppUser otherUser = requireChatCounterpart(currentUser, request.getOtherUserId());
        ChatConversation conversation = chatConversationRepository.findByLeftParticipantIdAndRightParticipantId(
                        normalizedLeftId(currentUser.getId(), otherUser.getId()),
                        normalizedRightId(currentUser.getId(), otherUser.getId())
                )
                .orElseGet(() -> createConversation(currentUser, otherUser));

        ChatPresence otherPresence = chatPresenceRepository.findByUserId(otherUser.getId()).orElse(null);
        return toConversationSummary(conversation, currentUser, 0L, otherPresence, Instant.now());
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getConversationMessages(Long conversationId, AppUser currentUser) {
        ChatConversation conversation = requireAccessibleConversation(conversationId, currentUser);
        return chatMessageRepository.findDetailedByConversationId(conversation.getId()).stream()
                .map(message -> toMessageDto(message, currentUser))
                .toList();
    }

    @Transactional
    public ChatMessageDTO sendMessage(Long conversationId, ChatMessageRequestDTO request, AppUser currentUser) {
        ChatConversation conversation = requireAccessibleConversation(conversationId, currentUser);
        requireMessageContent(request);

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(currentUser);
        message.setMessageBody(normalizeText(request.getMessageBody()));
        message.setImageDataUrl(normalizeText(request.getImageDataUrl()));
        ChatMessage saved = chatMessageRepository.save(message);

        conversation.setLastMessageAt(saved.getCreatedAt());
        conversation.setLastMessageSender(currentUser);
        conversation.setLastMessagePreview(buildPreview(saved));
        conversation.setLastMessageHasImage(saved.getImageDataUrl() != null);
        chatConversationRepository.save(conversation);
        chatRealtimeService.notifyConversationChanged(conversation, currentUser.getId(), "message_created");

        return toMessageDto(saved, currentUser);
    }

    @Transactional
    public void markConversationRead(Long conversationId, AppUser currentUser) {
        ChatConversation conversation = requireAccessibleConversation(conversationId, currentUser);
        Instant now = Instant.now();
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadIncomingByConversationId(conversation.getId(), currentUser.getId());
        unreadMessages.forEach(message -> message.setReadAt(now));
        if (!unreadMessages.isEmpty()) {
            chatMessageRepository.saveAll(unreadMessages);
            chatRealtimeService.notifyConversationChanged(conversation, currentUser.getId(), "conversation_read");
        }
    }

    @Transactional
    public void heartbeat(AppUser currentUser) {
        chatPresenceService.markActive(currentUser);
    }

    private ChatConversation createConversation(AppUser currentUser, AppUser otherUser) {
        ChatConversation conversation = new ChatConversation();
        AppUser left = currentUser.getId() < otherUser.getId() ? currentUser : otherUser;
        AppUser right = currentUser.getId() < otherUser.getId() ? otherUser : currentUser;
        conversation.setLeftParticipant(left);
        conversation.setRightParticipant(right);
        return chatConversationRepository.save(conversation);
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

    private ChatConversation requireAccessibleConversation(Long conversationId, AppUser currentUser) {
        ChatConversation conversation = chatConversationRepository.findDetailedById(conversationId)
                .orElseThrow(() -> ServiceErrors.notFound("Chat conversation not found with id " + conversationId));
        AppUser otherUser = otherParticipant(conversation, currentUser);
        if (otherUser == null) {
            throw ServiceErrors.forbidden("You can only access your own chat conversations");
        }
        if (!circleRelationService.isCircleBetween(currentUser, otherUser)) {
            throw ServiceErrors.forbidden("You can only chat with people in your circles");
        }
        return conversation;
    }

    private AppUser otherParticipant(ChatConversation conversation, AppUser currentUser) {
        if (Objects.equals(conversation.getLeftParticipant().getId(), currentUser.getId())) {
            return conversation.getRightParticipant();
        }
        if (Objects.equals(conversation.getRightParticipant().getId(), currentUser.getId())) {
            return conversation.getLeftParticipant();
        }
        return null;
    }

    private ChatConversationSummaryDTO toConversationSummary(
            ChatConversation conversation,
            AppUser currentUser,
            long unreadCount,
            Map<Long, ChatPresence> presenceByUserId,
            Instant now
    ) {
        AppUser otherUser = otherParticipant(conversation, currentUser);
        return toConversationSummary(conversation, currentUser, unreadCount, otherUser == null ? null : presenceByUserId.get(otherUser.getId()), now);
    }

    private ChatConversationSummaryDTO toConversationSummary(
            ChatConversation conversation,
            AppUser currentUser,
            long unreadCount,
            ChatPresence otherPresence,
            Instant now
    ) {
        AppUser otherUser = otherParticipant(conversation, currentUser);
        return ChatConversationSummaryDTO.builder()
                .conversationId(conversation.getId())
                .otherUserId(otherUser.getId())
                .otherUsername(otherUser.getUsername())
                .resolutionKey("conversation:" + conversation.getId())
                .resolutionLabel("Chat with " + otherUser.getUsername())
                .exactResolutionEligible(true)
                .otherUserProfileDescription(otherUser.getProfileDescription())
                .otherUserAvatarDataUrl(otherUser.getProfileAvatarDataUrl())
                .otherUserOnline(isOnline(otherPresence, now))
                .otherUserLastActiveAt(otherPresence == null ? null : otherPresence.getLastActiveAt().toString())
                .lastMessagePreview(conversation.getLastMessagePreview())
                .lastMessageAt(conversation.getLastMessageAt() == null ? null : conversation.getLastMessageAt().toString())
                .lastMessageFromCurrentUser(conversation.getLastMessageSender() != null
                        && Objects.equals(conversation.getLastMessageSender().getId(), currentUser.getId()))
                .lastMessageHasImage(conversation.isLastMessageHasImage())
                .unreadCount(unreadCount)
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
        return ChatMessageDTO.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderUserId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .senderAvatarDataUrl(message.getSender().getProfileAvatarDataUrl())
                .messageBody(message.getMessageBody())
                .imageDataUrl(message.getImageDataUrl())
                .createdAt(message.getCreatedAt().toString())
                .readAt(message.getReadAt() == null ? null : message.getReadAt().toString())
                .ownMessage(Objects.equals(message.getSender().getId(), currentUser.getId()))
                .build();
    }

    private Map<Long, ChatPresence> loadPresenceByUserId(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return chatPresenceRepository.findByUserIds(userIds).stream()
                .collect(Collectors.toMap(presence -> presence.getUser().getId(), presence -> presence));
    }

    private boolean isOnline(ChatPresence presence, Instant now) {
        return chatPresenceService.isOnline(presence, now);
    }

    private void requireMessageContent(ChatMessageRequestDTO request) {
        if (request == null) {
            throw ServiceErrors.badRequest("Chat message is required");
        }
        String messageBody = normalizeText(request.getMessageBody());
        String imageDataUrl = normalizeText(request.getImageDataUrl());
        if (messageBody == null && imageDataUrl == null) {
            throw ServiceErrors.badRequest("Chat message or image is required");
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private String buildPreview(ChatMessage message) {
        String messageBody = normalizeText(message.getMessageBody());
        if (messageBody != null) {
            return messageBody.length() <= MESSAGE_PREVIEW_LIMIT
                    ? messageBody
                    : messageBody.substring(0, MESSAGE_PREVIEW_LIMIT - 1) + "…";
        }
        return "Photo";
    }

    private Long normalizedLeftId(Long leftUserId, Long rightUserId) {
        return Math.min(leftUserId, rightUserId);
    }

    private Long normalizedRightId(Long leftUserId, Long rightUserId) {
        return Math.max(leftUserId, rightUserId);
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
            conversation.setLastMessageSender(null);
            conversation.setLastMessagePreview(null);
            conversation.setLastMessageHasImage(false);
            chatConversationRepository.save(conversation);
            return;
        }

        ChatMessage latestMessage = latestMessages.getFirst();
        conversation.setLastMessageAt(latestMessage.getCreatedAt());
        conversation.setLastMessageSender(latestMessage.getSender());
        conversation.setLastMessagePreview(buildPreview(latestMessage));
        conversation.setLastMessageHasImage(latestMessage.getImageDataUrl() != null);
        chatConversationRepository.save(conversation);
    }
}
