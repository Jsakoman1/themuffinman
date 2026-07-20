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
public class CircleEntityResolver implements VisionEntityResolver<VisionResolvedCircleTarget> {

    private final VisionCapabilityPreviewService visionCapabilityPreviewService;

    public CircleEntityResolver(VisionCapabilityPreviewService visionCapabilityPreviewService) {
        this.visionCapabilityPreviewService = visionCapabilityPreviewService;
    }

    @Override
    public SemanticEntityFamily family() {
        return SemanticEntityFamily.CIRCLE;
    }

    /**
     * Resolves owned circles through the existing visibility-aware service;
     * unresolved and ambiguous circles remain non-navigable.
     */
    @Override
    public SemanticEntityResolution<VisionResolvedCircleTarget> resolve(AppUser currentUser, String targetEntityQuery) {
        VisionResolvedCircleTarget target = visionCapabilityPreviewService.resolveOwnedCircle(currentUser, targetEntityQuery);
        SemanticEntityResolutionStatus status = target != null && target.resolved()
                ? SemanticEntityResolutionStatus.RESOLVED : inferStatus(target);
        return SemanticEntityResolution.<VisionResolvedCircleTarget>builder()
                .entityFamily(SemanticEntityFamily.CIRCLE)
                .status(status)
                .targetEntityQuery(targetEntityQuery)
                .entity(target)
                .canonicalLabel(target == null ? null : target.circleName())
                .ambiguityReason(target == null ? "No circle resolver result." : target.blockingMessage())
                .confidence(SemanticEntityResolutionSupport.confidenceForResolution(
                        status,
                        targetEntityQuery,
                        target == null ? null : target.circleName(),
                        target == null ? "No circle resolver result." : target.blockingMessage(),
                        List.of()
                ))
                .build();
    }

    private SemanticEntityResolutionStatus inferStatus(VisionResolvedCircleTarget target) {
        if (target == null || target.blockingMessage() == null) {
            return SemanticEntityResolutionStatus.NOT_FOUND;
        }
        return target.blockingMessage().toLowerCase().contains("several")
                ? SemanticEntityResolutionStatus.AMBIGUOUS
                : SemanticEntityResolutionStatus.NOT_FOUND;
    }
}
