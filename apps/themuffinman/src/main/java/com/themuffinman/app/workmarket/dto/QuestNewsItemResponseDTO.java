package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import lombok.*;
import org.springframework.lang.Nullable;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestNewsItemResponseDTO {
    private Long id;
    private QuestNewsType type;
    private String typeLabel;
    private String badgeClass;
    private String iconGlyph;
    private String title;
    private String message;
    @Nullable
    private Long questId;
    @Nullable
    private String questTitle;
    @Nullable
    private Long applicationId;
    @Nullable
    private Long circleRequestId;
    private QuestNewsDestinationType destinationType;
    @Nullable
    private Long destinationId;
    private NavigationTargetDTO navigation;
    private Long actorUserId;
    private String actorUsername;
    private boolean canAcceptCircleRequest;
    private boolean canDeclineCircleRequest;
    @Nullable
    private Instant readAt;
    private Instant createdAt;
}
