---
machine_kind: master-plan
machine_status: unknown
machine_title: TODO System Master Plan
---

# TODO System Master Plan

Goal: complete every open item from the persistent TODO/backlog system in a controlled sequence, with one child plan per open item and a final master closeout pass.

Scope sources:
- `docs/implementation-backlog.md`
- `docs/agent-improvement-backlog.md`
- `docs/codex-local-tooling-todo.md`

Execution rules:
- Work child plans sequentially in the order listed below unless an earlier plan creates a dependency that must be handled immediately.
- Do not commit or push unless the user explicitly asks.
- Remove completed backlog items from their persistent backlog source in the same change that completes them.
- Keep business and technical docs synchronized for behavior, workflow, permission, DTO, schema, or automation changes.
- If a child plan proves too broad for one safe pass, split it into smaller implementation phases inside that child plan and record any deferred remainder with a stable backlog ID.
- Close this master plan only after child plan status, persistent backlogs, generated artifacts, and validation evidence agree.

Up-front approval assessment:
- No approval is currently required for workspace file edits, plan generation, local Ruby audits, Maven tests, frontend type-check, or frontend build.
- If dependency downloads or local smoke tests require network, DB, or process access beyond the sandbox, request escalation at the failing command with the narrowest command prefix.
- Local dev server and DB smoke tests are optional unless a child plan changes runtime flows that cannot be validated deterministically otherwise.

