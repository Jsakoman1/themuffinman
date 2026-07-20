package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationReadService;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class VisionApplicationCandidateContextProvider implements VisionCandidateContextProvider {
    private static final Set<VisionIntent> SUPPORTED_INTENTS = EnumSet.of(
            VisionIntent.VIEW_APPLICATIONS,
            VisionIntent.VIEW_APPLICATION_DETAIL,
            VisionIntent.UPDATE_APPLICATION,
            VisionIntent.WITHDRAW_APPLICATION
    );

    private final WorkmarketQuestApplicationReadService applicationReadService;

    public VisionApplicationCandidateContextProvider(WorkmarketQuestApplicationReadService applicationReadService) {
        this.applicationReadService = applicationReadService;
    }

    @Override
    public boolean supports(VisionIntent intent) {
        return SUPPORTED_INTENTS.contains(intent);
    }

    @Override
    public VisionCandidateContext build(AppUser currentUser, VisionIntent intent, String requestId, String rawPrompt) {
        List<VisionCandidateItem> items = applicationReadService.getApplicationsForApplicant(currentUser).stream()
                .map(this::toCandidate)
                .toList();
        return VisionCandidateContextBuilder.completeOrPartial(
                "application",
                "current_user_applications",
                requestId,
                items,
                "authorized_application_read_model_complete"
        );
    }

    private VisionCandidateItem toCandidate(QuestApplicationResponseDTO application) {
        return VisionCandidateItem.builder()
                .stableCandidateId("application:" + application.getId())
                .family("application")
                .titleOrLabel(application.getQuestTitle())
                .compactSummary(application.getQuestDescription())
                .searchableFields(Map.of(
                        "questTitle", safe(application.getQuestTitle()),
                        "questDescription", safe(application.getQuestDescription()),
                        "creator", safe(application.getQuestCreatorUsername())
                ))
                .viewerSafeMetadata(Map.of(
                        "status", application.getStatus() == null ? "" : application.getStatus().name(),
                        "questId", application.getQuestId() == null ? "" : application.getQuestId().toString()
                ))
                .allowedActionHints(List.of("OPEN"))
                .build();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
