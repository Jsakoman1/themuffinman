package com.themuffinman.app.workmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestPresentationDTO {
    private String statusLabel;
    private String statusBadgeClass;
    private String statusSurfaceClass;
    private String timeTypeLabel;
    private String audienceLabel;
    @Nullable
    private String locationLabel;
    @Nullable
    private String locationSourceSummary;
    @Nullable
    private String locationVisibilitySummary;
    private boolean assigneeTargetVisible;
    private String assigneeTargetLabel;
    @Nullable
    private String slotProgressLabel;
    @Nullable
    private String remainingSlotsLabel;
    private boolean approvedApplicantsVisible;
    private boolean canEdit;
    private boolean canApply;
    private boolean canViewApplications;
    private boolean canManuallyAssign;
    @Nullable
    private BigDecimal suggestedApplicationPrice;
    @Nullable
    private QuestApplicationDraftRulesDTO applicationDraftRules;
    private boolean offerSectionVisible;
    private boolean applicationsSectionVisible;
    private boolean myApplicationAsideVisible;
    private boolean overviewStatusVisible;
    @Nullable
    private String primaryExecutionActionLabel;
    @Nullable
    private String termChangeSummaryLabel;
    @Nullable
    private String termChangeConfirmLabel;
    @Nullable
    private String termChangeRejectLabel;
    private boolean postingSettingsVisible;
    @Nullable
    private String visibleToCirclesLabel;
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
