package com.themuffinman.app.docs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentOperatingModelValidationTest {

    @Test
    void operatingModelAndImplementationControlExist() throws Exception {
        Path repoRoot = Path.of("..").toAbsolutePath().normalize().getParent();
        Path model = repoRoot.resolve("docs/agent-operating-model.yaml");
        Path control = repoRoot.resolve("docs/implementation-control.md");

        assertTrue(Files.exists(model), "agent-operating-model.yaml must exist");
        assertTrue(Files.exists(control), "implementation-control.md must exist");
        assertTrue(Files.readString(control).contains("single active workflow"));
        new ObjectMapper(new YAMLFactory()).readTree(Files.readString(model));

        Path presentationContract = repoRoot.resolve("docs/vision-presentation-contract.yaml");
        assertTrue(Files.exists(presentationContract), "vision-presentation-contract.yaml must exist");
        JsonNode contract = new ObjectMapper(new YAMLFactory()).readTree(Files.readString(presentationContract));
        assertEquals(1, contract.path("version").asInt());
        assertEquals("phone_handoff", contract.path("device_matrix").path("watch").path("allowed_actions").get(5).asText());
        assertTrue(contract.path("field_classification").path("diagnostic_only").size() > 0);
    }
}
