package com.themuffinman.app.workmarket.mapper;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.common.dto.NavigationTargetType;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.QuestAllowedActionDTO;
import com.themuffinman.app.vision.dto.QuestRequestDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.vision.dto.QuestViewerRelationDTO;
import com.themuffinman.app.location.model.QuestLocationSource;
import com.themuffinman.app.location.model.QuestLocationVisibility;
import com.themuffinman.app.social.dto.CircleSummaryDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.service.WorkmarketQuestPresentationAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkmarketQuestMgr {
    private final WorkmarketQuestPresentationAssembler questPresentationAssembler;

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
                .audience(com.themuffinman.app.vision.model.QuestAudience.valueOf(quest.getAudience().name()))
                .locationVisibility(quest.getLocationVisibility())
                .locationSource(quest.getLocationSource())
                .locationLabel(quest.getLocationLabel())
                .locationCountry(quest.getLocationCountry())
                .locationLocality(quest.getLocationLocality())
                .locationPostalCode(quest.getLocationPostalCode())
                .locationStreet(quest.getLocationStreet())
                .locationHouseNumber(quest.getLocationHouseNumber())
                .visibleToCircles(quest.getVisibleToCircles().stream()
                        .map(circle -> CircleSummaryDTO.builder()
                                .id(circle.getId())
                                .name(circle.getName())
                                .build())
                        .toList())
                .images(List.copyOf(quest.getImages()))
                .status(com.themuffinman.app.vision.model.QuestStatus.valueOf(quest.getStatus().name()))
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
        quest.setAudience(dto.getAudience() == null ? QuestAudience.CIRCLES : QuestAudience.valueOf(dto.getAudience().name()));
        quest.setImages(dto.getImages() == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(dto.getImages()));
        quest.setStatus(QuestStatus.OPEN);
        quest.setLocationVisibility(dto.getLocationVisibility() == null ? QuestLocationVisibility.INHERIT : dto.getLocationVisibility());
        quest.setLocationSource(dto.getLocationSource() == null ? QuestLocationSource.PROFILE : dto.getLocationSource());
        return quest;
    }

    public com.themuffinman.app.vision.model.Quest toVisionEntity(Quest quest) {
        if (quest == null) {
            return null;
        }

        com.themuffinman.app.vision.model.Quest visionQuest = new com.themuffinman.app.vision.model.Quest();
        visionQuest.setId(quest.getId());
        visionQuest.setCreator(quest.getCreator());
        visionQuest.setTitle(quest.getTitle());
        visionQuest.setDescription(quest.getDescription());
        visionQuest.setImages(quest.getImages());
        visionQuest.setAwardAmount(quest.getAwardAmount());
        visionQuest.setAssigneeTarget(quest.getAssigneeTarget());
        visionQuest.setShowApprovedApplicants(quest.isShowApprovedApplicants());
        visionQuest.setScheduledAt(quest.getScheduledAt());
        visionQuest.setEndsAt(quest.getEndsAt());
        visionQuest.setTermFixed(quest.isTermFixed());
        visionQuest.setPendingScheduledAt(quest.getPendingScheduledAt());
        visionQuest.setPendingEndsAt(quest.getPendingEndsAt());
        visionQuest.setPendingTermFixed(quest.getPendingTermFixed());
        visionQuest.setReopenedAt(quest.getReopenedAt());
        visionQuest.setAudience(com.themuffinman.app.vision.model.QuestAudience.valueOf(quest.getAudience().name()));
        visionQuest.setVisibleToCircles(quest.getVisibleToCircles());
        visionQuest.setTermChangePreviousStatus(quest.getTermChangePreviousStatus() == null
                ? null
                : com.themuffinman.app.vision.model.QuestStatus.valueOf(quest.getTermChangePreviousStatus().name()));
        visionQuest.setStatus(com.themuffinman.app.vision.model.QuestStatus.valueOf(quest.getStatus().name()));
        visionQuest.setLocationVisibility(quest.getLocationVisibility());
        visionQuest.setLocationSource(quest.getLocationSource());
        visionQuest.setLocationLabel(quest.getLocationLabel());
        visionQuest.setLocationProvider(quest.getLocationProvider());
        visionQuest.setLocationProviderPlaceId(quest.getLocationProviderPlaceId());
        visionQuest.setLocationCountryCode(quest.getLocationCountryCode());
        visionQuest.setLocationCountry(quest.getLocationCountry());
        visionQuest.setLocationLocality(quest.getLocationLocality());
        visionQuest.setLocationPostalCode(quest.getLocationPostalCode());
        visionQuest.setLocationStreet(quest.getLocationStreet());
        visionQuest.setLocationHouseNumber(quest.getLocationHouseNumber());
        visionQuest.setLocationLatitude(quest.getLocationLatitude());
        visionQuest.setLocationLongitude(quest.getLocationLongitude());
        visionQuest.setLocationResolvedAt(quest.getLocationResolvedAt());
        return visionQuest;
    }

    public Quest toWorkmarketEntity(com.themuffinman.app.vision.model.Quest quest) {
        if (quest == null) {
            return null;
        }

        Quest workmarketQuest = new Quest();
        workmarketQuest.setId(quest.getId());
        workmarketQuest.setCreator(quest.getCreator());
        workmarketQuest.setTitle(quest.getTitle());
        workmarketQuest.setDescription(quest.getDescription());
        workmarketQuest.setImages(quest.getImages());
        workmarketQuest.setAwardAmount(quest.getAwardAmount());
        workmarketQuest.setAssigneeTarget(quest.getAssigneeTarget());
        workmarketQuest.setShowApprovedApplicants(quest.isShowApprovedApplicants());
        workmarketQuest.setScheduledAt(quest.getScheduledAt());
        workmarketQuest.setEndsAt(quest.getEndsAt());
        workmarketQuest.setTermFixed(quest.isTermFixed());
        workmarketQuest.setPendingScheduledAt(quest.getPendingScheduledAt());
        workmarketQuest.setPendingEndsAt(quest.getPendingEndsAt());
        workmarketQuest.setPendingTermFixed(quest.getPendingTermFixed());
        workmarketQuest.setReopenedAt(quest.getReopenedAt());
        workmarketQuest.setAudience(quest.getAudience() == null ? null : QuestAudience.valueOf(quest.getAudience().name()));
        workmarketQuest.setVisibleToCircles(quest.getVisibleToCircles());
        workmarketQuest.setTermChangePreviousStatus(quest.getTermChangePreviousStatus() == null
                ? null
                : QuestStatus.valueOf(quest.getTermChangePreviousStatus().name()));
        workmarketQuest.setStatus(quest.getStatus() == null ? null : QuestStatus.valueOf(quest.getStatus().name()));
        workmarketQuest.setLocationVisibility(quest.getLocationVisibility());
        workmarketQuest.setLocationSource(quest.getLocationSource());
        workmarketQuest.setLocationLabel(quest.getLocationLabel());
        workmarketQuest.setLocationProvider(quest.getLocationProvider());
        workmarketQuest.setLocationProviderPlaceId(quest.getLocationProviderPlaceId());
        workmarketQuest.setLocationCountryCode(quest.getLocationCountryCode());
        workmarketQuest.setLocationCountry(quest.getLocationCountry());
        workmarketQuest.setLocationLocality(quest.getLocationLocality());
        workmarketQuest.setLocationPostalCode(quest.getLocationPostalCode());
        workmarketQuest.setLocationStreet(quest.getLocationStreet());
        workmarketQuest.setLocationHouseNumber(quest.getLocationHouseNumber());
        workmarketQuest.setLocationLatitude(quest.getLocationLatitude());
        workmarketQuest.setLocationLongitude(quest.getLocationLongitude());
        workmarketQuest.setLocationResolvedAt(quest.getLocationResolvedAt());
        return workmarketQuest;
    }
}
