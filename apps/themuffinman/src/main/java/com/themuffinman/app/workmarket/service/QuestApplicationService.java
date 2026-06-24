package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.ApplicationAllowedAction;
import com.themuffinman.app.workmarket.dto.QuestApplicationPresentationDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.workmarket.mapper.QuestApplicationMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
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
public class QuestApplicationService {

    private final QuestApplicationRepository questApplicationRepository;
    private final QuestRepository questRepository;
    private final QuestApplicationMgr questApplicationMgr;
    private final QuestNewsService questNewsService;
    private final QuestVisibilityService questVisibilityService;
    private final WorkmarketPresentationHelper presentationHelper;

    @Transactional
    public QuestApplicationResponseDTO applyForQuest(Long questId, QuestApplicationRequestDTO dto, AppUser currentUser) {
        Quest quest = requireVisibleOpenQuest(questId, currentUser);
        validateNotQuestCreator(quest, currentUser);
        validateNoDuplicateApplication(questId, currentUser.getId());
        validateApplicationInput(dto);

        QuestApplication application = questApplicationMgr.toEntity(dto, quest, currentUser);
        QuestApplicationResponseDTO response = saveAndMapApplication(application);
        questNewsService.notifyApplicationCreated(quest, application, currentUser);
        return response;
    }

    public List<QuestApplicationResponseDTO> getApplicationsForQuest(Long questId, AppUser currentUser) {
        validateQuestOwnerOrAdmin(requireQuest(questId), currentUser);

        return questApplicationRepository.findByQuestId(questId)
                .stream()
                .map(application -> withAllowedActions(questApplicationMgr.toDto(application), resolveManagementActions(application)))
                .toList();
    }

    public QuestApplicationsViewDTO getApplicationsViewForQuest(Long questId, AppUser currentUser, boolean showAll) {
        Quest quest = requireQuest(questId);
        validateQuestOwnerOrAdmin(quest, currentUser);

        List<QuestApplicationResponseDTO> sortedApplications = questApplicationRepository.findByQuestId(questId).stream()
                .sorted(Comparator.comparing(QuestApplication::getId).reversed())
                .map(application -> withAllowedActions(questApplicationMgr.toDto(application), resolveManagementActions(application)))
                .toList();

        QuestApplicationResponseDTO featuredApplication = sortedApplications.stream()
                .filter(application -> application.getStatus() == QuestApplicationStatus.APPROVED)
                .findFirst()
                .orElse(null);

        List<QuestApplicationResponseDTO> visibleApplications = resolveVisibleApplications(quest, sortedApplications, featuredApplication, showAll);
        int hiddenApplicationsCount = Math.max(0, sortedApplications.size() - visibleApplications.size() - (featuredApplication == null ? 0 : 1));
        boolean canRevealHiddenApplications = sortedApplications.size() > visibleApplications.size() + (featuredApplication == null ? 0 : 1);

        return QuestApplicationsViewDTO.builder()
                .featuredApplication(featuredApplication)
                .visibleApplications(visibleApplications)
                .hiddenApplicationsCount(hiddenApplicationsCount)
                .selectedApplicationId(resolveSelectedApplicationId(featuredApplication, visibleApplications))
                .canRevealHiddenApplications(canRevealHiddenApplications)
                .showingAllApplications(showAll)
                .revealLabel(resolveRevealLabel(quest, featuredApplication, showAll))
                .build();
    }

    public List<QuestApplicationResponseDTO> getApplicationsForApplicant(AppUser currentUser) {
        return questApplicationRepository.findByApplicantId(currentUser.getId())
                .stream()
                .map(application -> withAllowedActions(questApplicationMgr.toDto(application), resolveApplicantActions(application)))
                .toList();
    }

    public List<QuestApplicationResponseDTO> getAllApplicationsForAdmin(AppUser currentUser) {
        validateAdmin(currentUser);

        return questApplicationRepository.findAllDetailed().stream()
                .map(application -> withAllowedActions(questApplicationMgr.toDto(application), resolveManagementActions(application)))
                .toList();
    }

