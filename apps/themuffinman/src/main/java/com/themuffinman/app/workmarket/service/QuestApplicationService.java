package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.AdminQuestApplicationUpdateRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.QuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.QuestRepository;
import com.themuffinman.app.common.pagination.PageResponseFactory;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.common.normalization.SearchQueryNormalizer;
import com.themuffinman.app.common.errors.ServiceErrors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Comparator;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestApplicationService {

    private final QuestApplicationRepository questApplicationRepository;
    private final QuestRepository questRepository;
    private final QuestApplicationViewAssembler applicationViewAssembler;
    private final QuestApplicationWorkflowSupport workflowSupport;
    private final ApplyForQuestUseCase applyForQuestUseCase;
    private final UpdateMyApplicationUseCase updateMyApplicationUseCase;
    private final WithdrawMyApplicationUseCase withdrawMyApplicationUseCase;
    private final ApproveApplicationUseCase approveApplicationUseCase;
    private final DeclineApplicationUseCase declineApplicationUseCase;

    @Transactional
    public QuestApplicationResponseDTO applyForQuest(Long questId, QuestApplicationRequestDTO dto, AppUser currentUser) {
        return toApplicantResponse(applyForQuestUseCase.execute(questId, dto, currentUser));
    }

    public List<QuestApplicationResponseDTO> getApplicationsForQuest(Long questId, AppUser currentUser) {
        workflowSupport.validateQuestOwnerOrAdmin(workflowSupport.requireQuest(questId), currentUser);

        return questApplicationRepository.findForQuestApplicationManagement(questId)
                .stream()
                .map(this::toManagementResponse)
                .toList();
    }

    public QuestApplicationsViewDTO getApplicationsViewForQuest(Long questId, AppUser currentUser, boolean showAll) {
        Quest quest = workflowSupport.requireQuest(questId);
        workflowSupport.validateQuestOwnerOrAdmin(quest, currentUser);

        List<QuestApplication> applications = questApplicationRepository.findForQuestApplicationManagement(questId);
        List<QuestApplicationResponseDTO> sortedApplications = applications.stream()
                .sorted(Comparator.comparing(QuestApplication::getId).reversed())
                .map(this::toManagementResponse)
                .toList();
        List<QuestApplication> oldestPendingApplications = applications.stream()
                .filter(application -> application.getStatus() == QuestApplicationStatus.PENDING)
                .sorted(Comparator.comparing(QuestApplication::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(QuestApplication::getId))
                .toList();

        List<QuestApplicationResponseDTO> approvedApplications = sortedApplications.stream()
                .filter(application -> application.getStatus() == QuestApplicationStatus.APPROVED)
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
        List<QuestApplicationResponseDTO> approvedApplications = questApplicationRepository.findForQuestApplicationsByStatus(questId, QuestApplicationStatus.APPROVED).stream()
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

    public QuestApplicationResponseDTO toApplicantResponse(QuestApplication application) {
        return applicationViewAssembler.toApplicantResponse(application);
    }

    public QuestApplicationResponseDTO toViewerResponse(QuestApplication application, AppUser currentUser) {
        return applicationViewAssembler.toViewerResponse(application, currentUser);
    }

    private QuestApplicationResponseDTO toManagementResponse(QuestApplication application) {
        return applicationViewAssembler.toManagementResponse(application);
    }

    private QuestApplicationResponseDTO toPublicResponse(QuestApplication application) {
        return applicationViewAssembler.toPublicResponse(application);
    }

    public List<QuestApplicationResponseDTO> getAllApplicationsForAdmin(AppUser currentUser) {
        workflowSupport.validateAdmin(currentUser);

        return questApplicationRepository.findForAdminApplicationList().stream()
                .map(this::toManagementResponse)
                .toList();
    }

    public QuestApplicationListResponseDTO searchApplicationsForAdmin(
            AppUser currentUser,
            String query,
            QuestApplicationStatus status,
            Integer page,
            Integer size
    ) {
        workflowSupport.validateAdmin(currentUser);

        String normalizedQuery = SearchQueryNormalizer.normalize(query).toLowerCase(Locale.ROOT);
        int safeSize = size == null || size < 1 ? 20 : size;
        int safePage = page == null || page < 0 ? 0 : page;

        List<QuestApplicationResponseDTO> filtered = questApplicationRepository.findForAdminApplicationList().stream()
                .map(this::toManagementResponse)
                .filter(application -> status == null || application.getStatus() == status)
                .filter(application -> matchesAdminQuery(application, normalizedQuery))
                .sorted(Comparator.comparing(QuestApplicationResponseDTO::getCreatedAt).reversed())
                .toList();

        return PageResponseFactory.fromItems(filtered, safePage, safeSize, pageWindow -> QuestApplicationListResponseDTO.builder()
                .items(pageWindow.items())
                .page(pageWindow.page())
                .size(pageWindow.size())
                .totalItems(pageWindow.totalItems())
                .totalPages(pageWindow.totalPages())
                .build());
    }

    @Transactional
    public QuestApplicationResponseDTO updateApplicationForAdmin(
            Long applicationId,
            AdminQuestApplicationUpdateRequestDTO dto,
            AppUser currentUser
    ) {
        workflowSupport.validateAdmin(currentUser);
        if (dto == null) {
            throw ServiceErrors.badRequest("Application update request is required");
        }

        QuestApplication application = questApplicationRepository.findForApplicationDetail(applicationId)
                .orElseThrow(() -> ServiceErrors.notFound("Quest application not found with id " + applicationId));

        if (dto.getMessage() != null) {
            if (!RichTextInputValidator.hasContent(dto.getMessage())) {
                throw ServiceErrors.badRequest("Application message is required");
            }
            application.setMessage(RichTextInputValidator.sanitize(dto.getMessage()));
        }

        if (dto.getProposedPrice() != null) {
            if (workflowSupport.isFreeQuest(application.getQuest())) {
                throw ServiceErrors.badRequest("Free quest applications cannot have a proposed price");
            }
            if (dto.getProposedPrice().compareTo(java.math.BigDecimal.valueOf(0.01)) < 0) {
                throw ServiceErrors.badRequest("Proposed price must be at least 0.01");
            }
            application.setProposedPrice(dto.getProposedPrice());
        }

        if (dto.getStatus() != null && dto.getStatus() != application.getStatus()) {
            applyAdminStatusUpdate(application, dto.getStatus(), currentUser);
        }

        return toApplicantResponse(questApplicationRepository.save(application));
    }

    @Transactional
    public void deleteApplicationForAdmin(Long applicationId, AppUser currentUser) {
        workflowSupport.validateAdmin(currentUser);
        QuestApplication application = questApplicationRepository.findForApplicationDetail(applicationId)
                .orElseThrow(() -> ServiceErrors.notFound("Quest application not found with id " + applicationId));

        Quest quest = application.getQuest();
        if (application.getStatus() == QuestApplicationStatus.APPROVED) {
            if (quest.getStatus() == QuestStatus.ASSIGNED || quest.getStatus() == QuestStatus.WAITING_CONFIRMATION) {
                quest.setStatus(QuestStatus.OPEN);
                questRepository.save(quest);
            } else {
                throw ServiceErrors.badRequest("Approved applications can only be deleted while the quest is assigned or waiting confirmation");
            }
        }

        questApplicationRepository.delete(application);
    }

    @Transactional
    public QuestApplicationResponseDTO updateMyApplication(Long questId, QuestApplicationRequestDTO dto, AppUser currentUser) {
        return toApplicantResponse(updateMyApplicationUseCase.execute(questId, dto, currentUser));
    }

    @Transactional
    public QuestApplicationResponseDTO withdrawMyApplication(Long questId, AppUser currentUser) {
        return toApplicantResponse(withdrawMyApplicationUseCase.execute(questId, currentUser));
    }

    @Transactional
    public QuestApplicationResponseDTO approveApplication(Long questId, Long applicationId, AppUser currentUser) {
        return toApplicantResponse(approveApplicationUseCase.execute(questId, applicationId, currentUser));
    }

    @Transactional
    public QuestApplicationResponseDTO declineApplication(Long questId, Long applicationId, AppUser currentUser) {
        return toApplicantResponse(declineApplicationUseCase.execute(questId, applicationId, currentUser));
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

    private boolean matchesAdminQuery(QuestApplicationResponseDTO application, String normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            return true;
        }

        return containsNormalized(application.getQuestTitle(), normalizedQuery)
                || containsNormalized(application.getQuestCreatorUsername(), normalizedQuery)
                || containsNormalized(application.getApplicantUsername(), normalizedQuery)
                || containsNormalized(application.getQuestStatus().name(), normalizedQuery)
                || containsNormalized(application.getStatus().name(), normalizedQuery)
                || containsNormalized(application.getMessage(), normalizedQuery);
    }

    private void applyAdminStatusUpdate(QuestApplication application, QuestApplicationStatus targetStatus, AppUser currentUser) {
        Quest quest = application.getQuest();
        if (targetStatus == QuestApplicationStatus.APPROVED) {
            if (application.getStatus() != QuestApplicationStatus.PENDING) {
                throw ServiceErrors.badRequest("Only pending applications can be approved");
            }

            approveApplication(quest.getId(), application.getId(), currentUser);
            application.setStatus(QuestApplicationStatus.APPROVED);
            return;
        }

        if (targetStatus == QuestApplicationStatus.DECLINED) {
            if (application.getStatus() != QuestApplicationStatus.PENDING) {
                throw ServiceErrors.badRequest("Only pending applications can be declined");
            }

            declineApplication(quest.getId(), application.getId(), currentUser);
            application.setStatus(QuestApplicationStatus.DECLINED);
            return;
        }

        if (targetStatus == QuestApplicationStatus.WITHDRAWN) {
            if (application.getStatus() != QuestApplicationStatus.PENDING) {
                throw ServiceErrors.badRequest("Only pending applications can be withdrawn");
            }

            application.setStatus(QuestApplicationStatus.WITHDRAWN);
            return;
        }

        throw ServiceErrors.badRequest("Applications cannot be moved back to pending");
    }

    private boolean containsNormalized(String value, String normalizedQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }
}
