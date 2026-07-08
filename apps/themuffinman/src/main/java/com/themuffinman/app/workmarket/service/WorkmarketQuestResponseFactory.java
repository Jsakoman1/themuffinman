package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestMgr;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkmarketQuestResponseFactory {

    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketQuestAccessPolicyService questAccessPolicyService;
    private final WorkmarketQuestMgr questMgr;
    private final WorkmarketQuestPresentationAssembler questPresentationAssembler;

    public QuestResponseDTO toResponse(Quest quest, AppUser currentUser, Map<Long, QuestApplication> applicationsByQuestId) {
        QuestResponseDTO response = questMgr.toDto(quest);
        QuestApplication viewerApplication = currentUser == null ? null : applicationsByQuestId.get(quest.getId());
        var viewerRelation = questAccessPolicyService.resolveViewerRelation(quest, currentUser, viewerApplication);
        List<QuestAllowedActionDTO> allowedActions = questAccessPolicyService.resolveAllowedActions(quest, currentUser, viewerApplication);
        boolean canViewApplications = allowedActions.contains(QuestAllowedActionDTO.VIEW_APPLICATIONS);
        int workerTarget = Math.max(response.getAssigneeTarget() == null ? 1 : response.getAssigneeTarget(), 1);
        int approvedApplicationCount = Math.toIntExact(
                questApplicationRepository.countByQuestIdAndStatus(quest.getId(), QuestApplicationStatus.APPROVED)
        );
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

    public List<QuestResponseDTO> toResponses(
            List<Quest> quests,
            AppUser currentUser,
            Map<Long, QuestApplication> applicationsByQuestId
    ) {
        return quests.stream()
                .map(quest -> toResponse(quest, currentUser, applicationsByQuestId))
                .toList();
    }
}
