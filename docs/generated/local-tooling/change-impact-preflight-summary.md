# Change Impact Preflight

- Generated at: `2026-06-29T12:47:09Z`
- Changed files: `405`
- Unique docs to review: `7`
- Unique tests to consider: `36`
- Generated artifacts to check: `5`
- Scope guardrail warnings: `5`

## Scope Guardrails

- `mixed_product_domains`: `warn` - Changes touch multiple product domains in one scope.
  Evidence: `agent`, `business`, `chat`, `identity`, `location`, `rides`, `social`, `things`, `workmarket`
- `runtime_plus_tooling_or_infrastructure`: `warn` - Runtime code and tooling/infrastructure files changed together.
  Evidence: `.gitignore`, `apps/themuffinman/frontend/README.md`, `apps/themuffinman/frontend/src/components/ui/UiAppShellPage.vue`, `apps/themuffinman/frontend/src/components/ui/UiDashboardPage.vue`, `apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts`, `apps/themuffinman/frontend/src/modules/business/`, `apps/themuffinman/frontend/src/modules/chat/README.md`, `apps/themuffinman/frontend/src/modules/chat/views/`, `Makefile`, `apps/themuffinman/frontend/package.json`, `apps/themuffinman/frontend/scripts/generate-workmarket-contracts.mjs`, `apps/themuffinman/frontend/scripts/validate-admin-agent-ui-scenarios.mjs`, `docs/tooling/codex-local-audits.md`, `docs/tooling/codex-local-audits.yml`, `package.json`, `scripts/audits/audit-architecture-drift.rb`
- `large_generated_report_churn`: `warn` - More than ten generated report files changed; review whether they are task-required.
  Evidence: `docs/generated/README.md`, `docs/generated/agent-endpoint-inventory.json`, `docs/generated/artifact-policy.yaml`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/backend-audit-inventory.json`, `docs/generated/dead-code-audit/backend-unused-summary.md`, `docs/generated/dead-code-audit/backend-unused.json`, `docs/generated/dead-code-audit/dead-code-summary-summary.md`, `docs/generated/dead-code-audit/dead-code-summary.json`, `docs/generated/dead-code-audit/frontend-unused-summary.md`, `docs/generated/dead-code-audit/frontend-unused.json`, `docs/generated/local-tooling/.cache/audit-inputs.json`, `docs/generated/local-tooling/.history/`, `docs/generated/local-tooling/agent-model-feature-coverage-audit-summary.md`, `docs/generated/local-tooling/agent-model-feature-coverage-audit.json`, `docs/generated/local-tooling/api-contract-drift-summary.md`, `docs/generated/local-tooling/api-contract-drift.json`, `docs/generated/local-tooling/api-contract-snapshot-summary.md`, `docs/generated/local-tooling/api-contract-snapshot.json`, `docs/generated/local-tooling/architecture-decision-index-summary.md`
- `unexpected_generated_reports`: `warn` - Generated report changes include files not predicted by changed source files.
  Evidence: `docs/generated/README.md`, `docs/generated/artifact-policy.yaml`, `docs/generated/dead-code-audit/backend-unused-summary.md`, `docs/generated/dead-code-audit/backend-unused.json`, `docs/generated/dead-code-audit/dead-code-summary-summary.md`, `docs/generated/dead-code-audit/dead-code-summary.json`, `docs/generated/dead-code-audit/frontend-unused-summary.md`, `docs/generated/dead-code-audit/frontend-unused.json`, `docs/generated/local-tooling/.cache/audit-inputs.json`, `docs/generated/local-tooling/.history/`, `docs/generated/local-tooling/agent-model-feature-coverage-audit-summary.md`, `docs/generated/local-tooling/agent-model-feature-coverage-audit.json`, `docs/generated/local-tooling/api-contract-drift-summary.md`, `docs/generated/local-tooling/api-contract-drift.json`, `docs/generated/local-tooling/api-contract-snapshot-summary.md`, `docs/generated/local-tooling/api-contract-snapshot.json`, `docs/generated/local-tooling/architecture-decision-index-summary.md`, `docs/generated/local-tooling/architecture-decision-index.json`, `docs/generated/local-tooling/architecture-drift-summary.md`, `docs/generated/local-tooling/architecture-drift.json`
- `source_and_generated_only_review`: `warn` - Source changes and generated artifacts changed together; keep only generated outputs required by the source delta.
  Evidence: `docs/generated/README.md`, `docs/generated/agent-endpoint-inventory.json`, `docs/generated/artifact-policy.yaml`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/backend-audit-inventory.json`, `docs/generated/dead-code-audit/backend-unused-summary.md`, `docs/generated/dead-code-audit/backend-unused.json`, `docs/generated/dead-code-audit/dead-code-summary-summary.md`, `docs/generated/dead-code-audit/dead-code-summary.json`, `docs/generated/dead-code-audit/frontend-unused-summary.md`, `docs/generated/dead-code-audit/frontend-unused.json`, `docs/generated/local-tooling/.cache/audit-inputs.json`, `docs/generated/local-tooling/.history/`, `docs/generated/local-tooling/agent-model-feature-coverage-audit-summary.md`, `docs/generated/local-tooling/agent-model-feature-coverage-audit.json`, `docs/generated/local-tooling/api-contract-drift-summary.md`, `docs/generated/local-tooling/api-contract-drift.json`, `docs/generated/local-tooling/api-contract-snapshot-summary.md`, `docs/generated/local-tooling/api-contract-snapshot.json`, `docs/generated/local-tooling/architecture-decision-index-summary.md`

