package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.dto.QuestAllowedActionDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.vision.dto.QuestDetailExecutionSectionDTO;
import com.themuffinman.app.vision.dto.QuestDetailManagementSectionDTO;
import com.themuffinman.app.vision.dto.QuestDetailReviewSectionDTO;
import com.themuffinman.app.vision.dto.QuestDetailReviewTargetDTO;
import com.themuffinman.app.vision.dto.QuestDetailTermChangeSectionDTO;
import com.themuffinman.app.vision.dto.QuestDetailExecutionActionDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.vision.dto.QuestViewerRelationDTO;
import com.themuffinman.app.vision.mapper.QuestMgr;
import com.themuffinman.app.vision.mapper.UserReviewMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestApplication;
import com.themuffinman.app.vision.model.QuestApplicationStatus;
import com.themuffinman.app.vision.model.QuestAudience;
import com.themuffinman.app.vision.model.QuestStatus;
import com.themuffinman.app.vision.repository.QuestApplicationRepository;
import com.themuffinman.app.vision.repository.UserReviewRepository;
import com.themuffinman.app.vision.service.VisionPresentationHelper;
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
    private final QuestAccessPolicyService questAccessPolicyService;
    private final UserReviewRepository userReviewRepository;
    private final UserReviewMgr userReviewMgr;
    private final VisionPresentationHelper presentationHelper;
    private final QuestApplicationRepository questApplicationRepository;
    private final QuestPresentationAssembler questPresentationAssembler;

    public QuestResponseDTO toResponse(Quest quest, AppUser currentUser, Map<Long, QuestApplication> applicationsByQuestId) {
        QuestResponseDTO dto = questMgr.toDto(quest);
        QuestApplication viewerApplication = currentUser == null ? null : applicationsByQuestId.get(quest.getId());
        var viewerRelation = questAccessPolicyService.resolveViewerRelation(quest, currentUser, viewerApplication);
        var allowedActions = questAccessPolicyService.resolveAllowedActions(quest, currentUser, viewerApplication);
        boolean canViewApplications = allowedActions.contains(QuestAllowedActionDTO.VIEW_APPLICATIONS);
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
        response.setPresentation(questPresentationAssembler.buildPresentation(quest, response, currentUser));
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
        boolean canStart = questResponse.getAllowedActions().contains(QuestAllowedActionDTO.START);
        boolean canComplete = questResponse.getAllowedActions().contains(QuestAllowedActionDTO.COMPLETE);
        String helperText = questResponse.getViewerRelation() == QuestViewerRelationDTO.APPROVED_APPLICANT
                ? "You are the approved applicant for this quest."
                : null;

        return QuestDetailExecutionSectionDTO.builder()
                .visible(canStart || canComplete || helperText != null)
                .primaryAction(canStart
                        ? QuestDetailExecutionActionDTO.START
                        : (canComplete ? QuestDetailExecutionActionDTO.COMPLETE : null))
                .primaryActionLabel(canStart ? "Start work" : (canComplete ? "Mark complete" : null))
                .helperText(helperText)
                .build();
    }

    public QuestDetailTermChangeSectionDTO buildQuestDetailTermChangeSection(Quest quest, List<QuestAllowedActionDTO> allowedActions) {
        return QuestDetailTermChangeSectionDTO.builder()
                .visible(quest.getStatus() == QuestStatus.WAITING_CONFIRMATION)
                .actionable(
                        allowedActions.contains(QuestAllowedActionDTO.CONFIRM_TERM_CHANGE)
                                || allowedActions.contains(QuestAllowedActionDTO.REJECT_TERM_CHANGE)
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
        boolean canManageQuest = questResponse.getViewerRelation() == QuestViewerRelationDTO.OWNER;
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
                .editVisible(questResponse.getAllowedActions().contains(QuestAllowedActionDTO.EDIT))
                .deleteVisible(questResponse.getAllowedActions().contains(QuestAllowedActionDTO.DELETE))
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

}
