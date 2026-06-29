# Audit Router

- Generated At: `2026-06-29T12:04:50Z`
- Original File Count: `383`
- Filtered File Count: `193`
- Excluded File Count: `193`
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

## `recommended_audits`

- `make audit-read-surface-inventory`
- `make audit-repository-fetch`
- `make audit-mapper-usage`
- `make audit-api-contract-drift`
- `make audit-endpoint-callsite-linker`
- `make endpoint-contract-packs`
- `make audit-frontend-route-surfaces`
- `make audit-async-mutation-flow`
- `make audit-frontend-usage-graph`
- `make audit-doc-sync-preflight`
- `make audit-documentation`
- `make audit-doc-canonical-phrases`
- `make audit-test-gap-recommendations`
- `make audit-summary-index`

## `recommended_commands`

- `./mvnw test`
- `npm run type-check`
- `npm run build`
- `make generate-agent-artifacts`
- `make audit-repository-fetch`
- `make audit-read-surface-inventory`
- `make audit-documentation`
- `make audit-doc-canonical-phrases`
- `make audit-api-contract-drift`
- `make audit-async-mutation-flow`
- `make audit-frontend-route-surfaces`
- `ruby -c <script>`
- `make audit-summary-index`

## `notes`


