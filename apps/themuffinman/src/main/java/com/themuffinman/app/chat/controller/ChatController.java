package com.themuffinman.app.chat.controller;

import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
import com.themuffinman.app.chat.dto.ChatMessageDTO;
import com.themuffinman.app.chat.dto.ChatMessageRequestDTO;
import com.themuffinman.app.chat.dto.ChatOpenConversationRequestDTO;
import com.themuffinman.app.chat.dto.ChatWorkspaceDTO;
import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.common.dto.ActionResults;
import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.identity.model.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/workspace")
    public ChatWorkspaceDTO getWorkspace(@AuthenticationPrincipal AppUser currentUser) {
        return chatService.getWorkspace(currentUser);
    }

    @PostMapping("/conversations/open")
    public ChatConversationSummaryDTO openConversation(
            @Valid @RequestBody ChatOpenConversationRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.openConversation(request, currentUser);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<ChatMessageDTO> getConversationMessages(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.getConversationMessages(conversationId, currentUser);
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ChatMessageDTO sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody ChatMessageRequestDTO request,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatService.sendMessage(conversationId, request, currentUser);
    }

    @PatchMapping("/conversations/{conversationId}/read")
    public ActionResultDTO markConversationRead(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        chatService.markConversationRead(conversationId, currentUser);
        return ActionResults.of("MARK_CHAT_CONVERSATION_READ", "Conversation marked as read.");
    }

    @PostMapping("/presence/heartbeat")
    public ActionResultDTO heartbeat(@AuthenticationPrincipal AppUser currentUser) {
        chatService.heartbeat(currentUser);
        return ActionResults.of("CHAT_HEARTBEAT", "Presence updated.");
    }
}
