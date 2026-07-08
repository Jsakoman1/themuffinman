package com.themuffinman.app.agent.service;

import com.themuffinman.app.common.normalization.TextValueNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AdminSyntheticQuestExecutionPlanner {

    private static final Pattern COUNT_PATTERN = Pattern.compile("\\b(\\d{1,3})\\b");
    private static final Pattern TARGET_USER_PATTERN = Pattern.compile(
            "(?i)\\bfor user\\s+[\"']?(.+?)(?:(?:\\s+\\b(?:about|regarding|around)\\b)|[\\n,]|$)"
    );
    private static final Pattern TOPIC_PATTERN = Pattern.compile("(?i)\\b(?:about|regarding|around)\\s+[\"']?([^\"'\\n]+)[\"']?");

    public AdminSyntheticQuestExecutionPlan plan(AdminAgentPromptTranslation translation) {
        String translatedPrompt = translation.getTranslatedPrompt() == null ? "" : translation.getTranslatedPrompt().trim();
        String normalizedPrompt = TextValueNormalizer.lowerToEmpty(translatedPrompt);
        List<String> blockingReasons = new ArrayList<>();

        if (!mentionsSyntheticQuestBatch(normalizedPrompt)) {
            blockingReasons.add("Only synthetic quest batch generation is directly executable in this phase.");
        }

        String targetUserQuery = extractTargetUserQuery(translatedPrompt);
        if (targetUserQuery == null || targetUserQuery.isBlank()) {
            blockingReasons.add("An exact target user reference is required, for example: for user Josip.");
        }

        return AdminSyntheticQuestExecutionPlan.builder()
                .capabilityId(AdminAgentSurfacePolicy.SYNTHETIC_QUEST_BATCH_CAPABILITY)
                .targetUserQuery(targetUserQuery)
                .requestedCount(extractRequestedCount(normalizedPrompt))
                .topic(extractTopic(translatedPrompt))
                .blockingReasons(List.copyOf(blockingReasons))
                .build();
    }

    private boolean mentionsSyntheticQuestBatch(String normalizedPrompt) {
        return normalizedPrompt.contains("quest")
                && (normalizedPrompt.contains("generate")
                || normalizedPrompt.contains("create")
                || normalizedPrompt.contains("batch")
                || normalizedPrompt.contains("unique")
                || normalizedPrompt.contains("synthetic"));
    }

    private int extractRequestedCount(String normalizedPrompt) {
        Matcher matcher = COUNT_PATTERN.matcher(normalizedPrompt);
        if (!matcher.find()) {
            return 1;
        }
        return Math.max(1, Integer.parseInt(matcher.group(1)));
    }

    private String extractTargetUserQuery(String translatedPrompt) {
        Matcher matcher = TARGET_USER_PATTERN.matcher(translatedPrompt);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).trim();
    }

    private String extractTopic(String translatedPrompt) {
        Matcher matcher = TOPIC_PATTERN.matcher(translatedPrompt);
        if (!matcher.find()) {
            return "General support";
        }
        return matcher.group(1).trim();
    }
}
