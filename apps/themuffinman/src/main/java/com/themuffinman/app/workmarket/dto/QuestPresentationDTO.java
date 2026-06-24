package com.themuffinman.app.workmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestPresentationDTO {
    private String statusLabel;
    private String statusBadgeClass;
    private String statusSurfaceClass;
    private String termLabel;
    private String timeTypeLabel;
    private String audienceLabel;
    private boolean assigneeTargetVisible;
    private String assigneeTargetLabel;
    @Nullable
    private String pendingTermLabel;
    private boolean canEdit;
    private boolean canApply;
    private boolean canViewApplications;
    private boolean autoOpenEditForm;
    private boolean termChangeVisible;
    private boolean termChangeActionable;
    private boolean applicationSentVisible;
    private boolean canOpenMyApplication;
    private boolean deleteVisible;
    private boolean reopenedBadgeVisible;
    private boolean awaitingConfirmationBadgeVisible;
    @Nullable
    private QuestDetailExecutionAction primaryExecutionAction;
    @Nullable
    private String executionHelperText;
}
