package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
import com.themuffinman.app.chat.dto.ChatContactDTO;
import com.themuffinman.app.chat.dto.ChatWorkspaceDTO;
import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.identity.dto.AppUserRequestDTO;
import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.identity.dto.UserProfileViewDTO;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.service.AppUserService;
import com.themuffinman.app.identity.service.UserProfileViewService;
import com.themuffinman.app.common.normalization.SearchQueryNormalizer;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import com.themuffinman.app.location.dto.UserLocationSettingsDTO;
import com.themuffinman.app.location.dto.UserLocationSettingsRequestDTO;
import com.themuffinman.app.location.model.UserLocationMode;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleMemberDTO;
import com.themuffinman.app.social.dto.CircleGroupRequestDTO;
import com.themuffinman.app.social.dto.CircleRequestCreateDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.service.CircleService;
import com.themuffinman.app.vision.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.vision.dto.QuestApplicationDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestAllowedActionDTO;
import com.themuffinman.app.vision.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.vision.dto.QuestDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.vision.mapper.QuestNewsMgr;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VisionCapabilityPreviewService {

    private final AppUserService appUserService;
    private final AppUserMgr appUserMgr;
    private final AppUserRepository appUserRepository;
    private final UserProfileViewService userProfileViewService;
    private final ChatService chatService;
    private final CircleService circleService;
    private final QuestService questService;
    private final QuestApplicationService questApplicationService;
    private final QuestNewsService questNewsService;
    private final QuestNewsMgr questNewsMgr;
    private final SemanticAliasRegistry semanticAliasRegistry;
    private static final Pattern QUEST_ID_PATTERN = Pattern.compile("(?i)(?:quest\\s*#?\\s*|#)(\\d+)|^(\\d+)$");
    private static final Pattern APPLICATION_ID_PATTERN = Pattern.compile("(?i)(?:application\\s*#?\\s*|#)(\\d+)|^(\\d+)$");
    private static final Pattern CIRCLE_ID_PATTERN = Pattern.compile("(?i)(?:circle\\s*#?\\s*|#)(\\d+)|^(\\d+)$");
    private static final Pattern USER_ID_PATTERN = Pattern.compile("(?i)(?:user\\s*#?\\s*|profile\\s*#?\\s*|#)(\\d+)|^(\\d+)$");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            .withZone(ZoneId.systemDefault());

    public VisionCapabilityPreviewService(
            AppUserService appUserService,
            AppUserMgr appUserMgr,
            AppUserRepository appUserRepository,
            UserProfileViewService userProfileViewService,
            ChatService chatService,
            CircleService circleService,
            QuestService questService,
            QuestApplicationService questApplicationService,
            QuestNewsService questNewsService,
            QuestNewsMgr questNewsMgr,
            SemanticAliasRegistry semanticAliasRegistry
    ) {
        this.appUserService = appUserService;
        this.appUserMgr = appUserMgr;
        this.appUserRepository = appUserRepository;
        this.userProfileViewService = userProfileViewService;
        this.chatService = chatService;
        this.circleService = circleService;
        this.questService = questService;
        this.questApplicationService = questApplicationService;
        this.questNewsService = questNewsService;
        this.questNewsMgr = questNewsMgr;
        this.semanticAliasRegistry = semanticAliasRegistry;
    }

    public VisionCapabilityPreviewDTO previewProfile(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        AppUserResponseDTO profile = appUserMgr.withProfileStats(
                appUserMgr.toDto(currentUser),
                appUserService.countQuestsByCreatorId(currentUser.getId()),
                appUserService.getOpenQuestsByCreatorId(currentUser.getId())
        );
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "profile_username", "Username", profile.getUsername());
        addItem(items, "profile_email", "Email", profile.getEmail());
        addItem(items, "profile_role", "Role", profile.getRole());
        addItem(items, "profile_location", "Location", profile.getLocationSettings() == null ? null : profile.getLocationSettings().getLabel());
        addItem(items, "profile_location_mode", "Location mode", profile.getLocationSettings() == null || profile.getLocationSettings().getMode() == null
                ? null
                : profile.getLocationSettings().getMode().name());
        addItem(items, "profile_open_quests", "Open quests", String.valueOf(profile.getOpenQuestCount()));

        String summary = "Showing your profile snapshot.";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_profile")
                .title("Profile")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewSettings(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        AppUserResponseDTO profile = appUserMgr.toDto(appUserService.getAppUser(currentUser.getId()));
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "profile_email", "Email", profile.getEmail());
        addItem(items, "profile_username", "Username", profile.getUsername());
        addItem(items, "profile_location_mode", "Location mode", profile.getLocationSettings() == null || profile.getLocationSettings().getMode() == null
                ? null
                : profile.getLocationSettings().getMode().name());
        addItem(items, "profile_location_label", "Location", profile.getLocationSettings() == null ? null : profile.getLocationSettings().getLabel());

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_settings")
                .title("Settings")
                .summary("Showing your account settings snapshot.")
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewChatWorkspace(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        ChatWorkspaceDTO workspace = chatService.getWorkspace(currentUser);
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "chat_unread_conversations", "Unread conversations", String.valueOf(workspace.getUnreadConversationCount()));
        addItem(items, "chat_online_contacts", "Online contacts", String.valueOf(workspace.getOnlineContactCount()));
        addItem(items, "chat_contacts", "Contacts", String.valueOf(workspace.getContacts() == null ? 0 : workspace.getContacts().size()));

        List<ChatConversationSummaryDTO> conversations = workspace.getConversations() == null ? List.of() : workspace.getConversations();
        for (int index = 0; index < Math.min(conversations.size(), 4); index++) {
            ChatConversationSummaryDTO conversation = conversations.get(index);
            String value = conversation.getLastMessagePreview();
            if (conversation.getUnreadCount() > 0) {
                value = (value == null || value.isBlank() ? "" : value + " · ") + conversation.getUnreadCount() + " unread";
            }
            addItem(items, "chat_conversation_" + conversation.getConversationId(), conversation.getOtherUsername(), value);
        }

        List<ChatContactDTO> contacts = workspace.getContacts() == null ? List.of() : workspace.getContacts();
        for (int index = 0; index < Math.min(contacts.size(), 3); index++) {
            ChatContactDTO contact = contacts.get(index);
            addItem(items, "chat_contact_" + contact.getUserId(), "Contact " + (index + 1),
                    contact.getUsername() + (contact.isOnline() ? " · online" : ""));
        }

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_chat_workspace")
                .title("Chat")
                .summary("Showing your chat workspace snapshot.")
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewUserProfile(AppUser currentUser, Long profileUserId) {
        if (currentUser == null || profileUserId == null) {
            return null;
        }

        UserProfileViewDTO profileView = userProfileViewService.getProfileView(profileUserId, currentUser);
        if (profileView == null || profileView.getProfile() == null) {
            return null;
        }

        AppUserResponseDTO profile = profileView.getProfile();
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_user", "Username", profile.getUsername());
        addItem(items, "profile_description", "Profile description", profile.getProfileDescription());
        addItem(items, "profile_location_label", "Location", profile.getLocationSettings() == null ? null : profile.getLocationSettings().getLabel());
        addItem(items, "profile_open_quests", "Open quests", String.valueOf(profile.getOpenQuestCount()));
        addItem(items, "profile_relation", "Relation", profileView.getRelation() == null ? null : profileView.getRelation().getRelationLabel());

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_user_profile")
                .title("User profile")
                .summary("Showing the profile for " + profile.getUsername() + ".")
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewCircles(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        List<CircleGroupResponseDTO> circles = circleService.getCircles(currentUser);
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "circles_count", "Circles", String.valueOf(circles.size()));
        for (int index = 0; index < Math.min(circles.size(), 4); index++) {
            CircleGroupResponseDTO circle = circles.get(index);
            addItem(items, "circle_" + circle.getId(), circle.getName(),
                    circle.getMemberCount() + " members"
                            + (circle.getMemberPreviewLabel() == null || circle.getMemberPreviewLabel().isBlank()
                            ? ""
                            : " · " + circle.getMemberPreviewLabel()));
        }

        String summary = circles.isEmpty()
                ? "You do not have any circles yet."
                : "Showing " + Math.min(circles.size(), 4) + " of " + circles.size() + " circles.";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_circles")
                .title("Circles")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewApplications(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        List<QuestApplicationResponseDTO> applications = questApplicationService.getApplicationsForApplicant(currentUser);
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "applications_count", "Applications", String.valueOf(applications.size()));
        long pendingCount = applications.stream().filter(application -> application.getStatus() != null && "PENDING".equals(application.getStatus().name())).count();
        long approvedCount = applications.stream().filter(application -> application.getStatus() != null && "APPROVED".equals(application.getStatus().name())).count();
        addItem(items, "applications_pending", "Pending", String.valueOf(pendingCount));
        addItem(items, "applications_approved", "Approved", String.valueOf(approvedCount));
        for (int index = 0; index < Math.min(applications.size(), 4); index++) {
            QuestApplicationResponseDTO application = applications.get(index);
            addItem(items, "application_" + application.getId(),
                    application.getQuestTitle(),
                    application.getStatus() == null ? "Unknown status" : application.getStatus().name());
        }

        String summary = applications.isEmpty()
                ? "You have not applied to any quests yet."
                : "Showing " + Math.min(applications.size(), 4) + " of " + applications.size() + " applications.";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_applications")
                .title("Applications")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewQuestNews(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        List<QuestNewsItemResponseDTO> newsItems = questNewsService.getMyNews(currentUser).stream()
                .map(questNewsMgr::toDto)
                .toList();

        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "news_count", "Updates", String.valueOf(newsItems.size()));
        long unreadCount = newsItems.stream().filter(item -> item.getReadAt() == null).count();
        addItem(items, "news_unread", "Unread", String.valueOf(unreadCount));
        for (int index = 0; index < Math.min(newsItems.size(), 4); index++) {
            QuestNewsItemResponseDTO item = newsItems.get(index);
            addItem(items, "news_" + item.getId(), item.getTitle(), item.getMessage());
        }

        String summary = newsItems.isEmpty()
                ? "You do not have any quest updates yet."
                : "Showing " + Math.min(newsItems.size(), 4) + " of " + newsItems.size() + " quest updates.";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_quest_news")
                .title("Quest news")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewCircleDetail(AppUser currentUser, Long circleId) {
        if (currentUser == null || circleId == null) {
            return null;
        }

        CircleGroupResponseDTO circle = circleService.getCircles(currentUser).stream()
                .filter(candidate -> circleId.equals(candidate.getId()))
                .findFirst()
                .orElse(null);
        if (circle == null) {
            return null;
        }

        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_circle_query", "Circle", circle.getName());
        addItem(items, "circle_member_count", "Members", String.valueOf(circle.getMemberCount()));
        List<CircleMemberDTO> members = circle.getMembers() == null ? List.of() : circle.getMembers();
        for (int index = 0; index < Math.min(members.size(), 6); index++) {
            CircleMemberDTO member = members.get(index);
            addItem(items, "circle_member_" + (index + 1), "Member " + (index + 1), member.getUsername());
        }

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_circle_detail")
                .title("Circle")
                .summary("Showing the details for " + circle.getName() + ".")
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewQuestDetail(AppUser currentUser, Long questId) {
        if (currentUser == null || questId == null) {
            return null;
        }

        QuestDetailResponseDTO detail = questService.getQuestDetailResponseById(questId, currentUser);
        QuestResponseDTO quest = detail == null ? null : detail.getSummary();
        if (quest == null) {
            return null;
        }

        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_quest_query", "Quest", quest.getTitle());
        addItem(items, "quest_description", "Description", quest.getDescription());
        addItem(items, "reward_amount", "Reward", formatRewardLabel(quest));
        addItem(items, "visibility", "Visibility", quest.getAudience() == null ? null : quest.getAudience().name());
        addItem(items, "scheduled_at", "Schedule", quest.getScheduledAt() == null ? null : formatDateTime(quest.getScheduledAt()));
        addItem(items, "location_label", "Location",
                quest.getPresentation() == null ? quest.getLocationLabel() : quest.getPresentation().getLocationLabel());
        addItem(items, "quest_status", "Status", quest.getPresentation() == null ? null : quest.getPresentation().getStatusLabel());
        addItem(items, "quest_posted_by", "Posted by", quest.getCreatorUsername());

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_quest_detail")
                .title("Quest")
                .summary("Showing the details for " + quest.getTitle() + ".")
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewApplicationDetail(AppUser currentUser, Long applicationId) {
        if (currentUser == null || applicationId == null) {
            return null;
        }

        QuestApplicationDetailResponseDTO detail = questService.getApplicationDetailResponseById(applicationId, currentUser);
        QuestApplicationResponseDTO application = detail == null ? null : detail.getSummary();
        QuestResponseDTO quest = detail == null ? null : detail.getQuest();
        if (application == null) {
            return null;
        }

        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_application_query", "Application", "#" + application.getId());
        addItem(items, "target_quest_query", "Quest", application.getQuestTitle());
        addItem(items, "application_status", "Status", application.getPresentation() == null ? null : application.getPresentation().getStatusLabel());
        addItem(items, "application_posted_by", "Posted by", application.getQuestCreatorUsername());
        addItem(items, "application_message", "Message", application.getMessage());
        addItem(items, "application_proposed_price", "Proposed price",
                application.getProposedPrice() == null ? null : application.getProposedPrice().stripTrailingZeros().toPlainString());
        addItem(items, "application_scheduled_at", "Schedule",
                quest != null && quest.getScheduledAt() != null
                        ? formatDateTime(quest.getScheduledAt())
                        : application.getQuestScheduledAt() == null ? null : formatDateTime(application.getQuestScheduledAt()));
        addItem(items, "application_location", "Location",
                quest != null && quest.getPresentation() != null
                        ? quest.getPresentation().getLocationLabel()
                        : quest == null ? null : quest.getLocationLabel());

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_application_detail")
                .title("Application")
                .summary("Showing the details for your application on " + application.getQuestTitle() + ".")
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewCircleDraft(String circleName) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "circle_name", "Circle name", circleName);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("create_circle")
                .title("Circle draft")
                .summary(circleName == null || circleName.isBlank()
                        ? "A new circle is being prepared."
                        : "Review the new circle before confirmation.")
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewCreateCircleRequestDraft(String targetUsername) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_user", "Person", targetUsername);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("create_circle_request")
                .title("Circle request draft")
                .summary(targetUsername == null || targetUsername.isBlank()
                        ? "A new circle request is being prepared."
                        : "Review the connection invite before confirmation.")
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewAcceptCircleRequestDraft(String targetUsername) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_user", "Person", targetUsername);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("accept_circle_request")
                .title("Circle request acceptance review")
                .summary("Review the incoming circle request you are about to accept.")
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewDeleteCircleRequestDraft(String targetUsername, boolean incoming) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_user", "Person", targetUsername);
        addItem(items, "circle_request_direction", "Direction", incoming ? "Incoming request" : "Outgoing invite");
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("delete_circle_request")
                .title(incoming ? "Circle request decline review" : "Circle invite cancellation review")
                .summary(incoming
                        ? "Review the incoming circle request you are about to decline."
                        : "Review the outgoing circle invite you are about to cancel.")
                .items(items)
                .tone("warning")
                .build();
    }

    public VisionCapabilityPreviewDTO previewUpdateCircleDraft(
            String currentCircleName,
            String draftCircleName
    ) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_circle_query", "Circle", currentCircleName);
        addItem(items, "circle_name", "New name", draftCircleName);
        String summary = draftCircleName == null || draftCircleName.isBlank()
                ? "The current circle is loaded. Add the new circle name before confirmation."
                : "Review the circle rename before confirmation.";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("update_circle")
                .title("Circle update draft")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewDeleteCircleDraft(String currentCircleName, String memberCountLabel) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_circle_query", "Circle", currentCircleName);
        addItem(items, "circle_member_count", "Members", memberCountLabel);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("delete_circle")
                .title("Circle deletion review")
                .summary("Review the circle you are about to delete.")
                .items(items)
                .tone("warning")
                .build();
    }

    public VisionCapabilityPreviewDTO previewProfileDraft(AppUser currentUser, String username, String profileDescription) {
        if (currentUser == null) {
            return null;
        }

        AppUser existingUser = appUserService.getAppUser(currentUser.getId());
        String effectiveUsername = hasText(username) ? username.trim() : existingUser.getUsername();
        String effectiveDescription = profileDescription != null ? profileDescription.trim() : existingUser.getProfileDescription();
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "profile_username", "Username", effectiveUsername);
        addItem(items, "profile_description", "Profile description", effectiveDescription);
        addItem(items, "profile_email", "Email", existingUser.getEmail());

        int changedFieldCount = 0;
        if (hasText(username) && !username.trim().equals(existingUser.getUsername())) {
            changedFieldCount++;
        }
        if (profileDescription != null && !profileDescription.trim().equals(nullToEmpty(existingUser.getProfileDescription()))) {
            changedFieldCount++;
        }

        String summary = changedFieldCount == 0
                ? "Your current profile values are loaded. Add a username or profile description change to continue."
                : "Review " + changedFieldCount + " profile change" + (changedFieldCount == 1 ? "" : "s") + " before confirmation.";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("update_profile")
                .title("Profile draft")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewProfileLocationDraft(AppUser currentUser, String locationMode, String locationLabel) {
        if (currentUser == null) {
            return null;
        }

        AppUser existingUser = appUserService.getAppUser(currentUser.getId());
        UserLocationSettingsDTO currentSettings = appUserMgr.toDto(existingUser).getLocationSettings();
        String effectiveMode = hasText(locationMode)
                ? locationMode.trim()
                : currentSettings == null || currentSettings.getMode() == null ? null : currentSettings.getMode().name();
        String effectiveLabel = locationLabel != null
                ? locationLabel.trim()
                : currentSettings == null ? null : currentSettings.getLabel();
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "profile_location_mode", "Location mode", effectiveMode);
        addItem(items, "profile_location_label", "Location", effectiveLabel);

        String summary = hasText(locationMode) || locationLabel != null
                ? "Review the profile location changes before confirmation."
                : "Your current profile location values are loaded. Add a location mode or location label change to continue.";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("update_profile_location")
                .title("Profile location draft")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    public VisionResolvedQuestTarget resolveApplicationQuest(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedQuestTarget.unresolved("What quest should I apply to? Say the quest title or quest id.");
        }

        Long questId = extractQuestId(query);
        if (questId != null) {
            try {
                QuestResponseDTO quest = questService.getQuestResponseById(questId, currentUser);
                if (quest.getAllowedActions() == null || !quest.getAllowedActions().contains(QuestAllowedActionDTO.APPLY)) {
                    return VisionResolvedQuestTarget.unresolved("You cannot apply to quest #" + questId + ".");
                }
                return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
            } catch (RuntimeException ignored) {
                return VisionResolvedQuestTarget.unresolved("I could not find an applyable quest with id " + questId + ".");
            }
        }

        String normalizedQuery = normalizeEntityQuery(SemanticEntityFamily.QUEST, query).toLowerCase(Locale.ROOT);
        List<QuestResponseDTO> candidates = questService.getAllQuestResponses(currentUser).stream()
                .filter(quest -> quest.getAllowedActions() != null && quest.getAllowedActions().contains(QuestAllowedActionDTO.APPLY))
                .filter(quest -> matchesQuestQuery(quest, normalizedQuery))
                .toList();
        if (candidates.isEmpty()) {
            return VisionResolvedQuestTarget.unresolved("I could not find one open quest you can apply to from \"" + query.trim() + "\". Say the exact quest title or quest id.");
        }

        List<QuestResponseDTO> exactTitleCandidates = candidates.stream()
                .filter(quest -> quest.getTitle() != null && quest.getTitle().trim().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactTitleCandidates.size() == 1) {
            QuestResponseDTO quest = exactTitleCandidates.get(0);
            return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
        }

        if (candidates.size() == 1) {
            QuestResponseDTO quest = candidates.get(0);
            return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
        }

        String suggestions = candidates.stream()
                .limit(3)
                .map(quest -> "#" + quest.getId() + " " + quest.getTitle())
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching quests");
        return VisionResolvedQuestTarget.unresolved("I found several applyable quests matching \"" + query.trim() + "\": " + suggestions + ". Say the exact quest title or quest id.");
    }

    public VisionCapabilityPreviewDTO previewApplicationDraft(
            String questTitle,
            String questCreatorUsername,
            String rewardLabel,
            boolean priceRequired,
            String applicationMessage,
            String proposedPrice
    ) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_quest_query", "Quest", questTitle);
        addItem(items, "application_quest_creator", "Posted by", questCreatorUsername);
        addItem(items, "application_quest_reward", "Quest reward", rewardLabel);
        addItem(items, "application_message", "Application message", applicationMessage);
        if (priceRequired) {
            addItem(items, "application_proposed_price", "Proposed price", proposedPrice);
        }

        String summary = priceRequired
                ? "Review the quest target, message, and proposed price before confirmation."
                : "Review the quest target and application message before confirmation.";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("create_application")
                .title("Application draft")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    public VisionResolvedApplicationTarget resolveMyPendingApplication(
            AppUser currentUser,
            String query,
            ApplicationAllowedActionDTO requiredAction
    ) {
        if (!hasText(query)) {
            return VisionResolvedApplicationTarget.unresolved("What application should I use? Say the quest title or quest id.");
        }

        Long questId = extractQuestId(query);
        List<QuestApplicationResponseDTO> candidates = questApplicationService.getApplicationsForApplicant(currentUser).stream()
                .filter(application -> application.getAllowedActions() != null && application.getAllowedActions().contains(requiredAction))
                .filter(application -> matchesApplicationQuery(application, query, questId))
                .toList();
        if (candidates.isEmpty()) {
            return VisionResolvedApplicationTarget.unresolved(requiredAction == ApplicationAllowedActionDTO.WITHDRAW
                    ? "I could not find one pending application you can withdraw from \"" + query.trim() + "\". Say the exact quest title or quest id."
                    : "I could not find one pending application you can edit from \"" + query.trim() + "\". Say the exact quest title or quest id.");
        }

        List<QuestApplicationResponseDTO> exactTitleCandidates = candidates.stream()
                .filter(application -> application.getQuestTitle() != null
                        && application.getQuestTitle().trim().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactTitleCandidates.size() == 1) {
            return toResolvedApplicationTarget(exactTitleCandidates.get(0));
        }

        if (candidates.size() == 1) {
            return toResolvedApplicationTarget(candidates.get(0));
        }

        String suggestions = candidates.stream()
                .limit(3)
                .map(application -> "#" + application.getQuestId() + " " + application.getQuestTitle())
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching applications");
        return VisionResolvedApplicationTarget.unresolved("I found several pending applications matching \"" + query.trim() + "\": "
                + suggestions + ". Say the exact quest title or quest id.");
    }

    public VisionResolvedApplicationTarget resolveMyApplicationDetail(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedApplicationTarget.unresolved("What application should I open? Say the application id or the exact quest title.");
        }

        Long applicationId = extractApplicationId(query);
        List<QuestApplicationResponseDTO> applications = questApplicationService.getApplicationsForApplicant(currentUser);
        if (applicationId != null) {
            return applications.stream()
                    .filter(application -> applicationId.equals(application.getId()))
                    .findFirst()
                    .map(this::toResolvedApplicationTarget)
                    .orElseGet(() -> VisionResolvedApplicationTarget.unresolved(
                            "I could not find one application with id " + applicationId + "."
                    ));
        }

        List<QuestApplicationResponseDTO> candidates = applications.stream()
                .filter(application -> matchesApplicationDetailQuery(application, query))
                .toList();
        if (candidates.isEmpty()) {
            return VisionResolvedApplicationTarget.unresolved(
                    "I could not find one application from \"" + query.trim() + "\". Say the exact quest title or application id."
            );
        }

        List<QuestApplicationResponseDTO> exactTitleCandidates = candidates.stream()
                .filter(application -> application.getQuestTitle() != null
                        && application.getQuestTitle().trim().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactTitleCandidates.size() == 1) {
            return toResolvedApplicationTarget(exactTitleCandidates.get(0));
        }
        if (candidates.size() == 1) {
            return toResolvedApplicationTarget(candidates.get(0));
        }

        String suggestions = candidates.stream()
                .limit(3)
                .map(application -> "#" + application.getId() + " " + application.getQuestTitle())
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching applications");
        return VisionResolvedApplicationTarget.unresolved(
                "I found several applications matching \"" + query.trim() + "\": " + suggestions + ". Say the exact quest title or application id."
        );
    }

    public VisionResolvedQuestTarget resolveVisibleQuest(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedQuestTarget.unresolved("What quest should I open? Say the quest title or quest id.");
        }

        Long questId = extractQuestId(query);
        if (questId != null) {
            try {
                QuestResponseDTO quest = questService.getQuestResponseById(questId, currentUser);
                return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
            } catch (RuntimeException ignored) {
                return VisionResolvedQuestTarget.unresolved("I could not find one visible quest with id " + questId + ".");
            }
        }

        String normalizedQuery = normalizeEntityQuery(SemanticEntityFamily.QUEST, query).toLowerCase(Locale.ROOT);
        List<QuestResponseDTO> candidates = questService.getAllQuestResponses(currentUser).stream()
                .filter(quest -> matchesQuestQuery(quest, normalizedQuery))
                .toList();
        if (candidates.isEmpty()) {
            return VisionResolvedQuestTarget.unresolved("I could not find one visible quest from \"" + query.trim() + "\". Say the exact quest title or quest id.");
        }

        List<QuestResponseDTO> exactTitleCandidates = candidates.stream()
                .filter(quest -> quest.getTitle() != null && quest.getTitle().trim().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactTitleCandidates.size() == 1) {
            QuestResponseDTO quest = exactTitleCandidates.get(0);
            return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
        }
        if (candidates.size() == 1) {
            QuestResponseDTO quest = candidates.get(0);
            return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
        }

        String suggestions = candidates.stream()
                .limit(3)
                .map(quest -> "#" + quest.getId() + " " + quest.getTitle())
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching quests");
        return VisionResolvedQuestTarget.unresolved("I found several visible quests matching \"" + query.trim() + "\": " + suggestions + ". Say the exact quest title or quest id.");
    }

    public VisionResolvedUserTarget resolveUserProfileTarget(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedUserTarget.unresolved("What profile should I open? Say a username, email, or user id.");
        }

        Long userId = extractUserId(query);
        if (userId != null) {
            try {
                AppUser user = appUserService.getAppUser(userId);
                if (user == null) {
                    return VisionResolvedUserTarget.unresolved("I could not find one profile with id " + userId + ".");
                }
                return VisionResolvedUserTarget.resolved(user.getId(), user.getUsername());
            } catch (RuntimeException ignored) {
                return VisionResolvedUserTarget.unresolved("I could not find one profile with id " + userId + ".");
            }
        }

        String normalizedTargetQuery = normalizeEntityQuery(SemanticEntityFamily.USER, query).toLowerCase(Locale.ROOT);
        List<AppUser> matches = appUserRepository.searchByUsernameOrEmail(normalizedTargetQuery);
        if (matches.isEmpty()) {
            return VisionResolvedUserTarget.unresolved("I could not identify one profile for \"" + query.trim() + "\".");
        }
        List<AppUser> exactMatches = matches.stream()
                .filter(candidate -> candidate.getUsername() != null && candidate.getUsername().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactMatches.size() == 1) {
            AppUser target = exactMatches.getFirst();
            return VisionResolvedUserTarget.resolved(target.getId(), target.getUsername());
        }
        if (matches.size() == 1) {
            AppUser target = matches.getFirst();
            return VisionResolvedUserTarget.resolved(target.getId(), target.getUsername());
        }
        String suggestions = matches.stream()
                .limit(3)
                .map(AppUser::getUsername)
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching users");
        return VisionResolvedUserTarget.unresolved("I found several profiles matching \"" + query.trim() + "\": " + suggestions + ". Say the exact username or user id.");
    }

    public VisionResolvedManagedApplicationTarget resolveManagedPendingApplication(
            AppUser currentUser,
            String questQuery,
            String applicantQuery,
            ApplicationAllowedActionDTO requiredAction
    ) {
        VisionResolvedQuestTarget questTarget = resolveManagedQuest(currentUser, questQuery);
        if (!questTarget.resolved()) {
            return VisionResolvedManagedApplicationTarget.unresolved(questTarget.blockingMessage());
        }
        if (!hasText(applicantQuery)) {
            return VisionResolvedManagedApplicationTarget.unresolved("Who is the applicant? Say the applicant username.");
        }

        String normalizedApplicantQuery = SearchQueryNormalizer.normalize(applicantQuery).toLowerCase(Locale.ROOT);
        List<QuestApplicationResponseDTO> candidates = questApplicationService.getApplicationsForQuest(questTarget.questId(), currentUser).stream()
                .filter(application -> application.getAllowedActions() != null && application.getAllowedActions().contains(requiredAction))
                .filter(application -> matchesApplicantQuery(application, applicantQuery, normalizedApplicantQuery))
                .toList();
        if (candidates.isEmpty()) {
            return VisionResolvedManagedApplicationTarget.unresolved("I could not find one pending application for \"" + applicantQuery.trim()
                    + "\" on " + questTarget.questTitle() + ".");
        }

        List<QuestApplicationResponseDTO> exactCandidates = candidates.stream()
                .filter(application -> application.getApplicantUsername() != null
                        && application.getApplicantUsername().trim().equalsIgnoreCase(applicantQuery.trim()))
                .toList();
        if (exactCandidates.size() == 1) {
            return toResolvedManagedApplicationTarget(exactCandidates.get(0));
        }
        if (candidates.size() == 1) {
            return toResolvedManagedApplicationTarget(candidates.get(0));
        }

        String suggestions = candidates.stream()
                .limit(3)
                .map(QuestApplicationResponseDTO::getApplicantUsername)
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching applicants");
        return VisionResolvedManagedApplicationTarget.unresolved("I found several pending applicants matching \"" + applicantQuery.trim()
                + "\" on " + questTarget.questTitle() + ": " + suggestions + ". Say the exact username.");
    }

    public VisionCapabilityPreviewDTO previewUpdateApplicationDraft(
            String questTitle,
            String questCreatorUsername,
            String rewardLabel,
            boolean priceRequired,
            String currentMessage,
            String currentPrice,
            String draftMessage,
            String draftPrice
    ) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_quest_query", "Quest", questTitle);
        addItem(items, "application_quest_creator", "Posted by", questCreatorUsername);
        addItem(items, "application_quest_reward", "Quest reward", rewardLabel);
        addItem(items, "application_existing_message", "Current message", currentMessage);
        addItem(items, "application_message", "New message", draftMessage);
        if (priceRequired) {
            addItem(items, "application_existing_proposed_price", "Current price", currentPrice);
            addItem(items, "application_proposed_price", "New price", draftPrice);
        }

        String summary = draftMessage == null && draftPrice == null
                ? "The current application is loaded. Add the fields you want to change before confirmation."
                : "Review the application changes before confirmation. Unchanged values will be kept.";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("update_application")
                .title("Application update draft")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewWithdrawApplicationDraft(
            String questTitle,
            String questCreatorUsername,
            String rewardLabel,
            String currentMessage,
            String currentPrice
    ) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_quest_query", "Quest", questTitle);
        addItem(items, "application_quest_creator", "Posted by", questCreatorUsername);
        addItem(items, "application_quest_reward", "Quest reward", rewardLabel);
        addItem(items, "application_existing_message", "Current message", currentMessage);
        addItem(items, "application_existing_proposed_price", "Current price", currentPrice);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("withdraw_application")
                .title("Application withdrawal review")
                .summary("Review the pending application you are about to withdraw.")
                .items(items)
                .tone("warning")
                .build();
    }

    public VisionCapabilityPreviewDTO previewManagedApplicationDecisionDraft(
            String capabilityId,
            String title,
            String summary,
            String questTitle,
            String applicantUsername,
            String currentMessage,
            String currentPrice
    ) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_quest_query", "Quest", questTitle);
        addItem(items, "target_user", "Applicant", applicantUsername);
        addItem(items, "application_existing_message", "Application message", currentMessage);
        addItem(items, "application_existing_proposed_price", "Proposed price", currentPrice);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId(capabilityId)
                .title(title)
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    public CircleGroupResponseDTO createCircle(String circleName, AppUser currentUser) {
        return circleService.createCircle(CircleGroupRequestDTO.builder().name(circleName).build(), currentUser);
    }

    public VisionResolvedUserTarget resolveCircleRequestRecipient(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedUserTarget.unresolved("Who should receive the circle request? Say a username, email, or name fragment.");
        }

        String normalizedTargetQuery = normalizeEntityQuery(SemanticEntityFamily.USER, query).toLowerCase(Locale.ROOT);
        List<AppUser> matches = appUserRepository.searchByUsernameOrEmail(normalizedTargetQuery).stream()
                .filter(candidate -> candidate != null && currentUser != null && !candidate.getId().equals(currentUser.getId()))
                .toList();
        if (matches.isEmpty()) {
            return VisionResolvedUserTarget.unresolved("I could not identify one person for \"" + query.trim() + "\".");
        }
        List<AppUser> exactMatches = matches.stream()
                .filter(candidate -> candidate.getUsername() != null && candidate.getUsername().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactMatches.size() == 1) {
            AppUser target = exactMatches.getFirst();
            return VisionResolvedUserTarget.resolved(target.getId(), target.getUsername());
        }
        if (matches.size() == 1) {
            AppUser target = matches.getFirst();
            return VisionResolvedUserTarget.resolved(target.getId(), target.getUsername());
        }
        String suggestions = matches.stream()
                .limit(3)
                .map(AppUser::getUsername)
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching users");
        return VisionResolvedUserTarget.unresolved("I found several people matching \"" + query.trim() + "\": " + suggestions + ". Say the exact username.");
    }

    public VisionResolvedCircleRequestTarget resolveIncomingCircleRequest(AppUser currentUser, String query) {
        return resolveCircleRequest(currentUser, query, true);
    }

    public VisionResolvedCircleRequestTarget resolveAccessiblePendingCircleRequest(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedCircleRequestTarget.unresolved("Who is the person on this pending circle request? Say the exact username.");
        }
        VisionResolvedCircleRequestTarget incoming = resolveCircleRequest(currentUser, query, true);
        if (incoming.resolved()) {
            return incoming;
        }
        VisionResolvedCircleRequestTarget outgoing = resolveCircleRequest(currentUser, query, false);
        if (outgoing.resolved()) {
            return outgoing;
        }
        if (incoming.blockingMessage() != null && outgoing.blockingMessage() != null) {
            return VisionResolvedCircleRequestTarget.unresolved("I could not find one pending circle request for \"" + query.trim() + "\".");
        }
        return incoming.blockingMessage() == null ? outgoing : incoming;
    }

    public VisionResolvedCircleTarget resolveOwnedCircle(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedCircleTarget.unresolved("What circle should I use? Say the circle name or circle id.");
        }

        Long circleId = extractCircleId(query);
        List<CircleGroupResponseDTO> circles = circleService.getCircles(currentUser);
        List<CircleGroupResponseDTO> candidates = circles.stream()
                .filter(circle -> matchesCircleQuery(circle, query, circleId))
                .toList();
        if (candidates.isEmpty()) {
            return VisionResolvedCircleTarget.unresolved("I could not find one owned circle from \"" + query.trim() + "\". Say the exact circle name or circle id.");
        }
        List<CircleGroupResponseDTO> exactCandidates = candidates.stream()
                .filter(circle -> circle.getName() != null && circle.getName().trim().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactCandidates.size() == 1) {
            return toResolvedCircleTarget(exactCandidates.get(0));
        }
        if (candidates.size() == 1) {
            return toResolvedCircleTarget(candidates.get(0));
        }
        String suggestions = candidates.stream()
                .limit(3)
                .map(circle -> "#" + circle.getId() + " " + circle.getName())
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching circles");
        return VisionResolvedCircleTarget.unresolved("I found several circles matching \"" + query.trim() + "\": " + suggestions + ". Say the exact circle name or circle id.");
    }

    public CircleGroupResponseDTO updateCircle(AppUser currentUser, Long circleId, String circleName) {
        return circleService.updateCircle(circleId, CircleGroupRequestDTO.builder().name(circleName).build(), currentUser);
    }

    public void deleteCircle(AppUser currentUser, Long circleId) {
        circleService.deleteCircle(circleId, currentUser);
    }

    public CircleRequestResponseDTO createCircleRequest(AppUser currentUser, Long targetUserId) {
        return circleService.createCircleRequest(CircleRequestCreateDTO.builder().recipientId(targetUserId).build(), currentUser);
    }

    public CircleRequestResponseDTO acceptCircleRequest(AppUser currentUser, Long requestId) {
        return circleService.acceptCircleRequest(requestId, currentUser);
    }

    public void deleteCircleRequest(AppUser currentUser, Long requestId) {
        circleService.deleteCircleRequest(requestId, currentUser);
    }

    public QuestApplicationResponseDTO createApplication(
            AppUser currentUser,
            Long questId,
            String message,
            String proposedPrice
    ) {
        BigDecimal parsedPrice = hasText(proposedPrice)
                ? new BigDecimal(proposedPrice.trim().replace(',', '.'))
                : null;
        return questApplicationService.applyForQuest(
                questId,
                QuestApplicationRequestDTO.builder()
                        .message(message)
                        .proposedPrice(parsedPrice)
                        .build(),
                currentUser
        );
    }

    public QuestApplicationResponseDTO updateApplication(
            AppUser currentUser,
            Long questId,
            String message,
            String proposedPrice
    ) {
        BigDecimal parsedPrice = hasText(proposedPrice)
                ? new BigDecimal(proposedPrice.trim().replace(',', '.'))
                : null;
        return questApplicationService.updateMyApplication(
                questId,
                QuestApplicationRequestDTO.builder()
                        .message(message)
                        .proposedPrice(parsedPrice)
                        .build(),
                currentUser
        );
    }

    public QuestApplicationResponseDTO withdrawApplication(AppUser currentUser, Long questId) {
        return questApplicationService.withdrawMyApplication(questId, currentUser);
    }

    public QuestApplicationResponseDTO approveManagedApplication(AppUser currentUser, Long questId, Long applicationId) {
        return questApplicationService.approveApplication(questId, applicationId, currentUser);
    }

    public QuestApplicationResponseDTO declineManagedApplication(AppUser currentUser, Long questId, Long applicationId) {
        return questApplicationService.declineApplication(questId, applicationId, currentUser);
    }

    public AppUserResponseDTO updateProfile(AppUser currentUser, String username, String profileDescription) {
        if (currentUser == null) {
            return null;
        }

        AppUser existingUser = appUserService.getAppUser(currentUser.getId());
        AppUserRequestDTO request = AppUserRequestDTO.builder()
                .email(existingUser.getEmail())
                .username(hasText(username) ? username.trim() : existingUser.getUsername())
                .profileDescription(profileDescription == null ? existingUser.getProfileDescription() : profileDescription.trim())
                .profileAvatarDataUrl(existingUser.getProfileAvatarDataUrl())
                .locationSettings(toLocationRequest(appUserMgr.toDto(existingUser).getLocationSettings()))
                .build();
        AppUser updatedUser = appUserService.updateAppUser(existingUser.getId(), request);
        return appUserMgr.withProfileStats(
                appUserMgr.toDto(updatedUser),
                appUserService.countQuestsByCreatorId(updatedUser.getId()),
                appUserService.getOpenQuestsByCreatorId(updatedUser.getId())
        );
    }

    public AppUserResponseDTO updateProfileLocation(AppUser currentUser, String locationMode, String locationLabel) {
        if (currentUser == null) {
            return null;
        }

        AppUser existingUser = appUserService.getAppUser(currentUser.getId());
        UserLocationSettingsDTO currentSettings = appUserMgr.toDto(existingUser).getLocationSettings();
        UserLocationMode mode = locationMode == null || locationMode.isBlank()
                ? currentSettings == null || currentSettings.getMode() == null ? UserLocationMode.OFF : currentSettings.getMode()
                : UserLocationMode.valueOf(locationMode.trim().toUpperCase(Locale.ROOT));
        AppUserRequestDTO request = AppUserRequestDTO.builder()
                .email(existingUser.getEmail())
                .username(existingUser.getUsername())
                .profileDescription(existingUser.getProfileDescription())
                .profileAvatarDataUrl(existingUser.getProfileAvatarDataUrl())
                .locationSettings(toLocationRequestForProfileLocation(currentSettings, mode, locationLabel))
                .build();
        AppUser updatedUser = appUserService.updateAppUser(existingUser.getId(), request);
        return appUserMgr.withProfileStats(
                appUserMgr.toDto(updatedUser),
                appUserService.countQuestsByCreatorId(updatedUser.getId()),
                appUserService.getOpenQuestsByCreatorId(updatedUser.getId())
        );
    }

    private UserLocationSettingsRequestDTO toLocationRequest(UserLocationSettingsDTO source) {
        if (source == null) {
            return null;
        }

        return UserLocationSettingsRequestDTO.builder()
                .mode(source.getMode())
                .defaultRadiusKm(source.getDefaultRadiusKm())
                .exactVisibilityScope(source.getExactVisibilityScope())
                .exactVisibleCircleIds(source.getExactVisibleCircleIds())
                .exactVisibleUserIds(source.getExactVisibleUserIds())
                .provider(source.getProvider())
                .providerPlaceId(source.getProviderPlaceId())
                .label(source.getLabel())
                .countryCode(source.getCountryCode())
                .country(source.getCountry())
                .locality(source.getLocality())
                .postalCode(source.getPostalCode())
                .street(source.getStreet())
                .houseNumber(source.getHouseNumber())
                .latitude(source.getLatitude())
                .longitude(source.getLongitude())
                .resolvedAt(source.getResolvedAt())
                .build();
    }

    private UserLocationSettingsRequestDTO toLocationRequestForProfileLocation(
            UserLocationSettingsDTO source,
            UserLocationMode mode,
            String locationLabel
    ) {
        UserLocationSettingsRequestDTO.UserLocationSettingsRequestDTOBuilder builder = UserLocationSettingsRequestDTO.builder()
                .mode(mode)
                .defaultRadiusKm(source == null ? null : source.getDefaultRadiusKm())
                .exactVisibilityScope(source == null ? null : source.getExactVisibilityScope())
                .exactVisibleCircleIds(source == null ? null : source.getExactVisibleCircleIds())
                .exactVisibleUserIds(source == null ? null : source.getExactVisibleUserIds());
        if (mode == UserLocationMode.OFF) {
            return builder.build();
        }

        String nextLabel = locationLabel != null ? locationLabel.trim() : source == null ? null : source.getLabel();
        boolean keepResolvedData = source != null
                && nextLabel != null
                && nextLabel.equals(nullToEmpty(source.getLabel()).trim());
        if (keepResolvedData) {
            return builder
                    .provider(source.getProvider())
                    .providerPlaceId(source.getProviderPlaceId())
                    .label(source.getLabel())
                    .countryCode(source.getCountryCode())
                    .country(source.getCountry())
                    .locality(source.getLocality())
                    .postalCode(source.getPostalCode())
                    .street(source.getStreet())
                    .houseNumber(source.getHouseNumber())
                    .latitude(source.getLatitude())
                    .longitude(source.getLongitude())
                    .resolvedAt(source.getResolvedAt())
                    .build();
        }

        return builder
                .label(nextLabel)
                .provider(null)
                .providerPlaceId(null)
                .countryCode(null)
                .country(null)
                .locality(null)
                .postalCode(null)
                .street(null)
                .houseNumber(null)
                .latitude(null)
                .longitude(null)
                .resolvedAt(null)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private Long extractQuestId(String query) {
        if (!hasText(query)) {
            return null;
        }
        Matcher matcher = QUEST_ID_PATTERN.matcher(query.trim());
        if (!matcher.find()) {
            return null;
        }
        String direct = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        if (direct == null || direct.isBlank()) {
            return null;
        }
        return Long.parseLong(direct);
    }

    private Long extractApplicationId(String query) {
        if (!hasText(query)) {
            return null;
        }
        Matcher matcher = APPLICATION_ID_PATTERN.matcher(query.trim());
        if (!matcher.find()) {
            return null;
        }
        String direct = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        if (direct == null || direct.isBlank()) {
            return null;
        }
        return Long.parseLong(direct);
    }

    private Long extractUserId(String query) {
        if (!hasText(query)) {
            return null;
        }
        Matcher matcher = USER_ID_PATTERN.matcher(query.trim());
        if (!matcher.find()) {
            return null;
        }
        String direct = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        if (direct == null || direct.isBlank()) {
            return null;
        }
        return Long.parseLong(direct);
    }

    private Long extractCircleId(String query) {
        if (!hasText(query)) {
            return null;
        }
        Matcher matcher = CIRCLE_ID_PATTERN.matcher(query.trim());
        if (!matcher.find()) {
            return null;
        }
        String direct = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        if (direct == null || direct.isBlank()) {
            return null;
        }
        return Long.parseLong(direct);
    }

    private boolean matchesQuestQuery(QuestResponseDTO quest, String query) {
        if (!hasText(query)) {
            return false;
        }
        String haystack = String.join(" ",
                nullToEmpty(quest.getTitle()),
                nullToEmpty(quest.getDescription()),
                nullToEmpty(quest.getCreatorUsername()))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(query);
    }

    private boolean matchesApplicationQuery(QuestApplicationResponseDTO application, String rawQuery, Long questId) {
        if (application == null || !hasText(rawQuery)) {
            return false;
        }
        if (questId != null) {
            return questId.equals(application.getQuestId());
        }
        String query = normalizeEntityQuery(SemanticEntityFamily.APPLICATION, rawQuery).toLowerCase(Locale.ROOT);
        String haystack = String.join(" ",
                nullToEmpty(application.getQuestTitle()),
                nullToEmpty(application.getQuestDescription()),
                nullToEmpty(application.getQuestCreatorUsername()))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(query);
    }

    private boolean matchesApplicationDetailQuery(QuestApplicationResponseDTO application, String rawQuery) {
        if (application == null || !hasText(rawQuery)) {
            return false;
        }
        Long applicationId = extractApplicationId(rawQuery);
        if (applicationId != null) {
            return applicationId.equals(application.getId());
        }
        String query = normalizeEntityQuery(SemanticEntityFamily.APPLICATION, rawQuery).toLowerCase(Locale.ROOT);
        String haystack = String.join(" ",
                nullToEmpty(application.getQuestTitle()),
                nullToEmpty(application.getQuestDescription()),
                nullToEmpty(application.getQuestCreatorUsername()),
                "#" + application.getId())
                .toLowerCase(Locale.ROOT);
        return haystack.contains(query);
    }

    private boolean matchesApplicantQuery(QuestApplicationResponseDTO application, String rawQuery, String normalizedQuery) {
        if (application == null || !hasText(rawQuery)) {
            return false;
        }
        String haystack = String.join(" ",
                nullToEmpty(application.getApplicantUsername()),
                nullToEmpty(application.getApplicantProfileDescription()))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(normalizedQuery);
    }

    private boolean matchesCircleQuery(CircleGroupResponseDTO circle, String rawQuery, Long circleId) {
        if (circle == null || !hasText(rawQuery)) {
            return false;
        }
        if (circleId != null) {
            return circleId.equals(circle.getId());
        }
        String query = normalizeEntityQuery(SemanticEntityFamily.CIRCLE, rawQuery).toLowerCase(Locale.ROOT);
        String haystack = String.join(" ",
                nullToEmpty(circle.getName()),
                nullToEmpty(circle.getMemberPreviewLabel()))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(query);
    }

    private VisionResolvedQuestTarget resolveManagedQuest(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedQuestTarget.unresolved("What quest should I use? Say the quest title or quest id.");
        }

        Long questId = extractQuestId(query);
        List<QuestResponseDTO> candidates = questService.getAllQuestResponses(currentUser).stream()
                .filter(quest -> quest.getAllowedActions() != null && quest.getAllowedActions().contains(QuestAllowedActionDTO.VIEW_APPLICATIONS))
                .filter(quest -> matchesManagedQuestQuery(quest, query, questId))
                .toList();
        if (candidates.isEmpty()) {
            return VisionResolvedQuestTarget.unresolved("I could not find one quest you can manage from \"" + query.trim() + "\". Say the exact quest title or quest id.");
        }
        List<QuestResponseDTO> exactCandidates = candidates.stream()
                .filter(quest -> quest.getTitle() != null && quest.getTitle().trim().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactCandidates.size() == 1) {
            QuestResponseDTO quest = exactCandidates.get(0);
            return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
        }
        if (candidates.size() == 1) {
            QuestResponseDTO quest = candidates.get(0);
            return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
        }
        String suggestions = candidates.stream()
                .limit(3)
                .map(quest -> "#" + quest.getId() + " " + quest.getTitle())
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching quests");
        return VisionResolvedQuestTarget.unresolved("I found several manageable quests matching \"" + query.trim() + "\": " + suggestions + ". Say the exact quest title or quest id.");
    }

    private boolean matchesManagedQuestQuery(QuestResponseDTO quest, String rawQuery, Long questId) {
        if (quest == null || !hasText(rawQuery)) {
            return false;
        }
        if (questId != null) {
            return questId.equals(quest.getId());
        }
        String query = normalizeEntityQuery(SemanticEntityFamily.QUEST, rawQuery).toLowerCase(Locale.ROOT);
        return matchesQuestQuery(quest, query);
    }

    private boolean requiresApplicationPrice(QuestResponseDTO quest) {
        return quest != null && quest.getAwardAmount() != null && quest.getAwardAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    private VisionResolvedApplicationTarget toResolvedApplicationTarget(QuestApplicationResponseDTO application) {
        boolean priceRequired = application.getProposedPrice() != null && application.getProposedPrice().compareTo(BigDecimal.ZERO) > 0;
        return VisionResolvedApplicationTarget.resolved(
                application.getQuestId(),
                application.getQuestTitle(),
                application.getQuestCreatorUsername(),
                priceRequired,
                application.getProposedPrice() == null ? "Free" : application.getProposedPrice().stripTrailingZeros().toPlainString(),
                application.getMessage(),
                application.getProposedPrice() == null ? null : application.getProposedPrice().stripTrailingZeros().toPlainString(),
                application.getId()
        );
    }

    private VisionResolvedManagedApplicationTarget toResolvedManagedApplicationTarget(QuestApplicationResponseDTO application) {
        return VisionResolvedManagedApplicationTarget.resolved(
                application.getQuestId(),
                application.getQuestTitle(),
                application.getApplicantUsername(),
                application.getMessage(),
                application.getProposedPrice() == null ? null : application.getProposedPrice().stripTrailingZeros().toPlainString(),
                application.getId()
        );
    }

    private VisionResolvedCircleRequestTarget resolveCircleRequest(AppUser currentUser, String query, boolean incoming) {
        if (!hasText(query)) {
            return VisionResolvedCircleRequestTarget.unresolved("Who is the person on this circle request? Say the exact username.");
        }
        List<CircleRequestResponseDTO> requests = incoming
                ? circleService.getIncomingRequests(currentUser)
                : circleService.getOutgoingRequests(currentUser);
        String normalizedQuery = normalizeEntityQuery(SemanticEntityFamily.USER, query).toLowerCase(Locale.ROOT);
        List<CircleRequestResponseDTO> matches = requests.stream()
                .filter(request -> request.getAcceptedAt() == null)
                .filter(request -> matchesCircleRequestQuery(request, query, normalizedQuery))
                .toList();
        if (matches.isEmpty()) {
            return VisionResolvedCircleRequestTarget.unresolved(incoming
                    ? "I could not find one incoming circle request from \"" + query.trim() + "\"."
                    : "I could not find one outgoing circle invite for \"" + query.trim() + "\".");
        }
        List<CircleRequestResponseDTO> exactMatches = matches.stream()
                .filter(request -> request.getCounterpartUsername() != null
                        && request.getCounterpartUsername().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactMatches.size() == 1) {
            return toResolvedCircleRequestTarget(exactMatches.getFirst(), incoming);
        }
        if (matches.size() == 1) {
            return toResolvedCircleRequestTarget(matches.getFirst(), incoming);
        }
        String suggestions = matches.stream()
                .limit(3)
                .map(CircleRequestResponseDTO::getCounterpartUsername)
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching users");
        return VisionResolvedCircleRequestTarget.unresolved("I found several pending circle requests matching \"" + query.trim() + "\": " + suggestions + ". Say the exact username.");
    }

    private boolean matchesCircleRequestQuery(CircleRequestResponseDTO request, String rawQuery, String normalizedQuery) {
        if (!hasText(rawQuery)) {
            return false;
        }
        String haystack = String.join(" ",
                nullToEmpty(request.getCounterpartUsername()),
                nullToEmpty(request.getRequesterUsername()),
                nullToEmpty(request.getRecipientUsername()))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(normalizedQuery);
    }

    private VisionResolvedCircleRequestTarget toResolvedCircleRequestTarget(CircleRequestResponseDTO request, boolean incoming) {
        return VisionResolvedCircleRequestTarget.resolved(
                request.getId(),
                request.getCounterpartUserId(),
                request.getCounterpartUsername(),
                incoming
        );
    }

    private VisionResolvedCircleTarget toResolvedCircleTarget(CircleGroupResponseDTO circle) {
        return VisionResolvedCircleTarget.resolved(
                circle.getId(),
                circle.getName(),
                String.valueOf(circle.getMemberCount())
        );
    }

    private String formatRewardLabel(QuestResponseDTO quest) {
        if (quest == null || quest.getAwardAmount() == null || quest.getAwardAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "Free";
        }
        return quest.getAwardAmount().stripTrailingZeros().toPlainString();
    }

    private String formatDateTime(Instant value) {
        if (value == null) {
            return null;
        }
        return DATE_TIME_FORMAT.format(value);
    }

    private String normalizeEntityQuery(SemanticEntityFamily family, String query) {
        return semanticAliasRegistry.normalizeQuery(family, SearchQueryNormalizer.normalize(query));
    }

    private void addItem(List<VisionSlotSummaryDTO> items, String slotId, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        items.add(VisionSlotSummaryDTO.builder()
                .slotId(slotId)
                .label(label)
                .value(value)
                .build());
    }
}

