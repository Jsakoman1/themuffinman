package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.common.pagination.PageResponseFactory;
import com.themuffinman.app.common.pagination.PageWindow;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailRailItemDTO;
import com.themuffinman.app.workmarket.dto.QuestListPresetDTO;
import com.themuffinman.app.workmarket.dto.QuestListResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestListPresentationDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
@Service("workmarketQuestReadService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkmarketQuestReadService {

    private final WorkmarketQuestRepository questRepository;
    private final WorkmarketQuestApplicationReadService questApplicationReadService;
    private final WorkmarketQuestVisibilityService questVisibilityService;
    private final WorkmarketQuestQueryService questQueryService;
    private final WorkmarketQuestExecutionPrimitiveService questExecutionPrimitiveService;
    private final WorkmarketQuestSearchScopeService questSearchScopeService;
    private final WorkmarketQuestListPresetResolver questListPresetResolver;
    private final WorkmarketQuestViewerApplicationMapFactory questViewerApplicationMapFactory;
    private final WorkmarketQuestResponseFactory questResponseFactory;
    private final WorkmarketQuestDetailSectionsFactory questDetailSectionsFactory;

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
        List<Quest> scopedQuests = questSearchScopeService.loadQuestSearchScope(currentUser, radiusKm);
        return buildQuestListResponse(
                scopedQuests.stream()
                        .filter(quest -> questVisibilityService.canViewQuest(currentUser, quest))
                        .filter(quest -> !Boolean.TRUE.equals(excludeMine) || currentUser == null || !quest.getCreator().getId().equals(currentUser.getId()))
                        .filter(quest -> status == null || status == quest.getStatus())
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
        List<Quest> scopedQuests = questSearchScopeService.loadQuestSearchScope(currentUser, radiusKm);
        List<Quest> baseQuests = questListPresetResolver.resolve(preset, scopedQuests, currentUser);

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
        Quest quest = questExecutionPrimitiveService.resolveTarget(id);
        if (!questVisibilityService.canViewQuest(currentUser, quest)) {
            throw ServiceErrors.notFound("Quest not found with id " + id);
        }

        return quest;
    }

    public QuestResponseDTO getQuestResponseById(Long id, AppUser currentUser) {
        return toResponse(getQuestById(id, currentUser), currentUser);
    }

    public QuestDetailResponseDTO getQuestDetailResponseById(Long id, AppUser currentUser) {
        Map<Long, QuestApplication> applicationsByQuestId = questViewerApplicationMapFactory.getCurrentUserApplicationsByQuestId(currentUser);
        Quest quest = getQuestById(id, currentUser);
        QuestResponseDTO questResponse = questResponseFactory.toResponse(quest, currentUser, applicationsByQuestId);
        QuestApplication viewerApplication = currentUser == null ? null : applicationsByQuestId.get(quest.getId());
        QuestApplicationResponseDTO myApplication = questApplicationReadService.toViewerResponse(viewerApplication, currentUser);
        QuestApplicationsViewDTO applicationsView = questResponse.isCanViewApplications()
                ? questApplicationReadService.getApplicationsViewForQuest(quest.getId(), currentUser, false)
                : (questResponse.isShowApprovedApplicants()
                ? questApplicationReadService.getPublicApprovedApplicationsViewForQuest(quest.getId())
                : null);

        QuestDetailResponseDTO response = QuestDetailResponseDTO.builder()
                .summary(questResponse)
                .sections(questDetailSectionsFactory.buildSections(quest, currentUser, questResponse, myApplication, applicationsView))
                .quest(questResponse)
                .myApplication(myApplication)
                .applicationsView(applicationsView)
                .propertyRail(List.of(
                        QuestDetailRailItemDTO.builder().label("Reward").value(questResponse.getAwardAmount() + " €").build(),
                        QuestDetailRailItemDTO.builder().label("Schedule").value(questResponse.getPresentation().getTimeTypeLabel()).build(),
                        QuestDetailRailItemDTO.builder().label("Location").value(questResponse.getPresentation().getLocationLabel() == null ? "Anywhere" : questResponse.getPresentation().getLocationLabel()).build()
                ))
                .activityRail(List.of(
                        QuestDetailRailItemDTO.builder().label("Current status").value(questResponse.getPresentation().getStatusLabel()).build(),
                        QuestDetailRailItemDTO.builder().label("Applicants").value(questResponse.getApprovedApplicationCount() + " approved · " + questResponse.getRemainingAssigneeSlots() + " slots open").build()
                ))
                .build();
        return response;
    }

    public QuestResponseDTO toResponse(Quest quest, AppUser currentUser) {
        return questResponseFactory.toResponse(
                quest,
                currentUser,
                questViewerApplicationMapFactory.getCurrentUserApplicationsByQuestId(currentUser)
        );
    }

    public List<QuestResponseDTO> toResponses(List<Quest> quests, AppUser currentUser) {
        Map<Long, QuestApplication> applicationsByQuestId = questViewerApplicationMapFactory.getCurrentUserApplicationsByQuestId(currentUser);
        return questResponseFactory.toResponses(quests, currentUser, applicationsByQuestId);
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
                .presentation(QuestListPresentationDTO.builder()
                        .archetype("focus-list")
                        .density("scan")
                        .primaryActionLabel("Open")
                        .visibleFields(List.of("title", "awardAmount", "scheduledAt", "locationLabel", "status"))
                        .build())
                .build());
    }

}
