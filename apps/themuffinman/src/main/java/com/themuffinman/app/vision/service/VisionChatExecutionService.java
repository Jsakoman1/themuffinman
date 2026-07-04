package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
import com.themuffinman.app.chat.dto.ChatOpenConversationRequestDTO;
import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VisionChatExecutionService {

    private static final Pattern TARGET_USER_PATTERN = Pattern.compile(
            "(?i)(?:chat with|open chat with|start chat with|message|send a message to|dm|direct message|talk to)\\s+(.+?)\\s*$"
    );

    private final ChatService chatService;
    private final AppUserRepository appUserRepository;

    public VisionChatExecutionService(ChatService chatService, AppUserRepository appUserRepository) {
        this.chatService = chatService;
        this.appUserRepository = appUserRepository;
    }

    public VisionChatExecutionResult openChat(AppUser currentUser, String prompt, VisionSemanticPlan semanticPlan) {
        if (currentUser == null) {
            return VisionChatExecutionResult.blocked("Current user is required.");
        }

        String targetQuery = resolveTargetQuery(prompt, semanticPlan);
        if (targetQuery.isBlank()) {
            return VisionChatExecutionResult.blocked("Who should I open chat with?");
        }

        List<AppUser> matches = resolveTargetUserMatches(currentUser, targetQuery);
        if (matches.isEmpty()) {
            return VisionChatExecutionResult.blocked("I could not identify a chat contact for \"" + targetQuery + "\".");
        }
        if (matches.size() > 1) {
            return VisionChatExecutionResult.blocked(buildAmbiguousTargetMessage(targetQuery, matches));
        }

        AppUser targetUser = matches.getFirst();

        try {
            ChatConversationSummaryDTO conversation = chatService.openConversation(
                    ChatOpenConversationRequestDTO.builder()
                            .otherUserId(targetUser.getId())
                            .build(),
                    currentUser
            );
            return VisionChatExecutionResult.executed(conversation);
        } catch (RuntimeException exception) {
            String message = exception.getMessage();
            if (message == null || message.isBlank()) {
                message = "I could not open the chat.";
            }
            return VisionChatExecutionResult.blocked(message);
        }
    }

    private String resolveTargetQuery(String prompt, VisionSemanticPlan semanticPlan) {
        if (semanticPlan != null && semanticPlan.getTargetUserQuery() != null && !semanticPlan.getTargetUserQuery().isBlank()) {
            return semanticPlan.getTargetUserQuery().trim();
        }
        String normalizedPrompt = prompt == null ? "" : prompt.trim();
        Matcher matcher = TARGET_USER_PATTERN.matcher(normalizedPrompt);
        if (matcher.find()) {
            return cleanupTargetQuery(matcher.group(1));
        }
        return cleanupTargetQuery(normalizedPrompt);
    }

    private String cleanupTargetQuery(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value
                .replaceAll("(?i)\\b(the|a|an|to|with|please|user|users|person|people|profile|profiles|contact|contacts|member|members|account|accounts)\\b", " ")
                .replaceAll("[,.;!?]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return cleaned;
    }

    private List<AppUser> resolveTargetUserMatches(AppUser currentUser, String targetQuery) {
        String normalizedTargetQuery = targetQuery.toLowerCase(Locale.ROOT).trim();
        List<AppUser> matches = appUserRepository.searchByUsernameOrEmail(normalizedTargetQuery).stream()
                .filter(candidate -> candidate != null && !candidate.getId().equals(currentUser.getId()))
                .toList();
        if (matches.size() <= 1) {
            return matches;
        }
        for (AppUser candidate : matches) {
            if (candidate.getUsername() != null && candidate.getUsername().equalsIgnoreCase(targetQuery)) {
                return List.of(candidate);
            }
            if (candidate.getEmail() != null && candidate.getEmail().equalsIgnoreCase(targetQuery)) {
                return List.of(candidate);
            }
        }
        return matches;
    }

    private String buildAmbiguousTargetMessage(String targetQuery, List<AppUser> matches) {
        String suggestions = matches.stream()
                .limit(3)
                .map(candidate -> candidate.getUsername() == null || candidate.getUsername().isBlank()
                        ? candidate.getEmail()
                        : candidate.getUsername())
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching contacts");
        return "I found several possible chat contacts for \"" + targetQuery + "\": " + suggestions
                + ". Say the exact username or email.";
    }
}
