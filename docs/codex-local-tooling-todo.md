# Codex Local Tooling TODO

This file collects deferred local scripts, inventories, generated reports, and workstation-side helpers that can reduce Codex token usage and move repetitive repo inspection onto the local machine.

Use it for tooling that should produce compact outputs Codex can consume instead of repeatedly rediscovering the same project structure, drift, or dead code patterns.

## Goals

- Reduce token usage spent on broad repo discovery.
- Shift repetitive static inspection to deterministic local scripts.
- Give Codex compact, reviewable summaries instead of raw full-project scans.
- Keep future helper tooling visible in one place instead of scattering ideas across temporary notes.

## Available Local Audits

- `CODEX-LOCAL-CHANGE-IMPACT-PREFLIGHT`
  Entrypoints:
  - `ruby scripts/audits/audit-change-impact-preflight.rb [files...]`
  - `make audit-change-impact-preflight`
  Outputs:
  - `docs/generated/local-tooling/change-impact-preflight.json`
  - `docs/generated/local-tooling/change-impact-preflight-summary.md`

- `CODEX-LOCAL-READ-SURFACE-INVENTORY`
  Entrypoints:
  - `ruby scripts/audits/audit-read-surface-inventory.rb`
  - `make audit-read-surface-inventory`
  Outputs:
  - `docs/generated/local-tooling/read-surface-inventory.json`
  - `docs/generated/local-tooling/read-surface-inventory-summary.md`

- `CODEX-LOCAL-MAPPER-USAGE-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-mapper-usage.rb`
  - `make audit-mapper-usage`
  Outputs:
  - `docs/generated/local-tooling/mapper-usage-audit.json`
  - `docs/generated/local-tooling/mapper-usage-audit-summary.md`

- `CODEX-LOCAL-GENERATED-ARTIFACT-FRESHNESS-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-generated-artifact-freshness.rb`
  - `make audit-generated-artifact-freshness`
  Outputs:
  - `docs/generated/local-tooling/generated-artifact-freshness.json`
  - `docs/generated/local-tooling/generated-artifact-freshness-summary.md`

- `CODEX-LOCAL-API-CONTRACT-DRIFT-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-api-contract-drift.rb`
  - `make audit-api-contract-drift`
  Outputs:
  - `docs/generated/local-tooling/api-contract-drift.json`
  - `docs/generated/local-tooling/api-contract-drift-summary.md`

- `CODEX-LOCAL-REPOSITORY-FETCH-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-repository-fetch.rb`
  - `make audit-repository-fetch`
  Outputs:
  - `docs/generated/local-tooling/repository-fetch-audit.json`
  - `docs/generated/local-tooling/repository-fetch-audit-summary.md`

- `CODEX-LOCAL-ENDPOINT-CALLSITE-LINKER`
  Entrypoints:
  - `ruby scripts/audits/audit-endpoint-callsite-linker.rb`
  - `make audit-endpoint-callsite-linker`
  Outputs:
  - `docs/generated/local-tooling/endpoint-callsite-linker.json`
  - `docs/generated/local-tooling/endpoint-callsite-linker-summary.md`

- `CODEX-LOCAL-FRONTEND-ROUTE-SURFACE-INVENTORY`
  Entrypoints:
  - `ruby scripts/audits/audit-frontend-route-surfaces.rb`
  - `make audit-frontend-route-surfaces`
  Outputs:
  - `docs/generated/local-tooling/frontend-route-surface-inventory.json`
  - `docs/generated/local-tooling/frontend-route-surface-inventory-summary.md`

- `CODEX-LOCAL-FRONTEND-STALE-SURFACE-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-frontend-stale-surfaces.rb`
  - `make audit-frontend-stale-surfaces`
  Outputs:
  - `docs/generated/local-tooling/frontend-stale-surface-audit.json`
  - `docs/generated/local-tooling/frontend-stale-surface-audit-summary.md`

- `CODEX-LOCAL-FRONTEND-STATE-LOGIC-DUPLICATION-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-frontend-state-logic-duplication.rb`
  - `make audit-frontend-state-logic-duplication`
  Outputs:
  - `docs/generated/local-tooling/frontend-state-logic-duplication-audit.json`
  - `docs/generated/local-tooling/frontend-state-logic-duplication-audit-summary.md`

- `CODEX-LOCAL-DUPLICATE-LOGIC-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-duplicate-logic.rb`
  - `make audit-duplicate-logic`
  Outputs:
  - `docs/generated/local-tooling/duplicate-logic-audit.json`
  - `docs/generated/local-tooling/duplicate-logic-audit-summary.md`

- `CODEX-LOCAL-PERMISSION-RULE-DUPLICATION-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-permission-rule-duplication.rb`
  - `make audit-permission-rule-duplication`
  Outputs:
  - `docs/generated/local-tooling/permission-rule-duplication-audit.json`
  - `docs/generated/local-tooling/permission-rule-duplication-audit-summary.md`

- `CODEX-LOCAL-FRONTEND-DEAD-CODE-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-frontend-dead-code.rb`
  - `make audit-frontend-dead-code`
  Outputs:
  - `docs/generated/dead-code-audit/frontend-unused.json`
  - `docs/generated/dead-code-audit/frontend-unused-summary.md`

- `CODEX-LOCAL-BACKEND-DEAD-CODE-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-backend-dead-code.rb`
  - `make audit-backend-dead-code`
  Outputs:
  - `docs/generated/dead-code-audit/backend-unused.json`
  - `docs/generated/dead-code-audit/backend-unused-summary.md`

- `CODEX-LOCAL-AGGREGATE-DEAD-CODE-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-dead-code.rb`
  - `make audit-dead-code`
  Outputs:
  - `docs/generated/dead-code-audit/dead-code-summary.json`
  - `docs/generated/dead-code-audit/dead-code-summary-summary.md`

- `CODEX-LOCAL-STATE-TRANSITION-COVERAGE-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-state-transition-coverage.rb`
  - `make audit-state-transition-coverage`
  Outputs:
  - `docs/generated/local-tooling/state-transition-coverage-audit.json`
  - `docs/generated/local-tooling/state-transition-coverage-audit-summary.md`

- `CODEX-LOCAL-DOCS-TO-CODE-DRIFT-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-docs-to-code-drift.rb`
  - `make audit-docs-to-code-drift`
  Outputs:
  - `docs/generated/local-tooling/docs-to-code-drift-audit.json`
  - `docs/generated/local-tooling/docs-to-code-drift-audit-summary.md`

- `CODEX-LOCAL-DOC-COVERAGE-GAP-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-doc-coverage-gap.rb`
  - `make audit-doc-coverage-gap`
  Outputs:
  - `docs/generated/local-tooling/doc-coverage-gap-audit.json`
  - `docs/generated/local-tooling/doc-coverage-gap-audit-summary.md`

