# CODEX-LOCAL-SYMBOL-TO-TEST-LINKER Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 87 of 92

## Backlog Item

Map one symbol to its direct and nearby test surfaces so Codex can pick focused validation without broad repo search.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make link-symbol-to-tests symbol=QuestService`
- [x] `make generate-audit-registry-artifacts`
- [x] `make audit-plan-completion plan=.agents/todo-plans/87-codex-local-symbol-to-test-linker.md`

## Completion Evidence

- Status: complete
- Changed files:
  - `scripts/audits/link-symbol-to-tests.rb`
  - `scripts/audits/local_tooling_extended_tools.rb`
  - `Makefile`
  - `docs/codex-local-tooling-todo.md`
  - `docs/domain-technical.md`
  - `docs/generated/local-tooling/symbol-test-links/questservice.json`
  - `docs/generated/local-tooling/symbol-test-links/questservice-summary.md`
- Validation evidence:
  - `ruby -c scripts/audits/local_tooling_extended_tools.rb` passed.
  - `ruby -c scripts/audits/link-symbol-to-tests.rb` passed.
  - `make link-symbol-to-tests symbol=QuestService` passed and wrote the symbol-test-links JSON and summary.
  - `make generate-audit-registry-artifacts` passed with 54 registered audits.
  - `make audit-local-tooling-incremental` passed and included `make link-symbol-to-tests symbol=QuestService`.
  - `ruby scripts/todo-audit.rb` passed.
  - `make audit-plan-completion plan=.agents/todo-plans/87-codex-local-symbol-to-test-linker.md` passed.
- Backlog update: `CODEX-LOCAL-SYMBOL-TO-TEST-LINKER` is now marked complete in `docs/codex-local-tooling-todo.md` and listed in the available audits section.
- Residual risk: first version links by symbol reference and nearby-domain heuristics; it is intentionally conservative and may miss indirect test coverage hidden behind broader scenario fixtures.
