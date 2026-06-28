# Codex Local Audits

Use local audits before broad repo discovery when the question can be answered by a generated inventory or summary.

## Recommended Order

1. `make audit-change-impact-preflight`
2. `make audit-endpoint-callsite-linker` or `make audit-frontend-route-surfaces`
3. choose a focused audit from the target index

## Available Targets

- `api-contract-snapshot`
- `audit-agent-model-feature-coverage`
- `audit-agent-safety`
- `audit-api-contract-drift`
- `audit-async-mutation-flow`
- `audit-automation-readiness-gap`
- `audit-backend-dead-code`
- `audit-backend-dependency-graph`
- `audit-change-impact-preflight`
- `audit-config-sprawl`
- `audit-dead-code`
- `audit-doc-canonical-phrases`
- `audit-doc-coverage-gap`
- `audit-doc-sync-preflight`
- `audit-docs-to-code-drift`
- `audit-documentation`
- `audit-domain-ownership-inventory`
- `audit-dormant-code`
- `audit-duplicate-logic`
- `audit-endpoint-callsite-linker`
- `audit-error-pattern`
- `audit-feature-intro-check`
- `audit-file-relation-graph`
- `audit-frontend-dead-code`
- `audit-frontend-route-surfaces`
- `audit-frontend-stale-surfaces`
- `audit-frontend-state-logic-duplication`
- `audit-frontend-usage-graph`
- `audit-generated-artifact-freshness`
- `audit-local-tooling`
- `audit-local-tooling-incremental`
- `audit-make-target-index`
- `audit-manual-cleanup-candidate-report`
- `audit-mapper-usage`
- `audit-migration-entity-drift`
- `audit-naming-consistency`
- `audit-permission-rule-duplication`
- `audit-read-surface-inventory`
- `audit-repository-fetch`
- `audit-rich-text-safety`
- `audit-router`
- `audit-sandbox-data-coverage-pack`
- `audit-sandbox-generation-coverage`
- `audit-state-transition-coverage`
- `audit-style-token-usage`
- `audit-summary-index`
- `audit-test-gap-recommendations`
- `audit-test-surface-inventory`
- `audit-todo`
- `bootstrap-feature-work`
- `closeout-bundle`
- `context-pack`
- `diagnose-backend-test`
- `diagnose-frontend-build`
- `diagnose-frontend-type-check`
- `diff-summary`
- `endpoint-contract-packs`
- `fast-check`
- `feature-closeout-audit`
- `generate-agent-artifacts`
- `generate-agent-operating-model`
- `generate-audit-registry-artifacts`
- `repo-map`
- `session-handoff`
- `smoke-local-authenticated`
- `smoke-local-dashboard`
- `symbol-index`
- `validation-matrix`
