package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestApplicationDetailContextSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationDetailNavigationSectionDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationDetailResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationDetailSectionsDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.workmarket.dto.QuestAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestViewerRelationDTO;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestMgr;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service("workmarketQuestApplicationReadService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkmarketQuestApplicationReadService {

    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketQuestApplicationViewAssembler applicationViewAssembler;
    private final WorkmarketQuestMgr questMgr;
    private final WorkmarketQuestPresentationAssembler questPresentationAssembler;
    private final WorkmarketQuestAccessPolicyService questAccessPolicyService;
    private final WorkmarketQuestExecutionPrimitiveService workflowSupport;

    public List<QuestApplicationResponseDTO> getApplicationsForQuest(Long questId, AppUser currentUser) {
        workflowSupport.validateOwnerAuthority(workflowSupport.resolveTarget(questId), currentUser);
        return questApplicationRepository.findForQuestApplicationManagement(questId)
                .stream()
                .map(this::toManagementResponse)
                .toList();
    }

    public QuestApplicationsViewDTO getApplicationsViewForQuest(Long questId, AppUser currentUser, boolean showAll) {
        Quest quest = workflowSupport.resolveTarget(questId);
        workflowSupport.validateOwnerAuthority(quest, currentUser);

        List<QuestApplication> applications = questApplicationRepository.findForQuestApplicationManagement(questId);
        List<QuestApplicationResponseDTO> sortedApplications = applications.stream()
                .sorted(Comparator.comparing(QuestApplication::getId).reversed())
                .map(this::toManagementResponse)
                .toList();
        List<QuestApplication> oldestPendingApplications = applications.stream()
                .filter(application -> application.getStatus() == com.themuffinman.app.workmarket.model.QuestApplicationStatus.PENDING)
                .sorted(Comparator.comparing(QuestApplication::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(QuestApplication::getId))
                .toList();

        List<QuestApplicationResponseDTO> approvedApplications = sortedApplications.stream()
                .filter(application -> application.getStatus() == com.themuffinman.app.workmarket.model.QuestApplicationStatus.APPROVED)
                .toList();
        QuestApplicationResponseDTO featuredApplication = approvedApplications.isEmpty() ? null : approvedApplications.getFirst();

        List<QuestApplicationResponseDTO> visibleApplications = resolveVisibleApplications(quest, sortedApplications, approvedApplications, showAll);
        int hiddenApplicationsCount = Math.max(0, sortedApplications.size() - visibleApplications.size() - approvedApplications.size());
        boolean canRevealHiddenApplications = sortedApplications.size() > visibleApplications.size() + approvedApplications.size();

        return QuestApplicationsViewDTO.builder()
                .featuredApplication(featuredApplication)
                .approvedApplications(approvedApplications)
                .visibleApplications(visibleApplications)
                .pendingApplicationCount(oldestPendingApplications.size())
                .oldestPendingApplicationId(oldestPendingApplications.isEmpty() ? null : oldestPendingApplications.getFirst().getId())
                .hiddenApplicationsCount(hiddenApplicationsCount)
                .selectedApplicationId(resolveSelectedApplicationId(approvedApplications, visibleApplications))
                .canRevealHiddenApplications(canRevealHiddenApplications)
                .showingAllApplications(showAll)
                .revealLabel(resolveRevealLabel(quest, approvedApplications, showAll))
                .build();
    }

    public QuestApplicationsViewDTO getPublicApprovedApplicationsViewForQuest(Long questId) {
        List<QuestApplicationResponseDTO> approvedApplications = questApplicationRepository.findForQuestApplicationsByStatus(questId, com.themuffinman.app.workmarket.model.QuestApplicationStatus.APPROVED).stream()
                .sorted(Comparator.comparing(QuestApplication::getId).reversed())
                .map(this::toPublicResponse)
                .toList();

        return QuestApplicationsViewDTO.builder()
                .featuredApplication(approvedApplications.isEmpty() ? null : approvedApplications.getFirst())
                .approvedApplications(approvedApplications)
                .visibleApplications(List.of())
                .pendingApplicationCount(0)
                .oldestPendingApplicationId(null)
                .hiddenApplicationsCount(0)
                .selectedApplicationId(approvedApplications.isEmpty() ? null : approvedApplications.getFirst().getId())
                .canRevealHiddenApplications(false)
                .showingAllApplications(false)
                .revealLabel("")
                .build();
    }

    public List<QuestApplicationResponseDTO> getApplicationsForApplicant(AppUser currentUser) {
        return questApplicationRepository.findForApplicantDashboard(currentUser.getId())
                .stream()
                .map(this::toApplicantResponse)
                .toList();
    }

    public QuestApplicationListResponseDTO getApplicationsForApplicantPage(AppUser currentUser, Integer page, Integer size) {
        int safePage = Math.max(page == null ? 0 : page, 0);
        int safeSize = Math.min(Math.max(size == null ? 20 : size, 1), 50);
        var result = questApplicationRepository.findForApplicantDashboard(
                currentUser.getId(),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")))
        );
        return QuestApplicationListResponseDTO.builder()
                .items(result.getContent().stream().map(this::toApplicantResponse).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalItems(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    public QuestApplicationResponseDTO toApplicantResponse(QuestApplication application) {
        return applicationViewAssembler.toApplicantResponse(application);
    }

    public QuestApplicationResponseDTO toViewerResponse(QuestApplication application, AppUser currentUser) {
        return applicationViewAssembler.toViewerResponse(application, currentUser);
    }

    public QuestApplicationDetailResponseDTO getApplicationDetailResponseById(Long applicationId, AppUser currentUser) {
        QuestApplication application = questApplicationRepository.findForApplicationDetail(applicationId)
                .orElseThrow(() -> com.themuffinman.app.common.errors.ServiceErrors.notFound("Quest application not found with id " + applicationId));
        Quest quest = workflowSupport.resolveTarget(application.getQuest().getId());
        workflowSupport.validateApplicationDetailAccess(application, quest, currentUser);
        QuestApplicationResponseDTO applicationResponse = toViewerResponse(application, currentUser);
        QuestResponseDTO questResponse = toQuestResponse(quest, currentUser);
        return QuestApplicationDetailResponseDTO.builder()
                .summary(applicationResponse)
                .sections(QuestApplicationDetailSectionsDTO.builder()
                        .quest(questResponse)
                        .navigation(QuestApplicationDetailNavigationSectionDTO.builder()
                                .canOpenQuest(true)
                                .canOpenPostedBy(questResponse.getCreatorNavigation() != null)
                                .questId(applicationResponse.getQuestId())
                                .questNavigation(applicationResponse.getQuestNavigation())
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

    private QuestResponseDTO toQuestResponse(Quest quest, AppUser currentUser) {
        QuestResponseDTO response = questMgr.toDto(quest);
        QuestApplication viewerApplication = currentUser == null
                ? null
                : questApplicationRepository.findForViewerApplication(quest.getId(), currentUser.getId()).orElse(null);
        QuestViewerRelationDTO viewerRelation = questAccessPolicyService.resolveViewerRelation(quest, currentUser, viewerApplication);
        List<QuestAllowedActionDTO> allowedActions = questAccessPolicyService.resolveAllowedActions(quest, currentUser, viewerApplication);
        boolean canViewApplications = allowedActions.contains(QuestAllowedActionDTO.VIEW_APPLICATIONS);
        int workerTarget = Math.max(response.getAssigneeTarget() == null ? 1 : response.getAssigneeTarget(), 1);
        int approvedApplicationCount = Math.toIntExact(questApplicationRepository.countByQuestIdAndStatus(
                quest.getId(),
                com.themuffinman.app.workmarket.model.QuestApplicationStatus.APPROVED
        ));
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

    private QuestApplicationResponseDTO toManagementResponse(QuestApplication application) {
        return applicationViewAssembler.toManagementResponse(application);
    }

    private QuestApplicationResponseDTO toPublicResponse(QuestApplication application) {
        return applicationViewAssembler.toPublicResponse(application);
    }

    private List<QuestApplicationResponseDTO> resolveVisibleApplications(
            Quest quest,
            List<QuestApplicationResponseDTO> applications,
            List<QuestApplicationResponseDTO> approvedApplications,
            boolean showAll
    ) {
        if (quest.getStatus() == QuestStatus.CANCELLED) {
            return showAll ? applications : List.of();
        }

        if (!approvedApplications.isEmpty()) {
            if (!showAll) {
                return List.of();
            }

            return applications.stream()
                    .filter(application -> approvedApplications.stream()
                            .noneMatch(approved -> Objects.equals(approved.getId(), application.getId())))
                    .toList();
        }

        return applications;
    }

    private String resolveRevealLabel(Quest quest, List<QuestApplicationResponseDTO> approvedApplications, boolean showAll) {
        if (quest.getStatus() == QuestStatus.CANCELLED) {
            return showAll ? "Hide all applications" : "Show all applications";
        }

        if (!approvedApplications.isEmpty()) {
            return showAll ? "Hide other applications" : "Show other applications";
        }

        return showAll ? "Hide applications" : "Show applications";
    }

    private Long resolveSelectedApplicationId(
            List<QuestApplicationResponseDTO> approvedApplications,
            List<QuestApplicationResponseDTO> visibleApplications
    ) {
        if (!approvedApplications.isEmpty()) {
            return approvedApplications.getFirst().getId();
        }

        if (visibleApplications.isEmpty()) {
            return null;
        }

        return visibleApplications.getFirst().getId();
    }
}