- `CODEX-LOCAL-AUTOMATION-READINESS-GAP-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-automation-readiness-gap.rb`
  - `make audit-automation-readiness-gap`
  Outputs:
  - `docs/generated/local-tooling/automation-readiness-gap-audit.json`
  - `docs/generated/local-tooling/automation-readiness-gap-audit-summary.md`

- `CODEX-LOCAL-AGENT-MODEL-FEATURE-COVERAGE-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-agent-model-feature-coverage.rb`
  - `make audit-agent-model-feature-coverage`
  Outputs:
  - `docs/generated/local-tooling/agent-model-feature-coverage-audit.json`
  - `docs/generated/local-tooling/agent-model-feature-coverage-audit-summary.md`

- `CODEX-LOCAL-SANDBOX-GENERATION-COVERAGE-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-sandbox-generation-coverage.rb`
  - `make audit-sandbox-generation-coverage`
  Outputs:
  - `docs/generated/local-tooling/sandbox-generation-coverage-audit.json`
  - `docs/generated/local-tooling/sandbox-generation-coverage-audit-summary.md`

- `CODEX-LOCAL-DOMAIN-OWNERSHIP-INVENTORY`
  Entrypoints:
  - `ruby scripts/audits/audit-domain-ownership-inventory.rb`
  - `make audit-domain-ownership-inventory`
  Outputs:
  - `docs/generated/local-tooling/domain-ownership-inventory.json`
  - `docs/generated/local-tooling/domain-ownership-inventory-summary.md`

- `CODEX-LOCAL-CONFIG-SPRAWL-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-config-sprawl.rb`
  - `make audit-config-sprawl`
  Outputs:
  - `docs/generated/local-tooling/config-sprawl-audit.json`
  - `docs/generated/local-tooling/config-sprawl-audit-summary.md`

- `CODEX-LOCAL-NAMING-CONSISTENCY-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-naming-consistency.rb`
  - `make audit-naming-consistency`
  Outputs:
  - `docs/generated/local-tooling/naming-consistency-audit.json`
  - `docs/generated/local-tooling/naming-consistency-audit-summary.md`

- `CODEX-LOCAL-DORMANT-CODE-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-dormant-code.rb`
  - `make audit-dormant-code`
  Outputs:
  - `docs/generated/local-tooling/dormant-code-audit.json`
  - `docs/generated/local-tooling/dormant-code-audit-summary.md`

- `CODEX-LOCAL-MANUAL-CLEANUP-CANDIDATE-REPORT`
  Entrypoints:
  - `ruby scripts/audits/audit-manual-cleanup-candidate-report.rb`
  - `make audit-manual-cleanup-candidate-report`
  Outputs:
  - `docs/generated/local-tooling/manual-cleanup-candidate-report.json`
  - `docs/generated/local-tooling/manual-cleanup-candidate-report-summary.md`

- `CODEX-LOCAL-FILE-RELATION-GRAPH`
  Entrypoints:
  - `ruby scripts/audits/audit-file-relation-graph.rb`
  - `make audit-file-relation-graph`
  Outputs:
  - `docs/generated/local-tooling/file-relation-graph.json`
  - `docs/generated/local-tooling/file-relation-graph-summary.md`

- `CODEX-LOCAL-TEST-SURFACE-INVENTORY`
  Entrypoints:
  - `ruby scripts/audits/audit-test-surface-inventory.rb`
  - `make audit-test-surface-inventory`
  Outputs:
  - `docs/generated/local-tooling/test-surface-inventory.json`
  - `docs/generated/local-tooling/test-surface-inventory-summary.md`

- `CODEX-LOCAL-ERROR-PATTERN-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-error-pattern.rb`
  - `make audit-error-pattern`
  Outputs:
  - `docs/generated/local-tooling/error-pattern-audit.json`
  - `docs/generated/local-tooling/error-pattern-audit-summary.md`

- `CODEX-LOCAL-RICH-TEXT-SAFETY-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-rich-text-safety.rb`
  - `make audit-rich-text-safety`
  Outputs:
  - `docs/generated/local-tooling/rich-text-safety-audit.json`
  - `docs/generated/local-tooling/rich-text-safety-audit-summary.md`

- `CODEX-LOCAL-ASYNC-MUTATION-FLOW-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-async-mutation-flow.rb`
  - `make audit-async-mutation-flow`
  Outputs:
  - `docs/generated/local-tooling/async-mutation-flow-audit.json`
  - `docs/generated/local-tooling/async-mutation-flow-audit-summary.md`

- `CODEX-LOCAL-STYLE-TOKEN-USAGE-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-style-token-usage.rb`
  - `make audit-style-token-usage`
  Outputs:
  - `docs/generated/local-tooling/style-token-usage-audit.json`
  - `docs/generated/local-tooling/style-token-usage-audit-summary.md`

- `CODEX-LOCAL-FEATURE-INTRO-CHECK`
  Entrypoints:
  - `ruby scripts/audits/audit-feature-intro-check.rb`
  - `make audit-feature-intro-check`
  Outputs:
  - `docs/generated/local-tooling/feature-intro-check.json`
  - `docs/generated/local-tooling/feature-intro-check-summary.md`

- `CODEX-LOCAL-AUDIT-DOCUMENTATION`
  Entrypoints:
  - `ruby scripts/audits/audit-documentation.rb`
  - `make audit-documentation`
  Outputs:
  - `docs/generated/local-tooling/audit-documentation.json`
  - `docs/generated/local-tooling/audit-documentation-summary.md`
  - `docs/tooling/codex-local-audits.md`

- `CODEX-LOCAL-MAKE-TARGET-INDEX`
  Entrypoints:
  - `ruby scripts/audits/audit-make-target-index.rb`
  - `make audit-make-target-index`
  Outputs:
  - `docs/generated/local-tooling/make-target-index.json`
  - `docs/generated/local-tooling/make-target-index-summary.md`

- `CODEX-LOCAL-ARCHITECTURE-DRIFT-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-architecture-drift.rb`
  - `make audit-architecture-drift`
  Outputs:
  - `docs/generated/local-tooling/architecture-drift.json`
  - `docs/generated/local-tooling/architecture-drift-summary.md`

- `CODEX-LOCAL-PLAN-COMPLETION-AUDIT`
  Entrypoints:
  - `ruby scripts/audits/audit-plan-completion.rb plan=<plan-file> [manifest=<manifest-file>]`
  - `make audit-plan-completion plan=<plan-file> [manifest=<manifest-file>]`
  Outputs:
  - `docs/generated/local-tooling/plan-completion/<plan-id>.json`
  - `docs/generated/local-tooling/plan-completion/<plan-id>-summary.md`
  Notes:
  - Fails incomplete task checkboxes unless they are explicitly deferred to a stable backlog ID.
  - Fails completed manifests when their referenced plan lacks completion evidence.
  - Checks master-plan child rows when a master plan is passed as the target plan.

