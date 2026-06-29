# CODEX-LOCAL-DOC-TEMPLATE-COVERAGE-AUDIT Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 73 of 82

## Backlog Item

Check whether changes that add workflows, endpoints, DTOs, migrations, permissions, or modules used the expected documentation template sections.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/audit-doc-template-coverage.rb [files...]`
  - `make audit-doc-template-coverage`
  Proposed outputs:
  - `docs/generated/local-tooling/doc-template-coverage.json`
  - `docs/generated/local-tooling/doc-template-coverage-summary.md`
  Notes:
  - This should make documentation updates repeatable without making docs verbose.
  - Feed from `AGENT-DOC-TEMPLATE-BY-CHANGE-TYPE`.

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
- Changed files: `scripts/audits/audit-doc-template-coverage.rb`, `scripts/audits/local_tooling_extended_tools.rb`, `Makefile`, `docs/codex-local-tooling-todo.md`, `docs/domain-technical.md`, generated local-tooling artifacts
- Validation evidence: `ruby -c scripts/audits/audit-doc-template-coverage.rb`; `make audit-doc-template-coverage files=apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/controller/QuestController.java,apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/dto/QuestResponseDTO.java,apps/themuffinman/src/main/resources/db/migration/V1__init.sql`; `make generate-audit-registry-artifacts`; `make audit-documentation`; `ruby scripts/todo-audit.rb`; `make audit-local-tooling-incremental`; `cd apps/themuffinman && ./mvnw test` (273 tests, 0 failures)
- Backlog update: `CODEX-LOCAL-DOC-TEMPLATE-COVERAGE-AUDIT` marked complete in `docs/codex-local-tooling-todo.md`
- Residual risk: Section detection is advisory and checks signals, not prose quality.