Child plan inventory:
- [x] 01 `IMPL-QUEST-APPLICATION-DTO-AUDIT` (implementation, high risk): `.agents/todo-plans/01-impl-quest-application-dto-audit.md`
- [x] 02 `IMPL-BUSINESS-HUB-MODULE` (implementation, high risk): `.agents/todo-plans/02-impl-business-hub-module.md`
- [x] 03 `IMPL-THING-SHARING-MODULE` (implementation, high risk): `.agents/todo-plans/03-impl-thing-sharing-module.md`
- [x] 04 `IMPL-CAR-SHARING-MODULE` (implementation, high risk): `.agents/todo-plans/04-impl-car-sharing-module.md`
- [x] 05 `IMPL-SHARED-CHAT-MODULE-SURFACE` (implementation, high risk): `.agents/todo-plans/05-impl-shared-chat-module-surface.md`
- [x] 06 `IMPL-WORKMARKET-USE-CASE-BOUNDARIES` (implementation, high risk): `.agents/todo-plans/06-impl-workmarket-use-case-boundaries.md`
- [x] 07 `IMPL-READ-MODEL-ASSEMBLY-STANDARDIZATION` (implementation, medium risk): `.agents/todo-plans/07-impl-read-model-assembly-standardization.md`
- [x] 08 `IMPL-FRONTEND-THIN-STATE-SURFACES` (implementation, medium risk): `.agents/todo-plans/08-impl-frontend-thin-state-surfaces.md`
- [x] 09 `IMPL-GENERATED-ARTIFACT-SCOPE-CLEANUP` (implementation, medium risk): `.agents/todo-plans/09-impl-generated-artifact-scope-cleanup.md`
- [x] 10 `IMPL-DOMAIN-MODULE-README-CAPSULES` (implementation, high risk): `.agents/todo-plans/10-impl-domain-module-readme-capsules.md`
- [x] 11 `IMPL-TEST-FIXTURE-STANDARDIZATION` (implementation, medium risk): `.agents/todo-plans/11-impl-test-fixture-standardization.md`
- [x] 12 `IMPL-DOMAIN-APPLICATION-SERVICE-LAYERING` (implementation, medium risk): `.agents/todo-plans/12-impl-domain-application-service-layering.md`
- [x] 13 `IMPL-PERMISSION-POLICY-CENTRALIZATION` (implementation, high risk): `.agents/todo-plans/13-impl-permission-policy-centralization.md`
- [x] 14 `IMPL-WORKFLOW-STATE-MACHINE-CATALOG` (implementation, high risk): `.agents/todo-plans/14-impl-workflow-state-machine-catalog.md`
- [x] 15 `IMPL-READ-QUERY-FETCH-PROFILES` (implementation, medium risk): `.agents/todo-plans/15-impl-read-query-fetch-profiles.md`
- [x] 16 `IMPL-BACKEND-PREPARED-UI-SECTIONS` (implementation, medium risk): `.agents/todo-plans/16-impl-backend-prepared-ui-sections.md`
- [x] 17 `IMPL-CROSS-MODULE-CORE-CONCEPTS-PACKAGE` (implementation, high risk): `.agents/todo-plans/17-impl-cross-module-core-concepts-package.md`
- [x] 18 `IMPL-FRONTEND-APP-SHELL-CONSOLIDATION` (implementation, medium risk): `.agents/todo-plans/18-impl-frontend-app-shell-consolidation.md`
- [x] 19 `IMPL-API-CONTRACT-GENERATION-PIPELINE` (implementation, high risk): `.agents/todo-plans/19-impl-api-contract-generation-pipeline.md`
- [x] 20 `IMPL-DOMAIN-EVENT-BOUNDARIES` (implementation, high risk): `.agents/todo-plans/20-impl-domain-event-boundaries.md`
- [x] 21 `IMPL-ADMIN-SANDBOX-SEPARATION` (implementation, high risk): `.agents/todo-plans/21-impl-admin-sandbox-separation.md`
- [x] 22 `AGENT-SOCIAL-DTO-REQUEST-RELATION` (agent-improvement, medium risk): `.agents/todo-plans/22-agent-social-dto-request-relation.md`
- [x] 23 `AGENT-SOCIAL-DTO-OVERVIEW-MEMBER` (agent-improvement, medium risk): `.agents/todo-plans/23-agent-social-dto-overview-member.md`
- [x] 24 `AGENT-SOCIAL-DTO-SEARCH-CONTACT` (agent-improvement, medium risk): `.agents/todo-plans/24-agent-social-dto-search-contact.md`
- [x] 25 `AGENT-SOCIAL-DTO-ADMIN-CIRCLE` (agent-improvement, medium risk): `.agents/todo-plans/25-agent-social-dto-admin-circle.md`
- [x] 26 `AGENT-WORKMARKET-DASHBOARD-DTO` (agent-improvement, medium risk): `.agents/todo-plans/26-agent-workmarket-dashboard-dto.md`
- [x] 27 `AGENT-WORKMARKET-QUEST-DETAIL-DTO` (agent-improvement, medium risk): `.agents/todo-plans/27-agent-workmarket-quest-detail-dto.md`
- [x] 28 `AGENT-WORKMARKET-APPLICATION-DETAIL-DTO` (agent-improvement, medium risk): `.agents/todo-plans/28-agent-workmarket-application-detail-dto.md`
- [x] 29 `AGENT-WORKMARKET-LIST-SEARCH-OPTIONS-DTO` (agent-improvement, medium risk): `.agents/todo-plans/29-agent-workmarket-list-search-options-dto.md`
- [x] 30 `AGENT-AUTOMATION-RELEVANT-SERVICE-COVERAGE` (agent-improvement, medium risk): `.agents/todo-plans/30-agent-automation-relevant-service-coverage.md`
- [x] 31 `AGENT-RULE-SCOPED-READ-MODEL-SLICES` (agent-improvement, low risk): `.agents/todo-plans/31-agent-rule-scoped-read-model-slices.md`
- [x] 32 `AGENT-OWNERSHIP-AWARE-SOURCE-REPORTING` (agent-improvement, medium risk): `.agents/todo-plans/32-agent-ownership-aware-source-reporting.md`
- [x] 33 `AGENT-WORKFLOW-AWARE-FRONTEND-HELPERS` (agent-improvement, medium risk): `.agents/todo-plans/33-agent-workflow-aware-frontend-helpers.md`
- [x] 34 `AGENT-USE-CASE-CONTRACT-HARNESS` (agent-improvement, medium risk): `.agents/todo-plans/34-agent-use-case-contract-harness.md`
- [x] 35 `AGENT-DOC-TO-RUNTIME-SEMANTIC-CHECKS` (agent-improvement, low risk): `.agents/todo-plans/35-agent-doc-to-runtime-semantic-checks.md`
- [x] 36 `AGENT-BROADER-PLANNER-DTO-REGISTRATION` (agent-improvement, medium risk): `.agents/todo-plans/36-agent-broader-planner-dto-registration.md`
- [x] 37 `AGENT-CHANGESET-SCOPE-GUARDRAILS` (agent-improvement, medium risk): `.agents/todo-plans/37-agent-changeset-scope-guardrails.md`
- [x] 38 `AGENT-GENERATED-ARTIFACT-POLICY-TIGHTENING` (agent-improvement, medium risk): `.agents/todo-plans/38-agent-generated-artifact-policy-tightening.md`
- [x] 39 `AGENT-CONTEXT-FIRST-WORKFLOW` (agent-improvement, medium risk): `.agents/todo-plans/39-agent-context-first-workflow.md`
- [x] 40 `AGENT-IMPLEMENTATION-SLICE-CHECKPOINTS` (agent-improvement, medium risk): `.agents/todo-plans/40-agent-implementation-slice-checkpoints.md`
- [x] 41 `AGENT-DOCS-OWNERSHIP-MATRIX` (agent-improvement, medium risk): `.agents/todo-plans/41-agent-docs-ownership-matrix.md`
- [x] 42 `AGENT-VALIDATION-EVIDENCE-MANIFEST` (agent-improvement, high risk): `.agents/todo-plans/42-agent-validation-evidence-manifest.md`
- [x] 43 `AGENT-BACKLOG-TO-CODE-TRACEABILITY` (agent-improvement, high risk): `.agents/todo-plans/43-agent-backlog-to-code-traceability.md`
- [x] 44 `AGENT-CROSS-DOMAIN-CONCEPT-GLOSSARY` (agent-improvement, low risk): `.agents/todo-plans/44-agent-cross-domain-concept-glossary.md`
- [x] 45 `AGENT-DOC-TEMPLATE-BY-CHANGE-TYPE` (agent-improvement, high risk): `.agents/todo-plans/45-agent-doc-template-by-change-type.md`
- [x] 46 `AGENT-DOC-DELTA-SUMMARY-REQUIRED` (agent-improvement, medium risk): `.agents/todo-plans/46-agent-doc-delta-summary-required.md`
- [x] 47 `AGENT-DOC-STALENESS-SCORING` (agent-improvement, medium risk): `.agents/todo-plans/47-agent-doc-staleness-scoring.md`
- [x] 48 `AGENT-EXAMPLE-SCENARIO-LIBRARY` (agent-improvement, medium risk): `.agents/todo-plans/48-agent-example-scenario-library.md`
- [x] 49 `AGENT-SELF-TEST-MATRIX-BY-RISK` (agent-improvement, medium risk): `.agents/todo-plans/49-agent-self-test-matrix-by-risk.md`
- [x] 50 `AGENT-TEST-EVIDENCE-QUALITY-GATES` (agent-improvement, medium risk): `.agents/todo-plans/50-agent-test-evidence-quality-gates.md`
- [x] 51 `AGENT-REGRESSION-SCENARIO-CATALOG` (agent-improvement, medium risk): `.agents/todo-plans/51-agent-regression-scenario-catalog.md`
- [x] 52 `AGENT-DOCS-AS-CONTRACT-SLICES` (agent-improvement, medium risk): `.agents/todo-plans/52-agent-docs-as-contract-slices.md`
- [x] 53 `AGENT-ARCHITECTURE-DRIFT-REVIEW` (agent-improvement, medium risk): `.agents/todo-plans/53-agent-architecture-drift-review.md`
- [x] 54 `AGENT-POST-MERGE-LEARNING-LOOP` (agent-improvement, medium risk): `.agents/todo-plans/54-agent-post-merge-learning-loop.md`
- [x] 55 `AGENT-FEATURE-CLOSEOUT-HARD-ENFORCEMENT` (agent-improvement, high risk): `.agents/todo-plans/55-agent-feature-closeout-hard-enforcement.md`
- [x] 56 `AGENT-MANDATORY-MANIFEST-DECISION-GATE` (agent-improvement, high risk): `.agents/todo-plans/56-agent-mandatory-manifest-decision-gate.md`
- [x] 57 `AGENT-VALIDATION-EVIDENCE-SCHEMA` (agent-improvement, high risk): `.agents/todo-plans/57-agent-validation-evidence-schema.md`
- [x] 58 `AGENT-DOC-SYNC-POLICY-CLEANUP` (agent-improvement, high risk): `.agents/todo-plans/58-agent-doc-sync-policy-cleanup.md`
- [x] 59 `AGENT-PLAN-COMPLETION-ENFORCEMENT` (agent-improvement, high risk): `.agents/todo-plans/59-agent-plan-completion-enforcement.md`
- [x] 60 `AGENT-CLOSEOUT-REPORT-GENERATION` (agent-improvement, high risk): `.agents/todo-plans/60-agent-closeout-report-generation.md`
- [x] 61 `CODEX-LOCAL-GENERATED-NOISE-FILTER` (local-tooling, medium risk): `.agents/todo-plans/61-codex-local-generated-noise-filter.md`
- [x] 62 `CODEX-LOCAL-TASK-CONTEXT-BUDGETS` (local-tooling, medium risk): `.agents/todo-plans/62-codex-local-task-context-budgets.md`
- [x] 63 `CODEX-LOCAL-SELECTIVE-GENERATED-ARTIFACT-COMMIT-GUIDE` (local-tooling, medium risk): `.agents/todo-plans/63-codex-local-selective-generated-artifact-commit-guide.md`
- [x] 64 `CODEX-LOCAL-FEATURE-SLICE-RECOMMENDER` (local-tooling, medium risk): `.agents/todo-plans/64-codex-local-feature-slice-recommender.md`
- [x] 65 `CODEX-LOCAL-ARCHITECTURE-DECISION-INDEX` (local-tooling, medium risk): `.agents/todo-plans/65-codex-local-architecture-decision-index.md`
- [x] 66 `CODEX-LOCAL-CHANGESET-RISK-SCORER` (local-tooling, medium risk): `.agents/todo-plans/66-codex-local-changeset-risk-scorer.md`
- [x] 67 `CODEX-LOCAL-FAILURE-KNOWLEDGE-BASE` (local-tooling, low risk): `.agents/todo-plans/67-codex-local-failure-knowledge-base.md`
- [x] 68 `CODEX-LOCAL-PROMPTABLE-CODEBASE-CAPSULE` (local-tooling, low risk): `.agents/todo-plans/68-codex-local-promptable-codebase-capsule.md`
- [x] 69 `CODEX-LOCAL-TARGETED-TEST-MINIMIZER` (local-tooling, medium risk): `.agents/todo-plans/69-codex-local-targeted-test-minimizer.md`
- [x] 70 `CODEX-LOCAL-TEST-FLAKINESS-AND-DURATION-TRACKER` (local-tooling, medium risk): `.agents/todo-plans/70-codex-local-test-flakiness-and-duration-tracker.md`
- [x] 71 `CODEX-LOCAL-FIXTURE-DUPLICATION-AUDIT` (local-tooling, medium risk): `.agents/todo-plans/71-codex-local-fixture-duplication-audit.md`
- [x] 72 `CODEX-LOCAL-ARCHITECTURE-DRIFT-AUDIT` (local-tooling, medium risk): `.agents/todo-plans/72-codex-local-architecture-drift-audit.md`
- [x] 73 `CODEX-LOCAL-DOC-TEMPLATE-COVERAGE-AUDIT` (local-tooling, medium risk): `.agents/todo-plans/73-codex-local-doc-template-coverage-audit.md`
- [x] 74 `CODEX-LOCAL-CONTRACT-TEST-GAP-AUDIT` (local-tooling, medium risk): `.agents/todo-plans/74-codex-local-contract-test-gap-audit.md`
- [x] 75 `CODEX-LOCAL-MUTATION-SAFETY-AUDIT` (local-tooling, medium risk): `.agents/todo-plans/75-codex-local-mutation-safety-audit.md`
- [x] 76 `CODEX-LOCAL-DOCS-AS-TESTS-AUDIT` (local-tooling, medium risk): `.agents/todo-plans/76-codex-local-docs-as-tests-audit.md`
- [x] 77 `CODEX-LOCAL-FEATURE-CLOSEOUT-ENFORCER` (local-tooling, high risk): `.agents/todo-plans/77-codex-local-feature-closeout-enforcer.md`
- [x] 78 `CODEX-LOCAL-VALIDATION-EVIDENCE-RECORDER` (local-tooling, high risk): `.agents/todo-plans/78-codex-local-validation-evidence-recorder.md`
- [x] 79 `CODEX-LOCAL-MANIFEST-DECISION-AUDIT` (local-tooling, high risk): `.agents/todo-plans/79-codex-local-manifest-decision-audit.md`
- [x] 80 `CODEX-LOCAL-PLAN-COMPLETION-AUDIT` (local-tooling, high risk): `.agents/todo-plans/80-codex-local-plan-completion-audit.md`
- [x] 81 `CODEX-LOCAL-DOC-SYNC-DUPLICATE-CLEANUP-AUDIT` (local-tooling, medium risk): `.agents/todo-plans/81-codex-local-doc-sync-duplicate-cleanup-audit.md`
- [x] 82 `CODEX-LOCAL-CLOSEOUT-REPORT-GENERATOR` (local-tooling, medium risk): `.agents/todo-plans/82-codex-local-closeout-report-generator.md`

