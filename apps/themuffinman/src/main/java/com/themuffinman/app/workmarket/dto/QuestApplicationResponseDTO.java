package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
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
public class QuestApplicationResponseDTO {

    private Long id;

    private Long questId;
    private String questTitle;
    private String questCreatorUsername;
    private String questDescription;
    private QuestStatus questStatus;
    @Nullable
    private Integer questAssigneeTarget;
    @Nullable
    private Instant questScheduledAt;
    @Nullable
    private Instant questEndsAt;
    private boolean questTermFixed;

    private Long applicantId;
    private String applicantUsername;
    @Nullable
    private String applicantProfileDescription;
    @Nullable
    private String applicantProfileAvatarDataUrl;
    private NavigationTargetDTO questNavigation;
    private NavigationTargetDTO applicantNavigation;

    private String message;
    private BigDecimal proposedPrice;

    private QuestApplicationStatus status;
    private List<ApplicationAllowedAction> allowedActions;
    private QuestApplicationPresentationDTO presentation;
    private Instant createdAt;
}
