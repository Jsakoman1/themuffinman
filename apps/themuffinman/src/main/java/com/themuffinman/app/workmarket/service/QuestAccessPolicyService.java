package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
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

    public boolean isQuestOwner(Quest quest, AppUser currentUser) {
        return quest != null
                && currentUser != null
                && quest.getCreator() != null
                && quest.getCreator().getId().equals(currentUser.getId());
    }

    public boolean isAdmin(AppUser user) {
        return user != null && user.getRole() == AppUserRole.ADMIN;
    }
}
