package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestAllowedAction;
import com.themuffinman.app.workmarket.dto.QuestApplicationDetailResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationDetailContextSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationDetailNavigationSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationDetailSectionsDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailNavigationSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailExecutionAction;
import com.themuffinman.app.workmarket.dto.QuestDetailExecutionSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailManagementSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailReviewSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailReviewTargetDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailSectionsDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailTermChangeSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestListPreset;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.mapper.QuestMgr;
import com.themuffinman.app.workmarket.mapper.QuestApplicationMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import com.themuffinman.app.identity.service.AppUserLookupService;
import com.themuffinman.app.common.pagination.PageResponseFactory;
import com.themuffinman.app.common.pagination.PageWindow;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.workmarket.repository.QuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.QuestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestService {

    private final QuestRepository questRepository;
    private final AppUserLookupService appUserLookupService;
    private final QuestApplicationRepository questApplicationRepository;
    private final QuestApplicationMgr questApplicationMgr;
    private final QuestApplicationService questApplicationService;
    private final QuestWorkflowNotificationService questWorkflowNotificationService;
    private final QuestVisibilityService questVisibilityService;
    private final QuestValidationService questValidationService;
    private final QuestStateTransitionService questStateTransitionService;
    private final QuestAccessPolicyService questAccessPolicyService;
    private final QuestQueryService questQueryService;
    private final QuestUpdateService questUpdateService;
    private final QuestMgr questMgr;
    private final QuestViewAssembler questViewAssembler;

    public Quest createQuest(QuestRequestDTO dto, AppUser currentUser) {
        questValidationService.validateCreateRequest(dto);
        Quest quest = questMgr.toEntity(dto, resolveQuestCreator(dto, currentUser));
        if (quest.getAudience() == null) {
            quest.setAudience(QuestAudience.CIRCLES);
        }
        questValidationService.applyQuestVisibilityCircles(quest, dto.getAudience(), dto.getSelectedCircleIds(), quest.getCreator());
        quest.setAssigneeTarget(questValidationService.normalizeAssigneeTarget(dto.getAssigneeTarget()));
        questStateTransitionService.applyConfirmedQuestTermFields(quest, dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());
        return questRepository.save(quest);
    }

    public List<Quest> getAllQuests(AppUser currentUser) {
        return questRepository.findAllWithCreator().stream()
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
            Boolean excludeMine,
            Boolean withImages,
            Boolean scheduledOnly,
            String sort,
            Integer page,
            Integer size
    ) {
        return buildQuestListResponse(
                questRepository.findAllWithCreator().stream()
                        .filter(quest -> questVisibilityService.canViewQuest(currentUser, quest))
                        .filter(quest -> !Boolean.TRUE.equals(excludeMine) || currentUser == null || !quest.getCreator().getId().equals(currentUser.getId()))
                        .filter(quest -> status == null || quest.getStatus() == status)
                        .toList(),
                currentUser,
                query,
                audience,
                dateFrom,
                dateTo,
                withImages,
                scheduledOnly,
                sort,
                page,
                size
        );
    }

    public QuestListResponseDTO getQuestListPreset(
            QuestListPreset preset,
            AppUser currentUser,
            String query,
            QuestAudience audience,
            LocalDate dateFrom,
            LocalDate dateTo,
            Boolean withImages,
            Boolean scheduledOnly,
            String sort,
            Integer page,
            Integer size
    ) {
        List<Quest> baseQuests = switch (preset) {
            case AVAILABLE -> getAllQuests(currentUser).stream()
                    .filter(quest -> quest.getStatus() == QuestStatus.OPEN)
                    .filter(quest -> currentUser == null || !questAccessPolicyService.isQuestOwner(quest, currentUser))
                    .toList();
            case MY_VISIBLE -> getAllQuests(currentUser).stream()
                    .filter(quest -> questAccessPolicyService.isQuestOwner(quest, currentUser))
                    .filter(quest -> quest.getStatus().isVisibleOwnerWork())
                    .toList();
            case MY_ACTIVE -> getAllQuests(currentUser).stream()
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
                withImages,
                scheduledOnly,
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
        QuestApplicationResponseDTO myApplication = viewerApplication == null ? null : questApplicationMgr.toDto(viewerApplication);
        QuestApplicationsViewDTO applicationsView = questResponse.isCanViewApplications()
                ? questApplicationService.getApplicationsViewForQuest(quest.getId(), currentUser, false)
                : null;
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
        QuestApplication application = questApplicationRepository.findByIdDetailed(applicationId)
                .orElseThrow(() -> ServiceErrors.notFound("Quest application not found with id " + applicationId));
        Quest quest = requireQuest(application.getQuest().getId());
        validateApplicationDetailAccess(application, quest, currentUser);
        QuestApplicationResponseDTO applicationResponse = questApplicationMgr.toDto(application);
        QuestResponseDTO questResponse = toResponse(quest, currentUser);

        return QuestApplicationDetailResponseDTO.builder()
                .summary(applicationResponse)
                .sections(QuestApplicationDetailSectionsDTO.builder()
                        .quest(questResponse)
                        .navigation(QuestApplicationDetailNavigationSectionDTO.builder()
                                .canOpenQuest(true)
                                .questId(questResponse.getId())
                                .questNavigation(questResponse.getQuestNavigation())
                                .build())
                        .context(QuestApplicationDetailContextSectionDTO.builder()
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

    @Transactional
    public void deleteQuest(Long id, AppUser currentUser) {
        Quest quest = requireQuestForOwnerActions(id, currentUser);
        questWorkflowNotificationService.notifyQuestDeleted(quest, currentUser);
        questApplicationRepository.deleteByQuestId(id);
        questRepository.deleteById(id);
    }

    @Transactional
    public Quest updateQuest(Long id, QuestRequestDTO dto, AppUser currentUser) {
        Quest quest = requireQuestForOwnerActions(id, currentUser);
        questUpdateService.applyQuestUpdates(quest, dto, currentUser);
        return questRepository.save(quest);
    }

    @Transactional
    public Quest startQuest(Long id, AppUser currentUser) {
        Quest quest = requireQuestForExecutionActions(id, currentUser);
        questStateTransitionService.requireQuestStatus(quest, QuestStatus.ASSIGNED, "Quest can only be started after an application is approved");
        quest.setStatus(QuestStatus.IN_PROGRESS);
        Quest savedQuest = questRepository.save(quest);
        questWorkflowNotificationService.notifyApprovedApplicant(savedQuest, currentUser, QuestNewsType.QUEST_STARTED, "Quest started", "The quest \"" + savedQuest.getTitle() + "\" has started.");
        return savedQuest;
    }

    @Transactional
    public Quest completeQuest(Long id, AppUser currentUser) {
        Quest quest = requireQuestForExecutionActions(id, currentUser);
        questStateTransitionService.requireQuestStatus(quest, QuestStatus.IN_PROGRESS, "Quest can only be completed while it is in progress");
        quest.setStatus(QuestStatus.COMPLETED);
        Quest savedQuest = questRepository.save(quest);
        questWorkflowNotificationService.notifyApprovedApplicant(savedQuest, currentUser, QuestNewsType.QUEST_COMPLETED, "Quest completed", "The quest \"" + savedQuest.getTitle() + "\" has been completed.");
        return savedQuest;
    }

    @Transactional
    public Quest confirmQuestTermChange(Long id, AppUser currentUser) {
        Quest quest = requireQuest(id);
        questStateTransitionService.validateQuestTermDecisionAuthority(quest, currentUser);
        questStateTransitionService.confirmQuestTermChange(quest);
        Quest savedQuest = questRepository.save(quest);
        questWorkflowNotificationService.notifyApprovedApplicant(savedQuest, currentUser, QuestNewsType.QUEST_TERM_CONFIRMED, "Quest time confirmed", "The new time for \"" + savedQuest.getTitle() + "\" was confirmed.");
        questWorkflowNotificationService.notifyQuestCreator(savedQuest, currentUser, QuestNewsType.QUEST_TERM_CONFIRMED, "Quest time confirmed", "The new time for \"" + savedQuest.getTitle() + "\" was confirmed.");
        return savedQuest;
    }

    @Transactional
    public Quest rejectQuestTermChange(Long id, AppUser currentUser) {
        Quest quest = requireQuest(id);
        questStateTransitionService.validateQuestTermDecisionAuthority(quest, currentUser);
        questStateTransitionService.rejectQuestTermChange(quest);
        Quest savedQuest = questRepository.save(quest);
        questWorkflowNotificationService.notifyApprovedApplicant(savedQuest, currentUser, QuestNewsType.QUEST_TERM_REJECTED, "Quest time rejected", "The proposed time change for \"" + savedQuest.getTitle() + "\" was rejected.");
        questWorkflowNotificationService.notifyQuestCreator(savedQuest, currentUser, QuestNewsType.QUEST_TERM_REJECTED, "Quest time rejected", "The proposed time change for \"" + savedQuest.getTitle() + "\" was rejected.");
        return savedQuest;
    }

    private Quest requireQuest(Long id) {
        return questRepository.findByIdWithCreator(id)
                .orElseThrow(() -> ServiceErrors.notFound("Quest not found with id " + id));
    }

    private QuestListResponseDTO buildQuestListResponse(
            List<Quest> sourceQuests,
            AppUser currentUser,
            String query,
            QuestAudience audience,
            LocalDate dateFrom,
            LocalDate dateTo,
            Boolean withImages,
            Boolean scheduledOnly,
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
                withImages,
                scheduledOnly,
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

    private Map<Long, QuestApplication> getCurrentUserApplicationsByQuestId(AppUser currentUser) {
        if (currentUser == null) {
            return Map.of();
        }

        return questApplicationRepository.findByApplicantId(currentUser.getId()).stream()
                .collect(Collectors.toMap(
                        application -> application.getQuest().getId(),
                        Function.identity(),
                        (left, right) -> left
                ));
    }

    private Quest requireQuestForOwnerActions(Long id, AppUser currentUser) {
        Quest quest = requireQuest(id);
        validateQuestOwnerOrAdmin(quest, currentUser);
        return quest;
    }

    private Quest requireQuestForExecutionActions(Long id, AppUser currentUser) {
        Quest quest = requireQuest(id);
        questStateTransitionService.validateQuestExecutionAuthority(quest, currentUser);
        return quest;
    }

    private void validateQuestOwner(Quest quest, AppUser currentUser) {
        if (!quest.getCreator().getId().equals(currentUser.getId())) {
            throw ServiceErrors.forbidden("You are not allowed to modify this quest");
        }
    }

    private void validateQuestOwnerOrAdmin(Quest quest, AppUser currentUser) {
        if (questAccessPolicyService.isAdmin(currentUser)) {
            return;
        }

        validateQuestOwner(quest, currentUser);
    }

    private AppUser resolveQuestCreator(QuestRequestDTO dto, AppUser currentUser) {
        if (questAccessPolicyService.isAdmin(currentUser) && dto.getCreatorId() != null) {
            return appUserLookupService.requireById(dto.getCreatorId(), "Creator not found with id " + dto.getCreatorId());
        }

        return currentUser;
    }

    private void validateApplicationDetailAccess(QuestApplication application, Quest quest, AppUser currentUser) {
        if (currentUser == null) {
            throw ServiceErrors.forbidden("You are not allowed to view this application");
        }

        if (questAccessPolicyService.isAdmin(currentUser) || questAccessPolicyService.isQuestOwner(quest, currentUser)) {
            return;
        }

        if (application.getApplicant() != null && application.getApplicant().getId().equals(currentUser.getId())) {
            return;
        }

        throw ServiceErrors.forbidden("You are not allowed to view this application");
    }

}
