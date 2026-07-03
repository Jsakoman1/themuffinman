package com.themuffinman.app.agent.controller;

import com.themuffinman.app.agent.dto.AdminAgentExecutionRequestDTO;
import com.themuffinman.app.agent.dto.AdminAgentExecutionResponseDTO;
import com.themuffinman.app.agent.service.AdminAgentExecutionService;
import com.themuffinman.app.identity.model.AppUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/agent")
@RequiredArgsConstructor
public class AdminAgentController {

    private final AdminAgentExecutionService adminAgentExecutionService;

    @PostMapping("/execute")
    public AdminAgentExecutionResponseDTO runExecution(
            @Valid @RequestBody AdminAgentExecutionRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return adminAgentExecutionService.execute(dto, currentUser);
    }
}
