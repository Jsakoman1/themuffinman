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
import java.util.LinkedHashMap;
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
        validateGeneratedOperatingModelSections(repoRoot, yaml);
        validateEndpoints(endpoints);
        validateAllControllerMappingsAreDocumented(sourceFiles, endpoints);
        validateGeneratedEndpointInventory(repoRoot, endpoints);
        validatePolicies(yaml.path("policies"), sourceFiles.keySet());
        validateBackendAuditCoverage(yaml.path("backend_audit_coverage"));
        validateIntents(yaml.path("intents"), sourceFiles.keySet(), endpoints.keySet());
        validateAllEndpointsHaveIntentCoverage(yaml.path("intents"), endpoints.keySet());
        validateAutomationReadModels(yaml.path("automation_read_models"), repoRoot, sourceFiles);
        validateGeneratedAutomationReadModelInventory(repoRoot, yaml.path("automation_read_models"));
        validateMutatingIntentContracts(
                yaml.path("mutating_intent_contracts"),
                yaml.path("intents"),
                yaml.path("intent_safety_catalog"),
                sourceFiles.keySet()
        );
        validateIntentSafetyCatalog(yaml.path("intent_safety_catalog"), yaml.path("intents"), endpoints);
        validateDocumentationCoverage(yaml.path("documentation_coverage"), repoRoot, sourceFiles);
        validateDocumentationOwnership(yaml.path("documentation_ownership"), yaml.path("backend_audit_coverage"), repoRoot);
        validateGeneratedBackendAuditInventory(repoRoot, yaml.path("backend_audit_coverage"), sourceFiles);
        validateGeneratedSourceOfTruthAudit(repoRoot, sourceFiles);
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
        validatePersistentBacklogSystem(repoRoot);
        validateFeatureCompletionManifests(repoRoot);
        validateValidationEvidenceRecords(repoRoot);
        validateValidationMemory(repoRoot);
        validateExampleScenarioLibrary(repoRoot);
        validateRegressionScenarioCatalog(repoRoot);
        validateDocsAsContractSlices(repoRoot);
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

    private static void validateGeneratedOperatingModelSections(Path repoRoot, JsonNode yaml) throws Exception {
        Path sectionsDir = repoRoot.resolve("docs/agent-operating-model/sections");
        assertTrue(Files.isDirectory(sectionsDir), "docs/agent-operating-model/sections must exist");

        List<String> sectionOrder = List.of(
                "metadata",
                "source_of_truth",
                "backend_audit_coverage",
                "policies",
                "automation_read_models",
                "mutating_intent_contracts",
                "intent_safety_catalog",
                "documentation_coverage",
                "documentation_ownership",
                "frontend_contract_generation",
                "frontend_contracts",
                "frontend_safety_regressions",
                "frontend_feature_expectations",
                "dead_path_tracker",
                "capability_registry",
                "intent_lineage",
                "prompt_drift_detection",
                "backend_contract_snapshots",
                "service_workflow_inventory",
                "permission_matrix",
                "state_transition_audit",
                "request_validation_gate",
                "documentation_sync",
                "enums",
                "api",
                "intents"
        );

        Map<String, JsonNode> rebuilt = new HashMap<>();
        for (String sectionName : sectionOrder) {
            Path sectionPath = sectionsDir.resolve(sectionName + ".yaml");
            assertTrue(Files.exists(sectionPath), () -> "Missing agent operating model section: " + sectionPath);
            JsonNode sectionNode = YAML_MAPPER.readTree(Files.readString(sectionPath));
            assertTrue(sectionNode.has(sectionName), () -> "Section file must be keyed by " + sectionName + ": " + sectionPath);
            rebuilt.put(sectionName, sectionNode.path(sectionName));
        }

        for (String sectionName : sectionOrder) {
            assertEquals(yaml.path(sectionName), rebuilt.get(sectionName),
                    () -> "Generated operating model section drift for " + sectionName);
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

    private static void validateGeneratedEndpointInventory(Path repoRoot, Map<String, JsonNode> endpoints) throws Exception {
        Path inventoryPath = repoRoot.resolve("docs/generated/agent-endpoint-inventory.json");
        assertTrue(Files.exists(inventoryPath), "Generated agent endpoint inventory must exist");

        JsonNode inventory = JSON_MAPPER.readTree(Files.readString(inventoryPath));
        Set<String> generatedMappings = new LinkedHashSet<>();
        for (JsonNode entry : inventory) {
            generatedMappings.add(entry.path("controllerClass").asText()
                    + "#" + entry.path("handlerMethod").asText()
                    + "#" + entry.path("httpMethod").asText()
                    + "#" + entry.path("path").asText());
        }

        for (JsonNode endpointNode : endpoints.values()) {
            String documentedMapping = endpointNode.path("controller_class").asText()
                    + "#" + endpointNode.path("handler_method").asText()
                    + "#" + endpointNode.path("method").asText()
                    + "#" + endpointNode.path("path").asText();
            assertTrue(generatedMappings.contains(documentedMapping),
                    () -> "Generated endpoint inventory missing documented mapping: " + documentedMapping);
        }
    }

    private static void validateValidationMemory(Path repoRoot) throws Exception {
        Path jsonPath = repoRoot.resolve("docs/validation-memory.json");
        Path schemaPath = repoRoot.resolve("docs/validation-memory.schema.json");

        assertTrue(Files.exists(jsonPath), "validation-memory.json must exist");
        assertTrue(Files.exists(schemaPath), "validation-memory.schema.json must exist");

        JsonNode json = JSON_MAPPER.readTree(Files.readString(jsonPath));
        JsonNode schemaNode = JSON_MAPPER.readTree(Files.readString(schemaPath));
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
                .getSchema(schemaNode);
        Set<ValidationMessage> validationMessages = schema.validate(json);
        assertTrue(validationMessages.isEmpty(), () -> "Validation memory schema validation failed: " + validationMessages);

        assertEquals("docs/validation-memory.md", json.path("gatewayHints").path("primaryHumanDoc").asText(),
                "validation-memory primary human doc must stay aligned");
        assertTrue(Files.exists(repoRoot.resolve(json.path("gatewayHints").path("primaryHumanDoc").asText())),
                "validation-memory primary human doc must exist");
        assertTrue(json.path("canonicalCommands").path("frontendContract").toString().contains("npm run validate:contracts"),
                "validation-memory frontend contract commands must include npm run validate:contracts");
        assertTrue(json.path("canonicalCommands").path("backendLogic").toString().contains("./mvnw test"),
                "validation-memory backend logic commands must include ./mvnw test");
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
        Set<String> expectedSelfTestTierIds = Set.of(
                "syntax_only",
                "targeted_unit",
                "domain_scenario",
                "contract_type_check",
                "generated_artifact_validation",
                "full_validation");
        Set<String> selfTestTierIds = new LinkedHashSet<>();
        for (JsonNode tierNode : iterable(policiesNode.path("self_test_matrix").path("tiers"))) {
            String tierId = tierNode.path("id").asText();
            assertTrue(expectedSelfTestTierIds.contains(tierId), () -> "Unexpected self-test tier id: " + tierId);
            assertTrue(selfTestTierIds.add(tierId), () -> "Duplicate self-test tier id: " + tierId);
            assertFalse(tierNode.path("example_commands").isEmpty(),
                    () -> "Self-test tier must include example commands: " + tierId);
        }
        assertEquals(expectedSelfTestTierIds, selfTestTierIds,
                "self_test_matrix must define every required validation tier exactly once");
        for (JsonNode riskNode : iterable(policiesNode.path("self_test_matrix").path("risk_tiers"))) {
            for (String tierId : readStringList(riskNode.path("minimum_tiers"))) {
                assertTrue(selfTestTierIds.contains(tierId),
                        () -> "self_test_matrix risk tier references unknown tier: " + tierId);
            }
        }
        for (JsonNode profileNode : iterable(policiesNode.path("self_test_matrix").path("profile_overrides"))) {
            for (String tierId : readStringList(profileNode.path("add_tiers"))) {
                assertTrue(selfTestTierIds.contains(tierId),
                        () -> "self_test_matrix profile override references unknown tier: " + tierId);
            }
        }

        JsonNode validationEvidenceQualityNode = policiesNode.path("validation_evidence_quality");
        assertEquals("make audit-validation-evidence-quality", validationEvidenceQualityNode.path("audit_command").asText(),
                "validation evidence quality policy must name the strict audit command");
        Set<String> expectedValidationEvidenceRuleIds = Set.of(
                "exact_command_required",
                "scope_required",
                "skipped_reason_required",
                "vague_summary_rejected");
        Set<String> actualValidationEvidenceRuleIds = new LinkedHashSet<>();
        for (JsonNode ruleNode : iterable(validationEvidenceQualityNode.path("command_rules"))) {
            String ruleId = ruleNode.path("id").asText();
            assertTrue(expectedValidationEvidenceRuleIds.contains(ruleId),
                    () -> "Unexpected validation evidence quality rule id: " + ruleId);
            assertTrue(actualValidationEvidenceRuleIds.add(ruleId),
                    () -> "Duplicate validation evidence quality rule id: " + ruleId);
            assertFalse(ruleNode.path("applies_to").isEmpty(),
                    () -> "Validation evidence quality rule must name result states: " + ruleId);
        }
        assertEquals(expectedValidationEvidenceRuleIds, actualValidationEvidenceRuleIds,
                "validation_evidence_quality must define every required quality rule exactly once");

        JsonNode closeoutDocDeltaNode = policiesNode.path("closeout_doc_delta");
        assertEquals(
                Set.of("behavior_changed", "docs_updated", "intentionally_unchanged"),
                readStringSet(closeoutDocDeltaNode.path("required_fields")),
                "closeout_doc_delta must require behavior, docs, and intentionally unchanged summaries");
        assertFalse(closeoutDocDeltaNode.path("record_in").isEmpty(),
                "closeout_doc_delta must list where the summary is recorded");

        List<String> expectedCheckpointIds = List.of(
                "plan",
                "first_backend_slice",
                "first_frontend_slice",
                "docs_artifacts_sync",
                "validation",
                "commit_boundary"
        );
        List<String> actualCheckpointIds = new ArrayList<>();
        for (JsonNode checkpointNode : policiesNode.path("implementation_checkpoints").path("checkpoints")) {
            actualCheckpointIds.add(checkpointNode.path("id").asText());
            assertFalse(checkpointNode.path("required_evidence").isEmpty(),
                    () -> "Implementation checkpoint must define required evidence: " + checkpointNode.path("id").asText());
        }
        assertEquals(expectedCheckpointIds, actualCheckpointIds,
                "Implementation checkpoints must remain explicit and dependency-ordered");

        for (JsonNode edgeCaseNode : policiesNode.path("edge_cases")) {
            for (JsonNode sourceRefNode : edgeCaseNode.path("source_refs")) {
                String sourceRef = sourceRefNode.asText();
                assertTrue(sourceIds.contains(sourceRef), () -> "Unknown policy source_ref: " + sourceRef);
            }
        }
    }

    private static void validateBackendAuditCoverage(JsonNode backendAuditCoverageNode) {
        Set<String> tierIds = new LinkedHashSet<>();
        Set<String> domainIds = new LinkedHashSet<>();
        Set<String> domainOwnerPairs = new LinkedHashSet<>();
        Set<String> failHardTiers = readStringSet(backendAuditCoverageNode.path("current_enforcement").path("fail_hard_tiers"));
        Set<String> reportOnlyTiers = readStringSet(backendAuditCoverageNode.path("current_enforcement").path("report_only_tiers"));
        Set<String> strictSourceOfTruthRuleIds = readStringSet(backendAuditCoverageNode.path("current_enforcement").path("strict_source_of_truth_rule_ids"));
        Set<String> strictDocumentationRuleIds = readStringSet(backendAuditCoverageNode.path("current_enforcement").path("strict_documentation_rule_ids"));
        Set<String> knownTierIds = new LinkedHashSet<>();
        Set<String> knownRuleIds = new LinkedHashSet<>();

        for (JsonNode tierNode : iterable(backendAuditCoverageNode.path("tiers"))) {
            String tierId = tierNode.path("id").asText();
            assertTrue(tierIds.add(tierId), () -> "Duplicate backend_audit_coverage tier id: " + tierId);
            knownTierIds.add(tierId);
        }

        assertFalse(failHardTiers.isEmpty(), "backend_audit_coverage must define at least one fail_hard tier");
        assertTrue(failHardTiers.stream().allMatch(knownTierIds::contains),
                () -> "backend_audit_coverage fail_hard_tiers must reference known tiers only");
        assertTrue(reportOnlyTiers.stream().allMatch(knownTierIds::contains),
                () -> "backend_audit_coverage report_only_tiers must reference known tiers only");

        Set<String> overlap = new LinkedHashSet<>(failHardTiers);
        overlap.retainAll(reportOnlyTiers);
        assertTrue(overlap.isEmpty(), () -> "backend_audit_coverage fail_hard_tiers and report_only_tiers must not overlap: " + overlap);

        Set<String> coveredByEnforcement = new LinkedHashSet<>(failHardTiers);
        coveredByEnforcement.addAll(reportOnlyTiers);
        assertEquals(knownTierIds, coveredByEnforcement,
                "backend_audit_coverage enforcement sets must cover all defined tiers exactly");

        for (JsonNode domainNode : iterable(backendAuditCoverageNode.path("domain_ownership"))) {
            String domainId = domainNode.path("id").asText();
            String ownerId = domainNode.path("owner_id").asText();
            assertTrue(domainIds.add(domainId), () -> "Duplicate backend_audit_coverage domain id: " + domainId);
            assertFalse(ownerId.isBlank(), () -> "backend_audit_coverage owner_id must not be blank for domain " + domainId);
            assertTrue(domainOwnerPairs.add(domainId + "#" + ownerId),
                    () -> "Duplicate backend_audit_coverage domain ownership pair: " + domainId + "#" + ownerId);
        }
        assertFalse(domainIds.isEmpty(), "backend_audit_coverage must define at least one domain ownership rule");

        Set<String> ruleIds = new LinkedHashSet<>();
        for (JsonNode ruleNode : iterable(backendAuditCoverageNode.path("classification_rules"))) {
            String ruleId = ruleNode.path("id").asText();
            assertTrue(ruleIds.add(ruleId), () -> "Duplicate backend_audit_coverage classification rule id: " + ruleId);
            knownRuleIds.add(ruleId);
            String tier = ruleNode.path("tier").asText();
            assertTrue(knownTierIds.contains(tier), () -> "backend_audit_coverage classification rule references unknown tier: " + tier);
            boolean hasMatcher = ruleNode.has("file_names")
                    || ruleNode.has("file_suffixes")
                    || ruleNode.has("path_prefixes")
                    || ruleNode.has("path_contains_any");
            assertTrue(hasMatcher, () -> "backend_audit_coverage classification rule must define at least one matcher: " + ruleId);
        }

        assertTrue(strictSourceOfTruthRuleIds.stream().allMatch(knownRuleIds::contains),
                () -> "backend_audit_coverage strict_source_of_truth_rule_ids must reference known classification rules only");
        assertTrue(strictDocumentationRuleIds.stream().allMatch(knownRuleIds::contains),
                () -> "backend_audit_coverage strict_documentation_rule_ids must reference known classification rules only");

        for (JsonNode futureNode : iterable(backendAuditCoverageNode.path("future_tightening"))) {
            String targetTier = futureNode.path("target_tier").asText();
            assertTrue(knownTierIds.contains(targetTier),
                    () -> "backend_audit_coverage future_tightening references unknown tier: " + targetTier);
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

    private static void validateGeneratedAutomationReadModelInventory(Path repoRoot, JsonNode automationReadModelsNode) throws Exception {
        Path inventoryPath = repoRoot.resolve("docs/generated/automation-read-model-inventory.json");
        assertTrue(Files.exists(inventoryPath), "Generated automation read-model inventory must exist");

        JsonNode inventory = JSON_MAPPER.readTree(Files.readString(inventoryPath));
        Map<String, Set<String>> inventoryFieldsByClass = new HashMap<>();
        for (JsonNode entry : inventory) {
            inventoryFieldsByClass.put(
                    entry.path("javaClass").asText(),
                    readStringSet(entry.path("fields"))
            );
        }

        for (JsonNode modelNode : iterable(automationReadModelsNode)) {
            String javaClass = modelNode.path("java_class").asText();
            Set<String> generatedFields = inventoryFieldsByClass.get(javaClass);
            assertNotNull(generatedFields, () -> "Generated automation read-model inventory missing class: " + javaClass);
            for (JsonNode requiredFieldNode : modelNode.path("required_fields")) {
                String requiredField = requiredFieldNode.asText();
                assertTrue(generatedFields.contains(requiredField),
                        () -> "Generated automation read-model inventory missing field " + requiredField + " for " + javaClass);
            }
        }
    }

    private static void validateMutatingIntentContracts(
            JsonNode mutatingIntentContractsNode,
            JsonNode intentsNode,
            JsonNode intentSafetyCatalogNode,
            Set<String> sourceIds
    ) {
        Map<String, JsonNode> intentsById = readById(intentsNode);
        Set<String> mutatingIntentIds = intentsById.entrySet().stream()
                .filter(entry -> entry.getValue().path("mutating").asBoolean())
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> destructiveIntents = readStringSet(intentSafetyCatalogNode.path("destructive_confirmation"));
        Set<String> documentedIntentIds = new LinkedHashSet<>();

        for (JsonNode contractNode : iterable(mutatingIntentContractsNode)) {
            String intentId = contractNode.path("intent_id").asText();
            assertTrue(documentedIntentIds.add(intentId), () -> "Duplicate mutating_intent_contracts intent_id: " + intentId);
            JsonNode intentNode = intentsById.get(intentId);
            assertNotNull(intentNode, () -> "Unknown mutating_intent_contracts intent: " + intentId);
            assertTrue(intentNode.path("mutating").asBoolean(), () -> "mutating_intent_contracts may only describe mutating intents: " + intentId);

            assertFalse(readStringList(contractNode.path("preconditions")).isEmpty(),
                    () -> "mutating_intent_contracts.preconditions must not be empty for " + intentId);
            assertFalse(readStringList(contractNode.path("state_changes")).isEmpty(),
                    () -> "mutating_intent_contracts.state_changes must not be empty for " + intentId);
            assertFalse(readStringList(contractNode.path("side_effects")).isEmpty(),
                    () -> "mutating_intent_contracts.side_effects must not be empty for " + intentId);
            assertFalse(readStringList(contractNode.path("blocking_conditions")).isEmpty(),
                    () -> "mutating_intent_contracts.blocking_conditions must not be empty for " + intentId);
            for (String sourceRef : readStringList(contractNode.path("source_refs"))) {
                assertTrue(sourceIds.contains(sourceRef), () -> "Unknown mutating_intent_contracts source_ref: " + sourceRef);
            }

            if (destructiveIntents.contains(intentId)) {
                assertTrue(readStringList(contractNode.path("blocking_conditions")).stream()
                                .anyMatch(condition -> condition.toLowerCase().contains("destructive confirmation")),
                        () -> "Destructive mutating intent contract must mention destructive confirmation: " + intentId);
            }
        }

        assertEquals(mutatingIntentIds, documentedIntentIds,
                () -> "mutating_intent_contracts must cover every mutating intent exactly");
    }

    private static void validateGeneratedSourceOfTruthAudit(Path repoRoot, Map<String, String> sourceFiles) throws Exception {
        Path auditScriptPath = repoRoot.resolve("scripts/generate-source-of-truth-audit.rb");
        Path auditReportPath = repoRoot.resolve("docs/generated/source-of-truth-audit.json");
        assertTrue(Files.exists(auditScriptPath), () -> "Missing source-of-truth audit script: " + auditScriptPath);
        assertTrue(Files.exists(auditReportPath), () -> "Missing source-of-truth audit report: " + auditReportPath);

        JsonNode auditNode = JSON_MAPPER.readTree(Files.readString(auditReportPath));
        assertTrue(iterable(auditNode.path("candidateEntries")).size() > 0,
                "Source-of-truth audit must include ownership-aware candidateEntries");
        assertTrue(auditNode.path("summary").path("domainCounts").isObject(),
                "Source-of-truth audit summary must include domainCounts");
        assertTrue(auditNode.path("summary").path("ownerCounts").isObject(),
                "Source-of-truth audit summary must include ownerCounts");
        for (JsonNode candidateNode : iterable(auditNode.path("candidateEntries"))) {
            String path = candidateNode.path("path").asText();
            assertTrue(Files.exists(repoRoot.resolve(path)),
                    () -> "Source-of-truth audit candidate references missing file: " + path);
            assertTrue(candidateNode.path("domainId").isTextual() && !candidateNode.path("domainId").asText().isBlank(),
                    () -> "Source-of-truth audit candidate must include domainId: " + path);
            assertTrue(candidateNode.path("ownerId").isTextual() && !candidateNode.path("ownerId").asText().isBlank(),
                    () -> "Source-of-truth audit candidate must include ownerId: " + path);
            assertTrue(candidateNode.path("sourceRefId").isTextual() && sourceFiles.containsKey(candidateNode.path("sourceRefId").asText()),
                    () -> "Source-of-truth audit candidate must include known sourceRefId: " + path);
        }
        assertTrue(iterable(auditNode.path("missingSourceRefs")).isEmpty(),
                () -> "Source-of-truth audit found unregistered files: " + auditNode.path("missingSourceRefs"));
        assertTrue(iterable(auditNode.path("missingDocumentationCoverage")).isEmpty(),
                () -> "Source-of-truth audit found files missing documentation coverage: " + auditNode.path("missingDocumentationCoverage"));
        assertTrue(iterable(auditNode.path("missingServiceWorkflowCoverage")).isEmpty(),
                () -> "Source-of-truth audit found mutating services missing workflow coverage: " + auditNode.path("missingServiceWorkflowCoverage"));
    }

    private static void validateGeneratedBackendAuditInventory(
            Path repoRoot,
            JsonNode backendAuditCoverageNode,
            Map<String, String> sourceFiles
    ) throws Exception {
        Path inventoryScriptPath = repoRoot.resolve("scripts/generate-backend-audit-inventory.rb");
        Path inventoryReportPath = repoRoot.resolve("docs/generated/backend-audit-inventory.json");
        assertTrue(Files.exists(inventoryScriptPath), () -> "Missing backend audit inventory script: " + inventoryScriptPath);
        assertTrue(Files.exists(inventoryReportPath), () -> "Missing backend audit inventory report: " + inventoryReportPath);

        JsonNode inventoryNode = JSON_MAPPER.readTree(Files.readString(inventoryReportPath));
        Set<String> knownTierIds = readById(backendAuditCoverageNode.path("tiers")).keySet();
        Set<String> knownDomainIds = readById(backendAuditCoverageNode.path("domain_ownership")).keySet();
        Set<String> knownOwnerIds = iterable(backendAuditCoverageNode.path("domain_ownership")).stream()
                .map(node -> node.path("owner_id").asText())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> failHardTiers = readStringSet(backendAuditCoverageNode.path("current_enforcement").path("fail_hard_tiers"));
        Set<String> strictSourceOfTruthRuleIds = readStringSet(backendAuditCoverageNode.path("current_enforcement").path("strict_source_of_truth_rule_ids"));
        Set<String> strictDocumentationRuleIds = readStringSet(backendAuditCoverageNode.path("current_enforcement").path("strict_documentation_rule_ids"));
        assertTrue(iterable(inventoryNode.path("entries")).size() > 0, "backend audit inventory must contain entries");
        assertTrue(iterable(inventoryNode.path("unclassifiedPaths")).isEmpty(),
                () -> "backend audit inventory found unclassified backend files: " + inventoryNode.path("unclassifiedPaths"));
        assertTrue(iterable(inventoryNode.path("unownedDomainPaths")).isEmpty(),
                () -> "backend audit inventory found files without domain ownership mapping: " + inventoryNode.path("unownedDomainPaths"));
        assertTrue(iterable(inventoryNode.path("strictSourceOfTruthMissing")).isEmpty(),
                () -> "backend audit inventory found strict source-of-truth gaps: " + inventoryNode.path("strictSourceOfTruthMissing"));
        assertTrue(iterable(inventoryNode.path("strictDocumentationMissing")).isEmpty(),
                () -> "backend audit inventory found strict documentation coverage gaps: " + inventoryNode.path("strictDocumentationMissing"));

        Set<String> seenPaths = new LinkedHashSet<>();
        for (JsonNode entryNode : iterable(inventoryNode.path("entries"))) {
            String path = entryNode.path("path").asText();
            assertTrue(seenPaths.add(path), () -> "Duplicate backend audit inventory path: " + path);
            assertTrue(Files.exists(repoRoot.resolve(path)), () -> "backend audit inventory references missing file: " + path);

            String tier = entryNode.path("tier").asText();
            assertTrue(knownTierIds.contains(tier), () -> "backend audit inventory references unknown tier: " + tier);
            assertTrue(entryNode.path("classificationRuleId").isTextual() && !entryNode.path("classificationRuleId").asText().isBlank(),
                    () -> "backend audit inventory must include classificationRuleId for " + path);
            String domainId = entryNode.path("domainId").asText();
            String ownerId = entryNode.path("ownerId").asText();
            assertTrue(knownDomainIds.contains(domainId), () -> "backend audit inventory references unknown domain: " + domainId + " for " + path);
            assertTrue(knownOwnerIds.contains(ownerId), () -> "backend audit inventory references unknown owner: " + ownerId + " for " + path);

            boolean registeredInSourceOfTruth = entryNode.path("registeredInSourceOfTruth").asBoolean();
            String classificationRuleId = entryNode.path("classificationRuleId").asText();
            if (registeredInSourceOfTruth) {
                String sourceRefId = entryNode.path("sourceRefId").asText();
                assertTrue(sourceFiles.containsKey(sourceRefId),
                        () -> "backend audit inventory references unknown source ref " + sourceRefId + " for " + path);
            }

            if (failHardTiers.contains(tier)) {
                assertTrue(registeredInSourceOfTruth,
                        () -> "Fail-hard backend audit tier file must be registered in source_of_truth: " + path);
            }
            if (strictSourceOfTruthRuleIds.contains(classificationRuleId)) {
                assertTrue(registeredInSourceOfTruth,
                        () -> "Strict source-of-truth backend audit rule must be registered in source_of_truth: " + path);
            }
            if (strictDocumentationRuleIds.contains(classificationRuleId)) {
                assertTrue(entryNode.path("documentationCovered").asBoolean(),
                        () -> "Strict documentation backend audit rule must be documentation-covered: " + path);
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

    private static void validateDocumentationOwnership(JsonNode documentationOwnershipNode, JsonNode backendAuditCoverageNode, Path repoRoot) throws Exception {
        Set<String> requiredTemplateIds = Set.of(
                "new_workflow",
                "new_endpoint",
                "new_dto_contract",
                "new_module",
                "new_permission_rule",
                "schema_migration");
        Set<String> documentedTemplateIds = new LinkedHashSet<>();
        for (JsonNode templateNode : iterable(documentationOwnershipNode.path("documentation_templates"))) {
            String templateId = templateNode.path("id").asText();
            assertTrue(requiredTemplateIds.contains(templateId),
                    () -> "Unexpected documentation template id: " + templateId);
            assertTrue(documentedTemplateIds.add(templateId),
                    () -> "Duplicate documentation template id: " + templateId);
            assertExistingPaths(repoRoot, templateNode.path("required_living_docs"),
                    "documentation template docs " + templateId);

            Path templatePath = repoRoot.resolve(templateNode.path("template_path").asText());
            assertTrue(Files.exists(templatePath), () -> "Missing documentation template file: " + templatePath);
            String templateContent = Files.readString(templatePath);
            for (JsonNode sectionNode : iterable(templateNode.path("required_sections"))) {
                String section = sectionNode.asText();
                assertTrue(templateContent.contains("## " + section),
                        () -> "Documentation template " + templateId + " is missing section: " + section);
            }
        }
        assertEquals(requiredTemplateIds, documentedTemplateIds,
                "documentation_ownership must define every required documentation template exactly once");

        Set<String> knownDomainIds = new LinkedHashSet<>();
        for (JsonNode domainNode : iterable(backendAuditCoverageNode.path("domain_ownership"))) {
            knownDomainIds.add(domainNode.path("id").asText());
        }

        Set<String> documentedDomainIds = new LinkedHashSet<>();
        for (JsonNode domainNode : iterable(documentationOwnershipNode.path("domains"))) {
            String domainId = domainNode.path("id").asText();
            assertTrue(knownDomainIds.contains(domainId),
                    () -> "documentation_ownership domain must match backend_audit_coverage domain_ownership id: " + domainId);
            assertTrue(documentedDomainIds.add(domainId), () -> "Duplicate documentation_ownership domain id: " + domainId);
            assertExistingPaths(repoRoot, domainNode.path("required_living_docs"), "documentation_ownership domain docs " + domainId);
            assertExistingPaths(repoRoot, domainNode.path("generated_artifacts"), "documentation_ownership domain artifacts " + domainId);
            assertExistingPaths(repoRoot, domainNode.path("validation_tests"), "documentation_ownership domain tests " + domainId);
        }
        assertEquals(knownDomainIds, documentedDomainIds,
                "documentation_ownership must map every backend audit domain exactly once");

        Set<String> categoryIds = new LinkedHashSet<>();
        for (JsonNode categoryNode : iterable(documentationOwnershipNode.path("change_categories"))) {
            String categoryId = categoryNode.path("id").asText();
            assertTrue(categoryIds.add(categoryId), () -> "Duplicate documentation_ownership change category id: " + categoryId);
            assertExistingPaths(repoRoot, categoryNode.path("required_living_docs"), "documentation_ownership category docs " + categoryId);
            assertExistingPaths(repoRoot, categoryNode.path("generated_artifacts"), "documentation_ownership category artifacts " + categoryId);
            assertFalse(categoryNode.path("validation_commands").isEmpty(),
                    () -> "documentation_ownership change category must list validation commands: " + categoryId);
        }
    }

    private static void validateFrontendContractGeneration(JsonNode frontendContractGenerationNode, Path repoRoot) throws Exception {
        Path scriptPath = repoRoot.resolve(frontendContractGenerationNode.path("script").asText());
        Path generatedContractPath = repoRoot.resolve(frontendContractGenerationNode.path("generated_contract_file").asText());
        assertTrue(Files.exists(scriptPath), () -> "Missing frontend contract generation script: " + scriptPath);
        assertFalse(frontendContractGenerationNode.path("check_command").asText().isBlank(),
                "frontend_contract_generation.check_command must be set");
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
        Path todoAuditScriptPath = repoRoot.resolve("scripts/todo-audit.rb");
        Map<String, String> openBacklogEntries = readOpenBacklogEntries(repoRoot);

        assertTrue(Files.exists(schemaPath), "feature-completion-manifest.schema.json must exist");
        assertTrue(Files.exists(templatePath), "feature completion manifest template must exist");
        assertTrue(Files.exists(planTemplatePath), "feature implementation plan template must exist");
        assertTrue(Files.exists(bootstrapScriptPath), "bootstrap-feature-work.sh must exist");
        assertTrue(Files.exists(closeoutAuditScriptPath), "feature-closeout-audit.sh must exist");
        assertTrue(Files.exists(todoAuditScriptPath), "todo-audit.rb must exist");
        assertTrue(Files.isDirectory(manifestsDir), ".agents/feature-manifests must exist");
        String planTemplateContent = Files.readString(planTemplatePath);
        assertTrue(planTemplateContent.contains("Doc delta summary"),
                "feature implementation plan template must request doc delta summary evidence");

        JsonNode schemaNode = JSON_MAPPER.readTree(Files.readString(schemaPath));
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(schemaNode);
        JsonNode manifestTemplateNode = YAML_MAPPER.readTree(Files.readString(templatePath));
        JsonNode docDeltaNode = manifestTemplateNode.path("docDelta");
        assertFalse(docDeltaNode.path("behaviorChanged").isEmpty(),
                "feature completion manifest template must include docDelta.behaviorChanged");
        assertFalse(docDeltaNode.path("docsUpdated").isEmpty(),
                "feature completion manifest template must include docDelta.docsUpdated");
        assertFalse(docDeltaNode.path("intentionallyUnchanged").isEmpty(),
                "feature completion manifest template must include docDelta.intentionallyUnchanged");
        assertFalse(manifestTemplateNode.path("validationEvidence").path("commands").isEmpty(),
                "feature completion manifest template must include validationEvidence.commands");
        assertTrue(manifestTemplateNode.path("validationEvidence").path("commands").get(0).has("skippedReason"),
                "feature completion manifest template must include skipped-check reason placeholder");
        assertTrue(manifestTemplateNode.path("planCompletion").has("openTasks"),
                "feature completion manifest template must include planCompletion.openTasks");
        assertTrue(manifestTemplateNode.path("closeoutDecision").has("status"),
                "feature completion manifest template must include closeoutDecision.status");

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
            String changeMode = manifestNode.path("changeMode").asText();
            String changeImpact = manifestNode.path("changeImpact").asText();
            Set<String> changeProfiles = readStringSet(manifestNode.path("changeProfiles"));
            boolean backlogReviewed = manifestNode.path("backlog").path("reviewed").asBoolean();
            List<String> createdBacklogIds = readStringList(manifestNode.path("backlog").path("createdIds"));
            List<String> resolvedBacklogIds = readStringList(manifestNode.path("backlog").path("resolvedIds"));
            List<String> auditCommands = readStringList(manifestNode.path("artifacts").path("auditCommands"));
            List<String> generatorCommands = readStringList(manifestNode.path("artifacts").path("generatorCommands"));
            List<String> codePaths = readStringList(manifestNode.path("artifacts").path("codePaths"));
            List<String> docPaths = readStringList(manifestNode.path("artifacts").path("docPaths"));
            List<String> testPaths = readStringList(manifestNode.path("artifacts").path("testPaths"));
            List<JsonNode> validationEvidenceCommands = iterable(manifestNode.path("validationEvidence").path("commands"));

            assertEquals(codePaths.size(), new LinkedHashSet<>(codePaths).size(),
                    () -> "Feature manifest codePaths must not contain duplicates: " + manifestPath);
            assertEquals(docPaths.size(), new LinkedHashSet<>(docPaths).size(),
                    () -> "Feature manifest docPaths must not contain duplicates: " + manifestPath);
            assertEquals(testPaths.size(), new LinkedHashSet<>(testPaths).size(),
                    () -> "Feature manifest testPaths must not contain duplicates: " + manifestPath);

            for (String artifactPath : codePaths) {
                assertTrue(Files.exists(repoRoot.resolve(artifactPath)),
                        () -> "Feature manifest references missing codePath: " + artifactPath + " in " + manifestPath);
                assertFalse(artifactPath.contains("/src/test/") || artifactPath.contains("/src/test/resources/"),
                        () -> "Feature manifest codePaths must not contain test-only files: " + artifactPath + " in " + manifestPath);
            }
            for (String artifactPath : docPaths) {
                assertTrue(Files.exists(repoRoot.resolve(artifactPath)),
                        () -> "Feature manifest references missing docPath: " + artifactPath + " in " + manifestPath);
            }
            for (String artifactPath : testPaths) {
                assertTrue(Files.exists(repoRoot.resolve(artifactPath)),
                        () -> "Feature manifest references missing testPath: " + artifactPath + " in " + manifestPath);
            }

            Set<String> codeTestOverlap = new LinkedHashSet<>(codePaths);
            codeTestOverlap.retainAll(testPaths);
            assertTrue(codeTestOverlap.isEmpty(),
                    () -> "Feature manifest must not list the same path in both codePaths and testPaths: " + codeTestOverlap + " in " + manifestPath);
            Set<String> allArtifactPaths = new LinkedHashSet<>();
            List<String> duplicatedArtifactPaths = new ArrayList<>();
            for (String artifactPath : codePaths) {
                if (!allArtifactPaths.add(artifactPath)) {
                    duplicatedArtifactPaths.add(artifactPath);
                }
            }
            for (String artifactPath : docPaths) {
                if (!allArtifactPaths.add(artifactPath)) {
                    duplicatedArtifactPaths.add(artifactPath);
                }
            }
            for (String artifactPath : testPaths) {
                if (!allArtifactPaths.add(artifactPath)) {
                    duplicatedArtifactPaths.add(artifactPath);
                }
            }
            assertTrue(duplicatedArtifactPaths.isEmpty(),
                    () -> "Feature manifest must not list the same path in multiple artifact buckets: "
                            + duplicatedArtifactPaths + " in " + manifestPath);

            Set<String> createdResolvedOverlap = new LinkedHashSet<>(createdBacklogIds);
            createdResolvedOverlap.retainAll(new LinkedHashSet<>(resolvedBacklogIds));
            assertTrue(createdResolvedOverlap.isEmpty(),
                    () -> "Feature manifest backlog.createdIds and backlog.resolvedIds must not overlap: " + createdResolvedOverlap + " in " + manifestPath);

            if (!createdBacklogIds.isEmpty() || !resolvedBacklogIds.isEmpty()) {
                assertTrue(docPaths.contains("docs/implementation-backlog.md") || docPaths.contains("docs/agent-improvement-backlog.md"),
                        () -> "Feature manifest with backlog links must include a persistent backlog docPath: " + manifestPath);
            }
            for (String createdBacklogId : createdBacklogIds) {
                assertTrue(openBacklogEntries.containsKey(createdBacklogId),
                        () -> "Feature manifest created backlog ID must stay open in a persistent backlog file: "
                                + createdBacklogId + " in " + manifestPath);
            }
            for (String resolvedBacklogId : resolvedBacklogIds) {
                assertFalse(openBacklogEntries.containsKey(resolvedBacklogId),
                        () -> "Feature manifest resolved backlog ID must be removed from open backlog files: "
                                + resolvedBacklogId + " in " + manifestPath);
            }

            if ("high".equals(riskTier) || "executor-critical".equals(riskTier)) {
                assertTrue(auditCommands.contains("make audit-agent-safety"),
                        () -> "High-risk or executor-critical manifest must require make audit-agent-safety: " + manifestPath);
            }
            if (manifestNode.path("checklist").path("frontendValidationPassed").asBoolean()) {
                assertTrue(auditCommands.contains("npm run type-check"),
                        () -> "Manifest with frontend validation must include npm run type-check audit command: " + manifestPath);
            }
            if ("complete".equals(manifestNode.path("status").asText())) {
                assertTrue(backlogReviewed,
                        () -> "Completed manifest must mark backlog.reviewed=true: " + manifestPath);
                assertFalse(validationEvidenceCommands.isEmpty(),
                        () -> "Completed manifest must include validationEvidence.commands: " + manifestPath);
                for (JsonNode evidenceCommand : validationEvidenceCommands) {
                    assertFalse(evidenceCommand.path("command").asText().isBlank(),
                            () -> "Validation evidence command must name exact command: " + manifestPath);
                    assertFalse(evidenceCommand.path("summary").asText().isBlank(),
                            () -> "Validation evidence command must include summary: " + manifestPath);
                    assertFalse("failed".equals(evidenceCommand.path("result").asText()),
                            () -> "Completed manifest must not include failed validation evidence: " + manifestPath);
                    if ("skipped".equals(evidenceCommand.path("result").asText())
                            || "not_applicable".equals(evidenceCommand.path("result").asText())) {
                        assertFalse(evidenceCommand.path("skippedReason").asText().isBlank(),
                                () -> "Skipped validation evidence must include skippedReason: " + manifestPath);
                    }
                }
                assertTrue(manifestNode.path("checklist").path("codeImplemented").asBoolean(),
                        () -> "Completed manifest must mark codeImplemented=true: " + manifestPath);
                assertTrue(manifestNode.path("checklist").path("backendTestsPassed").asBoolean(),
                        () -> "Completed manifest must mark backendTestsPassed=true: " + manifestPath);
                assertTrue(manifestNode.path("checklist").path("docsSynced").asBoolean(),
                        () -> "Completed manifest must mark docsSynced=true: " + manifestPath);
                assertTrue(manifestNode.path("checklist").path("agentModelSynced").asBoolean(),
                        () -> "Completed manifest must mark agentModelSynced=true: " + manifestPath);
                assertTrue(manifestNode.path("checklist").path("destructivePolicyChecked").asBoolean(),
                        () -> "Completed manifest must mark destructivePolicyChecked=true: " + manifestPath);
                assertTrue(manifestNode.path("checklist").path("multilingualCoverageChecked").asBoolean(),
                        () -> "Completed manifest must mark multilingualCoverageChecked=true: " + manifestPath);
                assertTrue(manifestNode.path("planCompletion").path("reviewed").asBoolean(),
                        () -> "Completed manifest must mark planCompletion.reviewed=true: " + manifestPath);
                assertEquals(0, manifestNode.path("planCompletion").path("openTasks").asInt(),
                        () -> "Completed manifest must mark planCompletion.openTasks=0: " + manifestPath);
                assertEquals("ready", manifestNode.path("closeoutDecision").path("status").asText(),
                        () -> "Completed manifest must mark closeoutDecision.status=ready: " + manifestPath);
                assertTrue(hasPassedEvidence(validationEvidenceCommands, "make audit-todo"),
                        () -> "Completed manifest must include passed make audit-todo evidence: " + manifestPath);
                List<String> openPlanTasks = Files.readAllLines(referencedPlan).stream()
                        .filter(line -> line.startsWith("- [ ]"))
                        .toList();
                assertTrue(openPlanTasks.isEmpty(),
                        () -> "Completed manifest must not reference a plan with open tasks: " + manifestPath);
            }

            if ("major-change".equals(changeMode)) {
                assertTrue(manifestNode.path("checklist").path("tempPlanCreated").asBoolean(),
                        () -> "Major-change manifest must mark tempPlanCreated=true: " + manifestPath);
                assertFalse(generatorCommands.isEmpty(),
                        () -> "Major-change manifest must declare generatorCommands: " + manifestPath);
            }

            if ("cosmetic".equals(changeImpact)) {
                assertTrue("small-change".equals(changeMode),
                        () -> "Cosmetic manifest must use small-change mode: " + manifestPath);
            }

            if ("logic-drift".equals(changeImpact)) {
                assertTrue(docPaths.contains("docs/domain-technical.md"),
                        () -> "Logic-drift manifest must include docs/domain-technical.md: " + manifestPath);
            }

            if (changeProfiles.contains("backend-logic")) {
                assertTrue(docPaths.contains("docs/domain-technical.md"),
                        () -> "backend-logic manifest must include docs/domain-technical.md: " + manifestPath);
                assertTrue(auditCommands.contains("./mvnw test") || auditCommands.contains("make audit-agent-safety"),
                        () -> "backend-logic manifest must include backend validation audit command: " + manifestPath);
                if ("complete".equals(manifestNode.path("status").asText())) {
                    assertTrue(hasPassedEvidence(validationEvidenceCommands, "./mvnw test")
                                    || hasPassedEvidence(validationEvidenceCommands, "make audit-agent-safety"),
                            () -> "Completed backend-logic manifest must include passed backend validation evidence: " + manifestPath);
                }
            }

            if (changeProfiles.contains("agent-contract")) {
                assertTrue(docPaths.contains("docs/agent-operating-model.md"),
                        () -> "agent-contract manifest must include docs/agent-operating-model.md: " + manifestPath);
                assertTrue(docPaths.contains("docs/agent-operating-model.yaml"),
                        () -> "agent-contract manifest must include docs/agent-operating-model.yaml: " + manifestPath);
                assertTrue(generatorCommands.contains("make generate-agent-operating-model"),
                        () -> "agent-contract manifest must include make generate-agent-operating-model: " + manifestPath);
                assertTrue(auditCommands.contains("make audit-agent-safety"),
                        () -> "agent-contract manifest must include make audit-agent-safety: " + manifestPath);
                if ("complete".equals(manifestNode.path("status").asText())) {
                    assertTrue(hasPassedEvidence(validationEvidenceCommands, "make audit-agent-safety"),
                            () -> "Completed agent-contract manifest must include passed make audit-agent-safety evidence: " + manifestPath);
                    assertTrue(hasPassedEvidence(validationEvidenceCommands, "make generate-agent-operating-model")
                                    || hasPassedEvidence(validationEvidenceCommands, "make generate-agent-artifacts"),
                            () -> "Completed agent-contract manifest must include passed generated model/artifact evidence: " + manifestPath);
                }
            }

            if (changeProfiles.contains("frontend-contract")) {
                assertTrue(manifestNode.path("checklist").path("frontendValidationPassed").asBoolean(),
                        () -> "frontend-contract manifest must mark frontendValidationPassed=true: " + manifestPath);
                assertTrue(auditCommands.contains("npm run validate:contracts"),
                        () -> "frontend-contract manifest must include npm run validate:contracts: " + manifestPath);
                assertTrue(auditCommands.contains("npm run type-check"),
                        () -> "frontend-contract manifest must include npm run type-check: " + manifestPath);
                assertTrue(auditCommands.contains("npm run build") || auditCommands.contains("make audit-agent-safety"),
                        () -> "frontend-contract manifest must include frontend build validation: " + manifestPath);
                if ("complete".equals(manifestNode.path("status").asText())) {
                    assertTrue(hasPassedEvidence(validationEvidenceCommands, "npm run validate:contracts"),
                            () -> "Completed frontend-contract manifest must include passed contract validation evidence: " + manifestPath);
                    assertTrue(hasPassedEvidence(validationEvidenceCommands, "npm run type-check"),
                            () -> "Completed frontend-contract manifest must include passed type-check evidence: " + manifestPath);
                    assertTrue(hasPassedEvidence(validationEvidenceCommands, "npm run build"),
                            () -> "Completed frontend-contract manifest must include passed build evidence: " + manifestPath);
                }
            }

            if (changeProfiles.contains("workflow-expansion")) {
                assertTrue(generatorCommands.contains("make generate-agent-artifacts"),
                        () -> "workflow-expansion manifest must include make generate-agent-artifacts: " + manifestPath);
                assertTrue(testPaths.stream().anyMatch(path -> path.contains("ScenarioTest")),
                        () -> "workflow-expansion manifest must include at least one ScenarioTest: " + manifestPath);
                if ("complete".equals(manifestNode.path("status").asText())) {
                    assertTrue(hasPassedEvidence(validationEvidenceCommands, "ScenarioTest")
                                    || hasPassedEvidence(validationEvidenceCommands, "UseCaseContractTest"),
                            () -> "Completed workflow-expansion manifest must include passed scenario or use-case contract evidence: " + manifestPath);
                }
            }
        }
    }

    private static void validateValidationEvidenceRecords(Path repoRoot) throws Exception {
        Path evidenceDir = repoRoot.resolve(".agents/validation-evidence");
        Path schemaPath = repoRoot.resolve("docs/validation-evidence.schema.json");
        Path templatePath = repoRoot.resolve(".agents/templates/validation-evidence.template.yaml");

        assertTrue(Files.exists(schemaPath), "validation-evidence.schema.json must exist");
        assertTrue(Files.exists(templatePath), "validation evidence template must exist");
        assertTrue(Files.exists(repoRoot.resolve("scripts/audits/audit-validation-evidence-quality.rb")),
                "validation evidence quality audit script must exist");
        assertTrue(Files.isDirectory(evidenceDir), ".agents/validation-evidence must exist");

        JsonNode schemaNode = JSON_MAPPER.readTree(Files.readString(schemaPath));
        JsonSchema schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(schemaNode);

        JsonNode templateNode = YAML_MAPPER.readTree(Files.readString(templatePath));
        Set<ValidationMessage> templateMessages = schema.validate(templateNode);
        assertTrue(templateMessages.isEmpty(), () -> "Validation evidence template schema validation failed: " + templateMessages);

        List<Path> evidencePaths;
        try (var paths = Files.list(evidenceDir)) {
            evidencePaths = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".yaml"))
                    .sorted()
                    .toList();
        }

        Set<String> changeIds = new LinkedHashSet<>();
        for (Path evidencePath : evidencePaths) {
            JsonNode evidenceNode = YAML_MAPPER.readTree(Files.readString(evidencePath));
            Set<ValidationMessage> validationMessages = schema.validate(evidenceNode);
            assertTrue(validationMessages.isEmpty(),
                    () -> "Validation evidence schema validation failed for " + evidencePath + ": " + validationMessages);

            String changeId = evidenceNode.path("changeId").asText();
            assertTrue(changeIds.add(changeId), () -> "Duplicate validation evidence changeId: " + changeId);
            for (JsonNode artifactNode : iterable(evidenceNode.path("generatedArtifacts"))) {
                if (!"not_applicable".equals(artifactNode.path("action").asText())) {
                    String artifactPath = artifactNode.path("path").asText();
                    assertTrue(Files.exists(repoRoot.resolve(artifactPath)),
                            () -> "Validation evidence references missing generated artifact: " + artifactPath + " in " + evidencePath);
                }
            }
        }
    }

    private static void validateExampleScenarioLibrary(Path repoRoot) throws Exception {
        Path libraryPath = repoRoot.resolve("docs/example-scenario-library.md");
        assertTrue(Files.exists(libraryPath), "example scenario library must exist");
        String content = Files.readString(libraryPath);
        for (String heading : List.of(
                "## Add An Endpoint",
                "## Change A Workflow Transition",
                "## Add A DTO Contract",
                "## Add A Schema Migration",
                "## Update Documentation")) {
            assertTrue(content.contains(heading),
                    () -> "example scenario library is missing required heading: " + heading);
        }
    }

    private static void validateRegressionScenarioCatalog(Path repoRoot) throws Exception {
        Path catalogYamlPath = repoRoot.resolve("docs/regression-scenario-catalog.yaml");
        Path catalogMarkdownPath = repoRoot.resolve("docs/regression-scenario-catalog.md");
        assertTrue(Files.exists(catalogYamlPath), "regression scenario catalog YAML must exist");
        assertTrue(Files.exists(catalogMarkdownPath), "regression scenario catalog summary must exist");

        JsonNode catalogNode = YAML_MAPPER.readTree(Files.readString(catalogYamlPath));
        List<JsonNode> scenarios = iterable(catalogNode.path("regression_scenarios"));
        assertFalse(scenarios.isEmpty(), "regression scenario catalog must contain scenarios");

        Set<String> expectedDomains = Set.of(
                "agent",
                "workmarket",
                "social",
                "location",
                "identity",
                "business",
                "things",
                "rides",
                "common",
                "vision");
        Set<String> expectedRisks = Set.of("low", "medium", "high", "executor-critical");
        Set<String> seenIds = new LinkedHashSet<>();
        Set<String> coveredDomains = new LinkedHashSet<>();
        String markdown = Files.readString(catalogMarkdownPath);

        for (JsonNode scenarioNode : scenarios) {
            String id = scenarioNode.path("id").asText();
            String domain = scenarioNode.path("domain").asText();
            String risk = scenarioNode.path("risk").asText();
            assertFalse(id.isBlank(), "regression scenario id must not be blank");
            assertTrue(seenIds.add(id), () -> "Duplicate regression scenario id: " + id);
            assertTrue(markdown.contains(id), () -> "regression scenario summary must mention id: " + id);
            assertTrue(expectedDomains.contains(domain), () -> "Unexpected regression scenario domain: " + domain);
            assertTrue(expectedRisks.contains(risk), () -> "Unexpected regression scenario risk: " + risk);
            assertFalse(scenarioNode.path("scenario").asText().isBlank(),
                    () -> "Regression scenario must describe the protected behavior: " + id);

            List<String> testFiles = readStringList(scenarioNode.path("test_files"));
            List<String> commands = readStringList(scenarioNode.path("commands"));
            assertFalse(testFiles.isEmpty(), () -> "Regression scenario must list test files: " + id);
            assertFalse(commands.isEmpty(), () -> "Regression scenario must list commands: " + id);

            Set<String> testClassNames = new LinkedHashSet<>();
            for (String testFile : testFiles) {
                Path testPath = repoRoot.resolve(testFile);
                assertTrue(Files.exists(testPath), () -> "Regression scenario references missing test file: " + testFile);
                assertTrue(testFile.endsWith("Test.java"), () -> "Regression scenario test file must be a Java test: " + testFile);
                testClassNames.add(testPath.getFileName().toString().replace(".java", ""));
            }

            for (String command : commands) {
                assertTrue(command.startsWith("cd apps/themuffinman && ./mvnw test -Dtest="),
                        () -> "Regression scenario command must be a focused backend test command: " + command);
                for (String testClassName : testClassNames) {
                    assertTrue(command.contains(testClassName),
                            () -> "Regression scenario command must include listed test class " + testClassName + ": " + command);
                }
            }
            coveredDomains.add(domain);
        }

        assertTrue(coveredDomains.containsAll(expectedDomains),
                () -> "regression scenario catalog must cover core domains. Missing: " + expectedDomains.stream()
                        .filter(domain -> !coveredDomains.contains(domain))
                        .toList());
    }

    private static void validateDocsAsContractSlices(Path repoRoot) throws Exception {
        Path slicesYamlPath = repoRoot.resolve("docs/docs-as-contract-slices.yaml");
        Path slicesMarkdownPath = repoRoot.resolve("docs/docs-as-contract-slices.md");
        assertTrue(Files.exists(slicesYamlPath), "docs-as-contract slices YAML must exist");
        assertTrue(Files.exists(slicesMarkdownPath), "docs-as-contract slices summary must exist");

        JsonNode slicesNode = YAML_MAPPER.readTree(Files.readString(slicesYamlPath));
        List<JsonNode> slices = iterable(slicesNode.path("docs_as_contract_slices"));
        assertFalse(slices.isEmpty(), "docs-as-contract catalog must contain slices");

        Set<String> expectedDomains = Set.of(
                "agent",
                "workmarket",
                "social",
                "chat",
                "location",
                "identity",
                "common");
        Set<String> seenIds = new LinkedHashSet<>();
        String markdown = Files.readString(slicesMarkdownPath);

        for (JsonNode sliceNode : slices) {
            String id = sliceNode.path("id").asText();
            String domain = sliceNode.path("domain").asText();
            assertFalse(id.isBlank(), "docs-as-contract slice id must not be blank");
            assertTrue(seenIds.add(id), () -> "Duplicate docs-as-contract slice id: " + id);
            assertTrue(markdown.contains(id), () -> "docs-as-contract summary must mention id: " + id);
            assertTrue(expectedDomains.contains(domain), () -> "Unexpected docs-as-contract domain: " + domain);
            assertFalse(sliceNode.path("protected_claim").asText().isBlank(),
                    () -> "docs-as-contract slice must describe the protected claim: " + id);

            for (JsonNode docNode : iterable(sliceNode.path("docs"))) {
                String docPath = docNode.path("path").asText();
                Path resolvedDocPath = repoRoot.resolve(docPath);
                assertTrue(Files.exists(resolvedDocPath),
                        () -> "docs-as-contract slice references missing doc: " + docPath);
                String docContent = Files.readString(resolvedDocPath);
                List<String> headings = readStringList(docNode.path("headings"));
                assertFalse(headings.isEmpty(), () -> "docs-as-contract doc entry must list headings: " + id + " -> " + docPath);
                for (String heading : headings) {
                    assertTrue(docContent.contains(heading),
                            () -> "docs-as-contract slice references missing heading " + heading + " in " + docPath);
                }
            }

            List<String> testFiles = readStringList(sliceNode.path("test_files"));
            List<String> commands = readStringList(sliceNode.path("commands"));
            assertFalse(testFiles.isEmpty(), () -> "docs-as-contract slice must list test files: " + id);
            assertFalse(commands.isEmpty(), () -> "docs-as-contract slice must list commands: " + id);

            Set<String> testClassNames = new LinkedHashSet<>();
            for (String testFile : testFiles) {
                Path testPath = repoRoot.resolve(testFile);
                assertTrue(Files.exists(testPath), () -> "docs-as-contract slice references missing test file: " + testFile);
                assertTrue(testFile.endsWith("Test.java"), () -> "docs-as-contract test file must be a Java test: " + testFile);
                testClassNames.add(testPath.getFileName().toString().replace(".java", ""));
            }

            for (String command : commands) {
                assertTrue(command.startsWith("cd apps/themuffinman && ./mvnw test -Dtest="),
                        () -> "docs-as-contract command must be a focused backend test command: " + command);
                for (String testClassName : testClassNames) {
                    assertTrue(command.contains(testClassName),
                            () -> "docs-as-contract command must include listed test class " + testClassName + ": " + command);
                }
            }
        }
    }

    private static void validatePersistentBacklogSystem(Path repoRoot) throws Exception {
        Path implementationBacklogPath = repoRoot.resolve("docs/implementation-backlog.md");
        Path agentImprovementBacklogPath = repoRoot.resolve("docs/agent-improvement-backlog.md");
        Path todoAuditScriptPath = repoRoot.resolve("scripts/todo-audit.rb");

        assertTrue(Files.exists(implementationBacklogPath), "docs/implementation-backlog.md must exist");
        assertTrue(Files.exists(agentImprovementBacklogPath), "docs/agent-improvement-backlog.md must exist");
        assertTrue(Files.exists(todoAuditScriptPath), "scripts/todo-audit.rb must exist");

        readOpenBacklogEntries(repoRoot);
    }

    private static Map<String, String> readOpenBacklogEntries(Path repoRoot) throws Exception {
        Map<String, String> openEntries = new LinkedHashMap<>();
        for (String backlogPath : List.of("docs/implementation-backlog.md", "docs/agent-improvement-backlog.md")) {
            Path path = repoRoot.resolve(backlogPath);
            assertTrue(Files.exists(path), () -> "Missing persistent backlog file: " + backlogPath);
            List<String> lines = Files.readAllLines(path);
            for (int index = 0; index < lines.size(); index++) {
                String line = lines.get(index);
                int lineNumber = index + 1;
                if (line.startsWith("- [x]")) {
                    throw new AssertionError("Resolved backlog entries must be removed instead of kept as closed checkboxes: "
                            + backlogPath + ":" + lineNumber);
                }
                if (!line.startsWith("- [ ]")) {
                    continue;
                }

                int colonIndex = line.indexOf(": ");
                assertTrue(colonIndex > "- [ ] ".length(),
                        () -> "Persistent backlog entries must use '- [ ] BACKLOG_ID: description': "
                                + backlogPath + ":" + lineNumber);

                String backlogId = line.substring("- [ ] ".length(), colonIndex);
                assertTrue(backlogId.matches("[A-Z0-9_-]+"),
                        () -> "Persistent backlog ID must use uppercase letters, digits, underscore, or dash: "
                                + backlogPath + ":" + lineNumber);

                String location = backlogPath + ":" + lineNumber;
                assertFalse(openEntries.containsKey(backlogId),
                        () -> "Duplicate persistent backlog ID " + backlogId + " in "
                                + openEntries.get(backlogId) + " and " + location);
                openEntries.put(backlogId, location);
            }
        }
        return openEntries;
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

    private static boolean hasPassedEvidence(List<JsonNode> evidenceCommands, String commandFragment) {
        return evidenceCommands.stream()
                .anyMatch(commandNode -> "passed".equals(commandNode.path("result").asText())
                        && commandNode.path("command").asText().contains(commandFragment));
    }

    private static void assertExistingPaths(Path repoRoot, JsonNode pathNodes, String label) {
        for (JsonNode pathNode : iterable(pathNodes)) {
            String relativePath = pathNode.asText();
            assertTrue(Files.exists(repoRoot.resolve(relativePath)),
                    () -> "Missing " + label + " path: " + relativePath);
        }
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