## `.agents/agent-control-phase-two-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/agent-operating-refactor-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/agent-safety-enforcement-round2-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/agent-safety-upgrade-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/backend-audit-domain-tagging-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/backend-audit-identity-dto-tightening-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/backend-audit-location-dto-tightening-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/backend-audit-manifest-cleanup-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/backend-audit-tiering-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/backend-audit-tightening-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/executor-readiness-program-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/agent-control-phase-two-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/agent-operating-refactor-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/agent-safety-enforcement-round2-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/agent-safety-upgrade-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/backend-audit-domain-tagging-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/backend-audit-identity-dto-tightening-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/backend-audit-location-dto-tightening-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/backend-audit-manifest-cleanup-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/backend-audit-tiering-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/backend-audit-tightening-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/executor-readiness-program-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/feature-manifests/persistent-backlog-system-manifest.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/persistent-backlog-system-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/templates/feature-completion-manifest.template.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/templates/feature-implementation-plan.template.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.gitignore`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `Makefile`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/README.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/package.json`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/scripts/generate-workmarket-contracts.mjs`

- Category: `frontend_script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/scripts/validate-admin-agent-ui-scenarios.mjs`

- Category: `frontend_script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/components/ui/UiDashboardPage.vue`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts`

- Category: `frontend_contract`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts`
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/moduleRegistry.ts`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/modules/social/pages/AdminCirclesPage.vue`

- Category: `frontend_view`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/social/views/CirclesView.vue`

- Category: `frontend_view`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createDashboardSelectors.ts`

- Category: `frontend_composable`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/workmarket/composables/dashboard/createQuestDashboardStateModules.ts`

- Category: `frontend_composable`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/workmarket/composables/quest-detail/useQuestDetailEdit.ts`

- Category: `frontend_composable`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/workmarket/composables/useQuestDetailView.ts`

- Category: `frontend_composable`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminAgentPage.vue`

- Category: `frontend_view`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminApplicationsPage.vue`

- Category: `frontend_view`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminOverviewPage.vue`

- Category: `frontend_view`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminQuestsPage.vue`

- Category: `frontend_view`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/workmarket/pages/AdminUsersPage.vue`

- Category: `frontend_view`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/workmarket/pages/QuestsPage.vue`

- Category: `frontend_view`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/workmarket/views/QuestDetailView.vue`