- `CODEX-LOCAL-CLOSEOUT-REPORT-GENERATOR`
  Entrypoints:
  - `ruby scripts/audits/generate-closeout-report.rb manifest=<manifest-file>`
  - `make closeout-report manifest=<manifest-file>`
  Outputs:
  - `docs/generated/local-tooling/closeout-reports/<feature-id>.json`
  - `docs/generated/local-tooling/closeout-reports/<feature-id>-summary.md`
  Notes:
  - Reads feature manifest evidence plus the current changed-file list.
  - Summarizes artifact groups, validation commands, docs delta, generated artifacts, backlog delta, and residual risks for final review.

- `CODEX-LOCAL-GENERATED-NOISE-FILTER`
  Entrypoints:
  - Shared filtering in `scripts/local_tooling_common.rb`
  - Applies to `make diff-summary`, `make context-pack`, `make audit-router`, and `make fast-check`
  Options:
  - `include_generated=true`
  - `include_agents=true`
  Outputs:
  - Existing report JSON and Markdown files include `original_file_count`, `filtered_file_count`, `excluded_file_count`, and `excluded_files_sample`.
  Notes:
  - Default output excludes generated reports, generated frontend contracts, frontend dist output, and transient `.agents` plan/evidence files.
  - Opt-in flags keep full audit/debug visibility when generated or agent working files are the actual review target.

- `CODEX-LOCAL-TASK-CONTEXT-BUDGETS`
  Entrypoints:
  - `make context-pack topic=<topic> budget=small|medium|large`
  - `make session-handoff topic=<topic> budget=small|medium|large`
  Outputs:
  - Existing context pack and handoff JSON/Markdown reports include `budget`, `omitted_sections`, and `read_next`.
  Notes:
  - Default budget is `small`, capped at 20 files so first-read context stays predictable.
  - `medium` caps at 50 files and `large` caps at 100 files for explicit broader review.

- `CODEX-LOCAL-SELECTIVE-GENERATED-ARTIFACT-COMMIT-GUIDE`
  Entrypoints:
  - `ruby scripts/audits/audit-generated-commit-scope.rb`
  - `make audit-generated-commit-scope`
  Outputs:
  - `docs/generated/local-tooling/generated-commit-scope.json`
  - `docs/generated/local-tooling/generated-commit-scope-summary.md`
  Notes:
  - Classifies changed generated files as `task_required`, `supporting_context`, `stale_or_unrelated`, or `do_not_commit_by_default`.
  - Uses `docs/generated/artifact-policy.yaml` as the source of truth for generated artifact review policy.

- `CODEX-LOCAL-FEATURE-SLICE-RECOMMENDER`
  Entrypoints:
  - `ruby scripts/audits/recommend-feature-slices.rb topic=<topic> [files=<csv>]`
  - `make recommend-feature-slices topic=<topic> [files=<csv>]`
  Outputs:
  - `docs/generated/local-tooling/feature-slices/<topic>.json`
  - `docs/generated/local-tooling/feature-slices/<topic>-summary.md`
  Notes:
  - Recommends backend, frontend, docs/artifact, and final-validation slices from topic-inferred or explicit files.
  - Uses existing domain/category mapping and validation routing to keep slices conservative.

- `CODEX-LOCAL-ARCHITECTURE-DECISION-INDEX`
  Entrypoints:
  - `ruby scripts/audits/generate-architecture-decision-index.rb`
  - `make architecture-decision-index`
  Outputs:
  - `docs/generated/local-tooling/architecture-decision-index.json`
  - `docs/generated/local-tooling/architecture-decision-index-summary.md`
  Notes:
  - Summarizes stable repository decisions around backend-centric logic, docs sync, generated artifacts, migrations, sandbox separation, git boundaries, plan closeout, config, and frontend shell conventions.
  - Each decision includes source paths to read before editing related areas.

- `CODEX-LOCAL-CHANGESET-RISK-SCORER`
  Entrypoints:
  - `ruby scripts/audits/score-changeset-risk.rb [files...]`
  - `make changeset-risk [files=<csv>]`
  Outputs:
  - `docs/generated/local-tooling/changeset-risk.json`
  - `docs/generated/local-tooling/changeset-risk-summary.md`
  Notes:
  - Scores controller contracts, DTO/model/schema changes, workflow or permission services, frontend contracts, agent/docs contracts, generated churn, mixed domains, and tooling changes.
  - Uses transparent weighted factors and recommends validation commands from the resulting risk tier.

## Open Items

- [x] CODEX-LOCAL-CONTEXT-PACK-GENERATOR: Generate a compact feature-context pack from a topic, changed files, or domain name so Codex can read one short report before implementation instead of rediscovering controllers, services, DTOs, tests, docs, and related generated audits.
  Proposed entrypoints:
  - `ruby scripts/audits/generate-context-pack.rb topic=<topic> [files=<csv>]`
  - `make context-pack topic=<topic> [files=<csv>]`
  Proposed outputs:
  - `docs/generated/local-tooling/context-packs/<topic>.json`
  - `docs/generated/local-tooling/context-packs/<topic>-summary.md`
  Notes:
  - Include related backend domains, frontend modules, tests, docs, migrations, Make targets, and the top 10 relevant audit findings.
  - Prefer excerpts and path lists over copying full source content.

- [x] CODEX-LOCAL-CHANGED-FILES-AUDIT-ROUTER: Add a workstation-side router that reads `git status --short`, classifies changed files, and recommends only the audits and validations relevant to the current change.
  Proposed entrypoints:
  - `ruby scripts/audits/audit-router.rb [--changed] [files...]`
  - `make audit-router`
  Proposed outputs:
  - `docs/generated/local-tooling/audit-router.json`
  - `docs/generated/local-tooling/audit-router-summary.md`
  Notes:
  - Map backend service changes to mapper, read-surface, repository-fetch, state-transition, docs-to-code, and backend tests.
  - Map frontend view/composable/API changes to route-surface, async-mutation, stale-surface, style-token, type-check, and build checks.
  - Map docs and agent artifacts to documentation, feature-closeout, and agent-operating-model validation.

