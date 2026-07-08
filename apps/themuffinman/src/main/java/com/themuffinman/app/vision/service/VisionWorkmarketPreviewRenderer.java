package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationDetailResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationReadService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
class VisionWorkmarketPreviewRenderer {

    private final WorkmarketQuestReadService questReadService;
    private final WorkmarketQuestApplicationReadService questApplicationReadService;

    VisionCapabilityPreviewDTO previewApplications(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        List<QuestApplicationResponseDTO> applications = questApplicationReadService.getApplicationsForApplicant(currentUser);
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "applications_count", "Applications", String.valueOf(applications.size()));
        long pendingCount = applications.stream().filter(application -> application.getStatus() != null && "PENDING".equals(application.getStatus().name())).count();
        long approvedCount = applications.stream().filter(application -> application.getStatus() != null && "APPROVED".equals(application.getStatus().name())).count();
        addItem(items, "applications_pending", "Pending", String.valueOf(pendingCount));
        addItem(items, "applications_approved", "Approved", String.valueOf(approvedCount));
        for (int index = 0; index < Math.min(applications.size(), 4); index++) {
            QuestApplicationResponseDTO application = applications.get(index);
            addItem(items, "application_" + application.getId(), application.getQuestTitle(),
                    VisionCapabilityPreviewSupport.applicationListValue(application));
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

    VisionCapabilityPreviewDTO previewQuestDetail(AppUser currentUser, Long questId) {
        if (currentUser == null || questId == null) {
            return null;
        }

        QuestDetailResponseDTO detail = questReadService.getQuestDetailResponseById(questId, currentUser);
        QuestResponseDTO quest = detail == null ? null : detail.getSummary();
        if (quest == null) {
            return null;
        }

        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_quest_query", "Title", quest.getTitle());
        addItem(items, "quest_description", "Description", quest.getDescription());
        addItem(items, "reward_amount", "Reward", VisionCapabilityPreviewSupport.formatRewardLabel(quest));
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

    VisionCapabilityPreviewDTO previewQuestDraft(Map<String, String> slotData) {
        if (slotData == null) {
            return null;
        }

        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "quest_title", "Title", slotData.get("quest_title"));
        addItem(items, "quest_description", "Description", slotData.get("quest_description"));
        addItem(items, "reward_amount", "Reward", VisionCapabilityPreviewSupport.formatQuestDraftRewardLabel(slotData));
        addItem(items, "visibility", "Visibility", slotData.get("visibility"));
        addItem(items, "schedule_mode", "Schedule", VisionCapabilityPreviewSupport.formatQuestDraftScheduleMode(slotData));
        addItem(items, "scheduled_date", "Date", slotData.get("scheduled_date"));
        addItem(items, "scheduled_time", "Time", slotData.get("scheduled_time"));
        addItem(items, "location_mode", "Location", VisionCapabilityPreviewSupport.formatQuestDraftLocationMode(slotData));
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

    VisionCapabilityPreviewDTO previewApplicationDetail(AppUser currentUser, Long applicationId) {
        if (currentUser == null || applicationId == null) {
            return null;
        }

        QuestApplicationDetailResponseDTO detail = questApplicationReadService.getApplicationDetailResponseById(applicationId, currentUser);
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

    VisionCapabilityPreviewDTO previewApplicationDraft(
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

        long filledFieldCount = VisionCapabilityPreviewSupport.countFilledValues(items);
        String summary = VisionCapabilityPreviewSupport.draftSummary(
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

    VisionCapabilityPreviewDTO previewUpdateApplicationDraft(
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

    VisionCapabilityPreviewDTO previewWithdrawApplicationDraft(
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
        long filledFieldCount = VisionCapabilityPreviewSupport.countFilledValues(items);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("withdraw_application")
                .title("Application withdrawal review")
                .summary(VisionCapabilityPreviewSupport.draftSummary(
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

    VisionCapabilityPreviewDTO previewManagedApplicationDecisionDraft(
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
        long filledFieldCount = VisionCapabilityPreviewSupport.countFilledValues(items);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId(capabilityId)
                .title(title)
                .summary(VisionCapabilityPreviewSupport.draftSummary(
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void addItem(List<VisionSlotSummaryDTO> items, String slotId, String label, String value) {
        VisionCapabilityPreviewSupport.addItem(items, slotId, label, value);
    }

    private String formatDateTime(Instant value) {
        return VisionCapabilityPreviewSupport.formatDateTime(value);
    }
}