- Category: `frontend_view`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/router.ts`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/style.css`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundService.java`

- Category: `backend_service`
- Domain: `agent`
- Likely docs: `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/domain-technical.md`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentCapabilityBoundaryTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentGoldenPromptMatrixTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AgentOperatingScenarioTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/LocalAdminAgentPromptTranslator.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/OpenAiAdminAgentClient.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationSettingsService.java`

- Category: `backend_service`
- Domain: `location`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/location-services.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/location/service/LocationAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/location/service/LocationLookupServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/AdminDatabaseMetricsService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/GeoapifyLocationLookupClient.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationAccessPolicyService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationLookupService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/DashboardSectionsDTO.java`

- Category: `backend_dto`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `build`, `npm`, `run`, `type-check`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/repository/QuestApplicationRepository.java`

- Category: `backend_repository`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/repository/QuestRepository.java`

- Category: `backend_repository`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestExecutionPrimitiveService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestStateTransitionService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestUpdateService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestValidationService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestVisibilityService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestWorkflowNotificationService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/UserReviewService.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentCapabilityBoundaryTest.java`

- Category: `backend_service`
- Domain: `agent`
- Likely docs: `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/domain-technical.md`, `docs/change-completion-checklist.md`
- Likely tests: _none_
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentGoldenPromptMatrixTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AgentOperatingScenarioTest.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentGoldenPromptMatrixTest.java`

- Category: `backend_service`
- Domain: `agent`
- Likely docs: `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/domain-technical.md`, `docs/change-completion-checklist.md`
- Likely tests: _none_
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentCapabilityBoundaryTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AgentOperatingScenarioTest.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundServiceTest.java`

