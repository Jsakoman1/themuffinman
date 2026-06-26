package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.location.model.QuestLocationVisibility;
import com.themuffinman.app.location.model.QuestLocationSource;
import com.themuffinman.app.social.dto.CircleSummaryDTO;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.model.QuestAudience;
import lombok.*;
import org.springframework.lang.Nullable;

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
    @Nullable
    private String creatorProfileDescription;
    @Nullable
    private String creatorProfileAvatarDataUrl;
    private NavigationTargetDTO questNavigation;
    private NavigationTargetDTO creatorNavigation;

    private String title;
    private String description;

    private BigDecimal awardAmount;
    @Nullable
    private Integer assigneeTarget;
    @Nullable
    private Instant scheduledAt;
    @Nullable
    private Instant endsAt;
    private boolean termFixed;
    @Nullable
    private Instant pendingScheduledAt;
    @Nullable
    private Instant pendingEndsAt;
    @Nullable
    private Boolean pendingTermFixed;
    @Nullable
    private Instant reopenedAt;
    private QuestAudience audience;
    private QuestLocationVisibility locationVisibility;
    private QuestLocationSource locationSource;
    @Nullable
    private String locationLabel;
    @Nullable
    private String locationCountry;
    @Nullable
    private String locationLocality;
    @Nullable
    private String locationPostalCode;
    @Nullable
    private String locationStreet;
    @Nullable
    private String locationHouseNumber;
    private List<CircleSummaryDTO> visibleToCircles;
    private List<String> images;

    private QuestStatus status;
    private QuestViewerRelation viewerRelation;
    private List<QuestAllowedAction> allowedActions;
    private boolean hasApplied;
    @Nullable
    private Long myApplicationId;
    private boolean canViewApplications;
    private QuestPresentationDTO presentation;

}
