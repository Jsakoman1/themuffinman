# Codex Local Audits

Use local audits before broad repo discovery when the question can be answered by a generated inventory or summary.

## Recommended Order

1. `make audit-change-impact-preflight`
2. `make audit-endpoint-callsite-linker` or `make audit-frontend-route-surfaces`
3. choose a focused audit from the target index

`audit-change-impact-preflight` includes report-only `scope_guardrails` that warn when one changeset mixes multiple product domains, runtime code with tooling or infrastructure, broad generated-report churn, or generated reports that were not predicted by the changed source files.

## Context-First Session Start

Before broad repository searches, read the compact local context in this order:

1. `docs/generated/local-tooling/diff-summary.md` for the current changed-file shape.
2. `docs/generated/local-tooling/audit-summary-index.md` to choose the smallest relevant generated report. Treat it as a
   routing aid, not as the source of truth for current behavior.
3. `make context-pack topic=<topic>` when the task has a clear feature, domain, or changed-file focus.
4. `docs/generated/local-tooling/repo-map-summary.md` or `symbol-index-summary.md` only when the first three sources do not identify the needed files.

## Available Targets

- `api-contract-snapshot`
- `architecture-decision-index`
- `audit-agent-model-feature-coverage`
- `audit-agent-safety`
- `audit-api-contract-drift`
- `audit-architecture-drift`
- `audit-async-mutation-flow`
- `audit-automation-readiness-gap`
- `audit-backend-dead-code`
- `audit-backend-dependency-graph`
- `audit-change-impact-preflight`
- `audit-config-sprawl`
- `audit-contract-test-gaps`
- `audit-dead-code`
- `audit-delta-report`
- `audit-doc-canonical-phrases`
- `audit-doc-coverage-gap`
- `audit-doc-staleness-scoring`
- `audit-doc-sync-duplicates`
- `audit-doc-sync-preflight`
- `audit-doc-sync-required-surfaces`
- `audit-doc-template-coverage`
- `audit-docs-as-tests`
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
- `audit-generated-commit-scope`
- `audit-local-tooling`
- `audit-local-tooling-incremental`
- `audit-make-target-index`
- `audit-manifest-decision`
- `audit-manual-cleanup-candidate-report`
- `audit-mapper-usage`
- `audit-migration-entity-drift`
- `audit-mutation-safety`
- `audit-naming-consistency`
- `audit-permission-rule-duplication`
- `audit-plan-completion`
- `audit-read-surface-inventory`
- `audit-repository-fetch`
- `audit-rich-text-safety`
- `audit-router`
- `audit-sandbox-data-coverage-pack`
- `audit-sandbox-generation-coverage`
- `audit-state-transition-coverage`
- `audit-style-token-usage`
- `audit-summary-index`
- `audit-test-fixture-duplication`
- `audit-test-gap-recommendations`
- `audit-test-surface-inventory`
- `audit-todo`
- `audit-validation-evidence-quality`
- `audit-validation-memory-drift`
- `autofill-feature-closeout`
- `bootstrap-feature-work`
- `changeset-playbook`
- `changeset-risk`
- `clean-text-noise`
- `closeout-bundle`
- `closeout-report`
- `codebase-capsule`
- `codex-context`
- `codex-context-clean`
- `codex-context-explain`
- `context-pack`
- `diagnose-backend-test`
- `diagnose-frontend-build`
- `diagnose-frontend-type-check`
- `diff-summary`
- `domain-pack`
- `dto-usage-pack`
- `endpoint-contract-packs`
- `enforce-feature-closeout`
- `failure-knowledge-base`
- `fast-check`
- `feature-closeout-audit`
- `generate-agent-artifacts`
- `generate-agent-operating-model`
- `generate-audit-registry-artifacts`
- `generate-frontend-contracts`
- `link-symbol-to-tests`
- `plan-code-map`
- `post-merge-retrospective`
- `post-plan-memory-update`
- `rank-changeset-hotspots`
- `recommend-feature-slices`
- `recommend-targeted-tests`
- `recommend-validation-preset`
- `record-validation`
- `repo-map`
- `resolve-manifest-path`
- `session-handoff`
- `smoke-local-authenticated`
- `smoke-local-dashboard`
- `symbol-index`
- `test-history-summary`
- `validate-frontend-contracts`
- `validation-matrix`
- `validation-memory-closeout-card`
- `workflow-slice-pack`