- Category: `backend_service`
- Domain: `agent`
- Likely docs: `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/domain-technical.md`, `docs/change-completion-checklist.md`
- Likely tests: _none_
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentCapabilityBoundaryTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentGoldenPromptMatrixTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AgentOperatingScenarioTest.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/test/java/com/themuffinman/app/docs/AgentOperatingModelValidationTest.java`

- Category: `other`
- Domain: `docs`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: _none_
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: _none_
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: _none_
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: _none_
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: _none_
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: _none_
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-improvement-backlog.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model.schema.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model/sections/api.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model/sections/backend_audit_coverage.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model/sections/documentation_coverage.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model/sections/documentation_sync.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model/sections/frontend_contract_generation.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model/sections/intent_safety_catalog.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model/sections/intents.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model/sections/mutating_intent_contracts.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model/sections/policies.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model/sections/request_validation_gate.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model/sections/service_workflow_inventory.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model/sections/source_of_truth.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/business-logic.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/change-completion-checklist.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/codex-local-tooling-todo.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/documentation-sync-policy.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/domain-technical.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/feature-completion-manifest.schema.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/agent-endpoint-inventory.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/automation-read-model-inventory.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/backend-audit-inventory.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/dead-code-audit/backend-unused-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/dead-code-audit/backend-unused.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/dead-code-audit/dead-code-summary-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/dead-code-audit/dead-code-summary.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/dead-code-audit/frontend-unused-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/dead-code-audit/frontend-unused.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/.cache/audit-inputs.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/agent-model-feature-coverage-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/agent-model-feature-coverage-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/api-contract-drift-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/api-contract-drift.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/api-contract-snapshot-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/api-contract-snapshot.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/async-mutation-flow-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/async-mutation-flow-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/audit-documentation-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/audit-documentation.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/audit-registry-artifacts-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/audit-registry-artifacts.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/audit-router-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/audit-router.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/audit-summary-index.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/audit-summary-index.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/automation-readiness-gap-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/automation-readiness-gap-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/backend-dependency-graph-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/backend-dependency-graph.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/change-impact-preflight-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/change-impact-preflight.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/config-sprawl-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/config-sprawl-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/diff-summary.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/diff-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/doc-canonical-phrases-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/doc-canonical-phrases.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/doc-coverage-gap-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/doc-coverage-gap-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/doc-sync-preflight-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/doc-sync-preflight.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/docs-to-code-drift-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/docs-to-code-drift-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/domain-ownership-inventory-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/domain-ownership-inventory.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/dormant-code-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/dormant-code-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/duplicate-logic-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/duplicate-logic-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-callsite-linker-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-callsite-linker.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/admin-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/admin.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/agent-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/agent.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/app_users-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/app_users.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/applications-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/applications.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/auth-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/auth.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/chat-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/chat.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/circles-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/circles.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/dashboard-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/dashboard.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/index-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/index.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/location-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/location.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/news-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/news.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/quests-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/quests.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/error-pattern-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/error-pattern-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/fast-check-report-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/fast-check-report.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/feature-intro-check-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/feature-intro-check.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/file-relation-graph-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/file-relation-graph.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/frontend-route-surface-inventory-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/frontend-route-surface-inventory.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/frontend-stale-surface-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/frontend-stale-surface-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/frontend-state-logic-duplication-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/frontend-state-logic-duplication-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/frontend-usage-graph-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/frontend-usage-graph.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/generated-artifact-freshness-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/generated-artifact-freshness.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/make-target-index-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/make-target-index.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/manual-cleanup-candidate-report-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/manual-cleanup-candidate-report.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/mapper-usage-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/mapper-usage-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/migration-entity-drift-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/migration-entity-drift-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/naming-consistency-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/naming-consistency-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/permission-rule-duplication-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/permission-rule-duplication-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/read-surface-inventory-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/read-surface-inventory.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/repo-map-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/repo-map.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/repository-fetch-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/repository-fetch-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/rich-text-safety-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/rich-text-safety-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/sandbox-data-coverage-pack-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/sandbox-data-coverage-pack.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/sandbox-generation-coverage-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/sandbox-generation-coverage-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/state-transition-coverage-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/state-transition-coverage-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/style-token-usage-audit-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/style-token-usage-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/symbol-index-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/symbol-index.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/test-gap-recommendations-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/test-gap-recommendations.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/test-surface-inventory-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/test-surface-inventory.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/validation-matrix-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/validation-matrix.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/source-of-truth-audit.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/implementation-backlog.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/tooling/codex-local-audits.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/tooling/codex-local-audits.yml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `package.json`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/audit-change-impact-preflight.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/local_tooling_batch_audits.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/local_tooling_extended_tools.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/feature-closeout-audit.sh`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/generate-agent-operating-model.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/generate-source-of-truth-audit.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/local_tooling_common.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/todo-audit.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/local-context-batch-two-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/local-tooling-parallel-followup-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/templates/docs/`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/templates/validation-evidence.template.yaml`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/todo-master-plan.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/todo-plans/`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `.agents/validation-evidence/`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/components/ui/UiAppShellPage.vue`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/modules/business/`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/modules/chat/README.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/modules/chat/views/`

- Category: `frontend_view`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/modules/identity/README.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/modules/rides/`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/modules/social/README.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/modules/things/`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/modules/workmarket/README.md`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/modules/workmarket/api/agentWorkflowGuards.ts`

- Category: `frontend_api`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts`
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `type-check`, `npm`, `run`, `build`

## `apps/themuffinman/frontend/src/styles/business.css`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/styles/chat-module.css`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/styles/rides.css`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/frontend/src/styles/things.css`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/agent/README.md`

- Category: `other`
- Domain: `agent`
- Likely docs: `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/domain-technical.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentCapabilityBoundaryTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentGoldenPromptMatrixTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AgentOperatingScenarioTest.java`
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/agent/sandbox/`

- Category: `other`
- Domain: `agent`
- Likely docs: `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/domain-technical.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/agent/sandbox/`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentCapabilityBoundaryTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentGoldenPromptMatrixTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/agent/service/AgentOperatingScenarioTest.java`
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/business/`

- Category: `other`
- Domain: `business`
- Likely docs: _none_
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/business/`, `apps/themuffinman/src/test/java/com/themuffinman/app/business/service/BusinessProfileServiceTest.java`
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/chat/README.md`

- Category: `other`
- Domain: `chat`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/chat/service/ChatServiceTest.java`
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/common/README.md`

