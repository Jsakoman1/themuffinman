package com.themuffinman.app.workmarket.mapper;

import com.themuffinman.app.common.dto.NavigationTargetDTO;
import com.themuffinman.app.common.dto.NavigationTargetType;
import com.themuffinman.app.workmarket.dto.ApplicationAllowedAction;
import com.themuffinman.app.workmarket.dto.QuestApplicationPresentationDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationRequestDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.workmarket.service.WorkmarketPresentationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestApplicationMgr {
    private final WorkmarketPresentationHelper presentationHelper;

    public QuestApplication toEntity(QuestApplicationRequestDTO dto, Quest quest, AppUser applicant) {
        QuestApplication application = new QuestApplication();
        application.setQuest(quest);
        application.setApplicant(applicant);
        application.setMessage(RichTextInputValidator.sanitize(dto.getMessage()));
        application.setProposedPrice(dto.getProposedPrice());
        application.setStatus(QuestApplicationStatus.PENDING);
        return application;
    }

    public QuestApplicationResponseDTO toDto(QuestApplication application) {
        return toDto(application, List.of());
    }

    public QuestApplicationResponseDTO toDto(QuestApplication application, List<ApplicationAllowedAction> allowedActions) {
        if (application == null) {
            return null;
        }

        return QuestApplicationResponseDTO.builder()
                .id(application.getId())
                .questId(application.getQuest().getId())
                .questTitle(application.getQuest().getTitle())
                .questCreatorUsername(application.getQuest().getCreator().getUsername())
                .questDescription(application.getQuest().getDescription())
                .questStatus(application.getQuest().getStatus())
                .questAssigneeTarget(application.getQuest().getAssigneeTarget())
                .questScheduledAt(application.getQuest().getScheduledAt())
                .questEndsAt(application.getQuest().getEndsAt())
                .questTermFixed(application.getQuest().isTermFixed())
                .applicantId(application.getApplicant().getId())
                .applicantUsername(application.getApplicant().getUsername())
                .applicantProfileDescription(RichTextInputValidator.sanitize(application.getApplicant().getProfileDescription()))
                .applicantProfileAvatarDataUrl(application.getApplicant().getProfileAvatarDataUrl())
                .questNavigation(NavigationTargetDTO.builder()
                        .type(NavigationTargetType.QUEST_DETAIL)
                        .entityId(application.getQuest().getId())
                        .build())
                .applicantNavigation(NavigationTargetDTO.builder()
                        .type(NavigationTargetType.USER_PROFILE)
                        .entityId(application.getApplicant().getId())
                        .build())
                .message(RichTextInputValidator.sanitize(application.getMessage()))
                .proposedPrice(application.getProposedPrice())
                .status(application.getStatus())
                .allowedActions(List.copyOf(allowedActions))
                .presentation(QuestApplicationPresentationDTO.builder()
                        .statusLabel(presentationHelper.formatApplicationStatus(application.getStatus()))
                        .statusBadgeClass(presentationHelper.badgeClassForApplicationStatus(application.getStatus()))
                        .statusSurfaceClass(presentationHelper.surfaceClassForApplicationStatus(application.getStatus()))
                        .questStatusLabel(presentationHelper.formatQuestStatus(application.getQuest().getStatus()))
                        .questStatusBadgeClass(presentationHelper.badgeClassForQuestStatus(application.getQuest().getStatus()))
                        .canEdit(false)
                        .canWithdraw(false)
                        .autoOpenEditForm(false)
                        .canApprove(false)
                        .canDecline(false)
                        .showManagementActions(false)
                        .build())
                .createdAt(application.getCreatedAt())
                .build();
    }
}
