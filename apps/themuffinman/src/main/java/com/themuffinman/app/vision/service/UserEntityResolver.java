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
public class UserEntityResolver implements VisionEntityResolver<VisionResolvedUserTarget> {

    private final VisionCapabilityPreviewService visionCapabilityPreviewService;

    public UserEntityResolver(VisionCapabilityPreviewService visionCapabilityPreviewService) {
        this.visionCapabilityPreviewService = visionCapabilityPreviewService;
    }

    @Override
    public SemanticEntityFamily family() {
        return SemanticEntityFamily.USER;
    }

    @Override
    public SemanticEntityResolution<VisionResolvedUserTarget> resolve(AppUser currentUser, String targetEntityQuery) {
        VisionResolvedUserTarget target = visionCapabilityPreviewService.resolveUserProfileTarget(currentUser, targetEntityQuery);
        return SemanticEntityResolution.<VisionResolvedUserTarget>builder()
                .entityFamily(SemanticEntityFamily.USER)
                .status(target != null && target.resolved() ? SemanticEntityResolutionStatus.RESOLVED : inferStatus(target))
                .targetEntityQuery(targetEntityQuery)
                .entity(target)
                .canonicalLabel(target == null ? null : target.username())
                .ambiguityReason(target == null ? "No user resolver result." : target.blockingMessage())
                .confidence(SemanticEntityResolutionSupport.confidenceForResolution(
                        target != null && target.resolved() ? SemanticEntityResolutionStatus.RESOLVED : inferStatus(target),
                        targetEntityQuery,
                        target == null ? null : target.username(),
                        target == null ? "No user resolver result." : target.blockingMessage(),
                        List.of()
                ))
                .build();
    }

    private SemanticEntityResolutionStatus inferStatus(VisionResolvedUserTarget target) {
        if (target == null || target.blockingMessage() == null) {
            return SemanticEntityResolutionStatus.NOT_FOUND;
        }
        return target.blockingMessage().toLowerCase().contains("several")
                ? SemanticEntityResolutionStatus.AMBIGUOUS
                : SemanticEntityResolutionStatus.NOT_FOUND;
    }
}
