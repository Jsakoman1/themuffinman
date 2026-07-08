package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationPresentationDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
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

    public List<ApplicationAllowedActionDTO> resolveApplicantActions(QuestApplication application) {
        if (application == null || application.getStatus() != QuestApplicationStatus.PENDING) {
            return List.of();
        }

        return List.of(ApplicationAllowedActionDTO.EDIT, ApplicationAllowedActionDTO.WITHDRAW);
    }

    public List<ApplicationAllowedActionDTO> resolveManagementActions(QuestApplication application) {
        if (application == null || application.getStatus() != QuestApplicationStatus.PENDING) {
            return List.of();
        }

        return List.of(ApplicationAllowedActionDTO.APPROVE, ApplicationAllowedActionDTO.DECLINE);
    }
}
