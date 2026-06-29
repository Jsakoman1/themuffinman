package com.themuffinman.app.workmarket.mapper;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.common.dto.NavigationTargetType;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestViewerRelationDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.service.QuestPresentationAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestMgr {
    private final QuestPresentationAssembler questPresentationAssembler;

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
                .resolutionKey("quest:" + quest.getId())
                .resolutionLabel(quest.getTitle() + " by " + quest.getCreator().getUsername())
                .exactResolutionEligible(true)
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
                .showApprovedApplicants(quest.isShowApprovedApplicants())
                .approvedApplicationCount(0)
                .remainingAssigneeSlots(Math.max((quest.getAssigneeTarget() == null ? 1 : quest.getAssigneeTarget()), 1))
                .scheduledAt(quest.getScheduledAt())
                .endsAt(quest.getEndsAt())
                .termFixed(quest.isTermFixed())
                .pendingScheduledAt(quest.getPendingScheduledAt())
                .pendingEndsAt(quest.getPendingEndsAt())
                .pendingTermFixed(quest.getPendingTermFixed())
                .reopenedAt(quest.getReopenedAt())
                .audience(quest.getAudience())
                .locationVisibility(quest.getLocationVisibility())
                .locationSource(quest.getLocationSource())
                .locationLabel(quest.getLocationLabel())
                .locationCountry(quest.getLocationCountry())
                .locationLocality(quest.getLocationLocality())
                .locationPostalCode(quest.getLocationPostalCode())
                .locationStreet(quest.getLocationStreet())
                .locationHouseNumber(quest.getLocationHouseNumber())
                .visibleToCircles(quest.getVisibleToCircles().stream()
                        .map(circle -> com.themuffinman.app.social.dto.CircleSummaryDTO.builder()
                                .id(circle.getId())
                                .name(circle.getName())
                                .build())
                        .toList())
                .images(List.copyOf(quest.getImages()))
                .status(quest.getStatus())
                .viewerRelation(QuestViewerRelationDTO.VIEWER)
                .allowedActions(List.of())
                .hasApplied(false)
                .myApplicationId(null)
                .canViewApplications(false)
                .presentation(questPresentationAssembler.buildDefaultPresentation(quest))
                .build();
    }

    public QuestResponseDTO withViewerContext(
            QuestResponseDTO dto,
            QuestViewerRelationDTO viewerRelation,
            List<QuestAllowedActionDTO> allowedActions,
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
        quest.setShowApprovedApplicants(Boolean.TRUE.equals(dto.getShowApprovedApplicants()));
        quest.setScheduledAt(dto.getScheduledAt());
        quest.setEndsAt(dto.getEndsAt());
        quest.setTermFixed(Boolean.TRUE.equals(dto.getTermFixed()));
        quest.setAudience(dto.getAudience() == null ? com.themuffinman.app.workmarket.model.QuestAudience.CIRCLES : dto.getAudience());
        quest.setImages(dto.getImages() == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(dto.getImages()));
        quest.setStatus(QuestStatus.OPEN);
        quest.setLocationVisibility(dto.getLocationVisibility() == null ? com.themuffinman.app.location.model.QuestLocationVisibility.INHERIT : dto.getLocationVisibility());
        quest.setLocationSource(dto.getLocationSource() == null ? com.themuffinman.app.location.model.QuestLocationSource.PROFILE : dto.getLocationSource());

        return quest;
    }
}
