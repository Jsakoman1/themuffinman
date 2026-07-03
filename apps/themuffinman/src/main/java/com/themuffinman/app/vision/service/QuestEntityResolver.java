package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import com.themuffinman.app.semantic.SemanticEntityResolution;
import com.themuffinman.app.semantic.SemanticEntityResolutionStatus;
import com.themuffinman.app.semantic.SemanticEntityResolutionSupport;
import com.themuffinman.app.semantic.VisionEntityResolver;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestEntityResolver implements VisionEntityResolver<VisionResolvedQuestTarget> {

    private final VisionCapabilityPreviewService visionCapabilityPreviewService;

    public QuestEntityResolver(VisionCapabilityPreviewService visionCapabilityPreviewService) {
        this.visionCapabilityPreviewService = visionCapabilityPreviewService;
    }

    @Override
    public SemanticEntityFamily family() {
        return SemanticEntityFamily.QUEST;
    }

    @Override
    public SemanticEntityResolution<VisionResolvedQuestTarget> resolve(AppUser currentUser, String targetEntityQuery) {
        VisionResolvedQuestTarget target = visionCapabilityPreviewService.resolveVisibleQuest(currentUser, targetEntityQuery);
        return SemanticEntityResolution.<VisionResolvedQuestTarget>builder()
                .entityFamily(SemanticEntityFamily.QUEST)
                .status(target != null && target.resolved() ? SemanticEntityResolutionStatus.RESOLVED : inferStatus(target))
                .targetEntityQuery(targetEntityQuery)
                .entity(target)
                .canonicalLabel(target == null ? null : target.questTitle())
                .ambiguityReason(target == null ? "No quest resolver result." : target.blockingMessage())
                .confidence(SemanticEntityResolutionSupport.confidenceForResolution(
                        target != null && target.resolved() ? SemanticEntityResolutionStatus.RESOLVED : inferStatus(target),
                        targetEntityQuery,
                        target == null ? null : target.questTitle(),
                        target == null ? "No quest resolver result." : target.blockingMessage(),
                        List.of()
                ))
                .build();
    }

    private SemanticEntityResolutionStatus inferStatus(VisionResolvedQuestTarget target) {
        if (target == null || target.blockingMessage() == null) {
            return SemanticEntityResolutionStatus.NOT_FOUND;
        }
        return target.blockingMessage().toLowerCase().contains("several")
                ? SemanticEntityResolutionStatus.AMBIGUOUS
                : SemanticEntityResolutionStatus.NOT_FOUND;
    }
}
