package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.pagination.PageResponseFactory;
import com.themuffinman.app.common.pagination.PageWindow;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.common.dto.NavigationTargetType;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.vision.dto.QuestDetailExecutionSectionDTO;
import com.themuffinman.app.vision.dto.QuestDetailManagementSectionDTO;
import com.themuffinman.app.vision.dto.QuestDetailNavigationSectionDTO;
import com.themuffinman.app.vision.dto.QuestDetailReviewSectionDTO;
import com.themuffinman.app.vision.dto.QuestDetailReviewTargetDTO;
import com.themuffinman.app.vision.dto.QuestDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestDetailSectionsDTO;
import com.themuffinman.app.vision.dto.QuestDetailTermChangeSectionDTO;
import com.themuffinman.app.vision.dto.QuestListPresetDTO;
import com.themuffinman.app.vision.dto.QuestListResponseDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestMgr;
import com.themuffinman.app.workmarket.mapper.WorkmarketUserReviewMgr;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestRepository;
import com.themuffinman.app.workmarket.repository.WorkmarketUserReviewRepository;
import com.themuffinman.app.workmarket.service.WorkmarketPresentationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service("workmarketQuestReadService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkmarketQuestReadService {

    private final WorkmarketQuestRepository questRepository;
    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketQuestApplicationReadService questApplicationReadService;
    private final WorkmarketQuestVisibilityService questVisibilityService;
    private final WorkmarketQuestAccessPolicyService questAccessPolicyService;
    private final WorkmarketQuestQueryService questQueryService;
    private final WorkmarketQuestExecutionPrimitiveService questExecutionPrimitiveService;
    private final WorkmarketQuestMgr questMgr;
    private final WorkmarketQuestPresentationAssembler questPresentationAssembler;
    private final WorkmarketUserReviewRepository userReviewRepository;
    private final WorkmarketUserReviewMgr userReviewMgr;
    private final WorkmarketPresentationHelper presentationHelper;

    public List<Quest> getAllQuests(AppUser currentUser) {
        return questRepository.findForQuestList().stream()
                .filter(quest -> questVisibilityService.canViewQuest(currentUser, quest))
                .toList();
    }

    public List<QuestResponseDTO> getAllQuestResponses(AppUser currentUser) {
        return toResponses(getAllQuests(currentUser), currentUser);
    }

    public QuestListResponseDTO searchQuests(
            AppUser currentUser,
            String query,
            com.themuffinman.app.vision.model.QuestStatus status,
            com.themuffinman.app.vision.model.QuestAudience audience,
            LocalDate dateFrom,
            LocalDate dateTo,
            String viewerTimeZone,
            Integer viewerTimezoneOffsetMinutes,
            Boolean excludeMine,
            Boolean withImages,
            Boolean scheduledOnly,
            Integer radiusKm,
            String sort,
            Integer page,
            Integer size
    ) {
        List<Quest> scopedQuests = loadQuestSearchScope(currentUser, radiusKm);
        return buildQuestListResponse(
                scopedQuests.stream()
                        .filter(quest -> questVisibilityService.canViewQuest(currentUser, quest))
                        .filter(quest -> !Boolean.TRUE.equals(excludeMine) || currentUser == null || !quest.getCreator().getId().equals(currentUser.getId()))
                        .filter(quest -> status == null || QuestStatus.valueOf(status.name()) == quest.getStatus())
                        .toList(),
                currentUser,
                query,
                audience == null ? null : QuestAudience.valueOf(audience.name()),
                dateFrom,
                dateTo,
                viewerTimeZone,
                viewerTimezoneOffsetMinutes,
                withImages,
                scheduledOnly,
                null,
                sort,
                page,
                size
        );
    }

    public QuestListResponseDTO getQuestListPreset(
            QuestListPresetDTO preset,
            AppUser currentUser,
            String query,
            com.themuffinman.app.vision.model.QuestAudience audience,
            LocalDate dateFrom,
            LocalDate dateTo,
            String viewerTimeZone,
            Integer viewerTimezoneOffsetMinutes,
            Boolean withImages,
            Boolean scheduledOnly,
            Integer radiusKm,
            String sort,
            Integer page,
            Integer size
    ) {
        List<Quest> scopedQuests = loadQuestSearchScope(currentUser, radiusKm);
        List<Quest> baseQuests = switch (preset) {
            case AVAILABLE -> scopedQuests.stream()
                    .filter(quest -> questVisibilityService.canViewQuest(currentUser, quest))
                    .filter(quest -> quest.getStatus() == QuestStatus.OPEN)
                    .filter(quest -> currentUser == null || !questAccessPolicyService.isQuestOwner(quest, currentUser))
                    .toList();
            case MY_VISIBLE -> scopedQuests.stream()
                    .filter(quest -> questVisibilityService.canViewQuest(currentUser, quest))
                    .filter(quest -> questAccessPolicyService.isQuestOwner(quest, currentUser))
                    .filter(quest -> quest.getStatus() == QuestStatus.OPEN
                            || quest.getStatus() == QuestStatus.ASSIGNED
                            || quest.getStatus() == QuestStatus.IN_PROGRESS
                            || quest.getStatus() == QuestStatus.WAITING_CONFIRMATION)
                    .toList();
            case MY_ACTIVE -> scopedQuests.stream()
                    .filter(quest -> questVisibilityService.canViewQuest(currentUser, quest))
                    .filter(quest -> questAccessPolicyService.isQuestOwner(quest, currentUser))
                    .filter(quest -> quest.getStatus() == QuestStatus.ASSIGNED
                            || quest.getStatus() == QuestStatus.IN_PROGRESS
                            || quest.getStatus() == QuestStatus.WAITING_CONFIRMATION)
                    .toList();
        };

        return buildQuestListResponse(
                baseQuests,
                currentUser,
                query,
                audience == null ? null : QuestAudience.valueOf(audience.name()),
                dateFrom,
                dateTo,
                viewerTimeZone,
                viewerTimezoneOffsetMinutes,
                withImages,
                scheduledOnly,
                null,
                sort,
                page,
                size
        );
    }

    public Quest getQuestById(Long id, AppUser currentUser) {
        Quest quest = questExecutionPrimitiveService.resolveTarget(id);
        if (!questVisibilityService.canViewQuest(currentUser, quest)) {
            throw ServiceErrors.notFound("Quest not found with id " + id);
        }

        return quest;
    }

    public QuestResponseDTO getQuestResponseById(Long id, AppUser currentUser) {
        return toResponse(getQuestById(id, currentUser), currentUser);
    }

    public QuestDetailResponseDTO getQuestDetailResponseById(Long id, AppUser currentUser) {
        Map<Long, QuestApplication> applicationsByQuestId = getCurrentUserApplicationsByQuestId(currentUser);
        Quest quest = getQuestById(id, currentUser);
        QuestResponseDTO questResponse = toResponse(quest, currentUser, applicationsByQuestId);
        QuestApplication viewerApplication = currentUser == null ? null : applicationsByQuestId.get(quest.getId());
        QuestApplicationResponseDTO myApplication = questApplicationReadService.toViewerResponse(viewerApplication, currentUser);
        QuestApplicationsViewDTO applicationsView = questResponse.isCanViewApplications()
                ? questApplicationReadService.getApplicationsViewForQuest(quest.getId(), currentUser, false)
                : (questResponse.isShowApprovedApplicants()
                ? questApplicationReadService.getPublicApprovedApplicationsViewForQuest(quest.getId())
                : null);

        QuestDetailResponseDTO response = QuestDetailResponseDTO.builder()
                .summary(questResponse)
                .sections(QuestDetailSectionsDTO.builder()
                        .navigation(QuestDetailNavigationSectionDTO.builder()
                                .listNavigation(NavigationTargetDTO.builder()
                                        .type(NavigationTargetType.QUEST_LIST)
                                        .entityId(null)
                                        .build())
                                .build())
                        .myApplication(myApplication)
                        .applicationsView(applicationsView)
                        .review(buildQuestDetailReviewSection(quest, currentUser, myApplication, applicationsView))
                        .execution(buildQuestDetailExecutionSection(questResponse))
                        .termChange(buildQuestDetailTermChangeSection(quest, questResponse.getAllowedActions()))
                        .management(buildQuestDetailManagementSection(questResponse))
                        .build())
                .quest(questResponse)
                .myApplication(myApplication)
                .applicationsView(applicationsView)
                .build();
        return response;
    }

    public QuestResponseDTO toResponse(Quest quest, AppUser currentUser) {
        return toResponse(quest, currentUser, getCurrentUserApplicationsByQuestId(currentUser));
    }

    public List<QuestResponseDTO> toResponses(List<Quest> quests, AppUser currentUser) {
        Map<Long, QuestApplication> applicationsByQuestId = getCurrentUserApplicationsByQuestId(currentUser);
        return quests.stream()
                .map(quest -> toResponse(quest, currentUser, applicationsByQuestId))
                .toList();
    }

    private QuestResponseDTO toResponse(Quest quest, AppUser currentUser, Map<Long, QuestApplication> applicationsByQuestId) {
        QuestResponseDTO response = questMgr.toDto(quest);
        QuestApplication viewerApplication = currentUser == null ? null : applicationsByQuestId.get(quest.getId());
        var viewerRelation = questAccessPolicyService.resolveViewerRelation(quest, currentUser, viewerApplication);
        var allowedActions = questAccessPolicyService.resolveAllowedActions(quest, currentUser, viewerApplication);
        boolean canViewApplications = allowedActions.contains(com.themuffinman.app.vision.dto.QuestAllowedActionDTO.VIEW_APPLICATIONS);
        int workerTarget = Math.max(response.getAssigneeTarget() == null ? 1 : response.getAssigneeTarget(), 1);
        int approvedApplicationCount = Math.toIntExact(questApplicationRepository.countByQuestIdAndStatus(quest.getId(), com.themuffinman.app.workmarket.model.QuestApplicationStatus.APPROVED));
        int remainingAssigneeSlots = Math.max(workerTarget - approvedApplicationCount, 0);

        response = questMgr.withViewerContext(
                response,
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

    private QuestDetailReviewSectionDTO buildQuestDetailReviewSection(
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

    private QuestDetailExecutionSectionDTO buildQuestDetailExecutionSection(QuestResponseDTO questResponse) {
        boolean canStart = questResponse.getAllowedActions().contains(com.themuffinman.app.vision.dto.QuestAllowedActionDTO.START);
        boolean canComplete = questResponse.getAllowedActions().contains(com.themuffinman.app.vision.dto.QuestAllowedActionDTO.COMPLETE);
        String helperText = questResponse.getViewerRelation() == com.themuffinman.app.vision.dto.QuestViewerRelationDTO.APPROVED_APPLICANT
                ? "You are the approved applicant for this quest."
                : null;

        return QuestDetailExecutionSectionDTO.builder()
                .visible(canStart || canComplete || helperText != null)
                .primaryAction(canStart
                        ? com.themuffinman.app.vision.dto.QuestDetailExecutionActionDTO.START
                        : (canComplete ? com.themuffinman.app.vision.dto.QuestDetailExecutionActionDTO.COMPLETE : null))
                .primaryActionLabel(canStart ? "Start work" : (canComplete ? "Mark complete" : null))
                .helperText(helperText)
                .build();
    }

    private QuestDetailTermChangeSectionDTO buildQuestDetailTermChangeSection(Quest quest, List<com.themuffinman.app.vision.dto.QuestAllowedActionDTO> allowedActions) {
        return QuestDetailTermChangeSectionDTO.builder()
                .visible(quest.getStatus() == QuestStatus.WAITING_CONFIRMATION)
                .actionable(
                        allowedActions.contains(com.themuffinman.app.vision.dto.QuestAllowedActionDTO.CONFIRM_TERM_CHANGE)
                                || allowedActions.contains(com.themuffinman.app.vision.dto.QuestAllowedActionDTO.REJECT_TERM_CHANGE)
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

    private QuestDetailManagementSectionDTO buildQuestDetailManagementSection(QuestResponseDTO questResponse) {
        boolean canManageQuest = questResponse.getViewerRelation() == com.themuffinman.app.vision.dto.QuestViewerRelationDTO.OWNER;
        String visibleToCirclesLabel = null;
        if (canManageQuest && questResponse.getAudience() == com.themuffinman.app.vision.model.QuestAudience.CIRCLES) {
            List<String> circleNames = questResponse.getVisibleToCircles() == null
                    ? List.of()
                    : questResponse.getVisibleToCircles().stream()
                    .map(circle -> circle.getName())
                    .filter(name -> name != null && !name.isBlank())
                    .toList();
            visibleToCirclesLabel = circleNames.isEmpty() ? "Selected circles" : String.join(", ", circleNames);
        }

        return QuestDetailManagementSectionDTO.builder()
                .editVisible(questResponse.getAllowedActions().contains(com.themuffinman.app.vision.dto.QuestAllowedActionDTO.EDIT))
                .deleteVisible(questResponse.getAllowedActions().contains(com.themuffinman.app.vision.dto.QuestAllowedActionDTO.DELETE))
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

        if (myApplication != null && myApplication.getStatus() == com.themuffinman.app.vision.model.QuestApplicationStatus.APPROVED) {
            return QuestDetailReviewTargetDTO.builder()
                    .userId(quest.getCreator().getId())
                    .username(quest.getCreator().getUsername())
                    .roleLabel("employer")
                    .build();
        }

        return null;
    }

    private QuestListResponseDTO buildQuestListResponse(
            List<Quest> sourceQuests,
            AppUser currentUser,
            String query,
            QuestAudience audience,
            LocalDate dateFrom,
            LocalDate dateTo,
            String viewerTimeZone,
            Integer viewerTimezoneOffsetMinutes,
            Boolean withImages,
            Boolean scheduledOnly,
            Integer radiusKm,
            String sort,
            Integer page,
            Integer size
    ) {
        PageWindow<Quest> pageWindow = questQueryService.buildQuestPage(
                sourceQuests,
                query,
                audience,
                dateFrom,
                dateTo,
                viewerTimeZone,
                viewerTimezoneOffsetMinutes,
                withImages,
                scheduledOnly,
                radiusKm,
                currentUser == null ? null : currentUser.getLocationLatitude(),
                currentUser == null ? null : currentUser.getLocationLongitude(),
                sort,
                page,
                size,
                12
        );

        return PageResponseFactory.fromWindow(pageWindow, window -> QuestListResponseDTO.builder()
                .items(toResponses(window.items(), currentUser))
                .page(window.page())
                .size(window.size())
                .totalItems(window.totalItems())
                .totalPages(window.totalPages())
                .build());
    }

    private List<Quest> loadQuestSearchScope(AppUser currentUser, Integer radiusKm) {
        if (radiusKm == null) {
            return questRepository.findForQuestList();
        }

        if (currentUser == null || currentUser.getLocationLatitude() == null || currentUser.getLocationLongitude() == null) {
            return List.of();
        }

        List<Long> nearbyQuestIds = questRepository.findIdsWithinRadius(
                currentUser.getLocationLatitude(),
                currentUser.getLocationLongitude(),
                radiusKm * 1000
        );
        if (nearbyQuestIds.isEmpty()) {
            return List.of();
        }

        return questRepository.findForQuestListByIds(nearbyQuestIds);
    }

    private Map<Long, QuestApplication> getCurrentUserApplicationsByQuestId(AppUser currentUser) {
        if (currentUser == null) {
            return Map.of();
        }

        return questApplicationRepository.findForApplicantDashboard(currentUser.getId()).stream()
                .collect(Collectors.toMap(
                        application -> application.getQuest().getId(),
                        Function.identity(),
                        (left, right) -> left
                ));
    }
}
