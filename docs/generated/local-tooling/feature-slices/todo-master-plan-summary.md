# Feature Slices todo-master-plan

- Generated At: `2026-06-29T10:50:51Z`
- Topic: `todo-master-plan`
## `domains`

- `agent`
- `chat`
- `identity`
- `location`
- `shared`
- `social`
- `workmarket`

## `categories`

- `backend_dto`
- `backend_repository`
- `backend_service`
- `docs`
- `frontend_api`
- `frontend_composable`
- `frontend_script`
- `frontend_view`
- `other`
- `script`

- Original File Count: `334`
- Filtered File Count: `162`
## `files_considered`

- `.agents/feature-manifests/agent-control-phase-two-manifest.yaml`
- `.agents/feature-manifests/agent-operating-refactor-manifest.yaml`
- `.agents/feature-manifests/agent-safety-enforcement-round2-manifest.yaml`
- `.agents/feature-manifests/agent-safety-upgrade-manifest.yaml`
- `.agents/feature-manifests/backend-audit-domain-tagging-manifest.yaml`
- `.agents/feature-manifests/backend-audit-identity-dto-tightening-manifest.yaml`
- `.agents/feature-manifests/backend-audit-location-dto-tightening-manifest.yaml`
- `.agents/feature-manifests/backend-audit-manifest-cleanup-manifest.yaml`
- `.agents/feature-manifests/backend-audit-tiering-manifest.yaml`
- `.agents/feature-manifests/backend-audit-tightening-manifest.yaml`
- `.agents/feature-manifests/executor-readiness-program-manifest.yaml`
- `.agents/feature-manifests/persistent-backlog-system-manifest.yaml`
- `.agents/templates/feature-completion-manifest.template.yaml`
- `.agents/templates/feature-implementation-plan.template.md`
- `.gitignore`
- `Makefile`
- `apps/themuffinman/frontend/README.md`
- `apps/themuffinman/frontend/package.json`
- `apps/themuffinman/frontend/scripts/generate-workmarket-contracts.mjs`
- `apps/themuffinman/frontend/scripts/validate-admin-agent-ui-scenarios.mjs`
- `apps/themuffinman/frontend/src/components/ui/UiDashboardPage.vue`
- `apps/themuffinman/frontend/src/modules/moduleRegistry.ts`
- `apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue`
- `apps/themuffinman/frontend/src/modules/social/views/CirclesView.vue`
- `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createDashboardSelectors.ts`

## `slices`

