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
import com.themuffinman.app.identity.service.AppUserReadService;
import com.themuffinman.app.identity.service.AppUserService;
import com.themuffinman.app.identity.service.UserProfileViewService;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.location.dto.UserLocationSettingsDTO;
import com.themuffinman.app.location.dto.UserLocationSettingsRequestDTO;
import com.themuffinman.app.location.model.UserLocationMode;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleMemberDTO;
import com.themuffinman.app.social.dto.CircleGroupRequestDTO;
import com.themuffinman.app.social.dto.CircleRequestCreateDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.service.CircleReadService;
import com.themuffinman.app.social.service.CircleService;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.vision.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.vision.dto.DashboardNotificationItemDTO;
import com.themuffinman.app.vision.dto.QuestApplicationDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestAllowedActionDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.vision.dto.QuestDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.vision.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.vision.mapper.QuestNewsMgr;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
@Service
public class VisionCapabilityPreviewService {

    private final AppUserService appUserService;
    private final AppUserReadService appUserReadService;
    private final AppUserMgr appUserMgr;
    private final AppUserRepository appUserRepository;
    private final UserProfileViewService userProfileViewService;
    private final ChatService chatService;
    private final CircleReadService circleReadService;
    private final CircleService circleService;
    private final QuestService questService;
    private final QuestApplicationService questApplicationService;
    private final VisionIdentityPreviewRenderer visionIdentityPreviewRenderer;
    private final VisionFeedPreviewRenderer visionFeedPreviewRenderer;
    private final VisionCapabilityEntityResolutionSupport visionCapabilityEntityResolutionSupport;

    public VisionCapabilityPreviewService(
            AppUserService appUserService,
            AppUserReadService appUserReadService,
            AppUserMgr appUserMgr,
            AppUserRepository appUserRepository,
            UserProfileViewService userProfileViewService,
            ChatService chatService,
            CircleReadService circleReadService,
            CircleService circleService,
            QuestService questService,
            QuestApplicationService questApplicationService,
            QuestNewsService questNewsService,
            QuestNewsMgr questNewsMgr,
            DashboardNotificationAssembler dashboardNotificationAssembler,
            ThingSharingService thingSharingService,
            SemanticAliasRegistry semanticAliasRegistry
    ) {
        this.appUserService = appUserService;
        this.appUserReadService = appUserReadService;
        this.appUserMgr = appUserMgr;
        this.appUserRepository = appUserRepository;
        this.userProfileViewService = userProfileViewService;
        this.chatService = chatService;
        this.circleReadService = circleReadService;
        this.circleService = circleService;
        this.questService = questService;
        this.questApplicationService = questApplicationService;
        this.visionIdentityPreviewRenderer = new VisionIdentityPreviewRenderer(
                appUserService,
                appUserReadService,
                appUserMgr,
                appUserRepository,
                userProfileViewService,
                chatService
        );
        this.visionFeedPreviewRenderer = new VisionFeedPreviewRenderer(
                questNewsService,
                questNewsMgr,
                dashboardNotificationAssembler,
                thingSharingService
        );
        this.visionCapabilityEntityResolutionSupport = new VisionCapabilityEntityResolutionSupport(
                appUserRepository,
                appUserReadService,
                circleReadService,
                questApplicationService,
                questService,
                semanticAliasRegistry
        );
    }

    public VisionCapabilityPreviewDTO previewProfile(AppUser currentUser) {
        return visionIdentityPreviewRenderer.previewProfile(currentUser);
    }

    public VisionCapabilityPreviewDTO previewSettings(AppUser currentUser) {
        return visionIdentityPreviewRenderer.previewSettings(currentUser);
    }

    public VisionCapabilityPreviewDTO previewChatWorkspace(AppUser currentUser) {
        return visionIdentityPreviewRenderer.previewChatWorkspace(currentUser);
    }

    public VisionCapabilityPreviewDTO previewUserProfile(AppUser currentUser, Long profileUserId) {
        return visionIdentityPreviewRenderer.previewUserProfile(currentUser, profileUserId);
    }

