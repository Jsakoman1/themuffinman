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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.text.Normalizer;
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
        validateAllControllerMappingsAreDocumented(sourceFiles, endpoints);
        validatePolicies(yaml.path("policies"), sourceFiles.keySet());
        validateIntents(yaml.path("intents"), sourceFiles.keySet(), endpoints.keySet());
        validateAllEndpointsHaveIntentCoverage(yaml.path("intents"), endpoints.keySet());
        validateAutomationReadModels(yaml.path("automation_read_models"), repoRoot, sourceFiles);
        validateIntentSafetyCatalog(yaml.path("intent_safety_catalog"), yaml.path("intents"), endpoints);
        validateDocumentationCoverage(yaml.path("documentation_coverage"), repoRoot, sourceFiles);
        validateFrontendContractGeneration(yaml.path("frontend_contract_generation"), repoRoot);
        validateFrontendContracts(yaml.path("frontend_contracts"), repoRoot);
        validateFrontendSafetyRegressions(yaml.path("frontend_safety_regressions"), repoRoot);
        validateFrontendFeatureExpectations(yaml.path("frontend_feature_expectations"), yaml.path("frontend_contracts"), repoRoot);
        validateDeadPathTracker(yaml, repoRoot, sourceFiles);
        validateCapabilityRegistry(yaml.path("capability_registry"), yaml.path("intents"), yaml.path("intent_safety_catalog"));
        validateIntentLineage(yaml.path("intent_lineage"), yaml.path("intents"), yaml.path("automation_read_models"), endpoints.keySet(), yaml.path("dead_path_tracker"));
        validatePromptDriftDetection(yaml.path("prompt_drift_detection"), repoRoot);
        validateBackendContractSnapshots(yaml.path("backend_contract_snapshots"), repoRoot, sourceFiles.keySet());
        validateServiceWorkflowInventory(yaml.path("service_workflow_inventory"), yaml.path("intents"), repoRoot, sourceFiles.keySet());
        validatePermissionMatrix(yaml.path("permission_matrix"), yaml.path("intents"), endpoints.keySet(), yaml.path("intent_safety_catalog"));
        validateStateTransitionAudit(yaml.path("state_transition_audit"), yaml.path("intents"), repoRoot, yaml.path("permission_matrix"));
        validateRequestValidationGate(yaml.path("request_validation_gate"), repoRoot, sourceFiles);
        validateCompositeWorkflowSafety(yaml.path("intents"), yaml.path("intent_safety_catalog"));
        validateDocumentationSync(yaml.path("documentation_sync"), repoRoot, sourceFiles.keySet());
        validateFeatureCompletionManifests(repoRoot);
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

    private static void validateAllControllerMappingsAreDocumented(Map<String, String> sourceFiles, Map<String, JsonNode> endpoints) throws Exception {
        Set<String> documentedMappings = endpoints.values().stream()
                .map(endpointNode -> endpointNode.path("controller_class").asText()
                        + "#" + endpointNode.path("handler_method").asText()
                        + "#" + endpointNode.path("method").asText()
                        + "#" + endpointNode.path("path").asText())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (String sourcePath : sourceFiles.values()) {
            if (!sourcePath.contains("/controller/") || !sourcePath.endsWith(".java")) {
                continue;
            }

            Class<?> controllerClass = Class.forName(toJavaClassName(sourcePath));
            String classPath = extractClassPath(controllerClass);
            for (Method method : controllerClass.getDeclaredMethods()) {
                Mapping mapping = tryExtractMethodMapping(method);
                if (mapping == null) {
                    continue;
                }

                String mappingKey = controllerClass.getName()
                        + "#" + method.getName()
                        + "#" + mapping.httpMethod()
                        + "#" + normalizePath(classPath + mapping.path());
                assertTrue(documentedMappings.contains(mappingKey), () -> "Undocumented controller mapping: " + mappingKey);
            }
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

    private static void validateAllEndpointsHaveIntentCoverage(JsonNode intentsNode, Set<String> endpointIds) {
        Set<String> referencedEndpointIds = new LinkedHashSet<>();
        for (JsonNode intentNode : iterable(intentsNode)) {
            for (JsonNode workflowStep : intentNode.path("workflow")) {
                if (workflowStep.hasNonNull("endpoint_id")) {
                    referencedEndpointIds.add(workflowStep.path("endpoint_id").asText());
                }
            }
        }

        Set<String> uncoveredEndpoints = endpointIds.stream()
                .filter(endpointId -> !referencedEndpointIds.contains(endpointId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        assertTrue(uncoveredEndpoints.isEmpty(), () -> "Endpoints without intent coverage: " + uncoveredEndpoints);
    }

    private static void validatePolicies(JsonNode policiesNode, Set<String> sourceIds) {
        for (JsonNode edgeCaseNode : policiesNode.path("edge_cases")) {
            for (JsonNode sourceRefNode : edgeCaseNode.path("source_refs")) {
                String sourceRef = sourceRefNode.asText();
                assertTrue(sourceIds.contains(sourceRef), () -> "Unknown policy source_ref: " + sourceRef);
            }
        }
    }

    private static void validateAutomationReadModels(JsonNode automationReadModelsNode, Path repoRoot, Map<String, String> sourceFiles) throws Exception {
        Set<String> validatedModelIds = new LinkedHashSet<>();
        for (JsonNode modelNode : iterable(automationReadModelsNode)) {
            String modelId = modelNode.path("id").asText();
            assertTrue(validatedModelIds.add(modelId), () -> "Duplicate automation_read_models id: " + modelId);

            Class<?> dtoClass = Class.forName(modelNode.path("java_class").asText());
            Set<String> fieldNames = collectFieldNames(dtoClass);
            for (JsonNode requiredFieldNode : modelNode.path("required_fields")) {
                String requiredField = requiredFieldNode.asText();
                assertTrue(fieldNames.contains(requiredField),
                        () -> "Missing required automation read-model field " + requiredField + " on " + dtoClass.getName());
            }

            for (JsonNode sourceRefNode : modelNode.path("producer_source_refs")) {
                String sourceRef = sourceRefNode.asText();
                assertTrue(sourceFiles.containsKey(sourceRef), () -> "Unknown automation_read_models producer_source_ref: " + sourceRef);
                String producerContent = Files.readString(repoRoot.resolve(sourceFiles.get(sourceRef)));
                for (JsonNode requiredFieldNode : modelNode.path("required_fields")) {
                    String requiredField = requiredFieldNode.asText();
                    assertTrue(producerContent.contains(requiredField),
                            () -> "Producer source does not reference required automation read-model field " + requiredField + " for " + modelId + " in " + sourceRef);
                }
            }

            for (JsonNode verificationTestNode : modelNode.path("verification_tests")) {
                String verificationTest = verificationTestNode.asText();
                assertTrue(Files.exists(repoRoot.resolve(verificationTest)),
                        () -> "Missing automation_read_models verification test: " + verificationTest);
            }
        }
    }

    private static void validateIntentSafetyCatalog(JsonNode intentSafetyCatalogNode, JsonNode intentsNode, Map<String, JsonNode> endpoints) {
        Map<String, JsonNode> intentsById = readById(intentsNode);
        Set<String> mutatingIntentIds = intentsById.entrySet().stream()
                .filter(entry -> entry.getValue().path("mutating").asBoolean())
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> readOnlyIntentIds = intentsById.entrySet().stream()
                .filter(entry -> !entry.getValue().path("mutating").asBoolean())
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        JsonNode riskGroupsNode = intentSafetyCatalogNode.path("risk_groups");
        Set<String> catalogReadOnlyIds = readStringSet(riskGroupsNode.path("read_only"));
        Set<String> safeWriteIds = readStringSet(riskGroupsNode.path("safe_write"));
        Set<String> destructiveWriteIds = readStringSet(riskGroupsNode.path("destructive_write"));
        Set<String> multiActorWriteIds = readStringSet(riskGroupsNode.path("multi_actor_write"));
        Set<String> adminOnlyWriteIds = readStringSet(riskGroupsNode.path("admin_only_write"));

        Set<String> writeCatalogIds = new LinkedHashSet<>();
        writeCatalogIds.addAll(safeWriteIds);
        writeCatalogIds.addAll(destructiveWriteIds);
        writeCatalogIds.addAll(multiActorWriteIds);
        writeCatalogIds.addAll(adminOnlyWriteIds);

        assertEquals(readOnlyIntentIds, catalogReadOnlyIds, "intent_safety_catalog.read_only must match all non-mutating intents exactly");
        assertTrue(catalogReadOnlyIds.stream().noneMatch(mutatingIntentIds::contains), "Mutating intents must not be classified as read_only");
        assertTrue(mutatingIntentIds.stream().allMatch(writeCatalogIds::contains),
                () -> "Every mutating intent must appear in at least one write risk group. Missing: "
                        + mutatingIntentIds.stream().filter(id -> !writeCatalogIds.contains(id)).toList());

        Set<String> destructiveConfirmationIds = readStringSet(intentSafetyCatalogNode.path("destructive_confirmation"));
        Set<String> multiActorAuthorityIds = readStringSet(intentSafetyCatalogNode.path("multi_actor_authority"));
        Set<String> currentLocationCapabilityIds = readStringSet(intentSafetyCatalogNode.path("current_location_capability"));

        assertTrue(destructiveWriteIds.containsAll(destructiveConfirmationIds),
                "All destructive_confirmation intents must also be classified as destructive_write");
        assertTrue(multiActorWriteIds.containsAll(multiActorAuthorityIds),
                "All multi_actor_authority intents must also be classified as multi_actor_write");

        for (String intentId : destructiveConfirmationIds) {
            JsonNode intentNode = intentsById.get(intentId);
            assertNotNull(intentNode, () -> "Unknown destructive_confirmation intent: " + intentId);
            List<String> requiredInputs = readStringList(intentNode.path("required_inputs"));
            assertTrue(requiredInputs.contains("destructive_confirmation"),
                    () -> "Destructive intent must require destructive_confirmation: " + intentId);
        }

        for (String intentId : currentLocationCapabilityIds) {
            JsonNode intentNode = intentsById.get(intentId);
            assertNotNull(intentNode, () -> "Unknown current_location_capability intent: " + intentId);
            List<String> requiredInputs = readStringList(intentNode.path("required_inputs"));
            boolean hasCoordinates = requiredInputs.contains("latitude") && requiredInputs.contains("longitude");
            boolean referencesResolutionIntent = iterable(intentNode.path("workflow")).stream()
                    .anyMatch(step -> "resolve_current_location_input".equals(step.path("intent_ref").asText()));
            assertTrue(hasCoordinates || referencesResolutionIntent,
                    () -> "Current-location capability intent must require coordinates or reference resolve_current_location_input: " + intentId);
        }

        Set<String> validatedResolutionIntents = new LinkedHashSet<>();
        for (JsonNode resolutionNode : iterable(intentSafetyCatalogNode.path("exact_target_resolution"))) {
            String intentId = resolutionNode.path("intent_id").asText();
            assertTrue(validatedResolutionIntents.add(intentId), () -> "Duplicate exact_target_resolution intent: " + intentId);
            JsonNode intentNode = intentsById.get(intentId);
            assertNotNull(intentNode, () -> "Unknown exact_target_resolution intent: " + intentId);
            assertTrue(intentNode.path("mutating").asBoolean(), () -> "exact_target_resolution intent must be mutating: " + intentId);
            assertEquals("fail_closed", resolutionNode.path("ambiguity_policy").asText(),
                    () -> "exact_target_resolution intents must use fail_closed ambiguity policy: " + intentId);
            for (JsonNode resolutionWorkflowNode : resolutionNode.path("resolution_workflows")) {
                String resolutionWorkflowId = resolutionWorkflowNode.asText();
                JsonNode resolutionIntentNode = intentsById.get(resolutionWorkflowId);
                assertNotNull(resolutionIntentNode,
                        () -> "Unknown exact_target_resolution workflow ref " + resolutionWorkflowId + " for intent " + intentId);
                assertFalse(resolutionIntentNode.path("mutating").asBoolean(),
                        () -> "Resolution workflow ref must point to a non-mutating intent: " + resolutionWorkflowId);
            }
        }

        Set<String> mutatingEndpointIdsThatNeedMultiActor = Set.of(
                "accept_circle_request",
                "approve_application",
                "decline_application",
                "confirm_quest_term_change",
                "reject_quest_term_change"
        );
        Set<String> mutatingEndpointIdsThatNeedDestructiveConfirmation = Set.of(
                "delete_user_as_admin",
                "delete_circle",
                "delete_circle_as_admin",
                "delete_admin_application",
                "delete_quest"
        );

        for (Map.Entry<String, JsonNode> intentEntry : intentsById.entrySet()) {
            if (!intentEntry.getValue().path("mutating").asBoolean()) {
                continue;
            }

            String intentId = intentEntry.getKey();
            List<String> mutatingEndpointIds = iterable(intentEntry.getValue().path("workflow")).stream()
                    .map(step -> step.path("endpoint_id").asText(null))
                    .filter(endpointId -> endpointId != null && !endpointId.isBlank())
                    .filter(endpointId -> !"GET".equals(endpoints.get(endpointId).path("method").asText()))
                    .toList();
            List<String> mutatingIntentRefs = iterable(intentEntry.getValue().path("workflow")).stream()
                    .map(step -> step.path("intent_ref").asText(null))
                    .filter(intentRef -> intentRef != null && !intentRef.isBlank())
                    .filter(mutatingIntentIds::contains)
                    .toList();
            assertTrue(!mutatingEndpointIds.isEmpty() || !mutatingIntentRefs.isEmpty(),
                    () -> "Mutating intent must reference at least one mutating endpoint or delegated mutating intent: " + intentId);

            for (String endpointId : mutatingEndpointIds) {
                JsonNode endpointNode = endpoints.get(endpointId);
                assertNotNull(endpointNode, () -> "Unknown endpoint referenced by mutating intent " + intentId + ": " + endpointId);
                if (mutatingEndpointIdsThatNeedDestructiveConfirmation.contains(endpointId)) {
                    assertTrue(destructiveConfirmationIds.contains(intentId),
                            () -> "Mutating DELETE endpoint intent must require destructive confirmation: " + intentId + " -> " + endpointId);
                }
                if (mutatingEndpointIdsThatNeedMultiActor.contains(endpointId)) {
                    assertTrue(multiActorAuthorityIds.contains(intentId),
                            () -> "Multi-actor endpoint intent must be listed in multi_actor_authority: " + intentId + " -> " + endpointId);
                }
            }
        }
    }

    private static void validateDocumentationCoverage(JsonNode documentationCoverageNode, Path repoRoot, Map<String, String> sourceFiles) throws Exception {
        Set<String> documentedPaths = new LinkedHashSet<>(sourceFiles.values());

        Set<String> validatedScanIds = new LinkedHashSet<>();
        for (JsonNode scanNode : iterable(documentationCoverageNode.path("auto_scans"))) {
            String scanId = scanNode.path("id").asText();
            assertTrue(validatedScanIds.add(scanId), () -> "Duplicate documentation_coverage auto scan id: " + scanId);

            Path rootPath = repoRoot.resolve(scanNode.path("root_path").asText());
            assertTrue(Files.exists(rootPath), () -> "Missing documentation_coverage scan root: " + rootPath);

            String pathContains = scanNode.path("path_contains").asText();
            String fileSuffix = scanNode.path("file_suffix").asText();
            try (var paths = Files.walk(rootPath)) {
                List<String> matchingFiles = paths
                        .filter(Files::isRegularFile)
                        .map(repoRoot::relativize)
                        .map(Path::toString)
                        .map(path -> path.replace('\\', '/'))
                        .filter(path -> path.contains(pathContains))
                        .filter(path -> path.endsWith(fileSuffix))
                        .sorted()
                        .toList();
                assertFalse(matchingFiles.isEmpty(), () -> "documentation_coverage scan matched no files: " + scanId);
                for (String matchingFile : matchingFiles) {
                    assertTrue(documentedPaths.contains(matchingFile),
                            () -> "documentation_coverage scan found undocumented source file: " + matchingFile + " in scan " + scanId);
                }
            }
        }

        Set<String> validatedGroupIds = new LinkedHashSet<>();
        for (JsonNode groupNode : iterable(documentationCoverageNode.path("required_source_ref_groups"))) {
            String groupId = groupNode.path("id").asText();
            assertTrue(validatedGroupIds.add(groupId), () -> "Duplicate documentation_coverage group id: " + groupId);
            for (JsonNode sourceRefNode : groupNode.path("source_refs")) {
                String sourceRef = sourceRefNode.asText();
                assertTrue(sourceFiles.containsKey(sourceRef),
                        () -> "Unknown documentation_coverage source_ref in group " + groupId + ": " + sourceRef);
            }
        }
    }

    private static void validateFrontendContractGeneration(JsonNode frontendContractGenerationNode, Path repoRoot) throws Exception {
        Path scriptPath = repoRoot.resolve(frontendContractGenerationNode.path("script").asText());
        Path generatedContractPath = repoRoot.resolve(frontendContractGenerationNode.path("generated_contract_file").asText());
        assertTrue(Files.exists(scriptPath), () -> "Missing frontend contract generation script: " + scriptPath);
        assertTrue(Files.exists(generatedContractPath), () -> "Missing generated frontend contract file: " + generatedContractPath);

        String generatedContent = Files.readString(generatedContractPath);
        for (String alias : readStringList(frontendContractGenerationNode.path("required_aliases"))) {
            assertTrue(generatedContent.contains("export type " + alias + " = "),
                    () -> "Generated frontend contract file is missing required alias: " + alias);
        }
    }

    private static void validateFrontendContracts(JsonNode frontendContractsNode, Path repoRoot) throws Exception {
        Set<String> validatedContractIds = new LinkedHashSet<>();
        for (JsonNode contractNode : iterable(frontendContractsNode)) {
            String contractId = contractNode.path("id").asText();
            assertTrue(validatedContractIds.add(contractId), () -> "Duplicate frontend_contracts id: " + contractId);

            Class<?> backendClass = Class.forName(contractNode.path("backend_java_class").asText());
            Set<String> backendFieldNames = collectFieldNames(backendClass);
            String frontendContractFile = contractNode.path("frontend_contract_file").asText();
            String frontendGateFile = contractNode.path("frontend_gate_file").asText();
            Path contractFilePath = repoRoot.resolve(frontendContractFile);
            Path gateFilePath = repoRoot.resolve(frontendGateFile);
            assertTrue(Files.exists(contractFilePath), () -> "Missing frontend contract file: " + frontendContractFile);
            assertTrue(Files.exists(gateFilePath), () -> "Missing frontend gate file: " + frontendGateFile);

            String contractFileContent = Files.readString(contractFilePath);
            String gateFileContent = Files.readString(gateFilePath);
            assertTrue(contractFileContent.contains(contractNode.path("frontend_type").asText().replace("[\"clarificationContract\"]", "").replace("[\"executionReadiness\"]", "")) || gateFileContent.contains(contractNode.path("frontend_type").asText()),
                    () -> "Frontend type reference missing for contract " + contractId);
            for (JsonNode requiredFieldNode : contractNode.path("required_fields")) {
                String requiredField = requiredFieldNode.asText();
                assertTrue(backendFieldNames.contains(requiredField),
                        () -> "Backend class does not contain required frontend contract field " + requiredField + " for " + contractId);
                assertTrue(contractFileContent.contains(requiredField) || gateFileContent.contains(requiredField),
                        () -> "Frontend contract files do not reference required field " + requiredField + " for " + contractId);
            }
        }
    }

    private static void validateFrontendSafetyRegressions(JsonNode frontendSafetyRegressionsNode, Path repoRoot) throws Exception {
        Path validationScriptPath = repoRoot.resolve(frontendSafetyRegressionsNode.path("validation_script").asText());
        Path fixtureFilePath = repoRoot.resolve(frontendSafetyRegressionsNode.path("fixture_file").asText());
        assertTrue(Files.exists(validationScriptPath), () -> "Missing frontend safety regression validation script: " + validationScriptPath);
        assertTrue(Files.exists(fixtureFilePath), () -> "Missing frontend safety regression fixture file: " + fixtureFilePath);

        JsonNode fixtureNode = JSON_MAPPER.readTree(Files.readString(fixtureFilePath));
        Set<String> actualCaseIds = iterable(fixtureNode.path("cases")).stream()
                .map(node -> node.path("id").asText())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> requiredCaseIds = readStringSet(frontendSafetyRegressionsNode.path("required_case_ids"));
        assertEquals(requiredCaseIds, actualCaseIds,
                "frontend_safety_regressions fixture cases must match required_case_ids exactly");
    }

    private static void validateFrontendFeatureExpectations(JsonNode frontendFeatureExpectationsNode, JsonNode frontendContractsNode, Path repoRoot) {
        Set<String> knownContractIds = readById(frontendContractsNode).keySet();
        Set<String> expectationIds = new LinkedHashSet<>();
        for (JsonNode expectationNode : iterable(frontendFeatureExpectationsNode)) {
            String expectationId = expectationNode.path("id").asText();
            assertTrue(expectationIds.add(expectationId), () -> "Duplicate frontend_feature_expectations id: " + expectationId);

            for (String contractId : readStringList(expectationNode.path("when_contract_ids"))) {
                assertTrue(knownContractIds.contains(contractId), () -> "Unknown frontend_feature_expectations contract id: " + contractId);
            }

            for (String requiredFile : readStringList(expectationNode.path("required_files"))) {
                assertTrue(Files.exists(repoRoot.resolve(requiredFile)),
                        () -> "Missing frontend_feature_expectations required file: " + requiredFile);
            }
        }
    }

    private static void validateDeadPathTracker(JsonNode yaml, Path repoRoot, Map<String, String> sourceFiles) throws Exception {
        JsonNode deadPathTrackerNode = yaml.path("dead_path_tracker");
        Set<String> allowedUnusedSourceRefs = readStringSet(deadPathTrackerNode.path("allow_source_refs_without_direct_workflow_usage"));
        Set<String> directlyUsedSourceRefs = collectDirectMachineSourceRefs(yaml, sourceFiles);

        for (String sourceRef : sourceFiles.keySet()) {
            if (allowedUnusedSourceRefs.contains(sourceRef)) {
                continue;
            }
            assertTrue(directlyUsedSourceRefs.contains(sourceRef),
                    () -> "Source ref appears stale or unused in direct machine rules: " + sourceRef);
        }

        Set<String> criticalPromptMatrixWorkflows = readStringSet(deadPathTrackerNode.path("critical_prompt_matrix_workflows"));
        Set<String> coveredWorkflows = readCoveredPromptMatrixWorkflows(repoRoot);
        for (String workflowId : criticalPromptMatrixWorkflows) {
            assertTrue(coveredWorkflows.contains(workflowId),
                    () -> "Critical prompt-matrix workflow is not covered by the multilingual golden prompt matrix: " + workflowId);
        }

        Set<String> knownIntentIds = readById(yaml.path("intents")).keySet();
        for (String coveredWorkflow : coveredWorkflows) {
            assertTrue(knownIntentIds.contains(coveredWorkflow),
                    () -> "Golden prompt matrix references unknown workflow intent: " + coveredWorkflow);
        }
    }

    private static void validateFeatureCompletionManifests(Path repoRoot) throws Exception {
        Path manifestsDir = repoRoot.resolve(".agents/feature-manifests");
        Path schemaPath = repoRoot.resolve("docs/feature-completion-manifest.schema.json");
        Path templatePath = repoRoot.resolve(".agents/templates/feature-completion-manifest.template.yaml");
        Path planTemplatePath = repoRoot.resolve(".agents/templates/feature-implementation-plan.template.md");
        Path bootstrapScriptPath = repoRoot.resolve("scripts/bootstrap-feature-work.sh");
        Path closeoutAuditScriptPath = repoRoot.resolve("scripts/feature-closeout-audit.sh");

        assertTrue(Files.exists(schemaPath), "feature-completion-manifest.schema.json must exist");
        assertTrue(Files.exists(templatePath), "feature completion manifest template must exist");
        assertTrue(Files.exists(planTemplatePath), "feature implementation plan template must exist");
        assertTrue(Files.exists(bootstrapScriptPath), "bootstrap-feature-work.sh must exist");
        assertTrue(Files.exists(closeoutAuditScriptPath), "feature-closeout-audit.sh must exist");
        assertTrue(Files.isDirectory(manifestsDir), ".agents/feature-manifests must exist");

        JsonNode schemaNode = JSON_MAPPER.readTree(Files.readString(schemaPath));
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(schemaNode);

        List<Path> manifestPaths;
        try (var paths = Files.list(manifestsDir)) {
            manifestPaths = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith("-manifest.yaml"))
                    .sorted()
                    .toList();
        }
        assertFalse(manifestPaths.isEmpty(), "At least one feature completion manifest must exist");

        for (Path manifestPath : manifestPaths) {
            JsonNode manifestNode = YAML_MAPPER.readTree(Files.readString(manifestPath));
            Set<ValidationMessage> validationMessages = schema.validate(manifestNode);
            assertTrue(validationMessages.isEmpty(), () -> "Feature manifest schema validation failed for " + manifestPath + ": " + validationMessages);
            Path referencedPlan = repoRoot.resolve(manifestNode.path("planFile").asText());
            assertTrue(Files.exists(referencedPlan), () -> "Feature manifest references missing plan file: " + referencedPlan);

            String riskTier = manifestNode.path("riskTier").asText();
            List<String> auditCommands = readStringList(manifestNode.path("artifacts").path("auditCommands"));
            if ("high".equals(riskTier) || "executor-critical".equals(riskTier)) {
                assertTrue(auditCommands.contains("make audit-agent-safety"),
                        () -> "High-risk or executor-critical manifest must require make audit-agent-safety: " + manifestPath);
            }
            if (manifestNode.path("checklist").path("frontendValidationPassed").asBoolean()) {
                assertTrue(auditCommands.contains("npm run type-check"),
                        () -> "Manifest with frontend validation must include npm run type-check audit command: " + manifestPath);
            }
            if ("complete".equals(manifestNode.path("status").asText())) {
                assertTrue(manifestNode.path("checklist").path("codeImplemented").asBoolean(),
                        () -> "Completed manifest must mark codeImplemented=true: " + manifestPath);
                assertTrue(manifestNode.path("checklist").path("backendTestsPassed").asBoolean(),
                        () -> "Completed manifest must mark backendTestsPassed=true: " + manifestPath);
            }
        }
    }

    private static void validateCapabilityRegistry(JsonNode capabilityRegistryNode, JsonNode intentsNode, JsonNode intentSafetyCatalogNode) {
        Set<String> knownIntentIds = readById(intentsNode).keySet();
        Set<String> destructiveIntents = readStringSet(intentSafetyCatalogNode.path("destructive_confirmation"));
        Set<String> multiActorIntents = readStringSet(intentSafetyCatalogNode.path("multi_actor_authority"));
        Set<String> currentLocationIntents = readStringSet(intentSafetyCatalogNode.path("current_location_capability"));

        Set<String> capabilityIds = new LinkedHashSet<>();
        for (JsonNode capabilityNode : iterable(capabilityRegistryNode)) {
            String capabilityId = capabilityNode.path("id").asText();
            assertTrue(capabilityIds.add(capabilityId), () -> "Duplicate capability_registry id: " + capabilityId);
            for (JsonNode intentNode : capabilityNode.path("required_for_intents")) {
                String intentId = intentNode.asText();
                assertTrue(knownIntentIds.contains(intentId), () -> "Unknown capability_registry intent: " + intentId);
            }
        }

        assertTrue(capabilityIds.contains("admin_authority"), "capability_registry must include admin_authority");
        assertTrue(capabilityIds.contains("external_translation_provider"), "capability_registry must include external_translation_provider");
        assertTrue(capabilityIds.contains("current_location"), "capability_registry must include current_location");
        assertTrue(capabilityIds.contains("second_actor_context"), "capability_registry must include second_actor_context");
        assertTrue(capabilityIds.contains("destructive_confirmation"), "capability_registry must include destructive_confirmation");

        assertCapabilityContains(capabilityRegistryNode, "destructive_confirmation", destructiveIntents);
        assertCapabilityContains(capabilityRegistryNode, "second_actor_context", multiActorIntents);
        assertCapabilityContains(capabilityRegistryNode, "current_location", currentLocationIntents);
    }

    private static void assertCapabilityContains(JsonNode capabilityRegistryNode, String capabilityId, Set<String> expectedIntents) {
        JsonNode capabilityNode = iterable(capabilityRegistryNode).stream()
                .filter(node -> capabilityId.equals(node.path("id").asText()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing capability_registry node for " + capabilityId));
        Set<String> documentedIntents = readStringSet(capabilityNode.path("required_for_intents"));
        assertTrue(documentedIntents.containsAll(expectedIntents),
                () -> "capability_registry." + capabilityId + " must include expected intents. Missing: "
                        + expectedIntents.stream().filter(intent -> !documentedIntents.contains(intent)).toList());
    }

    private static void validateIntentLineage(JsonNode intentLineageNode, JsonNode intentsNode, JsonNode automationReadModelsNode, Set<String> endpointIds, JsonNode deadPathTrackerNode) {
        Set<String> knownIntentIds = readById(intentsNode).keySet();
        Set<String> readModelIds = readById(automationReadModelsNode).keySet();
        Set<String> requiredCriticalWorkflows = readStringSet(deadPathTrackerNode.path("critical_prompt_matrix_workflows"));
        Set<String> documentedIntentIds = new LinkedHashSet<>();

        for (JsonNode lineageNode : iterable(intentLineageNode)) {
            String intentId = lineageNode.path("intent_id").asText();
            assertTrue(documentedIntentIds.add(intentId), () -> "Duplicate intent_lineage intent_id: " + intentId);
            assertTrue(knownIntentIds.contains(intentId), () -> "Unknown intent_lineage intent: " + intentId);
            for (JsonNode resolutionWorkflowNode : lineageNode.path("resolution_workflows")) {
                String resolutionWorkflow = resolutionWorkflowNode.asText();
                assertTrue(knownIntentIds.contains(resolutionWorkflow), () -> "Unknown intent_lineage resolution workflow: " + resolutionWorkflow);
                assertFalse(readById(intentsNode).get(resolutionWorkflow).path("mutating").asBoolean(),
                        () -> "intent_lineage resolution workflow must point to non-mutating intent: " + resolutionWorkflow);
            }
            for (JsonNode endpointNode : lineageNode.path("target_endpoints")) {
                assertTrue(endpointIds.contains(endpointNode.asText()), () -> "Unknown intent_lineage target endpoint: " + endpointNode.asText());
            }
            for (JsonNode readModelNode : lineageNode.path("expected_read_models")) {
                assertTrue(readModelIds.contains(readModelNode.asText()), () -> "Unknown intent_lineage expected_read_model: " + readModelNode.asText());
            }
        }

        assertTrue(documentedIntentIds.containsAll(requiredCriticalWorkflows),
                () -> "intent_lineage must cover all critical prompt-matrix workflows. Missing: "
                        + requiredCriticalWorkflows.stream().filter(intent -> !documentedIntentIds.contains(intent)).toList());
    }

    private static void validatePromptDriftDetection(JsonNode promptDriftDetectionNode, Path repoRoot) throws Exception {
        Path matrixPath = repoRoot.resolve(promptDriftDetectionNode.path("matrix_resource").asText());
        assertTrue(Files.exists(matrixPath), () -> "Missing prompt drift matrix resource: " + matrixPath);
        JsonNode matrixNode = YAML_MAPPER.readTree(Files.readString(matrixPath));
        Set<String> requiredCaseFields = readStringSet(promptDriftDetectionNode.path("required_case_fields"));
        assertFalse(iterable(matrixNode.path("cases")).isEmpty(), "Prompt drift matrix must contain cases");
        for (JsonNode caseNode : iterable(matrixNode.path("cases"))) {
            for (String requiredField : requiredCaseFields) {
                assertTrue(caseNode.has(requiredField), () -> "Prompt drift matrix case is missing field " + requiredField + ": " + caseNode);
            }
            assertTrue(caseNode.path("driftFingerprint").isTextual() && !caseNode.path("driftFingerprint").asText().isBlank(),
                    "Prompt drift matrix cases must define a non-blank driftFingerprint");
        }
    }

    private static void validateBackendContractSnapshots(JsonNode backendContractSnapshotsNode, Path repoRoot, Set<String> sourceIds) throws Exception {
        Set<String> snapshotIds = new LinkedHashSet<>();
        for (JsonNode snapshotNode : iterable(backendContractSnapshotsNode)) {
            String snapshotId = snapshotNode.path("id").asText();
            assertTrue(snapshotIds.add(snapshotId), () -> "Duplicate backend_contract_snapshots id: " + snapshotId);

            Class<?> dtoClass = Class.forName(snapshotNode.path("java_class").asText());
            List<String> actualDeclaredFields = collectDeclaredFieldNames(dtoClass);
            List<String> documentedDeclaredFields = readStringList(snapshotNode.path("declared_fields"));
            assertEquals(documentedDeclaredFields, actualDeclaredFields,
                    () -> "backend_contract_snapshots declared_fields out of sync for " + dtoClass.getName());

            Set<String> actualFieldSet = new LinkedHashSet<>(actualDeclaredFields);
            for (String semanticField : readStringList(snapshotNode.path("semantic_required_fields"))) {
                assertTrue(actualFieldSet.contains(semanticField),
                        () -> "Missing semantic snapshot field " + semanticField + " on " + dtoClass.getName());
            }

            for (JsonNode sourceRefNode : snapshotNode.path("source_refs")) {
                String sourceRef = sourceRefNode.asText();
                assertTrue(sourceIds.contains(sourceRef), () -> "Unknown backend_contract_snapshots source_ref: " + sourceRef);
            }

            for (JsonNode verificationTestNode : snapshotNode.path("verification_tests")) {
                String verificationTest = verificationTestNode.asText();
                assertTrue(Files.exists(repoRoot.resolve(verificationTest)),
                        () -> "Missing backend_contract_snapshots verification test: " + verificationTest);
            }
        }
    }

    private static void validateServiceWorkflowInventory(JsonNode serviceWorkflowInventoryNode, JsonNode intentsNode, Path repoRoot, Set<String> sourceIds) throws Exception {
        Map<String, JsonNode> intentsById = readById(intentsNode);
        Set<String> inventoryIds = new LinkedHashSet<>();
        Set<String> coveredIntentIds = new LinkedHashSet<>();

        for (JsonNode inventoryNode : iterable(serviceWorkflowInventoryNode)) {
            String inventoryId = inventoryNode.path("id").asText();
            assertTrue(inventoryIds.add(inventoryId), () -> "Duplicate service_workflow_inventory id: " + inventoryId);

            Class<?> serviceClass = Class.forName(inventoryNode.path("service_class").asText());
            for (String methodName : readStringList(inventoryNode.path("service_methods"))) {
                findSingleMethod(serviceClass, methodName);
            }

            List<String> coveredIntents = readStringList(inventoryNode.path("covered_intents"));
            assertFalse(coveredIntents.isEmpty(), () -> "service_workflow_inventory.covered_intents must not be empty for " + inventoryId);
            for (String intentId : coveredIntents) {
                JsonNode intentNode = intentsById.get(intentId);
                assertNotNull(intentNode, () -> "Unknown service_workflow_inventory covered intent: " + intentId);
                assertTrue(intentNode.path("mutating").asBoolean(), () -> "service_workflow_inventory may only cover mutating intents: " + intentId);
                coveredIntentIds.add(intentId);
            }

            if (inventoryNode.path("automation_relevant").asBoolean()) {
                assertFalse(coveredIntents.isEmpty(), () -> "automation-relevant inventory item must cover intents: " + inventoryId);
            }

            for (JsonNode sourceRefNode : inventoryNode.path("source_refs")) {
                String sourceRef = sourceRefNode.asText();
                assertTrue(sourceIds.contains(sourceRef), () -> "Unknown service_workflow_inventory source_ref: " + sourceRef);
            }

            for (JsonNode docPathNode : inventoryNode.path("doc_paths")) {
                String docPath = docPathNode.asText();
                assertTrue(Files.exists(repoRoot.resolve(docPath)), () -> "Missing service_workflow_inventory doc path: " + docPath);
            }

            for (JsonNode verificationTestNode : inventoryNode.path("verification_tests")) {
                String verificationTest = verificationTestNode.asText();
                assertTrue(Files.exists(repoRoot.resolve(verificationTest)),
                        () -> "Missing service_workflow_inventory verification test: " + verificationTest);
            }
        }

        Set<String> expectedCoveredIntents = intentsById.entrySet().stream()
                .filter(entry -> entry.getValue().path("mutating").asBoolean())
                .map(Map.Entry::getKey)
                .filter(intentId -> !Set.of(
                        "authenticate_user",
                        "create_user_with_quests",
                        "create_circle_only_quest_for_selected_people",
                        "prepare_circle_only_quest_flow_to_start",
                        "voice_prepare_scheduled_circle_only_quest_for_selected_people",
                        "create_sandbox_user_with_circle_and_quest_flow"
                ).contains(intentId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        assertEquals(expectedCoveredIntents, coveredIntentIds,
                "service_workflow_inventory must cover every non-composite mutating intent exactly");
    }

    private static void validatePermissionMatrix(JsonNode permissionMatrixNode, JsonNode intentsNode, Set<String> endpointIds, JsonNode intentSafetyCatalogNode) {
        Set<String> actorTypes = readStringSet(permissionMatrixNode.path("actor_types"));
        Map<String, JsonNode> intentsById = readById(intentsNode);
        Set<String> destructiveIntents = readStringSet(intentSafetyCatalogNode.path("destructive_confirmation"));
        Set<String> multiActorIntents = readStringSet(intentSafetyCatalogNode.path("multi_actor_authority"));
        Set<String> adminOnlyIntents = readStringSet(intentSafetyCatalogNode.path("risk_groups").path("admin_only_write"));

        Set<String> ruleIds = new LinkedHashSet<>();
        Set<String> coveredHighRiskIntents = new LinkedHashSet<>();
        for (JsonNode ruleNode : iterable(permissionMatrixNode.path("rules"))) {
            String ruleId = ruleNode.path("id").asText();
            assertTrue(ruleIds.add(ruleId), () -> "Duplicate permission_matrix rule id: " + ruleId);

            Set<String> allowedActors = readStringSet(ruleNode.path("allowed_actors"));
            Set<String> forbiddenActors = readStringSet(ruleNode.path("forbidden_actors"));
            allowedActors.forEach(actor -> assertTrue(actorTypes.contains(actor), () -> "Unknown permission_matrix allowed actor: " + actor));
            forbiddenActors.forEach(actor -> assertTrue(actorTypes.contains(actor), () -> "Unknown permission_matrix forbidden actor: " + actor));

            for (String endpointId : readStringList(ruleNode.path("endpoint_ids"))) {
                assertTrue(endpointIds.contains(endpointId), () -> "Unknown permission_matrix endpoint_id: " + endpointId);
            }

            for (String intentId : readStringList(ruleNode.path("intent_ids"))) {
                JsonNode intentNode = intentsById.get(intentId);
                assertNotNull(intentNode, () -> "Unknown permission_matrix intent_id: " + intentId);
                assertTrue(intentNode.path("mutating").asBoolean(), () -> "permission_matrix may only reference mutating intents: " + intentId);
                if (destructiveIntents.contains(intentId) || multiActorIntents.contains(intentId) || adminOnlyIntents.contains(intentId)) {
                    coveredHighRiskIntents.add(intentId);
                }
                if (adminOnlyIntents.contains(intentId)) {
                    assertEquals(Set.of("admin"), allowedActors,
                            () -> "Admin-only intent must only allow admin actor: " + intentId);
                }
            }
        }

        Set<String> requiredCoverage = new LinkedHashSet<>();
        requiredCoverage.addAll(destructiveIntents);
        requiredCoverage.addAll(multiActorIntents);
        requiredCoverage.addAll(adminOnlyIntents);
        assertEquals(requiredCoverage, coveredHighRiskIntents,
                "permission_matrix must cover all destructive, multi-actor, and admin-only intents");
    }

    private static void validateStateTransitionAudit(JsonNode stateTransitionAuditNode, JsonNode intentsNode, Path repoRoot, JsonNode permissionMatrixNode) throws Exception {
        Map<String, JsonNode> intentsById = readById(intentsNode);
        Set<String> actorTypes = readStringSet(permissionMatrixNode.path("actor_types"));
        Set<String> auditIds = new LinkedHashSet<>();
        Set<String> coveredIntentIds = new LinkedHashSet<>();

        for (JsonNode auditNode : iterable(stateTransitionAuditNode)) {
            String auditId = auditNode.path("id").asText();
            assertTrue(auditIds.add(auditId), () -> "Duplicate state_transition_audit id: " + auditId);

            String intentId = auditNode.path("intent_id").asText();
            JsonNode intentNode = intentsById.get(intentId);
            assertNotNull(intentNode, () -> "Unknown state_transition_audit intent_id: " + intentId);
            assertTrue(intentNode.path("mutating").asBoolean(), () -> "state_transition_audit may only reference mutating intents: " + intentId);
            coveredIntentIds.add(intentId);

            Class<?> statusEnumClass = Class.forName(auditNode.path("status_enum_class").asText());
            assertTrue(statusEnumClass.isEnum(), () -> "state_transition_audit status_enum_class must be enum: " + statusEnumClass.getName());
            Set<String> enumValues = List.of(statusEnumClass.getEnumConstants()).stream()
                    .map(value -> ((Enum<?>) value).name())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            for (String state : readStringList(auditNode.path("from_states"))) {
                assertTrue(enumValues.contains(state), () -> "Unknown state_transition_audit from_state " + state + " for " + auditId);
            }
            for (String state : readStringList(auditNode.path("to_states"))) {
                assertTrue(enumValues.contains(state), () -> "Unknown state_transition_audit to_state " + state + " for " + auditId);
            }
            for (String state : readStringList(auditNode.path("forbidden_states"))) {
                assertTrue(enumValues.contains(state), () -> "Unknown state_transition_audit forbidden_state " + state + " for " + auditId);
            }
            for (String actor : readStringList(auditNode.path("allowed_actors"))) {
                assertTrue(actorTypes.contains(actor), () -> "Unknown state_transition_audit actor: " + actor);
            }
            for (JsonNode verificationTestNode : auditNode.path("verification_tests")) {
                String verificationTest = verificationTestNode.asText();
                assertTrue(Files.exists(repoRoot.resolve(verificationTest)),
                        () -> "Missing state_transition_audit verification test: " + verificationTest);
            }
        }

        Set<String> requiredTransitionIntents = Set.of(
                "approve_application",
                "decline_application",
                "withdraw_my_application",
                "start_quest",
                "complete_quest",
                "request_owner_term_change",
                "confirm_quest_term_change",
                "reject_quest_term_change"
        );
        assertTrue(coveredIntentIds.containsAll(requiredTransitionIntents),
                () -> "state_transition_audit must cover required transition intents. Missing: "
                        + difference(requiredTransitionIntents, coveredIntentIds));
    }

    private static void validateRequestValidationGate(JsonNode requestValidationGateNode, Path repoRoot, Map<String, String> sourceFiles) throws Exception {
        Set<String> gateIds = new LinkedHashSet<>();
        for (JsonNode gateNode : iterable(requestValidationGateNode)) {
            String gateId = gateNode.path("id").asText();
            assertTrue(gateIds.add(gateId), () -> "Duplicate request_validation_gate id: " + gateId);

            Class<?> dtoClass = Class.forName(gateNode.path("java_class").asText());
            Map<String, Field> fieldsByName = collectFieldsByName(dtoClass);
            for (JsonNode validatedFieldNode : gateNode.path("validated_fields")) {
                String fieldName = validatedFieldNode.path("field").asText();
                Field field = fieldsByName.get(fieldName);
                assertNotNull(field, () -> "Unknown request_validation_gate field " + fieldName + " on " + dtoClass.getName());
                Set<String> annotationNames = collectAnnotationSimpleNames(field);
                for (String annotationName : readStringList(validatedFieldNode.path("required_annotations"))) {
                    assertTrue(annotationNames.contains(annotationName),
                            () -> "Missing request validation annotation " + annotationName + " on " + dtoClass.getName() + "." + fieldName);
                }
            }

            StringBuilder validatorSourceContent = new StringBuilder();
            for (JsonNode sourceRefNode : gateNode.path("service_validator_source_refs")) {
                String sourceRef = sourceRefNode.asText();
                String sourcePath = sourceFiles.get(sourceRef);
                assertNotNull(sourcePath, () -> "Unknown request_validation_gate service_validator_source_ref: " + sourceRef);
                validatorSourceContent.append(Files.readString(repoRoot.resolve(sourcePath))).append('\n');
            }

            String validatorContent = validatorSourceContent.toString();
            for (String marker : readStringList(gateNode.path("service_validation_markers"))) {
                assertTrue(validatorContent.contains(marker),
                        () -> "Missing request_validation_gate service validation marker '" + marker + "' for " + gateId);
            }

            for (JsonNode docPathNode : gateNode.path("doc_paths")) {
                String docPath = docPathNode.asText();
                assertTrue(Files.exists(repoRoot.resolve(docPath)), () -> "Missing request_validation_gate doc path: " + docPath);
            }

            for (JsonNode verificationTestNode : gateNode.path("verification_tests")) {
                String verificationTest = verificationTestNode.asText();
                assertTrue(Files.exists(repoRoot.resolve(verificationTest)),
                        () -> "Missing request_validation_gate verification test: " + verificationTest);
            }
        }
    }

    private static void validateCompositeWorkflowSafety(JsonNode intentsNode, JsonNode intentSafetyCatalogNode) {
        Map<String, JsonNode> intentsById = readById(intentsNode);
        Set<String> destructiveIntents = readStringSet(intentSafetyCatalogNode.path("destructive_confirmation"));
        Set<String> multiActorIntents = readStringSet(intentSafetyCatalogNode.path("multi_actor_authority"));
        Set<String> currentLocationIntents = readStringSet(intentSafetyCatalogNode.path("current_location_capability"));

        for (JsonNode intentNode : iterable(intentsNode)) {
            List<JsonNode> workflowSteps = iterable(intentNode.path("workflow"));
            List<String> childIntentRefs = workflowSteps.stream()
                    .map(step -> step.path("intent_ref").asText(null))
                    .filter(ref -> ref != null && !ref.isBlank())
                    .toList();
            if (childIntentRefs.isEmpty()) {
                continue;
            }

            int firstMutatingChildIndex = -1;
            for (int index = 0; index < workflowSteps.size(); index++) {
                String childIntentRef = workflowSteps.get(index).path("intent_ref").asText(null);
                if (childIntentRef == null || childIntentRef.isBlank()) {
                    continue;
                }
                if (intentsById.get(childIntentRef).path("mutating").asBoolean()) {
                    firstMutatingChildIndex = index;
                    break;
                }
            }

            if (firstMutatingChildIndex >= 0) {
                boolean hasPriorReadOrResolutionStep = workflowSteps.subList(0, firstMutatingChildIndex + 1).stream()
                        .anyMatch(step -> step.hasNonNull("endpoint_id")
                                || (step.hasNonNull("intent_ref") && !intentsById.get(step.path("intent_ref").asText()).path("mutating").asBoolean()));
                assertTrue(hasPriorReadOrResolutionStep,
                        () -> "Composite workflow must resolve or read context before the first mutating child intent: " + intentNode.path("id").asText());
            }

            Set<String> requiredInputs = readStringSet(intentNode.path("required_inputs"));
            for (String childIntentRef : childIntentRefs) {
                if (destructiveIntents.contains(childIntentRef)) {
                    assertTrue(requiredInputs.contains("destructive_confirmation") || destructiveIntents.contains(intentNode.path("id").asText()),
                            () -> "Composite workflow invoking destructive child intent must carry destructive confirmation semantics: " + intentNode.path("id").asText());
                }
                if (multiActorIntents.contains(childIntentRef)) {
                    assertTrue(multiActorIntents.contains(intentNode.path("id").asText()),
                            () -> "Composite workflow invoking multi-actor child intent must itself be classified as multi-actor: " + intentNode.path("id").asText());
                }
                if (currentLocationIntents.contains(childIntentRef)) {
                    assertTrue(currentLocationIntents.contains(intentNode.path("id").asText()) || requiredInputs.contains("latitude"),
                            () -> "Composite workflow invoking current-location child intent must preserve current-location capability semantics: " + intentNode.path("id").asText());
                }
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
                String normalizedContent = normalizeDocumentationAssertionText(content);
                for (JsonNode expectedTextNode : ruleNode.path("must_contain_all")) {
                    String expectedText = expectedTextNode.asText();
                    assertTrue(normalizedContent.contains(normalizeDocumentationAssertionText(expectedText)),
                            () -> "Missing required documentation text for rule " + ruleId + " in " + docPath + ": " + expectedText);
                }
            }
        }
    }

    private static String normalizeDocumentationAssertionText(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC).toLowerCase();
        normalized = normalized.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", " ");
        return normalized.trim().replaceAll("\\s+", " ");
    }

    private static Set<String> collectFieldNames(Class<?> type) {
        Set<String> fieldNames = new LinkedHashSet<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                fieldNames.add(field.getName());
            }
            current = current.getSuperclass();
        }
        return fieldNames;
    }

    private static List<String> collectDeclaredFieldNames(Class<?> type) {
        List<String> fieldNames = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            fieldNames.add(field.getName());
        }
        return fieldNames;
    }

    private static Map<String, Field> collectFieldsByName(Class<?> type) {
        Map<String, Field> fields = new HashMap<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                fields.putIfAbsent(field.getName(), field);
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    private static Set<String> collectAnnotationSimpleNames(Field field) {
        Set<String> annotationNames = new LinkedHashSet<>();
        for (Annotation annotation : field.getAnnotations()) {
            annotationNames.add(annotation.annotationType().getSimpleName());
        }
        for (Annotation annotation : field.getAnnotatedType().getAnnotations()) {
            annotationNames.add(annotation.annotationType().getSimpleName());
        }
        return annotationNames;
    }

    private static Set<String> readStringSet(JsonNode arrayNode) {
        return readStringList(arrayNode).stream().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static List<String> readStringList(JsonNode arrayNode) {
        List<String> values = new ArrayList<>();
        for (JsonNode node : iterable(arrayNode)) {
            values.add(node.asText());
        }
        return values;
    }

    private static Set<String> collectDirectMachineSourceRefs(JsonNode yaml, Map<String, String> sourceFiles) {
        Set<String> usedSourceRefs = new LinkedHashSet<>();
        Map<String, String> sourceRefByPath = new HashMap<>();
        sourceFiles.forEach((id, path) -> sourceRefByPath.put(path, id));

        for (JsonNode edgeCaseNode : iterable(yaml.path("policies").path("edge_cases"))) {
            edgeCaseNode.path("source_refs").forEach(node -> usedSourceRefs.add(node.asText()));
        }
        for (JsonNode intentNode : iterable(yaml.path("intents"))) {
            for (JsonNode workflowStep : iterable(intentNode.path("workflow"))) {
                workflowStep.path("source_refs").forEach(node -> usedSourceRefs.add(node.asText()));
            }
        }
        for (JsonNode ruleNode : iterable(yaml.path("documentation_sync").path("rules"))) {
            ruleNode.path("source_refs").forEach(node -> usedSourceRefs.add(node.asText()));
        }
        for (JsonNode modelNode : iterable(yaml.path("automation_read_models"))) {
            modelNode.path("producer_source_refs").forEach(node -> usedSourceRefs.add(node.asText()));
        }
        for (JsonNode snapshotNode : iterable(yaml.path("backend_contract_snapshots"))) {
            snapshotNode.path("source_refs").forEach(node -> usedSourceRefs.add(node.asText()));
        }
        for (JsonNode inventoryNode : iterable(yaml.path("service_workflow_inventory"))) {
            inventoryNode.path("source_refs").forEach(node -> usedSourceRefs.add(node.asText()));
        }
        for (JsonNode auditNode : iterable(yaml.path("state_transition_audit"))) {
            auditNode.path("source_refs").forEach(node -> usedSourceRefs.add(node.asText()));
        }
        for (JsonNode gateNode : iterable(yaml.path("request_validation_gate"))) {
            gateNode.path("service_validator_source_refs").forEach(node -> usedSourceRefs.add(node.asText()));
        }
        for (JsonNode groupNode : iterable(yaml.path("documentation_coverage").path("required_source_ref_groups"))) {
            groupNode.path("source_refs").forEach(node -> usedSourceRefs.add(node.asText()));
        }
        for (JsonNode endpointNode : iterable(yaml.path("api").path("endpoints"))) {
            String controllerClassName = endpointNode.path("controller_class").asText();
            String sourcePath = toSourcePath(controllerClassName);
            String sourceRef = sourceRefByPath.get(sourcePath);
            if (sourceRef != null) {
                usedSourceRefs.add(sourceRef);
            }
        }
        return usedSourceRefs;
    }

    private static Set<String> readCoveredPromptMatrixWorkflows(Path repoRoot) throws Exception {
        Path matrixPath = repoRoot.resolve("apps/themuffinman/src/test/resources/agent/admin-agent-golden-prompt-matrix.yaml");
        assertTrue(Files.exists(matrixPath), "Golden prompt matrix file must exist");
        JsonNode matrixNode = YAML_MAPPER.readTree(Files.readString(matrixPath));
        Set<String> workflows = new LinkedHashSet<>();
        for (JsonNode caseNode : iterable(matrixNode.path("cases"))) {
            for (JsonNode workflowNode : iterable(caseNode.path("expected").path("suggestedWorkflows"))) {
                workflows.add(workflowNode.asText());
            }
        }
        return workflows;
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

    private static Set<String> difference(Set<String> expected, Set<String> actual) {
        return expected.stream()
                .filter(item -> !actual.contains(item))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static String extractClassPath(Class<?> controllerClass) {
        RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);
        if (requestMapping == null || requestMapping.value().length == 0) {
            return "";
        }
        return requestMapping.value()[0];
    }

    private static Mapping extractMethodMapping(Method method) {
        Mapping mapping = tryExtractMethodMapping(method);
        assertNotNull(mapping, () -> "No supported request mapping annotation found for " + method);
        return mapping;
    }

    private static Mapping tryExtractMethodMapping(Method method) {
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
        return null;
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

    private static String toJavaClassName(String sourcePath) {
        String normalizedPath = sourcePath.replace('/', '.');
        String marker = "src.main.java.";
        int markerIndex = normalizedPath.indexOf(marker);
        assertTrue(markerIndex >= 0, () -> "Expected src/main/java path for source file: " + sourcePath);
        return normalizedPath.substring(markerIndex + marker.length(), normalizedPath.length() - ".java".length());
    }

    private static String toSourcePath(String javaClassName) {
        return "apps/themuffinman/src/main/java/" + javaClassName.replace('.', '/') + ".java";
    }

    private record Mapping(String httpMethod, String path) {
    }
}
