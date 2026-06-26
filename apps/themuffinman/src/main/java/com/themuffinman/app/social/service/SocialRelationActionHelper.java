package com.themuffinman.app.social.service;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.common.dto.NavigationTargetType;
import com.themuffinman.app.identity.dto.ProfilePrimaryActionDTO;
import com.themuffinman.app.social.dto.CircleRelationStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class SocialRelationActionHelper {

    public SearchActions searchActions(CircleRelationStatus relationStatus, boolean blockedByCurrentUser) {
        if (relationStatus == CircleRelationStatus.NONE) {
            return SearchActions.builder()
                    .primaryAction(action("SEND_INVITE", "Send invite", true))
                    .secondaryAction(action("BLOCK", "Block", true))
                    .build();
        }

        if (relationStatus == CircleRelationStatus.BLOCKED) {
            if (blockedByCurrentUser) {
                return SearchActions.builder()
                        .primaryAction(action("UNBLOCK", "Unblock", true))
                        .build();
            }

            return SearchActions.builder()
                    .primaryAction(action("NONE", "Blocked by them", false))
                    .build();
        }

        return SearchActions.builder()
                .secondaryAction(action("BLOCK", "Block", true))
                .build();
    }

    public SearchActions requestActions(boolean incoming) {
        if (incoming) {
            return SearchActions.builder()
                    .primaryAction(action("ACCEPT_REQUEST", "Accept", true))
                    .secondaryAction(action("DECLINE_REQUEST", "Decline", true))
                    .build();
        }

        return SearchActions.builder()
                .primaryAction(action("CANCEL_REQUEST", "Cancel", true))
                .build();
    }

    public ProfilePrimaryActionDTO profilePrimaryAction(boolean ownProfile, CircleRelationStatus relationStatus, boolean blockedByCurrentUser) {
        if (ownProfile) {
            return action("EDIT_PROFILE", "Edit profile", true);
        }

        if (relationStatus == CircleRelationStatus.BLOCKED) {
            if (blockedByCurrentUser) {
                return action("UNBLOCK", "Unblock", true);
            }

            return action("NONE", "Blocked", false);
        }

        if (relationStatus == CircleRelationStatus.CIRCLE) {
            return action("NONE", "Connected", false);
        }

        if (relationStatus == CircleRelationStatus.OUTGOING_REQUEST) {
            return action("NONE", "Invite sent", false);
        }

        if (relationStatus == CircleRelationStatus.INCOMING_REQUEST) {
            return action("OPEN_CIRCLES", "Open circles", true, NavigationTargetType.CIRCLES, null);
        }

        return action("SEND_INVITE", "Send invite", true);
    }

    public String blockActionLabel() {
        return "Block";
    }

    private ProfilePrimaryActionDTO action(String type, String label, boolean enabled) {
        return action(type, label, enabled, null, null);
    }

    private ProfilePrimaryActionDTO action(
            String type,
            String label,
            boolean enabled,
            NavigationTargetType navigationType,
            Long navigationEntityId
    ) {
        return ProfilePrimaryActionDTO.builder()
                .type(type)
                .label(label)
                .enabled(enabled)
                .navigation(navigationType == null
                        ? null
                        : NavigationTargetDTO.builder()
                                .type(navigationType)
                                .entityId(navigationEntityId)
                                .build())
                .build();
    }

    @Getter
    @Builder
    public static class SearchActions {
        private ProfilePrimaryActionDTO primaryAction;
        private ProfilePrimaryActionDTO secondaryAction;
    }
}
