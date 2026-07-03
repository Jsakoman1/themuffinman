package com.themuffinman.app.agent.service;

import com.themuffinman.app.prompt.PromptSemanticsSupport;
import org.springframework.stereotype.Service;

@Service
public class AdminAgentPromptPreparationService {

    private final AdminAgentPromptTranslator localPromptTranslator;
    private final PromptSemanticsSupport promptSemanticsSupport;

    public AdminAgentPromptPreparationService(
            LocalAdminAgentPromptTranslator localPromptTranslator,
            PromptSemanticsSupport promptSemanticsSupport
    ) {
        this.localPromptTranslator = localPromptTranslator;
        this.promptSemanticsSupport = promptSemanticsSupport;
    }

    public AdminAgentPromptTranslation preparePrompt(String prompt) {
        AdminAgentPromptTranslation translation = localPromptTranslator.translateForPlanning(prompt);
        return translation.withSemanticPlan(promptSemanticsSupport.inferPlan(translation.getTranslatedPrompt()));
    }
}
