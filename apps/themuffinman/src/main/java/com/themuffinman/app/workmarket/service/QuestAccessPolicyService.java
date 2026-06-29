package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.concepts.ActorIdentity;
import com.themuffinman.app.common.concepts.ModuleOwnership;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestAllowedAction;
import com.themuffinman.app.workmarket.dto.QuestViewerRelation;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestAccessPolicyService {

    public QuestViewerRelation resolveViewerRelation(Quest quest, AppUser currentUser, QuestApplication viewerApplication) {
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

    public List<QuestAllowedAction> resolveAllowedActions(Quest quest, AppUser currentUser, QuestApplication viewerApplication) {
        List<QuestAllowedAction> allowedActions = new ArrayList<>();
        boolean approvedApplicant = viewerApplication != null && viewerApplication.getStatus() == QuestApplicationStatus.APPROVED;

        if (canManageQuest(quest, currentUser)) {
            allowedActions.add(QuestAllowedAction.EDIT);
            allowedActions.add(QuestAllowedAction.VIEW_APPLICATIONS);
            allowedActions.add(QuestAllowedAction.DELETE);
        }

        if (canApplyToQuest(quest, currentUser, viewerApplication)) {
            allowedActions.add(QuestAllowedAction.APPLY);
        }

        if (canExecuteQuest(quest, currentUser, approvedApplicant) && quest.getStatus() == QuestStatus.ASSIGNED) {
            allowedActions.add(QuestAllowedAction.START);
        }

        if (canExecuteQuest(quest, currentUser, approvedApplicant) && quest.getStatus() == QuestStatus.IN_PROGRESS) {
            allowedActions.add(QuestAllowedAction.COMPLETE);
        }

        if (canDecideQuestTermChange(quest, currentUser, approvedApplicant) && quest.getStatus() == QuestStatus.WAITING_CONFIRMATION) {
            allowedActions.add(QuestAllowedAction.CONFIRM_TERM_CHANGE);
            allowedActions.add(QuestAllowedAction.REJECT_TERM_CHANGE);
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
