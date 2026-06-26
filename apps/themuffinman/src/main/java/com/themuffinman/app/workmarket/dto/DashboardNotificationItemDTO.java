package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardNotificationItemDTO {
    private Long id;
    private QuestNewsType type;
    private String typeLabel;
    private String badgeClass;
    private String iconGlyph;
    private String title;
    private String message;
    private String actorUsername;
    @Nullable
    private String questTitle;
    @Nullable
    private Long questId;
    @Nullable
    private Long applicationId;
    @Nullable
    private Long circleRequestId;
    private Instant createdAt;
    @Nullable
    private Instant readAt;
    private DashboardNotificationDestinationType destinationType;
    @Nullable
    private Long destinationId;
    @Nullable
    private NavigationTargetDTO navigation;
    private boolean unread;
    private boolean canAcceptCircleRequest;
    private boolean canDeclineCircleRequest;
}
