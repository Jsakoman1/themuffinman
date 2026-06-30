package com.themuffinman.app.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.themuffinman.app.agent.dto.AdminAgentPlaygroundRequestDTO;
import com.themuffinman.app.agent.dto.AdminAgentPlaygroundResponseDTO;
import com.themuffinman.app.agent.sandbox.SandboxGenerationPlanner;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminAgentGoldenPromptMatrixTest {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    @ParameterizedTest(name = "{0}")
    @MethodSource("goldenCases")
    void validatesGoldenPromptMatrix(GoldenPromptCase promptCase) {
        AgentProperties agentProperties = new AgentProperties();
        StubAdminAgentTextProvider provider = new StubAdminAgentTextProvider();
        LocalAdminAgentPromptTranslator localTranslator = new LocalAdminAgentPromptTranslator();
        if (promptCase.providerTranslation != null) {
            agentProperties.setProvider("openai");
            provider.configured = true;
            provider.translation = AdminAgentPromptTranslation.builder()
                    .sourceLanguage(promptCase.providerTranslation.sourceLanguage)
                    .originalPrompt(promptCase.prompt)
                    .translatedPrompt(promptCase.providerTranslation.translatedPrompt)
                    .translationProvider("openai")
                    .translationApplied(true)
                    .translationReliable(promptCase.providerTranslation.reliable)
                    .build();
        }

        AdminAgentPlaygroundService service = new AdminAgentPlaygroundService(
                agentProperties,
                provider,
                localTranslator,
                new SandboxGenerationPlanner()
        );
        AppUser admin = new AppUser();
        admin.setRole(AppUserRole.ADMIN);

        AdminAgentPlaygroundResponseDTO response = service.runPrompt(
                AdminAgentPlaygroundRequestDTO.builder().prompt(promptCase.prompt).build(),
                admin
        );

        assertEquals(promptCase.expected.promptSourceLanguage, response.getPromptSourceLanguage(), promptCase.id);
        assertEquals(promptCase.expected.translationProvider, response.getPromptTranslationProvider(), promptCase.id);
        assertEquals(promptCase.expected.translationApplied, response.isPromptTranslationApplied(), promptCase.id);
        assertEquals(promptCase.expected.translationReliable, response.isPromptTranslationReliable(), promptCase.id);
        assertEquals(promptCase.expected.clarificationRequired, response.getClarificationContract().isClarificationRequired(), promptCase.id);
        assertEquals(promptCase.expected.destructiveConfirmationRequired, response.getExecutionReadiness().isDestructiveConfirmationRequired(), promptCase.id);
        assertEquals(promptCase.expected.multiActorContextRequired, response.getExecutionReadiness().isMultiActorContextRequired(), promptCase.id);
        assertEquals(promptCase.expected.currentLocationCapabilityRequired, response.getExecutionReadiness().isCurrentLocationCapabilityRequired(), promptCase.id);
        assertEquals(promptCase.driftFingerprint, buildDriftFingerprint(response), promptCase.id);

        promptCase.expected.matchedSignals.forEach(signal ->
                assertTrue(response.getMatchedSignals().contains(signal), () -> promptCase.id + " missing matched signal " + signal));
        promptCase.expected.suggestedWorkflows.forEach(workflow ->
                assertTrue(response.getSuggestedWorkflows().contains(workflow), () -> promptCase.id + " missing workflow " + workflow));
        promptCase.expected.unresolvedInputs.forEach(input ->
                assertTrue(response.getUnresolvedInputs().contains(input), () -> promptCase.id + " missing unresolved input " + input));
    }

    private static String buildDriftFingerprint(AdminAgentPlaygroundResponseDTO response) {
        return "lang=" + response.getPromptSourceLanguage()
                + "|signals=" + String.join(",", response.getMatchedSignals())
                + "|workflows=" + String.join(",", response.getSuggestedWorkflows())
                + "|clarify=" + response.getClarificationContract().isClarificationRequired()
                + "|destructive=" + response.getExecutionReadiness().isDestructiveConfirmationRequired()
                + "|multi=" + response.getExecutionReadiness().isMultiActorContextRequired();
    }

    static Stream<GoldenPromptCase> goldenCases() throws Exception {
        try (InputStream inputStream = AdminAgentGoldenPromptMatrixTest.class.getResourceAsStream("/agent/admin-agent-golden-prompt-matrix.yaml")) {
            GoldenPromptCaseFile file = YAML_MAPPER.readValue(Objects.requireNonNull(inputStream), GoldenPromptCaseFile.class);
            return file.cases.stream();
        }
    }

    private static class StubAdminAgentTextProvider extends OpenAiAdminAgentClient {
        private boolean configured;
        private AdminAgentPromptTranslation translation;
        private String summary = "stub";

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
            return translation != null ? translation : super.translatePromptToEnglish(prompt);
        }

        @Override
        public String generatePlanningSummary(
                String prompt,
                java.util.List<String> suggestedWorkflows,
                java.util.List<String> matchedSignals,
                java.util.List<String> unresolvedInputs,
                java.util.List<String> warnings,
                AgentModelProfile modelProfile
        ) {
            return summary;
        }
    }

    private static class GoldenPromptCaseFile {
        public List<GoldenPromptCase> cases = new ArrayList<>();
    }

    private static class GoldenPromptCase {
        public String id;
        public String prompt;
        public String driftFingerprint;
        public ProviderTranslation providerTranslation;
        public Expected expected;

        @Override
        public String toString() {
            return id;
        }
    }

    private static class ProviderTranslation {
        public String sourceLanguage;
        public String translatedPrompt;
        public boolean reliable;
    }

    private static class Expected {
        public String promptSourceLanguage;
        public String translationProvider;
        public boolean translationApplied;
        public boolean translationReliable;
        public boolean clarificationRequired;
        public boolean destructiveConfirmationRequired;
        public boolean multiActorContextRequired;
        public boolean currentLocationCapabilityRequired;
        public List<String> matchedSignals = new ArrayList<>();
        public List<String> suggestedWorkflows = new ArrayList<>();
        public List<String> unresolvedInputs = new ArrayList<>();
    }
}
