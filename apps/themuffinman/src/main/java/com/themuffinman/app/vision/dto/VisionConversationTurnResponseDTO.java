package com.themuffinman.app.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionConversationTurnResponseDTO {
    private Long conversationId;
    private Long turnId;
    private String intent;
    private String agentState;
    private String canvasMode;
    private String nextAction;
    private String workflowState;
    private List<String> allowedActions;
    private String message;
    private String requestedSlot;
    private String normalizedPrompt;
    private boolean translationApplied;
    private boolean translationReliable;
    private boolean executionEnabled;
    private VisionRuntimeContextDTO runtimeContext;
    private VisionWorkspaceHandoffDTO workspaceHandoff;
    private VisionWebActionDTO webAction;
    private VisionExecutionCandidateDTO executionCandidate;
    private VisionQuestDiscoveryDTO questDiscovery;
    private VisionSearchDiscoveryDTO searchDiscovery;
    private VisionSearchComparisonDTO searchComparison;
    private VisionMemoryTrailDTO memoryTrail;
    private List<VisionCanvasBlockDTO> blocks;
    private List<VisionSlotSummaryDTO> appliedSlotSummaries;
    private List<VisionSlotSummaryDTO> slotSummaries;
    private VisionQuestReviewDTO review;
    private List<VisionConversationSummaryDTO> recentConversations;
}
