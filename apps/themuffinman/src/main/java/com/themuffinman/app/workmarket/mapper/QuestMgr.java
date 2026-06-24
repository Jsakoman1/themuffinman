package com.themuffinman.app.workmarket.mapper;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.common.dto.NavigationTargetType;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestPresentationDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.social.dto.CircleSummaryDTO;
import com.themuffinman.app.workmarket.dto.QuestAllowedAction;
import com.themuffinman.app.workmarket.dto.QuestViewerRelation;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.workmarket.service.WorkmarketPresentationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestMgr {
    private final WorkmarketPresentationHelper presentationHelper;

    public QuestResponseDTO toDto(Quest quest) {
        if (quest == null) {
            return null;
        }

        return QuestResponseDTO.builder()
                .id(quest.getId())
                .creatorId(quest.getCreator().getId())
                .creatorUsername(quest.getCreator().getUsername())
                .creatorProfileDescription(RichTextInputValidator.sanitize(quest.getCreator().getProfileDescription()))
                .creatorProfileAvatarDataUrl(quest.getCreator().getProfileAvatarDataUrl())
                .questNavigation(NavigationTargetDTO.builder()
                        .type(NavigationTargetType.QUEST_DETAIL)
                        .entityId(quest.getId())
                        .build())
                .creatorNavigation(NavigationTargetDTO.builder()
                        .type(NavigationTargetType.USER_PROFILE)
                        .entityId(quest.getCreator().getId())
                        .build())
                .title(quest.getTitle())
                .description(RichTextInputValidator.sanitize(quest.getDescription()))
                .awardAmount(quest.getAwardAmount())
                .assigneeTarget(quest.getAssigneeTarget())
                .scheduledAt(quest.getScheduledAt())
                .endsAt(quest.getEndsAt())
                .termFixed(quest.isTermFixed())
                .pendingScheduledAt(quest.getPendingScheduledAt())
                .pendingEndsAt(quest.getPendingEndsAt())
                .pendingTermFixed(quest.getPendingTermFixed())
                .reopenedAt(quest.getReopenedAt())
                .audience(quest.getAudience())
                .visibleToCircles(quest.getVisibleToCircles().stream()
                        .map(circle -> CircleSummaryDTO.builder()
                                .id(circle.getId())
                                .name(circle.getName())
                                .build())
                        .toList())
                .images(List.copyOf(quest.getImages()))
                .status(quest.getStatus())
                .viewerRelation(QuestViewerRelation.VIEWER)
                .allowedActions(List.of())
                .hasApplied(false)
                .myApplicationId(null)
                .canViewApplications(false)
                .presentation(QuestPresentationDTO.builder()
                        .canEdit(false)
                        .canApply(false)
                .canViewApplications(false)
                        .statusLabel(presentationHelper.formatQuestStatus(quest.getStatus()))
                        .statusBadgeClass(presentationHelper.badgeClassForQuestStatus(quest.getStatus()))
                        .statusSurfaceClass(presentationHelper.surfaceClassForQuestStatus(quest.getStatus()))
                        .termLabel(presentationHelper.formatQuestTerm(quest.getScheduledAt(), quest.getEndsAt(), quest.isTermFixed()))
                        .timeTypeLabel(presentationHelper.formatTimeType(quest.isTermFixed()))
                        .audienceLabel(presentationHelper.formatAudience(quest.getAudience()))
                        .assigneeTargetVisible(presentationHelper.showAssigneeTarget(quest.getAssigneeTarget()))
                        .assigneeTargetLabel(presentationHelper.formatAssigneeTarget(quest.getAssigneeTarget()))
                        .pendingTermLabel(presentationHelper.formatQuestTerm(
                                quest.getPendingScheduledAt(),
                                quest.getPendingEndsAt(),
                                quest.getPendingTermFixed() == null ? quest.isTermFixed() : quest.getPendingTermFixed()
                        ))
                        .autoOpenEditForm(false)
                        .termChangeVisible(false)
                        .termChangeActionable(false)
                        .applicationSentVisible(false)
                        .canOpenMyApplication(false)
                        .deleteVisible(false)
                        .reopenedBadgeVisible(false)
                        .awaitingConfirmationBadgeVisible(false)
                        .primaryExecutionAction(null)
                        .executionHelperText(null)
                        .build())
                .build();
    }

    public QuestResponseDTO withViewerContext(
            QuestResponseDTO dto,
            QuestViewerRelation viewerRelation,
            List<QuestAllowedAction> allowedActions,
            boolean hasApplied,
            Long myApplicationId,
            boolean canViewApplications
    ) {
        if (dto == null) {
            return null;
        }

        dto.setViewerRelation(viewerRelation);
        dto.setAllowedActions(List.copyOf(allowedActions));
        dto.setHasApplied(hasApplied);
        dto.setMyApplicationId(myApplicationId);
        dto.setCanViewApplications(canViewApplications);
        return dto;
    }

    public Quest toEntity(QuestRequestDTO dto, AppUser creator) {
        if (dto == null) {
            return null;
        }

        Quest quest = new Quest();
        quest.setCreator(creator);
        quest.setTitle(dto.getTitle() == null ? null : dto.getTitle().trim());
        quest.setDescription(RichTextInputValidator.sanitize(dto.getDescription()));
        quest.setAwardAmount(dto.getAwardAmount());
        quest.setAssigneeTarget(dto.getAssigneeTarget() == null ? 1 : dto.getAssigneeTarget());
        quest.setScheduledAt(dto.getScheduledAt());
        quest.setEndsAt(dto.getEndsAt());
        quest.setTermFixed(Boolean.TRUE.equals(dto.getTermFixed()));
        quest.setAudience(dto.getAudience() == null ? com.themuffinman.app.workmarket.model.QuestAudience.CIRCLES : dto.getAudience());
        quest.setImages(dto.getImages() == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(dto.getImages()));
        quest.setStatus(QuestStatus.OPEN);

        return quest;
    }
}
