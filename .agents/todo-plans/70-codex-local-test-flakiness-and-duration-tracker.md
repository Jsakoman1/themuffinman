# CODEX-LOCAL-TEST-FLAKINESS-AND-DURATION-TRACKER Plan

Source: `docs/codex-local-tooling-todo.md`
Group: `local-tooling`
Risk: `medium`
Master order: 70 of 82

## Backlog Item

Track local test durations and repeated failures so Codex can choose cheaper targeted checks and identify unstable tests.

Source notes:
  Proposed entrypoints:
  - Wrapper support around backend and frontend validation commands.
  - `make test-history-summary`
  Proposed outputs:
  - `docs/generated/local-tooling/test-history.json`
  - `docs/generated/local-tooling/test-history-summary.md`
  Notes:
  - Keep raw logs out of git by default.
  - Store only compact metadata: command, duration, pass/fail, failing tests, and top error patterns.

## Implementation Plan

- [x] Read the directly affected code, docs, generated artifacts, and prior plans before editing.
- [x] Define the smallest implementation slice that satisfies the backlog item without mixing unrelated work.
- [x] Make code, docs, generated-artifact, and test updates together when the change affects behavior or automation contracts.
- [x] Remove or narrow the persistent backlog item only when the implementation is complete and validated.
- [x] Record any intentionally deferred remainder as a new stable backlog item before closing this plan.

## Expected Validation

- [x] `ruby scripts/todo-audit.rb`
- [x] `make audit-local-tooling-incremental`

## Completion Evidence

- Status: complete
- Changed files: `scripts/audits/local_tooling_extended_tools.rb`, `scripts/audits/generate-test-history-summary.rb`, `Makefile`, `docs/codex-local-tooling-todo.md`, `docs/domain-technical.md`, generated local-tooling artifacts
- Validation evidence: `ruby -c scripts/audits/generate-test-history-summary.rb`; `ruby -c scripts/audits/local_tooling_extended_tools.rb`; `make test-history-summary`; `make diagnose-frontend-type-check`; `make generate-audit-registry-artifacts`; `ruby scripts/todo-audit.rb`; `make audit-documentation`; `make audit-local-tooling-incremental`
- Backlog update: `CODEX-LOCAL-TEST-FLAKINESS-AND-DURATION-TRACKER` marked complete in `docs/codex-local-tooling-todo.md`
- Residual risk: History is local and advisory; it only reflects commands run through diagnostic wrappers.
