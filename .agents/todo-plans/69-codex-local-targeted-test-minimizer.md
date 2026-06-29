# CODEX-LOCAL-TARGETED-TEST-MINIMIZER Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 69 of 82

## Backlog Item

Given changed files, generate the smallest high-confidence command set that covers direct unit tests, scenario tests, contract/type checks, and affected docs validation.

Source notes:
  Proposed entrypoints:
  - `ruby scripts/audits/recommend-targeted-tests.rb [files...]`
  - `make recommend-targeted-tests files=<csv>`
  Proposed outputs:
  - `docs/generated/local-tooling/targeted-tests.json`
  - `docs/generated/local-tooling/targeted-tests-summary.md`
  Notes:
  - This should complement, not replace, full validation for high-risk changes.
  - Include why each command was selected and which risk remains uncovered.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-documentation`
- [x] `make audit-agent-safety`
- [x] `make audit-local-tooling-incremental`
- [x] `cd apps/themuffinman && ./mvnw test`

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/local_tooling_extended_tools.rb`, `scripts/audits/recommend-targeted-tests.rb`, `Makefile`, `docs/codex-local-tooling-todo.md`, `docs/domain-technical.md`, generated local-tooling artifacts
- Validation evidence: `ruby -c scripts/audits/recommend-targeted-tests.rb`; `ruby -c scripts/audits/local_tooling_extended_tools.rb`; `make recommend-targeted-tests files=apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/service/QuestService.java,apps/themuffinman/frontend/src/modules/workmarket/pages/QuestsPage.vue,docs/domain-technical.md`; `make generate-audit-registry-artifacts`; `ruby scripts/todo-audit.rb`; `make audit-documentation`; `make audit-agent-safety`; `make audit-local-tooling-incremental`; `cd apps/themuffinman && ./mvnw test` (273 tests, 0 failures)
- Backlog update: `CODEX-LOCAL-TARGETED-TEST-MINIMIZER` marked complete in `docs/codex-local-tooling-todo.md`
- Residual risk: Recommendations are report-only and do not replace full validation for high-risk backend or cross-domain changes.
