package com.themuffinman.app.docs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentOperatingModelValidationTest {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Test
    void yamlMatchesSchemaAndReferencedContracts() throws Exception {
        Path repoRoot = findRepoRoot();
        Path yamlPath = repoRoot.resolve("docs/agent-operating-model.yaml");
        Path schemaPath = repoRoot.resolve("docs/agent-operating-model.schema.json");

        assertTrue(Files.exists(yamlPath), "agent-operating-model.yaml must exist");
        assertTrue(Files.exists(schemaPath), "agent-operating-model.schema.json must exist");

        JsonNode yaml = YAML_MAPPER.readTree(Files.readString(yamlPath));
        JsonNode schemaNode = JSON_MAPPER.readTree(Files.readString(schemaPath));
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
                .getSchema(schemaNode);
        Set<ValidationMessage> validationMessages = schema.validate(yaml);
        assertTrue(validationMessages.isEmpty(), () -> "Schema validation failed: " + validationMessages);

        Map<String, String> sourceFiles = readSourceFiles(yaml);
        assertFalse(sourceFiles.isEmpty(), "source_of_truth.files must not be empty");
        sourceFiles.values().forEach(path -> assertTrue(Files.exists(repoRoot.resolve(path)), "Missing referenced source file: " + path));

        Map<String, JsonNode> endpoints = readById(yaml.path("api").path("endpoints"));
        assertFalse(endpoints.isEmpty(), "api.endpoints must not be empty");

        validateEnums(yaml.path("enums"));
        validateEndpoints(endpoints);
        validatePolicies(yaml.path("policies"), sourceFiles.keySet());
        validateIntents(yaml.path("intents"), sourceFiles.keySet(), endpoints.keySet());
        validateDocumentationSync(yaml.path("documentation_sync"), repoRoot, sourceFiles.keySet());
    }

    private static void validateEnums(JsonNode enumsNode) throws ClassNotFoundException {
        for (JsonNode enumNode : enumsNode) {
            String javaClass = enumNode.path("java_class").asText();
            Class<?> clazz = Class.forName(javaClass);
            assertTrue(clazz.isEnum(), () -> javaClass + " must be an enum");

            Object[] enumConstants = clazz.getEnumConstants();
            List<String> actualValues = new ArrayList<>();
            for (Object enumConstant : enumConstants) {
                actualValues.add(((Enum<?>) enumConstant).name());
            }

            List<String> documentedValues = new ArrayList<>();
            enumNode.path("values").forEach(node -> documentedValues.add(node.asText()));
            assertEquals(actualValues, documentedValues, () -> "Enum values out of sync for " + javaClass);
        }
    }

    private static void validateEndpoints(Map<String, JsonNode> endpoints) throws Exception {
        for (Map.Entry<String, JsonNode> entry : endpoints.entrySet()) {
            JsonNode endpointNode = entry.getValue();
            String controllerClassName = endpointNode.path("controller_class").asText();
            String handlerMethodName = endpointNode.path("handler_method").asText();
            String documentedMethod = endpointNode.path("method").asText();
            String documentedPath = endpointNode.path("path").asText();

            Class<?> controllerClass = Class.forName(controllerClassName);
            Method handlerMethod = findSingleMethod(controllerClass, handlerMethodName);
            assertNotNull(handlerMethod, () -> "Missing handler method " + handlerMethodName + " on " + controllerClassName);

            String classPath = extractClassPath(controllerClass);
            Mapping mapping = extractMethodMapping(handlerMethod);
            assertEquals(documentedMethod, mapping.httpMethod(), () -> "HTTP method mismatch for endpoint " + entry.getKey());
            assertEquals(normalizePath(classPath + mapping.path()), documentedPath, () -> "Path mismatch for endpoint " + entry.getKey());
        }
    }

    private static void validateIntents(JsonNode intentsNode, Set<String> sourceIds, Set<String> endpointIds) {
        List<JsonNode> intentNodes = iterable(intentsNode);
        Set<String> knownIntentIds = intentNodes.stream()
                .map(node -> node.path("id").asText())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        assertEquals(intentNodes.size(), knownIntentIds.size(), "Intent ids must be unique");

        Set<String> validatedIntentIds = new LinkedHashSet<>();
        for (JsonNode intentNode : intentNodes) {
            String intentId = intentNode.path("id").asText();
            assertTrue(validatedIntentIds.add(intentId), () -> "Duplicate intent id: " + intentId);

            int previousStep = 0;
            for (JsonNode workflowStep : intentNode.path("workflow")) {
                int step = workflowStep.path("step").asInt();
                assertTrue(step > previousStep, () -> "Workflow steps must be strictly increasing for intent " + intentId);
                previousStep = step;

                if (workflowStep.hasNonNull("endpoint_id")) {
                    assertTrue(endpointIds.contains(workflowStep.path("endpoint_id").asText()),
                            () -> "Unknown endpoint_id in intent " + intentId + ": " + workflowStep.path("endpoint_id").asText());
                }

                if (workflowStep.hasNonNull("intent_ref")) {
                    assertTrue(knownIntentIds.contains(workflowStep.path("intent_ref").asText()),
                            () -> "Unknown intent_ref in intent " + intentId + ": " + workflowStep.path("intent_ref").asText());
                }

                for (JsonNode sourceRefNode : workflowStep.path("source_refs")) {
                    String sourceRef = sourceRefNode.asText();
                    assertTrue(sourceIds.contains(sourceRef), () -> "Unknown source_ref in intent " + intentId + ": " + sourceRef);
                }
            }
        }
    }

    private static void validatePolicies(JsonNode policiesNode, Set<String> sourceIds) {
        for (JsonNode edgeCaseNode : policiesNode.path("edge_cases")) {
            for (JsonNode sourceRefNode : edgeCaseNode.path("source_refs")) {
                String sourceRef = sourceRefNode.asText();
                assertTrue(sourceIds.contains(sourceRef), () -> "Unknown policy source_ref: " + sourceRef);
            }
        }
    }

    private static void validateDocumentationSync(JsonNode documentationSyncNode, Path repoRoot, Set<String> sourceIds) throws Exception {
        for (JsonNode requiredDocNode : documentationSyncNode.path("required_docs")) {
            String path = requiredDocNode.asText();
            assertTrue(Files.exists(repoRoot.resolve(path)), () -> "Missing required documentation file: " + path);
        }

        for (JsonNode requiredTestNode : documentationSyncNode.path("required_tests")) {
            String path = requiredTestNode.asText();
            assertTrue(Files.exists(repoRoot.resolve(path)), () -> "Missing required validation or domain test: " + path);
        }

        Set<String> validatedRuleIds = new LinkedHashSet<>();
        for (JsonNode ruleNode : documentationSyncNode.path("rules")) {
            String ruleId = ruleNode.path("id").asText();
            assertTrue(validatedRuleIds.add(ruleId), () -> "Duplicate documentation_sync rule id: " + ruleId);

            for (JsonNode sourceRefNode : ruleNode.path("source_refs")) {
                String sourceRef = sourceRefNode.asText();
                assertTrue(sourceIds.contains(sourceRef), () -> "Unknown documentation_sync source_ref: " + sourceRef);
            }

            for (JsonNode docPathNode : ruleNode.path("doc_paths")) {
                String docPath = docPathNode.asText();
                Path resolvedDocPath = repoRoot.resolve(docPath);
                assertTrue(Files.exists(resolvedDocPath), () -> "Missing documentation_sync doc path: " + docPath);
                String content = Files.readString(resolvedDocPath);
                for (JsonNode expectedTextNode : ruleNode.path("must_contain_all")) {
                    String expectedText = expectedTextNode.asText();
                    assertTrue(content.contains(expectedText),
                            () -> "Missing required documentation text for rule " + ruleId + " in " + docPath + ": " + expectedText);
                }
            }
        }
    }

    private static Map<String, String> readSourceFiles(JsonNode yaml) {
        return iterable(yaml.path("source_of_truth").path("files")).stream()
                .collect(Collectors.toMap(
                        node -> node.path("id").asText(),
                        node -> node.path("path").asText(),
                        (left, right) -> left
                ));
    }

    private static Map<String, JsonNode> readById(JsonNode arrayNode) {
        return iterable(arrayNode).stream()
                .collect(Collectors.toMap(
                        node -> node.path("id").asText(),
                        node -> node,
                        (left, right) -> left
                ));
    }

    private static List<JsonNode> iterable(JsonNode arrayNode) {
        List<JsonNode> nodes = new ArrayList<>();
        Iterator<JsonNode> iterator = arrayNode.elements();
        while (iterator.hasNext()) {
            nodes.add(iterator.next());
        }
        return nodes;
    }

    private static Method findSingleMethod(Class<?> controllerClass, String handlerMethodName) {
        List<Method> matches = List.of(controllerClass.getDeclaredMethods()).stream()
                .filter(method -> method.getName().equals(handlerMethodName))
                .sorted(Comparator.comparing(Method::toString))
                .toList();
        assertEquals(1, matches.size(), () -> "Expected exactly one method named " + handlerMethodName + " on " + controllerClass.getName());
        return matches.getFirst();
    }

    private static String extractClassPath(Class<?> controllerClass) {
        RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);
        if (requestMapping == null || requestMapping.value().length == 0) {
            return "";
        }
        return requestMapping.value()[0];
    }

    private static Mapping extractMethodMapping(Method method) {
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation instanceof GetMapping getMapping) {
                return new Mapping("GET", firstPath(getMapping.value()));
            }
            if (annotation instanceof PostMapping postMapping) {
                return new Mapping("POST", firstPath(postMapping.value()));
            }
            if (annotation instanceof PutMapping putMapping) {
                return new Mapping("PUT", firstPath(putMapping.value()));
            }
            if (annotation instanceof PatchMapping patchMapping) {
                return new Mapping("PATCH", firstPath(patchMapping.value()));
            }
            if (annotation instanceof DeleteMapping deleteMapping) {
                return new Mapping("DELETE", firstPath(deleteMapping.value()));
            }
        }
        throw new IllegalStateException("No supported request mapping annotation found for " + method);
    }

    private static String firstPath(String[] values) {
        return values.length == 0 ? "" : values[0];
    }

    private static String normalizePath(String path) {
        String normalized = path.replaceAll("//+", "/");
        return normalized.endsWith("/") && normalized.length() > 1
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
    }

    private static Path findRepoRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("AGENTS.md")) && Files.exists(current.resolve("docs"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate repository root");
    }

    private record Mapping(String httpMethod, String path) {
    }
}
