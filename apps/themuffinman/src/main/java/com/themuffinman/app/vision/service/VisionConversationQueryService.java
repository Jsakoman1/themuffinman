package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.vision.dto.VisionConversationListResponseDTO;
import com.themuffinman.app.vision.dto.VisionConversationTurnResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisionConversationQueryService {

    private final VisionConversationService visionConversationService;
    private final VisionConversationReadModelAssembler visionConversationReadModelAssembler;

    public VisionConversationQueryService(
            VisionConversationService visionConversationService,
            VisionConversationReadModelAssembler visionConversationReadModelAssembler
    ) {
        this.visionConversationService = visionConversationService;
        this.visionConversationReadModelAssembler = visionConversationReadModelAssembler;
    }

    @Transactional(readOnly = true)
    public VisionConversationTurnResponseDTO loadConversation(Long conversationId, AppUser currentUser) {
        if (conversationId == null || conversationId <= 0) {
            throw new IllegalArgumentException("Conversation id must be positive");
        }
        return visionConversationService.loadConversation(conversationId, currentUser);
    }

    @Transactional(readOnly = true)
    public VisionConversationListResponseDTO listRecentConversations(AppUser currentUser) {
        if (currentUser == null) {
            throw ServiceErrors.forbidden("Authentication is required");
        }
        return VisionConversationListResponseDTO.builder()
                .items(visionConversationReadModelAssembler.recentConversationSummaries(currentUser))
                .build();
    }
}
