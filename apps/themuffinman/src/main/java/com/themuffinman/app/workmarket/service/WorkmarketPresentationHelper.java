package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import com.themuffinman.app.workmarket.model.QuestStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class WorkmarketPresentationHelper {

    public String formatQuestStatus(@Nullable QuestStatus status) {
        if (status == QuestStatus.WAITING_CONFIRMATION) {
            return "Waiting confirmation";
        }

        return status == null ? "" : status.name().replace("_", " ");
    }

    public String formatApplicationStatus(@Nullable QuestApplicationStatus status) {
        if (status == QuestApplicationStatus.PENDING) {
            return "Open";
        }

        return status == null ? "" : status.name().replace("_", " ");
    }

    public String badgeClassForQuestStatus(@Nullable QuestStatus status) {
        if (status == QuestStatus.WAITING_CONFIRMATION) {
            return "badge badge--warning";
        }

        if (status == QuestStatus.CANCELLED) {
            return "badge badge--danger";
        }

        return "badge";
    }

    public String badgeClassForApplicationStatus(@Nullable QuestApplicationStatus status) {
        if (status == QuestApplicationStatus.APPROVED) {
            return "badge badge--success";
        }

        if (status == QuestApplicationStatus.DECLINED || status == QuestApplicationStatus.WITHDRAWN) {
            return "badge badge--danger";
        }

        return "badge";
    }

    public String surfaceClassForApplicationStatus(@Nullable QuestApplicationStatus status) {
        if (status == QuestApplicationStatus.PENDING) {
            return "status-surface status-surface--open";
        }
        if (status == QuestApplicationStatus.APPROVED) {
            return "status-surface status-surface--assigned";
        }
        if (status == QuestApplicationStatus.DECLINED || status == QuestApplicationStatus.WITHDRAWN) {
            return "status-surface status-surface--cancelled";
        }

        return "status-surface status-surface--open";
    }

    public String surfaceClassForQuestStatus(@Nullable QuestStatus status) {
        if (status == QuestStatus.OPEN) {
            return "status-surface status-surface--open";
        }
        if (status == QuestStatus.ASSIGNED) {
            return "status-surface status-surface--assigned";
        }
        if (status == QuestStatus.WAITING_CONFIRMATION) {
            return "status-surface status-surface--waiting";
        }
        if (status == QuestStatus.IN_PROGRESS) {
            return "status-surface status-surface--progress";
        }
        if (status == QuestStatus.COMPLETED) {
            return "status-surface status-surface--done";
        }
        if (status == QuestStatus.CANCELLED) {
            return "status-surface status-surface--cancelled";
        }

        return "status-surface status-surface--open";
    }

    public String formatTimeType(boolean termFixed) {
        return termFixed ? "Fixed time" : "By agreement";
    }

    public String formatAudience(@Nullable QuestAudience audience) {
        if (audience == QuestAudience.EVERYONE) {
            return "Everyone";
        }

        if (audience == QuestAudience.CIRCLES) {
            return "Circles";
        }

        return "";
    }

    public boolean showAssigneeTarget(@Nullable Integer assigneeTarget) {
        return assigneeTarget == null || assigneeTarget > 1;
    }

    public String formatAssigneeTarget(@Nullable Integer assigneeTarget) {
        if (assigneeTarget == null) {
            return "Unlimited";
        }

        if (assigneeTarget == 1) {
            return "1";
        }

        return assigneeTarget + " spots";
    }

    public String formatQuestNewsType(@Nullable QuestNewsType type) {
        if (type == null) {
            return "";
        }

        return switch (type) {
            case APPLICATION_CREATED -> "New application";
            case APPLICATION_UPDATED -> "Application updated";
            case APPLICATION_WITHDRAWN -> "Application withdrawn";
            case APPLICATION_APPROVED -> "Application approved";
            case APPLICATION_DECLINED -> "Application declined";
            case QUEST_TERM_CONFIRMATION_REQUESTED -> "Time confirmation needed";
            case QUEST_TERM_CONFIRMED -> "Time confirmed";
            case QUEST_TERM_REJECTED -> "Time rejected";
            case QUEST_STARTED -> "Quest started";
            case QUEST_COMPLETED -> "Quest completed";
            case QUEST_REOPENED -> "Quest reopened";
            case QUEST_DELETED -> "Quest deleted";
            case CIRCLE_REQUEST_ACCEPTED -> "Circle request accepted";
        };
    }

    public String badgeClassForQuestNewsType(@Nullable QuestNewsType type) {
        if (type == null) {
            return "badge--accent";
        }

        return switch (type) {
            case APPLICATION_WITHDRAWN, APPLICATION_DECLINED, QUEST_TERM_REJECTED, QUEST_DELETED -> "badge--danger";
            case APPLICATION_APPROVED, QUEST_TERM_CONFIRMED, QUEST_COMPLETED, CIRCLE_REQUEST_ACCEPTED -> "badge--success";
            case QUEST_TERM_CONFIRMATION_REQUESTED, QUEST_REOPENED -> "badge--warning";
            default -> "badge--accent";
        };
    }
}
