package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.service.LocationQuestPresentationService;
import com.themuffinman.app.workmarket.dto.QuestAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationDraftRulesViewDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailExecutionActionDTO;
import com.themuffinman.app.workmarket.dto.QuestPresentationDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestViewerRelationDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.QuestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestPresentationAssembler {

    private final WorkmarketPresentationHelper presentationHelper;
    private final LocationQuestPresentationService locationQuestPresentationService;

    public QuestPresentationDTO buildDefaultPresentation(Quest quest) {
        return baseBuilder(quest, null)
                .canEdit(false)
                .canApply(false)
                .canViewApplications(false)
                .canManuallyAssign(false)
                .offerSectionVisible(false)
                .applicationsSectionVisible(false)
                .myApplicationAsideVisible(false)
                .overviewStatusVisible(false)
                .autoOpenEditForm(false)
                .termChangeVisible(false)
                .termChangeActionable(false)
                .applicationSentVisible(false)
                .canOpenMyApplication(false)
                .deleteVisible(false)
                .reopenedBadgeVisible(false)
                .awaitingConfirmationBadgeVisible(false)
                .primaryExecutionAction(null)
                .executionHelperText(null)
                .build();
    }

    public QuestPresentationDTO buildPresentation(Quest quest, QuestResponseDTO questResponse, AppUser currentUser) {
        boolean canStart = questResponse.getAllowedActions().contains(QuestAllowedActionDTO.START);
        boolean canComplete = questResponse.getAllowedActions().contains(QuestAllowedActionDTO.COMPLETE);
        boolean canRespondToTermChange = questResponse.getAllowedActions().contains(QuestAllowedActionDTO.CONFIRM_TERM_CHANGE)
                || questResponse.getAllowedActions().contains(QuestAllowedActionDTO.REJECT_TERM_CHANGE);
        boolean canManageQuest = questResponse.getViewerRelation() == QuestViewerRelationDTO.OWNER;
        boolean canViewApplications = questResponse.getAllowedActions().contains(QuestAllowedActionDTO.VIEW_APPLICATIONS);
        boolean showOfferSection = questResponse.getViewerRelation() != QuestViewerRelationDTO.OWNER
                && (questResponse.getAllowedActions().contains(QuestAllowedActionDTO.APPLY)
                || (questResponse.getStatus() == QuestStatus.OPEN && questResponse.isHasApplied())
                || questResponse.getMyApplicationId() != null);
        String visibleToCirclesLabel = resolveVisibleToCirclesLabel(questResponse, canManageQuest);

        return baseBuilder(quest, currentUser)
                .slotProgressLabel(questResponse.getApprovedApplicationCount() + " / " + Math.max(questResponse.getAssigneeTarget() == null ? 1 : questResponse.getAssigneeTarget(), 1) + " filled")
                .remainingSlotsLabel(questResponse.getRemainingAssigneeSlots() > 0 ? questResponse.getRemainingAssigneeSlots() + " open spots" : "All spots filled")
                .approvedApplicantsVisible(questResponse.isShowApprovedApplicants() && questResponse.getApprovedApplicationCount() > 0)
                .canEdit(questResponse.getAllowedActions().contains(QuestAllowedActionDTO.EDIT))
                .canApply(questResponse.getAllowedActions().contains(QuestAllowedActionDTO.APPLY))
                .canViewApplications(canViewApplications)
                .canManuallyAssign(
                        questResponse.getViewerRelation() == QuestViewerRelationDTO.OWNER
                                && questResponse.getStatus() == QuestStatus.OPEN
                                && questResponse.getApprovedApplicationCount() > 0
                )
                .offerSectionVisible(showOfferSection)
                .applicationsSectionVisible(canManageQuest && canViewApplications)
                .myApplicationAsideVisible(canManageQuest && !showOfferSection)
                .overviewStatusVisible(canManageQuest)
                .primaryExecutionActionLabel(canStart ? "Start work" : (canComplete ? "Mark complete" : null))
                .termChangeSummaryLabel(questResponse.getStatus() == QuestStatus.WAITING_CONFIRMATION ? "Term change waiting" : null)
                .termChangeConfirmLabel(canRespondToTermChange ? "Confirm term change" : null)
                .termChangeRejectLabel(canRespondToTermChange ? "Reject term change" : null)
                .postingSettingsVisible(canManageQuest)
                .visibleToCirclesLabel(visibleToCirclesLabel)
                .autoOpenEditForm(
                        questResponse.getAllowedActions().contains(QuestAllowedActionDTO.EDIT)
                                && questResponse.getStatus() == QuestStatus.OPEN
                )
                .termChangeVisible(questResponse.getStatus() == QuestStatus.WAITING_CONFIRMATION)
                .termChangeActionable(canRespondToTermChange)
                .applicationSentVisible(questResponse.getStatus() == QuestStatus.OPEN && questResponse.isHasApplied())
                .canOpenMyApplication(questResponse.getMyApplicationId() != null)
                .deleteVisible(questResponse.getAllowedActions().contains(QuestAllowedActionDTO.DELETE))
                .reopenedBadgeVisible(questResponse.getReopenedAt() != null && questResponse.getStatus() == QuestStatus.OPEN)
                .awaitingConfirmationBadgeVisible(questResponse.getStatus() == QuestStatus.WAITING_CONFIRMATION)
                .primaryExecutionAction(canStart
                        ? QuestDetailExecutionActionDTO.START
                        : (canComplete ? QuestDetailExecutionActionDTO.COMPLETE : null))
                .executionHelperText(questResponse.getViewerRelation() == QuestViewerRelationDTO.APPROVED_APPLICANT
                        ? "You are the approved applicant for this quest."
                        : null)
                .build();
    }

    private QuestPresentationDTO.QuestPresentationDTOBuilder baseBuilder(Quest quest, AppUser currentUser) {
        return QuestPresentationDTO.builder()
                .statusLabel(presentationHelper.formatQuestStatus(quest.getStatus()))
                .statusBadgeClass(presentationHelper.badgeClassForQuestStatus(quest.getStatus()))
                .statusSurfaceClass(presentationHelper.surfaceClassForQuestStatus(quest.getStatus()))
                .timeTypeLabel(presentationHelper.formatTimeType(quest.isTermFixed()))
                .audienceLabel(presentationHelper.formatAudience(quest.getAudience()))
                .locationLabel(locationQuestPresentationService.resolveQuestLocationLabel(quest, currentUser))
                .locationSourceSummary(locationQuestPresentationService.resolveQuestLocationSourceSummary(quest))
                .locationVisibilitySummary(locationQuestPresentationService.resolveQuestLocationVisibilitySummary(quest, currentUser))
                .assigneeTargetVisible(presentationHelper.showAssigneeTarget(quest.getAssigneeTarget()))
                .assigneeTargetLabel(presentationHelper.formatAssigneeTarget(quest.getAssigneeTarget()))
                .suggestedApplicationPrice(suggestedApplicationPrice(quest.getAwardAmount()))
                .applicationDraftRules(buildApplicationDraftRules(quest.getAwardAmount()))
                .slotProgressLabel(null)
                .remainingSlotsLabel(null)
                .approvedApplicantsVisible(false)
                .primaryExecutionActionLabel(null)
                .termChangeSummaryLabel(null)
                .termChangeConfirmLabel(null)
                .termChangeRejectLabel(null)
                .visibleToCirclesLabel(null);
    }

    private QuestApplicationDraftRulesViewDTO buildApplicationDraftRules(BigDecimal awardAmount) {
        return QuestApplicationDraftRulesViewDTO.builder()
                .messageRequired(true)
                .proposedPriceRequired(!isFreeQuest(awardAmount))
                .minimumProposedPrice(isFreeQuest(awardAmount) ? null : BigDecimal.valueOf(0.01))
                .suggestedApplicationPrice(suggestedApplicationPrice(awardAmount))
                .build();
    }

    private String resolveVisibleToCirclesLabel(QuestResponseDTO questResponse, boolean canManageQuest) {
        if (!canManageQuest || questResponse.getAudience() != QuestAudience.CIRCLES) {
            return null;
        }

        List<String> circleNames = questResponse.getVisibleToCircles() == null
                ? List.of()
                : questResponse.getVisibleToCircles().stream()
                .map(circle -> circle.getName())
                .filter(name -> name != null && !name.isBlank())
                .toList();
        return circleNames.isEmpty() ? "Selected circles" : String.join(", ", circleNames);
    }

    private BigDecimal suggestedApplicationPrice(BigDecimal awardAmount) {
        if (awardAmount == null || awardAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return awardAmount;
    }

    private boolean isFreeQuest(BigDecimal awardAmount) {
        return awardAmount != null && awardAmount.compareTo(BigDecimal.ZERO) == 0;
    }
}