Execution phases:
- [x] Phase 1: implementation backlog child plans 01-21.
- [x] Phase 2: agent-improvement backlog child plans 22-60.
- [x] Phase 3: local-tooling backlog child plans 61-82.
- [x] Phase 4: regenerate required docs/artifacts and run final validation.
- [x] Phase 5: master closeout audit confirms every child plan is complete or explicitly deferred with a new stable backlog ID.

## Completion Evidence
- Status: complete
- Generated child plans: 82
- Final validation evidence: Final master pass regenerated audit registry artifacts with 49 registered audits; `ruby scripts/todo-audit.rb` passed with 0 open backlog items and 0 inline TODO/FIXME references; frontend `npm run type-check` passed; frontend `npm run build` passed and validated generated contracts; `make audit-agent-safety` passed including 96 backend target tests, frontend type-check, admin-agent UI validation, frontend build, validation evidence quality, and todo audit; `make audit-local-tooling-incremental` passed with 93 Make targets; `cd apps/themuffinman && ./mvnw test` passed with 273 tests, 0 failures, 0 errors; `make audit-plan-completion plan=.agents/todo-master-plan.md` passed with 0 open tasks and 0 issues.
- Backlog delta: all persistent backlog items from the master inventory are closed; `docs/implementation-backlog.md`, `docs/agent-improvement-backlog.md`, and `docs/codex-local-tooling-todo.md` contain 0 open backlog items.
- Residual risks: local audit reports remain report-first for cleanup candidates, generated commit scope, doc-template coverage, doc-sync duplicates, stale frontend surfaces, and other advisory review queues; no unresolved child plan task or inline TODO/FIXME remains.
