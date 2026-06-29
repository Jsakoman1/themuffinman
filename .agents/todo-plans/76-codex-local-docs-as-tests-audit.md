# CODEX-LOCAL-DOCS-AS-TESTS-AUDIT Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 76 of 82

## Backlog Item

Extract protected behavioral statements from business/domain docs and report whether corresponding tests or audit checks exist.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/audit-docs-as-tests.rb`
  - `make audit-docs-as-tests`
  Proposed outputs:
  - `docs/generated/local-tooling/docs-as-tests.json`
  - `docs/generated/local-tooling/docs-as-tests-summary.md`
  Notes:
  - Start with workflow states, permissions, visibility, and sandbox/production separation.
  - Keep extraction conservative to avoid false precision.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-documentation`
- [x] `make audit-local-tooling-incremental`
- [x] `cd apps/themuffinman && ./mvnw test`

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/audit-docs-as-tests.rb`, `Makefile`, `scripts/audits/local_tooling_extended_tools.rb`, `docs/domain-technical.md`, `docs/codex-local-tooling-todo.md`, generated docs-as-tests and registry artifacts
- Validation evidence: `ruby -c scripts/audits/audit-docs-as-tests.rb` passed; `make audit-docs-as-tests` passed with 148 statements scanned, 0 review-needed rows; `make generate-audit-registry-artifacts` passed with 45 registered audits; `ruby scripts/todo-audit.rb` passed with 0 open backlog items and 0 inline TODO/FIXME references; `make audit-documentation` passed with 89 targets; `make audit-local-tooling-incremental` passed and included `make audit-docs-as-tests`; `cd apps/themuffinman && ./mvnw test` passed with 273 tests, 0 failures, 0 errors.
- Backlog update: `CODEX-LOCAL-DOCS-AS-TESTS-AUDIT` marked complete in `docs/codex-local-tooling-todo.md`
- Residual risk: audit is static and advisory; evidence matches route review and do not prove semantic behavior coverage.
