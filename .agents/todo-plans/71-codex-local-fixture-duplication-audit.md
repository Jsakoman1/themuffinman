# CODEX-LOCAL-FIXTURE-DUPLICATION-AUDIT Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 71 of 82

## Backlog Item

Detect repeated backend test setup patterns and recommend fixture-builder extraction candidates.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/audit-test-fixture-duplication.rb`
  - `make audit-test-fixture-duplication`
  Proposed outputs:
  - `docs/generated/local-tooling/test-fixture-duplication.json`
  - `docs/generated/local-tooling/test-fixture-duplication-summary.md`
  Notes:
  - Focus on users, circles, quests, applications, location settings, and chat setup.
  - This should support `IMPL-TEST-FIXTURE-STANDARDIZATION`.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-local-tooling-incremental`
- [x] `cd apps/themuffinman && ./mvnw test`
- [x] `cd apps/themuffinman/frontend && npm run type-check`
- [x] `cd apps/themuffinman/frontend && npm run build`

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/audit-test-fixture-duplication.rb`, `scripts/audits/local_tooling_extended_tools.rb`, `Makefile`, `docs/codex-local-tooling-todo.md`, `docs/domain-technical.md`, generated local-tooling artifacts
- Validation evidence: `ruby -c scripts/audits/audit-test-fixture-duplication.rb`; `make audit-test-fixture-duplication`; `make generate-audit-registry-artifacts`; `ruby scripts/todo-audit.rb`; `make audit-local-tooling-incremental`; `cd apps/themuffinman && ./mvnw test` (273 tests, 0 failures); `cd apps/themuffinman/frontend && npm run type-check`; `cd apps/themuffinman/frontend && npm run build`
- Backlog update: `CODEX-LOCAL-FIXTURE-DUPLICATION-AUDIT` marked complete in `docs/codex-local-tooling-todo.md`
- Residual risk: Static pattern matching is advisory and may miss semantic fixture duplication with different names.