record VisionResolvedQuestTarget(
        Long questId,
        String questTitle,
        String creatorUsername,
        boolean priceRequired,
        String rewardLabel,
        String blockingMessage
) {
    static VisionResolvedQuestTarget resolved(
            Long questId,
            String questTitle,
            String creatorUsername,
            boolean priceRequired,
            String rewardLabel
    ) {
        return new VisionResolvedQuestTarget(questId, questTitle, creatorUsername, priceRequired, rewardLabel, null);
    }

    static VisionResolvedQuestTarget unresolved(String blockingMessage) {
        return new VisionResolvedQuestTarget(null, null, null, false, null, blockingMessage);
    }

    boolean resolved() {
        return questId != null;
    }
}

record VisionResolvedApplicationTarget(
        Long questId,
        String questTitle,
        String creatorUsername,
        boolean priceRequired,
        String rewardLabel,
        String currentMessage,
        String currentPrice,
        Long applicationId,
        String blockingMessage
) {
    static VisionResolvedApplicationTarget resolved(
            Long questId,
            String questTitle,
            String creatorUsername,
            boolean priceRequired,
            String rewardLabel,
            String currentMessage,
            String currentPrice,
            Long applicationId
    ) {
        return new VisionResolvedApplicationTarget(
                questId,
                questTitle,
                creatorUsername,
                priceRequired,
                rewardLabel,
                currentMessage,
                currentPrice,
                applicationId,
                null
        );
    }

    static VisionResolvedApplicationTarget unresolved(String blockingMessage) {
        return new VisionResolvedApplicationTarget(null, null, null, false, null, null, null, null, blockingMessage);
    }

    boolean resolved() {
        return questId != null;
    }
}

