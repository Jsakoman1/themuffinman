package com.themuffinman.app.vision.service;

import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.testing.TestFixtures;
import com.themuffinman.app.vision.model.VisionIntent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VisionChatExecutionServiceTest {

    @Test
    void blocksWhenTargetUserCannotBeResolved() {
        ChatService chatService = mock(ChatService.class);
        AppUserRepository appUserRepository = mock(AppUserRepository.class);
        when(appUserRepository.searchByUsernameOrEmail("josip")).thenReturn(List.of());

        VisionChatExecutionService executionService = new VisionChatExecutionService(chatService, appUserRepository);
        AppUser currentUser = TestFixtures.user(7L, "vision-user");

        VisionChatExecutionResult result = executionService.openChat(
                currentUser,
                "chat with Josip",
                VisionSemanticPlan.builder()
                        .candidateIntent(VisionIntent.OPEN_CHAT.name())
                        .capabilityId("open_chat")
                        .targetUserQuery("Josip")
                        .build()
        );

        assertFalse(result.isExecuted());
        assertEquals("I could not identify a chat contact for \"Josip\".", result.getBlockingReason());
    }

    @Test
    void opensConversationThroughChatServiceWhenTargetIsResolved() {
        ChatService chatService = mock(ChatService.class);
        AppUserRepository appUserRepository = mock(AppUserRepository.class);
        AppUser currentUser = TestFixtures.user(7L, "vision-user");
        AppUser targetUser = TestFixtures.user(8L, "Josip");

        when(appUserRepository.searchByUsernameOrEmail("josip")).thenReturn(List.of(targetUser));
        when(chatService.openConversation(any(), any(AppUser.class))).thenReturn(ChatConversationSummaryDTO.builder()
                .conversationId(301L)
                .otherUserId(targetUser.getId())
                .otherUsername(targetUser.getUsername())
                .resolutionKey("conversation:301")
                .resolutionLabel("Chat with Josip")
                .exactResolutionEligible(true)
                .build());

        VisionChatExecutionService executionService = new VisionChatExecutionService(chatService, appUserRepository);
        VisionChatExecutionResult result = executionService.openChat(
                currentUser,
                "chat with Josip",
                VisionSemanticPlan.builder()
                        .candidateIntent(VisionIntent.OPEN_CHAT.name())
                        .capabilityId("open_chat")
                        .targetUserQuery("Josip")
                        .build()
        );

        assertTrue(result.isExecuted());
        assertEquals(301L, result.getConversation().getConversationId());
        assertEquals("Josip", result.getConversation().getOtherUsername());
    }
}
