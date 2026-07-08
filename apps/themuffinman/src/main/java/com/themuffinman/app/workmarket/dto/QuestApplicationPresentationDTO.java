package com.themuffinman.app.workmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestApplicationPresentationDTO {
    private String statusLabel;
    private String statusBadgeClass;
    private String statusSurfaceClass;
    private String questStatusLabel;
    private String questStatusBadgeClass;
    private boolean questAssigneeTargetVisible;
    private String questAssigneeTargetLabel;
    private boolean canEdit;
    private boolean canWithdraw;
    private boolean autoOpenEditForm;
    private boolean canApprove;
    private boolean canDecline;
    private boolean showManagementActions;
}
