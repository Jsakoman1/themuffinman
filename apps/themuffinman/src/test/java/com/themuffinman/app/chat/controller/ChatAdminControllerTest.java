package com.themuffinman.app.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuffinman.app.chat.dto.ChatAdminConversationSupportViewDTO;
import com.themuffinman.app.chat.dto.ChatAuditEventDTO;
import com.themuffinman.app.chat.dto.ChatAuditEventListDTO;
import com.themuffinman.app.chat.dto.ChatConversationSummaryDTO;
import com.themuffinman.app.chat.service.ChatAdminService;
import com.themuffinman.app.common.controller.GlobalExceptionHandler;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatAdminControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatAdminService chatAdminService = mock(ChatAdminService.class);
    private MockMvc mockMvc;
    private AppUser currentUser;

    @BeforeEach
    void setUp() {
        ChatAdminController controller = new ChatAdminController(chatAdminService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        currentUser = new AppUser();
        currentUser.setId(1L);
        currentUser.setUsername("admin");
        currentUser.setEmail("admin@sidequest.test");
        currentUser.setPasswordHash("hash");
        currentUser.setRole(AppUserRole.ADMIN);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAuditEventsDelegatesToService() throws Exception {
        authenticateCurrentUser();
        when(chatAdminService.getAuditEvents("MESSAGE_REACTION_ADDED", 2L, 5L, 25, currentUser))
                .thenReturn(ChatAuditEventListDTO.builder()
                        .events(List.of(ChatAuditEventDTO.builder()
                                .id(10L)
                                .eventType("MESSAGE_REACTION_ADDED")
                                .userId(2L)
                                .conversationId(5L)
                                .createdAt("2026-07-08T10:00:00Z")
                                .build()))
                        .limit(25)
                        .eventType("MESSAGE_REACTION_ADDED")
                        .userId(2L)
                        .conversationId(5L)
                        .build());

        mockMvc.perform(get("/chat/admin/audit-events")
                        .param("eventType", "MESSAGE_REACTION_ADDED")
                        .param("userId", "2")
                        .param("conversationId", "5")
                        .param("limit", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limit").value(25))
                .andExpect(jsonPath("$.events[0].eventType").value("MESSAGE_REACTION_ADDED"));

        verify(chatAdminService).getAuditEvents("MESSAGE_REACTION_ADDED", 2L, 5L, 25, currentUser);
    }

    @Test
    void getConversationSupportViewDelegatesToService() throws Exception {
        authenticateCurrentUser();
        when(chatAdminService.getConversationSupportView(5L, currentUser))
                .thenReturn(ChatAdminConversationSupportViewDTO.builder()
                        .conversation(ChatConversationSummaryDTO.builder().conversationId(5L).conversationType("GROUP").build())
                        .recentMessages(List.of())
                        .recentAuditEvents(List.of())
                        .recentMessagesLimit(25)
                        .recentAuditLimit(25)
                        .build());

        mockMvc.perform(get("/chat/admin/conversations/5/support-view"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversation.conversationId").value(5))
                .andExpect(jsonPath("$.recentMessagesLimit").value(25))
                .andExpect(jsonPath("$.recentAuditLimit").value(25));

        verify(chatAdminService).getConversationSupportView(5L, currentUser);
    }

    @Test
    void moderateDeleteMessageDelegatesToService() throws Exception {
        authenticateCurrentUser();

        mockMvc.perform(delete("/chat/admin/conversations/5/messages/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("ADMIN_REMOVE_CHAT_MESSAGE"));

        verify(chatAdminService).moderateDeleteMessage(5L, 9L, currentUser);
    }

    private void authenticateCurrentUser() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));
    }
}