    public VisionCapabilityPreviewDTO previewCircles(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        List<CircleGroupResponseDTO> circles = circleReadService.getCircles(currentUser);
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
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
                : circles.size() + " circle" + (circles.size() == 1 ? "" : "s") + ".";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_circles")
                .title("Circles")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewCircleDetail(AppUser currentUser, Long circleId) {
        if (currentUser == null || circleId == null) {
            return null;
        }

        CircleGroupResponseDTO circle = circleReadService.getCircles(currentUser).stream()
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
                .summary("Circle.")
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewCircleDraft(String circleName) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "circle_name", "Circle name", circleName);
        long filledFieldCount = countFilledValues(items);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("create_circle")
                .title("Circle draft")
                .summary(draftSummary(
                        filledFieldCount,
                        "Start the circle draft by adding a circle name.",
                        "Review the circle draft so far. Continue adding the remaining fields.",
                        "Review the new circle before confirmation.",
                        1
                ))
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewCreateCircleRequestDraft(String targetUsername) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_user", "Person", targetUsername);
        long filledFieldCount = countFilledValues(items);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("create_circle_request")
                .title("Circle request draft")
                .summary(draftSummary(
                        filledFieldCount,
                        "Start the circle request by adding the person.",
                        "Review the circle request so far. Continue adding the remaining fields.",
                        "Review the connection invite before confirmation.",
                        1
                ))
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewAcceptCircleRequestDraft(String targetUsername) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_user", "Person", targetUsername);
        long filledFieldCount = countFilledValues(items);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("accept_circle_request")
                .title("Circle request acceptance review")
                .summary(draftSummary(
                        filledFieldCount,
                        "Start the acceptance review by identifying the person.",
                        "Review the circle request acceptance so far.",
                        "Review the incoming circle request you are about to accept.",
                        1
                ))
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewDeleteCircleRequestDraft(String targetUsername, boolean incoming) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_user", "Person", targetUsername);
        addItem(items, "circle_request_direction", "Direction", incoming ? "Incoming request" : "Outgoing invite");
        long filledFieldCount = countFilledValues(items);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("delete_circle_request")
                .title(incoming ? "Circle request decline review" : "Circle invite cancellation review")
                .summary(draftSummary(
                        filledFieldCount,
                        incoming
                                ? "Start the decline review by identifying the person."
                                : "Start the cancellation review by identifying the person.",
                        incoming
                                ? "Review the incoming circle request so far."
                                : "Review the outgoing circle invite so far.",
                        incoming
                                ? "Review the incoming circle request you are about to decline."
                                : "Review the outgoing circle invite you are about to cancel.",
                        2
                ))
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
        long filledFieldCount = countFilledValues(items);
        String summary = draftSummary(
                filledFieldCount,
                "The current circle is loaded. Add the new circle name before confirmation.",
                "Review the circle rename so far. Continue adding the remaining fields.",
                "Review the circle rename before confirmation.",
                2
        );
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
        long filledFieldCount = countFilledValues(items);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("delete_circle")
                .title("Circle deletion review")
                .summary(draftSummary(
                        filledFieldCount,
                        "Start the deletion review by loading the circle.",
                        "Review the circle deletion so far.",
                        "Review the circle you are about to delete.",
                        2
                ))
                .items(items)
                .tone("warning")
                .build();
    }

    public CircleGroupResponseDTO createCircle(String circleName, AppUser currentUser) {
        return circleService.createCircle(CircleGroupRequestDTO.builder().name(circleName).build(), currentUser);
    }

    public VisionResolvedUserTarget resolveCircleRequestRecipient(AppUser currentUser, String query) {
        return visionCapabilityEntityResolutionSupport.resolveCircleRequestRecipient(currentUser, query);
    }

    public VisionResolvedCircleRequestTarget resolveIncomingCircleRequest(AppUser currentUser, String query) {
        return visionCapabilityEntityResolutionSupport.resolveIncomingCircleRequest(currentUser, query);
    }

    public VisionResolvedCircleRequestTarget resolveAccessiblePendingCircleRequest(AppUser currentUser, String query) {
        return visionCapabilityEntityResolutionSupport.resolveAccessiblePendingCircleRequest(currentUser, query);
    }

