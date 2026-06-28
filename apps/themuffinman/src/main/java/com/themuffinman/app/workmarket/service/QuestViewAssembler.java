package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.QuestAllowedAction;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailExecutionAction;
import com.themuffinman.app.workmarket.dto.QuestDetailExecutionSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailManagementSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailReviewSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailReviewTargetDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailTermChangeSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestPresentationDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestViewerRelation;
import com.themuffinman.app.workmarket.mapper.QuestApplicationMgr;
import com.themuffinman.app.workmarket.mapper.QuestMgr;
import com.themuffinman.app.workmarket.mapper.UserReviewMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.service.LocationSettingsService;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.QuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.UserReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestViewAssembler {

    private final QuestMgr questMgr;
    private final QuestApplicationMgr questApplicationMgr;
    private final QuestAccessPolicyService questAccessPolicyService;
    private final UserReviewRepository userReviewRepository;
    private final UserReviewMgr userReviewMgr;
    private final WorkmarketPresentationHelper presentationHelper;
    private final LocationSettingsService locationSettingsService;
    private final QuestApplicationRepository questApplicationRepository;

    public QuestResponseDTO toResponse(Quest quest, AppUser currentUser, Map<Long, QuestApplication> applicationsByQuestId) {
        QuestResponseDTO dto = questMgr.toDto(quest);
        QuestApplication viewerApplication = currentUser == null ? null : applicationsByQuestId.get(quest.getId());
        var viewerRelation = questAccessPolicyService.resolveViewerRelation(quest, currentUser, viewerApplication);
        var allowedActions = questAccessPolicyService.resolveAllowedActions(quest, currentUser, viewerApplication);
        boolean canViewApplications = allowedActions.contains(QuestAllowedAction.VIEW_APPLICATIONS);
        int workerTarget = Math.max(dto.getAssigneeTarget() == null ? 1 : dto.getAssigneeTarget(), 1);
        int approvedApplicationCount = Math.toIntExact(questApplicationRepository.countByQuestIdAndStatus(quest.getId(), QuestApplicationStatus.APPROVED));
        int remainingAssigneeSlots = Math.max(workerTarget - approvedApplicationCount, 0);

        QuestResponseDTO response = questMgr.withViewerContext(
                dto,
                viewerRelation,
                allowedActions,
                viewerApplication != null,
                viewerApplication == null ? null : viewerApplication.getId(),
                canViewApplications
        );
        response.setApprovedApplicationCount(approvedApplicationCount);
        response.setRemainingAssigneeSlots(remainingAssigneeSlots);
        response.setPresentation(buildQuestPresentation(quest, response, currentUser));
        return response;
    }

    public QuestDetailReviewSectionDTO buildQuestDetailReviewSection(
            Quest quest,
            AppUser currentUser,
            QuestApplicationResponseDTO myApplication,
            QuestApplicationsViewDTO applicationsView
    ) {
        if (quest.getStatus() != QuestStatus.COMPLETED) {
            return QuestDetailReviewSectionDTO.builder()
                    .visible(false)
                    .canSubmit(false)
                    .introTitle("Review")
                    .introSubtitle(null)
                    .placeholder("Add a short comment.")
                    .submitLabel("Submit")
                    .emptyStateMessage("Reviews become available here after the quest is completed.")
                    .build();
        }

        QuestDetailReviewTargetDTO target = resolveReviewTarget(quest, currentUser, myApplication, applicationsView);
        if (target == null || currentUser == null) {
            return QuestDetailReviewSectionDTO.builder()
                    .visible(true)
                    .canSubmit(false)
                    .introTitle("Review")
                    .introSubtitle(null)
                    .placeholder("Add a short comment.")
                    .submitLabel("Submit")
                    .emptyStateMessage("Reviews become available here after the quest is completed.")
                    .build();
        }

        var submittedReview = userReviewRepository.findByQuestIdAndReviewerIdAndReviewedUserId(
                        quest.getId(),
                        currentUser.getId(),
                        target.getUserId()
                )
                .map(userReviewMgr::toDto)
                .orElse(null);

        return QuestDetailReviewSectionDTO.builder()
                .visible(true)
                .canSubmit(true)
                .introTitle("Rate " + target.getUsername())
                .introSubtitle(target.getRoleLabel())
                .placeholder("Write a short comment about this " + target.getRoleLabel() + ".")
                .submitLabel(submittedReview == null ? "Submit" : "Update")
                .emptyStateMessage("Reviews become available here after the quest is completed.")
                .target(target)
                .submittedReview(submittedReview)
                .build();
    }

    public QuestDetailExecutionSectionDTO buildQuestDetailExecutionSection(QuestResponseDTO questResponse) {
        boolean canStart = questResponse.getAllowedActions().contains(QuestAllowedAction.START);
        boolean canComplete = questResponse.getAllowedActions().contains(QuestAllowedAction.COMPLETE);
        String helperText = questResponse.getViewerRelation() == QuestViewerRelation.APPROVED_APPLICANT
                ? "You are the approved applicant for this quest."
                : null;

        return QuestDetailExecutionSectionDTO.builder()
                .visible(canStart || canComplete || helperText != null)
                .primaryAction(canStart
                        ? QuestDetailExecutionAction.START
                        : (canComplete ? QuestDetailExecutionAction.COMPLETE : null))
                .primaryActionLabel(canStart ? "Start work" : (canComplete ? "Mark complete" : null))
                .helperText(helperText)
                .build();
    }

    public QuestDetailTermChangeSectionDTO buildQuestDetailTermChangeSection(Quest quest, List<QuestAllowedAction> allowedActions) {
        return QuestDetailTermChangeSectionDTO.builder()
                .visible(quest.getStatus() == QuestStatus.WAITING_CONFIRMATION)
                .actionable(
                        allowedActions.contains(QuestAllowedAction.CONFIRM_TERM_CHANGE)
                                || allowedActions.contains(QuestAllowedAction.REJECT_TERM_CHANGE)
                )
                .summaryLabel("Term change waiting")
                .confirmLabel("Confirm term change")
                .rejectLabel("Reject term change")
                .currentScheduledAt(quest.getScheduledAt())
                .currentEndsAt(quest.getEndsAt())
                .currentTermFixed(quest.isTermFixed())
                .pendingScheduledAt(quest.getPendingScheduledAt())
                .pendingEndsAt(quest.getPendingEndsAt())
                .pendingTermFixed(quest.getPendingTermFixed())
                .build();
    }

    public QuestDetailManagementSectionDTO buildQuestDetailManagementSection(QuestResponseDTO questResponse) {
        boolean canManageQuest = questResponse.getViewerRelation() == QuestViewerRelation.OWNER;
        String visibleToCirclesLabel = null;
        if (canManageQuest && questResponse.getAudience() == QuestAudience.CIRCLES) {
            List<String> circleNames = questResponse.getVisibleToCircles() == null
                    ? List.of()
                    : questResponse.getVisibleToCircles().stream()
                    .map(circle -> circle.getName())
                    .filter(name -> name != null && !name.isBlank())
                    .toList();
            visibleToCirclesLabel = circleNames.isEmpty() ? "Selected circles" : String.join(", ", circleNames);
        }

        return QuestDetailManagementSectionDTO.builder()
                .editVisible(questResponse.getAllowedActions().contains(QuestAllowedAction.EDIT))
                .deleteVisible(questResponse.getAllowedActions().contains(QuestAllowedAction.DELETE))
                .postingSettingsVisible(canManageQuest)
                .audienceLabel(canManageQuest ? presentationHelper.formatAudience(questResponse.getAudience()) : null)
                .visibleToCirclesLabel(visibleToCirclesLabel)
                .build();
    }

    private QuestDetailReviewTargetDTO resolveReviewTarget(
            Quest quest,
            AppUser currentUser,
            QuestApplicationResponseDTO myApplication,
            QuestApplicationsViewDTO applicationsView
    ) {
        if (currentUser == null) {
            return null;
        }

        if (questAccessPolicyService.isQuestOwner(quest, currentUser)) {
            List<QuestApplicationResponseDTO> approvedApplications = applicationsView == null
                    ? List.of()
                    : applicationsView.getApprovedApplications();
            if (approvedApplications.size() == 1) {
                QuestApplicationResponseDTO featuredApplication = approvedApplications.getFirst();
                return QuestDetailReviewTargetDTO.builder()
                        .userId(featuredApplication.getApplicantId())
                        .username(featuredApplication.getApplicantUsername())
                        .roleLabel("worker")
                        .build();
            }

            return null;
        }

        if (myApplication != null && myApplication.getStatus() == QuestApplicationStatus.APPROVED) {
            return QuestDetailReviewTargetDTO.builder()
                    .userId(quest.getCreator().getId())
                    .username(quest.getCreator().getUsername())
                    .roleLabel("employer")
                    .build();
        }

        return null;
    }

    private QuestPresentationDTO buildQuestPresentation(Quest quest, QuestResponseDTO questResponse, AppUser currentUser) {
        boolean canStart = questResponse.getAllowedActions().contains(QuestAllowedAction.START);
        boolean canComplete = questResponse.getAllowedActions().contains(QuestAllowedAction.COMPLETE);
        boolean canRespondToTermChange = questResponse.getAllowedActions().contains(QuestAllowedAction.CONFIRM_TERM_CHANGE)
                || questResponse.getAllowedActions().contains(QuestAllowedAction.REJECT_TERM_CHANGE);
        boolean canManageQuest = questResponse.getViewerRelation() == QuestViewerRelation.OWNER;
        String visibleToCirclesLabel = null;
        if (canManageQuest && questResponse.getAudience() == QuestAudience.CIRCLES) {
            List<String> circleNames = questResponse.getVisibleToCircles() == null
                    ? List.of()
                    : questResponse.getVisibleToCircles().stream()
                    .map(circle -> circle.getName())
                    .filter(name -> name != null && !name.isBlank())
                    .toList();
            visibleToCirclesLabel = circleNames.isEmpty() ? "Selected circles" : String.join(", ", circleNames);
        }

        return QuestPresentationDTO.builder()
                .statusLabel(presentationHelper.formatQuestStatus(questResponse.getStatus()))
                .statusBadgeClass(presentationHelper.badgeClassForQuestStatus(questResponse.getStatus()))
                .statusSurfaceClass(presentationHelper.surfaceClassForQuestStatus(questResponse.getStatus()))
                .timeTypeLabel(presentationHelper.formatTimeType(questResponse.isTermFixed()))
                .audienceLabel(presentationHelper.formatAudience(questResponse.getAudience()))
                .locationLabel(locationSettingsService.resolveQuestLocationLabel(quest, currentUser))
                .locationSourceSummary(locationSettingsService.resolveQuestLocationSourceSummary(quest))
                .locationVisibilitySummary(locationSettingsService.resolveQuestLocationVisibilitySummary(quest, currentUser))
                .assigneeTargetVisible(presentationHelper.showAssigneeTarget(questResponse.getAssigneeTarget()))
                .assigneeTargetLabel(presentationHelper.formatAssigneeTarget(questResponse.getAssigneeTarget()))
                .slotProgressLabel(questResponse.getApprovedApplicationCount() + " / " + Math.max(questResponse.getAssigneeTarget() == null ? 1 : questResponse.getAssigneeTarget(), 1) + " filled")
                .remainingSlotsLabel(questResponse.getRemainingAssigneeSlots() > 0 ? questResponse.getRemainingAssigneeSlots() + " open spots" : "All spots filled")
                .approvedApplicantsVisible(questResponse.isShowApprovedApplicants() && questResponse.getApprovedApplicationCount() > 0)
                .canEdit(questResponse.getAllowedActions().contains(QuestAllowedAction.EDIT))
                .canApply(questResponse.getAllowedActions().contains(QuestAllowedAction.APPLY))
                .canViewApplications(questResponse.getAllowedActions().contains(QuestAllowedAction.VIEW_APPLICATIONS))
                .canManuallyAssign(
                        questResponse.getViewerRelation() == QuestViewerRelation.OWNER
                                && questResponse.getStatus() == QuestStatus.OPEN
                                && questResponse.getApprovedApplicationCount() > 0
                )
                .primaryExecutionActionLabel(canStart ? "Start work" : (canComplete ? "Mark complete" : null))
                .termChangeSummaryLabel(questResponse.getStatus() == QuestStatus.WAITING_CONFIRMATION ? "Term change waiting" : null)
                .termChangeConfirmLabel(canRespondToTermChange ? "Confirm term change" : null)
                .termChangeRejectLabel(canRespondToTermChange ? "Reject term change" : null)
                .postingSettingsVisible(canManageQuest)
                .visibleToCirclesLabel(visibleToCirclesLabel)
                .autoOpenEditForm(
                        questResponse.getAllowedActions().contains(QuestAllowedAction.EDIT)
                                && questResponse.getStatus() == QuestStatus.OPEN
                )
                .termChangeVisible(questResponse.getStatus() == QuestStatus.WAITING_CONFIRMATION)
                .termChangeActionable(canRespondToTermChange)
                .applicationSentVisible(questResponse.getStatus() == QuestStatus.OPEN && questResponse.isHasApplied())
                .canOpenMyApplication(questResponse.getMyApplicationId() != null)
                .deleteVisible(questResponse.getAllowedActions().contains(QuestAllowedAction.DELETE))
                .reopenedBadgeVisible(questResponse.getReopenedAt() != null && questResponse.getStatus() == QuestStatus.OPEN)
                .awaitingConfirmationBadgeVisible(questResponse.getStatus() == QuestStatus.WAITING_CONFIRMATION)
                .primaryExecutionAction(canStart
                        ? QuestDetailExecutionAction.START
                        : (canComplete ? QuestDetailExecutionAction.COMPLETE : null))
                .executionHelperText(questResponse.getViewerRelation() == QuestViewerRelation.APPROVED_APPLICANT
                        ? "You are the approved applicant for this quest."
                        : null)
                .build();
    }

}
