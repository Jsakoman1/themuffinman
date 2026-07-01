package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.pagination.PageResponseFactory;
import com.themuffinman.app.common.pagination.PageWindow;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.QuestApplicationDetailContextSectionDTO;
import com.themuffinman.app.vision.dto.QuestApplicationDetailNavigationSectionDTO;
import com.themuffinman.app.vision.dto.QuestApplicationDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationDetailSectionsDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.vision.dto.QuestDetailNavigationSectionDTO;
import com.themuffinman.app.vision.dto.QuestDetailSectionsDTO;
import com.themuffinman.app.vision.dto.QuestDetailResponseDTO;
import com.themuffinman.app.vision.dto.QuestListPresetDTO;
import com.themuffinman.app.vision.dto.QuestListResponseDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.vision.mapper.QuestMgr;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestApplication;
import com.themuffinman.app.vision.model.QuestAudience;
import com.themuffinman.app.vision.model.QuestStatus;
import com.themuffinman.app.vision.repository.QuestApplicationRepository;
import com.themuffinman.app.vision.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestReadService {

    private final QuestRepository questRepository;
    private final QuestApplicationRepository questApplicationRepository;
    private final QuestApplicationService questApplicationService;
    private final QuestVisibilityService questVisibilityService;
    private final QuestAccessPolicyService questAccessPolicyService;
    private final QuestQueryService questQueryService;
    private final QuestExecutionPrimitiveService questExecutionPrimitiveService;
    private final QuestMgr questMgr;
    private final QuestViewAssembler questViewAssembler;

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
            QuestStatus status,
            QuestAudience audience,
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
                        .filter(quest -> status == null || quest.getStatus() == status)
                        .toList(),
                currentUser,
                query,
                audience,
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
                    .filter(quest -> quest.getStatus().isVisibleOwnerWork())
                    .toList();
            case MY_ACTIVE -> scopedQuests.stream()
                    .filter(quest -> questVisibilityService.canViewQuest(currentUser, quest))
                    .filter(quest -> questAccessPolicyService.isQuestOwner(quest, currentUser))
                    .filter(quest -> quest.getStatus().isActiveForOwner())
                    .toList();
        };

        return buildQuestListResponse(
                baseQuests,
                currentUser,
                query,
                audience,
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
        Quest quest = requireQuest(id);
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
        QuestApplicationResponseDTO myApplication = questApplicationService.toViewerResponse(viewerApplication, currentUser);
        QuestApplicationsViewDTO applicationsView = questResponse.isCanViewApplications()
                ? questApplicationService.getApplicationsViewForQuest(quest.getId(), currentUser, false)
                : (questResponse.isShowApprovedApplicants()
                ? questApplicationService.getPublicApprovedApplicationsViewForQuest(quest.getId())
                : null);
        QuestDetailSectionsDTO sections = QuestDetailSectionsDTO.builder()
                .navigation(QuestDetailNavigationSectionDTO.builder()
                        .listNavigation(com.themuffinman.app.common.dto.NavigationTargetDTO.builder()
                                .type(com.themuffinman.app.common.dto.NavigationTargetType.QUEST_LIST)
                                .entityId(null)
                                .build())
                        .build())
                .myApplication(myApplication)
                .applicationsView(applicationsView)
                .review(questViewAssembler.buildQuestDetailReviewSection(quest, currentUser, myApplication, applicationsView))
                .execution(questViewAssembler.buildQuestDetailExecutionSection(questResponse))
                .termChange(questViewAssembler.buildQuestDetailTermChangeSection(quest, questResponse.getAllowedActions()))
                .management(questViewAssembler.buildQuestDetailManagementSection(questResponse))
                .build();
        return QuestDetailResponseDTO.builder()
                .summary(questResponse)
                .sections(sections)
                .quest(questResponse)
                .myApplication(sections.getMyApplication())
                .applicationsView(sections.getApplicationsView())
                .build();
    }

    public QuestApplicationDetailResponseDTO getApplicationDetailResponseById(Long applicationId, AppUser currentUser) {
        QuestApplication application = questApplicationRepository.findForApplicationDetail(applicationId)
                .orElseThrow(() -> ServiceErrors.notFound("Quest application not found with id " + applicationId));
        Quest quest = questExecutionPrimitiveService.resolveTarget(application.getQuest().getId());
        questExecutionPrimitiveService.validateApplicationDetailAccess(application, quest, currentUser);
        QuestApplicationResponseDTO applicationResponse = questApplicationService.toViewerResponse(application, currentUser);
        QuestResponseDTO questResponse = toResponse(quest, currentUser);

        return QuestApplicationDetailResponseDTO.builder()
                .summary(applicationResponse)
                .sections(QuestApplicationDetailSectionsDTO.builder()
                        .quest(questResponse)
                        .navigation(QuestApplicationDetailNavigationSectionDTO.builder()
                                .canOpenQuest(true)
                                .canOpenPostedBy(questResponse.getCreatorNavigation() != null)
                                .questId(questResponse.getId())
                                .questNavigation(questResponse.getQuestNavigation())
                                .postedByNavigation(questResponse.getCreatorNavigation())
                                .build())
                        .context(QuestApplicationDetailContextSectionDTO.builder()
                                .questLabel(applicationResponse.getQuestTitle())
                                .postedByLabel(questResponse.getCreatorUsername())
                                .showStatus(true)
                                .showTerm(true)
                                .showWorkers(applicationResponse.getQuestAssigneeTarget() == null || applicationResponse.getQuestAssigneeTarget() > 1)
                                .build())
                        .build())
                .application(applicationResponse)
                .quest(questResponse)
                .build();
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

    private Quest requireQuest(Long id) {
        return questExecutionPrimitiveService.resolveTarget(id);
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

    private QuestResponseDTO toResponse(Quest quest, AppUser currentUser, Map<Long, QuestApplication> applicationsByQuestId) {
        return questViewAssembler.toResponse(quest, currentUser, applicationsByQuestId);
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
