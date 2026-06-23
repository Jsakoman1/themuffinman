package com.sidequest.sidequest.service;

import com.sidequest.sidequest.dto.QuestRequestDTO;
import com.sidequest.sidequest.dto.QuestAllowedAction;
import com.sidequest.sidequest.dto.QuestApplicationDetailResponseDTO;
import com.sidequest.sidequest.dto.QuestApplicationDetailSectionsDTO;
import com.sidequest.sidequest.dto.QuestApplicationResponseDTO;
import com.sidequest.sidequest.dto.QuestDetailResponseDTO;
import com.sidequest.sidequest.dto.QuestDetailSectionsDTO;
import com.sidequest.sidequest.dto.QuestListResponseDTO;
import com.sidequest.sidequest.dto.QuestListPreset;
import com.sidequest.sidequest.dto.QuestResponseDTO;
import com.sidequest.sidequest.dto.QuestViewerRelation;
import com.sidequest.sidequest.mapper.QuestMgr;
import com.sidequest.sidequest.mapper.QuestApplicationMgr;
import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.model.AppUserRole;
import com.sidequest.sidequest.model.QuestAudience;
import com.sidequest.sidequest.model.Quest;
import com.sidequest.sidequest.model.QuestApplication;
import com.sidequest.sidequest.model.QuestStatus;
import com.sidequest.sidequest.model.QuestApplicationStatus;
import com.sidequest.sidequest.model.QuestNewsType;
import com.sidequest.sidequest.model.CircleGroup;
import com.sidequest.sidequest.repository.AppUserRepository;
import com.sidequest.sidequest.repository.QuestApplicationRepository;
import com.sidequest.sidequest.repository.QuestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestService {

    private final QuestRepository questRepository;
    private final AppUserRepository appUserRepository;
    private final QuestApplicationRepository questApplicationRepository;
    private final QuestApplicationMgr questApplicationMgr;
    private final QuestApplicationService questApplicationService;
    private final QuestNewsService questNewsService;
    private final QuestVisibilityService questVisibilityService;
    private final QuestMgr questMgr;

    public Quest createQuest(QuestRequestDTO dto, AppUser currentUser) {
        validateQuestBasics(dto);
        validateQuestCreationTermInput(dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());
        validateAssigneeTarget(dto.getAssigneeTarget());
        validateQuestImages(dto.getImages());
        Quest quest = questMgr.toEntity(dto, resolveQuestCreator(dto, currentUser));
        if (quest.getAudience() == null) {
            quest.setAudience(QuestAudience.CIRCLES);
        }
        applyQuestVisibilityCircles(quest, dto.getAudience(), dto.getSelectedCircleIds(), quest.getCreator());
        quest.setAssigneeTarget(normalizeAssigneeTarget(dto.getAssigneeTarget()));
        applyConfirmedQuestTermFields(quest, dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());
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
                    .filter(quest -> currentUser == null || !isQuestOwner(quest, currentUser))
                    .toList();
            case MY_VISIBLE -> getAllQuests(currentUser).stream()
                    .filter(quest -> isQuestOwner(quest, currentUser))
                    .filter(quest -> quest.getStatus().isVisibleOwnerWork())
                    .toList();
            case MY_ACTIVE -> getAllQuests(currentUser).stream()
                    .filter(quest -> isQuestOwner(quest, currentUser))
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
        QuestDetailSectionsDTO sections = QuestDetailSectionsDTO.builder()
                .myApplication(viewerApplication == null ? null : questApplicationMgr.toDto(viewerApplication))
                .applicationsView(questResponse.isCanViewApplications()
                        ? questApplicationService.getApplicationsViewForQuest(quest.getId(), currentUser, false)
                        : null)
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
        notifyQuestDeleted(quest, currentUser);
        questApplicationRepository.deleteByQuestId(id);
        questRepository.deleteById(id);
    }

    @Transactional
    public Quest updateQuest(Long id, QuestRequestDTO dto, AppUser currentUser) {
        Quest quest = requireQuestForOwnerActions(id, currentUser);
        applyQuestUpdates(quest, dto, currentUser);
        return questRepository.save(quest);
    }

    @Transactional
    public Quest startQuest(Long id, AppUser currentUser) {
        Quest quest = requireQuestForExecutionActions(id, currentUser);
        requireQuestStatus(quest, QuestStatus.ASSIGNED, "Quest can only be started after an application is approved");
        quest.setStatus(QuestStatus.IN_PROGRESS);
        Quest savedQuest = questRepository.save(quest);
        notifyApprovedApplicant(savedQuest, currentUser, QuestNewsType.QUEST_STARTED, "Quest started", "The quest \"" + savedQuest.getTitle() + "\" has started.");
        return savedQuest;
    }

    @Transactional
    public Quest completeQuest(Long id, AppUser currentUser) {
        Quest quest = requireQuestForExecutionActions(id, currentUser);
        requireQuestStatus(quest, QuestStatus.IN_PROGRESS, "Quest can only be completed while it is in progress");
        quest.setStatus(QuestStatus.COMPLETED);
        Quest savedQuest = questRepository.save(quest);
        notifyApprovedApplicant(savedQuest, currentUser, QuestNewsType.QUEST_COMPLETED, "Quest completed", "The quest \"" + savedQuest.getTitle() + "\" has been completed.");
        return savedQuest;
    }

    @Transactional
    public Quest confirmQuestTermChange(Long id, AppUser currentUser) {
        Quest quest = requireQuest(id);
        validateQuestTermDecisionAuthority(quest, currentUser);
        applyConfirmedQuestTermChange(quest);
        Quest savedQuest = questRepository.save(quest);
        notifyApprovedApplicant(savedQuest, currentUser, QuestNewsType.QUEST_TERM_CONFIRMED, "Quest time confirmed", "The new time for \"" + savedQuest.getTitle() + "\" was confirmed.");
        notifyQuestCreator(savedQuest, currentUser, QuestNewsType.QUEST_TERM_CONFIRMED, "Quest time confirmed", "The new time for \"" + savedQuest.getTitle() + "\" was confirmed.");
        return savedQuest;
    }

    @Transactional
    public Quest rejectQuestTermChange(Long id, AppUser currentUser) {
        Quest quest = requireQuest(id);
        validateQuestTermDecisionAuthority(quest, currentUser);
        rejectPendingQuestTermChange(quest);
        Quest savedQuest = questRepository.save(quest);
        notifyApprovedApplicant(savedQuest, currentUser, QuestNewsType.QUEST_TERM_REJECTED, "Quest time rejected", "The proposed time change for \"" + savedQuest.getTitle() + "\" was rejected.");
        notifyQuestCreator(savedQuest, currentUser, QuestNewsType.QUEST_TERM_REJECTED, "Quest time rejected", "The proposed time change for \"" + savedQuest.getTitle() + "\" was rejected.");
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
        String normalizedQuery = SearchQueryNormalizer.normalize(query).toLowerCase(Locale.ROOT);
        int safeSize = size == null || size < 1 ? 12 : size;
        int safePage = page == null || page < 0 ? 0 : page;

        List<Quest> quests = sourceQuests.stream()
                .filter(quest -> audience == null || quest.getAudience() == audience)
                .filter(quest -> matchesDateRange(quest, dateFrom, dateTo))
                .filter(quest -> withImages == null || !withImages || (quest.getImages() != null && !quest.getImages().isEmpty()))
                .filter(quest -> scheduledOnly == null || !scheduledOnly || quest.getScheduledAt() != null)
                .filter(quest -> matchesQuery(quest, normalizedQuery))
                .sorted(sortQuests(sort))
                .toList();

        int totalItems = quests.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / safeSize));
        int start = Math.min(safePage * safeSize, totalItems);
        int end = Math.min(start + safeSize, totalItems);

        return QuestListResponseDTO.builder()
                .items(toResponses(quests.subList(start, end), currentUser))
                .page(safePage)
                .size(safeSize)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .build();
    }

    private QuestResponseDTO toResponse(Quest quest, AppUser currentUser, Map<Long, QuestApplication> applicationsByQuestId) {
        QuestResponseDTO dto = questMgr.toDto(quest);
        QuestApplication viewerApplication = currentUser == null ? null : applicationsByQuestId.get(quest.getId());
        QuestViewerRelation viewerRelation = resolveViewerRelation(quest, currentUser, viewerApplication);
        List<QuestAllowedAction> allowedActions = resolveAllowedActions(quest, currentUser, viewerApplication);
        boolean canViewApplications = allowedActions.contains(QuestAllowedAction.VIEW_APPLICATIONS);

        return questMgr.withViewerContext(
                dto,
                viewerRelation,
                allowedActions,
                viewerApplication != null,
                viewerApplication == null ? null : viewerApplication.getId(),
                canViewApplications
        );
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

    private QuestViewerRelation resolveViewerRelation(Quest quest, AppUser currentUser, QuestApplication viewerApplication) {
        if (currentUser == null) {
            return QuestViewerRelation.VIEWER;
        }

        if (isQuestOwner(quest, currentUser)) {
            return QuestViewerRelation.OWNER;
        }

        if (isAdmin(currentUser)) {
            return QuestViewerRelation.ADMIN;
        }

        if (viewerApplication != null && viewerApplication.getStatus() == QuestApplicationStatus.APPROVED) {
            return QuestViewerRelation.APPROVED_APPLICANT;
        }

        if (viewerApplication != null) {
            return QuestViewerRelation.APPLICANT;
        }

        return QuestViewerRelation.VIEWER;
    }

    private List<QuestAllowedAction> resolveAllowedActions(Quest quest, AppUser currentUser, QuestApplication viewerApplication) {
        List<QuestAllowedAction> allowedActions = new ArrayList<>();
        boolean owner = isQuestOwner(quest, currentUser);
        boolean admin = isAdmin(currentUser);
        boolean approvedApplicant = viewerApplication != null && viewerApplication.getStatus() == QuestApplicationStatus.APPROVED;
        boolean hasApplied = viewerApplication != null;

        if (owner || admin) {
            allowedActions.add(QuestAllowedAction.EDIT);
            allowedActions.add(QuestAllowedAction.VIEW_APPLICATIONS);
            allowedActions.add(QuestAllowedAction.DELETE);
        }

        if (quest.getStatus() == QuestStatus.OPEN && !owner && !admin && !hasApplied) {
            allowedActions.add(QuestAllowedAction.APPLY);
        }

        if ((owner || admin || approvedApplicant) && quest.getStatus() == QuestStatus.ASSIGNED) {
            allowedActions.add(QuestAllowedAction.START);
        }

        if ((owner || admin || approvedApplicant) && quest.getStatus() == QuestStatus.IN_PROGRESS) {
            allowedActions.add(QuestAllowedAction.COMPLETE);
        }

        if ((admin || approvedApplicant) && quest.getStatus() == QuestStatus.WAITING_CONFIRMATION) {
            allowedActions.add(QuestAllowedAction.CONFIRM_TERM_CHANGE);
            allowedActions.add(QuestAllowedAction.REJECT_TERM_CHANGE);
        }

        return allowedActions;
    }

    private Quest requireQuestForOwnerActions(Long id, AppUser currentUser) {
        Quest quest = requireQuest(id);
        validateQuestOwnerOrAdmin(quest, currentUser);
        return quest;
    }

    private Quest requireQuestForExecutionActions(Long id, AppUser currentUser) {
        Quest quest = requireQuest(id);
        validateQuestExecutionAuthority(quest, currentUser);
        return quest;
    }

    private void applyQuestUpdates(Quest quest, QuestRequestDTO dto, AppUser currentUser) {
        validateQuestBasics(dto);
        quest.setTitle(normalizeQuestText(dto.getTitle()));
        quest.setDescription(normalizeQuestText(dto.getDescription()));
        quest.setAwardAmount(dto.getAwardAmount());
        validateAssigneeTarget(dto.getAssigneeTarget());
        if (dto.getAssigneeTarget() != null) {
            quest.setAssigneeTarget(normalizeAssigneeTarget(dto.getAssigneeTarget()));
        }
        if (dto.getImages() != null) {
            validateQuestImages(dto.getImages());
            quest.setImages(new ArrayList<>(dto.getImages()));
        }
        if (dto.getAudience() != null) {
            quest.setAudience(dto.getAudience());
        }
        applyQuestVisibilityCircles(quest, quest.getAudience(), dto.getSelectedCircleIds(), quest.getCreator());

        if (!isAdmin(currentUser)) {
            applyQuestTermUpdateForOwner(quest, dto, currentUser);
            return;
        }

        if (dto.getCreatorId() != null) {
            quest.setCreator(requireAppUser(dto.getCreatorId()));
            applyQuestVisibilityCircles(quest, quest.getAudience(), dto.getSelectedCircleIds(), quest.getCreator());
        }

        if (dto.getStatus() != null) {
            applyAdminQuestStatusChange(quest, dto.getStatus(), currentUser);
        }

        if (dto.getScheduledAt() != null || dto.getEndsAt() != null || dto.getTermFixed() != null) {
            validateTermInput(dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());
            applyConfirmedQuestTermFields(quest, dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());

            if (quest.getStatus() == QuestStatus.WAITING_CONFIRMATION
                    && (dto.getStatus() == null || dto.getStatus() == QuestStatus.WAITING_CONFIRMATION)) {
                restoreQuestStatusAfterTermDecision(quest);
            }

            clearPendingQuestTermChange(quest);
        }
    }

    private void applyQuestTermUpdateForOwner(Quest quest, QuestRequestDTO dto, AppUser currentUser) {
        if (dto.getScheduledAt() == null && dto.getEndsAt() == null && dto.getTermFixed() == null) {
            return;
        }

        validateTermInput(dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());

        if (quest.getStatus() == QuestStatus.OPEN || quest.getStatus() == QuestStatus.CANCELLED) {
            applyConfirmedQuestTermFields(quest, dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());
            clearPendingQuestTermChange(quest);
            return;
        }

        if (quest.getStatus() == QuestStatus.WAITING_CONFIRMATION) {
            applyPendingQuestTermChange(quest, dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());
            notifyApprovedApplicant(quest, currentUser, QuestNewsType.QUEST_TERM_CONFIRMATION_REQUESTED, "Term change updated", "The pending time for \"" + quest.getTitle() + "\" was updated.");
            return;
        }

        if (quest.getStatus() == QuestStatus.ASSIGNED || quest.getStatus() == QuestStatus.IN_PROGRESS) {
            queueQuestTermChange(quest, dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());
            notifyApprovedApplicant(quest, currentUser, QuestNewsType.QUEST_TERM_CONFIRMATION_REQUESTED, "Term confirmation needed", "The owner requested a new time for \"" + quest.getTitle() + "\".");
            return;
        }

        throw ServiceErrors.badRequest("Term can only be changed on an active quest");
    }

    private AppUser requireAppUser(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> ServiceErrors.notFound("Creator not found with id " + userId));
    }

    private void validateQuestBasics(QuestRequestDTO dto) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Quest request is required");
        }

        validateQuestTitle(dto.getTitle());
        validateQuestDescription(dto.getDescription());
        validateQuestAwardAmount(dto.getAwardAmount());
    }

    private void validateQuestCreationTermInput(Instant scheduledAt, Instant endsAt, Boolean termFixed) {
        if (termFixed == null) {
            throw ServiceErrors.badRequest("Term fixed flag is required");
        }

        if (Boolean.TRUE.equals(termFixed) && scheduledAt == null) {
            throw ServiceErrors.badRequest("Scheduled time is required when the term is fixed");
        }

        validateTermRange(scheduledAt, endsAt);
    }

    private void validateQuestImages(List<String> images) {
        if (images == null) {
          return;
        }

        if (images.size() > 10) {
            throw ServiceErrors.badRequest("A quest can have at most 10 images");
        }

        for (String image : images) {
            if (image == null || image.isBlank()) {
                throw ServiceErrors.badRequest("Quest images must not be empty");
            }
            if (!image.startsWith("data:image/")) {
                throw ServiceErrors.badRequest("Quest images must be image data URLs");
            }
            if (image.length() > 12_000) {
                throw ServiceErrors.badRequest("Quest images must be 12000 characters or less");
            }
        }
    }

    private void validateQuestDescription(String description) {
        if (!RichTextInputValidator.hasContent(description)) {
            throw ServiceErrors.badRequest("Quest description is required");
        }
    }

    private void validateQuestTitle(String title) {
        String normalizedTitle = normalizeQuestText(title);
        if (normalizedTitle == null || normalizedTitle.isBlank()) {
            throw ServiceErrors.badRequest("Quest title is required");
        }

        if (normalizedTitle.length() > 255) {
            throw ServiceErrors.badRequest("Quest title must be 255 characters or less");
        }
    }

    private void validateQuestAwardAmount(BigDecimal awardAmount) {
        if (awardAmount == null) {
            throw ServiceErrors.badRequest("Award amount is required");
        }

        if (awardAmount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            throw ServiceErrors.badRequest("Award amount must be at least 0.01");
        }
    }

    private void validateAssigneeTarget(Integer assigneeTarget) {
        if (assigneeTarget != null && assigneeTarget < 1) {
            throw ServiceErrors.badRequest("Assignee target must be at least 1 when provided");
        }
    }

    private Integer normalizeAssigneeTarget(Integer assigneeTarget) {
        return assigneeTarget == null ? 1 : assigneeTarget;
    }

    private void applyQuestVisibilityCircles(Quest quest, QuestAudience audience, List<Long> selectedCircleIds, AppUser owner) {
        if (audience != QuestAudience.CIRCLES) {
            quest.getVisibleToCircles().clear();
            return;
        }

        if (selectedCircleIds == null) {
            return;
        }

        List<CircleGroup> selectedCircles = questVisibilityService.getVisibleCircles(owner, selectedCircleIds);
        if (selectedCircleIds.size() != selectedCircles.size()) {
            throw ServiceErrors.badRequest("One or more selected circles are invalid");
        }

        quest.getVisibleToCircles().clear();
        quest.getVisibleToCircles().addAll(selectedCircles);
    }

    private void validateTermInput(Instant scheduledAt, Instant endsAt, Boolean termFixed) {
        if ((scheduledAt != null || endsAt != null) && termFixed == null) {
            throw ServiceErrors.badRequest("Term fixed flag is required when providing a time");
        }

        if (Boolean.TRUE.equals(termFixed) && scheduledAt == null) {
            throw ServiceErrors.badRequest("Scheduled time is required when the term is fixed");
        }

        if (endsAt != null && scheduledAt == null) {
            throw ServiceErrors.badRequest("Start time is required when providing an end time");
        }

        validateTermRange(scheduledAt, endsAt);
    }

    private void validateTermRange(Instant scheduledAt, Instant endsAt) {
        if (scheduledAt != null && endsAt != null && !endsAt.isAfter(scheduledAt)) {
            throw ServiceErrors.badRequest("End time must be after the start time");
        }
    }

    private String normalizeQuestText(String value) {
        if (value == null) {
            return null;
        }

        return value.trim();
    }

    private void applyConfirmedQuestTermFields(Quest quest, Instant scheduledAt, Instant endsAt, Boolean termFixed) {
        quest.setScheduledAt(scheduledAt);
        quest.setEndsAt(endsAt);
        quest.setTermFixed(Boolean.TRUE.equals(termFixed));
    }

    private void applyPendingQuestTermChange(Quest quest, Instant scheduledAt, Instant endsAt, Boolean termFixed) {
        quest.setPendingScheduledAt(scheduledAt);
        quest.setPendingEndsAt(endsAt);
        quest.setPendingTermFixed(termFixed);
    }

    private void applyAdminQuestStatusChange(Quest quest, QuestStatus newStatus, AppUser actor) {
        QuestStatus previousStatus = quest.getStatus();
        quest.setStatus(newStatus);

        if (newStatus != QuestStatus.WAITING_CONFIRMATION) {
            clearPendingQuestTermChange(quest);
        }

        if (previousStatus != QuestStatus.OPEN && newStatus == QuestStatus.OPEN) {
            quest.setReopenedAt(Instant.now());
            reopenQuestApplications(quest);
            notifyQuestApplicants(quest, actor, QuestNewsType.QUEST_REOPENED, "Quest reopened", "The quest \"" + quest.getTitle() + "\" was reopened.");
        }
    }

    private void queueQuestTermChange(Quest quest, Instant scheduledAt, Instant endsAt, Boolean termFixed) {
        applyPendingQuestTermChange(quest, scheduledAt, endsAt, termFixed);
        quest.setTermChangePreviousStatus(quest.getStatus());
        quest.setStatus(QuestStatus.WAITING_CONFIRMATION);
    }

    private void clearPendingQuestTermChange(Quest quest) {
        quest.setPendingScheduledAt(null);
        quest.setPendingEndsAt(null);
        quest.setPendingTermFixed(null);
        quest.setTermChangePreviousStatus(null);
    }

    private void reopenQuestApplications(Quest quest) {
        List<QuestApplication> reopenedApplications = new ArrayList<>();
        List<QuestApplication> applications = questApplicationRepository.findByQuestId(quest.getId());
        if (applications.isEmpty()) {
            return;
        }

        for (QuestApplication application : applications) {
            if (application.getStatus() == QuestApplicationStatus.WITHDRAWN) {
                continue;
            }

            application.setStatus(QuestApplicationStatus.PENDING);
            reopenedApplications.add(application);
        }

        if (!reopenedApplications.isEmpty()) {
            questApplicationRepository.saveAll(reopenedApplications);
        }
    }

    private void notifyQuestDeleted(Quest quest, AppUser actor) {
        List<QuestApplication> applications = questApplicationRepository.findByQuestId(quest.getId());
        if (applications.isEmpty()) {
            return;
        }

        for (QuestApplication application : applications) {
            if (application.getStatus() == QuestApplicationStatus.WITHDRAWN) {
                continue;
            }

            questNewsService.notifyQuestDeleted(quest, actor, application.getApplicant());
        }
    }

    private void notifyApprovedApplicant(Quest quest, AppUser actor, QuestNewsType type, String title, String message) {
        List<QuestApplication> approvedApplications = questApplicationRepository.findByQuestIdAndStatus(quest.getId(), QuestApplicationStatus.APPROVED);
        if (approvedApplications.isEmpty()) {
            return;
        }

        for (QuestApplication application : approvedApplications) {
            questNewsService.notifyQuestEvent(application.getApplicant(), actor, quest, null, type, title, message);
        }
    }

    private void notifyQuestCreator(Quest quest, AppUser actor, QuestNewsType type, String title, String message) {
        questNewsService.notifyQuestEvent(quest.getCreator(), actor, quest, null, type, title, message);
    }

    private void notifyQuestApplicants(Quest quest, AppUser actor, QuestNewsType type, String title, String message) {
        List<QuestApplication> applications = questApplicationRepository.findByQuestId(quest.getId());
        if (applications.isEmpty()) {
            return;
        }

        for (QuestApplication application : applications) {
            if (application.getStatus() == QuestApplicationStatus.WITHDRAWN) {
                continue;
            }

            questNewsService.notifyQuestEvent(application.getApplicant(), actor, quest, null, type, title, message);
        }
    }

    private void applyConfirmedQuestTermChange(Quest quest) {
        if (quest.getStatus() != QuestStatus.WAITING_CONFIRMATION) {
            throw ServiceErrors.badRequest("Quest term change is not waiting for confirmation");
        }

        if (quest.getPendingTermFixed() == null && quest.getPendingScheduledAt() == null && quest.getPendingEndsAt() == null) {
            throw ServiceErrors.badRequest("No pending term change to confirm");
        }

        quest.setScheduledAt(quest.getPendingScheduledAt());
        quest.setEndsAt(quest.getPendingEndsAt());
        quest.setTermFixed(Boolean.TRUE.equals(quest.getPendingTermFixed()));
        restoreQuestStatusAfterTermDecision(quest);
        clearPendingQuestTermChange(quest);
    }

    private void rejectPendingQuestTermChange(Quest quest) {
        if (quest.getStatus() != QuestStatus.WAITING_CONFIRMATION) {
            throw ServiceErrors.badRequest("Quest term change is not waiting for confirmation");
        }
        restoreQuestStatusAfterTermDecision(quest);
        clearPendingQuestTermChange(quest);
    }

    private void restoreQuestStatusAfterTermDecision(Quest quest) {
        if (quest.getTermChangePreviousStatus() == null) {
            throw ServiceErrors.badRequest("Missing previous quest status for term change");
        }

        quest.setStatus(quest.getTermChangePreviousStatus());
    }

    private void validateQuestTermDecisionAuthority(Quest quest, AppUser currentUser) {
        if (isAdmin(currentUser)) {
            return;
        }

        if (questApplicationRepository.findByQuestIdAndApplicantIdAndStatus(
                quest.getId(),
                currentUser.getId(),
                QuestApplicationStatus.APPROVED
        ).isEmpty()) {
            throw ServiceErrors.forbidden("You are not allowed to confirm this quest term change");
        }
    }

    private void validateQuestOwner(Quest quest, AppUser currentUser) {
        if (!quest.getCreator().getId().equals(currentUser.getId())) {
            throw ServiceErrors.forbidden("You are not allowed to modify this quest");
        }
    }

    private void validateQuestOwnerOrAdmin(Quest quest, AppUser currentUser) {
        if (isAdmin(currentUser)) {
            return;
        }

        validateQuestOwner(quest, currentUser);
    }

    private void validateQuestExecutionAuthority(Quest quest, AppUser currentUser) {
        if (isAdmin(currentUser)) {
            return;
        }

        if (quest.getCreator().getId().equals(currentUser.getId())) {
            return;
        }

        if (questApplicationRepository.findByQuestIdAndApplicantIdAndStatus(
                quest.getId(),
                currentUser.getId(),
                QuestApplicationStatus.APPROVED
        ).isPresent()) {
            return;
        }

        throw ServiceErrors.forbidden("You are not allowed to manage this quest");
    }

    private void requireQuestStatus(Quest quest, QuestStatus requiredStatus, String message) {
        if (quest.getStatus() != requiredStatus) {
            throw ServiceErrors.badRequest(message);
        }
    }

    private boolean matchesQuery(Quest quest, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        return safeLower(quest.getTitle()).contains(query)
                || safeLower(quest.getDescription()).contains(query)
                || safeLower(quest.getCreator().getUsername()).contains(query);
    }

    private boolean matchesDateRange(Quest quest, LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null && dateTo == null) {
            return true;
        }

        if (quest.getScheduledAt() == null) {
            return false;
        }

        LocalDate questDate = quest.getScheduledAt().atZone(ZoneOffset.UTC).toLocalDate();
        if (dateFrom != null && questDate.isBefore(dateFrom)) {
            return false;
        }

        return dateTo == null || !questDate.isAfter(dateTo);
    }

    private Comparator<Quest> sortQuests(String sort) {
        String normalizedSort = sort == null ? "recommended" : sort.trim().toLowerCase(Locale.ROOT);

        return switch (normalizedSort) {
            case "newest" -> Comparator
                    .comparing(Quest::getScheduledAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Quest::getId, Comparator.reverseOrder());
            case "highest" -> Comparator
                    .comparing(Quest::getAwardAmount, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Quest::getScheduledAt, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Quest::getId, Comparator.reverseOrder());
            default -> Comparator
                    .comparing(Quest::getAwardAmount, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Quest::getScheduledAt, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Quest::getId, Comparator.reverseOrder());
        };
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private AppUser resolveQuestCreator(QuestRequestDTO dto, AppUser currentUser) {
        if (isAdmin(currentUser) && dto.getCreatorId() != null) {
            return requireAppUser(dto.getCreatorId());
        }

        return currentUser;
    }

    private void validateApplicationDetailAccess(QuestApplication application, Quest quest, AppUser currentUser) {
        if (currentUser == null) {
            throw ServiceErrors.forbidden("You are not allowed to view this application");
        }

        if (isAdmin(currentUser) || isQuestOwner(quest, currentUser)) {
            return;
        }

        if (application.getApplicant() != null && application.getApplicant().getId().equals(currentUser.getId())) {
            return;
        }

        throw ServiceErrors.forbidden("You are not allowed to view this application");
    }

    private boolean isQuestOwner(Quest quest, AppUser currentUser) {
        return quest != null
                && currentUser != null
                && quest.getCreator() != null
                && quest.getCreator().getId().equals(currentUser.getId());
    }

    private boolean isAdmin(AppUser user) {
        return user != null && user.getRole() == AppUserRole.ADMIN;
    }

}
