# Feature Slices implementation-system

- Generated At: `2026-07-10T13:53:00Z`
- Topic: `implementation-system`
## `domains`

- `agent`
- `business`
- `chat`
- `identity`
- `shared`

## `categories`

- `backend_controller`
- `backend_dto`
- `backend_model`
- `backend_repository`
- `backend_service`
- `docs`
- `frontend_api`
- `frontend_composable`
- `... 4 more`

- Original File Count: `88`
- Filtered File Count: `0`
## `files_considered`

- `.agents/feature-manifests/chat-prod-ready-manifest.yaml`
- `.agents/templates/feature-implementation-plan.template.md`
- `AGENTS.md`
- `Makefile`
- `apps/themuffinman/frontend/scripts/generate-vision-contracts.mjs`
- `apps/themuffinman/frontend/src/modules/vision/api/visionConversationApi.ts`
- `apps/themuffinman/frontend/src/modules/vision/components/VisionCanvasRenderer.vue`
- `apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts`
- `... 72 more`

## `slices`

- `{:id: "backend", :purpose: "Implement the smallest backend behavior or contract change before widening scope.", :files: ["apps/themuffinman/src/main/java/com/themuffinman/app/chat/controller/ChatController.java", "apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatConversationListDTO.java", "apps/themuffinman/src/main/java/com/themuffinman/app/chat/service/ChatService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionConversationTurnRequestDTO.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionConversationTurnResponseDTO.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/model/VisionConversation.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/model/VisionIntent.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/repository/VisionConversationRepository.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionBusinessPreviewRenderer.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionCanvasAssembler.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionCapabilityPreviewService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionChatExecutionService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionClarificationService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationLifecycleSupport.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionConversationSnapshotSupport.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionEntityFamilySupport.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionIntentRouter.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionIntentSignalSupport.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionReadOnlyConversationTurnHandler.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSearchDiscoveryService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSemanticMapper.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSemanticOrchestrationContextService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSemanticOrchestrationRequest.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSemanticPromptPayloadBuilder.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSemanticRouteCatalogService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSemanticRuntimeHints.java", "apps/themuffinman/src/test/java/com/themuffinman/app/business/service/BusinessBookingValidationServiceTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/chat/controller/ChatControllerTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/chat/service/ChatServiceTest.java"], :docs: ["docs/agent-operating-model.md", "docs/agent-operating-model.yaml", "docs/domain-technical.md", "docs/business-logic.md"], :validation: ["./mvnw test", "make generate-agent-artifacts", "make audit-api-contract-drift", "npm run type-check", "npm run build", "make audit-migration-entity-drift", "make audit-repository-fetch", "make audit-read-surface-inventory", "make audit-documentation", "make audit-doc-canonical-phrases"]}`
- `{:id: "frontend", :purpose: "Implement the smallest frontend behavior or contract change before widening scope.", :files: ["apps/themuffinman/frontend/scripts/generate-vision-contracts.mjs", "apps/themuffinman/frontend/src/modules/vision/api/visionConversationApi.ts", "apps/themuffinman/frontend/src/modules/vision/composables/useVisionConversation.ts", "apps/themuffinman/frontend/src/modules/vision/views/VisionSurfaceModernView.vue"], :docs: ["docs/agent-operating-model.md", "docs/agent-operating-model.yaml", "docs/domain-technical.md", "docs/business-logic.md"], :validation: ["./mvnw test", "make generate-agent-artifacts", "make audit-api-contract-drift", "npm run type-check", "npm run build", "make audit-migration-entity-drift", "make audit-repository-fetch", "make audit-read-surface-inventory", "make audit-documentation", "make audit-doc-canonical-phrases"]}`
- `{:id: "docs-and-artifacts", :purpose: "Update living docs and generated artifacts that move with the implementation.", :files: ["docs/agent-improvement-backlog.md", "docs/agent-operating-model.md", "docs/agent-operating-model.yaml", "docs/agent-operating-model/sections/backend_contract_snapshots.yaml", "docs/agent-operating-model/sections/documentation_coverage.yaml", "docs/agent-operating-model/sections/policies.yaml", "docs/agent-operating-model/sections/service_workflow_inventory.yaml", "docs/agent-operating-model/sections/source_of_truth.yaml", "docs/business-logic.md", "docs/change-completion-checklist.md", "docs/codex-fast-path.md", "docs/documentation-sync-policy.md", "docs/domain-technical.md", "docs/feature-delivery-workflow.md", "docs/product-vision.md", "docs/program-planning-model.md", "docs/tooling/codex-local-audits.yml", "docs/validation-memory.json", "docs/validation-memory.md", "docs/vision-architecture-patterns.md", "docs/vision-status-ledger.md"], :validation: ["make audit-documentation", "make audit-generated-artifact-freshness"]}`
- `{:id: "final-validation", :purpose: "Run focused and broad validation after implementation slices are complete.", :files: [], :validation: ["npm run type-check", "npm run build", "make audit-api-contract-drift", "make audit-async-mutation-flow", "make audit-frontend-route-surfaces", "./mvnw test", "make generate-agent-artifacts", "make audit-read-surface-inventory", "make audit-repository-fetch", "make audit-migration-entity-drift", "make audit-documentation", "make audit-doc-canonical-phrases"]}`

## `read_next`

- `Run `make context-pack topic=implementation-system` before editing if more file context is needed.`
- `Run `make audit-router files=<csv>` after the first implementation slice.`
- `Keep slices sequential; avoid mixing backend, frontend, generated artifacts, and final validation in one edit pass unless the change is tiny.`

