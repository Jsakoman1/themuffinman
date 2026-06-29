package com.themuffinman.app.docs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowStateMachineCatalogTest {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final Set<String> SPECIAL_TARGETS = Set.of("previous_status", "any_declared_state");
    private static final Set<String> PLACEHOLDER_INTENTS = Set.of("none", "future_booking_flow");

    @Test
    void catalogStatesAndTransitionsAreValid() throws Exception {
        Path repoRoot = findRepoRoot();
        JsonNode catalog = YAML_MAPPER.readTree(Files.readString(repoRoot.resolve("docs/workflow-state-machines.yaml")));
        JsonNode intentsCatalog = YAML_MAPPER.readTree(Files.readString(repoRoot.resolve("docs/agent-operating-model/sections/intents.yaml")));
        Set<String> mutatingIntentIds = readMutatingIntentIds(intentsCatalog);

        assertEquals(1, catalog.path("version").asInt(), "Unexpected workflow state-machine catalog version");

        Set<String> machineIds = new LinkedHashSet<>();
        for (JsonNode machine : catalog.path("stateMachines")) {
            String machineId = machine.path("id").asText();
            assertFalse(machineId.isBlank(), "State machine id is required");
            assertTrue(machineIds.add(machineId), () -> "Duplicate state machine id: " + machineId);

            List<String> states = readTextList(machine.path("states"));
            assertFalse(states.isEmpty(), () -> "State machine must declare states: " + machineId);
            assertEquals(states.size(), new LinkedHashSet<>(states).size(), () -> "Duplicate states in " + machineId);

            String initialState = machine.path("initialState").asText();
            assertTrue(states.contains(initialState), () -> "Initial state is not declared for " + machineId + ": " + initialState);
            for (String terminalState : readTextList(machine.path("terminalStates"))) {
                assertTrue(states.contains(terminalState), () -> "Terminal state is not declared for " + machineId + ": " + terminalState);
            }

            validateEnumStateSource(machine, states);
            validateTransitions(machineId, machine.path("transitions"), states, mutatingIntentIds);
        }
    }

    private static void validateEnumStateSource(JsonNode machine, List<String> states) throws ClassNotFoundException {
        JsonNode stateSource = machine.path("stateSource");
        String javaClass = stateSource.path("javaClass").asText();
        if ("none".equals(javaClass)) {
            return;
        }

        Class<?> stateClass = Class.forName(javaClass);
        if (!stateClass.isEnum()) {
            return;
        }

        List<String> enumStates = new ArrayList<>();
        for (Object constant : stateClass.getEnumConstants()) {
            enumStates.add(((Enum<?>) constant).name());
        }
        assertEquals(enumStates, states, () -> "Catalog states must match enum constants for " + javaClass);
    }

    private static void validateTransitions(String machineId, JsonNode transitions, List<String> states, Set<String> mutatingIntentIds) {
        assertFalse(transitions.isEmpty(), () -> "State machine must declare transitions: " + machineId);

        Set<String> transitionIds = new LinkedHashSet<>();
        for (JsonNode transition : transitions) {
            String transitionId = transition.path("id").asText();
            assertFalse(transitionId.isBlank(), () -> "Transition id is required in " + machineId);
            assertTrue(transitionIds.add(transitionId), () -> "Duplicate transition id in " + machineId + ": " + transitionId);

            for (String fromState : readTextList(transition.path("from"))) {
                assertTrue(states.contains(fromState), () -> "Transition " + transitionId + " has unknown from state: " + fromState);
            }

            String toState = transition.path("to").asText();
            assertTrue(states.contains(toState) || SPECIAL_TARGETS.contains(toState),
                    () -> "Transition " + transitionId + " has unknown to state: " + toState);

            String intentId = transition.path("intentId").asText();
            assertFalse(intentId.isBlank(), () -> "Transition intentId is required: " + transitionId);
            if (!PLACEHOLDER_INTENTS.contains(intentId)) {
                assertTrue(mutatingIntentIds.contains(intentId),
                        () -> "Transition " + machineId + "." + transitionId
                                + " must reference a known mutating agent intent: " + intentId);
            }
            assertFalse(transition.path("authority").asText().isBlank(), () -> "Transition authority is required: " + transitionId);
        }
    }

    private static Set<String> readMutatingIntentIds(JsonNode intentsCatalog) {
        Set<String> intentIds = new LinkedHashSet<>();
        for (JsonNode intent : intentsCatalog.path("intents")) {
            String intentId = intent.path("id").asText();
            assertFalse(intentId.isBlank(), "Agent intent id is required");
            if (intent.path("mutating").asBoolean(false)) {
                intentIds.add(intentId);
            }
        }
        assertFalse(intentIds.isEmpty(), "Agent operating model must declare mutating intents");
        return intentIds;
    }

    private static List<String> readTextList(JsonNode node) {
        List<String> values = new ArrayList<>();
        for (JsonNode child : node) {
            values.add(child.asText());
        }
        return values;
    }

    private static Path findRepoRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("docs/workflow-state-machines.yaml"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not find repository root");
    }
}
