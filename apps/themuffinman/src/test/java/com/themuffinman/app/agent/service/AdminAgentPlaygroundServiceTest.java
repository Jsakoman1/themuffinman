package com.themuffinman.app.agent.service;

import com.themuffinman.app.agent.dto.AdminAgentPlaygroundRequestDTO;
import com.themuffinman.app.agent.dto.AdminAgentPlaygroundResponseDTO;
import com.themuffinman.app.agent.dto.AdminAgentSimulationRequestDTO;
import com.themuffinman.app.agent.dto.AdminAgentSimulationResponseDTO;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminAgentPlaygroundServiceTest {

    private final AgentProperties agentProperties = new AgentProperties();
    private final StubAdminAgentTextProvider provider = new StubAdminAgentTextProvider();
    private final LocalAdminAgentPromptTranslator localTranslator = new LocalAdminAgentPromptTranslator();
    private final AdminAgentPlaygroundService service = new AdminAgentPlaygroundService(agentProperties, provider, localTranslator);

    @Test
    void rejectsNonAdminAccess() {
        AppUser user = new AppUser();
        user.setRole(AppUserRole.USER);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.runPrompt(AdminAgentPlaygroundRequestDTO.builder().prompt("test").build(), user));

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void returnsBatchGenerationWarningsForQuestPrompt() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Generate 20 unique quests for user Josip tomorrow")
                        .build(),
                admin
        );

        assertTrue(response.getSuggestedWorkflows().contains("create_user_with_quests"));
        assertTrue(response.getWarnings().stream().anyMatch(item -> item.contains("meaningfully unique")));
        assertTrue(response.getWarnings().stream().anyMatch(item -> item.contains("timezone")));
    }

    @Test
    void returnsFreeQuestWarningWhenPromptMentionsFreePricing() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Can we create a free quest with award 0?")
                        .build(),
                admin
        );

        assertTrue(response.getSuggestedWorkflows().contains("create_quest"));
        assertTrue(response.getWarnings().stream().anyMatch(item -> item.contains("omit proposed price")));
    }

    @Test
    void fallsBackToMockWhenOpenAiProviderIsSelectedWithoutApiKey() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);
        agentProperties.setProvider("openai");
        provider.configured = false;

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Generate 20 unique quests for user Josip")
                        .build(),
                admin
        );

        assertEquals("mock", response.getProvider());
        assertTrue(response.getWarnings().stream().anyMatch(item -> item.contains("no backend API key is configured")));
        assertTrue(response.getSummary().contains("Prompt received"));
    }

    @Test
    void usesProviderSummaryWhenOpenAiProviderIsConfigured() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);
        agentProperties.setProvider("openai");
        provider.configured = true;
        provider.summary = "Live provider summary";

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Create a free quest with award 0")
                        .build(),
                admin
        );

        assertEquals("openai", response.getProvider());
        assertTrue(response.isExternalLlmConfigured());
        assertEquals("Live provider summary", response.getSummary());
    }

    @Test
    void classifiesCroatianAdminPromptForUserQuestAndConnectionFlows() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Napravi novi user account Marko i napravi 3 questa i posalji friend request na jsak")
                        .build(),
                admin
        );

        assertTrue(response.getSuggestedWorkflows().contains("create_user"));
        assertTrue(response.getSuggestedWorkflows().contains("create_user_with_quests"));
        assertTrue(response.getSuggestedWorkflows().contains("resolve_circle_recipient"));
        assertTrue(response.getSuggestedWorkflows().contains("create_circle_connection"));
        assertTrue(response.getResolutionRequirements().stream().anyMatch(item -> item.getWorkflowId().equals("resolve_circle_recipient")));
        assertEquals("hr", response.getPromptSourceLanguage());
        assertTrue(response.isPromptTranslationApplied());
        assertTrue(response.isPromptTranslationReliable());
        assertTrue(response.getMatchedSignals().contains("user_creation"));
        assertTrue(response.getMatchedSignals().contains("quest_generation"));
        assertTrue(response.getMatchedSignals().contains("connection_request"));
        assertTrue(response.getUnresolvedInputs().contains("unique email"));
        assertTrue(response.getUnresolvedInputs().contains("exact recipient identity"));
        assertTrue(response.getSuggestedWorkflows().stream().noneMatch(item -> item.equals("create_circle_only_quest_for_selected_people")));
    }

    @Test
    void includesProviderFailureDetailInWarnings() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);
        agentProperties.setProvider("openai");
        provider.configured = true;
        provider.failureMessage = "OpenAI request failed: 400 Bad Request";

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Create a free quest with award 0")
                        .build(),
                admin
        );

        assertEquals("mock", response.getProvider());
        assertTrue(response.getWarnings().stream().anyMatch(item -> item.contains("OpenAI failure detail")));
        assertTrue(response.getWarnings().stream().anyMatch(item -> item.contains("400 Bad Request")));
    }

    @Test
    void addsCircleOnlyFlowOnlyForExplicitSelectedAudiencePrompt() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Create a quest only for selected circle friends tomorrow")
                        .build(),
                admin
        );

        assertTrue(response.getMatchedSignals().contains("circle_only_flow"));
        assertTrue(response.getSuggestedWorkflows().contains("create_circle_only_quest_for_selected_people"));
        assertTrue(response.getUnresolvedInputs().contains("accepted connection ids"));
        assertEquals(true, response.getExecutionReadiness().isMultiActorContextRequired());
    }

    @Test
    void marksUnknownLanguageTranslationAsUnreliableWithoutProvider() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("给 Tom 发送好友请求")
                        .build(),
                admin
        );

        assertEquals("unknown", response.getPromptSourceLanguage());
        assertEquals(false, response.isPromptTranslationReliable());
        assertTrue(response.getUnresolvedInputs().contains("reliable English translation"));
        assertEquals(true, response.getClarificationContract().isClarificationRequired());
    }

    @Test
    void classifiesApproveFirstApplicantPromptWithReadBeforeWriteWorkflows() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Ako ima vise prijava na moj quest approvaj onog tko se applicirao prvi")
                        .build(),
                admin
        );

        assertTrue(response.getMatchedSignals().contains("application_approval"));
        assertTrue(response.getMatchedSignals().contains("oldest_pending_selection"));
        assertTrue(response.getSuggestedWorkflows().contains("find_owned_quest_candidates"));
        assertTrue(response.getSuggestedWorkflows().contains("inspect_owned_quest_pending_applications"));
        assertTrue(response.getSuggestedWorkflows().contains("select_oldest_pending_application"));
        assertTrue(response.getSuggestedWorkflows().contains("approve_application"));
        assertTrue(response.getUnresolvedInputs().contains("exact owned quest identity"));
        assertTrue(response.getWarnings().stream().anyMatch(item -> item.contains("oldest-pending")));
        assertTrue(response.getResolutionRequirements().stream().anyMatch(item -> item.getEntityType().equals("application")));
    }

    @Test
    void classifiesDeleteQuestPromptAsDestructiveFlow() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Obrisi moj quest Taj i Taj")
                        .build(),
                admin
        );

        assertTrue(response.getMatchedSignals().contains("quest_deletion"));
        assertTrue(response.getSuggestedWorkflows().contains("find_owned_quest_candidates"));
        assertTrue(response.getSuggestedWorkflows().contains("delete_quest"));
        assertTrue(response.getUnresolvedInputs().contains("exact owned quest identity"));
        assertTrue(response.getUnresolvedInputs().contains("destructive confirmation"));
        assertTrue(response.getWarnings().stream().anyMatch(item -> item.contains("destructive")));
        assertEquals(true, response.getExecutionReadiness().isDestructiveConfirmationRequired());
    }

    @Test
    void classifiesCurrentLocationUpdateAsAgentSafeCapability() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Change my account location to my current location")
                        .build(),
                admin
        );

        assertTrue(response.getMatchedSignals().contains("current_location_update"));
        assertTrue(response.getSuggestedWorkflows().contains("resolve_current_location_input"));
        assertTrue(response.getSuggestedWorkflows().contains("set_profile_current_location"));
        assertTrue(response.getUnresolvedInputs().contains("current device coordinates"));
        assertEquals(true, response.getExecutionReadiness().isCurrentLocationCapabilityRequired());
    }

    @Test
    void classifiesApplicationUpdatePromptThroughPendingApplicationResolution() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Update my application for the fence quest and change the price")
                        .build(),
                admin
        );

        assertTrue(response.getMatchedSignals().contains("application_update"));
        assertTrue(response.getSuggestedWorkflows().contains("find_my_pending_application_candidates"));
        assertTrue(response.getSuggestedWorkflows().contains("update_my_application"));
        assertTrue(response.getUnresolvedInputs().contains("exact pending application identity"));
        assertTrue(response.getUnresolvedInputs().contains("validated application update payload"));
        assertTrue(response.getResolutionRequirements().stream().anyMatch(item -> item.getWorkflowId().equals("find_my_pending_application_candidates")));
    }

    @Test
    void classifiesCircleDeletionPromptAsDestructiveFlow() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Delete my circle Helpers")
                        .build(),
                admin
        );

        assertTrue(response.getMatchedSignals().contains("circle_deletion"));
        assertTrue(response.getSuggestedWorkflows().contains("resolve_circle_candidate"));
        assertTrue(response.getSuggestedWorkflows().contains("delete_circle"));
        assertTrue(response.getUnresolvedInputs().contains("exact owned circle identity"));
        assertTrue(response.getUnresolvedInputs().contains("destructive confirmation"));
        assertEquals(true, response.getExecutionReadiness().isDestructiveConfirmationRequired());
    }

    @Test
    void classifiesOutgoingRequestCancellationPrompt() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Cancel my friend request to Tom")
                        .build(),
                admin
        );

        assertTrue(response.getMatchedSignals().contains("circle_request_cancellation"));
        assertTrue(response.getSuggestedWorkflows().contains("resolve_outgoing_circle_request"));
        assertTrue(response.getSuggestedWorkflows().contains("cancel_circle_request"));
        assertTrue(response.getUnresolvedInputs().contains("exact outgoing request identity"));
    }

    @Test
    void classifiesChatReadPromptViaConversationResolution() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Mark chat with John as read")
                        .build(),
                admin
        );

        assertTrue(response.getMatchedSignals().contains("chat_read"));
        assertTrue(response.getSuggestedWorkflows().contains("resolve_chat_conversation"));
        assertTrue(response.getSuggestedWorkflows().contains("mark_chat_conversation_read"));
        assertTrue(response.getUnresolvedInputs().contains("exact conversation identity"));
        assertTrue(response.getResolutionRequirements().stream().anyMatch(item -> item.getEntityType().equals("conversation")));
    }

    @Test
    void classifiesProfileSelfUpdatePrompt() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Change my bio and update my profile")
                        .build(),
                admin
        );

        assertTrue(response.getMatchedSignals().contains("profile_self_update"));
        assertTrue(response.getSuggestedWorkflows().contains("set_profile_details"));
        assertTrue(response.getUnresolvedInputs().contains("validated profile update payload"));
    }

    @Test
    void classifiesMarkSpecificNewsItemReadPrompt() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Mark the notification from Tom as read")
                        .build(),
                admin
        );

        assertTrue(response.getMatchedSignals().contains("news_read_item"));
        assertTrue(response.getSuggestedWorkflows().contains("resolve_news_item_candidate"));
        assertTrue(response.getSuggestedWorkflows().contains("mark_news_item_read"));
        assertTrue(response.getUnresolvedInputs().contains("exact news item identity"));
    }

    @Test
    void classifiesMarkAllNewsReadPrompt() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Mark all notifications as read")
                        .build(),
                admin
        );

        assertTrue(response.getMatchedSignals().contains("news_read_all"));
        assertTrue(response.getSuggestedWorkflows().contains("mark_all_news_read"));
    }

    @Test
    void classifiesAdminApplicationDeletePromptAsDestructive() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Delete the application for Fix fence")
                        .build(),
                admin
        );

        assertTrue(response.getMatchedSignals().contains("admin_application_deletion"));
        assertTrue(response.getSuggestedWorkflows().contains("find_admin_application_candidates"));
        assertTrue(response.getSuggestedWorkflows().contains("delete_admin_application"));
        assertTrue(response.getUnresolvedInputs().contains("exact application identity"));
        assertTrue(response.getUnresolvedInputs().contains("destructive confirmation"));
        assertEquals(true, response.getExecutionReadiness().isDestructiveConfirmationRequired());
    }

    @Test
    void classifiesAdminUserUpdatePrompt() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Update user Tom and change his role to admin")
                        .build(),
                admin
        );

        assertTrue(response.getMatchedSignals().contains("admin_user_update"));
        assertTrue(response.getSuggestedWorkflows().contains("update_user_as_admin"));
        assertTrue(response.getUnresolvedInputs().contains("exact user identity"));
        assertTrue(response.getUnresolvedInputs().contains("validated admin user update payload"));
    }

    @Test
    void classifiesAdminUserDeletePromptAsDestructive() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("Delete user Tom")
                        .build(),
                admin
        );

        assertTrue(response.getMatchedSignals().contains("admin_user_deletion"));
        assertTrue(response.getSuggestedWorkflows().contains("delete_user_as_admin"));
        assertTrue(response.getUnresolvedInputs().contains("exact user identity"));
        assertTrue(response.getUnresolvedInputs().contains("destructive confirmation"));
        assertEquals(true, response.getExecutionReadiness().isDestructiveConfirmationRequired());
    }

    @Test
    void usesProviderTranslationForMandarinPromptWhenConfigured() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);
        agentProperties.setProvider("openai");
        provider.configured = true;
        provider.translation = AdminAgentPromptTranslation.builder()
                .sourceLanguage("zh")
                .originalPrompt("给 Tom 发送好友请求")
                .translatedPrompt("send friend request to Tom")
                .translationProvider("openai")
                .translationApplied(true)
                .translationReliable(true)
                .build();

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder()
                        .prompt("给 Tom 发送好友请求")
                        .build(),
                admin
        );

        assertEquals("zh", response.getPromptSourceLanguage());
        assertEquals("openai", response.getPromptTranslationProvider());
        assertTrue(response.isPromptTranslationReliable());
        assertTrue(response.getSuggestedWorkflows().contains("create_circle_connection"));
    }

    @Test
    void simulatesDeleteQuestPromptAsBlockedDryRun() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentSimulationResponseDTO response = service.simulatePrompt(
                AdminAgentSimulationRequestDTO.builder()
                        .prompt("Delete my quest Garden Help")
                        .build(),
                admin
        );

        assertEquals("delete_quest", response.getSelectedIntentId());
        assertEquals(false, response.isSafeToExecute());
        assertEquals("low", response.getResolutionConfidence().getTier());
        assertTrue(response.getBlockingReasons().contains("destructive confirmation"));
        assertTrue(response.getBlockingReasons().stream().anyMatch(item -> item.contains("destructive confirmation")));
        assertEquals("delete_quest", response.getIntentLineage().getIntentId());
        assertEquals("delete_quest", response.getEndpointPlan().getFirst().getEndpointId());
    }

    @Test
    void simulatesMandarinFriendRequestWithProviderBackedTranslation() {
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);
        agentProperties.setProvider("openai");
        provider.configured = true;
        provider.translation = AdminAgentPromptTranslation.builder()
                .sourceLanguage("zh")
                .originalPrompt("给 Tom 发送好友请求")
                .translatedPrompt("send friend request to Tom")
                .translationProvider("openai")
                .translationApplied(true)
                .translationReliable(true)
                .build();

        AdminAgentSimulationResponseDTO response = service.simulatePrompt(
                AdminAgentSimulationRequestDTO.builder()
                        .prompt("给 Tom 发送好友请求")
                        .build(),
                admin
        );

        assertEquals("create_circle_connection", response.getSelectedIntentId());
        assertEquals("zh", response.getPromptSourceLanguage());
        assertEquals("medium", response.getResolutionConfidence().getTier());
        assertTrue(response.getCapabilityAssessments().stream().anyMatch(item -> item.getCapabilityId().equals("external_translation_provider")));
        assertTrue(response.getIntentLineage().getResolutionWorkflows().contains("resolve_circle_recipient"));
    }

    private static class StubAdminAgentTextProvider extends OpenAiAdminAgentClient {
        private boolean configured;
        private String summary = "stub";
        private String failureMessage;
        private AdminAgentPromptTranslation translation = null;

        private StubAdminAgentTextProvider() {
            super(new AgentProperties());
        }

        @Override
        public boolean isConfigured() {
            return configured;
        }

        @Override
        public String providerName() {
            return "openai";
        }

        @Override
        public AdminAgentPromptTranslation translatePromptToEnglish(String prompt) {
            if (failureMessage != null) {
                throw new IllegalStateException(failureMessage);
            }
            if (translation != null) {
                return translation;
            }
            return AdminAgentPromptTranslation.builder()
                    .sourceLanguage("en")
                    .originalPrompt(prompt)
                    .translatedPrompt(prompt)
                    .translationProvider("openai")
                    .translationApplied(false)
                    .translationReliable(true)
                    .build();
        }

        @Override
        public String generatePlanningSummary(
                String prompt,
                java.util.List<String> suggestedWorkflows,
                java.util.List<String> matchedSignals,
                java.util.List<String> unresolvedInputs,
                java.util.List<String> warnings
        ) {
            if (failureMessage != null) {
                throw new IllegalStateException(failureMessage);
            }
            return summary;
        }
    }
}
