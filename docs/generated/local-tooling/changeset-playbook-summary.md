# Changeset Playbook

- Generated At: `2026-06-29T12:47:29Z`
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

- Changed File Count: `178`
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

## `files`

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

- `manifest_decision`: `8` entries
- `manifest_resolution`: `7` entries
- `validation_preset`: `9` entries
## `doc_targets`

- `docs/agent-operating-model.md`
- `docs/agent-operating-model.yaml`
- `docs/business-logic.md`
- `docs/codex-local-tooling-todo.md`
- `docs/domain-technical.md`
- `docs/location-services.md`

## `source_reports`

- `docs/generated/local-tooling/diff-summary.md`
- `docs/generated/local-tooling/audit-router-summary.md`
- `docs/generated/local-tooling/doc-sync-preflight-summary.md`
- `docs/generated/local-tooling/manifest-decision-summary.md`
- `docs/generated/local-tooling/manifest-path-resolution-summary.md`
- `docs/generated/local-tooling/validation-preset-summary.md`

## `ordered_actions`

- `{:step: 1, :kind: "read", :title: "Review changeset shape", :commands: ["make diff-summary"], :outputs: ["docs/generated/local-tooling/diff-summary.md"], :purpose: "See domain/category grouping before opening files."}`
- `{:step: 2, :kind: "decide", :title: "Confirm manifest path", :commands: ["make audit-manifest-decision", "make resolve-manifest-path"], :outputs: ["docs/generated/local-tooling/manifest-decision-summary.md", "docs/generated/local-tooling/manifest-path-resolution-summary.md"], :purpose: "Manifest is required before final closeout.", :decision: "required", :resolved_manifest: nil}`
- `{:step: 3, :kind: "audit", :title: "Run focused audits", :commands: ["make audit-read-surface-inventory", "make audit-repository-fetch", "make audit-mapper-usage", "make audit-api-contract-drift", "make audit-endpoint-callsite-linker", "make endpoint-contract-packs", "make audit-frontend-route-surfaces", "make audit-async-mutation-flow", "make audit-frontend-usage-graph", "make audit-doc-sync-preflight", "make audit-documentation", "make audit-doc-canonical-phrases"], :outputs: ["docs/generated/local-tooling/audit-router-summary.md", "docs/generated/local-tooling/doc-sync-preflight-summary.md"], :purpose: "Pick the smallest report set that matches the current files."}`
- `{:step: 4, :kind: "update", :title: "Update living docs and agent artifacts", :files: ["docs/agent-operating-model.md", "docs/agent-operating-model.yaml", "docs/domain-technical.md", "docs/business-logic.md", "docs/location-services.md", "docs/codex-local-tooling-todo.md"], :purpose: "Keep business, technical, and agent-safety docs synchronized with the changeset."}`
- `{:step: 5, :kind: "validate", :title: "Run preset validation", :commands: ["make recommend-targeted-tests", "make closeout-bundle manifest=.agents/feature-manifests/<feature-id>-manifest.yaml", "make feature-closeout-audit manifest=.agents/feature-manifests/<feature-id>-manifest.yaml", "cd apps/themuffinman && ./mvnw test -Dtest=SandboxGenerationPlannerTest,AdminAgentCapabilityBoundaryTest,AdminAgentGoldenPromptMatrixTest,AdminAgentPlaygroundServiceTest,AgentOperatingScenarioTest,BusinessProfileServiceTest,ChatServiceTest,CoreConceptsTest", "npm --prefix apps/themuffinman/frontend run type-check", "npm --prefix apps/themuffinman/frontend run build", "make audit-documentation", "make audit-doc-canonical-phrases", "make audit-generated-artifact-freshness", "make audit-generated-commit-scope", "make audit-agent-safety"], :outputs: ["docs/generated/local-tooling/targeted-tests-summary.md", "docs/generated/local-tooling/audit-router-summary.md", "docs/generated/local-tooling/closeout-bundle-summary.md"], :purpose: "Use one preset instead of manually assembling commands."}`
- `{:step: 6, :kind: "closeout", :title: "Use resolved manifest path for final closeout", :commands: ["make closeout-bundle manifest=.agents/feature-manifests/<feature-id>-manifest.yaml"], :purpose: "Resolver could not pick one manifest deterministically; replace the placeholder after review."}`

