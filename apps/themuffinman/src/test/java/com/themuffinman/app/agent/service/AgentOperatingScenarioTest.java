package com.themuffinman.app.agent.service;

import com.themuffinman.app.agent.dto.AdminAgentPlaygroundRequestDTO;
import com.themuffinman.app.agent.dto.AdminAgentPlaygroundResponseDTO;
import com.themuffinman.app.agent.dto.AdminAgentSimulationRequestDTO;
import com.themuffinman.app.agent.dto.AdminAgentSimulationResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AgentOperatingScenarioTest {

    @Autowired
    private AdminAgentPlaygroundService adminAgentPlaygroundService;

    @Test
    void ambiguousDeletePromptFailsClosedUntilExactTargetAndConfirmationExist() {
        AdminAgentPlaygroundResponseDTO response = adminAgentPlaygroundService.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder().prompt("Delete my quest Garden Help").build(),
                admin()
        );

        assertTrue(response.getClarificationContract().isClarificationRequired());
        assertTrue(response.getClarificationContract().isFailClosedOnAmbiguity());
        assertTrue(response.getUnresolvedInputs().contains("exact owned quest identity"));
        assertTrue(response.getUnresolvedInputs().contains("destructive confirmation"));
        assertTrue(response.getExecutionReadiness().isDestructiveConfirmationRequired());
    }

    @Test
    void approveFirstApplicantSimulationFailsClosedWithoutExactResolution() {
        AdminAgentSimulationResponseDTO response = adminAgentPlaygroundService.simulatePrompt(
                AdminAgentSimulationRequestDTO.builder().prompt("Approve the first applicant for my quest").build(),
                admin()
        );

        assertEquals("approve_application", response.getSelectedIntentId());
        assertEquals(false, response.isSafeToExecute());
        assertTrue(response.getBlockingReasons().contains("exact owned quest identity"));
        assertTrue(response.getIntentLineage().getResolutionWorkflows().contains("find_owned_quest_candidates"));
        assertTrue(response.getIntentLineage().getResolutionWorkflows().contains("select_oldest_pending_application"));
    }

    private AppUser admin() {
        AppUser admin = new AppUser();
        admin.setId(99L);
        admin.setRole(AppUserRole.ADMIN);
        return admin;
    }
}
