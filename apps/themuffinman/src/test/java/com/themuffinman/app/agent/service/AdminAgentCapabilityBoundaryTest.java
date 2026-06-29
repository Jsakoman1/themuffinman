package com.themuffinman.app.agent.service;

import com.themuffinman.app.agent.dto.AdminAgentPlaygroundRequestDTO;
import com.themuffinman.app.agent.dto.AdminAgentPlaygroundResponseDTO;
import com.themuffinman.app.agent.sandbox.SandboxGenerationPlanner;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminAgentCapabilityBoundaryTest {

    private final AgentProperties agentProperties = new AgentProperties();
    private final OpenAiAdminAgentClient provider = new OpenAiAdminAgentClient(agentProperties);
    private final LocalAdminAgentPromptTranslator localTranslator = new LocalAdminAgentPromptTranslator();
    private final SandboxGenerationPlanner sandboxGenerationPlanner = new SandboxGenerationPlanner();
    private final AdminAgentPlaygroundService service = new AdminAgentPlaygroundService(agentProperties, provider, localTranslator, sandboxGenerationPlanner);

    @Test
    void deletePromptStaysBlockedUntilExactTargetAndConfirmationExist() {
        AdminAgentPlaygroundResponseDTO response = runAsAdmin("Delete my quest Garden Help");

        assertTrue(response.getClarificationContract().isClarificationRequired());
        assertTrue(response.getClarificationContract().isFailClosedOnAmbiguity());
        assertTrue(response.getUnresolvedInputs().contains("exact owned quest identity"));
        assertTrue(response.getUnresolvedInputs().contains("destructive confirmation"));
        assertTrue(response.getExecutionReadiness().isDestructiveConfirmationRequired());
        assertEquals(true, response.getExecutionReadiness().isPlanningOnly());
    }

    @Test
    void approvalPromptStaysBlockedWithoutExactQuestAndApplicantResolution() {
        AdminAgentPlaygroundResponseDTO response = runAsAdmin("Approve the first applicant for my quest");

        assertTrue(response.getClarificationContract().isClarificationRequired());
        assertTrue(response.getUnresolvedInputs().contains("exact owned quest identity"));
        assertTrue(response.getExecutionReadiness().isMultiActorContextRequired());
        assertTrue(response.getResolutionRequirements().stream().anyMatch(item -> item.getEntityType().equals("quest")));
        assertTrue(response.getResolutionRequirements().stream().anyMatch(item -> item.getEntityType().equals("application")));
    }

    @Test
    void currentLocationPromptStaysBlockedWithoutTrustedCoordinates() {
        AdminAgentPlaygroundResponseDTO response = runAsAdmin("Set my account location to my current location");

        assertTrue(response.getClarificationContract().isClarificationRequired());
        assertTrue(response.getUnresolvedInputs().contains("current device coordinates"));
        assertTrue(response.getExecutionReadiness().isCurrentLocationCapabilityRequired());
        assertEquals("requires_device_coordinates_or_reverse_lookup", response.getExecutionReadiness().getCurrentLocationCapabilityStatus());
    }

    @Test
    void unknownLanguagePromptFailsClosedWithoutReliableTranslation() {
        AdminAgentPlaygroundResponseDTO response = runAsAdmin("请删除我的任务");

        assertTrue(response.getClarificationContract().isClarificationRequired());
        assertTrue(response.getClarificationContract().isFailClosedOnAmbiguity());
        assertTrue(response.getUnresolvedInputs().contains("reliable English translation"));
        assertEquals(false, response.isPromptTranslationReliable());
        assertEquals(true, response.getExecutionReadiness().isRequiresExternalTranslationProvider());
    }

    private AdminAgentPlaygroundResponseDTO runAsAdmin(String prompt) {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);
        return service.runPrompt(AdminAgentPlaygroundRequestDTO.builder().prompt(prompt).build(), admin);
    }
}