- `{:id: "backend", :purpose: "Implement the smallest backend behavior or contract change before widening scope.", :files: ["apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationSettingsService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/DashboardSectionsDTO.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/repository/QuestApplicationRepository.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/repository/QuestRepository.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestExecutionPrimitiveService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestStateTransitionService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestUpdateService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestValidationService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestVisibilityService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestWorkflowNotificationService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/UserReviewService.java", "apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentCapabilityBoundaryTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentGoldenPromptMatrixTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundServiceTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java", "apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationAccessPolicyService.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/DashboardNavigationItemDTO.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/DashboardNavigationSectionDTO.java", "apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java"], :docs: ["docs/agent-operating-model.md", "docs/agent-operating-model.yaml", "docs/domain-technical.md", "docs/business-logic.md", "docs/location-services.md"], :validation: ["./mvnw test", "npm run type-check", "npm run build", "make generate-agent-artifacts", "make audit-repository-fetch", "make audit-read-surface-inventory", "make audit-documentation", "make audit-doc-canonical-phrases", "make audit-api-contract-drift", "make audit-async-mutation-flow"]}`
- `{:id: "frontend", :purpose: "Implement the smallest frontend behavior or contract change before widening scope.", :files: ["apps/themuffinman/frontend/scripts/generate-workmarket-contracts.mjs", "apps/themuffinman/frontend/scripts/validate-admin-agent-ui-scenarios.mjs", "apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue", "apps/themuffinman/frontend/src/modules/social/views/CirclesView.vue", "apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createDashboardSelectors.ts", "apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createQuestDashboardStateModules.ts", "apps/themuffinman/frontend/src/modules/workmarket/composables/quest-detail/useQuestDetailEdit.ts", "apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDetailView.ts", "apps/themuffinman/frontend/src/modules/workmarket/pages/AdminAgentPage.vue", "apps/themuffinman/frontend/src/modules/workmarket/pages/AdminApplicationsPage.vue", "apps/themuffinman/frontend/src/modules/workmarket/pages/AdminOverviewPage.vue", "apps/themuffinman/frontend/src/modules/workmarket/pages/AdminQuestsPage.vue", "apps/themuffinman/frontend/src/modules/workmarket/pages/AdminUsersPage.vue", "apps/themuffinman/frontend/src/modules/workmarket/pages/QuestsPage.vue", "apps/themuffinman/frontend/src/modules/workmarket/views/QuestDetailView.vue", "apps/themuffinman/frontend/src/modules/chat/views/", "apps/themuffinman/frontend/src/modules/workmarket/api/agentWorkflowGuards.ts"], :docs: ["docs/agent-operating-model.md", "docs/agent-operating-model.yaml", "docs/domain-technical.md", "docs/business-logic.md", "docs/location-services.md"], :validation: ["./mvnw test", "npm run type-check", "npm run build", "make generate-agent-artifacts", "make audit-repository-fetch", "make audit-read-surface-inventory", "make audit-documentation", "make audit-doc-canonical-phrases", "make audit-api-contract-drift", "make audit-async-mutation-flow"]}`
- `{:id: "docs-and-artifacts", :purpose: "Update living docs and generated artifacts that move with the implementation.", :files: ["docs/agent-improvement-backlog.md", "docs/agent-operating-model.md", "docs/agent-operating-model.schema.json", "docs/agent-operating-model.yaml", "docs/agent-operating-model/sections/api.yaml", "docs/agent-operating-model/sections/backend_audit_coverage.yaml", "docs/agent-operating-model/sections/documentation_coverage.yaml", "docs/agent-operating-model/sections/documentation_sync.yaml", "docs/agent-operating-model/sections/frontend_contract_generation.yaml", "docs/agent-operating-model/sections/intent_safety_catalog.yaml", "docs/agent-operating-model/sections/intents.yaml", "docs/agent-operating-model/sections/mutating_intent_contracts.yaml", "docs/agent-operating-model/sections/policies.yaml", "docs/agent-operating-model/sections/request_validation_gate.yaml", "docs/agent-operating-model/sections/service_workflow_inventory.yaml", "docs/agent-operating-model/sections/source_of_truth.yaml", "docs/business-logic.md", "docs/change-completion-checklist.md", "docs/codex-local-tooling-todo.md", "docs/documentation-sync-policy.md", "docs/domain-technical.md", "docs/feature-completion-manifest.schema.json", "docs/implementation-backlog.md", "docs/tooling/codex-local-audits.md", "docs/tooling/codex-local-audits.yml", "docs/agent-operating-model/sections/documentation_ownership.yaml", "docs/cross-domain-glossary.md", "docs/docs-as-contract-slices.md", "docs/docs-as-contract-slices.yaml", "docs/example-scenario-library.md"], :validation: ["make audit-documentation", "make audit-generated-artifact-freshness"]}`
- `{:id: "final-validation", :purpose: "Run focused and broad validation after implementation slices are complete.", :files: [], :validation: ["npm run type-check", "npm run build", "make audit-frontend-route-surfaces", "make audit-async-mutation-flow", "./mvnw test", "make audit-read-surface-inventory", "make audit-repository-fetch", "make generate-agent-artifacts", "make audit-documentation", "make audit-doc-canonical-phrases", "ruby -c <script>", "make audit-summary-index"]}`

## `read_next`

- `Run `make context-pack topic=todo-master-plan` before editing if more file context is needed.`
- `Run `make audit-router files=<csv>` after the first implementation slice.`
- `Keep slices sequential; avoid mixing backend, frontend, generated artifacts, and final validation in one edit pass unless the change is tiny.`

