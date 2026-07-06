package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionIntent;
import com.themuffinman.app.vision.model.VisionTurn;

final class VisionReadOnlyConversationTurnHandler {

    VisionTurn handleViewProfileTurn(
            VisionConversationService service,
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handle(service, conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.VIEW_PROFILE);
    }

    VisionTurn handleViewChatWorkspaceTurn(
            VisionConversationService service,
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handle(service, conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.VIEW_CHAT_WORKSPACE);
    }

    VisionTurn handleViewSettingsTurn(
            VisionConversationService service,
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handle(service, conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.VIEW_SETTINGS);
    }

    VisionTurn handleViewCirclesTurn(
            VisionConversationService service,
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handle(service, conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.VIEW_CIRCLES);
    }

    VisionTurn handleViewQuestNewsTurn(
            VisionConversationService service,
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handle(service, conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.VIEW_QUEST_NEWS);
    }

    VisionTurn handleViewNotificationsTurn(
            VisionConversationService service,
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handle(service, conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.VIEW_NOTIFICATIONS);
    }

    VisionTurn handleViewApplicationsTurn(
            VisionConversationService service,
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handle(service, conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.VIEW_APPLICATIONS);
    }

    VisionTurn handleViewThingsTurn(
            VisionConversationService service,
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source
    ) {
        return handle(service, conversation, prompt, normalizedPrompt, understanding, source, VisionIntent.VIEW_THINGS);
    }

    private VisionTurn handle(
            VisionConversationService service,
            VisionConversation conversation,
            String prompt,
            String normalizedPrompt,
            VisionPromptUnderstandingResult understanding,
            String source,
            VisionIntent intent
    ) {
        return service.handleReadOnlySnapshotTurn(
                conversation,
                prompt,
                normalizedPrompt,
                understanding,
                source,
                intent,
                VisionConversationSnapshotSupport.readOnlySnapshotMessage(intent)
        );
    }
}
