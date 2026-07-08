package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.pagination.PageResponseFactory;
import com.themuffinman.app.common.pagination.PaginationSupport;
import com.themuffinman.app.common.search.SearchTextSupport;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestApplicationListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service("workmarketQuestApplicationAdminQueryService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkmarketQuestApplicationAdminQueryService {

    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketQuestApplicationViewAssembler applicationViewAssembler;
    private final WorkmarketQuestApplicationWorkflowSupport workflowSupport;

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

        String normalizedQuery = SearchTextSupport.normalizeQuery(query);
        int safeSize = PaginationSupport.safeSize(size, 20, Integer.MAX_VALUE);
        int safePage = PaginationSupport.safePage(page);

        List<QuestApplicationResponseDTO> filtered = questApplicationRepository.findForAdminApplicationList().stream()
                .map(this::toManagementResponse)
                .filter(application -> status == null || application.getStatus().name().equals(status.name()))
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

    private QuestApplicationResponseDTO toManagementResponse(QuestApplication application) {
        return applicationViewAssembler.toManagementResponse(application);
    }

    private boolean matchesAdminQuery(QuestApplicationResponseDTO application, String normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            return true;
        }

        return SearchTextSupport.containsAnyNormalized(normalizedQuery,
                application.getQuestTitle(),
                application.getQuestCreatorUsername(),
                application.getApplicantUsername(),
                application.getQuestStatus().name(),
                application.getStatus().name(),
                application.getMessage()
        );
    }
}