- [x] CODEX-LOCAL-INCREMENTAL-AUDIT-CACHE: Cache audit inputs, mtimes, and content hashes so broad audits can skip unchanged surfaces and report when cached output is still valid.
  Proposed entrypoints:
  - Shared support in `scripts/local_tooling_common.rb`
  - Optional `make audit-local-tooling-incremental`
  Proposed outputs:
  - `docs/generated/local-tooling/.cache/audit-inputs.json`
  - Existing audit JSON and summary files with `cache_status`
  Notes:
  - This should reduce local runtime enough that Codex can routinely run more checks without spending tokens reviewing irrelevant output.
  - Keep a `--refresh` option for cases where generated artifacts or dependencies changed outside tracked source paths.

- [x] CODEX-LOCAL-COMPACT-REPO-MAP: Generate a small, stable repository map that summarizes modules, domains, controllers, services, repositories, DTO packages, frontend modules, scripts, docs, and generated artifacts.
  Proposed entrypoints:
  - `ruby scripts/audits/generate-repo-map.rb`
  - `make repo-map`
  Proposed outputs:
  - `docs/generated/local-tooling/repo-map.json`
  - `docs/generated/local-tooling/repo-map-summary.md`
  Notes:
  - Codex should be able to read this before broad work instead of scanning directory trees.
  - Include counts, primary paths, ownership/domain hints, and "read first" files for common task types.

- [x] CODEX-LOCAL-SYMBOL-INDEX: Build a lightweight symbol index for Java classes/methods and frontend exported functions/components to make impact discovery deterministic.
  Proposed entrypoints:
  - `ruby scripts/audits/generate-symbol-index.rb`
  - `make symbol-index`
  Proposed outputs:
  - `docs/generated/local-tooling/symbol-index.json`
  - `docs/generated/local-tooling/symbol-index-summary.md`
  Notes:
  - Include definition path, rough category, import/reference counts, test references, and documented feature/domain where detectable.
  - This should support context-pack generation and targeted dead-code review.

- [x] CODEX-LOCAL-ENDPOINT-CONTRACT-PACKS: Generate one compact contract pack per backend endpoint family with controller method, request/response DTOs, frontend callsites, tests, docs references, and agent-operating-model mapping.
  Proposed entrypoints:
  - `ruby scripts/audits/generate-endpoint-contract-packs.rb`
  - `make endpoint-contract-packs`
  Proposed outputs:
  - `docs/generated/local-tooling/endpoint-contract-packs/<endpoint-family>.json`
  - `docs/generated/local-tooling/endpoint-contract-packs/<endpoint-family>-summary.md`
  Notes:
  - Extend, do not replace, `CODEX-LOCAL-API-CONTRACT-DRIFT-AUDIT` and `CODEX-LOCAL-ENDPOINT-CALLSITE-LINKER`.
  - Optimize for "I am changing quest applications; what contracts must I not break?"

- [x] CODEX-LOCAL-VALIDATION-MATRIX: Generate a deterministic matrix from file categories to required commands so Codex can pick validations without rereading AGENTS.md or Makefiles every time.
  Proposed entrypoints:
  - `ruby scripts/audits/generate-validation-matrix.rb`
  - `make validation-matrix`
  Proposed outputs:
  - `docs/generated/local-tooling/validation-matrix.json`
  - `docs/generated/local-tooling/validation-matrix-summary.md`
  Notes:
  - Include backend tests, frontend type-check/build, docs validation tests, feature-closeout audit, and generated-artifact refresh targets.
  - The audit-router should consume this matrix.

- [x] `CODEX-LOCAL-CHANGESET-PLAYBOOK`
  Entrypoints:
  - `ruby scripts/audits/generate-changeset-playbook.rb [files...]`
  - `make changeset-playbook files=<csv>`
  Outputs:
  - `docs/generated/local-tooling/changeset-playbook.json`
  - `docs/generated/local-tooling/changeset-playbook-summary.md`
  Notes:
  - Combines diff shape, manifest decision, focused audit routing, likely doc targets, and validation preset guidance into one ordered workflow.
  - Intended as the main first-read report before broad repository exploration on a non-trivial changeset.

- [x] `CODEX-LOCAL-VALIDATION-PRESET-RECOMMENDER`
  Entrypoints:
  - `ruby scripts/audits/recommend-validation-preset.rb [files...]`
  - `make recommend-validation-preset files=<csv>`
  Outputs:
  - `docs/generated/local-tooling/validation-preset.json`
  - `docs/generated/local-tooling/validation-preset-summary.md`
  Notes:
  - Classifies the current changeset into deterministic presets such as `fast`, `standard-backend`, `standard-frontend`, `full-closeout`, or `manifest-required`.
  - Sits one level above targeted tests and the validation matrix so routine work does not require manual command assembly.

- [x] `CODEX-LOCAL-AUDIT-DELTA-REPORT`
  Entrypoints:
  - `ruby scripts/audits/audit-delta-report.rb audit=<audit-id>`
  - `make audit-delta-report audit=<audit-id>`
  Outputs:
  - `docs/generated/local-tooling/audit-deltas/<audit-id>.json`
  - `docs/generated/local-tooling/audit-deltas/<audit-id>-summary.md`
  Notes:
  - Compares the latest JSON report to the previous archived snapshot for the same audit target.
  - Highlights count deltas plus newly introduced or fixed risk signals so Codex can read a small delta instead of the full report.

- [x] CODEX-LOCAL-DOC-SYNC-PREFLIGHT: Add a preflight that lists which living docs and agent artifacts likely need updates for the changed code paths before Codex starts editing.
  Proposed entrypoints:
  - `ruby scripts/audits/audit-doc-sync-preflight.rb [files...]`
  - `make audit-doc-sync-preflight`
  Proposed outputs:
  - `docs/generated/local-tooling/doc-sync-preflight.json`
  - `docs/generated/local-tooling/doc-sync-preflight-summary.md`
  Notes:
  - This should explain whether `docs/business-logic.md`, `docs/domain-technical.md`, `docs/agent-operating-model.md`, `docs/agent-operating-model.yaml`, completion manifests, or persistent backlogs are likely in scope.
  - Keep it advisory and deterministic; do not ask Codex to infer product behavior from weak matches.

- [x] CODEX-LOCAL-MIGRATION-ENTITY-DRIFT-AUDIT: Compare JPA entities, enum values, validation constraints, and Flyway migrations to detect schema and domain drift before backend changes close.
  Proposed entrypoints:
  - `ruby scripts/audits/audit-migration-entity-drift.rb`
  - `make audit-migration-entity-drift`
  Proposed outputs:
  - `docs/generated/local-tooling/migration-entity-drift-audit.json`
  - `docs/generated/local-tooling/migration-entity-drift-audit-summary.md`
  Notes:
  - Flag new entity fields without migration coverage, migrations without mapped model fields, and enum/status mismatches.
  - This reduces repeated manual scanning of `model/` and `db/migration/` during backend work.

