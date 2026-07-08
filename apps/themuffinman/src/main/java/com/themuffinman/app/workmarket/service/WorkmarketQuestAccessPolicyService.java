package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.concepts.ActorIdentity;
import com.themuffinman.app.common.concepts.ModuleOwnership;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestViewerRelationDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("workmarketQuestAccessPolicyService")
public class WorkmarketQuestAccessPolicyService {

    public QuestViewerRelationDTO resolveViewerRelation(Quest quest, AppUser currentUser, QuestApplication viewerApplication) {
        if (currentUser == null) {
            return QuestViewerRelationDTO.VIEWER;
        }

        if (isQuestOwner(quest, currentUser)) {
            return QuestViewerRelationDTO.OWNER;
        }

        if (isAdmin(currentUser)) {
            return QuestViewerRelationDTO.ADMIN;
        }

        if (viewerApplication != null && viewerApplication.getStatus() == QuestApplicationStatus.APPROVED) {
            return QuestViewerRelationDTO.APPROVED_APPLICANT;
        }

        if (viewerApplication != null) {
            return QuestViewerRelationDTO.APPLICANT;
        }

        return QuestViewerRelationDTO.VIEWER;
    }

    public List<QuestAllowedActionDTO> resolveAllowedActions(Quest quest, AppUser currentUser, QuestApplication viewerApplication) {
        List<QuestAllowedActionDTO> allowedActions = new ArrayList<>();
        boolean approvedApplicant = viewerApplication != null && viewerApplication.getStatus() == QuestApplicationStatus.APPROVED;

        if (canManageQuest(quest, currentUser)) {
            allowedActions.add(QuestAllowedActionDTO.EDIT);
            allowedActions.add(QuestAllowedActionDTO.VIEW_APPLICATIONS);
            allowedActions.add(QuestAllowedActionDTO.DELETE);
        }

        if (canApplyToQuest(quest, currentUser, viewerApplication)) {
            allowedActions.add(QuestAllowedActionDTO.APPLY);
        }

        if (canExecuteQuest(quest, currentUser, approvedApplicant) && quest.getStatus() == QuestStatus.ASSIGNED) {
            allowedActions.add(QuestAllowedActionDTO.START);
        }

        if (canExecuteQuest(quest, currentUser, approvedApplicant) && quest.getStatus() == QuestStatus.IN_PROGRESS) {
            allowedActions.add(QuestAllowedActionDTO.COMPLETE);
        }

        if (canDecideQuestTermChange(quest, currentUser, approvedApplicant) && quest.getStatus() == QuestStatus.WAITING_CONFIRMATION) {
            allowedActions.add(QuestAllowedActionDTO.CONFIRM_TERM_CHANGE);
            allowedActions.add(QuestAllowedActionDTO.REJECT_TERM_CHANGE);
        }

        return allowedActions;
    }

    public boolean isQuestOwner(Quest quest, AppUser currentUser) {
        return quest != null
                && quest.getCreator() != null
                && ModuleOwnership.isOwner(quest.getCreator().getId(), currentUser);
    }

    public boolean canManageQuest(Quest quest, AppUser currentUser) {
        return isAdmin(currentUser) || isQuestOwner(quest, currentUser);
    }

    public boolean canManageQuestApplications(Quest quest, AppUser currentUser) {
        return canManageQuest(quest, currentUser);
    }

    public boolean canViewQuestApplication(QuestApplication application, Quest quest, AppUser currentUser) {
        ActorIdentity actor = ActorIdentity.from(currentUser);
        if (!actor.authenticated()) {
            return false;
        }

        if (canManageQuestApplications(quest, currentUser)) {
            return true;
        }

        return application != null
                && application.getApplicant() != null
                && actor.sameUser(application.getApplicant().getId());
    }

    public boolean canApplyToQuest(Quest quest, AppUser currentUser, QuestApplication viewerApplication) {
        return quest != null
                && quest.getStatus() == QuestStatus.OPEN
                && !isQuestOwner(quest, currentUser)
                && !isAdmin(currentUser)
                && viewerApplication == null;
    }

    public boolean canExecuteQuest(Quest quest, AppUser currentUser, boolean hasApprovedApplication) {
        return canManageQuest(quest, currentUser) || hasApprovedApplication;
    }

    public boolean canDecideQuestTermChange(Quest quest, AppUser currentUser, boolean hasApprovedApplication) {
        return isAdmin(currentUser) || hasApprovedApplication;
    }

    public boolean isAdmin(AppUser user) {
        return ActorIdentity.from(user).admin();
    }
}
