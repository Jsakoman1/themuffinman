package com.themuffinman.app.vision.dto;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.location.model.QuestLocationVisibility;
import com.themuffinman.app.location.model.QuestLocationSource;
import com.themuffinman.app.social.dto.CircleSummaryDTO;
import com.themuffinman.app.vision.model.QuestStatus;
import com.themuffinman.app.vision.model.QuestAudience;
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
    private String resolutionKey;
    private String resolutionLabel;
    private boolean exactResolutionEligible;

    private String title;
    private String description;

    private BigDecimal awardAmount;
    @Nullable
    private Integer assigneeTarget;
    private boolean showApprovedApplicants;
    private int approvedApplicationCount;
    private int remainingAssigneeSlots;
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
    private QuestViewerRelationDTO viewerRelation;
    private List<QuestAllowedActionDTO> allowedActions;
    private boolean hasApplied;
    @Nullable
    private Long myApplicationId;
    private boolean canViewApplications;
    private QuestPresentationDTO presentation;

}
