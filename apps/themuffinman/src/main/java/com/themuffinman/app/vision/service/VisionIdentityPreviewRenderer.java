package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
import com.themuffinman.app.chat.dto.ChatConversationSyncDTO;
import com.themuffinman.app.chat.dto.ChatContactDTO;
import com.themuffinman.app.chat.dto.ChatWorkspaceDTO;
import com.themuffinman.app.chat.dto.ChatMessageDTO;
import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.identity.dto.UserProfileViewDTO;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.service.AppUserReadService;
import com.themuffinman.app.identity.service.AppUserService;
import com.themuffinman.app.identity.service.UserProfileViewService;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
class VisionIdentityPreviewRenderer {

    private final AppUserService appUserService;
    private final AppUserReadService appUserReadService;
    private final AppUserMgr appUserMgr;
    private final AppUserRepository appUserRepository;
    private final UserProfileViewService userProfileViewService;
    private final ChatService chatService;

    VisionCapabilityPreviewDTO previewProfile(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        AppUserResponseDTO profile = appUserMgr.withProfileStats(
                appUserMgr.toDto(currentUser),
                appUserReadService.countQuestsByCreatorId(currentUser.getId()),
                appUserReadService.getOpenQuestsByCreatorId(currentUser.getId())
        );
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        VisionCapabilityPreviewSupport.addItem(items, "profile_username", "Username", profile.getUsername());
        VisionCapabilityPreviewSupport.addItem(items, "profile_email", "Email", profile.getEmail());
        VisionCapabilityPreviewSupport.addItem(items, "profile_role", "Role", profile.getRole());
        VisionCapabilityPreviewSupport.addItem(items, "profile_location", "Location",
                profile.getLocationSettings() == null ? null : profile.getLocationSettings().getLabel());
        VisionCapabilityPreviewSupport.addItem(items, "profile_location_mode", "Location mode",
                profile.getLocationSettings() == null || profile.getLocationSettings().getMode() == null
                        ? null
                        : profile.getLocationSettings().getMode().name());
        VisionCapabilityPreviewSupport.addItem(items, "profile_open_quests", "Open quests", String.valueOf(profile.getOpenQuestCount()));

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_profile")
                .title("Profile")
                .summary("Profile.")
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewSettings(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        AppUserResponseDTO profile = appUserMgr.toDto(appUserReadService.getAppUser(currentUser.getId()));
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        VisionCapabilityPreviewSupport.addItem(items, "profile_email", "Email", profile.getEmail());
        VisionCapabilityPreviewSupport.addItem(items, "profile_username", "Username", profile.getUsername());
        VisionCapabilityPreviewSupport.addItem(items, "profile_location_mode", "Location mode",
                profile.getLocationSettings() == null || profile.getLocationSettings().getMode() == null
                        ? null
                        : profile.getLocationSettings().getMode().name());
        VisionCapabilityPreviewSupport.addItem(items, "profile_location_label", "Location",
                profile.getLocationSettings() == null ? null : profile.getLocationSettings().getLabel());

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_settings")
                .title("Settings")
                .summary("Settings.")
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewChatWorkspace(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        ChatWorkspaceDTO workspace = chatService.getWorkspace(currentUser);
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        VisionCapabilityPreviewSupport.addItem(items, "chat_unread_conversations", "Unread conversations",
                String.valueOf(workspace.getUnreadConversationCount()));
        VisionCapabilityPreviewSupport.addItem(items, "chat_online_contacts", "Online contacts",
                String.valueOf(workspace.getOnlineContactCount()));
        VisionCapabilityPreviewSupport.addItem(items, "chat_contacts", "Contacts",
                String.valueOf(workspace.getContacts() == null ? 0 : workspace.getContacts().size()));

        List<ChatConversationSummaryDTO> conversations = workspace.getConversations() == null ? List.of() : workspace.getConversations();
        for (int index = 0; index < Math.min(conversations.size(), 4); index++) {
            ChatConversationSummaryDTO conversation = conversations.get(index);
            String value = conversation.getLastMessagePreview();
            if (conversation.getUnreadCount() > 0) {
                value = (value == null || value.isBlank() ? "" : value + " · ") + conversation.getUnreadCount() + " unread";
            }
            VisionCapabilityPreviewSupport.addItem(items, "chat_conversation_" + conversation.getConversationId(), conversation.getOtherUsername(), value);
        }

        List<ChatContactDTO> contacts = workspace.getContacts() == null ? List.of() : workspace.getContacts();
        for (int index = 0; index < Math.min(contacts.size(), 3); index++) {
            ChatContactDTO contact = contacts.get(index);
            VisionCapabilityPreviewSupport.addItem(items, "chat_contact_" + contact.getUserId(), "Contact " + (index + 1),
                    contact.getUsername() + (contact.isOnline() ? " · online" : ""));
        }

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_chat_workspace")
                .title("Chat")
                .summary("Chat.")
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewChatSync(AppUser currentUser, Long conversationId) {
        if (currentUser == null || conversationId == null) {
            return null;
        }
        ChatConversationSyncDTO sync = chatService.getConversationSync(conversationId, null, 20, currentUser);
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        if (sync.getConversation() != null) {
            VisionCapabilityPreviewSupport.addItem(items, "chat_conversation", "Conversation",
                    sync.getConversation().getOtherUsername());
            VisionCapabilityPreviewSupport.addItem(items, "chat_latest_message_id", "Latest message",
                    sync.getLatestMessageId() == null ? "none" : String.valueOf(sync.getLatestMessageId()));
        }
        List<ChatMessageDTO> messages = sync.getMessages() == null ? List.of() : sync.getMessages();
        for (ChatMessageDTO message : messages.stream().skip(Math.max(0, messages.size() - 5)).toList()) {
            String body = message.isDeleted() ? "Message deleted." : message.getMessageBody();
            if (body == null || body.isBlank()) {
                body = message.getAttachmentName() == null ? "Attachment" : message.getAttachmentName();
            }
            VisionCapabilityPreviewSupport.addItem(items, "chat_message_" + message.getId(),
                    message.getSenderUsername(), body);
            if (message.getAttachmentUrl() != null && !message.getAttachmentUrl().isBlank()) {
                VisionCapabilityPreviewSupport.addItem(items, "chat_attachment_" + message.getId(),
                        message.getAttachmentName() == null ? "Attachment" : message.getAttachmentName(),
                        message.getAttachmentUrl());
                if (message.getAttachmentUrlExpiresAt() != null && !message.getAttachmentUrlExpiresAt().isBlank()) {
                    VisionCapabilityPreviewSupport.addItem(items, "chat_attachment_expiry_" + message.getId(),
                            "Attachment URL expires", message.getAttachmentUrlExpiresAt());
                }
            }
        }
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("sync_chat")
                .title("Chat sync")
                .summary(messages.isEmpty() ? "Chat is up to date." : "Chat refreshed with " + messages.size() + " messages.")
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewChatAttachment(AppUser currentUser, Long conversationId, Long messageId) {
        if (currentUser == null || conversationId == null || messageId == null) {
            return null;
        }
        ChatMessageDTO message = chatService.getMessage(conversationId, messageId, currentUser);
        if (message == null || message.getAttachmentUrl() == null || message.getAttachmentUrl().isBlank()) {
            return VisionCapabilityPreviewDTO.builder()
                    .capabilityId("view_chat_attachment")
                    .title("Chat attachment")
                    .summary("This message has no available attachment.")
                    .items(List.of())
                    .tone("muted")
                    .build();
        }
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        VisionCapabilityPreviewSupport.addItem(items, "chat_conversation_id", "Conversation", String.valueOf(conversationId));
        VisionCapabilityPreviewSupport.addItem(items, "chat_message_id", "Message", String.valueOf(messageId));
        VisionCapabilityPreviewSupport.addItem(items, "chat_attachment_name", "Attachment", message.getAttachmentName());
        VisionCapabilityPreviewSupport.addItem(items, "chat_attachment_url", "Open attachment", message.getAttachmentUrl());
        VisionCapabilityPreviewSupport.addItem(items, "chat_attachment_url_expires_at", "URL expires", message.getAttachmentUrlExpiresAt());
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_chat_attachment")
                .title("Chat attachment")
                .summary("Attachment access is authorized for this conversation participant.")
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewUserProfile(AppUser currentUser, Long profileUserId) {
        if (currentUser == null || profileUserId == null) {
            return null;
        }

        UserProfileViewDTO profileView = userProfileViewService.getProfileView(profileUserId, currentUser);
        if (profileView == null || profileView.getProfile() == null) {
            return null;
        }

        AppUserResponseDTO profile = profileView.getProfile();
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        VisionCapabilityPreviewSupport.addItem(items, "target_user", "Username", profile.getUsername());
        VisionCapabilityPreviewSupport.addItem(items, "profile_description", "Profile description", profile.getProfileDescription());
        VisionCapabilityPreviewSupport.addItem(items, "profile_location_label", "Location",
                profile.getLocationSettings() == null ? null : profile.getLocationSettings().getLabel());
        VisionCapabilityPreviewSupport.addItem(items, "profile_open_quests", "Open quests", String.valueOf(profile.getOpenQuestCount()));
        VisionCapabilityPreviewSupport.addItem(items, "profile_relation", "Relation",
                profileView.getRelation() == null ? null : profileView.getRelation().getRelationLabel());

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_user_profile")
                .title("User profile")
                .summary("User profile.")
                .items(items)
                .tone("info")
                .build();
    }
}
