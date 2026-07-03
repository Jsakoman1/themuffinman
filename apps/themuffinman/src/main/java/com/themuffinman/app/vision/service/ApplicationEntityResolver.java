package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import com.themuffinman.app.semantic.SemanticEntityResolution;
import com.themuffinman.app.semantic.SemanticEntityResolutionStatus;
import com.themuffinman.app.semantic.SemanticEntityResolutionSupport;
import com.themuffinman.app.semantic.VisionEntityResolver;
import com.themuffinman.app.vision.dto.ApplicationAllowedActionDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationEntityResolver implements VisionEntityResolver<VisionResolvedApplicationTarget> {

    private final VisionCapabilityPreviewService visionCapabilityPreviewService;

    public ApplicationEntityResolver(VisionCapabilityPreviewService visionCapabilityPreviewService) {
        this.visionCapabilityPreviewService = visionCapabilityPreviewService;
    }

    @Override
    public SemanticEntityFamily family() {
        return SemanticEntityFamily.APPLICATION;
    }

    @Override
    public SemanticEntityResolution<VisionResolvedApplicationTarget> resolve(AppUser currentUser, String targetEntityQuery) {
        VisionResolvedApplicationTarget target = visionCapabilityPreviewService.resolveMyPendingApplication(
                currentUser,
                targetEntityQuery,
                ApplicationAllowedActionDTO.EDIT
        );
        return SemanticEntityResolution.<VisionResolvedApplicationTarget>builder()
                .entityFamily(SemanticEntityFamily.APPLICATION)
                .status(target != null && target.resolved() ? SemanticEntityResolutionStatus.RESOLVED : inferStatus(target))
                .targetEntityQuery(targetEntityQuery)
                .entity(target)
                .canonicalLabel(target == null ? null : target.questTitle())
                .ambiguityReason(target == null ? "No application resolver result." : target.blockingMessage())
                .confidence(SemanticEntityResolutionSupport.confidenceForResolution(
                        target != null && target.resolved() ? SemanticEntityResolutionStatus.RESOLVED : inferStatus(target),
                        targetEntityQuery,
                        target == null ? null : target.questTitle(),
                        target == null ? "No application resolver result." : target.blockingMessage(),
                        List.of()
                ))
                .build();
    }

    private SemanticEntityResolutionStatus inferStatus(VisionResolvedApplicationTarget target) {
        if (target == null || target.blockingMessage() == null) {
            return SemanticEntityResolutionStatus.NOT_FOUND;
        }
        return target.blockingMessage().toLowerCase().contains("several")
                ? SemanticEntityResolutionStatus.AMBIGUOUS
                : SemanticEntityResolutionStatus.NOT_FOUND;
    }
}
