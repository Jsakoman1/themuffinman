package com.themuffinman.app.vision.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.config.VoiceProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiVoiceClient {

    private final VoiceProperties voiceProperties;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    public OpenAiVoiceClient(VoiceProperties voiceProperties) {
        this.voiceProperties = voiceProperties;
    }

    public boolean isConfigured() {
        return "openai".equalsIgnoreCase(voiceProperties.getProvider())
                && voiceProperties.getApiKey() != null
                && !voiceProperties.getApiKey().isBlank();
    }

    public DashboardVoiceTranscriptionResult transcribe(MultipartFile audioFile) {
        if (!isConfigured()) {
            throw ServiceErrors.badRequest("OpenAI voice provider is not configured");
        }

        MultiValueMap<String, Object> payload = new LinkedMultiValueMap<>();
        payload.add("model", voiceProperties.getTranscriptionModel());
        payload.add("file", new MultipartFileResource(audioFile));
        if (voiceProperties.getPreferredLocale() != null && !voiceProperties.getPreferredLocale().isBlank()) {
            payload.add("language", voiceProperties.getPreferredLocale());
        }
        payload.add("response_format", "json");

        String responseBody;
        try {
            responseBody = restClient.post()
                    .uri(voiceProperties.getBaseUrl() + "/audio/transcriptions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + voiceProperties.getApiKey())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(payload)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException exception) {
            throw ServiceErrors.badRequest("OpenAI transcription request failed: " + exception.getMessage());
        }

        JsonNode response = parseResponse(responseBody);
        String text = extractText(response);
        if (text == null || text.isBlank()) {
            throw ServiceErrors.badRequest("OpenAI transcription response did not include text");
        }

        return new DashboardVoiceTranscriptionResult(text.trim(), "openai", voiceProperties.getTranscriptionModel());
    }

    public byte[] synthesize(String text) {
        if (!isConfigured()) {
            throw ServiceErrors.badRequest("OpenAI voice provider is not configured");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", voiceProperties.getSpeechModel());
        payload.put("voice", voiceProperties.getSpeechVoice());
        payload.put("input", text);
        payload.put("response_format", "mp3");

        byte[] audio;
        try {
            audio = restClient.post()
                    .uri(voiceProperties.getBaseUrl() + "/audio/speech")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + voiceProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .body(payload)
                    .retrieve()
                    .body(byte[].class);
        } catch (RestClientException exception) {
            throw ServiceErrors.badRequest("OpenAI speech synthesis request failed: " + exception.getMessage());
        }

        if (audio == null || audio.length == 0) {
            throw ServiceErrors.badRequest("OpenAI speech synthesis response was empty");
        }

        return audio;
    }

    private JsonNode parseResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw ServiceErrors.badRequest("OpenAI voice response body was empty");
        }

        try {
            return objectMapper.readTree(responseBody);
        } catch (Exception exception) {
            throw ServiceErrors.badRequest("OpenAI voice response parsing failed: " + exception.getMessage());
        }
    }

    private String extractText(JsonNode response) {
        if (response == null || response.isNull()) {
            return null;
        }

        JsonNode textNode = response.get("text");
        if (textNode != null && textNode.isTextual()) {
            return textNode.asText();
        }

        JsonNode outputNode = response.get("output");
        if (outputNode != null && outputNode.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : outputNode) {
                JsonNode contentNode = item.get("content");
                if (contentNode == null || !contentNode.isArray()) {
                    continue;
                }

                for (JsonNode contentItem : contentNode) {
                    JsonNode valueNode = contentItem.get("text");
                    if (valueNode != null && valueNode.isTextual()) {
                        if (!builder.isEmpty()) {
                            builder.append("\n");
                        }
                        builder.append(valueNode.asText().trim());
                    }
                }
            }
            if (!builder.isEmpty()) {
                return builder.toString();
            }
        }

        return null;
    }

    public record DashboardVoiceTranscriptionResult(String text, String provider, String model) {
    }

    private static final class MultipartFileResource extends ByteArrayResource {
        private final String filename;

        private MultipartFileResource(MultipartFile multipartFile) {
            super(readBytes(multipartFile));
            this.filename = multipartFile.getOriginalFilename() != null ? multipartFile.getOriginalFilename() : "audio.webm";
        }

        @Override
        public String getFilename() {
            return filename;
        }

        private static byte[] readBytes(MultipartFile multipartFile) {
            try {
                return multipartFile.getBytes();
            } catch (IOException exception) {
                throw ServiceErrors.badRequest("Unable to read uploaded audio file: " + exception.getMessage());
            }
        }
    }
}