    public VisionResolvedCircleTarget resolveOwnedCircle(AppUser currentUser, String query) {
        return visionCapabilityEntityResolutionSupport.resolveOwnedCircle(currentUser, query);
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
                    applicationListValue(application));
        }

        String summary = applications.isEmpty()
                ? "No applications."
                : applications.size() + " application" + (applications.size() == 1 ? "" : "s") + ".";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_applications")
                .title("Applications")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewQuestNews(AppUser currentUser) {
        return visionFeedPreviewRenderer.previewQuestNews(currentUser);
    }

    public VisionCapabilityPreviewDTO previewNotifications(AppUser currentUser) {
        return visionFeedPreviewRenderer.previewNotifications(currentUser);
    }

    public VisionCapabilityPreviewDTO previewThings(AppUser currentUser) {
        return visionFeedPreviewRenderer.previewThings(currentUser);
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
        addItem(items, "target_quest_query", "Title", quest.getTitle());
        addItem(items, "quest_description", "Description", quest.getDescription());
        addItem(items, "reward_amount", "Reward", formatRewardLabel(quest));
        addItem(items, "visibility", "Visibility", quest.getAudience() == null ? null : quest.getAudience().name());
        addItem(items, "scheduled_at", "When", quest.getScheduledAt() == null ? null : formatDateTime(quest.getScheduledAt()));
        addItem(items, "location_label", "Location",
                quest.getPresentation() == null ? quest.getLocationLabel() : quest.getPresentation().getLocationLabel());
        addItem(items, "quest_status", "Status", quest.getPresentation() == null ? null : quest.getPresentation().getStatusLabel());
        addItem(items, "quest_posted_by", "Posted by", quest.getCreatorUsername());

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_quest_detail")
                .title("Quest")
                .summary("Quest.")
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewQuestDraft(Map<String, String> slotData) {
        if (slotData == null) {
            return null;
        }

        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "quest_title", "Title", slotData.get("quest_title"));
        addItem(items, "quest_description", "Description", slotData.get("quest_description"));
        addItem(items, "reward_amount", "Reward", formatQuestDraftRewardLabel(slotData));
        addItem(items, "visibility", "Visibility", slotData.get("visibility"));
        addItem(items, "schedule_mode", "Schedule", formatQuestDraftScheduleMode(slotData));
        addItem(items, "scheduled_date", "Date", slotData.get("scheduled_date"));
        addItem(items, "scheduled_time", "Time", slotData.get("scheduled_time"));
        addItem(items, "location_mode", "Location", formatQuestDraftLocationMode(slotData));
        addItem(items, "location_label", "Custom place", slotData.get("location_label"));

        long filledFieldCount = items.stream()
                .map(VisionSlotSummaryDTO::getValue)
                .filter(this::hasText)
                .count();
        String summary;
        if (filledFieldCount == 0) {
            summary = "Start the quest draft by adding a title and description.";
        } else if (filledFieldCount < 3) {
            summary = "Review the quest draft so far. Continue adding the remaining fields.";
        } else {
            summary = "Review the quest draft so far before confirmation.";
        }

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("create_quest")
                .title("Quest draft")
                .summary(summary)
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
                .summary("Application.")
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewProfileDraft(AppUser currentUser, String username, String profileDescription) {
        if (currentUser == null) {
            return null;
        }

        AppUser existingUser = appUserReadService.getAppUser(currentUser.getId());
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

        AppUser existingUser = appUserReadService.getAppUser(currentUser.getId());
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
        return visionCapabilityEntityResolutionSupport.resolveApplicationQuest(currentUser, query);
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
        addItem(items, "application_quest_reward", "Reward", rewardLabel);
        addItem(items, "application_message", "Message", applicationMessage);
        if (priceRequired) {
            addItem(items, "application_proposed_price", "Proposed price", proposedPrice);
        }

        long filledFieldCount = countFilledValues(items);
        String summary = draftSummary(
                filledFieldCount,
                "Start the application draft by choosing a quest and writing your message.",
                "Review the application draft so far. Continue adding the remaining fields.",
                priceRequired
                        ? "Review the quest target, message, and proposed price before confirmation."
                        : "Review the quest target and application message before confirmation.",
                priceRequired ? 3 : 2
        );
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
        return visionCapabilityEntityResolutionSupport.resolveMyPendingApplication(currentUser, query, requiredAction);
    }

    public VisionResolvedApplicationTarget resolveMyApplicationDetail(AppUser currentUser, String query) {
        return visionCapabilityEntityResolutionSupport.resolveMyApplicationDetail(currentUser, query);
    }

    public VisionResolvedQuestTarget resolveVisibleQuest(AppUser currentUser, String query) {
        return visionCapabilityEntityResolutionSupport.resolveVisibleQuest(currentUser, query);
    }

    public VisionResolvedUserTarget resolveUserProfileTarget(AppUser currentUser, String query) {
        return visionCapabilityEntityResolutionSupport.resolveUserProfileTarget(currentUser, query);
    }

    public VisionResolvedManagedApplicationTarget resolveManagedPendingApplication(
            AppUser currentUser,
            String questQuery,
            String applicantQuery,
            ApplicationAllowedActionDTO requiredAction
    ) {
        return visionCapabilityEntityResolutionSupport.resolveManagedPendingApplication(currentUser, questQuery, applicantQuery, requiredAction);
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
        addItem(items, "application_quest_reward", "Reward", rewardLabel);
        addItem(items, "application_existing_message", "Current message", currentMessage);
        addItem(items, "application_message", "New message", draftMessage);
        if (priceRequired) {
            addItem(items, "application_existing_proposed_price", "Current price", currentPrice);
            addItem(items, "application_proposed_price", "New price", draftPrice);
        }

        boolean hasDraftChanges = hasText(draftMessage) || hasText(draftPrice);
        String summary = hasDraftChanges
                ? "Review the application changes before confirmation. Unchanged values will be kept."
                : "The current application is loaded. Add the fields you want to change before confirmation.";
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
        addItem(items, "application_quest_reward", "Reward", rewardLabel);
        addItem(items, "application_existing_message", "Current message", currentMessage);
        addItem(items, "application_existing_proposed_price", "Current price", currentPrice);
        long filledFieldCount = countFilledValues(items);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("withdraw_application")
                .title("Application withdrawal review")
                .summary(draftSummary(
                        filledFieldCount,
                        "Start the withdrawal review by loading the pending application.",
                        "Review the application withdrawal so far.",
                        "Review the pending application you are about to withdraw.",
                        3
                ))
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
        addItem(items, "application_existing_message", "Message", currentMessage);
        addItem(items, "application_existing_proposed_price", "Proposed price", currentPrice);
        long filledFieldCount = countFilledValues(items);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId(capabilityId)
                .title(title)
                .summary(draftSummary(
                        filledFieldCount,
                        "Start the decision review by loading the quest and applicant.",
                        "Review the application decision so far.",
                        summary,
                        3
                ))
                .items(items)
                .tone("info")
                .build();
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

        AppUser existingUser = appUserReadService.getAppUser(currentUser.getId());
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
                appUserReadService.countQuestsByCreatorId(updatedUser.getId()),
                appUserReadService.getOpenQuestsByCreatorId(updatedUser.getId())
        );
    }

    public AppUserResponseDTO updateProfileLocation(AppUser currentUser, String locationMode, String locationLabel) {
        if (currentUser == null) {
            return null;
        }

        AppUser existingUser = appUserReadService.getAppUser(currentUser.getId());
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
                appUserReadService.countQuestsByCreatorId(updatedUser.getId()),
                appUserReadService.getOpenQuestsByCreatorId(updatedUser.getId())
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

    private String formatRewardLabel(QuestResponseDTO quest) {
        return VisionCapabilityPreviewSupport.formatRewardLabel(quest);
    }

    private String formatQuestDraftRewardLabel(Map<String, String> slotData) {
        return VisionCapabilityPreviewSupport.formatQuestDraftRewardLabel(slotData);
    }

    private String formatQuestDraftScheduleMode(Map<String, String> slotData) {
        return VisionCapabilityPreviewSupport.formatQuestDraftScheduleMode(slotData);
    }

    private String formatQuestDraftLocationMode(Map<String, String> slotData) {
        return VisionCapabilityPreviewSupport.formatQuestDraftLocationMode(slotData);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String formatDateTime(Instant value) {
        return VisionCapabilityPreviewSupport.formatDateTime(value);
    }

    private long countFilledValues(List<VisionSlotSummaryDTO> items) {
        return VisionCapabilityPreviewSupport.countFilledValues(items);
    }

    private String draftSummary(long filledFieldCount, String emptySummary, String partialSummary, String fullSummary, int fullThreshold) {
        return VisionCapabilityPreviewSupport.draftSummary(filledFieldCount, emptySummary, partialSummary, fullSummary, fullThreshold);
    }

    private void addItem(List<VisionSlotSummaryDTO> items, String slotId, String label, String value) {
        VisionCapabilityPreviewSupport.addItem(items, slotId, label, value);
    }

    private String applicationListValue(QuestApplicationResponseDTO application) {
        return VisionCapabilityPreviewSupport.applicationListValue(application);
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
