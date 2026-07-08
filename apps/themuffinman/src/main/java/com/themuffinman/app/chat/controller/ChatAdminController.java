package com.themuffinman.app.chat.controller;

import com.themuffinman.app.chat.dto.ChatAdminConversationSupportViewDTO;
import com.themuffinman.app.chat.dto.ChatAuditEventListDTO;
import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.common.dto.ActionResults;
import com.themuffinman.app.chat.service.ChatAdminService;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat/admin")
@RequiredArgsConstructor
public class ChatAdminController {

    private final ChatAdminService chatAdminService;

    @GetMapping("/audit-events")
    public ChatAuditEventListDTO getAuditEvents(
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "conversationId", required = false) Long conversationId,
            @RequestParam(value = "limit", required = false) Integer limit,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatAdminService.getAuditEvents(eventType, userId, conversationId, limit, currentUser);
    }

    @GetMapping("/conversations/{conversationId}/support-view")
    public ChatAdminConversationSupportViewDTO getConversationSupportView(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return chatAdminService.getConversationSupportView(conversationId, currentUser);
    }

    @DeleteMapping("/conversations/{conversationId}/messages/{messageId}")
    public ActionResultDTO moderateDeleteMessage(
            @PathVariable Long conversationId,
            @PathVariable Long messageId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        chatAdminService.moderateDeleteMessage(conversationId, messageId, currentUser);
        return ActionResults.of("ADMIN_REMOVE_CHAT_MESSAGE", "Chat message removed.");
    }
}