- [x] CODEX-LOCAL-TEST-GAP-RECOMMENDER: Generate targeted test recommendations from changed backend/frontend files and existing test-surface inventory.
  Proposed entrypoints:
  - `ruby scripts/audits/audit-test-gap-recommendations.rb [files...]`
  - `make audit-test-gap-recommendations`
  Proposed outputs:
  - `docs/generated/local-tooling/test-gap-recommendations.json`
  - `docs/generated/local-tooling/test-gap-recommendations-summary.md`
  Notes:
  - Report nearest existing tests, missing scenario tests, likely unit-test classes to extend, and suggested command subsets.
  - Keep recommendations path-based and explicit rather than relying on broad natural-language summaries.

- [x] CODEX-LOCAL-FRONTEND-COMPONENT-USAGE-GRAPH: Generate a component/composable/store usage graph for frontend modules so Codex can avoid reading large Vue surfaces when only call relationships are needed.
  Proposed entrypoints:
  - `ruby scripts/audits/audit-frontend-usage-graph.rb`
  - `make audit-frontend-usage-graph`
  Proposed outputs:
  - `docs/generated/local-tooling/frontend-usage-graph.json`
  - `docs/generated/local-tooling/frontend-usage-graph-summary.md`
  Notes:
  - Include views, components, composables, API clients, route registration, and imported contracts.
  - Feed stale-surface and context-pack outputs.

- [x] CODEX-LOCAL-BACKEND-DEPENDENCY-GRAPH: Generate a backend dependency graph for controllers, services, repositories, mappers, DTOs, and entities.
  Proposed entrypoints:
  - `ruby scripts/audits/audit-backend-dependency-graph.rb`
  - `make audit-backend-dependency-graph`
  Proposed outputs:
  - `docs/generated/local-tooling/backend-dependency-graph.json`
  - `docs/generated/local-tooling/backend-dependency-graph-summary.md`
  Notes:
  - Include constructor-injected dependencies, mapper usage, repository usage, transactional annotations, and test references.
  - This should support faster LazyInitializationException audits and sibling read-surface discovery.

- [x] CODEX-LOCAL-DIFF-SUMMARY-FOR-CODEX: Generate a concise summary of current uncommitted changes grouped by feature/domain, file category, and validation risk.
  Proposed entrypoints:
  - `ruby scripts/audits/generate-diff-summary.rb`
  - `make diff-summary`
  Proposed outputs:
  - `docs/generated/local-tooling/diff-summary.json`
  - `docs/generated/local-tooling/diff-summary.md`
  Notes:
  - Codex can read this at session start instead of inspecting many changed files individually.
  - Include only paths, statuses, changed symbol names where cheap, and suggested next validation commands.

- [x] CODEX-LOCAL-SESSION-HANDOFF-GENERATOR: Generate a short handoff note for interrupted or long-running work from plans, manifests, changed files, generated audit reports, and validation status.
  Proposed entrypoints:
  - `ruby scripts/audits/generate-session-handoff.rb topic=<topic>`
  - `make session-handoff topic=<topic>`
  Proposed outputs:
  - `docs/generated/local-tooling/session-handoffs/<topic>.md`
  - `docs/generated/local-tooling/session-handoffs/<topic>.json`
  Notes:
  - Include current goal, touched files, open risks, required docs, validations run/not run, and next concrete steps.
  - This should reduce token usage when a later Codex session resumes work.

- [x] CODEX-LOCAL-PLAN-SCAFFOLD-FROM-TOPIC: Extend `make bootstrap-feature-work` with optional local discovery so it can pre-fill implementation plans with likely files, audits, docs, validations, and generated-artifact updates.
  Proposed entrypoints:
  - `make bootstrap-feature-work topic=<topic> discover=true`
  - Shared implementation can consume context packs, repo map, endpoint contract packs, and validation matrix.
  Proposed outputs:
  - `.agents/<topic>-plan.md`
  - Optional `.agents/feature-manifests/<topic>-manifest.yaml`
  Notes:
  - Keep generated plan text conservative and editable.
  - Do not mark checklist items complete automatically.

- [x] CODEX-LOCAL-AUDIT-SUMMARY-INDEX: Generate one top-level index of all local audit summaries, freshness timestamps, risk counts, and direct output paths.
  Proposed entrypoints:
  - `ruby scripts/audits/generate-audit-summary-index.rb`
  - `make audit-summary-index`
  Proposed outputs:
  - `docs/generated/local-tooling/audit-summary-index.json`
  - `docs/generated/local-tooling/audit-summary-index.md`
  Notes:
  - Codex should read this first when deciding which detailed audit summaries are worth opening.
  - Include stale/missing output detection for every registered local audit.

- [x] CODEX-LOCAL-AUDIT-REGISTRY: Move local audit metadata into a single machine-readable registry and generate Make targets, documentation, and audit summary index entries from it.
  Proposed entrypoints:
  - `ruby scripts/audits/generate-audit-registry-artifacts.rb`
  - `make generate-audit-registry-artifacts`
  Proposed outputs:
  - `docs/tooling/codex-local-audits.yml`
  - Generated sections for `docs/codex-local-tooling-todo.md`, `docs/tooling/codex-local-audits.md`, and Make target checks
  Notes:
  - Avoid hand-maintaining the same audit names, entrypoints, and outputs in multiple places.
  - Keep manual TODO items separate from generated "available audits" sections.

- [x] CODEX-LOCAL-FAST-CHECK-PRESET: Add a small preset command for low-risk code changes that runs only targeted router-selected audits plus the minimum relevant compile/type/docs checks.
  Proposed entrypoints:
  - `make fast-check [files=<csv>]`
  Proposed outputs:
  - `docs/generated/local-tooling/fast-check-report.json`
  - `docs/generated/local-tooling/fast-check-report-summary.md`
  Notes:
  - This is not a replacement for full backend/frontend validation when behavior changes.
  - It should give Codex a cheap first pass before deciding whether heavier commands are justified.

- [x] CODEX-LOCAL-FAILED-VALIDATION-DIAGNOSTIC-PACK: Capture failing command output into a compact diagnostic report with failing tests, stack traces, changed-file context, and likely owning domain.
  Proposed entrypoints:
  - Wrapper scripts for `./mvnw test`, `npm run type-check`, and `npm run build`
  - `make diagnose-backend-test`, `make diagnose-frontend-type-check`, `make diagnose-frontend-build`
  Proposed outputs:
  - `docs/generated/local-tooling/diagnostics/<command>-latest.json`
  - `docs/generated/local-tooling/diagnostics/<command>-latest-summary.md`
  Notes:
  - Keep raw logs out of the main summary unless needed.
  - This reduces token usage spent reading long terminal output after failures.

