package com.themuffinman.app.chat.controller;

import com.themuffinman.app.chat.service.ChatService;
import com.themuffinman.app.identity.model.AppUser;
import java.time.Instant;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AisChatStatusController {
    private final ChatService chatService;
    public AisChatStatusController(ChatService chatService) { this.chatService = chatService; }

    @GetMapping("/ais/chat-status")
    public ChatStatus status(@AuthenticationPrincipal AppUser currentUser) {
        var workspace = chatService.getWorkspace(currentUser, 20, false);
        return new ChatStatus("themuffinman_chat_status",1,"TheMuffinMan",Instant.now(),
                workspace.getUnreadConversationCount(),workspace.getOnlineContactCount(),
                workspace.getConversationLimit(),false,0,
                "source_owned_visible_workspace_not_message_delivery_or_global_unread_total");
    }
    public record ChatStatus(String kind,int version,String component,Instant observedAt,
            long unreadConversationCount,long onlineContactCount,int conversationLimit,
            boolean privateMessageBodiesReturned,int mutations,String evidenceBoundary) { }
}
