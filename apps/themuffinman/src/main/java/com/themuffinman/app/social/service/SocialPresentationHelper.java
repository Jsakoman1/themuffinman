package com.themuffinman.app.social.service;

import com.themuffinman.app.social.dto.CircleRelationStatusDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SocialPresentationHelper {

    public String relationLabel(CircleRelationStatusDTO status) {
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

    public String relationBadgeClass(CircleRelationStatusDTO status) {
        if (status == CircleRelationStatusDTO.BLOCKED) {
            return "badge badge--danger";
        }

        if (status == CircleRelationStatusDTO.INCOMING_REQUEST || status == CircleRelationStatusDTO.OUTGOING_REQUEST) {
            return "badge badge--warning";
        }

        if (status == CircleRelationStatusDTO.NONE) {
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
