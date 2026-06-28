package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.ApplicationAllowedAction;
import com.themuffinman.app.workmarket.dto.AdminQuestApplicationUpdateRequestDTO;
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
@Transactional(readOnly = true)
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
        validateApplicationInput(dto, quest);

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

        List<QuestApplication> applications = questApplicationRepository.findByQuestId(questId);
        List<QuestApplicationResponseDTO> sortedApplications = applications.stream()
                .sorted(Comparator.comparing(QuestApplication::getId).reversed())
                .map(application -> withAllowedActions(questApplicationMgr.toDto(application), resolveManagementActions(application)))
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
        List<QuestApplicationResponseDTO> approvedApplications = questApplicationRepository.findByQuestIdAndStatus(questId, QuestApplicationStatus.APPROVED).stream()
                .sorted(Comparator.comparing(QuestApplication::getId).reversed())
                .map(application -> withAllowedActions(questApplicationMgr.toDto(application), List.of()))
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
        return questApplicationRepository.findByApplicantId(currentUser.getId())
                .stream()
                .map(this::toApplicantResponse)
                .toList();
    }

    public QuestApplicationResponseDTO toApplicantResponse(QuestApplication application) {
        return withAllowedActions(questApplicationMgr.toDto(application), resolveApplicantActions(application));
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
    public QuestApplicationResponseDTO updateApplicationForAdmin(
            Long applicationId,
            AdminQuestApplicationUpdateRequestDTO dto,
            AppUser currentUser
    ) {
        validateAdmin(currentUser);
        if (dto == null) {
            throw ServiceErrors.badRequest("Application update request is required");
        }

        QuestApplication application = questApplicationRepository.findByIdDetailed(applicationId)
                .orElseThrow(() -> ServiceErrors.notFound("Quest application not found with id " + applicationId));

        if (dto.getMessage() != null) {
            if (!RichTextInputValidator.hasContent(dto.getMessage())) {
                throw ServiceErrors.badRequest("Application message is required");
            }
            application.setMessage(RichTextInputValidator.sanitize(dto.getMessage()));
        }

        if (dto.getProposedPrice() != null) {
            if (isFreeQuest(application.getQuest())) {
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

        return saveAndMapApplication(application);
    }

    @Transactional
    public void deleteApplicationForAdmin(Long applicationId, AppUser currentUser) {
        validateAdmin(currentUser);
        QuestApplication application = questApplicationRepository.findByIdDetailed(applicationId)
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
        QuestApplication application = requirePendingMyApplication(questId, currentUser);
        validateApplicationInput(dto, application.getQuest());
        application.setMessage(RichTextInputValidator.sanitize(dto.getMessage()));
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
        int assigneeTarget = Math.max(quest.getAssigneeTarget() == null ? 1 : quest.getAssigneeTarget(), 1);
        long approvedCount = questApplicationRepository.countByQuestIdAndStatus(questId, QuestApplicationStatus.APPROVED);
        if (approvedCount >= assigneeTarget) {
            throw ServiceErrors.badRequest("This quest already has all worker spots filled");
        }

        application.setStatus(QuestApplicationStatus.APPROVED);
        boolean filledAllSpots = approvedCount + 1 >= assigneeTarget;
        quest.setStatus(filledAllSpots ? QuestStatus.ASSIGNED : QuestStatus.OPEN);

        List<QuestApplication> declinedApplications = filledAllSpots
                ? declineOtherPendingApplications(questId, applicationId)
                : List.of();
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

    private void validateApplicationInput(QuestApplicationRequestDTO dto, Quest quest) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Quest application request is required");
        }

        if (!RichTextInputValidator.hasContent(dto.getMessage())) {
            throw ServiceErrors.badRequest("Application message is required");
        }

        if (isFreeQuest(quest)) {
            if (dto.getProposedPrice() != null) {
                throw ServiceErrors.badRequest("Free quest applications cannot have a proposed price");
            }
            return;
        }

        if (dto.getProposedPrice() == null) {
            throw ServiceErrors.badRequest("Proposed price is required for paid quests");
        }

        if (dto.getProposedPrice().compareTo(java.math.BigDecimal.valueOf(0.01)) < 0) {
            throw ServiceErrors.badRequest("Proposed price must be at least 0.01");
        }
    }

    private boolean isFreeQuest(Quest quest) {
        return quest.getAwardAmount() != null && quest.getAwardAmount().compareTo(java.math.BigDecimal.ZERO) == 0;
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
