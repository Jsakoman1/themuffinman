package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.VisionConversationListResponseDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisionConversationQueryService {

    private final VisionConversationService visionConversationService;

    public VisionConversationQueryService(VisionConversationService visionConversationService) {
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
}
