package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.common.dto.NavigationTargetType;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailExecutionActionDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailExecutionSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailManagementSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailNavigationSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailReviewSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailReviewTargetDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailSectionsDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailTermChangeSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestViewerRelationDTO;
import com.themuffinman.app.workmarket.mapper.WorkmarketUserReviewMgr;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketUserReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkmarketQuestDetailSectionsFactory {

    private final WorkmarketQuestAccessPolicyService questAccessPolicyService;
    private final WorkmarketUserReviewRepository userReviewRepository;
    private final WorkmarketUserReviewMgr userReviewMgr;
    private final WorkmarketPresentationHelper presentationHelper;

    public QuestDetailSectionsDTO buildSections(
            Quest quest,
            AppUser currentUser,
            QuestResponseDTO questResponse,
            QuestApplicationResponseDTO myApplication,
            QuestApplicationsViewDTO applicationsView
    ) {
        return QuestDetailSectionsDTO.builder()
                .navigation(QuestDetailNavigationSectionDTO.builder()
                        .listNavigation(NavigationTargetDTO.builder()
                                .type(NavigationTargetType.QUEST_LIST)
                                .entityId(null)
                                .build())
                        .build())
                .myApplication(myApplication)
                .applicationsView(applicationsView)
                .review(buildReviewSection(quest, currentUser, myApplication, applicationsView))
                .execution(buildExecutionSection(questResponse))
                .termChange(buildTermChangeSection(quest, questResponse.getAllowedActions()))
                .management(buildManagementSection(quest, questResponse))
                .build();
    }

    private QuestDetailReviewSectionDTO buildReviewSection(
            Quest quest,
            AppUser currentUser,
            QuestApplicationResponseDTO myApplication,
            QuestApplicationsViewDTO applicationsView
    ) {
        if (quest.getStatus() != QuestStatus.COMPLETED) {
            return hiddenOrLockedReviewSection(false);
        }

        QuestDetailReviewTargetDTO target = resolveReviewTarget(quest, currentUser, myApplication, applicationsView);
        if (target == null || currentUser == null) {
            return hiddenOrLockedReviewSection(true);
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

    private QuestDetailReviewSectionDTO hiddenOrLockedReviewSection(boolean visible) {
        return QuestDetailReviewSectionDTO.builder()
                .visible(visible)
                .canSubmit(false)
                .introTitle("Review")
                .introSubtitle(null)
                .placeholder("Add a short comment.")
                .submitLabel("Submit")
                .emptyStateMessage("Reviews become available here after the quest is completed.")
                .build();
    }

    private QuestDetailExecutionSectionDTO buildExecutionSection(QuestResponseDTO questResponse) {
        boolean canStart = questResponse.getAllowedActions().contains(QuestAllowedActionDTO.START);
        boolean canComplete = questResponse.getAllowedActions().contains(QuestAllowedActionDTO.COMPLETE);
        String helperText = questResponse.getViewerRelation() == QuestViewerRelationDTO.APPROVED_APPLICANT
                ? "You are the approved applicant for this quest."
                : null;

        return QuestDetailExecutionSectionDTO.builder()
                .visible(canStart || canComplete || helperText != null)
                .primaryAction(canStart ? QuestDetailExecutionActionDTO.START : (canComplete ? QuestDetailExecutionActionDTO.COMPLETE : null))
                .primaryActionLabel(canStart ? "Start work" : (canComplete ? "Mark complete" : null))
                .helperText(helperText)
                .build();
    }

    private QuestDetailTermChangeSectionDTO buildTermChangeSection(Quest quest, List<QuestAllowedActionDTO> allowedActions) {
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

    private QuestDetailManagementSectionDTO buildManagementSection(Quest quest, QuestResponseDTO questResponse) {
        boolean canManageQuest = questResponse.getViewerRelation() == QuestViewerRelationDTO.OWNER;
        String visibleToCirclesLabel = null;
        if (canManageQuest && quest.getAudience() == QuestAudience.CIRCLES) {
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
                .audienceLabel(canManageQuest ? presentationHelper.formatAudience(quest.getAudience()) : null)
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

        if (myApplication != null && myApplication.getStatus() == com.themuffinman.app.workmarket.model.QuestApplicationStatus.APPROVED) {
            return QuestDetailReviewTargetDTO.builder()
                    .userId(quest.getCreator().getId())
                    .username(quest.getCreator().getUsername())
                    .roleLabel("employer")
                    .build();
        }

        return null;
    }
}
