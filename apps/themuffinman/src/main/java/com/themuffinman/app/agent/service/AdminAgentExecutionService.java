package com.themuffinman.app.agent.service;

import com.themuffinman.app.agent.dto.AdminAgentExecutionRequestDTO;
import com.themuffinman.app.agent.dto.AdminAgentExecutionResponseDTO;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.location.model.QuestLocationSource;
import com.themuffinman.app.location.model.QuestLocationVisibility;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.service.WorkmarketQuestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminAgentExecutionService {

    private final AgentProperties agentProperties;
    private final AdminAgentSurfacePolicy adminAgentSurfacePolicy;
    private final AdminAgentPromptPreparationService adminAgentPromptPreparationService;
    private final AdminSyntheticQuestExecutionPlanner adminSyntheticQuestExecutionPlanner;
    private final AdminAgentTargetUserResolver adminAgentTargetUserResolver;
    private final WorkmarketQuestService questService;

    public AdminAgentExecutionService(
            AgentProperties agentProperties,
            AdminAgentSurfacePolicy adminAgentSurfacePolicy,
            AdminAgentPromptPreparationService adminAgentPromptPreparationService,
            AdminSyntheticQuestExecutionPlanner adminSyntheticQuestExecutionPlanner,
            AdminAgentTargetUserResolver adminAgentTargetUserResolver,
            WorkmarketQuestService questService
    ) {
        this.agentProperties = agentProperties;
        this.adminAgentSurfacePolicy = adminAgentSurfacePolicy;
        this.adminAgentPromptPreparationService = adminAgentPromptPreparationService;
        this.adminSyntheticQuestExecutionPlanner = adminSyntheticQuestExecutionPlanner;
        this.adminAgentTargetUserResolver = adminAgentTargetUserResolver;
        this.questService = questService;
    }

    @Transactional
    public AdminAgentExecutionResponseDTO execute(AdminAgentExecutionRequestDTO dto, AppUser currentUser) {
        validateAdmin(currentUser);
        if (dto == null || dto.getPrompt() == null || dto.getPrompt().isBlank()) {
            throw ServiceErrors.badRequest("Prompt is required");
        }

        var translation = adminAgentPromptPreparationService.preparePrompt(dto.getPrompt().trim());
        AdminSyntheticQuestExecutionPlan plan = adminSyntheticQuestExecutionPlanner.plan(translation);

        if (!adminAgentSurfacePolicy.canExecuteCapability(plan.getCapabilityId())) {
            return blocked(plan, List.of("Admin direct execution is disabled for this environment."));
        }
        if (!plan.isExecutable()) {
            return blocked(plan, plan.getBlockingReasons());
        }

        AppUser targetUser = adminAgentTargetUserResolver.resolveExactUser(plan.getTargetUserQuery());
        int effectiveCount = Math.min(plan.getRequestedCount(), agentProperties.getAdminQuestBatchLimit());
        List<String> warnings = new ArrayList<>();
        if (effectiveCount < plan.getRequestedCount()) {
            warnings.add("Requested batch size exceeded the configured limit and was capped.");
        }

        List<String> previewTitles = buildQuestTitles(plan.getTopic(), effectiveCount);
        if (!dto.isConfirmed()) {
            List<String> blockingReasons = List.of("Explicit admin confirmation is required before direct execution.");
            return AdminAgentExecutionResponseDTO.builder()
                    .capabilityId(plan.getCapabilityId())
                    .executionEnabled(true)
                    .executed(false)
                    .confirmationRequired(true)
                    .summary("Synthetic quest batch is ready for confirmation.")
                    .targetUserLabel(targetUser.getUsername())
                    .targetUserId(targetUser.getId())
                    .requestedCount(plan.getRequestedCount())
                    .effectiveCount(effectiveCount)
                    .topic(plan.getTopic())
                    .questTitles(previewTitles)
                    .createdQuestIds(List.of())
                    .warnings(List.copyOf(warnings))
                    .blockingReasons(blockingReasons)
                    .build();
        }

        List<Quest> createdQuests = new ArrayList<>();
        for (int index = 0; index < effectiveCount; index++) {
            QuestRequestDTO request = QuestRequestDTO.builder()
                    .title(previewTitles.get(index))
                    .description(buildQuestDescription(plan.getTopic(), targetUser, index + 1, effectiveCount))
                    .awardAmount(BigDecimal.ZERO)
                    .termFixed(false)
                    .audience(QuestAudience.EVERYONE)
                    .creatorId(targetUser.getId())
                    .locationVisibility(QuestLocationVisibility.OFF)
                    .locationSource(QuestLocationSource.PROFILE)
                    .build();
            createdQuests.add(questService.createQuest(request, currentUser));
        }

        warnings.add("Synthetic quests were created through admin-scoped execution.");
        return AdminAgentExecutionResponseDTO.builder()
                .capabilityId(plan.getCapabilityId())
                .executionEnabled(true)
                .executed(true)
                .confirmationRequired(false)
                .summary("Synthetic quest batch executed successfully.")
                .targetUserLabel(targetUser.getUsername())
                .targetUserId(targetUser.getId())
                .requestedCount(plan.getRequestedCount())
                .effectiveCount(effectiveCount)
                .topic(plan.getTopic())
                .questTitles(createdQuests.stream().map(Quest::getTitle).toList())
                .createdQuestIds(createdQuests.stream().map(Quest::getId).toList())
                .warnings(List.copyOf(warnings))
                .blockingReasons(List.of())
                .build();
    }

    private AdminAgentExecutionResponseDTO blocked(AdminSyntheticQuestExecutionPlan plan, List<String> blockingReasons) {
        return AdminAgentExecutionResponseDTO.builder()
                .capabilityId(plan.getCapabilityId())
                .executionEnabled(agentProperties.isAdminExecutionEnabled())
                .executed(false)
                .confirmationRequired(false)
                .summary("Direct execution is not available for this prompt.")
                .targetUserLabel(plan.getTargetUserQuery())
                .requestedCount(plan.getRequestedCount())
                .effectiveCount(Math.min(plan.getRequestedCount(), agentProperties.getAdminQuestBatchLimit()))
                .topic(plan.getTopic())
                .questTitles(List.of())
                .createdQuestIds(List.of())
                .warnings(List.of())
                .blockingReasons(List.copyOf(blockingReasons))
                .build();
    }

    private void validateAdmin(AppUser currentUser) {
        if (currentUser == null || currentUser.getRole() != AppUserRole.ADMIN) {
            throw ServiceErrors.forbidden("Admin access is required");
        }
    }

    private List<String> buildQuestTitles(String topic, int count) {
        String marker = agentProperties.getSyntheticQuestMarker();
        String[] prefixes = new String[] {
                "Handle", "Prepare", "Review", "Coordinate", "Organize",
                "Support", "Inspect", "Document", "Follow up on", "Deliver"
        };
        String[] suffixes = new String[] {
                "intake", "handoff", "schedule", "cleanup", "checklist",
                "update", "summary", "request", "follow-up", "delivery"
        };

        List<String> titles = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            String prefix = prefixes[index % prefixes.length];
            String suffix = suffixes[index % suffixes.length];
            String title = "%s %s %s %s".formatted(marker, prefix, topic, suffix).trim();
            if (index >= prefixes.length) {
                title = title + " " + (index + 1);
            }
            titles.add(title);
        }
        return titles;
    }

    private String buildQuestDescription(String topic, AppUser targetUser, int itemIndex, int totalCount) {
        return """
                %s Synthetic admin-generated quest %d of %d for user %s.
                Focus area: %s.
                This record exists for sandbox/operator testing and should be treated as synthetic.
                """.formatted(
                agentProperties.getSyntheticQuestMarker(),
                itemIndex,
                totalCount,
                targetUser.getUsername(),
                topic
        ).trim();
    }
}
