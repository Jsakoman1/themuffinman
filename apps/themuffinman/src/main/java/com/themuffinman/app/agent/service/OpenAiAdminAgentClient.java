package com.themuffinman.app.agent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.AgentProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiAdminAgentClient implements AdminAgentTextProvider {

    private final AgentProperties agentProperties;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    public OpenAiAdminAgentClient(AgentProperties agentProperties) {
        this.agentProperties = agentProperties;
    }

    @Override
    public String providerName() {
        return "openai";
    }

    @Override
    public boolean isConfigured() {
        return providerName().equalsIgnoreCase(agentProperties.getProvider())
                && agentProperties.getApiKey() != null
                && !agentProperties.getApiKey().isBlank();
    }

    @Override
    public String generatePlanningSummary(
            String prompt,
            List<String> suggestedWorkflows,
            List<String> matchedSignals,
            List<String> unresolvedInputs,
            List<String> warnings
    ) {
        if (!isConfigured()) {
            throw ServiceErrors.badRequest("OpenAI provider is not configured");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", agentProperties.getModel());
        payload.put("input", buildInput(prompt, suggestedWorkflows, matchedSignals, unresolvedInputs, warnings));
        payload.put("reasoning", Map.of("effort", "low"));
        payload.put("text", Map.of("verbosity", "low"));

        String responseBody;
        try {
            responseBody = restClient.post()
                    .uri(agentProperties.getBaseUrl() + "/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException exception) {
            throw ServiceErrors.badRequest("OpenAI request failed: " + exception.getMessage());
        }

        JsonNode response = parseResponseBody(responseBody);
        String outputText = extractOutputText(response);
        if (outputText == null || outputText.isBlank()) {
            throw ServiceErrors.badRequest("OpenAI response did not include usable text output");
        }

        return outputText.trim();
    }

    @Override
    public AdminAgentPromptTranslation translatePromptToEnglish(String prompt) {
        if (!isConfigured()) {
            throw ServiceErrors.badRequest("OpenAI provider is not configured");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", agentProperties.getModel());
        payload.put("input", """
                Translate the following admin agent prompt into concise English for backend intent planning.
                Preserve names, ids, and quoted literals.
                Return JSON with keys sourceLanguage and translatedPrompt.

                Prompt:
                %s
                """.formatted(prompt));
        payload.put("reasoning", Map.of("effort", "low"));
        payload.put("text", Map.of("verbosity", "low"));

        String responseBody;
        try {
            responseBody = restClient.post()
                    .uri(agentProperties.getBaseUrl() + "/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + agentProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException exception) {
            throw ServiceErrors.badRequest("OpenAI translation request failed: " + exception.getMessage());
        }

        JsonNode response = parseResponseBody(responseBody);
        String outputText = extractOutputText(response);
        if (outputText == null || outputText.isBlank()) {
            throw ServiceErrors.badRequest("OpenAI translation response did not include usable text output");
        }

        try {
            JsonNode translationNode = objectMapper.readTree(outputText);
            String sourceLanguage = translationNode.path("sourceLanguage").asText("unknown");
            String translatedPrompt = translationNode.path("translatedPrompt").asText(prompt);
            return AdminAgentPromptTranslation.builder()
                    .sourceLanguage(sourceLanguage)
                    .originalPrompt(prompt)
                    .translatedPrompt(translatedPrompt)
                    .translationProvider(providerName())
                    .translationApplied(!prompt.equals(translatedPrompt))
                    .translationReliable(true)
                    .build();
        } catch (Exception exception) {
            throw ServiceErrors.badRequest("OpenAI translation parsing failed: " + exception.getMessage());
        }
    }

    private String buildInput(
            String prompt,
            List<String> suggestedWorkflows,
            List<String> matchedSignals,
            List<String> unresolvedInputs,
            List<String> warnings
    ) {
        return """
                You are assisting an internal admin agent playground for a backend-centric product.
                Do not claim to execute mutations.
                Keep the answer concise and operational.
                Use plain English.
                Treat the deterministic backend planner as authoritative.
                Do not invent workflow ids, request types, entity names, prerequisites, or unresolved inputs that are not listed below.
                Only refer to workflow ids from the allowed workflow list below.
                If something is missing, say it is unresolved instead of guessing.
                Mention pricing-mode constraints only when the deterministic signals or unresolved inputs already imply them.
                Keep the response as one short planning note.

                Allowed workflow ids: %s
                Matched deterministic signals: %s
                Unresolved required inputs: %s
                Current deterministic warnings: %s

                Admin prompt:
                %s
                """.formatted(
                String.join(", ", suggestedWorkflows),
                String.join(", ", matchedSignals),
                String.join(", ", unresolvedInputs),
                String.join(" | ", warnings),
                prompt
        );
    }

    private JsonNode parseResponseBody(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw ServiceErrors.badRequest("OpenAI response body was empty");
        }

        try {
            return objectMapper.readTree(responseBody);
        } catch (Exception exception) {
            throw ServiceErrors.badRequest("OpenAI response parsing failed: " + exception.getMessage());
        }
    }

    private String extractOutputText(JsonNode response) {
        if (response == null || response.isNull()) {
            return null;
        }

        JsonNode outputTextNode = response.get("output_text");
        if (outputTextNode != null && outputTextNode.isTextual()) {
            return outputTextNode.asText();
        }

        JsonNode outputNode = response.get("output");
        if (outputNode == null || !outputNode.isArray()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (JsonNode item : outputNode) {
            JsonNode contentNode = item.get("content");
            if (contentNode == null || !contentNode.isArray()) {
                continue;
            }

            for (JsonNode contentItem : contentNode) {
                JsonNode textNode = contentItem.get("text");
                if (textNode != null && textNode.isTextual()) {
                    if (!builder.isEmpty()) {
                        builder.append("\n\n");
                    }
                    builder.append(textNode.asText().trim());
                } else if (textNode != null && textNode.isObject()) {
                    JsonNode valueNode = textNode.get("value");
                    if (valueNode != null && valueNode.isTextual()) {
                        if (!builder.isEmpty()) {
                            builder.append("\n\n");
                        }
                        builder.append(valueNode.asText().trim());
                    }
                }
            }
        }

        return builder.isEmpty() ? null : builder.toString();
    }
}
