package com.themuffinman.app.agent.service;

import com.themuffinman.app.agent.dto.AdminAgentExecutionRequestDTO;
import com.themuffinman.app.agent.dto.AdminAgentExecutionResponseDTO;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.prompt.PromptSemanticsSupport;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.service.WorkmarketQuestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAgentExecutionServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private WorkmarketQuestService questService;

    private AgentProperties agentProperties;
    private AdminAgentExecutionService service;
    private AppUser adminUser;
    private AppUser targetUser;

    @BeforeEach
    void setUp() {
        agentProperties = new AgentProperties();
        agentProperties.setAdminExecutionEnabled(true);
        agentProperties.setAdminQuestBatchLimit(3);

        AdminAgentPromptPreparationService promptPreparationService =
                new AdminAgentPromptPreparationService(new PromptSemanticsSupport(), new com.themuffinman.app.vision.service.VisionSemanticRouteCatalogService());
        AdminAgentSurfacePolicy adminAgentSurfacePolicy = new AdminAgentSurfacePolicy(agentProperties);
        AdminSyntheticQuestExecutionPlanner planner = new AdminSyntheticQuestExecutionPlanner();
        AdminAgentTargetUserResolver targetUserResolver = new AdminAgentTargetUserResolver(appUserRepository);

        service = new AdminAgentExecutionService(
                agentProperties,
                adminAgentSurfacePolicy,
                promptPreparationService,
                planner,
                targetUserResolver,
                questService
        );

        adminUser = new AppUser();
        adminUser.setRole(AppUserRole.ADMIN);
        adminUser.setId(1L);
        adminUser.setUsername("admin");

        targetUser = new AppUser();
        targetUser.setId(7L);
        targetUser.setUsername("Josip");
        targetUser.setEmail("josip@example.com");
    }

    @Test
    void returnsConfirmationPreviewBeforeExecution() {
        when(appUserRepository.findByEmail("josip@example.com")).thenReturn(Optional.of(targetUser));

        AdminAgentExecutionResponseDTO response = service.execute(
                AdminAgentExecutionRequestDTO.builder()
                        .prompt("Generate 5 unique synthetic quests for user josip@example.com about moving help")
                        .confirmed(false)
                        .build(),
                adminUser
        );

        assertFalse(response.isExecuted());
        assertTrue(response.isConfirmationRequired());
        assertEquals(5, response.getRequestedCount());
        assertEquals(3, response.getEffectiveCount());
        assertEquals("Josip", response.getTargetUserLabel());
        assertEquals(3, response.getQuestTitles().size());
    }

    @Test
    void executesSyntheticQuestBatchAfterConfirmation() {
        when(appUserRepository.findByEmail("josip@example.com")).thenReturn(Optional.of(targetUser));
        when(questService.createQuest(any(), any())).thenAnswer(invocation -> {
            Quest quest = new Quest();
            quest.setId((long) (100 + Math.random() * 100));
            quest.setTitle(invocation.<com.themuffinman.app.workmarket.dto.QuestRequestDTO>getArgument(0).getTitle());
            return quest;
        });

        AdminAgentExecutionResponseDTO response = service.execute(
                AdminAgentExecutionRequestDTO.builder()
                        .prompt("Generate 2 unique synthetic quests for user josip@example.com about moving help")
                        .confirmed(true)
                        .build(),
                adminUser
        );

        ArgumentCaptor<com.themuffinman.app.workmarket.dto.QuestRequestDTO> requestCaptor =
                ArgumentCaptor.forClass(com.themuffinman.app.workmarket.dto.QuestRequestDTO.class);
        verify(questService, times(2)).createQuest(requestCaptor.capture(), any());

        assertTrue(response.isExecuted());
        assertFalse(response.isConfirmationRequired());
        assertEquals(2, response.getCreatedQuestIds().size());
        assertTrue(requestCaptor.getAllValues().stream().allMatch(item -> item.getCreatorId().equals(targetUser.getId())));
        assertTrue(requestCaptor.getAllValues().stream().allMatch(item -> item.getTitle().startsWith("[SYNTHETIC]")));
    }
}
