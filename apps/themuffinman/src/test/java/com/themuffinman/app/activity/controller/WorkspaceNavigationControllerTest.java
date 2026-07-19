package com.themuffinman.app.activity.controller;

import com.themuffinman.app.activity.dto.WorkspaceNavigationResponseDTO;
import com.themuffinman.app.activity.service.WorkspaceNavigationService;
import com.themuffinman.app.common.controller.GlobalExceptionHandler;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkspaceNavigationControllerTest {
    private final WorkspaceNavigationService service = mock(WorkspaceNavigationService.class);
    private MockMvc mockMvc;
    private AppUser currentUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WorkspaceNavigationController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        currentUser = new AppUser();
        currentUser.setId(9L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                currentUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsViewerNavigationContract() throws Exception {
        when(service.getNavigation(currentUser)).thenReturn(WorkspaceNavigationResponseDTO.builder()
                .contractVersion("workspace-navigation-v1")
                .generatedAt(Instant.parse("2026-07-19T20:00:00Z"))
                .refreshAfterSeconds(30)
                .unreadCount(2)
                .modules(List.of())
                .build());

        mockMvc.perform(get("/workspace/navigation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractVersion").value("workspace-navigation-v1"))
                .andExpect(jsonPath("$.refreshAfterSeconds").value(30))
                .andExpect(jsonPath("$.unreadCount").value(2))
                .andExpect(jsonPath("$.modules").isArray());

        verify(service).getNavigation(currentUser);
    }
}
