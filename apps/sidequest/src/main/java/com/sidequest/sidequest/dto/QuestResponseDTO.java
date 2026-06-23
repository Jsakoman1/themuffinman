package com.sidequest.sidequest.dto;

import com.sidequest.sidequest.model.QuestStatus;
import com.sidequest.sidequest.model.QuestAudience;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestResponseDTO {
    private long id;

    private Long creatorId;
    private String creatorUsername;
    private String creatorProfileDescription;
    private String creatorProfileAvatarDataUrl;

    private String title;
    private String description;

    private BigDecimal awardAmount;
    private Integer assigneeTarget;
    private Instant scheduledAt;
    private Instant endsAt;
    private boolean termFixed;
    private Instant pendingScheduledAt;
    private Instant pendingEndsAt;
    private Boolean pendingTermFixed;
    private Instant reopenedAt;
    private QuestAudience audience;
    private List<CircleSummaryDTO> visibleToCircles;
    private List<String> images;

    private QuestStatus status;
    private QuestViewerRelation viewerRelation;
    private List<QuestAllowedAction> allowedActions;
    private boolean hasApplied;
    private Long myApplicationId;
    private boolean canViewApplications;

}
