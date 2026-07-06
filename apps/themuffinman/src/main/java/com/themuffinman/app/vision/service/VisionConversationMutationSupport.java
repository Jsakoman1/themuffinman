package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.vision.model.VisionAgentState;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionNextAction;
import com.themuffinman.app.vision.model.VisionTurn;
import com.themuffinman.app.vision.model.VisionTurnSource;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.vision.repository.VisionTurnRepository;

import java.time.Instant;

final class VisionConversationMutationSupport {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().findAndAddModules().build();

    private final VisionConversationRepository visionConversationRepository;
    private final VisionTurnRepository visionTurnRepository;
    private final VisionSemanticOrchestrationContextService visionSemanticOrchestrationContextService;
    private final VisionProperties visionProperties;

    VisionConversationMutationSupport(
            VisionConversationRepository visionConversationRepository,
            VisionTurnRepository visionTurnRepository,
            VisionSemanticOrchestrationContextService visionSemanticOrchestrationContextService,
            VisionProperties visionProperties
    ) {
        this.visionConversationRepository = visionConversationRepository;
        this.visionTurnRepository = visionTurnRepository;
        this.visionSemanticOrchestrationContextService = visionSemanticOrchestrationContextService;
        this.visionProperties = visionProperties;
    }

    void ensureTurnCapacity(VisionConversation conversation) {
        long turnCount = visionTurnRepository.countByConversation(conversation);
        if (turnCount >= visionProperties.getMaxTurnsPerConversation()) {
            throw ServiceErrors.conflict("Vision conversation reached the maximum number of turns");
        }
    }

    VisionTurn createTurn(
            VisionConversation conversation,
            String source,
            String prompt,
            String normalizedPrompt,
            VisionIntent intent,
            VisionAgentState agentState,
            VisionNextAction nextAction,
            String requestedSlot,
            boolean translationApplied,
            boolean translationReliable,
            String assistantMessage
    ) {
        VisionTurn turn = new VisionTurn();
        turn.setConversation(conversation);
        turn.setTurnIndex((int) visionTurnRepository.countByConversation(conversation) + 1);
        turn.setSource(VisionTurnSource.from(source));
        turn.setPrompt(prompt);
        turn.setNormalizedPrompt(normalizedPrompt);
        turn.setDetectedIntent(intent);
        turn.setAgentState(agentState);
        turn.setNextAction(nextAction);
        turn.setRequestedSlot(requestedSlot);
        turn.setTranslationApplied(translationApplied);
        turn.setTranslationReliable(translationReliable);
        turn.setAssistantMessage(assistantMessage);
        return visionTurnRepository.save(turn);
    }

    void updateConversationMetadata(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            String assistantMessage,
            boolean translationReliable
    ) {
        Instant now = Instant.now();
        conversation.setLastUserPrompt(prompt);
        conversation.setLastNormalizedPrompt(normalizedPrompt);
        conversation.setLastAssistantMessage(assistantMessage);
        conversation.setLastTranslationReliable(translationReliable);
        conversation.setSessionMemorySnapshot(serializeSessionMemorySnapshot(conversation));
        conversation.setUpdatedAt(now);
        conversation.setLastTurnAt(now);
    }

    private String serializeSessionMemorySnapshot(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) {
            return null;
        }

        VisionSemanticMemoryContext memoryContext = visionSemanticOrchestrationContextService.buildMemoryContext(conversation.getOwner(), conversation);
        VisionSemanticSessionMemoryContext sessionMemory = memoryContext == null ? null : memoryContext.getSessionMemory();
        if (sessionMemory == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(sessionMemory);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize vision session memory snapshot", exception);
        }
    }
}
