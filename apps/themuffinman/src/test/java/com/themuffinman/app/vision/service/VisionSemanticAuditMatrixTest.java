package com.themuffinman.app.vision.service;

import com.themuffinman.app.agent.service.LocalAdminAgentPromptTranslator;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.config.VoiceProperties;
import com.themuffinman.app.prompt.PromptSemanticsSupport;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VisionSemanticAuditMatrixTest {

    private final VisionPromptUnderstandingService service = new VisionPromptUnderstandingService(
            new AgentProperties(),
            new LocalAdminAgentPromptTranslator(),
            new VisionSemanticMapper(),
            new PromptSemanticsSupport(),
            new VisionSemanticOrchestrationContextService(new VoiceProperties()),
            new VisionSemanticRouteCatalogService(),
            new VisionSemanticContractSanitizer(),
            new VisionSemanticResponseValidator()
    );

    @ParameterizedTest(name = "{0}")
    @MethodSource("promptCases")
    void routesCommonPromptShapesIntoTheExpectedSemanticIntent(String label, String prompt, String expectedIntent, String expectedCapabilityId) {
        VisionPromptUnderstandingResult result = service.understandPrompt(prompt, null);

        assertEquals(expectedIntent, result.semanticPlanOrEmpty().getCandidateIntent(), label);
        assertEquals(expectedCapabilityId, result.semanticPlanOrEmpty().getCapabilityId(), label);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("mixedSignalCases")
    void prefersTheMostRelevantEntityFamilyWhenPromptContainsMultipleSignals(String label, String prompt, String expectedIntent, String expectedCapabilityId) {
        VisionPromptUnderstandingResult result = service.understandPrompt(prompt, null);

        assertEquals(expectedIntent, result.semanticPlanOrEmpty().getCandidateIntent(), label);
        assertEquals(expectedCapabilityId, result.semanticPlanOrEmpty().getCapabilityId(), label);
    }

    static Stream<Arguments> promptCases() {
        return Stream.of(
                Arguments.of("create quest with split fields", "Create a paid quest called Move my sofa for tomorrow at 7 pm in Zurich for 20 euros", "CREATE_QUEST", "create_quest"),
                Arguments.of("discover moving help", "show me open quests for moving help", "DISCOVER_QUESTS", "discover_quests"),
                Arguments.of("open chat with Josip", "open chat with Josip", "OPEN_CHAT", "open_chat"),
                Arguments.of("view chat workspace", "show chat", "VIEW_CHAT_WORKSPACE", "view_chat_workspace"),
                Arguments.of("view profile", "show my profile", "VIEW_PROFILE", "view_profile"),
                Arguments.of("view circles", "show circles", "VIEW_CIRCLES", "view_circles"),
                Arguments.of("view applications", "show applications", "VIEW_APPLICATIONS", "view_applications"),
                Arguments.of("create circle", "create circle Neighbours", "CREATE_CIRCLE", "create_circle"),
                Arguments.of("create group", "make a group called Neighbours", "CREATE_CIRCLE", "create_circle"),
                Arguments.of("create circle request", "send circle request to Josip", "CREATE_CIRCLE_REQUEST", "create_circle_request"),
                Arguments.of("accept circle request", "accept circle request from Josip", "ACCEPT_CIRCLE_REQUEST", "accept_circle_request"),
                Arguments.of("delete circle request", "decline circle request from Josip", "DELETE_CIRCLE_REQUEST", "delete_circle_request"),
                Arguments.of("update circle", "rename circle Neighbours to Core Team", "UPDATE_CIRCLE", "update_circle"),
                Arguments.of("delete circle", "delete circle Neighbours", "DELETE_CIRCLE", "delete_circle"),
                Arguments.of("create application", "apply to quest 42", "CREATE_APPLICATION", "create_application"),
                Arguments.of("submit application", "submit application for quest 42", "CREATE_APPLICATION", "create_application"),
                Arguments.of("update application", "update my application for quest 42", "UPDATE_APPLICATION", "update_application"),
                Arguments.of("withdraw application", "withdraw my application for quest 42", "WITHDRAW_APPLICATION", "withdraw_application"),
                Arguments.of("approve application", "approve application Josip for quest 42", "APPROVE_APPLICATION", "approve_application"),
                Arguments.of("decline application", "decline application Josip for quest 42", "DECLINE_APPLICATION", "decline_application"),
                Arguments.of("update profile", "update my profile username to jsak and bio to Reliable mover", "UPDATE_PROFILE", "update_profile"),
                Arguments.of("edit profile", "edit profile and change bio", "UPDATE_PROFILE", "update_profile"),
                Arguments.of("update profile location", "set my location to Zurich, Switzerland", "UPDATE_PROFILE_LOCATION", "update_profile_location"),
                Arguments.of("chat message", "send message to Josip", "OPEN_CHAT", "open_chat"),
                Arguments.of("view quest detail", "show quest #42", "VIEW_QUEST_DETAIL", "view_quest_detail"),
                Arguments.of("view application detail", "show application #42", "VIEW_APPLICATION_DETAIL", "view_application_detail"),
                Arguments.of("view user profile", "show user Josip", "VIEW_USER_PROFILE", "view_user_profile"),
                Arguments.of("view circle detail", "open circle Family", "VIEW_CIRCLE_DETAIL", "view_circle_detail")
        );
    }

    static Stream<Arguments> mixedSignalCases() {
        return Stream.of(
                Arguments.of("quest beats circles", "create quest still wins when circle words are present", "CREATE_QUEST", "create_quest"),
                Arguments.of("profile beats chat when explicit", "update my profile and also message Josip", "UPDATE_PROFILE", "update_profile")
        );
    }
}
