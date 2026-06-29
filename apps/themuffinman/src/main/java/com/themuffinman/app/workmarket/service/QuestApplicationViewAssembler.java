package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.ApplicationAllowedAction;
import com.themuffinman.app.workmarket.dto.QuestApplicationPresentationDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.mapper.QuestApplicationMgr;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestApplicationViewAssembler {

    private final QuestApplicationMgr questApplicationMgr;
    private final WorkmarketPresentationHelper presentationHelper;

    public QuestApplicationResponseDTO toApplicantResponse(QuestApplication application) {
        return withAllowedActions(questApplicationMgr.toDto(application), resolveApplicantActions(application));
    }

    public QuestApplicationResponseDTO toManagementResponse(QuestApplication application) {
        return withAllowedActions(questApplicationMgr.toDto(application), resolveManagementActions(application));
    }

    public QuestApplicationResponseDTO toPublicResponse(QuestApplication application) {
        return withAllowedActions(questApplicationMgr.toDto(application), List.of());
    }

    public QuestApplicationResponseDTO toViewerResponse(QuestApplication application, AppUser currentUser) {
        if (application == null) {
            return null;
        }

        if (currentUser != null && application.getApplicant() != null && currentUser.getId().equals(application.getApplicant().getId())) {
            return toApplicantResponse(application);
        }

        return toPublicResponse(application);
    }

    private QuestApplicationResponseDTO withAllowedActions(
            QuestApplicationResponseDTO dto,
            List<ApplicationAllowedAction> allowedActions
    ) {
        if (dto == null) {
            return null;
        }

        dto.setAllowedActions(List.copyOf(allowedActions));
        boolean canApprove = allowedActions.contains(ApplicationAllowedAction.APPROVE);
        boolean canDecline = allowedActions.contains(ApplicationAllowedAction.DECLINE);
        dto.setPresentation(QuestApplicationPresentationDTO.builder()
                .statusLabel(presentationHelper.formatApplicationStatus(dto.getStatus()))
                .statusBadgeClass(presentationHelper.badgeClassForApplicationStatus(dto.getStatus()))
                .statusSurfaceClass(presentationHelper.surfaceClassForApplicationStatus(dto.getStatus()))
                .questStatusLabel(presentationHelper.formatQuestStatus(dto.getQuestStatus()))
                .questStatusBadgeClass(presentationHelper.badgeClassForQuestStatus(dto.getQuestStatus()))
                .questAssigneeTargetVisible(presentationHelper.showAssigneeTarget(dto.getQuestAssigneeTarget()))
                .questAssigneeTargetLabel(presentationHelper.formatAssigneeTarget(dto.getQuestAssigneeTarget()))
                .canEdit(allowedActions.contains(ApplicationAllowedAction.EDIT))
                .canWithdraw(allowedActions.contains(ApplicationAllowedAction.WITHDRAW))
                .autoOpenEditForm(allowedActions.contains(ApplicationAllowedAction.EDIT))
                .canApprove(canApprove)
                .canDecline(canDecline)
                .showManagementActions(canApprove || canDecline)
                .build());
        return dto;
    }

    private List<ApplicationAllowedAction> resolveApplicantActions(QuestApplication application) {
        if (application.getStatus() != QuestApplicationStatus.PENDING) {
            return List.of();
        }

        return List.of(ApplicationAllowedAction.EDIT, ApplicationAllowedAction.WITHDRAW);
    }

    private List<ApplicationAllowedAction> resolveManagementActions(QuestApplication application) {
        if (application.getStatus() != QuestApplicationStatus.PENDING) {
            return List.of();
        }

        return List.of(ApplicationAllowedAction.APPROVE, ApplicationAllowedAction.DECLINE);
    }
}
