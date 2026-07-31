package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationPresentationDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.common.dto.ClientActionDTO;
import com.themuffinman.app.common.dto.ClientActionToneDTO;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.service.WorkmarketPresentationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("workmarketQuestApplicationPresentationAssembler")
@RequiredArgsConstructor
public class WorkmarketQuestApplicationPresentationAssembler {

    private final WorkmarketPresentationHelper presentationHelper;

    public QuestApplicationResponseDTO withAllowedActions(
            QuestApplicationResponseDTO dto,
            List<ApplicationAllowedActionDTO> allowedActions
    ) {
        if (dto == null) {
            return null;
        }

        dto.setAllowedActions(List.copyOf(allowedActions));
        dto.setActions(toClientActions(allowedActions));
        boolean canApprove = allowedActions.contains(ApplicationAllowedActionDTO.APPROVE);
        boolean canDecline = allowedActions.contains(ApplicationAllowedActionDTO.DECLINE);
        dto.setPresentation(QuestApplicationPresentationDTO.builder()
                .statusLabel(presentationHelper.formatApplicationStatus(dto.getStatus()))
                .statusBadgeClass(presentationHelper.badgeClassForApplicationStatus(dto.getStatus()))
                .statusSurfaceClass(presentationHelper.surfaceClassForApplicationStatus(dto.getStatus()))
                .questStatusLabel(presentationHelper.formatQuestStatus(dto.getQuestStatus()))
                .questStatusBadgeClass(presentationHelper.badgeClassForQuestStatus(dto.getQuestStatus()))
                .questAssigneeTargetVisible(presentationHelper.showAssigneeTarget(dto.getQuestAssigneeTarget()))
                .questAssigneeTargetLabel(presentationHelper.formatAssigneeTarget(dto.getQuestAssigneeTarget()))
                .canEdit(allowedActions.contains(ApplicationAllowedActionDTO.EDIT))
                .canWithdraw(allowedActions.contains(ApplicationAllowedActionDTO.WITHDRAW))
                .autoOpenEditForm(allowedActions.contains(ApplicationAllowedActionDTO.EDIT))
                .canApprove(canApprove)
                .canDecline(canDecline)
                .showManagementActions(canApprove || canDecline)
                .build());
        return dto;
    }

    private List<ClientActionDTO> toClientActions(List<ApplicationAllowedActionDTO> allowedActions) {
        return allowedActions.stream().map(action -> {
            boolean destructive = action == ApplicationAllowedActionDTO.WITHDRAW
                    || action == ApplicationAllowedActionDTO.DECLINE
                    || action == ApplicationAllowedActionDTO.RELEASE_WORKER;
            String label = switch (action) {
                case EDIT -> "Edit application";
                case WITHDRAW -> "Withdraw application";
                case APPROVE -> "Approve applicant";
                case DECLINE -> "Decline applicant";
                case RELEASE_WORKER -> "Release worker";
                case REPLACE_WORKER -> "Replace worker";
            };
            return ClientActionDTO.builder()
                    .id(action.name())
                    .label(label)
                    .tone(destructive ? ClientActionToneDTO.DANGER : ClientActionToneDTO.PRIMARY)
                    .enabled(true)
                    .requiresConfirmation(action != ApplicationAllowedActionDTO.EDIT && action != ApplicationAllowedActionDTO.REPLACE_WORKER)
                    .confirmationTitle(label)
                    .confirmationMessage(label + "?")
                    .outcome(actionOutcome(action))
                    .build();
        }).toList();
    }

    private String actionOutcome(ApplicationAllowedActionDTO action) {
        return switch (action) {
            case EDIT -> "Your changes are saved for the work owner to review.";
            case WITHDRAW -> "Your application is withdrawn and the work owner is notified.";
            case APPROVE -> "This person is assigned to the work and is notified.";
            case DECLINE -> "This person is not selected and is notified.";
            case RELEASE_WORKER -> "This person's assignment ends and the work slot becomes available again.";
            case REPLACE_WORKER -> "Choose another pending applicant for the available work slot.";
        };
    }

    public List<ApplicationAllowedActionDTO> resolveApplicantActions(QuestApplication application) {
        if (application == null || application.getStatus() != QuestApplicationStatus.PENDING) {
            return List.of();
        }

        return List.of(ApplicationAllowedActionDTO.EDIT, ApplicationAllowedActionDTO.WITHDRAW);
    }

    public List<ApplicationAllowedActionDTO> resolveManagementActions(QuestApplication application) {
        if (application == null) {
            return List.of();
        }
        if (application.getStatus() == QuestApplicationStatus.PENDING) {
            return List.of(ApplicationAllowedActionDTO.APPROVE, ApplicationAllowedActionDTO.DECLINE);
        }
        if (application.getStatus() == QuestApplicationStatus.APPROVED) {
            return List.of(ApplicationAllowedActionDTO.RELEASE_WORKER, ApplicationAllowedActionDTO.REPLACE_WORKER);
        }
        return List.of();
    }
}
