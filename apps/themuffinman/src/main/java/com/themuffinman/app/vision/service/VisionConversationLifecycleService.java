package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionConversationListResponseDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisionConversationLifecycleService {

    private final VisionConversationService visionConversationService;

    public VisionConversationLifecycleService(VisionConversationService visionConversationService) {
        this.visionConversationService = visionConversationService;
    }

    @Transactional(readOnly = true)
    public VisionConversationTurnResponseDTO loadConversation(Long conversationId, AppUser currentUser) {
        return visionConversationService.loadConversation(conversationId, currentUser);
    }

    @Transactional(readOnly = true)
    public VisionConversationListResponseDTO listRecentConversations(AppUser currentUser) {
        return visionConversationService.listRecentConversations(currentUser);
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
