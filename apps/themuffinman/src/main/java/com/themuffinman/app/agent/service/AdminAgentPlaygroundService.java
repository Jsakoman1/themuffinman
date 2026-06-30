package com.themuffinman.app.agent.service;

import com.themuffinman.app.agent.dto.AdminAgentPlaygroundRequestDTO;
import com.themuffinman.app.agent.dto.AdminAgentPlaygroundResponseDTO;
import com.themuffinman.app.agent.dto.AdminAgentSimulationRequestDTO;
import com.themuffinman.app.agent.dto.AdminAgentSimulationResponseDTO;
import com.themuffinman.app.agent.dto.AgentCapabilityAssessmentDTO;
import com.themuffinman.app.agent.dto.AgentClarificationContractDTO;
import com.themuffinman.app.agent.dto.AgentEndpointPlanDTO;
import com.themuffinman.app.agent.dto.AgentExecutionReadinessDTO;
import com.themuffinman.app.agent.dto.AgentIntentLineageDTO;
import com.themuffinman.app.agent.dto.AgentResolutionConfidenceDTO;
import com.themuffinman.app.agent.dto.AgentResolutionRequirementDTO;
import com.themuffinman.app.agent.sandbox.SandboxGenerationPlan;
import com.themuffinman.app.agent.sandbox.SandboxGenerationPlanner;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.AgentProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class AdminAgentPlaygroundService {

    private static final Map<String, EndpointPlan> ENDPOINT_PLAN_BY_INTENT = Map.ofEntries(
            Map.entry("create_circle_connection", new EndpointPlan("create_circle_request", "POST", "/circles/requests")),
            Map.entry("approve_application", new EndpointPlan("approve_application", "POST", "/quests/{questId}/applications/{applicationId}/approve")),
            Map.entry("delete_quest", new EndpointPlan("delete_quest", "DELETE", "/quests/{id}")),
            Map.entry("set_profile_current_location", new EndpointPlan("update_current_user", "PUT", "/app_users/me")),
            Map.entry("delete_user_as_admin", new EndpointPlan("delete_user_as_admin", "DELETE", "/app_users/{id}"))
    );

    private static final Map<String, IntentLineageTemplate> INTENT_LINEAGE_BY_INTENT = Map.ofEntries(
            Map.entry("create_circle_connection", new IntentLineageTemplate(
                    List.of("Send friend request to Tom", "Pošalji friend request Tomu"),
                    List.of("resolve_circle_recipient"),
                    List.of("create_circle_request"),
                    List.of("fail_closed_ambiguity", "recipient_acceptance_required"),
                    List.of("app_user_resolution_view", "circle_search_result_resolution_view")
            )),
            Map.entry("approve_application", new IntentLineageTemplate(
                    List.of("Approve the first applicant for my quest", "Approvaj onog tko se applicirao prvi"),
                    List.of("find_owned_quest_candidates", "inspect_owned_quest_pending_applications", "select_oldest_pending_application"),
                    List.of("approve_application"),
                    List.of("fail_closed_ambiguity", "multi_actor_authority", "oldest_pending_selection"),
                    List.of("quest_resolution_view", "quest_applications_selection_view", "quest_application_resolution_view")
            )),
            Map.entry("delete_quest", new IntentLineageTemplate(
                    List.of("Delete my quest Garden Help", "Obriši moj quest Taj i Taj"),
                    List.of("find_owned_quest_candidates"),
                    List.of("delete_quest"),
                    List.of("fail_closed_ambiguity", "destructive_confirmation"),
                    List.of("quest_resolution_view")
            )),
            Map.entry("set_profile_current_location", new IntentLineageTemplate(
                    List.of("Change my account location to my current location", "Promijeni lokaciju mog accounta na trenutnu"),
                    List.of("resolve_current_location_input"),
                    List.of("update_current_user"),
                    List.of("current_location_capability", "fail_closed_ambiguity"),
                    List.of("user_profile_resolution_view")
            )),
            Map.entry("delete_user_as_admin", new IntentLineageTemplate(
                    List.of("Delete user Tom", "Obriši user Tom"),
                    List.of("resolve_user_candidate"),
                    List.of("delete_user_as_admin"),
                    List.of("destructive_confirmation", "admin_authority", "fail_closed_ambiguity"),
                    List.of("app_user_resolution_view")
            ))
    );

    private final AgentProperties agentProperties;
    private final AdminAgentTextProvider adminAgentTextProvider;
    private final AdminAgentPromptTranslator localPromptTranslator;
    private final SandboxGenerationPlanner sandboxGenerationPlanner;

    public AdminAgentPlaygroundService(
            AgentProperties agentProperties,
            OpenAiAdminAgentClient adminAgentTextProvider,
            LocalAdminAgentPromptTranslator localPromptTranslator,
            SandboxGenerationPlanner sandboxGenerationPlanner
    ) {
        this.agentProperties = agentProperties;
        this.adminAgentTextProvider = adminAgentTextProvider;
        this.localPromptTranslator = localPromptTranslator;
        this.sandboxGenerationPlanner = sandboxGenerationPlanner;
    }

    public AdminAgentPlaygroundResponseDTO runPrompt(AdminAgentPlaygroundRequestDTO dto, AppUser currentUser) {
        validateAdmin(currentUser);
        validateRequest(dto);
        return analyzePrompt(dto.getPrompt());
    }

    public AdminAgentPlaygroundResponseDTO analyzePrompt(String rawPrompt) {
        if (rawPrompt == null || rawPrompt.isBlank()) {
            throw ServiceErrors.badRequest("Prompt is required");
        }

        String prompt = rawPrompt.trim();
        AdminAgentPromptTranslation translation = translatePrompt(prompt);
        String normalizedPrompt = translation.getTranslatedPrompt().trim().toLowerCase(Locale.ROOT);

        Set<String> suggestedWorkflows = new LinkedHashSet<>();
        Set<String> matchedSignals = new LinkedHashSet<>();
        Set<String> unresolvedInputs = new LinkedHashSet<>();
        List<AgentResolutionRequirementDTO> resolutionRequirements = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> nextSteps = new ArrayList<>();

        warnings.add("This playground is a planning surface only and does not execute backend mutations.");
        nextSteps.add("Keep the UI and backend contract stable first, then change providers behind the same endpoint.");
        nextSteps.add("Use a backend-managed OpenAI API key instead of relying on the ChatGPT consumer app.");
        if (!translation.isTranslationReliable()) {
            warnings.add("Prompt translation is not yet reliable for this language without a configured external provider.");
            unresolvedInputs.add("reliable English translation");
        }

        if (mentionsUserCreation(normalizedPrompt)) {
            matchedSignals.add("user_creation");
            suggestedWorkflows.add("create_user");
            suggestedWorkflows.add("create_user_as_admin");
            warnings.add("User creation prompts still need explicit uniqueness checks for email and username.");
            unresolvedInputs.add("unique email");
            unresolvedInputs.add("unique username");
        }

        if (mentionsAdminUserUpdate(normalizedPrompt)) {
            matchedSignals.add("admin_user_update");
            suggestedWorkflows.add("resolve_user_candidate");
            suggestedWorkflows.add("update_user_as_admin");
            resolutionRequirements.add(resolutionRequirement("user", "resolve_user_candidate", "app-user-directory", "exact_one_candidate", "stop_and_clarify", "GET /app_users"));
            warnings.add("Admin user updates still require one exact account target and must reuse backend full-update validation for email, username, optional password, role, and profile fields.");
            unresolvedInputs.add("exact user identity");
            unresolvedInputs.add("validated admin user update payload");
        }

        if (mentionsAdminUserDeletion(normalizedPrompt)) {
            matchedSignals.add("admin_user_deletion");
            suggestedWorkflows.add("resolve_user_candidate");
            suggestedWorkflows.add("delete_user_as_admin");
            resolutionRequirements.add(resolutionRequirement("user", "resolve_user_candidate", "app-user-directory", "exact_one_candidate", "stop_and_clarify", "GET /app_users"));
            warnings.add("Admin user deletion is destructive and must respect self-delete and last-admin protections after exact user resolution.");
            unresolvedInputs.add("exact user identity");
            unresolvedInputs.add("destructive confirmation");
        }

        boolean userCreationMentioned = mentionsUserCreation(normalizedPrompt);
        SandboxGenerationPlan sandboxGenerationPlan = sandboxGenerationPlanner.planFor(normalizedPrompt, userCreationMentioned);
        if (!sandboxGenerationPlan.isEmpty()) {
            matchedSignals.add("quest_generation");
            matchedSignals.addAll(sandboxGenerationPlan.matchedSignals());
            if (userCreationMentioned) {
                suggestedWorkflows.add("create_user_with_quests");
            }
            suggestedWorkflows.add("create_quest");
            suggestedWorkflows.add("create_user_with_quests");
            suggestedWorkflows.addAll(sandboxGenerationPlan.suggestedWorkflows());
            warnings.add("Batch quest generation must keep titles and descriptions meaningfully unique.");
            warnings.addAll(sandboxGenerationPlan.warnings());
            nextSteps.add("Review uniqueness policy and batch stop conditions before enabling execution.");
            nextSteps.addAll(sandboxGenerationPlan.nextSteps());
            unresolvedInputs.add("unique quest titles");
            unresolvedInputs.add("unique quest descriptions");
            unresolvedInputs.addAll(sandboxGenerationPlan.unresolvedInputs());
        }

        if (normalizedPrompt.contains("free quest") || normalizedPrompt.contains("besplat") || normalizedPrompt.contains("award") && normalizedPrompt.contains("0")) {
            matchedSignals.add("free_quest_pricing");
            suggestedWorkflows.add("create_quest");
            suggestedWorkflows.add("apply_to_quest");
            warnings.add("Free quests use award amount 0 and applications must omit proposed price.");
        }

        if (mentionsRelativeTime(normalizedPrompt)) {
            matchedSignals.add("relative_time");
            suggestedWorkflows.add("voice_prepare_scheduled_circle_only_quest_for_selected_people");
            warnings.add("Relative time instructions require a reliable caller timezone before mutation.");
            unresolvedInputs.add("caller timezone");
        }

        if (mentionsCircleOnlyAutomation(normalizedPrompt)) {
            matchedSignals.add("circle_only_flow");
            suggestedWorkflows.add("create_circle_only_quest_for_selected_people");
            suggestedWorkflows.add("prepare_circle_only_quest_flow_to_start");
            warnings.add("Selected workers must resolve to accepted connections before circle-only automation can continue.");
            unresolvedInputs.add("accepted connection ids");
        }

        if (mentionsConnectionRequest(normalizedPrompt)) {
            matchedSignals.add("connection_request");
            suggestedWorkflows.add("resolve_circle_recipient");
            suggestedWorkflows.add("create_circle_connection");
            resolutionRequirements.add(resolutionRequirement("user", "resolve_circle_recipient", "social-search", "exact_one_candidate", "stop_and_clarify", "GET /circles/search"));
            warnings.add("Connection-request automation still requires an exact recipient match and recipient-side acceptance before the users become contacts.");
            unresolvedInputs.add("exact recipient identity");
        }

        if (mentionsApplicationApproval(normalizedPrompt)) {
            matchedSignals.add("application_approval");
            suggestedWorkflows.add("find_owned_quest_candidates");
            suggestedWorkflows.add("inspect_owned_quest_pending_applications");
            resolutionRequirements.add(resolutionRequirement("quest", "find_owned_quest_candidates", "owned-quest-search", "exact_one_candidate", "stop_and_clarify", "GET /quests/search"));
            resolutionRequirements.add(resolutionRequirement("application", "inspect_owned_quest_pending_applications", "owned-quest-applications", "pending_application_required", "stop_and_clarify", "GET /quests/{questId}/applications/view"));
            warnings.add("Approval automation needs one exact owned quest target before any mutation can be considered.");
            unresolvedInputs.add("exact owned quest identity");

            if (mentionsFirstApplicantSelection(normalizedPrompt)) {
                matchedSignals.add("oldest_pending_selection");
                suggestedWorkflows.add("select_oldest_pending_application");
                warnings.add("Approve-the-first-applicant flows must use deterministic backend oldest-pending metadata instead of incidental UI ordering.");
            } else {
                warnings.add("Approval automation still needs a deterministic pending-application selection rule when more than one pending applicant exists.");
            }
            suggestedWorkflows.add("approve_application");
        }

        if (mentionsQuestDeletion(normalizedPrompt)) {
            matchedSignals.add("quest_deletion");
            suggestedWorkflows.add("find_owned_quest_candidates");
            suggestedWorkflows.add("delete_quest");
            resolutionRequirements.add(resolutionRequirement("quest", "find_owned_quest_candidates", "owned-quest-search", "exact_one_candidate", "stop_and_clarify", "GET /quests/search"));
            warnings.add("Quest deletion is destructive and requires exact owned-quest resolution plus explicit confirmation before mutation.");
            unresolvedInputs.add("exact owned quest identity");
            unresolvedInputs.add("destructive confirmation");
        }

        if (mentionsQuestUpdate(normalizedPrompt)) {
            matchedSignals.add("quest_update");
            suggestedWorkflows.add("find_owned_quest_candidates");
            suggestedWorkflows.add("update_quest");
            resolutionRequirements.add(resolutionRequirement("quest", "find_owned_quest_candidates", "owned-quest-search", "exact_one_candidate", "stop_and_clarify", "GET /quests/search"));
            warnings.add("Quest updates still require exact owned-quest resolution and must reuse backend quest validation, schedule, pricing, and visibility rules.");
            unresolvedInputs.add("exact owned quest identity");
            unresolvedInputs.add("validated quest update payload");
        }

        if (mentionsApplicationUpdate(normalizedPrompt)) {
            matchedSignals.add("application_update");
            suggestedWorkflows.add("find_my_pending_application_candidates");
            suggestedWorkflows.add("update_my_application");
            resolutionRequirements.add(resolutionRequirement("application", "find_my_pending_application_candidates", "my-pending-applications", "exact_one_candidate", "stop_and_clarify", "GET /quests/applications/me"));
            warnings.add("Application edits must resolve one of the acting user's pending applications and still match the target quest pricing mode.");
            unresolvedInputs.add("exact pending application identity");
            unresolvedInputs.add("validated application update payload");
        }

        if (mentionsApplicationWithdraw(normalizedPrompt)) {
            matchedSignals.add("application_withdraw");
            suggestedWorkflows.add("find_my_pending_application_candidates");
            suggestedWorkflows.add("withdraw_my_application");
            resolutionRequirements.add(resolutionRequirement("application", "find_my_pending_application_candidates", "my-pending-applications", "exact_one_candidate", "stop_and_clarify", "GET /quests/applications/me"));
            warnings.add("Application withdrawal must stop unless the acting user has exactly one matching pending application.");
            unresolvedInputs.add("exact pending application identity");
        }

        if (mentionsCurrentLocationUpdate(normalizedPrompt)) {
            matchedSignals.add("current_location_update");
            suggestedWorkflows.add("resolve_current_location_input");
            suggestedWorkflows.add("set_profile_current_location");
            resolutionRequirements.add(resolutionRequirement("location", "resolve_current_location_input", "device-geolocation-or-reverse-lookup", "single_coordinate_source", "stop_and_clarify", "POST /location/reverse-lookup"));
            warnings.add("Current-location updates require trusted device coordinates or reverse-lookup-backed address resolution before profile mutation.");
            unresolvedInputs.add("current device coordinates");
        }

        if (mentionsProfileSelfUpdate(normalizedPrompt) && !mentionsCurrentLocationUpdate(normalizedPrompt)) {
            matchedSignals.add("profile_self_update");
            suggestedWorkflows.add("set_profile_details");
            warnings.add("Profile self-update flows must reuse the authenticated self-update endpoint and still provide a fully validated profile payload.");
            unresolvedInputs.add("validated profile update payload");
        }

        if (mentionsCircleRequestCancellation(normalizedPrompt)) {
            matchedSignals.add("circle_request_cancellation");
            suggestedWorkflows.add("resolve_outgoing_circle_request");
            suggestedWorkflows.add("cancel_circle_request");
            resolutionRequirements.add(resolutionRequirement("circle_request", "resolve_outgoing_circle_request", "outgoing-pending-requests", "exact_one_candidate", "stop_and_clarify", "GET /circles/requests/outgoing"));
            warnings.add("Outgoing-request cancellation must resolve one exact pending request before mutation.");
            unresolvedInputs.add("exact outgoing request identity");
        }

        if (mentionsUserBlock(normalizedPrompt)) {
            matchedSignals.add("user_block");
            suggestedWorkflows.add("resolve_user_candidate");
            suggestedWorkflows.add("block_user");
            resolutionRequirements.add(resolutionRequirement("user", "resolve_user_candidate", "social-search", "exact_one_candidate", "stop_and_clarify", "GET /circles/search"));
            warnings.add("Block flows must resolve one exact user target and may replace existing social relations with a blocked state.");
            unresolvedInputs.add("exact user identity");
        }

        if (mentionsUserUnblock(normalizedPrompt)) {
            matchedSignals.add("user_unblock");
            suggestedWorkflows.add("resolve_user_candidate");
            suggestedWorkflows.add("unblock_user");
            resolutionRequirements.add(resolutionRequirement("user", "resolve_user_candidate", "blocked-user-list", "exact_one_candidate", "stop_and_clarify", "GET /circles/blocked"));
            warnings.add("Unblock flows must resolve one exact user from the acting user's current blocked list.");
            unresolvedInputs.add("exact blocked user identity");
        }

        if (mentionsCircleUpdate(normalizedPrompt)) {
            matchedSignals.add("circle_update");
            suggestedWorkflows.add("resolve_circle_candidate");
            suggestedWorkflows.add("update_circle");
            resolutionRequirements.add(resolutionRequirement("circle", "resolve_circle_candidate", "owned-circles", "exact_one_candidate", "stop_and_clarify", "GET /circles/groups"));
            warnings.add("Circle updates must resolve one owner-owned circle and still enforce duplicate-name checks.");
            unresolvedInputs.add("exact owned circle identity");
            unresolvedInputs.add("validated circle update payload");
        }

        if (mentionsCircleDeletion(normalizedPrompt)) {
            matchedSignals.add("circle_deletion");
            suggestedWorkflows.add("resolve_circle_candidate");
            suggestedWorkflows.add("delete_circle");
            resolutionRequirements.add(resolutionRequirement("circle", "resolve_circle_candidate", "owned-circles", "exact_one_candidate", "stop_and_clarify", "GET /circles/groups"));
            warnings.add("Circle deletion is destructive and requires exact owned-circle resolution plus explicit confirmation before mutation.");
            unresolvedInputs.add("exact owned circle identity");
            unresolvedInputs.add("destructive confirmation");
        }

        if (mentionsNewsReadAll(normalizedPrompt)) {
            matchedSignals.add("news_read_all");
            suggestedWorkflows.add("mark_all_news_read");
            warnings.add("Mark-all-news-read flows should only operate on the authenticated user's own news feed.");
        } else if (mentionsNewsReadItem(normalizedPrompt)) {
            matchedSignals.add("news_read_item");
            suggestedWorkflows.add("resolve_news_item_candidate");
            suggestedWorkflows.add("mark_news_item_read");
            resolutionRequirements.add(resolutionRequirement("news_item", "resolve_news_item_candidate", "my-news-feed", "exact_one_candidate", "stop_and_clarify", "GET /news/me"));
            warnings.add("Specific news-read flows must resolve one exact notification from the authenticated user's current news feed.");
            unresolvedInputs.add("exact news item identity");
        }

        if (mentionsChatIntent(normalizedPrompt)) {
            matchedSignals.add("chat_intent");
            suggestedWorkflows.add("resolve_user_candidate");
            suggestedWorkflows.add("open_chat_conversation");
            resolutionRequirements.add(resolutionRequirement("user", "resolve_user_candidate", "accepted-chat-contact", "exact_one_candidate", "stop_and_clarify", "GET /chat/workspace"));
            warnings.add("Chat automation must stop unless the target user is a current accepted contact.");
        }

        if (mentionsChatRead(normalizedPrompt)) {
            matchedSignals.add("chat_read");
            suggestedWorkflows.add("resolve_chat_conversation");
            suggestedWorkflows.add("mark_chat_conversation_read");
            resolutionRequirements.add(resolutionRequirement("conversation", "resolve_chat_conversation", "chat-workspace", "exact_one_candidate", "stop_and_clarify", "GET /chat/workspace"));
            warnings.add("Mark-as-read flows must resolve one exact currently accessible conversation from the acting user's chat workspace.");
            unresolvedInputs.add("exact conversation identity");
        }

        if (mentionsAdminApplicationUpdate(normalizedPrompt)) {
            matchedSignals.add("admin_application_update");
            suggestedWorkflows.add("find_admin_application_candidates");
            suggestedWorkflows.add("update_admin_application");
            resolutionRequirements.add(resolutionRequirement("application", "find_admin_application_candidates", "admin-application-search", "exact_one_candidate", "stop_and_clarify", "GET /admin/applications"));
            warnings.add("Admin application updates still require one exact application target and must reuse backend admin validation rules for message, price, and status updates.");
            unresolvedInputs.add("exact application identity");
            unresolvedInputs.add("validated admin application update payload");
        }

        if (mentionsAdminApplicationDeletion(normalizedPrompt)) {
            matchedSignals.add("admin_application_deletion");
            suggestedWorkflows.add("find_admin_application_candidates");
            suggestedWorkflows.add("delete_admin_application");
            resolutionRequirements.add(resolutionRequirement("application", "find_admin_application_candidates", "admin-application-search", "exact_one_candidate", "stop_and_clarify", "GET /admin/applications"));
            warnings.add("Admin application deletion is destructive and still depends on exact application resolution plus explicit confirmation.");
            unresolvedInputs.add("exact application identity");
            unresolvedInputs.add("destructive confirmation");
        }

        if (mentionsProfileIntent(normalizedPrompt)) {
            matchedSignals.add("profile_intent");
            suggestedWorkflows.add("resolve_user_candidate");
            suggestedWorkflows.add("open_user_profile");
            resolutionRequirements.add(resolutionRequirement("user", "resolve_user_candidate", "profile-search", "exact_one_candidate", "stop_and_clarify", "GET /app_users/{id}/profile-view"));
        }

        if (suggestedWorkflows.isEmpty()) {
            suggestedWorkflows.add("create_quest");
            matchedSignals.add("generic_quest_planning");
            nextSteps.add("Add more intent classification once you decide which admin or user prompts should graduate from planning into execution.");
        }

        boolean externalLlmConfigured = adminAgentTextProvider.isConfigured();
        String provider = externalLlmConfigured ? adminAgentTextProvider.providerName() : "mock";
        String summary = buildSummary(prompt, translation.getTranslatedPrompt(), suggestedWorkflows);
        AgentModelProfile summaryModelProfile = selectSummaryModelProfile(
                sandboxGenerationPlan,
                matchedSignals,
                suggestedWorkflows,
                unresolvedInputs,
                resolutionRequirements
        );

        if (externalLlmConfigured) {
            try {
                summary = adminAgentTextProvider.generatePlanningSummary(
                        prompt,
                        List.copyOf(suggestedWorkflows),
                        List.copyOf(matchedSignals),
                        List.copyOf(unresolvedInputs),
                        List.copyOf(warnings),
                        summaryModelProfile
                );
            } catch (RuntimeException exception) {
                warnings.add("OpenAI provider failed, so the playground fell back to the deterministic local planner.");
                String failureReason = exception.getMessage();
                if (failureReason != null && !failureReason.isBlank()) {
                    warnings.add("OpenAI failure detail: " + failureReason);
                }
                nextSteps.add("Check SIDEQUEST_AGENT_PROVIDER, SIDEQUEST_AGENT_MODEL, OPENAI_API_KEY, and OPENAI_BASE_URL if you expected live output.");
                provider = "mock";
                externalLlmConfigured = false;
            }
        } else if ("openai".equalsIgnoreCase(agentProperties.getProvider())) {
            warnings.add("OpenAI provider is selected but no backend API key is configured, so the deterministic local planner was used.");
            nextSteps.add("Set OPENAI_API_KEY on the backend if you want live model output from the same admin endpoint.");
        }

        return AdminAgentPlaygroundResponseDTO.builder()
                .provider(provider)
                .externalLlmConfigured(externalLlmConfigured)
                .promptSourceLanguage(translation.getSourceLanguage())
                .promptTranslationProvider(translation.getTranslationProvider())
                .promptTranslationApplied(translation.isTranslationApplied())
                .promptTranslationReliable(translation.isTranslationReliable())
                .originalPrompt(translation.getOriginalPrompt())
                .translatedPrompt(translation.getTranslatedPrompt())
                .title("Agent playground response")
                .summary(summary)
                .resolutionRequirements(List.copyOf(resolutionRequirements))
                .clarificationContract(buildClarificationContract(unresolvedInputs, translation))
                .executionReadiness(buildExecutionReadiness(normalizedPrompt, translation))
                .matchedSignals(List.copyOf(matchedSignals))
                .unresolvedInputs(List.copyOf(unresolvedInputs))
                .warnings(List.copyOf(warnings))
                .suggestedWorkflows(List.copyOf(suggestedWorkflows))
                .nextSteps(List.copyOf(nextSteps))
                .build();
    }

    public AdminAgentSimulationResponseDTO simulatePrompt(AdminAgentSimulationRequestDTO dto, AppUser currentUser) {
        AdminAgentPlaygroundResponseDTO planningResponse = runPrompt(
                AdminAgentPlaygroundRequestDTO.builder().prompt(dto.getPrompt()).build(),
                currentUser
        );

        String selectedIntentId = selectPrimaryIntent(planningResponse.getSuggestedWorkflows());
        AgentExecutionReadinessDTO executionReadiness = planningResponse.getExecutionReadiness();
        List<String> blockingReasons = new ArrayList<>(planningResponse.getUnresolvedInputs());
        if (!planningResponse.isPromptTranslationReliable()) {
            blockingReasons.add("translation is not reliable enough");
        }
        if (executionReadiness.isDestructiveConfirmationRequired()) {
            blockingReasons.add("destructive confirmation is still required");
        }
        if (executionReadiness.isMultiActorContextRequired()) {
            blockingReasons.add("multi-actor context is still required");
        }
        if (executionReadiness.isCurrentLocationCapabilityRequired()) {
            blockingReasons.add("current-location capability is still required");
        }

        return AdminAgentSimulationResponseDTO.builder()
                .planningOnly(true)
                .safeToExecute(blockingReasons.isEmpty() && planningResponse.getClarificationContract() != null && !planningResponse.getClarificationContract().isClarificationRequired())
                .promptSourceLanguage(planningResponse.getPromptSourceLanguage())
                .translatedPrompt(planningResponse.getTranslatedPrompt())
                .selectedIntentId(selectedIntentId)
                .resolutionConfidence(buildResolutionConfidence(planningResponse, selectedIntentId))
                .capabilityAssessments(buildCapabilityAssessments(planningResponse))
                .intentLineage(buildIntentLineage(selectedIntentId))
                .endpointPlan(buildEndpointPlan(selectedIntentId))
                .clarificationContract(planningResponse.getClarificationContract())
                .executionReadiness(executionReadiness)
                .matchedSignals(planningResponse.getMatchedSignals())
                .unresolvedInputs(planningResponse.getUnresolvedInputs())
                .blockingReasons(List.copyOf(new LinkedHashSet<>(blockingReasons)))
                .suggestedWorkflows(planningResponse.getSuggestedWorkflows())
                .build();
    }

    private AgentClarificationContractDTO buildClarificationContract(Set<String> unresolvedInputs, AdminAgentPromptTranslation translation) {
        boolean clarificationRequired = !unresolvedInputs.isEmpty() || !translation.isTranslationReliable();
        String reason = !translation.isTranslationReliable()
                ? "Translation is not reliable enough for fail-closed planning."
                : clarificationRequired
                ? "One or more required target-resolution fields are still unresolved."
                : "No clarification is required at the planning layer.";
        return AgentClarificationContractDTO.builder()
                .clarificationRequired(clarificationRequired)
                .failClosedOnAmbiguity(true)
                .reason(reason)
                .unresolvedFields(List.copyOf(unresolvedInputs))
                .build();
    }

    private AgentExecutionReadinessDTO buildExecutionReadiness(String normalizedPrompt, AdminAgentPromptTranslation translation) {
        return AgentExecutionReadinessDTO.builder()
                .planningOnly(true)
                .translationReady(translation.isTranslationReliable())
                .requiresExternalTranslationProvider(!translation.isTranslationReliable())
                .currentLocationCapabilityRequired(mentionsCurrentLocationUpdate(normalizedPrompt))
                .currentLocationCapabilityStatus(mentionsCurrentLocationUpdate(normalizedPrompt) ? "requires_device_coordinates_or_reverse_lookup" : "not_required")
                .destructiveConfirmationRequired(mentionsQuestDeletion(normalizedPrompt)
                        || mentionsCircleDeletion(normalizedPrompt)
                        || mentionsAdminApplicationDeletion(normalizedPrompt)
                        || mentionsAdminUserDeletion(normalizedPrompt))
                .multiActorContextRequired(mentionsCircleOnlyAutomation(normalizedPrompt)
                        || mentionsApplicationApproval(normalizedPrompt)
                        || mentionsConnectionAcceptance(normalizedPrompt)
                        || mentionsChatIntent(normalizedPrompt))
                .build();
    }

    private AgentModelProfile selectSummaryModelProfile(
            SandboxGenerationPlan sandboxGenerationPlan,
            Set<String> matchedSignals,
            Set<String> suggestedWorkflows,
            Set<String> unresolvedInputs,
            List<AgentResolutionRequirementDTO> resolutionRequirements
    ) {
        boolean creativeSummaryNeeded = !sandboxGenerationPlan.isEmpty()
                || matchedSignals.contains("application_approval")
                || matchedSignals.contains("quest_deletion")
                || matchedSignals.contains("circle_deletion")
                || matchedSignals.contains("admin_application_deletion")
                || matchedSignals.contains("admin_user_deletion")
                || matchedSignals.contains("circle_only_flow")
                || matchedSignals.contains("chat_intent")
                || matchedSignals.contains("current_location_update")
                || matchedSignals.contains("admin_user_update")
                || matchedSignals.size() >= 4
                || suggestedWorkflows.size() >= 5
                || unresolvedInputs.size() >= 4
                || resolutionRequirements.size() >= 2;

        return creativeSummaryNeeded ? AgentModelProfile.CREATIVE : AgentModelProfile.DEFAULT;
    }

    private AgentResolutionRequirementDTO resolutionRequirement(
            String entityType,
            String workflowId,
            String scope,
            String selectionRule,
            String ambiguityPolicy,
            String endpointHint
    ) {
        return AgentResolutionRequirementDTO.builder()
                .entityType(entityType)
                .workflowId(workflowId)
                .scope(scope)
                .selectionRule(selectionRule)
                .ambiguityPolicy(ambiguityPolicy)
                .endpointHint(endpointHint)
                .build();
    }

    private AdminAgentPromptTranslation translatePrompt(String prompt) {
        if (adminAgentTextProvider.isConfigured()) {
            try {
                return adminAgentTextProvider.translatePromptToEnglish(prompt);
            } catch (RuntimeException ignored) {
                return localPromptTranslator.translateForPlanning(prompt);
            }
        }
        return localPromptTranslator.translateForPlanning(prompt);
    }

    private void validateAdmin(AppUser currentUser) {
        if (currentUser == null || currentUser.getRole() != AppUserRole.ADMIN) {
            throw ServiceErrors.forbidden("Admin access is required");
        }
    }

    private void validateRequest(AdminAgentPlaygroundRequestDTO dto) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Agent playground request is required");
        }

        if (dto.getPrompt() == null || dto.getPrompt().isBlank()) {
            throw ServiceErrors.badRequest("Prompt is required");
        }

        if (dto.getPrompt().length() > agentProperties.getPromptMaxLength()) {
            throw ServiceErrors.badRequest("Prompt must be " + agentProperties.getPromptMaxLength() + " characters or less");
        }
    }

    private boolean mentionsUserCreation(String normalizedPrompt) {
        return (normalizedPrompt.contains("create user")
                || normalizedPrompt.contains("new user")
                || normalizedPrompt.contains("user account")
                || normalizedPrompt.contains("account")
                || normalizedPrompt.contains("korisnik")
                || normalizedPrompt.contains("napravi user")
                || normalizedPrompt.contains("novi user"))
                && (normalizedPrompt.contains("create")
                || normalizedPrompt.contains("napravi")
                || normalizedPrompt.contains("new")
                || normalizedPrompt.contains("novi"));
    }

    private boolean mentionsAdminUserUpdate(String normalizedPrompt) {
        return (normalizedPrompt.contains("user")
                || normalizedPrompt.contains("account")
                || normalizedPrompt.contains("korisnik"))
                && (normalizedPrompt.contains("update")
                || normalizedPrompt.contains("edit")
                || normalizedPrompt.contains("change")
                || normalizedPrompt.contains("uredi")
                || normalizedPrompt.contains("promijeni")
                || normalizedPrompt.contains("izmijeni"))
                && !mentionsProfileSelfUpdate(normalizedPrompt)
                && !mentionsCurrentLocationUpdate(normalizedPrompt)
                && !mentionsUserCreation(normalizedPrompt);
    }

    private boolean mentionsAdminUserDeletion(String normalizedPrompt) {
        return (normalizedPrompt.contains("user")
                || normalizedPrompt.contains("account")
                || normalizedPrompt.contains("korisnik"))
                && (normalizedPrompt.contains("delete")
                || normalizedPrompt.contains("remove")
                || normalizedPrompt.contains("obrisi")
                || normalizedPrompt.contains("obriši"));
    }

    private boolean mentionsConnectionRequest(String normalizedPrompt) {
        return normalizedPrompt.contains("friend request")
                || normalizedPrompt.contains("connection request")
                || normalizedPrompt.contains("connect with")
                || normalizedPrompt.contains("posalji friend request")
                || normalizedPrompt.contains("pošalji friend request")
                || normalizedPrompt.contains("dodaj prijatelja");
    }

    private boolean mentionsApplicationApproval(String normalizedPrompt) {
        return (normalizedPrompt.contains("approve")
                || normalizedPrompt.contains("approv")
                || normalizedPrompt.contains("approveaj")
                || normalizedPrompt.contains("approvaj")
                || normalizedPrompt.contains("odobri"))
                && (mentionsQuest(normalizedPrompt) || normalizedPrompt.contains("applicant") || normalizedPrompt.contains("application"));
    }

    private boolean mentionsApplicationUpdate(String normalizedPrompt) {
        return normalizedPrompt.contains("update my application")
                || normalizedPrompt.contains("edit my application")
                || normalizedPrompt.contains("change my application")
                || normalizedPrompt.contains("uredi moju prijavu")
                || normalizedPrompt.contains("promijeni moju prijavu")
                || normalizedPrompt.contains("izmijeni moju prijavu");
    }

    private boolean mentionsAdminApplicationUpdate(String normalizedPrompt) {
        return (normalizedPrompt.contains("admin application")
                || normalizedPrompt.contains("application")
                || normalizedPrompt.contains("prijavu")
                || normalizedPrompt.contains("application for"))
                && (normalizedPrompt.contains("update")
                || normalizedPrompt.contains("edit")
                || normalizedPrompt.contains("change")
                || normalizedPrompt.contains("uredi")
                || normalizedPrompt.contains("promijeni")
                || normalizedPrompt.contains("izmijeni"))
                && !mentionsApplicationUpdate(normalizedPrompt);
    }

    private boolean mentionsAdminApplicationDeletion(String normalizedPrompt) {
        return (normalizedPrompt.contains("application")
                || normalizedPrompt.contains("prijavu"))
                && (normalizedPrompt.contains("delete")
                || normalizedPrompt.contains("remove")
                || normalizedPrompt.contains("obrisi")
                || normalizedPrompt.contains("obriši"));
    }

    private boolean mentionsApplicationWithdraw(String normalizedPrompt) {
        return normalizedPrompt.contains("withdraw my application")
                || normalizedPrompt.contains("cancel my application")
                || normalizedPrompt.contains("povuci moju prijavu")
                || normalizedPrompt.contains("otkazi moju prijavu")
                || normalizedPrompt.contains("otkaži moju prijavu");
    }

    private boolean mentionsFirstApplicantSelection(String normalizedPrompt) {
        return normalizedPrompt.contains("first applicant")
                || normalizedPrompt.contains("first one")
                || normalizedPrompt.contains("oldest pending")
                || normalizedPrompt.contains("applied first")
                || normalizedPrompt.contains("applicirao prvi")
                || normalizedPrompt.contains("prijavio prvi")
                || normalizedPrompt.contains("onog tko se applicirao prvi")
                || normalizedPrompt.contains("onog tko se prijavio prvi")
                || normalizedPrompt.contains("prvog");
    }

    private boolean mentionsQuestDeletion(String normalizedPrompt) {
        return mentionsQuest(normalizedPrompt)
                && (normalizedPrompt.contains("delete")
                || normalizedPrompt.contains("remove")
                || normalizedPrompt.contains("obrisi")
                || normalizedPrompt.contains("obriši"));
    }

    private boolean mentionsQuestUpdate(String normalizedPrompt) {
        return mentionsQuest(normalizedPrompt)
                && (normalizedPrompt.contains("update")
                || normalizedPrompt.contains("edit")
                || normalizedPrompt.contains("change")
                || normalizedPrompt.contains("rename")
                || normalizedPrompt.contains("uredi")
                || normalizedPrompt.contains("promijeni")
                || normalizedPrompt.contains("izmijeni"));
    }

    private boolean mentionsCurrentLocationUpdate(String normalizedPrompt) {
        return normalizedPrompt.contains("current location")
                || normalizedPrompt.contains("my location")
                || normalizedPrompt.contains("use current location")
                || normalizedPrompt.contains("trenutnu lokaciju")
                || normalizedPrompt.contains("trenutna lokacija");
    }

    private boolean mentionsProfileSelfUpdate(String normalizedPrompt) {
        return normalizedPrompt.contains("update my profile")
                || normalizedPrompt.contains("edit my profile")
                || normalizedPrompt.contains("change my profile")
                || normalizedPrompt.contains("change my bio")
                || normalizedPrompt.contains("change my username")
                || normalizedPrompt.contains("change my email")
                || normalizedPrompt.contains("uredi moj profil")
                || normalizedPrompt.contains("promijeni moj profil")
                || normalizedPrompt.contains("promijeni moj bio")
                || normalizedPrompt.contains("promijeni moj username")
                || normalizedPrompt.contains("promijeni moj email");
    }

    private boolean mentionsCircleRequestCancellation(String normalizedPrompt) {
        return mentionsConnectionRequest(normalizedPrompt)
                && (normalizedPrompt.contains("cancel")
                || normalizedPrompt.contains("delete")
                || normalizedPrompt.contains("remove")
                || normalizedPrompt.contains("otkazi")
                || normalizedPrompt.contains("otkaži")
                || normalizedPrompt.contains("obrisi")
                || normalizedPrompt.contains("obriši"));
    }

    private boolean mentionsUserBlock(String normalizedPrompt) {
        return normalizedPrompt.contains("block user")
                || normalizedPrompt.contains("block ")
                || normalizedPrompt.contains("blokiraj");
    }

    private boolean mentionsUserUnblock(String normalizedPrompt) {
        return normalizedPrompt.contains("unblock user")
                || normalizedPrompt.contains("unblock ")
                || normalizedPrompt.contains("odblokiraj");
    }

    private boolean mentionsCircleUpdate(String normalizedPrompt) {
        return (normalizedPrompt.contains("circle")
                || normalizedPrompt.contains("group")
                || normalizedPrompt.contains("krug"))
                && (normalizedPrompt.contains("update")
                || normalizedPrompt.contains("edit")
                || normalizedPrompt.contains("rename")
                || normalizedPrompt.contains("change")
                || normalizedPrompt.contains("uredi")
                || normalizedPrompt.contains("promijeni")
                || normalizedPrompt.contains("preimenuj"));
    }

    private boolean mentionsCircleDeletion(String normalizedPrompt) {
        return (normalizedPrompt.contains("circle")
                || normalizedPrompt.contains("group")
                || normalizedPrompt.contains("krug"))
                && (normalizedPrompt.contains("delete")
                || normalizedPrompt.contains("remove")
                || normalizedPrompt.contains("obrisi")
                || normalizedPrompt.contains("obriši"));
    }

    private boolean mentionsChatIntent(String normalizedPrompt) {
        return normalizedPrompt.contains("chat with")
                || normalizedPrompt.contains("open chat")
                || normalizedPrompt.contains("send message")
                || normalizedPrompt.contains("pošalji poruku")
                || normalizedPrompt.contains("posalji poruku");
    }

    private boolean mentionsChatRead(String normalizedPrompt) {
        return (normalizedPrompt.contains("mark") && normalizedPrompt.contains("read")
                && (normalizedPrompt.contains("chat") || normalizedPrompt.contains("conversation")))
                || normalizedPrompt.contains("mark chat as read")
                || normalizedPrompt.contains("mark conversation as read")
                || normalizedPrompt.contains("mark as read")
                || normalizedPrompt.contains("oznaci chat kao procitan")
                || normalizedPrompt.contains("označi chat kao pročitan")
                || normalizedPrompt.contains("oznaci razgovor kao procitan")
                || normalizedPrompt.contains("označi razgovor kao pročitan");
    }

    private boolean mentionsNewsReadAll(String normalizedPrompt) {
        return (normalizedPrompt.contains("mark all") || normalizedPrompt.contains("read all"))
                && (normalizedPrompt.contains("news")
                || normalizedPrompt.contains("notifications")
                || normalizedPrompt.contains("updates")
                || normalizedPrompt.contains("obavijesti")
                || normalizedPrompt.contains("novosti"));
    }

    private boolean mentionsNewsReadItem(String normalizedPrompt) {
        return (normalizedPrompt.contains("mark") && normalizedPrompt.contains("read")
                && (normalizedPrompt.contains("news")
                || normalizedPrompt.contains("notification")
                || normalizedPrompt.contains("update")
                || normalizedPrompt.contains("obavijest")
                || normalizedPrompt.contains("novost")))
                || normalizedPrompt.contains("oznaci obavijest kao procitanu")
                || normalizedPrompt.contains("označi obavijest kao pročitanu");
    }

    private boolean mentionsProfileIntent(String normalizedPrompt) {
        return normalizedPrompt.contains("open profile")
                || normalizedPrompt.contains("show profile")
                || normalizedPrompt.contains("prikaži profil")
                || normalizedPrompt.contains("prikazi profil");
    }

    private boolean mentionsCircleOnlyAutomation(String normalizedPrompt) {
        return normalizedPrompt.contains("selected people")
                || normalizedPrompt.contains("selected workers")
                || normalizedPrompt.contains("circle-only")
                || normalizedPrompt.contains("circle only")
                || normalizedPrompt.contains("selected circle")
                || normalizedPrompt.contains("circle friends")
                || normalizedPrompt.contains("only for friends")
                || normalizedPrompt.contains("samo za prijatelje")
                || normalizedPrompt.contains("samo za krug")
                || normalizedPrompt.contains("only for selected");
    }

    private boolean mentionsQuest(String normalizedPrompt) {
        return normalizedPrompt.contains("quest")
                || normalizedPrompt.contains("questa")
                || normalizedPrompt.contains("questa ")
                || normalizedPrompt.contains("questove")
                || normalizedPrompt.contains("questova")
                || normalizedPrompt.contains("job");
    }

    private boolean mentionsRelativeTime(String normalizedPrompt) {
        return normalizedPrompt.contains("tomorrow")
                || normalizedPrompt.contains("next week")
                || normalizedPrompt.contains("tonight")
                || normalizedPrompt.contains("sutra");
    }

    private boolean mentionsConnectionAcceptance(String normalizedPrompt) {
        return normalizedPrompt.contains("accept friend request")
                || normalizedPrompt.contains("accept connection")
                || normalizedPrompt.contains("prihvati zahtjev")
                || normalizedPrompt.contains("prihvati request");
    }

    private String buildSummary(String prompt, String translatedPrompt, Set<String> suggestedWorkflows) {
        return "Prompt received: \"" + prompt + "\". "
                + "Translated planning prompt: \"" + translatedPrompt + "\". "
                + "The local planner matched it against current safety rules and suggests reviewing these workflows first: "
                + String.join(", ", suggestedWorkflows)
                + ".";
    }

    private String selectPrimaryIntent(List<String> suggestedWorkflows) {
        for (int index = suggestedWorkflows.size() - 1; index >= 0; index--) {
            String workflow = suggestedWorkflows.get(index);
            if (!(workflow.startsWith("resolve_")
                    || workflow.startsWith("find_")
                    || workflow.startsWith("inspect_")
                    || workflow.startsWith("select_")
                    || workflow.startsWith("open_"))) {
                return workflow;
            }
        }
        return suggestedWorkflows.isEmpty() ? null : suggestedWorkflows.getLast();
    }

    private AgentResolutionConfidenceDTO buildResolutionConfidence(AdminAgentPlaygroundResponseDTO planningResponse, String selectedIntentId) {
        int score = 100;
        List<String> reasons = new ArrayList<>();
        if (!planningResponse.isPromptTranslationReliable()) {
            score -= 45;
            reasons.add("translation is not reliable");
        }
        if (planningResponse.getClarificationContract() != null && planningResponse.getClarificationContract().isClarificationRequired()) {
            score -= 25;
            reasons.add("clarification is still required");
        }
        if (planningResponse.getExecutionReadiness().isDestructiveConfirmationRequired()) {
            score -= 10;
            reasons.add("destructive confirmation is still missing");
        }
        if (planningResponse.getClarificationContract() != null
                && planningResponse.getClarificationContract().isClarificationRequired()
                && planningResponse.getExecutionReadiness().isDestructiveConfirmationRequired()) {
            score -= 20;
            reasons.add("destructive intent still lacks exact target resolution");
        }
        if (planningResponse.getExecutionReadiness().isMultiActorContextRequired()) {
            score -= 10;
            reasons.add("multi-actor context is required");
        }
        if (planningResponse.getExecutionReadiness().isCurrentLocationCapabilityRequired()) {
            score -= 10;
            reasons.add("current-location capability is required");
        }
        if (selectedIntentId == null) {
            score -= 10;
            reasons.add("no primary intent could be selected");
        } else {
            reasons.add("selected intent: " + selectedIntentId);
        }
        score = Math.max(score, 0);
        return AgentResolutionConfidenceDTO.builder()
                .score(score)
                .tier(score >= 80 ? "high" : score >= 50 ? "medium" : "low")
                .reasons(List.copyOf(reasons))
                .build();
    }

    private List<AgentCapabilityAssessmentDTO> buildCapabilityAssessments(AdminAgentPlaygroundResponseDTO planningResponse) {
        List<AgentCapabilityAssessmentDTO> items = new ArrayList<>();
        items.add(capability("admin_authority", "available", "Admin-only surface is already enforced by the backend."));
        items.add(capability("external_translation_provider",
                planningResponse.isPromptTranslationReliable() ? "not_required_or_available" : "missing",
                planningResponse.isPromptTranslationReliable()
                        ? "Current prompt can be planned safely without an extra translation dependency."
                        : "A reliable external translation provider is still required."));
        items.add(capability("current_location",
                planningResponse.getExecutionReadiness().isCurrentLocationCapabilityRequired() ? "missing" : "not_required",
                planningResponse.getExecutionReadiness().isCurrentLocationCapabilityRequired()
                        ? "Current device coordinates or reverse-lookup backed data are still missing."
                        : "Current-location capability is not required for this prompt."));
        items.add(capability("second_actor_context",
                planningResponse.getExecutionReadiness().isMultiActorContextRequired() ? "missing" : "not_required",
                planningResponse.getExecutionReadiness().isMultiActorContextRequired()
                        ? "Another actor's authority or acceptance is still required."
                        : "No second-actor context is required for this prompt."));
        items.add(capability("destructive_confirmation",
                planningResponse.getExecutionReadiness().isDestructiveConfirmationRequired() ? "missing" : "not_required",
                planningResponse.getExecutionReadiness().isDestructiveConfirmationRequired()
                        ? "Explicit destructive confirmation is still required."
                        : "Destructive confirmation is not required for this prompt."));
        return List.copyOf(items);
    }

    private AgentCapabilityAssessmentDTO capability(String capabilityId, String status, String reason) {
        return AgentCapabilityAssessmentDTO.builder()
                .capabilityId(capabilityId)
                .status(status)
                .reason(reason)
                .build();
    }

    private AgentIntentLineageDTO buildIntentLineage(String selectedIntentId) {
        IntentLineageTemplate template = INTENT_LINEAGE_BY_INTENT.get(selectedIntentId);
        if (template == null) {
            return null;
        }
        return AgentIntentLineageDTO.builder()
                .intentId(selectedIntentId)
                .sourcePromptExamples(template.sourcePromptExamples())
                .resolutionWorkflows(template.resolutionWorkflows())
                .targetEndpoints(template.targetEndpoints())
                .safetyPolicies(template.safetyPolicies())
                .expectedReadModels(template.expectedReadModels())
                .build();
    }

    private List<AgentEndpointPlanDTO> buildEndpointPlan(String selectedIntentId) {
        EndpointPlan plan = ENDPOINT_PLAN_BY_INTENT.get(selectedIntentId);
        if (plan == null) {
            return List.of();
        }
        return List.of(AgentEndpointPlanDTO.builder()
                .endpointId(plan.endpointId())
                .method(plan.method())
                .path(plan.path())
                .build());
    }

    private record EndpointPlan(String endpointId, String method, String path) {
    }

    private record IntentLineageTemplate(
            List<String> sourcePromptExamples,
            List<String> resolutionWorkflows,
            List<String> targetEndpoints,
            List<String> safetyPolicies,
            List<String> expectedReadModels
    ) {
    }
}