- [x] CODEX-LOCAL-OPENAPI-OR-CONTRACT-SNAPSHOT: Generate a local endpoint/request/response snapshot from Spring controllers and DTOs, even before adopting a full OpenAPI generator.
  Proposed entrypoints:
  - `ruby scripts/audits/generate-api-contract-snapshot.rb`
  - `make api-contract-snapshot`
  Proposed outputs:
  - `docs/generated/local-tooling/api-contract-snapshot.json`
  - `docs/generated/local-tooling/api-contract-snapshot-summary.md`
  Notes:
  - Include method, path, auth hints where detectable, request DTO, response DTO, status codes if obvious, and frontend callsites.
  - Feed API drift and endpoint contract pack reports.

- [x] CODEX-LOCAL-DOC-CANONICAL-PHRASE-INDEX: Generate an index of protected documentation-sync phrases, required target files, and current match status.
  Proposed entrypoints:
  - `ruby scripts/audits/audit-doc-canonical-phrases.rb`
  - `make audit-doc-canonical-phrases`
  Proposed outputs:
  - `docs/generated/local-tooling/doc-canonical-phrases.json`
  - `docs/generated/local-tooling/doc-canonical-phrases-summary.md`
  Notes:
  - This gives Codex a compact source for exact protected wording instead of searching YAML and docs repeatedly.
  - Keep it aligned with `AgentOperatingModelValidationTest`.

- [x] CODEX-LOCAL-SANDBOX-DATA-COVERAGE-PACK: Generate a report that maps synthetic/admin generation paths to entities, workflows, validations, edge cases, and docs references.
  Proposed entrypoints:
  - `ruby scripts/audits/audit-sandbox-data-coverage-pack.rb`
  - `make audit-sandbox-data-coverage-pack`
  Proposed outputs:
  - `docs/generated/local-tooling/sandbox-data-coverage-pack.json`
  - `docs/generated/local-tooling/sandbox-data-coverage-pack-summary.md`
  Notes:
  - Extend sandbox-generation coverage by making the missing scenario/entity coverage more actionable.
  - Useful before adding new workflow states or domain entities.

- [x] CODEX-LOCAL-LOCAL-DB-SMOKE-PACK: Add opt-in local smoke scripts that exercise high-value authenticated endpoints against the developer database and write compact status reports.
  Proposed entrypoints:
  - `make smoke-local-authenticated`
  - `make smoke-local-dashboard`
  Proposed outputs:
  - `docs/generated/local-tooling/smoke/local-authenticated-latest.json`
  - `docs/generated/local-tooling/smoke/local-dashboard-latest.json`
  Notes:
  - Keep this explicitly opt-in because it depends on local services and data.
  - Do not mix this with deterministic static audits.

- [x] CODEX-LOCAL-CI-LIKE-CLOSEOUT-BUNDLE: Add one command that runs generated-artifact refresh, router-selected audits, required validation matrix commands, and a final feature-closeout audit when a manifest is provided.
  Proposed entrypoints:
  - `make closeout-bundle manifest=<manifest-file> [files=<csv>]`
  Proposed outputs:
  - `docs/generated/local-tooling/closeout-bundle.json`
  - `docs/generated/local-tooling/closeout-bundle-summary.md`
  Notes:
  - This should not hide failures; it should aggregate them into one readable report.
  - Useful for large multi-layer changes where Codex otherwise spends many tokens coordinating closeout steps.

- [x] CODEX-LOCAL-FAILURE-KNOWLEDGE-BASE: Capture recurring validation failures and their fixes into a compact local troubleshooting index.
  Proposed entrypoints:
  - `ruby scripts/audits/update-failure-knowledge-base.rb source=<diagnostic-report>`
  - `make failure-knowledge-base`
  Proposed outputs:
  - `docs/generated/local-tooling/failure-knowledge-base.json`
  - `docs/generated/local-tooling/failure-knowledge-base-summary.md`
  Notes:
  - This should help Codex resolve repeated Maven, frontend type-check, Flyway, and documentation validation failures without rereading long logs.
  - Keep entries factual: failure pattern, owning surface, likely cause, verified fix, and source report.

- [x] CODEX-LOCAL-PROMPTABLE-CODEBASE-CAPSULE: Generate a very small "read this first" capsule for new Codex sessions with current repo layout, active conventions, current open backlogs, and the preferred first commands.
  Proposed entrypoints:
  - `ruby scripts/audits/generate-codebase-capsule.rb`
  - `make codebase-capsule`
  Proposed outputs:
  - `docs/generated/local-tooling/codebase-capsule.md`
  - `docs/generated/local-tooling/codebase-capsule.json`
  Notes:
  - This should be intentionally shorter than repo map and audit summary index.
  - Use it as the default first read before broad work.

- [x] CODEX-LOCAL-TARGETED-TEST-MINIMIZER: Given changed files, generate the smallest high-confidence command set that covers direct unit tests, scenario tests, contract/type checks, and affected docs validation.
  Proposed entrypoints:
  - `ruby scripts/audits/recommend-targeted-tests.rb [files...]`
  - `make recommend-targeted-tests files=<csv>`
  Proposed outputs:
  - `docs/generated/local-tooling/targeted-tests.json`
  - `docs/generated/local-tooling/targeted-tests-summary.md`
  Notes:
  - This should complement, not replace, full validation for high-risk changes.
  - Include why each command was selected and which risk remains uncovered.

- [x] CODEX-LOCAL-TEST-FLAKINESS-AND-DURATION-TRACKER: Track local test durations and repeated failures so Codex can choose cheaper targeted checks and identify unstable tests.
  Proposed entrypoints:
  - Wrapper support around backend and frontend validation commands.
  - `make test-history-summary`
  Proposed outputs:
  - `docs/generated/local-tooling/test-history.json`
  - `docs/generated/local-tooling/test-history-summary.md`
  Notes:
  - Keep raw logs out of git by default.
  - Store only compact metadata: command, duration, pass/fail, failing tests, and top error patterns.

- [x] CODEX-LOCAL-FIXTURE-DUPLICATION-AUDIT: Detect repeated backend test setup patterns and recommend fixture-builder extraction candidates.
  Proposed entrypoints:
  - `ruby scripts/audits/audit-test-fixture-duplication.rb`
  - `make audit-test-fixture-duplication`
  Proposed outputs:
  - `docs/generated/local-tooling/test-fixture-duplication.json`
  - `docs/generated/local-tooling/test-fixture-duplication-summary.md`
  Notes:
  - Focus on users, circles, quests, applications, location settings, and chat setup.
  - This should support `IMPL-TEST-FIXTURE-STANDARDIZATION`.

