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
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.UserReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuestViewAssembler {

    private final QuestMgr questMgr;
    private final QuestApplicationMgr questApplicationMgr;
    private final QuestAccessPolicyService questAccessPolicyService;
    private final UserReviewRepository userReviewRepository;
    private final UserReviewMgr userReviewMgr;
    private final WorkmarketPresentationHelper presentationHelper;

    public QuestResponseDTO toResponse(Quest quest, AppUser currentUser, Map<Long, QuestApplication> applicationsByQuestId) {
        QuestResponseDTO dto = questMgr.toDto(quest);
        QuestApplication viewerApplication = currentUser == null ? null : applicationsByQuestId.get(quest.getId());
        var viewerRelation = questAccessPolicyService.resolveViewerRelation(quest, currentUser, viewerApplication);
        var allowedActions = questAccessPolicyService.resolveAllowedActions(quest, currentUser, viewerApplication);
        boolean canViewApplications = allowedActions.contains(QuestAllowedAction.VIEW_APPLICATIONS);

        QuestResponseDTO response = questMgr.withViewerContext(
                dto,
                viewerRelation,
                allowedActions,
                viewerApplication != null,
                viewerApplication == null ? null : viewerApplication.getId(),
                canViewApplications
        );
        response.setPresentation(buildQuestPresentation(response));
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
                    .placeholder("Add a short comment.")
                    .build();
        }

        QuestDetailReviewTargetDTO target = resolveReviewTarget(quest, currentUser, myApplication, applicationsView);
        if (target == null || currentUser == null) {
            return QuestDetailReviewSectionDTO.builder()
                    .visible(true)
                    .canSubmit(false)
                    .placeholder("Add a short comment.")
                    .build();
        }

        return QuestDetailReviewSectionDTO.builder()
                .visible(true)
                .canSubmit(true)
                .placeholder("Write a short comment about this " + target.getRoleLabel() + ".")
                .target(target)
                .submittedReview(userReviewRepository.findByQuestIdAndReviewerIdAndReviewedUserId(
                                quest.getId(),
                                currentUser.getId(),
                                target.getUserId()
                        )
                        .map(userReviewMgr::toDto)
                        .orElse(null))
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
                .currentTermLabel(presentationHelper.formatQuestTerm(
                        quest.getScheduledAt(),
                        quest.getEndsAt(),
                        quest.isTermFixed()
                ))
                .pendingTermLabel(presentationHelper.formatQuestTerm(
                        quest.getPendingScheduledAt(),
                        quest.getPendingEndsAt(),
                        quest.getPendingTermFixed() == null ? quest.isTermFixed() : quest.getPendingTermFixed()
                ))
                .currentScheduledAt(quest.getScheduledAt())
                .currentEndsAt(quest.getEndsAt())
                .currentTermFixed(quest.isTermFixed())
                .pendingScheduledAt(quest.getPendingScheduledAt())
                .pendingEndsAt(quest.getPendingEndsAt())
                .pendingTermFixed(quest.getPendingTermFixed())
                .build();
    }

    public QuestDetailManagementSectionDTO buildQuestDetailManagementSection(List<QuestAllowedAction> allowedActions) {
        return QuestDetailManagementSectionDTO.builder()
                .deleteVisible(allowedActions.contains(QuestAllowedAction.DELETE))
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
            QuestApplicationResponseDTO featuredApplication = applicationsView == null ? null : applicationsView.getFeaturedApplication();
            if (featuredApplication != null && featuredApplication.getStatus() == QuestApplicationStatus.APPROVED) {
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

    private QuestPresentationDTO buildQuestPresentation(QuestResponseDTO questResponse) {
        boolean canStart = questResponse.getAllowedActions().contains(QuestAllowedAction.START);
        boolean canComplete = questResponse.getAllowedActions().contains(QuestAllowedAction.COMPLETE);
        boolean canRespondToTermChange = questResponse.getAllowedActions().contains(QuestAllowedAction.CONFIRM_TERM_CHANGE)
                || questResponse.getAllowedActions().contains(QuestAllowedAction.REJECT_TERM_CHANGE);

        return QuestPresentationDTO.builder()
                .statusLabel(presentationHelper.formatQuestStatus(questResponse.getStatus()))
                .statusBadgeClass(presentationHelper.badgeClassForQuestStatus(questResponse.getStatus()))
                .statusSurfaceClass(presentationHelper.surfaceClassForQuestStatus(questResponse.getStatus()))
                .termLabel(presentationHelper.formatQuestTerm(
                        questResponse.getScheduledAt(),
                        questResponse.getEndsAt(),
                        questResponse.isTermFixed()
                ))
                .timeTypeLabel(presentationHelper.formatTimeType(questResponse.isTermFixed()))
                .audienceLabel(presentationHelper.formatAudience(questResponse.getAudience()))
                .assigneeTargetVisible(presentationHelper.showAssigneeTarget(questResponse.getAssigneeTarget()))
                .assigneeTargetLabel(presentationHelper.formatAssigneeTarget(questResponse.getAssigneeTarget()))
                .pendingTermLabel(presentationHelper.formatQuestTerm(
                        questResponse.getPendingScheduledAt(),
                        questResponse.getPendingEndsAt(),
                        questResponse.getPendingTermFixed() == null ? questResponse.isTermFixed() : questResponse.getPendingTermFixed()
                ))
                .canEdit(questResponse.getAllowedActions().contains(QuestAllowedAction.EDIT))
                .canApply(questResponse.getAllowedActions().contains(QuestAllowedAction.APPLY))
                .canViewApplications(questResponse.getAllowedActions().contains(QuestAllowedAction.VIEW_APPLICATIONS))
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
