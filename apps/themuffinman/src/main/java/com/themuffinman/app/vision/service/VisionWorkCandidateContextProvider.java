package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.workmarket.dto.QuestAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestReadService;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class VisionWorkCandidateContextProvider implements VisionCandidateContextProvider {
    private static final Set<VisionIntent> SUPPORTED_INTENTS = EnumSet.of(
            VisionIntent.VIEW_MY_WORK,
            VisionIntent.VIEW_QUEST_DETAIL,
            VisionIntent.DISCOVER_QUESTS,
            VisionIntent.CREATE_APPLICATION,
            VisionIntent.UPDATE_QUEST,
            VisionIntent.REOPEN_QUEST,
            VisionIntent.CANCEL_QUEST,
            VisionIntent.PAUSE_QUEST,
            VisionIntent.RESUME_QUEST,
            VisionIntent.RELEASE_WORKER,
            VisionIntent.REPLACE_WORKER
    );

    private final WorkmarketQuestReadService questReadService;

    public VisionWorkCandidateContextProvider(WorkmarketQuestReadService questReadService) {
        this.questReadService = questReadService;
    }

    @Override
    public boolean supports(VisionIntent intent) {
        return SUPPORTED_INTENTS.contains(intent);
    }

    @Override
    public VisionCandidateContext build(AppUser currentUser, VisionIntent intent, String requestId, String rawPrompt) {
        boolean owned = intent == VisionIntent.VIEW_MY_WORK
                || intent == VisionIntent.UPDATE_QUEST
                || intent == VisionIntent.REOPEN_QUEST
                || intent == VisionIntent.CANCEL_QUEST
                || intent == VisionIntent.PAUSE_QUEST
                || intent == VisionIntent.RESUME_QUEST
                || intent == VisionIntent.RELEASE_WORKER
                || intent == VisionIntent.REPLACE_WORKER;
        boolean applyableOnly = intent == VisionIntent.CREATE_APPLICATION;
        String scope = owned ? "current_user_owned_quests" : "visible_quests";
        List<VisionCandidateItem> items = questReadService.getAllQuestResponses(currentUser).stream()
                .filter(quest -> !owned || currentUser.getId().equals(quest.getCreatorId()))
                .filter(quest -> !applyableOnly || hasAction(quest, QuestAllowedActionDTO.APPLY))
                .map(this::toCandidate)
                .toList();
        return VisionCandidateContextBuilder.completeOrPartial("quest", scope, requestId, items, "authorized_quest_read_model");
    }

    private VisionCandidateItem toCandidate(QuestResponseDTO quest) {
        return VisionCandidateItem.builder()
                .stableCandidateId("quest:" + quest.getId())
                .family("quest")
                .titleOrLabel(quest.getTitle())
                .compactSummary(quest.getDescription())
                .searchableFields(Map.of(
                        "title", safe(quest.getTitle()),
                        "description", safe(quest.getDescription()),
                        "creator", safe(quest.getCreatorUsername())
                ))
                .viewerSafeMetadata(Map.of(
                        "status", quest.getStatus() == null ? "" : quest.getStatus().name(),
                        "scheduledAt", quest.getScheduledAt() == null ? "" : quest.getScheduledAt().toString()
                ))
                .allowedActionHints(List.of("OPEN"))
                .build();
    }

    private boolean hasAction(QuestResponseDTO quest, QuestAllowedActionDTO action) {
        return quest.getAllowedActions() != null && quest.getAllowedActions().contains(action);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