- [x] CODEX-LOCAL-DOC-TEMPLATE-COVERAGE-AUDIT: Check whether changes that add workflows, endpoints, DTOs, migrations, permissions, or modules used the expected documentation template sections.
  Proposed entrypoints:
  - `ruby scripts/audits/audit-doc-template-coverage.rb [files...]`
  - `make audit-doc-template-coverage`
  Proposed outputs:
  - `docs/generated/local-tooling/doc-template-coverage.json`
  - `docs/generated/local-tooling/doc-template-coverage-summary.md`
  Notes:
  - This should make documentation updates repeatable without making docs verbose.
  - Feed from `AGENT-DOC-TEMPLATE-BY-CHANGE-TYPE`.

- [x] CODEX-LOCAL-CONTRACT-TEST-GAP-AUDIT: Map endpoints and DTOs to backend tests, frontend contract usage, generated contracts, and documented behavior to find missing contract checks.
  Proposed entrypoints:
  - `ruby scripts/audits/audit-contract-test-gaps.rb`
  - `make audit-contract-test-gaps`
  Proposed outputs:
  - `docs/generated/local-tooling/contract-test-gaps.json`
  - `docs/generated/local-tooling/contract-test-gaps-summary.md`
  Notes:
  - Prioritize automation-relevant and planner-visible DTOs first.
  - This should reduce silent backend/frontend drift.

- [x] CODEX-LOCAL-MUTATION-SAFETY-AUDIT: Identify mutation endpoints and services without scenario tests for permissions, invalid transitions, ownership checks, and side effects.
  Proposed entrypoints:
  - `ruby scripts/audits/audit-mutation-safety.rb`
  - `make audit-mutation-safety`
  Proposed outputs:
  - `docs/generated/local-tooling/mutation-safety.json`
  - `docs/generated/local-tooling/mutation-safety-summary.md`
  Notes:
  - Focus on high-risk operations before broad coverage.
  - Use the result to choose self-test scope before changing workflows.

- [x] CODEX-LOCAL-DOCS-AS-TESTS-AUDIT: Extract protected behavioral statements from business/domain docs and report whether corresponding tests or audit checks exist.
  Proposed entrypoints:
  - `ruby scripts/audits/audit-docs-as-tests.rb`
  - `make audit-docs-as-tests`
  Proposed outputs:
  - `docs/generated/local-tooling/docs-as-tests.json`
  - `docs/generated/local-tooling/docs-as-tests-summary.md`
  Notes:
  - Start with workflow states, permissions, visibility, and sandbox/production separation.
  - Keep extraction conservative to avoid false precision.

- [x] CODEX-LOCAL-FEATURE-CLOSEOUT-ENFORCER: Replace advisory closeout output with a local hard-fail auditor that validates manifest schema, checklist completion, validation evidence, artifact paths, duplicate artifact buckets, backlog links, and plan completion.
  Proposed entrypoints:
  - `ruby scripts/audits/enforce-feature-closeout.rb manifest=<manifest-file>`
  - `make enforce-feature-closeout manifest=<manifest-file>`
  Proposed outputs:
  - `docs/generated/local-tooling/closeout-enforcement/<feature-id>.json`
  - `docs/generated/local-tooling/closeout-enforcement/<feature-id>-summary.md`
  Required checks:
  - Fail if `status: complete` is missing for final closeout.
  - Fail if any required checklist field is false without an allowed `not_applicable` evidence record.
  - Fail if required profile commands have no validation evidence.
  - Fail if generated artifact commands are declared but output files are missing or stale.
  - Fail if the same path appears in more than one artifact group.
  - Fail if backlog created/resolved IDs do not match persistent backlog state.
  - Fail if the referenced plan is missing or lacks completion evidence.

- [x] CODEX-LOCAL-VALIDATION-EVIDENCE-RECORDER: Add a small wrapper that records validation command results into a feature manifest or companion evidence file.
  Proposed entrypoints:
  - `ruby scripts/audits/record-validation-evidence.rb manifest=<manifest-file> -- <command...>`
  - `make record-validation manifest=<manifest-file> command="<command>"`
  Proposed outputs:
  - Updated `.agents/feature-manifests/<feature-id>-manifest.yaml`
  - Optional `docs/generated/local-tooling/validation-evidence/<feature-id>.json`
  Notes:
  - Capture command, scope, result, timestamp, duration, summary, output path, and skipped reason.
  - Keep raw command logs out of git by default.
  - This should feed `CODEX-LOCAL-FEATURE-CLOSEOUT-ENFORCER`.

- [x] CODEX-LOCAL-MANIFEST-DECISION-AUDIT: Determine whether the current changeset requires a feature manifest and report the reason.
  Proposed entrypoints:
  - `ruby scripts/audits/audit-manifest-decision.rb [files...]`
  - `make audit-manifest-decision files=<csv>`
  Proposed outputs:
  - `docs/generated/local-tooling/manifest-decision.json`
  - `docs/generated/local-tooling/manifest-decision-summary.md`
  Rules:
  - Require manifest for multi-file multi-layer changes.
  - Require manifest for high-risk, executor-critical, agent-contract, workflow-expansion, frontend-contract, schema, generated-artifact, or automation-safety changes.
  - Allow skip for cosmetic or single-file contract-neutral refactors with a documented reason.

- [x] CODEX-LOCAL-DOC-SYNC-DUPLICATE-CLEANUP-AUDIT: Find duplicated protected phrases, fragment-only policy bullets, and conflicting doc-sync wording across `AGENTS.md`, documentation policy, checklist, and operating model docs.
  Proposed entrypoints:
  - `ruby scripts/audits/audit-doc-sync-duplicates.rb`
  - `make audit-doc-sync-duplicates`
  Proposed outputs:
  - `docs/generated/local-tooling/doc-sync-duplicates.json`
  - `docs/generated/local-tooling/doc-sync-duplicates-summary.md`
  Notes:
  - Must preserve exact protected canonical phrases.
  - Should recommend consolidation targets without auto-rewriting docs.

- [x] `CODEX-LOCAL-CHANGESET-PLAYBOOK`
  Entrypoints:
  - `ruby scripts/audits/generate-changeset-playbook.rb [files...]`
  - `make changeset-playbook files=<csv>`
  Outputs:
  - `docs/generated/local-tooling/changeset-playbook.json`
  - `docs/generated/local-tooling/changeset-playbook-summary.md`
  Notes:
  - Combine diff summary, audit router, validation matrix, doc-sync preflight, and manifest decision into one ordered action list.
  - Emit a deterministic "read this, then run this, then update these docs" workflow for the current changeset.
  - Primary value is simpler operator flow and fewer separate reports to read before implementation.

