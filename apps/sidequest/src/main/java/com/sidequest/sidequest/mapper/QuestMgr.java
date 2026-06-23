package com.sidequest.sidequest.mapper;

import com.sidequest.sidequest.dto.QuestRequestDTO;
import com.sidequest.sidequest.dto.QuestResponseDTO;
import com.sidequest.sidequest.dto.CircleSummaryDTO;
import com.sidequest.sidequest.dto.QuestAllowedAction;
import com.sidequest.sidequest.dto.QuestViewerRelation;
import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.model.Quest;
import com.sidequest.sidequest.model.QuestStatus;
import com.sidequest.sidequest.service.RichTextInputValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestMgr {

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
        quest.setAudience(dto.getAudience() == null ? com.sidequest.sidequest.model.QuestAudience.CIRCLES : dto.getAudience());
        quest.setImages(dto.getImages() == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(dto.getImages()));
        quest.setStatus(QuestStatus.OPEN);

        return quest;
    }
}
