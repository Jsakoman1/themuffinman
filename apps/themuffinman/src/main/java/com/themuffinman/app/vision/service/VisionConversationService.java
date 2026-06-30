package com.themuffinman.app.vision.service;

import com.themuffinman.app.agent.dto.AdminAgentPlaygroundResponseDTO;
import com.themuffinman.app.agent.service.AdminAgentPlaygroundService;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.VisionProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionConversationTurnRequestDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import com.themuffinman.app.vision.model.VisionAgentState;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionConversationStatus;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionNextAction;
import com.themuffinman.app.vision.model.VisionTurn;
import com.themuffinman.app.vision.model.VisionTurnSource;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.vision.repository.VisionTurnRepository;
import com.themuffinman.app.workmarket.model.Quest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class VisionConversationService {

    private final VisionConversationRepository visionConversationRepository;
    private final VisionTurnRepository visionTurnRepository;
    private final VisionIntentRouter visionIntentRouter;
    private final VisionSlotService visionSlotService;
    private final VisionClarificationService visionClarificationService;
    private final VisionCanvasAssembler visionCanvasAssembler;
    private final VisionCreateQuestExecutionAdapter visionCreateQuestExecutionAdapter;
    private final AdminAgentPlaygroundService adminAgentPlaygroundService;
    private final VisionProperties visionProperties;

    public VisionConversationService(
            VisionConversationRepository visionConversationRepository,
            VisionTurnRepository visionTurnRepository,
            VisionIntentRouter visionIntentRouter,
            VisionSlotService visionSlotService,
            VisionClarificationService visionClarificationService,
            VisionCanvasAssembler visionCanvasAssembler,
            VisionCreateQuestExecutionAdapter visionCreateQuestExecutionAdapter,
            AdminAgentPlaygroundService adminAgentPlaygroundService,
            VisionProperties visionProperties
    ) {
        this.visionConversationRepository = visionConversationRepository;
        this.visionTurnRepository = visionTurnRepository;
        this.visionIntentRouter = visionIntentRouter;
        this.visionSlotService = visionSlotService;
        this.visionClarificationService = visionClarificationService;
        this.visionCanvasAssembler = visionCanvasAssembler;
        this.visionCreateQuestExecutionAdapter = visionCreateQuestExecutionAdapter;
        this.adminAgentPlaygroundService = adminAgentPlaygroundService;
        this.visionProperties = visionProperties;
    }

    @Transactional
    public VisionConversationTurnResponseDTO processTurn(VisionConversationTurnRequestDTO dto, AppUser currentUser) {
        validateAccess(currentUser);
        if (!visionProperties.isEnabled()) {
            throw ServiceErrors.forbidden("Vision conversations are disabled");
        }

        String prompt = requirePrompt(dto);
        AdminAgentPlaygroundResponseDTO analysis = adminAgentPlaygroundService.analyzePrompt(prompt);
        String normalizedPrompt = normalizePrompt(prompt, analysis);

        VisionConversation conversation = loadOrCreateConversation(dto.getConversationId(), currentUser, normalizedPrompt);
        ensureTurnCapacity(conversation);

        VisionTurn turn = switch (conversation.getIntent()) {
            case CREATE_QUEST -> handleCreateQuestTurn(conversation, prompt, normalizedPrompt, analysis, dto.getSource());
            case UNSUPPORTED -> handleUnsupportedTurn(conversation, prompt, normalizedPrompt, analysis, dto.getSource());
        };

        return visionCanvasAssembler.assemble(conversation, turn);
    }

    private VisionConversation loadOrCreateConversation(Long conversationId, AppUser currentUser, String normalizedPrompt) {
        if (conversationId != null) {
            return visionConversationRepository.findByIdAndOwner(conversationId, currentUser)
                    .orElseThrow(() -> ServiceErrors.notFound("Vision conversation was not found"));
        }

        VisionConversation conversation = new VisionConversation();
        conversation.setOwner(currentUser);
        conversation.setIntent(visionIntentRouter.detectIntent(normalizedPrompt));
        conversation.setStatus(conversation.getIntent() == VisionIntent.UNSUPPORTED
                ? VisionConversationStatus.BLOCKED
                : VisionConversationStatus.ACTIVE);
        conversation.setSlotData(new LinkedHashMap<>());
        return visionConversationRepository.save(conversation);
    }

    private VisionTurn handleCreateQuestTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            AdminAgentPlaygroundResponseDTO analysis,
            String source
    ) {
        if (conversation.getStatus() == VisionConversationStatus.REVIEW_READY) {
            return handleCreateQuestReviewTurn(conversation, prompt, normalizedPrompt, analysis, source);
        }

        Map<String, String> mergedSlots = visionSlotService.mergeCreateQuestSlots(conversation, normalizedPrompt);
        String missingSlot = visionClarificationService.nextMissingCreateQuestSlot(mergedSlots);

        String message;
        VisionAgentState agentState;
        VisionNextAction nextAction;
        VisionConversationStatus status;
        if (missingSlot == null) {
            status = VisionConversationStatus.REVIEW_READY;
            nextAction = VisionNextAction.SHOW_REVIEW;
            agentState = VisionAgentState.REVIEW_READY;
            message = visionProperties.isExecutionEnabled()
                    ? "I have enough to prepare a quest review. Confirm when you want me to create it."
                    : "I have enough for a quest review. Execution is still disabled, so this phase stops at review.";
        } else {
            status = VisionConversationStatus.ACTIVE;
            nextAction = VisionNextAction.ASK_FOR_SLOT;
            agentState = VisionAgentState.ASKING;
            message = visionClarificationService.buildQuestion(missingSlot);
        }

        conversation.setSlotData(mergedSlots);
        conversation.setRequestedSlot(missingSlot);
        conversation.setStatus(status);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, analysis.isPromptTranslationReliable());
        visionConversationRepository.save(conversation);

        VisionTurn turn = new VisionTurn();
        turn.setConversation(conversation);
        turn.setTurnIndex((int) visionTurnRepository.countByConversation(conversation) + 1);
        turn.setSource(VisionTurnSource.from(source));
        turn.setPrompt(prompt);
        turn.setNormalizedPrompt(normalizedPrompt);
        turn.setDetectedIntent(VisionIntent.CREATE_QUEST);
        turn.setAgentState(agentState);
        turn.setNextAction(nextAction);
        turn.setRequestedSlot(missingSlot);
        turn.setTranslationApplied(analysis.isPromptTranslationApplied());
        turn.setTranslationReliable(analysis.isPromptTranslationReliable());
        turn.setAssistantMessage(message);
        return visionTurnRepository.save(turn);
    }

    private VisionTurn handleCreateQuestReviewTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            AdminAgentPlaygroundResponseDTO analysis,
            String source
    ) {
        String message;
        VisionAgentState agentState;
        VisionNextAction nextAction;
        VisionConversationStatus status;

        if (isConfirmationPrompt(normalizedPrompt)) {
            if (!visionProperties.isExecutionEnabled()) {
                status = VisionConversationStatus.REVIEW_READY;
                nextAction = VisionNextAction.SHOW_REVIEW;
                agentState = VisionAgentState.REVIEW_READY;
                message = "Execution is still disabled. The quest review stays ready until that flag is enabled.";
            } else {
                Quest createdQuest = visionCreateQuestExecutionAdapter.execute(conversation.getSlotData(), conversation.getOwner());
                conversation.getSlotData().put("created_quest_id", createdQuest.getId().toString());
                status = VisionConversationStatus.COMPLETED;
                nextAction = VisionNextAction.COMPLETE;
                agentState = VisionAgentState.COMPLETE;
                message = "Quest created successfully. The first real `/vision` executor completed this task.";
            }
        } else {
            status = VisionConversationStatus.REVIEW_READY;
            nextAction = VisionNextAction.SHOW_REVIEW;
            agentState = VisionAgentState.REVIEW_READY;
            message = visionProperties.isExecutionEnabled()
                    ? "The review is ready. Confirm to create the quest, or tell me what should change."
                    : "The review is ready, but execution is still disabled. Tell me what should change if you want to revise it.";
        }

        conversation.setRequestedSlot(null);
        conversation.setStatus(status);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, analysis.isPromptTranslationReliable());
        visionConversationRepository.save(conversation);

        VisionTurn turn = new VisionTurn();
        turn.setConversation(conversation);
        turn.setTurnIndex((int) visionTurnRepository.countByConversation(conversation) + 1);
        turn.setSource(VisionTurnSource.from(source));
        turn.setPrompt(prompt);
        turn.setNormalizedPrompt(normalizedPrompt);
        turn.setDetectedIntent(VisionIntent.CREATE_QUEST);
        turn.setAgentState(agentState);
        turn.setNextAction(nextAction);
        turn.setRequestedSlot(null);
        turn.setTranslationApplied(analysis.isPromptTranslationApplied());
        turn.setTranslationReliable(analysis.isPromptTranslationReliable());
        turn.setAssistantMessage(message);
        return visionTurnRepository.save(turn);
    }

    private VisionTurn handleUnsupportedTurn(
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            AdminAgentPlaygroundResponseDTO analysis,
            String source
    ) {
        String message = "This Phase 1 vision backend only supports stepwise create_quest planning for now.";
        conversation.setStatus(VisionConversationStatus.BLOCKED);
        conversation.setRequestedSlot(null);
        updateConversationMetadata(conversation, prompt, normalizedPrompt, message, analysis.isPromptTranslationReliable());
        visionConversationRepository.save(conversation);

        VisionTurn turn = new VisionTurn();
        turn.setConversation(conversation);
        turn.setTurnIndex((int) visionTurnRepository.countByConversation(conversation) + 1);
        turn.setSource(VisionTurnSource.from(source));
        turn.setPrompt(prompt);
        turn.setNormalizedPrompt(normalizedPrompt);
        turn.setDetectedIntent(VisionIntent.UNSUPPORTED);
        turn.setAgentState(VisionAgentState.BLOCKED);
        turn.setNextAction(VisionNextAction.BLOCKED);
        turn.setRequestedSlot(null);
        turn.setTranslationApplied(analysis.isPromptTranslationApplied());
        turn.setTranslationReliable(analysis.isPromptTranslationReliable());
        turn.setAssistantMessage(message);
        return visionTurnRepository.save(turn);
    }

    private void updateConversationMetadata(
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
        conversation.setUpdatedAt(now);
        conversation.setLastTurnAt(now);
    }

    private void ensureTurnCapacity(VisionConversation conversation) {
        long turnCount = visionTurnRepository.countByConversation(conversation);
        if (turnCount >= visionProperties.getMaxTurnsPerConversation()) {
            throw ServiceErrors.conflict("Vision conversation reached the maximum number of turns");
        }
    }

    private String normalizePrompt(String prompt, AdminAgentPlaygroundResponseDTO analysis) {
        if (analysis.getTranslatedPrompt() != null && !analysis.getTranslatedPrompt().isBlank()) {
            return analysis.getTranslatedPrompt().trim();
        }
        return prompt;
    }

    private String requirePrompt(VisionConversationTurnRequestDTO dto) {
        if (dto == null || dto.getPrompt() == null || dto.getPrompt().isBlank()) {
            throw ServiceErrors.badRequest("Prompt is required");
        }
        String prompt = dto.getPrompt().trim();
        if (prompt.length() > visionProperties.getMaxPromptLength()) {
            throw ServiceErrors.badRequest("Prompt is too long");
        }
        return prompt;
    }

    private void validateAccess(AppUser currentUser) {
        if (currentUser == null) {
            throw ServiceErrors.forbidden("Authentication is required");
        }
    }

    private boolean isConfirmationPrompt(String prompt) {
        String lower = prompt.toLowerCase(Locale.ROOT).trim();
        return lower.equals("confirm")
                || lower.equals("yes")
                || lower.equals("yes confirm")
                || lower.equals("go ahead")
                || lower.equals("create it")
                || lower.equals("create the quest")
                || lower.equals("submit");
    }
}
