package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.normalization.SearchQueryNormalizer;
import com.themuffinman.app.common.pagination.PageResponseFactory;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.QuestApplicationListResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.model.QuestApplication;
import com.themuffinman.app.vision.model.QuestApplicationStatus;
import com.themuffinman.app.vision.repository.QuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestApplicationAdminQueryService {

    private final QuestApplicationRepository questApplicationRepository;
    private final QuestApplicationViewAssembler applicationViewAssembler;
    private final QuestApplicationWorkflowSupport workflowSupport;

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

    private QuestApplicationResponseDTO toManagementResponse(QuestApplication application) {
        return applicationViewAssembler.toManagementResponse(application);
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

    private boolean containsNormalized(String value, String normalizedQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }
}