record VisionResolvedManagedApplicationTarget(
        Long questId,
        String questTitle,
        String applicantUsername,
        String currentMessage,
        String currentPrice,
        Long applicationId,
        String blockingMessage
) {
    static VisionResolvedManagedApplicationTarget resolved(
            Long questId,
            String questTitle,
            String applicantUsername,
            String currentMessage,
            String currentPrice,
            Long applicationId
    ) {
        return new VisionResolvedManagedApplicationTarget(
                questId,
                questTitle,
                applicantUsername,
                currentMessage,
                currentPrice,
                applicationId,
                null
        );
    }

    static VisionResolvedManagedApplicationTarget unresolved(String blockingMessage) {
        return new VisionResolvedManagedApplicationTarget(null, null, null, null, null, null, blockingMessage);
    }

    boolean resolved() {
        return questId != null;
    }
}

record VisionResolvedCircleTarget(
        Long circleId,
        String circleName,
        String memberCountLabel,
        String blockingMessage
) {
    static VisionResolvedCircleTarget resolved(Long circleId, String circleName, String memberCountLabel) {
        return new VisionResolvedCircleTarget(circleId, circleName, memberCountLabel, null);
    }

    static VisionResolvedCircleTarget unresolved(String blockingMessage) {
        return new VisionResolvedCircleTarget(null, null, null, blockingMessage);
    }

    boolean resolved() {
        return circleId != null;
    }
}

record VisionResolvedUserTarget(
        Long userId,
        String username,
        String blockingMessage
) {
    static VisionResolvedUserTarget resolved(Long userId, String username) {
        return new VisionResolvedUserTarget(userId, username, null);
    }

    static VisionResolvedUserTarget unresolved(String blockingMessage) {
        return new VisionResolvedUserTarget(null, null, blockingMessage);
    }

    boolean resolved() {
        return userId != null;
    }
}

record VisionResolvedCircleRequestTarget(
        Long requestId,
        Long counterpartUserId,
        String counterpartUsername,
        boolean incoming,
        String blockingMessage
) {
    static VisionResolvedCircleRequestTarget resolved(
            Long requestId,
            Long counterpartUserId,
            String counterpartUsername,
            boolean incoming
    ) {
        return new VisionResolvedCircleRequestTarget(requestId, counterpartUserId, counterpartUsername, incoming, null);
    }

    static VisionResolvedCircleRequestTarget unresolved(String blockingMessage) {
        return new VisionResolvedCircleRequestTarget(null, null, null, false, blockingMessage);
    }

    boolean resolved() {
        return requestId != null;
    }
}
