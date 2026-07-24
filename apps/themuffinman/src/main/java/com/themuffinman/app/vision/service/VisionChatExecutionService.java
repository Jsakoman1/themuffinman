package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
import com.themuffinman.app.chat.dto.ChatOpenConversationRequestDTO;
import com.themuffinman.app.chat.dto.ChatMessageRequestDTO;
import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.common.normalization.TextValueNormalizer;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
public class VisionChatExecutionService {

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
            return VisionChatExecutionResult.blocked("Who should I open chat with? I won't guess on another person's behalf.");
        }

        List<AppUser> matches = resolveTargetUserMatches(currentUser, targetQuery);
        if (matches.isEmpty()) {
            return VisionChatExecutionResult.blocked("I could not identify a chat contact for \"" + targetQuery + "\". I won't open a chat without a clear person.");
        }
        if (matches.size() > 1) {
            return VisionChatExecutionResult.blocked(
                    buildAmbiguousTargetMessage(targetQuery, matches),
                    buildCandidates(matches)
            );
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

    public VisionChatExecutionResult sendMessage(AppUser currentUser, String targetQuery, String messageBody) {
        if (currentUser == null) return VisionChatExecutionResult.blocked("Current user is required.");
        if (messageBody == null || messageBody.isBlank()) return VisionChatExecutionResult.blocked("Message text is required.");
        List<AppUser> matches = resolveTargetUserMatches(currentUser, targetQuery == null ? "" : targetQuery.trim());
        if (matches.isEmpty()) return VisionChatExecutionResult.blocked("I could not identify a chat contact for \"" + targetQuery + "\".");
        if (matches.size() > 1) return VisionChatExecutionResult.blocked(buildAmbiguousTargetMessage(targetQuery, matches), buildCandidates(matches));
        try {
            ChatConversationSummaryDTO conversation = chatService.openConversation(
                    ChatOpenConversationRequestDTO.builder().otherUserId(matches.getFirst().getId()).build(), currentUser);
            var message = chatService.sendMessage(conversation.getConversationId(),
                    ChatMessageRequestDTO.builder().messageBody(messageBody.trim()).build(), currentUser);
            return VisionChatExecutionResult.messageSent(conversation, message);
        } catch (RuntimeException exception) {
            return VisionChatExecutionResult.blocked(exception.getMessage() == null ? "I could not send the message." : exception.getMessage());
        }
    }

    private String resolveTargetQuery(String prompt, VisionSemanticPlan semanticPlan) {
        if (semanticPlan != null) {
            // Production target extraction belongs to the OpenAI semantic plan.
            // Prompt-prefix parsing remains available only for development fixtures.
            if (semanticPlan.getTargetUserQuery() != null && !semanticPlan.getTargetUserQuery().isBlank()) {
                return semanticPlan.getTargetUserQuery().trim();
            }
            if (semanticPlan.getCapabilityId() != null && !semanticPlan.getCapabilityId().isBlank()) {
                return "";
            }
        }
        String normalizedPrompt = prompt == null ? "" : prompt.trim();
        String stripped = VisionPromptTextSupport.extractAfterAnyPrefix(
                normalizedPrompt,
                List.of(
                        "chat with",
                        "open chat with",
                        "start chat with",
                        "open conversation with",
                        "start conversation with",
                        "open dm with",
                        "send dm to",
                        "message",
                        "send a message to",
                        "send message to",
                        "tell",
                        "write to",
                        "dm",
                        "direct message",
                        "talk to",
                        "text"
                )
        );
        String query = stripped == null ? normalizedPrompt : stripped;
        return cleanupTargetQuery(query);
    }

    private String cleanupTargetQuery(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = VisionPromptTextSupport.stripLeadingWords(
                value,
                List.of("the", "a", "an", "to", "with", "please", "user", "users", "person", "people", "profile", "profiles", "contact", "contacts", "member", "members", "account", "accounts")
        );
        if (cleaned == null) {
            return "";
        }
        return cleaned
                .replaceAll("[,.;!?]", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .replaceAll("(?i)\\s+(please|profile|profiles|chat|conversation|dm|message|messages)$", "")
                .trim();
    }

    private List<AppUser> resolveTargetUserMatches(AppUser currentUser, String targetQuery) {
        String normalizedTargetQuery = TextValueNormalizer.lowerTrimToEmpty(targetQuery);
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
                + ". Say the exact username or email, or choose a numbered candidate before I contact anyone.";
    }

    private List<VisionChatTargetCandidate> buildCandidates(List<AppUser> matches) {
        List<VisionChatTargetCandidate> candidates = new ArrayList<>();
        for (AppUser candidate : matches) {
            String value = candidate.getUsername() == null || candidate.getUsername().isBlank()
                    ? candidate.getEmail()
                    : candidate.getUsername();
            if (value == null || value.isBlank()) {
                continue;
            }
            candidates.add(VisionChatTargetCandidate.builder()
                    .value(value)
                    .label(value)
                    .build());
        }
        return List.copyOf(candidates);
    }
}
