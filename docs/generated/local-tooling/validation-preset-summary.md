# Validation Preset

- Preset: `manifest-required`
## `reasons`

- `Manifest decision is `required`.`
- `Changed files span 10 categories.`
- `Changed files span 8 domains.`
- `Preset `manifest-required` keeps validation selection deterministic for 178 files.`

## `commands`

- `make recommend-targeted-tests`
- `make closeout-bundle manifest=.agents/feature-manifests/<feature-id>-manifest.yaml`
- `make feature-closeout-audit manifest=.agents/feature-manifests/<feature-id>-manifest.yaml`
- `cd apps/themuffinman && ./mvnw test -Dtest=SandboxGenerationPlannerTest,AdminAgentCapabilityBoundaryTest,AdminAgentGoldenPromptMatrixTest,AdminAgentPlaygroundServiceTest,AgentOperatingScenarioTest,BusinessProfileServiceTest,ChatServiceTest,CoreConceptsTest`
- `npm --prefix apps/themuffinman/frontend run type-check`
- `npm --prefix apps/themuffinman/frontend run build`
- `make audit-documentation`
- `make audit-doc-canonical-phrases`
- `make audit-generated-artifact-freshness`
- `make audit-generated-commit-scope`
- `make audit-agent-safety`

## `supporting_reports`

- `docs/generated/local-tooling/targeted-tests-summary.md`
- `docs/generated/local-tooling/audit-router-summary.md`
- `docs/generated/local-tooling/closeout-bundle-summary.md`

- Manifest Decision: `required`
- `manifest_resolution`: `7` entries
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

## `domains`

- `agent`
- `chat`
- `common`
- `identity`
- `location`
- `shared`
- `social`
- `workmarket`

- Changed File Count: `178`
- Generated At: `2026-06-29T12:47:32Z`
- Original File Count: `373`
- Filtered File Count: `195`
- Excluded File Count: `195`
## `excluded_files_sample`

- `{:path: ".agents/agent-control-phase-two-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/agent-operating-refactor-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/agent-safety-enforcement-round2-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/agent-safety-upgrade-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/backend-audit-domain-tagging-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/backend-audit-identity-dto-tightening-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/backend-audit-location-dto-tightening-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/backend-audit-manifest-cleanup-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/backend-audit-tiering-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/backend-audit-tightening-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/executor-readiness-program-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: ".agents/persistent-backlog-system-plan.md", :excluded: true, :reasons: ["agent_transient"]}`
- `{:path: "apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/agent-endpoint-inventory.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/automation-read-model-inventory.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/backend-audit-inventory.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/dead-code-audit/backend-unused-summary.md", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/dead-code-audit/backend-unused.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/dead-code-audit/dead-code-summary-summary.md", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/dead-code-audit/dead-code-summary.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/dead-code-audit/frontend-unused-summary.md", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/dead-code-audit/frontend-unused.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/.cache/audit-inputs.json", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/agent-model-feature-coverage-audit-summary.md", :excluded: true, :reasons: ["generated"]}`
- `{:path: "docs/generated/local-tooling/agent-model-feature-coverage-audit.json", :excluded: true, :reasons: ["generated"]}`

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

