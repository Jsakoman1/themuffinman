package com.themuffinman.app.social.service;

import com.themuffinman.app.social.dto.CircleRelationStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SocialPresentationHelper {

    public String relationLabel(CircleRelationStatus status) {
        if (status == null) {
            return "";
        }

        return switch (status) {
            case NONE -> "Not connected";
            case CIRCLE -> "Connected";
            case INCOMING_REQUEST -> "Invite received";
            case OUTGOING_REQUEST -> "Invite sent";
            case BLOCKED -> "Blocked";
        };
    }

    public String relationBadgeClass(CircleRelationStatus status) {
        if (status == CircleRelationStatus.BLOCKED) {
            return "badge badge--danger";
        }

        if (status == CircleRelationStatus.INCOMING_REQUEST || status == CircleRelationStatus.OUTGOING_REQUEST) {
            return "badge badge--warning";
        }

        if (status == CircleRelationStatus.NONE) {
            return "badge badge--accent";
        }

        return "badge";
    }

    public String circleSummaryLabel(List<String> circleNames) {
        if (circleNames == null || circleNames.isEmpty()) {
            return "Unassigned";
        }

        return String.join(", ", circleNames);
    }

    public String memberPreviewLabel(List<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return "No members";
        }

        return String.join(", ", usernames);
    }

    public String requestSummaryLabel(boolean incoming) {
        return incoming ? "Wants to connect" : "Invite sent";
    }
}
