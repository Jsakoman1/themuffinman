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
import com.themuffinman.app.activity.dto.ActivityItemDTO;
import com.themuffinman.app.activity.service.ActivityReadService;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.service.AppUserReadService;
import com.themuffinman.app.identity.service.AppUserService;
import com.themuffinman.app.identity.service.UserProfileViewService;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.service.CircleReadService;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.vision.dto.DashboardNotificationItemDTO;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.workmarket.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class VisionCapabilityPreviewService {

    private final AppUserService appUserService;
    private final AppUserReadService appUserReadService;
    private final AppUserMgr appUserMgr;
    private final AppUserRepository appUserRepository;
    private final UserProfileViewService userProfileViewService;
    private final ChatService chatService;
    private final CircleReadService circleReadService;
    private final VisionSocialPreviewRenderer visionSocialPreviewRenderer;
    private final VisionSocialMutationAdapter visionSocialMutationAdapter;
    private final VisionProfilePreviewRenderer visionProfilePreviewRenderer;
    private final VisionProfileMutationAdapter visionProfileMutationAdapter;
    private final VisionIdentityPreviewRenderer visionIdentityPreviewRenderer;
    private final VisionFeedPreviewRenderer visionFeedPreviewRenderer;
    private final VisionBusinessPreviewRenderer visionBusinessPreviewRenderer;
    private final VisionCapabilityEntityResolutionSupport visionCapabilityEntityResolutionSupport;
    private final VisionWorkmarketPreviewRenderer visionWorkmarketPreviewRenderer;
    private final VisionWorkmarketApplicationMutationAdapter visionWorkmarketApplicationMutationAdapter;
    private final ActivityReadService activityReadService;

    public VisionCapabilityPreviewDTO previewProfile(AppUser currentUser) {
        return visionIdentityPreviewRenderer.previewProfile(currentUser);
    }

    public VisionCapabilityPreviewDTO previewBusiness(AppUser currentUser) {
        return visionBusinessPreviewRenderer.previewBusiness(currentUser);
    }

    public VisionCapabilityPreviewDTO previewBusinessAvailability(AppUser currentUser) {
        return visionBusinessPreviewRenderer.previewBusinessAvailability(currentUser);
    }

    public VisionCapabilityPreviewDTO previewBusinessBookings(AppUser currentUser) {
        return visionBusinessPreviewRenderer.previewBusinessBookings(currentUser);
    }

    public VisionCapabilityPreviewDTO previewSettings(AppUser currentUser) {
        return visionIdentityPreviewRenderer.previewSettings(currentUser);
    }

    public VisionCapabilityPreviewDTO previewChatWorkspace(AppUser currentUser) {
        return visionIdentityPreviewRenderer.previewChatWorkspace(currentUser);
    }

    public VisionCapabilityPreviewDTO previewChatSync(AppUser currentUser, Long conversationId) {
        return visionIdentityPreviewRenderer.previewChatSync(currentUser, conversationId);
    }

    public VisionCapabilityPreviewDTO previewChatAttachment(AppUser currentUser, Long conversationId, Long messageId) {
        return visionIdentityPreviewRenderer.previewChatAttachment(currentUser, conversationId, messageId);
    }

    public VisionCapabilityPreviewDTO previewUserProfile(AppUser currentUser, Long profileUserId) {
        return visionIdentityPreviewRenderer.previewUserProfile(currentUser, profileUserId);
    }

    public VisionCapabilityPreviewDTO previewCircles(AppUser currentUser) {
        return visionSocialPreviewRenderer.previewCircles(currentUser);
    }

    public VisionCapabilityPreviewDTO previewCircleDetail(AppUser currentUser, Long circleId) {
        return visionSocialPreviewRenderer.previewCircleDetail(currentUser, circleId);
    }

    public VisionCapabilityPreviewDTO previewAccessibleCircle(AppUser currentUser, Long circleId) {
        return visionSocialPreviewRenderer.previewAccessibleCircle(currentUser, circleId);
    }

    public VisionCapabilityPreviewDTO previewCircleDraft(String circleName) {
        return visionSocialPreviewRenderer.previewCircleDraft(circleName);
    }

    public VisionCapabilityPreviewDTO previewCreateCircleRequestDraft(String targetUsername) {
        return visionSocialPreviewRenderer.previewCreateCircleRequestDraft(targetUsername);
    }

    public VisionCapabilityPreviewDTO previewAcceptCircleRequestDraft(String targetUsername) {
        return visionSocialPreviewRenderer.previewAcceptCircleRequestDraft(targetUsername);
    }

    public VisionCapabilityPreviewDTO previewDeleteCircleRequestDraft(String targetUsername, boolean incoming) {
        return visionSocialPreviewRenderer.previewDeleteCircleRequestDraft(targetUsername, incoming);
    }

    public VisionCapabilityPreviewDTO previewUpdateCircleDraft(
            String currentCircleName,
            String draftCircleName
    ) {
        return visionSocialPreviewRenderer.previewUpdateCircleDraft(currentCircleName, draftCircleName);
    }

    public VisionCapabilityPreviewDTO previewDeleteCircleDraft(String currentCircleName, String memberCountLabel) {
        return visionSocialPreviewRenderer.previewDeleteCircleDraft(currentCircleName, memberCountLabel);
    }

    public CircleGroupResponseDTO createCircle(String circleName, AppUser currentUser) {
        return visionSocialMutationAdapter.createCircle(circleName, currentUser);
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
        return visionSocialMutationAdapter.updateCircle(currentUser, circleId, circleName);
    }

    public void deleteCircle(AppUser currentUser, Long circleId) {
        visionSocialMutationAdapter.deleteCircle(currentUser, circleId);
    }

    public CircleRequestResponseDTO createCircleRequest(AppUser currentUser, Long targetUserId) {
        return visionSocialMutationAdapter.createCircleRequest(currentUser, targetUserId);
    }

    public CircleRequestResponseDTO acceptCircleRequest(AppUser currentUser, Long requestId) {
        return visionSocialMutationAdapter.acceptCircleRequest(currentUser, requestId);
    }

    public void deleteCircleRequest(AppUser currentUser, Long requestId) {
        visionSocialMutationAdapter.deleteCircleRequest(currentUser, requestId);
    }

    public VisionCapabilityPreviewDTO previewApplications(AppUser currentUser) {
        return visionWorkmarketPreviewRenderer.previewApplications(currentUser);
    }

    public VisionCapabilityPreviewDTO previewQuestNews(AppUser currentUser) {
        return visionFeedPreviewRenderer.previewQuestNews(currentUser);
    }

    public VisionCapabilityPreviewDTO previewNotifications(AppUser currentUser) {
        return visionFeedPreviewRenderer.previewNotifications(currentUser);
    }

    public VisionCapabilityPreviewDTO previewActivity(AppUser currentUser) {
        if (currentUser == null) return null;
        List<ActivityItemDTO> activity = activityReadService.getMine(currentUser);
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        VisionCapabilityPreviewSupport.addItem(items, "activity_count", "Activity", String.valueOf(activity.size()));
        for (int index = 0; index < Math.min(activity.size(), 5); index++) {
            ActivityItemDTO item = activity.get(index);
            VisionCapabilityPreviewSupport.addItem(items, "activity_" + index, item.getTitle(), item.getSummary());
        }
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_activity")
                .title("Activity")
                .summary(activity.isEmpty() ? "No recent activity." : activity.size() + " recent activity item" + (activity.size() == 1 ? "." : "s."))
                .items(items)
                .tone("info")
                .build();
    }

    public VisionCapabilityPreviewDTO previewThings(AppUser currentUser) {
        return visionFeedPreviewRenderer.previewThings(currentUser);
    }

    public VisionCapabilityPreviewDTO previewThingDetail(AppUser currentUser, Long listingId) {
        return visionFeedPreviewRenderer.previewThingDetail(currentUser, listingId);
    }

    public VisionCapabilityPreviewDTO previewBorrowRequests(AppUser currentUser) {
        return visionFeedPreviewRenderer.previewBorrowRequests(currentUser);
    }

    public VisionCapabilityPreviewDTO previewQuestDetail(AppUser currentUser, Long questId) {
        return visionWorkmarketPreviewRenderer.previewQuestDetail(currentUser, questId);
    }

    public VisionCapabilityPreviewDTO previewQuestDraft(Map<String, String> slotData) {
        return visionWorkmarketPreviewRenderer.previewQuestDraft(slotData);
    }

    public VisionCapabilityPreviewDTO previewApplicationDetail(AppUser currentUser, Long applicationId) {
        return visionWorkmarketPreviewRenderer.previewApplicationDetail(currentUser, applicationId);
    }

    public VisionCapabilityPreviewDTO previewProfileDraft(AppUser currentUser, String username, String profileDescription) {
        return visionProfilePreviewRenderer.previewProfileDraft(currentUser, username, profileDescription);
    }

    public VisionCapabilityPreviewDTO previewProfileLocationDraft(AppUser currentUser, String locationMode, String locationLabel) {
        return visionProfilePreviewRenderer.previewProfileLocationDraft(currentUser, locationMode, locationLabel);
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
        return visionWorkmarketPreviewRenderer.previewApplicationDraft(
                questTitle,
                questCreatorUsername,
                rewardLabel,
                priceRequired,
                applicationMessage,
                proposedPrice
        );
    }

    public VisionResolvedApplicationTarget resolveMyPendingApplication(
            AppUser currentUser,
            String query,
            ApplicationAllowedActionDTO requiredAction
    ) {
        return visionCapabilityEntityResolutionSupport.resolveMyPendingApplication(
                currentUser,
                query,
                com.themuffinman.app.workmarket.dto.ApplicationAllowedActionDTO.valueOf(requiredAction.name())
        );
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
        return visionCapabilityEntityResolutionSupport.resolveManagedPendingApplication(
                currentUser,
                questQuery,
                applicantQuery,
                com.themuffinman.app.workmarket.dto.ApplicationAllowedActionDTO.valueOf(requiredAction.name())
        );
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
        return visionWorkmarketPreviewRenderer.previewUpdateApplicationDraft(
                questTitle,
                questCreatorUsername,
                rewardLabel,
                priceRequired,
                currentMessage,
                currentPrice,
                draftMessage,
                draftPrice
        );
    }

    public VisionCapabilityPreviewDTO previewWithdrawApplicationDraft(
            String questTitle,
            String questCreatorUsername,
            String rewardLabel,
            String currentMessage,
            String currentPrice
    ) {
        return visionWorkmarketPreviewRenderer.previewWithdrawApplicationDraft(
                questTitle,
                questCreatorUsername,
                rewardLabel,
                currentMessage,
                currentPrice
        );
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
        return visionWorkmarketPreviewRenderer.previewManagedApplicationDecisionDraft(
                capabilityId,
                title,
                summary,
                questTitle,
                applicantUsername,
                currentMessage,
                currentPrice
        );
    }

    public QuestApplicationResponseDTO createApplication(
            AppUser currentUser,
            Long questId,
            String message,
            String proposedPrice
    ) {
        return visionWorkmarketApplicationMutationAdapter.createApplication(currentUser, questId, message, proposedPrice);
    }

    public QuestApplicationResponseDTO updateApplication(
            AppUser currentUser,
            Long questId,
            String message,
            String proposedPrice
    ) {
        return visionWorkmarketApplicationMutationAdapter.updateApplication(currentUser, questId, message, proposedPrice);
    }

    public QuestApplicationResponseDTO withdrawApplication(AppUser currentUser, Long questId) {
        return visionWorkmarketApplicationMutationAdapter.withdrawApplication(currentUser, questId);
    }

    public QuestApplicationResponseDTO approveManagedApplication(AppUser currentUser, Long questId, Long applicationId) {
        return visionWorkmarketApplicationMutationAdapter.approveManagedApplication(currentUser, questId, applicationId);
    }

    public QuestApplicationResponseDTO declineManagedApplication(AppUser currentUser, Long questId, Long applicationId) {
        return visionWorkmarketApplicationMutationAdapter.declineManagedApplication(currentUser, questId, applicationId);
    }

    public AppUserResponseDTO updateProfile(AppUser currentUser, String username, String profileDescription) {
        return visionProfileMutationAdapter.updateProfile(currentUser, username, profileDescription);
    }

    public AppUserResponseDTO updateProfileLocation(AppUser currentUser, String locationMode, String locationLabel) {
        return visionProfileMutationAdapter.updateProfileLocation(currentUser, locationMode, locationLabel);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
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
