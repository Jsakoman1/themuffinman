---
machine_kind: master-plan
machine_status: unknown
machine_title: Local Tooling 24 Tools Master Plan
---

# Local Tooling 24 Tools Master Plan

Purpose: implement the next batch of workstation-side Codex helper tools from `docs/codex-local-tooling-todo.md` so future sessions can use compact generated context instead of broad repo rediscovery.

## Scope

- [x] Add deterministic local scripts for the 24 new `CODEX-LOCAL-*` tooling items.
- [x] Wire scripts into root `Makefile`.
- [x] Generate compact JSON and Markdown summaries under `docs/generated/local-tooling/`.
- [x] Keep the tools report-first and safe; no automatic deletion or production data mutation.
- [x] Update local tooling documentation to show available entrypoints.

## Phase 1: Registry And Shared Foundations

- [x] Add common helper methods for paths, changed files, references, endpoint extraction, docs targets, and command summaries.
- [x] Add wrappers for new batch audit modes.
- [x] Add a machine-readable audit registry and generated registry artifact support.

## Phase 2: Context And Index Tools

- [x] Implement context pack, repo map, symbol index, backend graph, frontend graph, endpoint contract packs, API contract snapshot, diff summary, and session handoff tools.

## Phase 3: Routing, Validation, Drift, And Diagnostics

- [x] Implement audit router, validation matrix, doc sync preflight, migration/entity drift audit, test gap recommender, canonical phrase index, sandbox data coverage pack, fast-check report, failed-validation diagnostic wrappers, local smoke pack placeholders, and closeout bundle aggregation.

## Phase 4: Make Targets And Verification

- [x] Add Make targets for every new tool.
- [x] Run representative generated-report commands.
- [x] Run `make audit-make-target-index` and `make audit-documentation`.
- [x] Update this plan with validation status.

## Completion Check

- [x] New scripts execute without Ruby syntax errors.
- [x] Representative reports are generated.
- [x] Documentation and Make target index are refreshed.
- [x] Existing user changes outside this tooling batch are left untouched.

## Validation Notes

- Ran `ruby -c scripts/audits/local_tooling_extended_tools.rb`.
- Ran syntax checks for all new wrapper scripts once after creation.
- Ran representative static targets: `repo-map`, `symbol-index`, `endpoint-contract-packs`, `validation-matrix`, `audit-router`, `audit-doc-sync-preflight`, `audit-migration-entity-drift`, `audit-test-gap-recommendations`, `audit-frontend-usage-graph`, `audit-backend-dependency-graph`, `diff-summary`, `session-handoff`, `audit-summary-index`, `generate-audit-registry-artifacts`, `fast-check`, `api-contract-snapshot`, `audit-doc-canonical-phrases`, `audit-sandbox-data-coverage-pack`, `closeout-bundle`, and smoke placeholder targets.
- Ran `make audit-make-target-index audit-documentation`.
- Did not run diagnostic targets because they intentionally execute backend tests or frontend build/type-check commands.
