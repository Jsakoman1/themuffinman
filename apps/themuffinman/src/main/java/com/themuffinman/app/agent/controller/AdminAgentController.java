package com.themuffinman.app.agent.controller;

import com.themuffinman.app.agent.dto.AdminAgentPlaygroundRequestDTO;
import com.themuffinman.app.agent.dto.AdminAgentPlaygroundResponseDTO;
import com.themuffinman.app.agent.dto.AdminAgentExecutionRequestDTO;
import com.themuffinman.app.agent.dto.AdminAgentExecutionResponseDTO;
import com.themuffinman.app.agent.dto.AdminAgentSimulationRequestDTO;
import com.themuffinman.app.agent.dto.AdminAgentSimulationResponseDTO;
import com.themuffinman.app.agent.service.AdminAgentExecutionService;
import com.themuffinman.app.agent.service.AdminAgentPlaygroundService;
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

    private final AdminAgentPlaygroundService adminAgentPlaygroundService;
    private final AdminAgentExecutionService adminAgentExecutionService;

    @PostMapping("/playground")
    public AdminAgentPlaygroundResponseDTO runPlaygroundPrompt(
            @Valid @RequestBody AdminAgentPlaygroundRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return adminAgentPlaygroundService.runPrompt(dto, currentUser);
    }

    @PostMapping("/simulate")
    public AdminAgentSimulationResponseDTO runSimulation(
            @Valid @RequestBody AdminAgentSimulationRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return adminAgentPlaygroundService.simulatePrompt(dto, currentUser);
    }

    @PostMapping("/execute")
    public AdminAgentExecutionResponseDTO runExecution(
            @Valid @RequestBody AdminAgentExecutionRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return adminAgentExecutionService.execute(dto, currentUser);
    }
}
