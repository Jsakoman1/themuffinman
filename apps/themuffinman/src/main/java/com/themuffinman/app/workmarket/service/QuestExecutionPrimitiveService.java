package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.AppUserLookupService;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.QuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestExecutionPrimitiveService {

    private final QuestRepository questRepository;
    private final QuestApplicationRepository questApplicationRepository;
    private final AppUserLookupService appUserLookupService;
    private final QuestAccessPolicyService questAccessPolicyService;
    private final QuestStateTransitionService questStateTransitionService;
    private final QuestWorkflowNotificationService questWorkflowNotificationService;

    @Transactional(readOnly = true)
    public Quest resolveTarget(Long questId) {
        return questRepository.findForQuestDetail(questId)
                .orElseThrow(() -> ServiceErrors.notFound("Quest not found with id " + questId));
    }

    public Quest resolveTargetForOwnerMutation(Long questId, AppUser actor) {
        Quest quest = resolveTarget(questId);
        validateOwnerAuthority(quest, actor);
        return quest;
    }

    public Quest resolveTargetForExecutionMutation(Long questId, AppUser actor) {
        Quest quest = resolveTarget(questId);
        validateExecutionAuthority(quest, actor);
        return quest;
    }

    @Transactional(readOnly = true)
    public Quest resolveTargetForTermDecision(Long questId, AppUser actor) {
        Quest quest = resolveTarget(questId);
        validateTermDecisionAuthority(quest, actor);
        return quest;
    }

    @Transactional(readOnly = true)
    public AppUser resolveCreator(QuestRequestDTO dto, AppUser actor) {
        if (questAccessPolicyService.isAdmin(actor) && dto.getCreatorId() != null) {
            return appUserLookupService.requireById(dto.getCreatorId(), "Creator not found with id " + dto.getCreatorId());
        }

        return actor;
    }

    public void validateOwnerAuthority(Quest quest, AppUser actor) {
        if (!questAccessPolicyService.canManageQuest(quest, actor)) {
            throw ServiceErrors.forbidden("You are not allowed to modify this quest");
        }
    }

    public void validateExecutionAuthority(Quest quest, AppUser actor) {
        questStateTransitionService.validateQuestExecutionAuthority(quest, actor);
    }

    public void validateTermDecisionAuthority(Quest quest, AppUser actor) {
        questStateTransitionService.validateQuestTermDecisionAuthority(quest, actor);
    }

    public void validateState(Quest quest, QuestStatus requiredStatus, String message) {
        questStateTransitionService.requireQuestStatus(quest, requiredStatus, message);
    }

    public Quest persistMutation(Quest quest) {
        return questRepository.save(quest);
    }

    public void deleteMutationData(Quest quest, AppUser actor) {
        questWorkflowNotificationService.notifyQuestDeleted(quest, actor);
        questApplicationRepository.deleteByQuestId(quest.getId());
        questRepository.deleteById(quest.getId());
    }

    public void emitApprovedApplicantNotification(Quest quest, AppUser actor, QuestNewsType type, String title, String message) {
        questWorkflowNotificationService.notifyApprovedApplicant(quest, actor, type, title, message);
    }

    public void emitCreatorNotification(Quest quest, AppUser actor, QuestNewsType type, String title, String message) {
        questWorkflowNotificationService.notifyQuestCreator(quest, actor, type, title, message);
    }

    public void validateApplicationDetailAccess(QuestApplication application, Quest quest, AppUser currentUser) {
        if (questAccessPolicyService.canViewQuestApplication(application, quest, currentUser)) {
            return;
        }

        throw ServiceErrors.forbidden("You are not allowed to view this application");
    }
}
