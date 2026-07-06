package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisionConversationLifecycleService {

    private final VisionConversationService visionConversationService;

    public VisionConversationLifecycleService(VisionConversationService visionConversationService) {
        this.visionConversationService = visionConversationService;
    }

    @Transactional
    public VisionConversationTurnResponseDTO resetConversation(Long conversationId, AppUser currentUser) {
        return visionConversationService.resetConversation(conversationId, currentUser);
    }

    @Transactional
    public VisionConversationTurnResponseDTO cancelConversation(Long conversationId, AppUser currentUser) {
        return visionConversationService.cancelConversation(conversationId, currentUser);
    }
}