- Category: `other`
- Domain: `common`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/common/concepts/`

- Category: `other`
- Domain: `common`
- Likely docs: _none_
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/common/concepts/`
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/common/event/`

- Category: `other`
- Domain: `common`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/identity/README.md`

- Category: `other`
- Domain: `identity`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AdminUserDetailServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AppUserServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/AuthServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/identity/service/UserProfileViewServiceTest.java`
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/location/README.md`

- Category: `other`
- Domain: `location`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/location-services.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/location/service/LocationAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/location/service/LocationLookupServiceTest.java`
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationAccessPolicyService.java`

- Category: `backend_service`
- Domain: `location`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/location-services.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/location/service/LocationAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/location/service/LocationLookupServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/AdminDatabaseMetricsService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/GeoapifyLocationLookupClient.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationLookupService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/location/service/LocationSettingsService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/rides/`

- Category: `other`
- Domain: `rides`
- Likely docs: _none_
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/rides/`, `apps/themuffinman/src/test/java/com/themuffinman/app/rides/service/RideOfferServiceTest.java`
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/social/README.md`

- Category: `other`
- Domain: `social`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleDiscoveryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleMembershipServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleRelationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleServiceTest.java`
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/things/`

- Category: `other`
- Domain: `things`
- Likely docs: _none_
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/things/`, `apps/themuffinman/src/test/java/com/themuffinman/app/things/service/ThingSharingServiceTest.java`
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/README.md`

- Category: `other`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/DashboardNavigationItemDTO.java`

- Category: `backend_dto`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `build`, `npm`, `run`, `type-check`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/DashboardNavigationSectionDTO.java`

- Category: `backend_dto`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `npm`, `run`, `build`, `npm`, `run`, `type-check`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/event/`

- Category: `other`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/event/`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyService.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeclineApplicationUseCase.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationViewAssembler.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestApplicationWorkflowSupport.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/UpdateMyApplicationUseCase.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/WithdrawMyApplicationUseCase.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestVisibilityServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/WorkmarketOptionsServiceTest.java`
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApplyForQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ApproveApplicationUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CompleteQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/ConfirmQuestTermChangeUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/CreateQuestUseCase.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardSectionsFactory.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DashboardService.java`, `apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/DeleteQuestUseCase.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/main/resources/db/migration/V31__create_business_profile_table.sql`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/resources/db/migration/V32__create_thing_sharing_tables.sql`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/main/resources/db/migration/V33__create_ride_offer_tables.sql`

- Category: `other`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/test/java/com/themuffinman/app/agent/sandbox/`

- Category: `other`
- Domain: `agent`
- Likely docs: `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/domain-technical.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/test/java/com/themuffinman/app/business/`

- Category: `other`
- Domain: `business`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/test/java/com/themuffinman/app/common/concepts/`

- Category: `other`
- Domain: `common`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/test/java/com/themuffinman/app/config/ServiceLayeringConventionTest.java`

- Category: `other`
- Domain: `config`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/test/java/com/themuffinman/app/docs/RepositoryFetchProfileContractTest.java`

- Category: `other`
- Domain: `docs`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/test/java/com/themuffinman/app/docs/WorkflowStateMachineCatalogTest.java`

- Category: `other`
- Domain: `docs`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/test/java/com/themuffinman/app/location/service/LocationAccessPolicyServiceTest.java`

- Category: `backend_service`
- Domain: `location`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/location-services.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: _none_
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/test/java/com/themuffinman/app/location/service/LocationLookupServiceTest.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/test/java/com/themuffinman/app/rides/`

- Category: `other`
- Domain: `rides`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/test/java/com/themuffinman/app/testing/`

- Category: `other`
- Domain: `testing`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/test/java/com/themuffinman/app/things/`

- Category: `other`
- Domain: `things`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/event/`

- Category: `other`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestAccessPolicyServiceTest.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: _none_
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationUseCaseContractTest.java`

