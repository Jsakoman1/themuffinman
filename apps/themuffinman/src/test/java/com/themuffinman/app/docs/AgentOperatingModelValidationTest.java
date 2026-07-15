package com.themuffinman.app.docs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

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
    }
}
