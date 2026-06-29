# CODEX-LOCAL-MUTATION-SAFETY-AUDIT Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 75 of 82

## Backlog Item

Identify mutation endpoints and services without scenario tests for permissions, invalid transitions, ownership checks, and side effects.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/audit-mutation-safety.rb`
  - `make audit-mutation-safety`
  Proposed outputs:
  - `docs/generated/local-tooling/mutation-safety.json`
  - `docs/generated/local-tooling/mutation-safety-summary.md`
  Notes:
  - Focus on high-risk operations before broad coverage.
  - Use the result to choose self-test scope before changing workflows.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-agent-safety`
- [x] `make audit-local-tooling-incremental`
- [x] `cd apps/themuffinman && ./mvnw test`

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/audit-mutation-safety.rb`, `Makefile`, `scripts/audits/local_tooling_extended_tools.rb`, `docs/domain-technical.md`, `docs/codex-local-tooling-todo.md`, generated local-tooling outputs, registry artifacts
- Validation evidence: `ruby -c scripts/audits/audit-mutation-safety.rb` passed; `make audit-mutation-safety` passed with 106 mutation surfaces scanned, 0 review-needed rows; `make generate-audit-registry-artifacts` passed with 44 registered audits; `ruby scripts/todo-audit.rb` passed with 0 open backlog items and 0 inline TODO/FIXME references; `make audit-agent-safety` passed including 96 backend target tests, frontend type-check, admin-agent UI validation, frontend build, validation evidence quality, and todo audit; `make audit-local-tooling-incremental` passed and included `make audit-mutation-safety`; `cd apps/themuffinman && ./mvnw test` passed with 273 tests, 0 failures, 0 errors.
- Backlog update: `CODEX-LOCAL-MUTATION-SAFETY-AUDIT` marked complete in `docs/codex-local-tooling-todo.md`
- Residual risk: audit is static and advisory; it identifies review candidates but does not prove semantic test completeness.