- Category: `backend_service`
- Domain: `workmarket`
- Likely docs: `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, `docs/change-completion-checklist.md`
- Likely tests: _none_
- Generated artifacts: `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/DashboardServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestApplicationServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestNewsServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestQueryServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestServiceTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestUseCaseContractTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/QuestWorkflowScenarioTest.java`, `apps/themuffinman/src/test/java/com/themuffinman/app/workmarket/service/UserReviewServiceTest.java`
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/agent-operating-model/sections/documentation_ownership.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/cross-domain-glossary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/docs-as-contract-slices.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/docs-as-contract-slices.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/example-scenario-library.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/README.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/artifact-policy.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/.history/`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/architecture-decision-index-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/architecture-decision-index.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/architecture-drift-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/architecture-drift.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/audit-deltas/`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/changeset-playbook-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/changeset-playbook.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/changeset-risk-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/changeset-risk.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/closeout-enforcement/`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/codebase-capsule.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/codebase-capsule.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/contract-test-gaps-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/contract-test-gaps.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/doc-staleness-scoring-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/doc-staleness-scoring.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/doc-sync-duplicates-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/doc-sync-duplicates.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/doc-template-coverage-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/doc-template-coverage.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/docs-as-tests-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/docs-as-tests.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/domain-packs/`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/dto-usage-packs/`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/business-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/business.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/rides-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/rides.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/things-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/endpoint-contract-packs/things.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/failure-knowledge-base-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/failure-knowledge-base.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/feature-slices/`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/generated-commit-scope-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/generated-commit-scope.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/hotspots-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/hotspots.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/manifest-decision-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/manifest-decision.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/manifest-path-resolution-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/manifest-path-resolution.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/mutation-safety-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/mutation-safety.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/plan-code-maps/`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/plan-completion/`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/symbol-test-links/`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/targeted-tests-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/targeted-tests.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/test-fixture-duplication-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/test-fixture-duplication.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/test-history-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/test-history.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/validation-evidence/`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/validation-preset-summary.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/validation-preset.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/generated/local-tooling/workflow-slices/`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/regression-scenario-catalog.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/regression-scenario-catalog.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/validation-evidence.schema.json`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/workflow-state-machines.md`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `docs/workflow-state-machines.yaml`

- Category: `docs`
- Domain: `unknown`
- Likely docs: _none_
- Likely tests: _none_
- Generated artifacts: `docs/generated/agent-endpoint-inventory.json`, `docs/generated/automation-read-model-inventory.json`, `docs/generated/source-of-truth-audit.json`, `docs/generated/backend-audit-inventory.json`
- Sibling read surfaces: _none_
- Suggested commands: `./mvnw`, `test`, `make`, `generate-agent-artifacts`

## `scripts/audits/audit-architecture-drift.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/audit-contract-test-gaps.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/audit-delta-report.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/audit-doc-staleness-scoring.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/audit-doc-sync-duplicates.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/audit-doc-template-coverage.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/audit-docs-as-tests.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/audit-generated-commit-scope.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/audit-manifest-decision.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/audit-mutation-safety.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/audit-plan-completion.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/audit-test-fixture-duplication.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/audit-validation-evidence-quality.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/enforce-feature-closeout.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/generate-architecture-decision-index.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/generate-changeset-playbook.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/generate-closeout-report.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/generate-codebase-capsule.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/generate-domain-pack.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/generate-dto-usage-pack.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/generate-plan-code-map.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/generate-post-merge-retrospective.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/generate-test-history-summary.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/generate-workflow-slice-pack.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/link-symbol-to-tests.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/rank-changeset-hotspots.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/recommend-feature-slices.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/recommend-targeted-tests.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/recommend-validation-preset.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/record-validation-evidence.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/resolve-manifest-path.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/score-changeset-risk.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_

## `scripts/audits/update-failure-knowledge-base.rb`

- Category: `script`
- Domain: `unknown`
- Likely docs: `docs/codex-local-tooling-todo.md`
- Likely tests: _none_
- Generated artifacts: _none_
- Sibling read surfaces: _none_
- Suggested commands: _none_