- [x] `CODEX-LOCAL-AUDIT-DELTA-REPORT`
  Entrypoints:
  - `ruby scripts/audits/audit-delta-report.rb audit=<audit-id>`
  - `make audit-delta-report audit=<audit-id>`
  Outputs:
  - `docs/generated/local-tooling/audit-deltas/<audit-id>.json`
  - `docs/generated/local-tooling/audit-deltas/<audit-id>-summary.md`
  Notes:
  - Compare the latest audit output with the previous saved output and highlight only newly introduced risks, fixed findings, and count deltas.
  - Reduce token usage by letting Codex read small deltas instead of re-reading full audit summaries after each change.

- [x] `CODEX-LOCAL-VALIDATION-PRESET-RECOMMENDER`
  Entrypoints:
  - `ruby scripts/audits/recommend-validation-preset.rb [files...]`
  - `make recommend-validation-preset files=<csv>`
  Outputs:
  - `docs/generated/local-tooling/validation-preset.json`
  - `docs/generated/local-tooling/validation-preset-summary.md`
  Notes:
  - Classify a changeset into presets like `fast`, `standard-backend`, `standard-frontend`, `full-closeout`, or `manifest-required`.
  - Sit one layer above targeted tests and the validation matrix so Codex does not need to assemble a command set manually on routine work.
  - Primary value is faster, more consistent validation selection with lower prompt overhead.

- [x] `CODEX-LOCAL-MANIFEST-PATH-RESOLVER`
  Entrypoints:
  - `ruby scripts/audits/resolve-manifest-path.rb [files...]`
  - `make resolve-manifest-path files=<csv>`
  Outputs:
  - `docs/generated/local-tooling/manifest-path-resolution.json`
  - `docs/generated/local-tooling/manifest-path-resolution-summary.md`
  Notes:
  - Infer the most likely feature manifest path for the current changeset from changed files, topic names, active plans, and existing manifest metadata.
  - Feed `CODEX-LOCAL-CHANGESET-PLAYBOOK`, `CODEX-LOCAL-VALIDATION-PRESET-RECOMMENDER`, and closeout commands so they can emit concrete manifest paths instead of placeholders when the mapping is deterministic.
  - Primary value is lower prompt overhead and fewer manual substitutions during routine closeout.

- [x] `CODEX-LOCAL-SYMBOL-TO-TEST-LINKER`
  Entrypoints:
  - `ruby scripts/audits/link-symbol-to-tests.rb symbol=<symbol-name>`
  - `make link-symbol-to-tests symbol=<symbol-name>`
  Outputs:
  - `docs/generated/local-tooling/symbol-test-links/<symbol-name>.json`
  - `docs/generated/local-tooling/symbol-test-links/<symbol-name>-summary.md`
  Notes:
  - Map Java classes, methods, DTOs, frontend composables, and components to direct tests, nearby scenario tests, contract checks, and regression catalog entries.
  - Primary value is faster targeted validation selection and less manual repo search after editing one symbol.

- [x] `CODEX-LOCAL-DTO-TO-ENDPOINT-TO-FRONTEND-PACK`
  Entrypoints:
  - `ruby scripts/audits/generate-dto-usage-pack.rb dto=<dto-name>`
  - `make dto-usage-pack dto=<dto-name>`
  Outputs:
  - `docs/generated/local-tooling/dto-usage-packs/<dto-name>.json`
  - `docs/generated/local-tooling/dto-usage-packs/<dto-name>-summary.md`
  Notes:
  - Build one compact pack from DTO to controller methods, frontend API usage, views, docs references, generated contracts, and tests.
  - Primary value is safer contract work with less wide scanning across backend and frontend.

- [x] `CODEX-LOCAL-WORKFLOW-SLICE-PACK`
  Entrypoints:
  - `ruby scripts/audits/generate-workflow-slice-pack.rb workflow=<workflow-id>`
  - `make workflow-slice-pack workflow=<workflow-id>`
  Outputs:
  - `docs/generated/local-tooling/workflow-slices/<workflow-id>.json`
  - `docs/generated/local-tooling/workflow-slices/<workflow-id>-summary.md`
  Notes:
  - Gather service methods, state transitions, permission gates, scenario tests, docs sections, and frontend actions for one workflow.
  - Primary value is high local-context density for workflow changes without reopening many mixed-layer files.

- [x] `CODEX-LOCAL-CONTEXT-GATEWAY-MVP`
  Entrypoints:
  - `ruby scripts/audits/codex-context.rb collect [mode=<mode>] [budget=<tokens>] [topic=<topic>] [files=<csv>]`
  - `ruby scripts/audits/codex-context.rb explain`
  - `ruby scripts/audits/codex-context.rb clean`
  - `make codex-context`
  - `make codex-context-explain`
  - `make codex-context-clean`
  Outputs:
  - `docs/generated/local-tooling/codex-context/latest.machine.json`
  - `docs/generated/local-tooling/codex-context/latest.human.md`
  - `docs/generated/local-tooling/codex-context/latest.explain.md`
  - `docs/generated/local-tooling/codex-context/packs/*.json`
  Notes:
  - Acts as a thin orchestration layer over the existing local audit stack instead of replacing those tools.
  - Centralizes ContextPack normalization, relevance scoring, budget downgrade, provenance, and compact human output.
  - Uses `docs/generated/local-tooling/codex-context/` as the repo-native writable equivalent of the requested `.codex/context/` target.

- [ ] `CODEX-LOCAL-CONTEXT-GATEWAY-JAVA-AST-DIFF`
  Notes:
  - The gateway already uses parser-backed extraction for TypeScript/JavaScript/Vue through local frontend dependencies.
  - Replace the remaining heuristic Java changed-symbol isolation with a parser-backed Java implementation.
  - Keep the pack contract stable while improving symbol precision and caller/callee extraction.

- [ ] `CODEX-LOCAL-CONTEXT-GATEWAY-POSTGRES-SCHEMA-PROVIDER`
  Notes:
  - Add a local-only schema snapshot provider with touched-table focus, FK-neighbor expansion, and sensitive-column filtering.
  - Do not expose data rows or connect to production environments.

- [ ] `CODEX-LOCAL-CONTEXT-GATEWAY-VUE-RUNTIME-BRIDGE`
  Notes:
  - Add a dev-only runtime snapshot bridge for active route, matched components, and sanitized store shapes.
  - Keep auth, cookies, localStorage, and personal data out of the payload by default.

## Operating Notes

- Prefer scripts that generate compact machine-readable output plus one short human summary.
- Prefer deterministic reports saved under `docs/generated/` so Codex can read the result instead of re-running wide scans.
- Prefer report-first tooling before auto-delete or auto-rewrite tooling.
- If a new local helper would clearly reduce repeated repo exploration, Codex may either implement it directly or add it here as a deferred item.