    public QuestApplicationListResponseDTO searchApplicationsForAdmin(
            AppUser currentUser,
            String query,
            QuestApplicationStatus status,
            Integer page,
            Integer size
    ) {
        validateAdmin(currentUser);

        String normalizedQuery = SearchQueryNormalizer.normalize(query).toLowerCase(Locale.ROOT);
        int safeSize = size == null || size < 1 ? 20 : size;
        int safePage = page == null || page < 0 ? 0 : page;

        List<QuestApplicationResponseDTO> filtered = questApplicationRepository.findAllDetailed().stream()
                .map(application -> withAllowedActions(questApplicationMgr.toDto(application), resolveManagementActions(application)))
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
    public QuestApplicationResponseDTO updateMyApplication(Long questId, QuestApplicationRequestDTO dto, AppUser currentUser) {
        QuestApplication application = requirePendingMyApplication(questId, currentUser);
        validateApplicationInput(dto);
        application.setMessage(dto.getMessage() == null ? null : dto.getMessage().trim());
        application.setProposedPrice(dto.getProposedPrice());
        QuestApplicationResponseDTO response = saveAndMapApplication(application);
        questNewsService.notifyApplicationUpdated(application.getQuest(), application, currentUser);
        return response;
    }

    @Transactional
    public QuestApplicationResponseDTO withdrawMyApplication(Long questId, AppUser currentUser) {
        QuestApplication application = requirePendingMyApplication(questId, currentUser);
        application.setStatus(QuestApplicationStatus.WITHDRAWN);
        QuestApplicationResponseDTO response = saveAndMapApplication(application);
        questNewsService.notifyApplicationWithdrawn(application.getQuest(), application, currentUser);
        return response;
    }

    @Transactional
    public QuestApplicationResponseDTO approveApplication(Long questId, Long applicationId, AppUser currentUser) {
        Quest quest = requireOpenQuest(questId);
        validateQuestOwnerOrAdmin(quest, currentUser);

        QuestApplication application = requirePendingApplication(questId, applicationId);
        application.setStatus(QuestApplicationStatus.APPROVED);
        quest.setStatus(QuestStatus.ASSIGNED);

        List<QuestApplication> declinedApplications = declineOtherPendingApplications(questId, applicationId);
        questRepository.save(quest);
        QuestApplicationResponseDTO response = saveAndMapApplication(application);
        for (QuestApplication declinedApplication : declinedApplications) {
            questNewsService.notifyApplicationDeclined(quest, declinedApplication, currentUser);
        }
        questNewsService.notifyApplicationApproved(quest, application, currentUser);
        return response;
    }

    @Transactional
    public QuestApplicationResponseDTO declineApplication(Long questId, Long applicationId, AppUser currentUser) {
        Quest quest = requireOpenQuest(questId);
        validateQuestOwnerOrAdmin(quest, currentUser);

        QuestApplication application = requirePendingApplication(questId, applicationId);
        application.setStatus(QuestApplicationStatus.DECLINED);
        QuestApplicationResponseDTO response = saveAndMapApplication(application);
        questNewsService.notifyApplicationDeclined(quest, application, currentUser);
        return response;
    }

    private Quest requireQuest(Long questId) {
        return questRepository.findByIdWithCreator(questId)
                .orElseThrow(() -> ServiceErrors.notFound("Quest not found with id " + questId));
    }

    private Quest requireOpenQuest(Long questId) {
        Quest quest = requireQuest(questId);
        validateQuestIsOpen(quest);
        return quest;
    }

    private Quest requireVisibleOpenQuest(Long questId, AppUser currentUser) {
        Quest quest = requireQuest(questId);
        if (!questVisibilityService.canViewQuest(currentUser, quest)) {
            throw ServiceErrors.notFound("Quest not found with id " + questId);
        }

        validateQuestIsOpen(quest);
        return quest;
    }

    private QuestApplication requirePendingApplication(Long questId, Long applicationId) {
        QuestApplication application = questApplicationRepository.findByIdAndQuestId(applicationId, questId)
                .orElseThrow(() -> ServiceErrors.notFound("Quest application not found with id " + applicationId));

        if (application.getStatus() != QuestApplicationStatus.PENDING) {
            throw ServiceErrors.badRequest("Only pending applications can be modified");
        }

        return application;
    }

    private QuestApplication requirePendingMyApplication(Long questId, AppUser currentUser) {
        QuestApplication application = questApplicationRepository.findByQuestIdAndApplicantId(questId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Quest application not found for current user"));

        if (application.getStatus() != QuestApplicationStatus.PENDING) {
            throw ServiceErrors.badRequest("Only pending applications can be modified");
        }

        return application;
    }

    private QuestApplicationResponseDTO saveAndMapApplication(QuestApplication application) {
        QuestApplication saved = questApplicationRepository.save(application);
        return withAllowedActions(questApplicationMgr.toDto(saved), resolveApplicantActions(saved));
    }

    private QuestApplicationResponseDTO withAllowedActions(
            QuestApplicationResponseDTO dto,
            List<ApplicationAllowedAction> allowedActions
    ) {
        if (dto == null) {
            return null;
        }

        dto.setAllowedActions(List.copyOf(allowedActions));
        boolean canApprove = allowedActions.contains(ApplicationAllowedAction.APPROVE);
        boolean canDecline = allowedActions.contains(ApplicationAllowedAction.DECLINE);
        dto.setPresentation(QuestApplicationPresentationDTO.builder()
                .statusLabel(presentationHelper.formatApplicationStatus(dto.getStatus()))
                .statusBadgeClass(presentationHelper.badgeClassForApplicationStatus(dto.getStatus()))
                .statusSurfaceClass(presentationHelper.surfaceClassForApplicationStatus(dto.getStatus()))
                .questStatusLabel(presentationHelper.formatQuestStatus(dto.getQuestStatus()))
                .questStatusBadgeClass(presentationHelper.badgeClassForQuestStatus(dto.getQuestStatus()))
                .questTermLabel(presentationHelper.formatQuestTerm(
                        dto.getQuestScheduledAt(),
                        dto.getQuestEndsAt(),
                        dto.isQuestTermFixed()
                ))
                .questAssigneeTargetVisible(presentationHelper.showAssigneeTarget(dto.getQuestAssigneeTarget()))
                .questAssigneeTargetLabel(presentationHelper.formatAssigneeTarget(dto.getQuestAssigneeTarget()))
                .canEdit(allowedActions.contains(ApplicationAllowedAction.EDIT))
                .canWithdraw(allowedActions.contains(ApplicationAllowedAction.WITHDRAW))
                .autoOpenEditForm(allowedActions.contains(ApplicationAllowedAction.EDIT))
                .canApprove(canApprove)
                .canDecline(canDecline)
                .showManagementActions(canApprove || canDecline)
                .build());
        return dto;
    }

    private List<ApplicationAllowedAction> resolveApplicantActions(QuestApplication application) {
        if (application.getStatus() != QuestApplicationStatus.PENDING) {
            return List.of();
        }

        return List.of(ApplicationAllowedAction.EDIT, ApplicationAllowedAction.WITHDRAW);
    }

    private List<ApplicationAllowedAction> resolveManagementActions(QuestApplication application) {
        if (application.getStatus() != QuestApplicationStatus.PENDING) {
            return List.of();
        }

        return List.of(ApplicationAllowedAction.APPROVE, ApplicationAllowedAction.DECLINE);
    }

    private List<QuestApplicationResponseDTO> resolveVisibleApplications(
            Quest quest,
            List<QuestApplicationResponseDTO> applications,
            QuestApplicationResponseDTO featuredApplication,
            boolean showAll
    ) {
        if (quest.getStatus() == QuestStatus.CANCELLED) {
            return showAll ? applications : List.of();
        }

        if (featuredApplication != null) {
            if (!showAll) {
                return List.of();
            }

            return applications.stream()
                    .filter(application -> !Objects.equals(application.getId(), featuredApplication.getId()))
                    .toList();
        }

        return applications;
    }

    private String resolveRevealLabel(Quest quest, QuestApplicationResponseDTO featuredApplication, boolean showAll) {
        if (quest.getStatus() == QuestStatus.CANCELLED) {
            return showAll ? "Hide all applications" : "Show all applications";
        }

        if (featuredApplication != null) {
            return showAll ? "Hide declined" : "Show declined";
        }

        return "Show applications";
    }

    private Long resolveSelectedApplicationId(
            QuestApplicationResponseDTO featuredApplication,
            List<QuestApplicationResponseDTO> visibleApplications
    ) {
        if (featuredApplication != null) {
            return featuredApplication.getId();
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
                || containsNormalized(application.getApplicantUsername(), normalizedQuery)
                || containsNormalized(application.getQuestStatus().name(), normalizedQuery)
                || containsNormalized(application.getStatus().name(), normalizedQuery)
                || containsNormalized(application.getMessage(), normalizedQuery);
    }

    private boolean containsNormalized(String value, String normalizedQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }

    private List<QuestApplication> declineOtherPendingApplications(Long questId, Long approvedApplicationId) {
        List<QuestApplication> pendingApplications = questApplicationRepository.findByQuestIdAndStatus(questId, QuestApplicationStatus.PENDING);
        if (pendingApplications.isEmpty()) {
            return List.of();
        }

        List<QuestApplication> declinedApplications = new java.util.ArrayList<>();
        for (QuestApplication application : pendingApplications) {
            if (!Objects.equals(application.getId(), approvedApplicationId)) {
                application.setStatus(QuestApplicationStatus.DECLINED);
                declinedApplications.add(application);
            }
        }

        if (!declinedApplications.isEmpty()) {
            questApplicationRepository.saveAll(declinedApplications);
        }

        return declinedApplications;
    }

    private void validateQuestIsOpen(Quest quest) {
        if (quest.getStatus() != QuestStatus.OPEN) {
            throw ServiceErrors.badRequest("Applications are only allowed for open quests");
        }
    }

    private void validateNotQuestCreator(Quest quest, AppUser currentUser) {
        if (quest.getCreator().getId().equals(currentUser.getId())) {
            throw ServiceErrors.badRequest("Quest creator cannot apply to their own quest");
        }
    }

    private void validateNoDuplicateApplication(Long questId, Long applicantId) {
        if (questApplicationRepository.existsByQuestIdAndApplicantId(questId, applicantId)) {
            throw ServiceErrors.conflict("You have already applied for this quest");
        }
    }

    private void validateApplicationInput(QuestApplicationRequestDTO dto) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Quest application request is required");
        }

        if (!RichTextInputValidator.hasContent(dto.getMessage())) {
            throw ServiceErrors.badRequest("Application message is required");
        }

        if (dto.getProposedPrice() == null) {
            throw ServiceErrors.badRequest("Proposed price is required");
        }

        if (dto.getProposedPrice().compareTo(java.math.BigDecimal.valueOf(0.01)) < 0) {
            throw ServiceErrors.badRequest("Proposed price must be at least 0.01");
        }
    }

    private void validateQuestOwnerOrAdmin(Quest quest, AppUser currentUser) {
        if (isAdmin(currentUser)) {
            return;
        }

        if (!quest.getCreator().getId().equals(currentUser.getId())) {
            throw ServiceErrors.forbidden("You are not allowed to view these applications");
        }
    }

    private boolean isAdmin(AppUser user) {
        return user != null && user.getRole() == AppUserRole.ADMIN;
    }

    private void validateAdmin(AppUser currentUser) {
        if (!isAdmin(currentUser)) {
            throw ServiceErrors.forbidden("Admin access is required");
        }
    }
}
