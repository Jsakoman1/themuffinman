# Targeted Tests

- Generated At: `2026-06-29T12:47:32Z`
- Original File Count: `373`
- Filtered File Count: `195`
- Excluded File Count: `195`
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

## `domains`

- `agent`
- `chat`
- `common`
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

## `direct_tests`

- `apps/themuffinman/src/test/java/com/themuffinman/app/agent/sandbox/SandboxGenerationPlannerTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentCapabilityBoundaryTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentGoldenPromptMatrixTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AgentOperatingScenarioTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/business/service/BusinessProfileServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/chat/service/ChatServiceTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/common/concepts/CoreConceptsTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/common/controller/GlobalExceptionHandlerTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/common/validation/RichTextInputValidatorTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/config/ServiceLayeringConventionTest.java`
- `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`

## `recommended_commands`

- `{:command: "cd apps/themuffinman && ./mvnw test -Dtest=SandboxGenerationPlannerTest,AdminAgentCapabilityBoundaryTest,AdminAgentGoldenPromptMatrixTest,AdminAgentPlaygroundServiceTest,AgentOperatingScenarioTest,BusinessProfileServiceTest,ChatServiceTest,CoreConceptsTest", :reason: "Runs nearest backend tests for changed Java files.", :confidence: "high", :covers: ["apps/themuffinman/src/test/java/com/themuffinman/app/agent/sandbox/SandboxGenerationPlannerTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentCapabilityBoundaryTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentGoldenPromptMatrixTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundServiceTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AgentOperatingScenarioTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/business/service/BusinessProfileServiceTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/chat/service/ChatServiceTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/common/concepts/CoreConceptsTest.java"], :uncovered: ["Some backend files only matched by domain or broad category."]}`
- `{:command: "npm --prefix apps/themuffinman/frontend run type-check", :reason: "Frontend TypeScript or Vue files changed.", :confidence: "high", :covers: ["apps/themuffinman/frontend/README.md", "apps/themuffinman/frontend/package.json", "apps/themuffinman/frontend/scripts/generate-workmarket-contracts.mjs", "apps/themuffinman/frontend/scripts/validate-admin-agent-ui-scenarios.mjs", "apps/themuffinman/frontend/src/components/ui/UiDashboardPage.vue", "apps/themuffinman/frontend/src/modules/moduleRegistry.ts", "apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue", "apps/themuffinman/frontend/src/modules/social/views/CirclesView.vue", "apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createDashboardSelectors.ts", "apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createQuestDashboardStateModules.ts", "apps/themuffinman/frontend/src/modules/workmarket/composables/quest-detail/useQuestDetailEdit.ts", "apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDetailView.ts"], :uncovered: []}`
- `{:command: "npm --prefix apps/themuffinman/frontend run build", :reason: "Covers Vite/build-time imports, generated contract usage, and production bundling.", :confidence: "medium", :covers: ["apps/themuffinman/frontend/README.md", "apps/themuffinman/frontend/package.json", "apps/themuffinman/frontend/scripts/generate-workmarket-contracts.mjs", "apps/themuffinman/frontend/scripts/validate-admin-agent-ui-scenarios.mjs", "apps/themuffinman/frontend/src/components/ui/UiDashboardPage.vue", "apps/themuffinman/frontend/src/modules/moduleRegistry.ts", "apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue", "apps/themuffinman/frontend/src/modules/social/views/CirclesView.vue", "apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createDashboardSelectors.ts", "apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createQuestDashboardStateModules.ts", "apps/themuffinman/frontend/src/modules/workmarket/composables/quest-detail/useQuestDetailEdit.ts", "apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDetailView.ts"], :uncovered: ["Does not replace browser-level interaction checks for visual or workflow changes."]}`
- `{:command: "make audit-documentation", :reason: "Docs, plans, or agent artifacts changed.", :confidence: "high", :covers: [".agents/feature-manifests/agent-control-phase-two-manifest.yaml", ".agents/feature-manifests/agent-operating-refactor-manifest.yaml", ".agents/feature-manifests/agent-safety-enforcement-round2-manifest.yaml", ".agents/feature-manifests/agent-safety-upgrade-manifest.yaml", ".agents/feature-manifests/backend-audit-domain-tagging-manifest.yaml", ".agents/feature-manifests/backend-audit-identity-dto-tightening-manifest.yaml", ".agents/feature-manifests/backend-audit-location-dto-tightening-manifest.yaml", ".agents/feature-manifests/backend-audit-manifest-cleanup-manifest.yaml", ".agents/feature-manifests/backend-audit-tiering-manifest.yaml", ".agents/feature-manifests/backend-audit-tightening-manifest.yaml", ".agents/feature-manifests/executor-readiness-program-manifest.yaml", ".agents/feature-manifests/persistent-backlog-system-manifest.yaml"], :uncovered: []}`
- `{:command: "make audit-doc-canonical-phrases", :reason: "Protected documentation wording may be affected by docs or agent-safety edits.", :confidence: "medium", :covers: ["docs/agent-improvement-backlog.md", "docs/agent-operating-model.md", "docs/agent-operating-model.schema.json", "docs/agent-operating-model.yaml", "docs/agent-operating-model/sections/api.yaml", "docs/agent-operating-model/sections/backend_audit_coverage.yaml", "docs/agent-operating-model/sections/documentation_coverage.yaml", "docs/agent-operating-model/sections/documentation_sync.yaml", "docs/agent-operating-model/sections/frontend_contract_generation.yaml", "docs/agent-operating-model/sections/intent_safety_catalog.yaml", "docs/agent-operating-model/sections/intents.yaml", "docs/agent-operating-model/sections/mutating_intent_contracts.yaml"], :uncovered: ["Does not validate Java-side agent operating model tests."]}`
- `{:command: "make audit-generated-artifact-freshness", :reason: "Generated artifacts, generation scripts, or Make targets changed.", :confidence: "high", :covers: ["Makefile", "scripts/audits/audit-change-impact-preflight.rb", "scripts/audits/local_tooling_batch_audits.rb", "scripts/audits/local_tooling_extended_tools.rb", "scripts/feature-closeout-audit.sh", "scripts/generate-agent-operating-model.rb", "scripts/generate-source-of-truth-audit.rb", "scripts/local_tooling_common.rb", "scripts/todo-audit.rb", "scripts/audits/audit-architecture-drift.rb", "scripts/audits/audit-contract-test-gaps.rb", "scripts/audits/audit-delta-report.rb"], :uncovered: []}`
- `{:command: "make audit-generated-commit-scope", :reason: "Classifies changed generated artifacts before closeout.", :confidence: "medium", :covers: [], :uncovered: ["Advisory only; reviewer still chooses which generated files belong in the changeset."]}`
- `{:command: "cd apps/themuffinman && ./mvnw test -Dtest=AgentOperatingScenarioTest,AdminAgentCapabilityBoundaryTest", :reason: "Regression scenario `agent-exact-target-fail-closed` covers Admin-agent mutating prompts must fail closed until exact target resolution and required confirmation exist.", :confidence: "high", :covers: ["apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AgentOperatingScenarioTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentCapabilityBoundaryTest.java"], :uncovered: []}`
- `{:command: "cd apps/themuffinman && ./mvnw test -Dtest=QuestUseCaseContractTest,QuestWorkflowScenarioTest", :reason: "Regression scenario `workmarket-quest-lifecycle` covers Quest create, update, delete, start, complete, and term-change flows must resolve actors, validate state, persist changes, and publish expected notifications.", :confidence: "high", :covers: ["apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java"], :uncovered: []}`
- `{:command: "cd apps/themuffinman && ./mvnw test -Dtest=QuestApplicationUseCaseContractTest,QuestApplicationServiceTest", :reason: "Regression scenario `workmarket-application-lifecycle` covers Application apply, applicant edit, withdraw, owner approve, owner decline, and admin mutation flows must enforce ownership, status, pricing, and notification rules.", :confidence: "high", :covers: ["apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java"], :uncovered: []}`
- `{:command: "cd apps/themuffinman && ./mvnw test -Dtest=DashboardServiceTest", :reason: "Regression scenario `workmarket-dashboard-read-model` covers Dashboard sections, applicant actions, open-work groups, and notification destinations must stay backend-prepared and role-aware.", :confidence: "medium", :covers: ["apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java"], :uncovered: []}`
- `{:command: "cd apps/themuffinman && ./mvnw test -Dtest=CircleServiceTest,CircleRelationServiceTest,ChatServiceTest", :reason: "Regression scenario `social-chat-relation-access` covers Circle membership, accepted relation state, blocking, discovery, and chat eligibility must not leak pending or stale contacts.", :confidence: "high", :covers: ["apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleServiceTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleRelationServiceTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/chat/service/ChatServiceTest.java"], :uncovered: []}`
- `{:command: "cd apps/themuffinman && ./mvnw test -Dtest=LocationAccessPolicyServiceTest,LocationLookupServiceTest", :reason: "Regression scenario `location-exact-visibility` covers Exact location access must allow owners, respect circle and explicit-user scopes, and avoid provider calls when lookup is disabled.", :confidence: "high", :covers: ["apps/themuffinman/src/test/java/com/themuffinman/app/location/service/LocationAccessPolicyServiceTest.java", "apps/themuffinman/src/test/java/com/themuffinman/app/location/service/LocationLookupServiceTest.java"], :uncovered: []}`

## `residual_risk`

- `Multiple domains changed; targeted commands do not prove cross-domain integration.`
- `Backend behavior changed; full `cd apps/themuffinman && ./mvnw test` may still be required before closeout.`
- `Frontend behavior changed; targeted type/build checks do not verify manual UX behavior.`

## `notes`

- `This is a targeted recommendation report, not a replacement for full validation.`
- `Use full `cd apps/themuffinman && ./mvnw test` for high-risk backend behavior, schema, or broad cross-domain changes.`

