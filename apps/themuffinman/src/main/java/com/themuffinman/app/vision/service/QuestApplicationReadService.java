package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestApplication;
import com.themuffinman.app.vision.model.QuestApplicationStatus;
import com.themuffinman.app.vision.model.QuestStatus;
import com.themuffinman.app.vision.repository.QuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestApplicationReadService {

    private final QuestApplicationRepository questApplicationRepository;
    private final QuestApplicationViewAssembler applicationViewAssembler;
    private final QuestApplicationWorkflowSupport workflowSupport;

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
