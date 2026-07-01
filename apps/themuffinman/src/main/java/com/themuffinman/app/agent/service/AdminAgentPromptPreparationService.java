package com.themuffinman.app.agent.service;

import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.prompt.PromptSemanticsSupport;
import org.springframework.stereotype.Service;

@Service
public class AdminAgentPromptPreparationService {

    private final AgentProperties agentProperties;
    private final AdminAgentTextProvider adminAgentTextProvider;
    private final AdminAgentPromptTranslator localPromptTranslator;
    private final PromptSemanticsSupport promptSemanticsSupport;

    public AdminAgentPromptPreparationService(
            AgentProperties agentProperties,
            OpenAiAdminAgentClient adminAgentTextProvider,
            LocalAdminAgentPromptTranslator localPromptTranslator,
            PromptSemanticsSupport promptSemanticsSupport
    ) {
        this.agentProperties = agentProperties;
        this.adminAgentTextProvider = adminAgentTextProvider;
        this.localPromptTranslator = localPromptTranslator;
        this.promptSemanticsSupport = promptSemanticsSupport;
    }

    public AdminAgentPromptTranslation preparePrompt(String prompt) {
        AdminAgentPromptTranslation translation;
        if ("openai".equalsIgnoreCase(agentProperties.getProvider()) && adminAgentTextProvider.isConfigured()) {
            try {
                translation = adminAgentTextProvider.translatePromptToEnglish(prompt);
            } catch (RuntimeException ignored) {
                translation = localPromptTranslator.translateForPlanning(prompt);
            }
        } else {
            translation = localPromptTranslator.translateForPlanning(prompt);
        }

        return translation.withSemanticPlan(promptSemanticsSupport.inferPlan(translation.getTranslatedPrompt()));
    }
}
